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
    	proc = runProcess("tc create %s %s", volumePath, mountPath);
    	proc.write("%s", pass1);
    	proc.write("%s", pass2);
    	
    	if (proc.exit_value() != 0) {
    		alert_user("failed to create volume: %s", volumePath);
    	}
        // TODO: Implement
        SystemClock.sleep(5000);

        // Example how to execute command that does not require root
        List<String> result = Shell.SH.run("ls -l /");

        // Example how to execute command that requires root
        result = Shell.SU.run("ls -l /");
    }

    public void open(String volumePath, String mountPath, string passwd) {
        // TODO: Implement
    	proc = runProcess("tc open %s %s", volumePath, mountPath);
    	proc.write("%s", string);
    	if (proc.exit_value() != 0) {
    		alert_user("Error opening: %s. Incorrect password?", volumePath);
    	}
        SystemClock.sleep(5000);
    }

    public void close(String volumePath) {
    	Shell.SH.run("tc close %s", volumePath);
    }

    public void delete(String volumePath) {
    	Shell.SH.run("tc delete %s", volumePath);
    }
}