package com.example.ui.viewmodel

import android.app.Application
import android.app.admin.DevicePolicyManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.SecurityAuditLog
import com.example.data.repository.AuditRepository
import com.example.manager.DeviceSecurityDetails
import com.example.manager.DeviceSecurityManager
import com.example.manager.PasswordPolicyConfig
import com.example.model.EnterprisePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isAdminActive: Boolean = false,
    val isScreenCaptureDisabled: Boolean = false,
    val isInAppFlagSecureEnabled: Boolean = false,
    val isPasswordRequirementEnabled: Boolean = true,
    val passwordQuality: Int = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC,
    val passwordMinLength: Int = 4,
    val maxFailedAttempts: Int = 5,
    val expirationDays: Int = 0,
    val historyLength: Int = 0,
    val inactivityLockSeconds: Int = 60,
    val isPasswordSufficient: Boolean = true,
    val deviceDetails: DeviceSecurityDetails? = null,
    val bannerMessage: String? = null,
    val bannerSeverity: String = "INFO" // INFO, SUCCESS, ERROR
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val securityManager = DeviceSecurityManager(application)
    private val auditRepository: AuditRepository

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val auditLogs: StateFlow<List<SecurityAuditLog>>

    init {
        val db = AppDatabase.getDatabase(application)
        auditRepository = AuditRepository(db.auditLogDao())

        auditLogs = auditRepository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        refreshSecurityState()
    }

    fun refreshSecurityState() {
        val adminActive = securityManager.isAdminActive()
        val screenCaptureDisabled = securityManager.getScreenCaptureDisabled()
        val quality = securityManager.getPasswordQuality()
        val minLen = securityManager.getPasswordMinimumLength()
        val maxFailed = securityManager.getMaximumFailedPasswordsForWipe()
        val sufficient = securityManager.isPasswordSufficient()
        val details = securityManager.getDeviceSecurityDetails()

        val isPwEnabled = adminActive && quality != DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED

        _uiState.update { current ->
            current.copy(
                isAdminActive = adminActive,
                isScreenCaptureDisabled = screenCaptureDisabled,
                isInAppFlagSecureEnabled = screenCaptureDisabled || current.isInAppFlagSecureEnabled,
                isPasswordRequirementEnabled = if (adminActive) isPwEnabled else current.isPasswordRequirementEnabled,
                passwordQuality = if (adminActive && quality != DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED) quality else current.passwordQuality,
                passwordMinLength = if (adminActive && minLen > 0) minLen else current.passwordMinLength,
                maxFailedAttempts = if (adminActive && maxFailed > 0) maxFailed else current.maxFailedAttempts,
                isPasswordSufficient = sufficient,
                deviceDetails = details
            )
        }
    }

    fun toggleScreenCapturePolicy(
        disableCapture: Boolean,
        onWindowFlagChange: (Boolean) -> Unit
    ) {
        if (!_uiState.value.isAdminActive) {
            _uiState.update {
                it.copy(
                    bannerMessage = "Device Administrator privileges required to enforce system-wide screen capture blocking.",
                    bannerSeverity = "ERROR"
                )
            }
            return
        }

        val result = securityManager.setScreenCaptureDisabled(disableCapture)
        result.fold(
            onSuccess = {
                _uiState.update {
                    it.copy(
                        isScreenCaptureDisabled = disableCapture,
                        isInAppFlagSecureEnabled = disableCapture,
                        bannerMessage = if (disableCapture) {
                            "Screen Recording & Screenshots are now BLOCKED system-wide."
                        } else {
                            "Screen Recording & Screenshots are now ALLOWED."
                        },
                        bannerSeverity = if (disableCapture) "SUCCESS" else "INFO"
                    )
                }
                onWindowFlagChange(disableCapture)

                viewModelScope.launch {
                    auditRepository.logEvent(
                        action = if (disableCapture) "Screen Capture Disabled" else "Screen Capture Enabled",
                        details = if (disableCapture) {
                            "System-wide screenshot and recording blocking enforced by enterprise policy"
                        } else {
                            "Screen capture restrictions relaxed"
                        },
                        category = "SCREEN_POLICY",
                        severity = if (disableCapture) "SUCCESS" else "WARNING"
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        bannerMessage = "Failed to update screen policy: ${error.localizedMessage}",
                        bannerSeverity = "ERROR"
                    )
                }
            }
        )
    }

    fun toggleInAppFlagSecure(
        enableFlagSecure: Boolean,
        onWindowFlagChange: (Boolean) -> Unit
    ) {
        _uiState.update { it.copy(isInAppFlagSecureEnabled = enableFlagSecure) }
        onWindowFlagChange(enableFlagSecure)
        viewModelScope.launch {
            auditRepository.logEvent(
                action = if (enableFlagSecure) "In-App FLAG_SECURE Activated" else "In-App FLAG_SECURE Cleared",
                details = "Window surface protected against view inspection and screenshot preview",
                category = "SCREEN_POLICY",
                severity = "INFO"
            )
        }
    }

    fun togglePasswordRequirement(enabled: Boolean) {
        _uiState.update { it.copy(isPasswordRequirementEnabled = enabled) }
        applyPasswordPolicy()
    }

    fun setPasswordQuality(quality: Int, suggestedLength: Int) {
        _uiState.update {
            it.copy(
                passwordQuality = quality,
                passwordMinLength = if (it.passwordMinLength < suggestedLength) suggestedLength else it.passwordMinLength
            )
        }
        applyPasswordPolicy()
    }

    fun setPasswordMinLength(length: Int) {
        _uiState.update { it.copy(passwordMinLength = length) }
        applyPasswordPolicy()
    }

    fun setMaxFailedAttempts(attempts: Int) {
        _uiState.update { it.copy(maxFailedAttempts = attempts) }
        applyPasswordPolicy()
    }

    fun setExpirationDays(days: Int) {
        _uiState.update { it.copy(expirationDays = days) }
        applyPasswordPolicy()
    }

    fun applyPasswordPolicy() {
        val current = _uiState.value
        if (!current.isAdminActive) {
            _uiState.update {
                it.copy(
                    bannerMessage = "Device Administrator privileges required to enforce device password policy.",
                    bannerSeverity = "ERROR"
                )
            }
            return
        }

        val config = PasswordPolicyConfig(
            enabled = current.isPasswordRequirementEnabled,
            quality = if (current.isPasswordRequirementEnabled) current.passwordQuality else DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED,
            minLength = if (current.isPasswordRequirementEnabled) current.passwordMinLength else 0,
            maxFailedAttempts = current.maxFailedAttempts,
            expirationDays = current.expirationDays,
            historyLength = current.historyLength,
            inactivityLockSeconds = current.inactivityLockSeconds
        )

        val result = securityManager.applyPasswordPolicy(config)
        result.fold(
            onSuccess = {
                val isSufficient = securityManager.isPasswordSufficient()
                _uiState.update {
                    it.copy(
                        isPasswordSufficient = isSufficient,
                        bannerMessage = if (config.enabled) {
                            "Password policy enforced: ${getQualityLabel(config.quality)} (Min ${config.minLength} chars/digits)."
                        } else {
                            "Password requirement disabled (Swipe / None permitted)."
                        },
                        bannerSeverity = "SUCCESS"
                    )
                }

                viewModelScope.launch {
                    auditRepository.logEvent(
                        action = if (config.enabled) "Password Policy Updated" else "Password Policy Disabled",
                        details = if (config.enabled) {
                            "Quality=${getQualityLabel(config.quality)}, MinLength=${config.minLength}, MaxFailed=${config.maxFailedAttempts}"
                        } else {
                            "Device password requirements removed"
                        },
                        category = "PASSWORD_POLICY",
                        severity = if (config.enabled) "SUCCESS" else "WARNING"
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        bannerMessage = "Error enforcing password policy: ${error.localizedMessage}",
                        bannerSeverity = "ERROR"
                    )
                }
            }
        )
    }

    fun applyPreset(
        preset: EnterprisePreset,
        onWindowFlagChange: (Boolean) -> Unit
    ) {
        if (!_uiState.value.isAdminActive) {
            _uiState.update {
                it.copy(
                    bannerMessage = "Please activate Device Administrator first to apply enterprise presets.",
                    bannerSeverity = "ERROR"
                )
            }
            return
        }

        // Apply screen capture policy
        val screenResult = securityManager.setScreenCaptureDisabled(preset.screenCaptureDisabled)
        // Apply password policy
        val passwordResult = securityManager.applyPasswordPolicy(preset.config)

        val isSufficient = securityManager.isPasswordSufficient()

        _uiState.update {
            it.copy(
                isScreenCaptureDisabled = preset.screenCaptureDisabled,
                isInAppFlagSecureEnabled = preset.screenCaptureDisabled,
                isPasswordRequirementEnabled = preset.config.enabled,
                passwordQuality = preset.config.quality,
                passwordMinLength = preset.config.minLength,
                maxFailedAttempts = preset.config.maxFailedAttempts,
                expirationDays = preset.config.expirationDays,
                historyLength = preset.config.historyLength,
                inactivityLockSeconds = preset.config.inactivityLockSeconds,
                isPasswordSufficient = isSufficient,
                bannerMessage = "Applied preset '${preset.name}'.",
                bannerSeverity = "SUCCESS"
            )
        }
        onWindowFlagChange(preset.screenCaptureDisabled)

        viewModelScope.launch {
            auditRepository.logEvent(
                action = "Preset Applied: ${preset.name}",
                details = "Screen capture blocked=${preset.screenCaptureDisabled}, Password Quality=${getQualityLabel(preset.config.quality)}",
                category = "ENROLLMENT",
                severity = "SUCCESS"
            )
        }
    }

    fun lockDeviceNow() {
        if (!_uiState.value.isAdminActive) {
            _uiState.update {
                it.copy(
                    bannerMessage = "Device Administrator privileges required to lock screen.",
                    bannerSeverity = "ERROR"
                )
            }
            return
        }

        val result = securityManager.lockDeviceNow()
        result.fold(
            onSuccess = {
                viewModelScope.launch {
                    auditRepository.logEvent(
                        action = "Immediate Screen Lock",
                        details = "Device locked immediately by administrator request",
                        category = "DEVICE_LOCK",
                        severity = "INFO"
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        bannerMessage = "Failed to lock device: ${error.localizedMessage}",
                        bannerSeverity = "ERROR"
                    )
                }
            }
        )
    }

    fun deactivateAdmin() {
        val result = securityManager.deactivateAdmin()
        result.fold(
            onSuccess = {
                refreshSecurityState()
                _uiState.update {
                    it.copy(
                        bannerMessage = "Device Administrator privileges deactivated.",
                        bannerSeverity = "INFO"
                    )
                }
                viewModelScope.launch {
                    auditRepository.logEvent(
                        action = "Admin Deactivated",
                        details = "User manually deactivated administrator status",
                        category = "ENROLLMENT",
                        severity = "WARNING"
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        bannerMessage = "Could not deactivate admin: ${error.localizedMessage}",
                        bannerSeverity = "ERROR"
                    )
                }
            }
        )
    }

    fun clearBanner() {
        _uiState.update { it.copy(bannerMessage = null) }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            auditRepository.clearLogs()
            auditRepository.logEvent(
                action = "Logs Cleared",
                details = "Audit history was cleared by administrator",
                category = "COMPLIANCE",
                severity = "INFO"
            )
        }
    }

    private fun getQualityLabel(quality: Int): String {
        return when (quality) {
            DevicePolicyManager.PASSWORD_QUALITY_SOMETHING -> "Pattern / Biometrics"
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC -> "Numeric PIN (4+ Digits)"
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX -> "Complex PIN"
            DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC -> "Alphabetic"
            DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC -> "Alphanumeric"
            DevicePolicyManager.PASSWORD_QUALITY_COMPLEX -> "Enterprise Complex"
            else -> "Swipe / None"
        }
    }
}
