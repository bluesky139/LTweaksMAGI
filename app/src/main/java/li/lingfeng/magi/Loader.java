package li.lingfeng.magi;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import li.lingfeng.magi.tweaks.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;

public class Loader {

    private static TweakBase[] sTweaks;
    private static Application sApp;

    public static void load(String niceName) {
        Logger.d("Loader.load() " + niceName);
        sTweaks = L.instantiateTweaks(niceName);
        if (sTweaks == null) {
            Logger.e("No tweaks for " + niceName + ", dex should not be loaded.");
            return;
        }
        onApplicationReady(() -> {
            for (TweakBase tweak : sTweaks) {
                Logger.v("Load " + tweak.getClass() + " for " + niceName);
                tweak.load(sApp);
            }
        });
    }

    private static void onApplicationReady(Runnable runnable) {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Application application = getApplication();
                if (application == null) {
                    Logger.v("Wait application ready.");
                    pool.schedule(this, 200, TimeUnit.MILLISECONDS);
                    return;
                }
                Looper looper = Looper.getMainLooper();
                if (looper == null) {
                    Logger.v("Wait main looper ready.");
                    pool.schedule(this, 200, TimeUnit.MILLISECONDS);
                    return;
                }
                Handler handler = new Handler(looper);
                handler.post(runnable);
            }
        };
        pool.submit(task);
    }

    public static Application getApplication() {
        if (sApp != null) {
            return sApp;
        }
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            sApp = (Application) ReflectUtils.callStaticMethod(activityThread, "currentApplication");
            return sApp;
        } catch (Throwable e) {
            return null;
        }
    }
}
