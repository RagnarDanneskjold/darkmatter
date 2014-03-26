package me.opsec.darkmatter.service;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import eu.chainfire.libsuperuser.Application;
import eu.chainfire.libsuperuser.Shell;

/**
 * Secure storage using TrueCrypt.
 */
public class DarkStorage {

    private Context mAppContext;
    private SharedPreferences mPreferences;

    private static String PREF_VOLUME_PATH = "volume.path";
    private static String PREF_MOUNT_PATH = "mount.path";

    public DarkStorage(Context context) {
        super();
        mAppContext = context.getApplicationContext();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isCreated() {
        String volumePath = getVolumePath();
        return volumePath != null && new File(volumePath).exists();
    }

    public boolean isOpen() {
        String mountPath = getMountPath();
        return mountPath != null && new File(mountPath + "/" + "lost+found").exists();
    }

    /**
     * @param volumePath
     *            full path from root
     * @param size1
     *            in megabytes
     * @param size2
     *            in megabytes
     * @param pass1
     *            outer password
     * @param pass2
     *            hidden password
     */
    public void create(String volumePath, int size1, int size2, String pass1, String pass2) {

        List<String> result = suRun("tc create %s %s %s %s %s", volumePath, size1, size2, pass1,
                pass2);

        if (result == null) {
            Application
                    .toast(mAppContext, String.format("Failed to create volume: %s", volumePath));
        } else {
            mPreferences.edit().putString(PREF_VOLUME_PATH, volumePath).commit();
        }
    }

    public boolean open(String mountPath, String passwd) {
        String volumePath = getVolumePath();
        if (volumePath == null) {
            Application.toast(mAppContext,
                    String.format("Internal error. Volume path not set %s", volumePath));
            return false;
        }

        List<String> result = suRun("tc open %s %s %s", volumePath, mountPath, passwd);

        if (result == null) {
            Application.toast(mAppContext,
                    String.format("Error opening: %s. Incorrect password?", volumePath));
            return false;
        }

        mPreferences.edit().putString(PREF_MOUNT_PATH, mountPath).commit();
        return true;
    }

    public void close() {
        List<String> result = suRun("tc close %s", getVolumePath());
        // TODO: What if this fails? We don't want to show a toast message in some situations, right?
    }

    public void delete() {
        String volumePath = getVolumePath();
        List<String> result = suRun("tc delete %s", volumePath);
        if (result == null) {
            Application.toast(mAppContext, String.format("Error deleting: %s.", volumePath));
        } else {
            mPreferences.edit().remove(PREF_VOLUME_PATH).commit();
        }
    }

    private List<String> suRun(String format, Object... args) {
        String binDir = mAppContext.getFilesDir() + "/bin/";
        String command = binDir + String.format(format, args);
        // String[] environment = new String[] { "PATH=/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin:"
        // + binDir };
        return Shell.SU.run(command);
    }

    private String getVolumePath() {
        return mPreferences.getString(PREF_VOLUME_PATH, null);
    }

    private String getMountPath() {
        return mPreferences.getString(PREF_MOUNT_PATH, null);
    }
}