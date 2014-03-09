package me.opsec.darkmatter.service;

import java.util.GregorianCalendar;

import me.opsec.darkmatter.receiver.TimeoutHandler;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Service for dispatching events in a background thread.
 */
public class DarkService extends IntentService {

    public static final String ACTION_CREATE = "create";
    public static final String ACTION_OPEN = "open";
    public static final String ACTION_CLOSE = "close";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_PASSWORD_FAIL = "fail";
    public static final String ACTION_PASSWORD_SUCCESS = "success";
    public static final String ACTION_TIMEOUT = "timeout";
    public static final String ACTION_REBOOTED = "rebooted";

    public static final String EXTRA_VOLUME_PATH = "volume.path";
    public static final String EXTRA_SIZE = "size";
    public static final String EXTRA_PASS_1 = "pass1";
    public static final String EXTRA_PASS_2 = "pass2";
    public static final String EXTRA_MOUNT_PATH = "mount.path";

    private static final String MOUNT_POINT = "/mnt/extSdCard";

    private static final int NOTIFICATION_ID = 42;

    private static final int TIMEOUT_REQUEST_ID = 42;
    private static final int HOUR = 60 * 60 * 1000;

    private DarkStorage mStorage;
    private SecurityRatchet mRatchet;

    private AlarmManager mAlarmManager;
    private PendingIntent mTimeoutIntent;

    { // Static initialization
        initEnvironment();
    }

    public DarkService() {
        super("Dark Matter");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStorage = new DarkStorage();
        mRatchet = new SecurityRatchet(mStorage);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (ACTION_CREATE.equals(action)) {
            startForeground();
            String volumePath = extras.getString(EXTRA_VOLUME_PATH);
            int size = extras.getInt(EXTRA_SIZE);
            String pass1 = extras.getString(EXTRA_PASS_1);
            String pass2 = extras.getString(EXTRA_PASS_2);
            mStorage.create(volumePath, size, pass1, pass2);
        } else if (ACTION_OPEN.equals(action)) {
            startForeground();
            String volumePath = extras.getString(EXTRA_VOLUME_PATH);
            mStorage.open(volumePath);
            restartTimeout(); // TODO: Only restart the timeout if password successful
        } else if (ACTION_CLOSE.equals(action)) {
            String mountPath = extras.getString(EXTRA_MOUNT_PATH);
            mStorage.close(mountPath);
        } else if (ACTION_DELETE.equals(action)) {
            String volumePath = extras.getString(EXTRA_VOLUME_PATH);
            mStorage.delete(volumePath);
        } else if (ACTION_REBOOTED.equals(action)) {
            mRatchet.increase(); // TODO: Verify that this is correct
        } else if (ACTION_TIMEOUT.equals(action)) {
            mRatchet.increase();
        } else if (ACTION_PASSWORD_FAIL.equals(action)) {
            mRatchet.increase();
            clearTimeout();
        } else if (ACTION_PASSWORD_SUCCESS.equals(action)) {
            mRatchet.reset();
            restartTimeout();
        }

    }

    private static void initEnvironment() {
        // check to see if we have files installed already, if so, return
        // if (File.open("bin/script.sh").access()) { return; }

        // no files, ok, lets install:
        // copy script to "bin/", set permissions to the permission value
        // install_script_to_bin("script.sh", "bin/script.sh", "0755");
        // install_script_to_bin("tcplay", "bin/tcplay", "0755");
        // install_script_to_bin("tc.sh", "bin/tc.sh", "0755");
    }

    private void restartTimeout() {
        // TODO: Make configurable?
        long time = new GregorianCalendar().getTimeInMillis() + 12 * HOUR;

        Intent intent = new Intent(this, TimeoutHandler.class);
        mTimeoutIntent = PendingIntent.getBroadcast(this, TIMEOUT_REQUEST_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, time, mTimeoutIntent);
    }

    private void clearTimeout() {
        mAlarmManager.cancel(mTimeoutIntent);
    }

    private void startForeground() {
        Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Dark Matter");
        builder.setContentText("Processing...");
        builder.setSmallIcon(android.R.drawable.stat_notify_sync_noanim);
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not used
    }
}
