package com.meidusa.venus.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileWatchdog extends Thread {
    private static Logger logger = LoggerFactory.getLogger(FileWatchdog.class);
    /**
     * The default delay between every file modification check, set to 60 seconds.
     */
    static final public long DEFAULT_DELAY = 60000;

    /**
     * The delay to observe between every check. By default set {@link #DEFAULT_DELAY}.
     */
    protected long delay = DEFAULT_DELAY;

    File[] files;
    long lastModif = 0;
    boolean warnedAlready = false;
    boolean interrupted = false;

    protected FileWatchdog(File... files) {
        long last = 0;
        if (files == null || files.length == 0) {
            throw new NullPointerException("create fileWatchDog error,File is null");
        }
        for (File file : files) {
            last = (file.lastModified() > last ? file.lastModified() : last);
        }
        this.files = files;
        setDaemon(true);
        checkAndConfigure();
    }

    /**
     * Set the delay to observe between each check of the file changes.
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }

    abstract protected void doOnChange();

    protected void checkAndConfigure() {
        boolean needFire = false;
        for (File file : files) {
            boolean fileExists;
            try {
                fileExists = file.exists();
            } catch (SecurityException e) {
                logger.warn("Was not allowed to read check file existance, file:[" + file.getAbsolutePath() + "].");
                interrupted = true; // there is no point in continuing
                return;
            }

            if (fileExists) {
                long l = file.lastModified(); // this can also throw a
                                              // SecurityException
                if (l > lastModif) { // however, if we reached this point this
                    lastModif = l; // is very unlikely.
                    needFire = true;
                }
            } else {
                if (!warnedAlready) {
                    logger.debug("[" + file.getAbsolutePath() + "] does not exist.");
                    warnedAlready = true;
                }
            }
        }

        if (needFire) {
            doOnChange();
            warnedAlready = false;
        }
    }

    public void run() {
        while (!interrupted) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                // no interruption expected
            }
            checkAndConfigure();
        }
    }
}
