package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import kotlinx.coroutines.delay

@Composable
fun ScreenCaptureScreen(
    isAdminActive: Boolean,
    isScreenCaptureDisabled: Boolean,
    isInAppFlagSecureEnabled: Boolean,
    onToggleCapturePolicy: (Boolean) -> Unit,
    onToggleFlagSecure: (Boolean) -> Unit,
    onEnrollAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showTestHintDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("screen_capture_screen")
    ) {
        // Header Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isScreenCaptureDisabled) CrimsonDanger.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isScreenCaptureDisabled) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    contentDescription = null,
                    tint = if (isScreenCaptureDisabled) CrimsonDanger else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Screen Recording & Capture",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Prevent corporate espionage & unauthorized recording",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Main Policy Toggle Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("capture_policy_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Disable Screen Capture",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isScreenCaptureDisabled) CrimsonDanger.copy(alpha = 0.15f)
                                        else EmeraldSuccess.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isScreenCaptureDisabled) "BLOCKED" else "ALLOWED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isScreenCaptureDisabled) CrimsonDanger else EmeraldSuccess
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isScreenCaptureDisabled) {
                                "Screenshots, screen recording apps, and recents switcher previews are blocked across all applications on this device."
                            } else {
                                "Screenshots and video screen recorders are permitted on this device."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Switch(
                        checked = isScreenCaptureDisabled,
                        onCheckedChange = { onToggleCapturePolicy(it) },
                        enabled = isAdminActive,
                        modifier = Modifier.testTag("capture_policy_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CrimsonDanger
                        )
                    )
                }

                if (!isAdminActive) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Device Administrator privileges required to enforce system-wide policy.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = onEnrollAdminClick,
                                modifier = Modifier.testTag("enroll_for_capture_btn")
                            ) {
                                Text("Enroll", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // In-App Window Protection Toggle (FLAG_SECURE)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "In-App Window Privacy (FLAG_SECURE)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Treats this application window as secure surface, preventing screenshots and hiding view in Android Task Switcher.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Switch(
                        checked = isInAppFlagSecureEnabled,
                        onCheckedChange = { onToggleFlagSecure(it) },
                        modifier = Modifier.testTag("in_app_flag_secure_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Testing Studio
        Text(
            text = "Interactive Screen Capture Test Studio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Verify whether capture protection is active in real-time",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        CaptureTestCanvas(
            isBlocked = isScreenCaptureDisabled || isInAppFlagSecureEnabled,
            onAttemptScreenshot = { showTestHintDialog = true }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Technical Guide Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "How Android Enforces Screen Capture Blocking",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• System Screen Capture Policy: DevicePolicyManager.setScreenCaptureDisabled() instructs Android's SurfaceFlinger display compositor to reject screenshot requests from all apps and the system UI (Power + Vol-Down).\n\n" +
                            "• Screen Recording Apps: Any screen casting, recording, or mirroring tool receives a blank black screen whenever secure surfaces are active.\n\n" +
                            "• Task Switcher Protection: In the Android Recent Apps / Multitasking carousel, the app window thumbnail is obscured with a privacy shield.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showTestHintDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTestHintDialog = false },
            icon = {
                Icon(
                    imageVector = if (isScreenCaptureDisabled || isInAppFlagSecureEnabled) Icons.Default.VisibilityOff else Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = if (isScreenCaptureDisabled || isInAppFlagSecureEnabled) CrimsonDanger else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isScreenCaptureDisabled || isInAppFlagSecureEnabled) "Screen Capture is Blocked" else "Screen Capture is Allowed",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isScreenCaptureDisabled || isInAppFlagSecureEnabled) {
                            "Verification Instructions:\n\n1. Press Power + Volume Down on your device (or use the screenshot button).\n2. Notice Android displays: \"Taking screenshots isn't allowed by the app or your organization\".\n3. Switch to Recent Apps; the preview is blanked out."
                        } else {
                            "Screen capture is currently allowed. You can freely capture screenshots or record videos of this screen."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showTestHintDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun CaptureTestCanvas(
    isBlocked: Boolean,
    onAttemptScreenshot: () -> Unit
) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "watermark")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("capture_test_canvas"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBlocked) Color(0xFF1E1118) else Color(0xFF101B2E)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isBlocked) CrimsonDanger.copy(alpha = 0.6f) else EmeraldSuccess.copy(alpha = 0.6f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background Watermark Text
            Text(
                text = if (isBlocked) "CLASSIFIED • DO NOT CAPTURE" else "PUBLIC SANDBOX • CAPTURE OK",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = if (isBlocked) CrimsonDanger.copy(alpha = 0.12f) else EmeraldSuccess.copy(alpha = 0.12f),
                modifier = Modifier.rotate(rotation),
                textAlign = TextAlign.Center
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isBlocked) CrimsonDanger.copy(alpha = 0.2f)
                            else EmeraldSuccess.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isBlocked) Icons.Default.Block else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isBlocked) CrimsonDanger else EmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBlocked) "SCREEN CAPTURE DISABLED" else "SCREEN CAPTURE ALLOWED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBlocked) CrimsonDanger else EmeraldSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CONFIDENTIAL ENTERPRISE PAYLOAD",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "AUTH_TOKEN::MDM-SEC-8941",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Live Sync Clock: ${currentTimeMillis}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAttemptScreenshot,
                    modifier = Modifier.testTag("test_screenshot_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBlocked) CrimsonDanger else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Test Screenshot Protection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
