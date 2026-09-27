package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.manager.DeviceSecurityManager
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.viewmodel.DashboardViewModel

enum class NavigationTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    SCREEN_CAPTURE("Screen Capture", Icons.Default.VideocamOff),
    PASSWORD_POLICY("Passcode Rules", Icons.Default.Lock),
    PRESETS("Presets", Icons.Default.AutoAwesome),
    AUDIT_LOGS("Audit Log", Icons.Default.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: DashboardViewModel,
    onWindowFlagChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(NavigationTab.OVERVIEW) }
    var showDeactivateDialog by remember { mutableStateOf(false) }

    // Launcher for Android Device Admin activation prompt
    val adminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshSecurityState()
    }

    // Refresh on return
    LaunchedEffect(Unit) {
        viewModel.refreshSecurityState()
    }

    // Handle back button: return to Overview if on another tab
    BackHandler(enabled = currentTab != NavigationTab.OVERVIEW) {
        currentTab = NavigationTab.OVERVIEW
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ShieldMDM",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (uiState.isAdminActive) EmeraldSuccess.copy(alpha = 0.15f)
                                    else CrimsonDanger.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (uiState.isAdminActive) "ENFORCING" else "UNENROLLED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isAdminActive) EmeraldSuccess else CrimsonDanger
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Notification Banner if present
            AnimatedVisibility(visible = uiState.bannerMessage != null) {
                uiState.bannerMessage?.let { msg ->
                    val (bannerBg, bannerIcon, bannerTint) = when (uiState.bannerSeverity) {
                        "SUCCESS" -> Triple(EmeraldSuccess.copy(alpha = 0.15f), Icons.Default.CheckCircle, EmeraldSuccess)
                        "ERROR" -> Triple(CrimsonDanger.copy(alpha = 0.15f), Icons.Default.Error, CrimsonDanger)
                        else -> Triple(MaterialTheme.colorScheme.primaryContainer, Icons.Default.Info, MaterialTheme.colorScheme.primary)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bannerBg)
                            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = bannerIcon,
                                contentDescription = null,
                                tint = bannerTint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearBanner() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Screen content based on selected tab
            when (currentTab) {
                NavigationTab.OVERVIEW -> {
                    OverviewScreen(
                        isAdminActive = uiState.isAdminActive,
                        isScreenCaptureDisabled = uiState.isScreenCaptureDisabled,
                        isPasswordRequirementEnabled = uiState.isPasswordRequirementEnabled,
                        passwordQuality = uiState.passwordQuality,
                        passwordMinLength = uiState.passwordMinLength,
                        isPasswordSufficient = uiState.isPasswordSufficient,
                        deviceDetails = uiState.deviceDetails,
                        onEnrollAdminClick = {
                            val manager = DeviceSecurityManager(context)
                            adminLauncher.launch(manager.createAddAdminIntent())
                        },
                        onDeactivateAdminClick = { showDeactivateDialog = true },
                        onNavigateToScreenCapture = { currentTab = NavigationTab.SCREEN_CAPTURE },
                        onNavigateToPasswordPolicy = { currentTab = NavigationTab.PASSWORD_POLICY },
                        onLockDeviceNow = { viewModel.lockDeviceNow() },
                        onSetNewPasswordClick = {
                            val manager = DeviceSecurityManager(context)
                            try {
                                context.startActivity(manager.createSetNewPasswordIntent())
                            } catch (e: Exception) {
                                // fallback to security settings
                                context.startActivity(Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS))
                            }
                        }
                    )
                }

                NavigationTab.SCREEN_CAPTURE -> {
                    ScreenCaptureScreen(
                        isAdminActive = uiState.isAdminActive,
                        isScreenCaptureDisabled = uiState.isScreenCaptureDisabled,
                        isInAppFlagSecureEnabled = uiState.isInAppFlagSecureEnabled,
                        onToggleCapturePolicy = { disable ->
                            viewModel.toggleScreenCapturePolicy(disable, onWindowFlagChange)
                        },
                        onToggleFlagSecure = { secure ->
                            viewModel.toggleInAppFlagSecure(secure, onWindowFlagChange)
                        },
                        onEnrollAdminClick = {
                            val manager = DeviceSecurityManager(context)
                            adminLauncher.launch(manager.createAddAdminIntent())
                        }
                    )
                }

                NavigationTab.PASSWORD_POLICY -> {
                    PasswordPolicyScreen(
                        isAdminActive = uiState.isAdminActive,
                        isPasswordRequirementEnabled = uiState.isPasswordRequirementEnabled,
                        currentQuality = uiState.passwordQuality,
                        currentMinLength = uiState.passwordMinLength,
                        maxFailedAttempts = uiState.maxFailedAttempts,
                        expirationDays = uiState.expirationDays,
                        isPasswordSufficient = uiState.isPasswordSufficient,
                        onToggleEnabled = { enabled -> viewModel.togglePasswordRequirement(enabled) },
                        onQualitySelected = { quality, suggestedLen -> viewModel.setPasswordQuality(quality, suggestedLen) },
                        onMinLengthChanged = { len -> viewModel.setPasswordMinLength(len) },
                        onMaxFailedAttemptsChanged = { attempts -> viewModel.setMaxFailedAttempts(attempts) },
                        onExpirationDaysChanged = { days -> viewModel.setExpirationDays(days) },
                        onSetNewPasswordClick = {
                            val manager = DeviceSecurityManager(context)
                            try {
                                context.startActivity(manager.createSetNewPasswordIntent())
                            } catch (e: Exception) {
                                context.startActivity(Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS))
                            }
                        },
                        onLockDeviceNow = { viewModel.lockDeviceNow() },
                        onEnrollAdminClick = {
                            val manager = DeviceSecurityManager(context)
                            adminLauncher.launch(manager.createAddAdminIntent())
                        }
                    )
                }

                NavigationTab.PRESETS -> {
                    PresetsScreen(
                        isAdminActive = uiState.isAdminActive,
                        onApplyPreset = { preset -> viewModel.applyPreset(preset, onWindowFlagChange) },
                        onEnrollAdminClick = {
                            val manager = DeviceSecurityManager(context)
                            adminLauncher.launch(manager.createAddAdminIntent())
                        }
                    )
                }

                NavigationTab.AUDIT_LOGS -> {
                    AuditLogsScreen(
                        logs = auditLogs,
                        onClearLogs = { viewModel.clearAuditLogs() }
                    )
                }
            }
        }
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CrimsonDanger,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Deactivate Device Administrator?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Deactivating ShieldMDM will remove all enterprise security restrictions. Screen recording/screenshots will be allowed and PIN/passcode quality requirements will be removed.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deactivateAdmin()
                        showDeactivateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger)
                ) {
                    Text("Deactivate Now")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeactivateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
