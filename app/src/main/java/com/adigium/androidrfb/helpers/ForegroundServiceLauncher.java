package com.adigium.androidrfb.helpers;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;

public class ForegroundServiceLauncher
{
    private static volatile ForegroundServiceLauncher foregroundServiceLauncher;

    public static ForegroundServiceLauncher getInstance()
    {
        if (foregroundServiceLauncher == null)
        {
            synchronized (ForegroundServiceLauncher.class)
            {
                if (foregroundServiceLauncher == null)
                {
                    foregroundServiceLauncher = new ForegroundServiceLauncher();
                }
            }
        }
        return foregroundServiceLauncher;
    }

    private ForegroundServiceLauncher()
    {
        //Prevent form the reflection api.
        if (foregroundServiceLauncher != null)
        {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public synchronized void startService(Context context)
    {

        if (!isServiceRunning(context))
        {
            if (isOreoOrHigher())
            {
                context.startForegroundService(new Intent(context, MediaProjectionManager.class));
            }
            else
            {
                context.startService(new Intent(context, MediaProjectionManager.class));
            }
        }

    }

    public synchronized void stopService(Context context)
    {
        context.stopService(new Intent(context, MediaProjectionManager.class));
    }

    private boolean isOreoOrHigher()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean isServiceRunning(Context ctx) {

        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MediaProjectionManager.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
