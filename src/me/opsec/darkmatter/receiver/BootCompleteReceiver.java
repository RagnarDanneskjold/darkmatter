package me.opsec.darkmatter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO: Add code that should happen after boot completed.
        // Note! Can't use su here. Send intent to service instead.

    }
}