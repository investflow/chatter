package ru.investflow.android.chat.util;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import org.slf4j.Logger;

public final class ThreadUtils {

    private static final Logger log = AppUtils.getLogger(ThreadUtils.class);

    public static boolean isOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void runOnMainThread(Runnable runnable) {
        if (isOnMainThread()) {
            runnable.run();
            return;
        }
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static void runOnBackgroundThread(final Runnable action) {
        if (isOnMainThread()) {
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(final Void... params) {
                    try {
                        action.run();
                    } catch (Exception e) {
                        log.error("runOnBackgroundThread failed!", e);
                    }
                    return null;
                }
            }.execute();
        } else {
            action.run();
        }
    }
}
