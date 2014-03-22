package me.opsec.darkmatter.service;

import eu.chainfire.libsuperuser.Shell;

/**
 * Various security hardening.
 */
public class SecurityRatchet {

    private DarkStorage mStorage;
    private int mLevel = 0;

    public SecurityRatchet(DarkStorage storage) {
        mStorage = storage;
        reset();
    }

    public void increase() {
        ++mLevel;
        process();
    }

    public void reset() {
        mLevel = 0;
        process();
    }

    private void process() {
        if (mLevel == 0) {
            return;
        } else if (mLevel == 1) {
            Shell.SH.run("bin/ratchet");
            // TODO: Use context instead of null
            mStorage.close(null, "mountPath"); // TODO: Get mount path from parameter or settings
        }
    }
}
