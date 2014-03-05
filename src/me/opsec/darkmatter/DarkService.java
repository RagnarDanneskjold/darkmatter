package me.opsec.darkmatter;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class DarkService extends IntentService {

    public static final String ACTION_CREATE = "create";
    public static final String ACTION_OPEN = "open";
    public static final String ACTION_CLOSE = "close";
    public static final String ACTION_DELETE = "delete";

    public static final String EXTRA_VOLUME_PATH = "volume.path";
    public static final String EXTRA_SIZE = "size";
    public static final String EXTRA_PASS_1 = "pass1";
    public static final String EXTRA_PASS_2 = "pass2";
    public static final String EXTRA_MOUNT_PATH = "mount.path";

    private static final String MOUNT_POINT = "/mnt/extSdCard";

    public DarkService() {
        super("Dark Matter");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (ACTION_CREATE.equals(action)) {
            String volumePath = extras.getString(EXTRA_VOLUME_PATH);
            int size = extras.getInt(EXTRA_SIZE);
            String pass1 = extras.getString(EXTRA_PASS_1);
            String pass2 = extras.getString(EXTRA_PASS_2);
            create(volumePath, size, pass1, pass2);
        } else if (ACTION_OPEN.equals(action)) {
            String volumePath = extras.getString(EXTRA_VOLUME_PATH);
            open(volumePath);
        } else if (ACTION_CLOSE.equals(action)) {
            String mountPath = extras.getString(EXTRA_MOUNT_PATH);
            close(mountPath);
        } else if (ACTION_DELETE.equals(action)) {
            String mountPath = extras.getString(EXTRA_MOUNT_PATH);
            close(mountPath);
        }
    }

    public void create(String volumePath /* full path from root */, int size, String pass1,
            String pass2) {

    }

    public void open(String volumePath) {

    }

    public void close(String mountPath) {

    }

    public void delete(String volumePath) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not used
    }
}
