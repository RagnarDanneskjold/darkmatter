package me.opsec.darkmatter.service;

import java.util.List;

import android.content.Context;
import android.os.SystemClock;
import eu.chainfire.libsuperuser.Application;
import eu.chainfire.libsuperuser.Shell;

/**
 * Secure storage using TrueCrypt.
 */
public class DarkStorage {

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
    public void create(Context context, String volumePath, int size1, int size2, String pass1,
            String pass2) {

        List<String> result = suRun("tc create %s %s %s %s %s", volumePath, size1, size2, pass1,
                pass2);

        SystemClock.sleep(2000); // TODO: Remove

        if (result == null) {
            Application.toast(context, String.format("Failed to create volume: %s", volumePath));
        }
    }

    public void open(Context context, String volumePath, String mountPath, String passwd) {
        List<String> result = suRun("tc open %s %s %s", volumePath, mountPath, passwd);

        SystemClock.sleep(2000); // TODO: Remove

        if (result == null) {
            Application.toast(context,
                    String.format("Error opening: %s. Incorrect password?", volumePath));
        }
    }

    public void close(String volumePath) {
        List<String> result = suRun("tc close %s", volumePath);
    }

    public void delete(Context context, String volumePath) {
        List<String> result = suRun("tc delete %s", volumePath);
        if (result == null) {
            Application.toast(context, String.format("Error deleting: %s.", volumePath));
        }
    }

    private List<String> suRun(String format, Object... args) {
        String command = String.format(format, args);
        return Shell.SU.run(command);
    }
}