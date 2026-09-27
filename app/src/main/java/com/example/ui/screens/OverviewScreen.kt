package com.example.ui.screens

import android.app.admin.DevicePolicyManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manager.DeviceSecurityDetails
import com.example.ui.components.EnrollmentCard
import com.example.ui.components.SecurityHeader
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess

@Composable
fun OverviewScreen(
    isAdminActive: Boolean,
    isScreenCaptureDisabled: Boolean,
    isPasswordRequirementEnabled: Boolean,
    passwordQuality: Int,
    passwordMinLength: Int,
    isPasswordSufficient: Boolean,
    deviceDetails: DeviceSecurityDetails?,
    onEnrollAdminClick: () -> Unit,
    onDeactivateAdminClick: () -> Unit,
    onNavigateToScreenCapture: () -> Unit,
    onNavigateToPasswordPolicy: () -> Unit,
    onLockDeviceNow: () -> Unit,
    onSetNewPasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("overview_screen")
    ) {
        // High-level security summary header
        SecurityHeader(
            isAdminActive = isAdminActive,
            isScreenCaptureDisabled = isScreenCaptureDisabled,
            isPasswordSufficient = isPasswordSufficient
        )

        Spacer(modifier = Modifier.height(16.dp))

        // If not active, show the prominent enrollment card
        if (!isAdminActive) {
            EnrollmentCard(onEnrollClick = onEnrollAdminClick)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active Admin Badge & Management
        if (isAdminActive) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_active_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Device Administrator Enrolled",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "All enterprise policies are currently actively enforced",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onDeactivateAdminClick,
                        modifier = Modifier.testTag("deactivate_admin_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Deactivate",
                            fontSize = 11.sp,
                            color = CrimsonDanger
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Core Policy Control Cards Grid
        Text(
            text = "Active Policy Enforcement",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Screen Capture Status Card
            PolicyStatusCard(
                title = "Screen Recording",
                status = if (isScreenCaptureDisabled) "BLOCKED" else "ALLOWED",
                description = if (isScreenCaptureDisabled) "Screenshots disabled" else "Screenshots allowed",
                icon = Icons.Default.VideocamOff,
                isSecured = isScreenCaptureDisabled,
                onClick = onNavigateToScreenCapture,
                modifier = Modifier.weight(1f)
            )

            // Password Policy Status Card
            val qualityLabel = when (passwordQuality) {
                DevicePolicyManager.PASSWORD_QUALITY_SOMETHING -> "Pattern"
                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC -> "4-Digit PIN"
                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX -> "Complex PIN"
                DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC -> "Alphanumeric"
                DevicePolicyManager.PASSWORD_QUALITY_COMPLEX -> "Enterprise"
                else -> "None / Swipe"
            }

            PolicyStatusCard(
                title = "Credential Quality",
                status = if (isPasswordRequirementEnabled) qualityLabel else "DISABLED",
                description = if (isPasswordRequirementEnabled) "Min $passwordMinLength chars" else "Swipe / None allowed",
                icon = Icons.Default.Pin,
                isSecured = isPasswordRequirementEnabled && isPasswordSufficient,
                onClick = onNavigateToPasswordPolicy,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Administrative Actions
        Text(
            text = "Quick Administrative Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onLockDeviceNow,
                enabled = isAdminActive,
                modifier = Modifier
                    .weight(1f)
                    .testTag("overview_lock_now_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Lock Device", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onSetNewPasswordClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("overview_set_pwd_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Update Passcode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Device Telemetry Card
        if (deviceDetails != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_telemetry_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Device Hardware & Security Specs",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TelemetryRow(label = "Model", value = "${deviceDetails.manufacturer} ${deviceDetails.model}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerDefaults.color.copy(alpha = 0.3f))
                    TelemetryRow(label = "OS Version", value = "${deviceDetails.androidVersion} (API ${deviceDetails.apiLevel})")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerDefaults.color.copy(alpha = 0.3f))
                    TelemetryRow(label = "Security Patch", value = deviceDetails.securityPatch)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerDefaults.color.copy(alpha = 0.3f))
                    TelemetryRow(label = "Storage Encryption", value = deviceDetails.encryptionStatusText)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerDefaults.color.copy(alpha = 0.3f))
                    TelemetryRow(label = "Failed Unlock Attempts", value = "${deviceDetails.failedUnlockAttempts}")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PolicyStatusCard(
    title: String,
    status: String,
    description: String,
    icon: ImageVector,
    isSecured: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("policy_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSecured) EmeraldSuccess.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSecured) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = status,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSecured) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TelemetryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
