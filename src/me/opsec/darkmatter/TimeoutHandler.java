package me.opsec.darkmatter;

import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimeoutHandler extends BroadcastReceiver {

    private static final int HOUR = 60 * 60 * 1000;

    public static void updateTimeout(Context context) {
        Long time = new GregorianCalendar().getTimeInMillis() + 12 * HOUR;
        Intent intentAlarm = new Intent(context, TimeoutHandler.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, 42,
                intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: Code to run after X hours of inactivity
    }
}
