package me.opsec.darkmatter.service;

import java.util.GregorianCalendar;

import me.opsec.darkmatter.receiver.TimeoutHandler;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import eu.chainfire.libsuperuser.Shell;

/**
 * Various security hardening.
 */
public class SecurityRatchet {

    private DarkStorage mStorage;
    private int mLevel = 0;

    private static final int TIMEOUT_REQUEST_ID = 42;
    private static final int HOUR = 60 * 60 * 1000;

    private Context mAppContext;
    private AlarmManager mAlarmManager;
    private PendingIntent mTimeoutIntent;

    public SecurityRatchet(Context context, DarkStorage storage) {
        mStorage = storage;
        mAppContext = context.getApplicationContext();
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        reset();
    }

    public void increase() {
        ++mLevel;
        process();
    }

    public void reset() {
        mLevel = 0;
        process();
    }

    private void process() {
        if (mLevel == 0) {
            restartTimeout();
        } else if (mLevel == 1) {
            Shell.SH.run("bin/ratchet");
            mStorage.close();
            clearTimeout();
        }
    }

    private void restartTimeout() {
        // TODO: Make configurable
        long time = new GregorianCalendar().getTimeInMillis() + 12 * HOUR;

        Intent intent = new Intent(mAppContext, TimeoutHandler.class);
        mTimeoutIntent = PendingIntent.getBroadcast(mAppContext, TIMEOUT_REQUEST_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, time, mTimeoutIntent);
    }

    private void clearTimeout() {
        mAlarmManager.cancel(mTimeoutIntent);
    }
}
