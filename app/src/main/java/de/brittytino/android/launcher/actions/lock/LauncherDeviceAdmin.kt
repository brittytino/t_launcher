package de.brittytino.android.launcher.actions.lock

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import de.brittytino.android.launcher.R

class LauncherDeviceAdmin : DeviceAdminReceiver() {
    companion object {
        private fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, LauncherDeviceAdmin::class.java)
        }

        private fun requestDeviceAdmin(context: Context) {

            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(context))
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    context.getString(R.string.device_admin_explanation)
                )
            }
            context.startActivity(intent)
        }

        fun isDeviceAdmin(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            return dpm.isAdminActive(getComponentName(context))
        }

        private fun assertDeviceAdmin(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            if (!dpm.isAdminActive(getComponentName(context))) {
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_device_admin_not_enabled),
                    Toast.LENGTH_LONG
                ).show()
                requestDeviceAdmin(context)
                return false
            }
            return true
        }

        fun lockScreen(context: Context) {

            assertDeviceAdmin(context) || return

            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            dpm.lockNow()
        }
    }
}