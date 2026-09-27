package com.example.model

import android.app.admin.DevicePolicyManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.manager.PasswordPolicyConfig

data class PasswordQualityOption(
    val qualityCode: Int,
    val title: String,
    val subtitle: String,
    val defaultLength: Int,
    val icon: ImageVector,
    val requiresPinNumeric: Boolean = false
)

object SecurityPolicies {
    val qualityOptions = listOf(
        PasswordQualityOption(
            qualityCode = DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED,
            title = "Swipe / None (Disabled)",
            subtitle = "No PIN or password required. Standard device swipe or no lock screen.",
            defaultLength = 0,
            icon = Icons.Default.Key
        ),
        PasswordQualityOption(
            qualityCode = DevicePolicyManager.PASSWORD_QUALITY_SOMETHING,
            title = "Pattern / Biometrics",
            subtitle = "Requires user to draw a lock pattern, scan biometric fingerprint/face, or swipe credential.",
            defaultLength = 4,
            icon = Icons.Default.Fingerprint
        ),
        PasswordQualityOption(
            qualityCode = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC,
            title = "4-Digit PIN (Numeric)",
            subtitle = "Enforces a numeric PIN code. Typically 4 or more numeric digits.",
            defaultLength = 4,
            icon = Icons.Default.Pin,
            requiresPinNumeric = true
        ),
        PasswordQualityOption(
            qualityCode = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX,
            title = "Complex Numeric PIN",
            subtitle = "Numeric PIN prohibiting repeated numbers (e.g. 1111) and sequential patterns (e.g. 1234).",
            defaultLength = 6,
            icon = Icons.Default.Pin,
            requiresPinNumeric = true
        ),
        PasswordQualityOption(
            qualityCode = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC,
            title = "Alphabetic Passphrase",
            subtitle = "Requires letters from the alphabet, case-sensitive words or phrases.",
            defaultLength = 6,
            icon = Icons.Default.Password
        ),
        PasswordQualityOption(
            qualityCode = DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC,
            title = "Alphanumeric Password",
            subtitle = "Requires a combination of letters and numeric digits for enterprise defense.",
            defaultLength = 8,
            icon = Icons.Default.Lock
        ),
        PasswordQualityOption(
            qualityCode = DevicePolicyManager.PASSWORD_QUALITY_COMPLEX,
            title = "Enterprise Complex",
            subtitle = "Mandates letters, numerical digits, and special symbols for maximum cryptographic security.",
            defaultLength = 10,
            icon = Icons.Default.Security
        )
    )
}

data class EnterprisePreset(
    val id: String,
    val name: String,
    val description: String,
    val tag: String,
    val icon: ImageVector,
    val screenCaptureDisabled: Boolean,
    val config: PasswordPolicyConfig
)

val enterprisePresets = listOf(
    EnterprisePreset(
        id = "preset_high_sec",
        name = "High-Security Banking & FinTech",
        description = "Blocks all screenshots & screen recordings. Enforces 8+ character Alphanumeric password with 5 max attempts limit.",
        tag = "Strict MDM",
        icon = Icons.Default.Shield,
        screenCaptureDisabled = true,
        config = PasswordPolicyConfig(
            enabled = true,
            quality = DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC,
            minLength = 8,
            maxFailedAttempts = 5,
            expirationDays = 30,
            historyLength = 5,
            inactivityLockSeconds = 60
        )
    ),
    EnterprisePreset(
        id = "preset_corp_balanced",
        name = "Standard Corporate (4-Digit PIN)",
        description = "Blocks screen capture on device. Requires standard 4-digit numeric PIN with 10 failed attempts threshold.",
        tag = "Recommended",
        icon = Icons.Default.Pin,
        screenCaptureDisabled = true,
        config = PasswordPolicyConfig(
            enabled = true,
            quality = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC,
            minLength = 4,
            maxFailedAttempts = 10,
            expirationDays = 90,
            historyLength = 3,
            inactivityLockSeconds = 120
        )
    ),
    EnterprisePreset(
        id = "preset_pattern_biometric",
        name = "Pattern & Biometric Security",
        description = "Allows screen capture. Requires at least an Android Pattern, fingerprint biometric, or PIN.",
        tag = "Flexible",
        icon = Icons.Default.Fingerprint,
        screenCaptureDisabled = false,
        config = PasswordPolicyConfig(
            enabled = true,
            quality = DevicePolicyManager.PASSWORD_QUALITY_SOMETHING,
            minLength = 4,
            maxFailedAttempts = 10,
            expirationDays = 0,
            historyLength = 0,
            inactivityLockSeconds = 300
        )
    ),
    EnterprisePreset(
        id = "preset_byod_casual",
        name = "Relaxed / BYOD Mode",
        description = "Screen recording and screenshots allowed. Passcode requirements disabled (Swipe / None permitted).",
        tag = "Open",
        icon = Icons.Default.Key,
        screenCaptureDisabled = false,
        config = PasswordPolicyConfig(
            enabled = false,
            quality = DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED,
            minLength = 0,
            maxFailedAttempts = 0,
            expirationDays = 0,
            historyLength = 0,
            inactivityLockSeconds = 0
        )
    )
)
