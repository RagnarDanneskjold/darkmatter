package me.opsec.darkmatter.receiver;

import me.opsec.darkmatter.MainActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Hidden way to launch the app since we hide the launcher icon. We listen to ACTION_NEW_OUTGOING_CALL and launch the
 * app if our "secret number" is dialed.
 */
public class LaunchViaDialReceiver extends BroadcastReceiver {

    private static final String LAUNCHER_NUMBER = "12345";

    @Override
    public void onReceive(Context context, Intent intent) {
        String phoneNubmer = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        if (LAUNCHER_NUMBER.equals(phoneNubmer)) {
            setResultData(null);
            Intent appIntent = new Intent(context, MainActivity.class);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(appIntent);
        }
    }
}