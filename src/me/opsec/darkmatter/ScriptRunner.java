package me.opsec.darkmatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.util.Log;

/**
 * Helper class for running "res/raw/script.sh"
 */
public class ScriptRunner {

    private static final String TAG = "ScriptRunner";

    private static Context appContext;

    public static void startScript(Context context) {

        appContext = context.getApplicationContext();

        // Run as a separate thread to make sure we don't block the UI thread
        new Thread() {
            @Override
            public void run() {
                runScript();
            }
        }.start();
    }

    private static final void runScript() {

        File destFile = copyScriptToDisk();

        // Set execute permission and run the script
        String scriptPath = destFile.getAbsolutePath();
        List<String> args = Arrays.asList("/system/xbin/chmod", "0700", scriptPath);
        if (exec(args)) {
            exec(scriptPath);
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

    private static boolean exec(String command) {
        List<String> args = Arrays.asList(command);
        return exec(args);
        /*
         * Process process = null; try { process = new
         * ProcessBuilder().command(command).redirectErrorStream(true).start(); process.waitFor(); Log.i(TAG,
         * "Command completed"); return true; } catch (IOException e) { Log.w(TAG, "Error running command: " + command,
         * e); } catch (InterruptedException e) { Log.w(TAG, "Command interrupted: " + command, e); } finally { if
         * (process != null) { process.destroy(); } } return false;
         */
    }

    /**
     * We can't run directly from res/raw, so we copy the file to disk.
     */
    private static File copyScriptToDisk() {
        File destFile = new File(appContext.getFilesDir(), "script.sh");
        if (destFile.exists()) {
            return destFile;
        }

        try {
            InputStream in = appContext.getResources().openRawResource(R.raw.script);
            OutputStream out = new FileOutputStream(destFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Cannot open file for writing", e);
        } catch (IOException e) {
            Log.w(TAG, "Error writing script file", e);
        }
        return destFile;
    }
}