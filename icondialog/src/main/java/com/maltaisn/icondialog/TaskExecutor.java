package com.maltaisn.icondialog;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;

class TaskExecutor {

    private boolean isRunning;
    private Thread backgroundThread;
    private Handler uiThreadHandler;

    TaskExecutor() {
        uiThreadHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Execute a runnable task in background then call a callback on UI thread
     * @param r runnable task
     * @param cb callback to call when done
     */
    public void execute(final Runnable r, @Nullable final Callback cb) {
        isRunning = true;
        backgroundThread = new Thread("taskExecutor") {
            @Override
            public void run() {
                r.run();
                isRunning = false;
                if (cb != null) {
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cb.onDone();
                        }
                    });
                }
            }
        };
        backgroundThread.start();
    }

    public void interrupt() {
        if (backgroundThread != null && backgroundThread.isAlive()) {
            backgroundThread.interrupt();
        }
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public interface Callback {
        void onDone();
    }

}
