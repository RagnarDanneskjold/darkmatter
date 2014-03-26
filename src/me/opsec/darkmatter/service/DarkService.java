package me.opsec.darkmatter.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import me.opsec.darkmatter.R;
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
import android.util.Log;

/**
 * Service for dispatching events in a background thread.
 */
public class DarkService extends IntentService {

    public static final String TAG = "DarkService";

    public static final String ACTION_CREATE = "create";
    public static final String ACTION_OPEN = "open";
    public static final String ACTION_CLOSE = "close";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_PASSWORD_FAIL = "fail";
    public static final String ACTION_PASSWORD_SUCCESS = "success";
    public static final String ACTION_TIMEOUT = "timeout";
    public static final String ACTION_REBOOTED = "rebooted";
    public static final String ACTION_RADIO_LOST = "radio.lost";

    public static final String EXTRA_VOLUME_PATH = "volume.path";
    public static final String EXTRA_SIZE1 = "size1";
    public static final String EXTRA_SIZE2 = "size2";
    public static final String EXTRA_PASS_1 = "pass1";
    public static final String EXTRA_PASS_2 = "pass2";
    public static final String EXTRA_PASS = "pass";
    public static final String EXTRA_MOUNT_PATH = "mount.path";

    private static final int NOTIFICATION_ID = 42;

    private static final int TIMEOUT_REQUEST_ID = 42;
    private static final int HOUR = 60 * 60 * 1000;

    private DarkStorage mStorage;
    private SecurityRatchet mRatchet;

    private AlarmManager mAlarmManager;
    private PendingIntent mTimeoutIntent;

    private String mBinDirectory;

    public DarkService() {
        super("Dark Matter");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStorage = new DarkStorage(this);
        mRatchet = new SecurityRatchet(mStorage);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        initEnvironment();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (ACTION_CREATE.equals(action)) {
            startForeground();
            String volumePath = extras.getString(EXTRA_VOLUME_PATH);
            int size1 = extras.getInt(EXTRA_SIZE1);
            int size2 = extras.getInt(EXTRA_SIZE2);
            String pass1 = extras.getString(EXTRA_PASS_1);
            String pass2 = extras.getString(EXTRA_PASS_2);
            mStorage.create(volumePath, size1, size2, pass1, pass2);
        } else if (ACTION_OPEN.equals(action)) {
            startForeground();
            String mountPath = extras.getString(EXTRA_MOUNT_PATH);
            String passwd = extras.getString(EXTRA_PASS);
            mStorage.open(mountPath, passwd);
            restartTimeout(); // TODO: Move to ratchet
        } else if (ACTION_CLOSE.equals(action)) {
            mStorage.close();
        } else if (ACTION_DELETE.equals(action)) {
            mStorage.delete();
        } else if (ACTION_REBOOTED.equals(action)) {
            mRatchet.increase();
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

    private void initEnvironment() {
        // Return if files installed already
        mBinDirectory = getFilesDir() + "/bin";
        File script = new File(mBinDirectory, "script.sh");
        if (script.exists()) {
            return;
        }

        // Create bin directory
        new File(mBinDirectory).mkdirs();

        // copy scripts to "bin/" and set permissions to run them
        copyScriptToBin(R.raw.script, "script.sh");
        copyScriptToBin(R.raw.tcplay, "tcplay");
        copyScriptToBin(R.raw.tc, "tc");
        copyScriptToBin(R.raw.ratchet, "ratchet");
        copyScriptToBin(R.raw.smem, "smem");
    }

    private void copyScriptToBin(int resId, String filename) {
        try {
            File destFile = new File(mBinDirectory, filename);
            InputStream in = getResources().openRawResource(resId);
            OutputStream out = new FileOutputStream(destFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            // Set execute permission
            String scriptPath = destFile.getAbsolutePath();
            exec(Arrays.asList("/system/xbin/chmod", "0700", scriptPath));
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Cannot open file for writing", e);
        } catch (IOException e) {
            Log.w(TAG, "Error writing script file", e);
        }
    }

    private static boolean exec(List<String> command) {
        Process process = null;
        try {
            process = new ProcessBuilder().command(command).redirectErrorStream(true).start();
            process.waitFor();
            Log.i(TAG, "Command completed");
            return true;
        } catch (IOException e) {
            Log.w(TAG, "Error running command: " + command, e);
        } catch (InterruptedException e) {
            Log.w(TAG, "Command interrupted: " + command, e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    private void restartTimeout() {
        // TODO: Make configurable? XXX: yes.
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
