package com.noduq.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/**
 * Android only tells us "not granted", never "refused for good". The difference matters: one
 * deserves another ask, the other has to send the shopkeeper to Settings. We remember what we
 * already asked to tell them apart.
 */
class AndroidPermissions(private val context: Context) : DevicePermissions {

    private val history = context.getSharedPreferences("noduq_permissions", Context.MODE_PRIVATE)
    private val oneAtATime = Mutex()

    private var activity: ComponentActivity? = null
    private var launcher: ActivityResultLauncher<String>? = null
    private var answering: CancellableContinuation<Boolean>? = null
    private var pendingPermission: String? = null

    fun attach(host: ComponentActivity) {
        activity = host
        launcher = host.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val waiter = answering
            answering = null
            pendingPermission = null
            if (waiter?.isActive == true) waiter.resume(granted)
        }
        replayPendingAsk()
    }

    fun detach() {
        activity = null
        launcher = null
    }

    override fun notifications(): PermissionState =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            PermissionState.NotNeeded
        } else {
            state(Manifest.permission.POST_NOTIFICATIONS)
        }

    override fun sms(): PermissionState = state(Manifest.permission.RECEIVE_SMS)

    override suspend fun requestNotifications(): PermissionState =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            PermissionState.NotNeeded
        } else {
            ask(Manifest.permission.POST_NOTIFICATIONS)
        }

    override suspend fun requestSms(): PermissionState = ask(Manifest.permission.RECEIVE_SMS)

    override fun openSettings() {
        val target = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        runCatching { context.startActivity(target) }
    }

    private fun state(permission: String): PermissionState = when {
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED ->
            PermissionState.Granted
        asked(permission) && !canAskAgain(permission) -> PermissionState.Blocked
        else -> PermissionState.Denied
    }

    private suspend fun ask(permission: String): PermissionState = oneAtATime.withLock {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            return@withLock PermissionState.Granted
        }
        val dialog = launcher ?: return@withLock state(permission)
        history.edit().putBoolean(permission, true).apply()
        val granted = suspendCancellableCoroutine { waiting ->
            answering = waiting
            pendingPermission = permission
            waiting.invokeOnCancellation {
                if (answering === waiting) {
                    answering = null
                    pendingPermission = null
                }
            }
            dialog.launch(permission)
        }
        if (granted) PermissionState.Granted else state(permission)
    }

    private fun replayPendingAsk() {
        val permission = pendingPermission ?: return
        val waiter = answering ?: return
        if (!waiter.isActive) return
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            answering = null
            pendingPermission = null
            waiter.resume(true)
            return
        }
        launcher?.launch(permission)
    }

    private fun asked(permission: String): Boolean = history.getBoolean(permission, false)

    private fun canAskAgain(permission: String): Boolean {
        val host = activity ?: return true
        return ActivityCompat.shouldShowRequestPermissionRationale(host, permission)
    }
}
