package me.opsec.darkmatter.service;

import java.util.List;

import android.os.SystemClock;
import eu.chainfire.libsuperuser.Shell;

/**
 * Secure storage using TrueCrypt.
 */
public class DarkStorage {

    public void create(String volumePath /* full path from root */, int size, String pass1,
            String pass2) {
        // TODO: Implement
        SystemClock.sleep(5000);

        // Example how to execute command that does not require root
        List<String> result = Shell.SH.run("ls -l /");

        // Example how to execute command that requires root
        result = Shell.SU.run("ls -l /");
    }

    public void open(String volumePath) {
        // TODO: Implement
        SystemClock.sleep(5000);
    }

    public void close(String mountPath) {
        // TODO: Implement
    }

    public void delete(String volumePath) {
        // TODO: Implement
    }
}