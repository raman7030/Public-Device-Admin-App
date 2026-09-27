package com.example.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.widget.Toast
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EnterpriseAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "ShieldMDM Device Admin Activated", Toast.LENGTH_SHORT).show()
        logEvent(context, "Admin Activated", "Device Administrator privileges granted to ShieldMDM", "ENROLLMENT", "SUCCESS")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Warning: Disabling ShieldMDM Device Administrator will revoke enterprise screen capture controls and password policies."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "ShieldMDM Device Admin Deactivated", Toast.LENGTH_SHORT).show()
        logEvent(context, "Admin Deactivated", "Device Administrator privileges removed", "ENROLLMENT", "WARNING")
    }

    override fun onPasswordChanged(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordChanged(context, intent, user)
        logEvent(context, "Password Changed", "Device passcode/pattern was updated", "PASSWORD_POLICY", "SUCCESS")
    }

    override fun onPasswordFailed(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordFailed(context, intent, user)
        logEvent(context, "Password Failed", "Incorrect device lock entry attempt", "PASSWORD_POLICY", "WARNING")
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordSucceeded(context, intent, user)
        logEvent(context, "Unlock Succeeded", "Device successfully unlocked", "PASSWORD_POLICY", "INFO")
    }

    override fun onPasswordExpiring(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordExpiring(context, intent, user)
        logEvent(context, "Password Expiring", "Device passcode is near expiration period", "PASSWORD_POLICY", "WARNING")
    }

    private fun logEvent(
        context: Context,
        action: String,
        details: String,
        category: String,
        severity: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                db.auditLogDao().insertLog(
                    com.example.data.model.SecurityAuditLog(
                        action = action,
                        details = details,
                        category = category,
                        severity = severity
                    )
                )
            } catch (e: Exception) {
                // Silently ignore if DB write fails during broadcast
            }
        }
    }

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, EnterpriseAdminReceiver::class.java)
        }
    }
}
