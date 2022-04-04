package li.lingfeng.magi;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.prefs.PrefStore;
import li.lingfeng.magi.tweaks.SystemServer;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.tweaks.proxy.ServiceManagerProxy;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;

public class Loader {

    private static SystemServer sSystemServer;
    private static TweakBase[] sTweaks;
    private static ServiceManagerProxy sServiceManagerProxy;
    private static Application sApp;
    private static Handler sMainHandler;

    public static void load(String niceName) {
        Logger.d("Loader.load() " + niceName);
        if (PackageNames.ANDROID.equals(niceName)) {
            /*sSystemServer = new SystemServer();
            onApplicationReady(() -> {
                sSystemServer.load();
            }, niceName);
            return;*/
        }
        sTweaks = L.instantiateTweaks(niceName);
        if (sTweaks == null) {
            Logger.e("No tweaks for " + niceName + ", dex should not be loaded.");
            return;
        }
        sTweaks = Arrays.stream(sTweaks).filter(t -> {
            String key = t.getClass().getAnnotation(AppLoad.class).pref();
            return StringUtils.isEmpty(key) || PrefStore.instance.contains(key);
        }).toArray(TweakBase[]::new);
        sServiceManagerProxy = new ServiceManagerProxy(sTweaks);
        sServiceManagerProxy.proxy();
    }

    // TODO: add a timeout to stop
    private static void onApplicationReady(Runnable runnable, String niceName) {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Application application = getApplication();
                if (application == null) {
                    Logger.v("Wait application ready, " + niceName);
                    pool.schedule(this, 200, TimeUnit.MILLISECONDS);
                    return;
                }
                Handler handler = getMainHandler();
                if (handler == null) {
                    Logger.v("Wait main looper ready, " + niceName);
                    pool.schedule(this, 200, TimeUnit.MILLISECONDS);
                    return;
                }
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

    public static Handler getMainHandler() {
        if (sMainHandler != null) {
            return sMainHandler;
        }
        Looper looper = Looper.getMainLooper();
        if (looper == null) {
            return null;
        }
        sMainHandler = new Handler(looper);
        return sMainHandler;
    }

    public interface OnReadyRunnable {
        boolean run();
        void onReady();
    }

    public static void onMainReady(OnReadyRunnable runnable, long interval) {
        sMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (runnable.run()) {
                    runnable.onReady();
                } else {
                    sMainHandler.postDelayed(this, interval);
                }
            }
        }, interval);
    }
}
