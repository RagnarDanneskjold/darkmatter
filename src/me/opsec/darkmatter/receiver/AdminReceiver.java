package me.opsec.darkmatter.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

//import dev.ukanth.ufirewall.RootShell.RootCommand

/**
 * The receiver of admin events.
 */
public class AdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        DevicePolicyManager pm = (DevicePolicyManager) context
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(context, AdminReceiver.class);
        pm.setCameraDisabled(admin, true);
        pm.setKeyguardDisabledFeatures(admin, DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL);

        // TODO: Add code like this to start the preferences when enabled
        // intent = new Intent(context, MainActivity.class);
        // // Need the FLAG_ACTIVITY_NEW_TASK since we start form a receiver
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // context.startActivity(intent);
    }

    // TODO: Add this if we want a warning text when the device admin is disabled
    // @Override
    // public CharSequence onDisableRequested(Context context, Intent intent) {
    // return context.getString(R.string.admin_receiver_status_disable_warning);
    // }

    // @Override
    // public void onPasswordFailed(Context context, Intent intent) {
    // XXX: set this to send an intent to the DarkService -> "PASSWORD_FAIL"
    // ScriptRunner.startScript(context);
    // }
    
    // @Override
    // public void onPasswordSuccess(Context context, Intent intent) {
    // XXX: set this to send an intent to the DarkService -> "PASSWORD_SUCCESS"
    // }
}
