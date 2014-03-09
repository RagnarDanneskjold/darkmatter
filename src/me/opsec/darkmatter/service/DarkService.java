package me.opsec.darkmatter.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.Builder;
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

    public static final String EXTRA_VOLUME_PATH = "volume.path";
    public static final String EXTRA_SIZE = "size";
    public static final String EXTRA_PASS_1 = "pass1";
    public static final String EXTRA_PASS_2 = "pass2";
    public static final String EXTRA_MOUNT_PATH = "mount.path";

    private static final String MOUNT_POINT = "/mnt/extSdCard";

    private static final int NOTIFICATION_ID = 42;

    private DarkStorage mStorage;
    private SecurityRatchet mRatchet;

    { // Static initialization
        initEnvironment();
    }

    public DarkService() {
        super("Dark Matter");
        mStorage = new DarkStorage();
        mRatchet = new SecurityRatchet(mStorage);
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
        } else if (ACTION_CLOSE.equals(action)) {
            String mountPath = extras.getString(EXTRA_MOUNT_PATH);
            mStorage.close(mountPath);
        } else if (ACTION_DELETE.equals(action)) {
            String volumePath = extras.getString(EXTRA_VOLUME_PATH);
            mStorage.delete(volumePath);
        } else if (ACTION_PASSWORD_FAIL.equals(action)) {
            mRatchet.increase();
        } else if (ACTION_PASSWORD_SUCCESS.equals(action)) {
            mRatchet.reset();
        }

    }

    private void initEnvironment() {
        // check to see if we have files installed already, if so, return
        // if (File.open("bin/script.sh").access()) { return; }

        // no files, ok, lets install:
        // copy script to "bin/", set permissions to the permission value
        // install_script_to_bin("script.sh", "bin/script.sh", "0755");
        // install_script_to_bin("tcplay", "bin/tcplay", "0755");
        // install_script_to_bin("tc.sh", "bin/tc.sh", "0755");
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
