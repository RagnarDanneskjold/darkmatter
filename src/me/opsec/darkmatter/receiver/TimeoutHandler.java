package me.opsec.darkmatter.receiver;

import me.opsec.darkmatter.service.DarkService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimeoutHandler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        intent = new Intent(context, DarkService.class);
        intent.setAction(DarkService.ACTION_TIMEOUT);
        context.startService(intent);
    }
}
