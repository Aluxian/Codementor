package com.aluxian.codementor.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.aluxian.codementor.BuildConfig;
import com.crashlytics.android.Crashlytics;

import bolts.Task;

public class ErrorHandler {

    private static final String TAG_INIT = "> ";
    private Context context;

    public ErrorHandler(Context context) {
        this.context = context;
    }

    /**
     * Log the given debug exception.
     *
     * @param e The error to log.
     */
    public static void logDebug(String message, Exception e) {
        Log.e(tag(), message, e);
        reportToCrashlytics(e);
    }

    /**
     * Log the given warning exception.
     *
     * @param e The error to log.
     */
    public static void logWarn(Exception e) {
        logWarn(e, true);
    }

    /**
     * Log the given warning exception.
     *
     * @param e                   The error to log.
     * @param reportToCrashlytics Whether the exception should be sent to Crashlytics.
     */
    public static void logWarn(Exception e, boolean reportToCrashlytics) {
        Log.w(tag(), e);
        if (reportToCrashlytics) {
            reportToCrashlytics(e);
        }
    }

    /**
     * Log the given exception.
     *
     * @param e The error to log.
     */
    public static void logError(Exception e) {
        if (e == null) {
            return;
        }

        Log.e(tag(), e.getMessage(), e);
        reportToCrashlytics(e);
    }

    /**
     * Log the given exception and show a toast with an error message.
     *
     * @param e The error to log.
     */
    public void logAndToast(Exception e) {
        if (e == null) {
            return;
        }

        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(tag(), e.getMessage(), e);
        reportToCrashlytics(e);
    }

    /**
     * A simple task continuation which logs the error and shows a toast.
     */
    public static <T> T logErrorTask(Task<T> task) {
        if (task.isFaulted()) {
            logError(task.getError());
        }

        return task.getResult();
    }

    /**
     * A simple task continuation which logs the error and shows a toast.
     */
    public <T> T logAndToastTask(Task<T> task) {
        if (task.isFaulted()) {
            logAndToast(task.getError());
        }

        return task.getResult();
    }

    /**
     * @return The calling class and method name to be used as the logcat tag.
     */
    private static String tag() {
        if (!BuildConfig.DEBUG) {
            return "ERROR";
        }

        StackTraceElement element = new Exception().getStackTrace()[2];

        String clazz = element.getClassName().replace(BuildConfig.APPLICATION_ID + ".", "");
        String method = element.getMethodName();
        int line = element.getLineNumber();

        return TAG_INIT + clazz + "::" + method + "@L" + line;
    }

    private static void reportToCrashlytics(Throwable throwable) {
        if (!BuildConfig.DEBUG) {
            Crashlytics.logException(throwable);
        }
    }

}
