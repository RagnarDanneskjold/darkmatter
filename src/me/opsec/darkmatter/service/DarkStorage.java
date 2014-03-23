package me.opsec.darkmatter.service;

import java.util.List;

import android.content.Context;
import eu.chainfire.libsuperuser.Application;
import eu.chainfire.libsuperuser.Shell;

/**
 * Secure storage using TrueCrypt.
 */
public class DarkStorage {

    private Context mAppContext;

    public DarkStorage(Context context) {
        super();
        mAppContext = context.getApplicationContext();
    }

    /**
     * @param context
     * @param volumePath
     *            full path from root
     * @param mountPath
     * @param size
     *            in megabytes
     * @param pass1
     * @param pass2
     */
    public void create(String volumePath, int size1, int size2, String pass1, String pass2) {

        List<String> result = suRun("tc create %s %s %s %s %s", volumePath, size1, size2, pass1,
                pass2);

        if (result == null) {
            Application
                    .toast(mAppContext, String.format("Failed to create volume: %s", volumePath));
        }
    }

    public void open(String mountPath, String passwd) {
        String volumePath = mAppContext.getFilesDir() + "/volume.dat";
        List<String> result = suRun("tc open %s %s %s", volumePath, mountPath, passwd);

        if (result == null) {
            Application.toast(mAppContext,
                    String.format("Error opening: %s. Incorrect password?", volumePath));
        }
    }

    public void close() {
        // TODO: Get mount path from parameter or settings
        List<String> result = suRun("tc close %s", mAppContext.getFilesDir() + "/volume.dat");
    }

    public void delete() {
        String volumePath = mAppContext.getFilesDir() + "/volume.dat";
        List<String> result = suRun("tc delete %s", volumePath);
        if (result == null) {
            Application.toast(mAppContext, String.format("Error deleting: %s.", volumePath));
        }
    }

    private List<String> suRun(String format, Object... args) {
        String binDir = mAppContext.getFilesDir() + "/bin/";
        String command = binDir + String.format(format, args);
        // String[] environment = new String[] { "PATH=/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin:"
        // + binDir };
        return Shell.SU.run(command);
    }
}