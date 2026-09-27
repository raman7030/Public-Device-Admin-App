package com.example.manager

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.receiver.EnterpriseAdminReceiver

data class PasswordPolicyConfig(
    val enabled: Boolean = true,
    val quality: Int = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC,
    val minLength: Int = 4,
    val maxFailedAttempts: Int = 5,
    val expirationDays: Int = 0, // 0 = never
    val historyLength: Int = 0,
    val inactivityLockSeconds: Int = 60
)

data class DeviceSecurityDetails(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val isEncryptionSupported: Boolean,
    val encryptionStatusText: String,
    val failedUnlockAttempts: Int
)

class DeviceSecurityManager(private val context: Context) {

    private val dpm: DevicePolicyManager by lazy {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    val adminComponent: ComponentName by lazy {
        EnterpriseAdminReceiver.getComponentName(context)
    }

    fun isAdminActive(): Boolean {
        return try {
            dpm.isAdminActive(adminComponent)
        } catch (e: Exception) {
            false
        }
    }

    /** True only when Android has provisioned this app with organization-level ownership. */
    fun isDeviceOwnerOrProfileOwner(): Boolean {
        return try {
            dpm.isDeviceOwnerApp(context.packageName) ||
                dpm.isProfileOwnerApp(context.packageName)
        } catch (e: Exception) {
            false
        }
    }

    fun getScreenCaptureDisabled(): Boolean {
        return try {
            isAdminActive() && isDeviceOwnerOrProfileOwner() &&
                dpm.getScreenCaptureDisabled(adminComponent)
        } catch (e: Exception) {
            false
        }
    }

    fun setScreenCaptureDisabled(disabled: Boolean): Result<Boolean> {
        return try {
            if (!isAdminActive()) {
                return Result.failure(IllegalStateException("Activate Device Admin first in Android Settings."))
            }
            if (!isDeviceOwnerOrProfileOwner()) {
                return Result.failure(IllegalStateException(
                    "Ordinary Device Admin cannot control system-wide screenshots. Provision this app as Device Owner or Profile Owner on a managed device."
                ))
            }
            dpm.setScreenCaptureDisabled(adminComponent, disabled)
            val actual = dpm.getScreenCaptureDisabled(adminComponent)
            if (actual != disabled) {
                return Result.failure(IllegalStateException("Android did not confirm the requested screen-capture policy."))
            }
            Result.success(actual)
        } catch (e: SecurityException) {
            Result.failure(IllegalStateException(
                "Android denied this policy. Device Owner/Profile Owner provisioning may be required: ${e.localizedMessage}", e
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPasswordQuality(): Int {
        return try {
            if (isAdminActive()) {
                dpm.getPasswordQuality(adminComponent)
            } else {
                DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED
            }
        } catch (e: Exception) {
            DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED
        }
    }

    fun getPasswordMinimumLength(): Int {
        return try {
            if (isAdminActive()) {
                dpm.getPasswordMinimumLength(adminComponent)
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    fun getMaximumFailedPasswordsForWipe(): Int {
        return try {
            if (isAdminActive()) {
                dpm.getMaximumFailedPasswordsForWipe(adminComponent)
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    fun applyPasswordPolicy(config: PasswordPolicyConfig): Result<Unit> {
        return try {
            if (!isAdminActive()) {
                return Result.failure(IllegalStateException("Device Administrator privileges not active."))
            }

            if (!config.enabled) {
                // Disable password enforcement
                dpm.setPasswordQuality(adminComponent, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED)
                dpm.setPasswordMinimumLength(adminComponent, 0)
                dpm.setMaximumFailedPasswordsForWipe(adminComponent, 0)
                dpm.setPasswordExpirationTimeout(adminComponent, 0L)
                dpm.setPasswordHistoryLength(adminComponent, 0)
            } else {
                // Enable and enforce requested policy
                dpm.setPasswordQuality(adminComponent, config.quality)
                dpm.setPasswordMinimumLength(adminComponent, config.minLength)
                dpm.setMaximumFailedPasswordsForWipe(adminComponent, config.maxFailedAttempts)
                
                val timeoutMs = if (config.expirationDays > 0) {
                    config.expirationDays.toLong() * 24 * 60 * 60 * 1000L
                } else {
                    0L
                }
                dpm.setPasswordExpirationTimeout(adminComponent, timeoutMs)
                dpm.setPasswordHistoryLength(adminComponent, config.historyLength)

                if (config.inactivityLockSeconds > 0) {
                    dpm.setMaximumTimeToLock(adminComponent, config.inactivityLockSeconds * 1000L)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isPasswordSufficient(): Boolean {
        return try {
            if (isAdminActive()) {
                dpm.isActivePasswordSufficient
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    fun lockDeviceNow(): Result<Unit> {
        return try {
            if (!isAdminActive()) {
                return Result.failure(IllegalStateException("Device Administrator privileges not active."))
            }
            dpm.lockNow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deactivateAdmin(): Result<Unit> {
        return try {
            if (isAdminActive()) {
                dpm.removeActiveAdmin(adminComponent)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDeviceSecurityDetails(): DeviceSecurityDetails {
        val encryptionStatus = try {
            when (dpm.storageEncryptionStatus) {
                DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE -> "Hardware Encrypted"
                DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY -> "Default Key Encrypted"
                DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE -> "Not Encrypted"
                DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING -> "Activating..."
                DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED -> "Unsupported"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Protected (OS)"
        }

        val failedAttempts = try {
            if (isAdminActive()) dpm.currentFailedPasswordAttempts else 0
        } catch (e: Exception) {
            0
        }

        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            "Standard"
        }

        return DeviceSecurityDetails(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = securityPatch,
            isEncryptionSupported = true,
            encryptionStatusText = encryptionStatus,
            failedUnlockAttempts = failedAttempts
        )
    }

    fun createAddAdminIntent(): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "ShieldMDM uses Device Administrator for supported password policies and screen locking. System-wide screenshot controls require Device Owner or Profile Owner provisioning by Android. No policy is applied until Android grants and confirms it."
            )
        }
    }

    fun createSetNewPasswordIntent(): Intent {
        return Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)
    }
}
