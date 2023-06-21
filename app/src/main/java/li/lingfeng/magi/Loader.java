package li.lingfeng.magi;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.lib.HookMethod;
import li.lingfeng.lib.Type;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.prefs.PrefStore;
import li.lingfeng.magi.tweaks.SystemServer;
import li.lingfeng.magi.tweaks.base.IMethodBase;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.tweaks.proxy.ServiceManagerProxy;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;

public class Loader {

    private static SystemServer sSystemServer;
    private static TweakBase[] sTweaks;
    private static TweakBase[] sHookTweaks;
    public static ServiceManagerProxy sServiceManagerProxy;
    private static Application sApp;
    private static Handler sMainHandler;
    private static boolean sOnAppReady = false;
    private static Set<Method> sToHookMethods;
    private static Map<Class /*hooker*/, Method> sBackups = new HashMap<>();

    public static boolean load(String niceName) {
        Logger.d("Loader.load() " + niceName);
        if (PackageNames.ANDROID.equals(niceName)) {
            /*sSystemServer = new SystemServer();
            onApplicationReady(() -> {
                sSystemServer.load();
            }, niceName);
            return;*/
            return false;
        }
        sTweaks = L.instantiateTweaks(niceName);
        if (sTweaks == null) {
            Logger.e("No tweaks for " + niceName + ", dex should not be loaded.");
            return false;
        }
        sTweaks = Arrays.stream(sTweaks).filter(t -> {
            String key = t.getClass().getAnnotation(AppLoad.class).pref();
            return StringUtils.isEmpty(key) || PrefStore.instance.contains(key);
        }).toArray(TweakBase[]::new);
        sServiceManagerProxy = new ServiceManagerProxy(sTweaks);
        sServiceManagerProxy.proxy();

        // check hook or not
        sHookTweaks = Arrays.stream(sTweaks)
                .filter(t -> t.getClass().getAnnotation(AppLoad.class).hook())
                .toArray(TweakBase[]::new);
        if (sHookTweaks.length > 0) {
            List<Method> allMethods = Arrays.stream(IMethodBase.class.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(HookMethod.class))
                    .collect(Collectors.toList());
            sToHookMethods = new HashSet<>();
            sHookTweaks = Arrays.stream(sTweaks)
                    .filter(t -> {
                        List<Method> methods = allMethods.stream().filter(h -> {
                                    try {
                                        t.getClass().getDeclaredMethod(h.getName(), h.getParameterTypes());
                                        return true;
                                    } catch (NoSuchMethodException e) {
                                        return false;
                                    }
                                })
                                .collect(Collectors.toList());
                        if (methods.size() > 0) {
                            sToHookMethods.addAll(methods);
                            return true;
                        }
                        return false;
                    })
                    .toArray(TweakBase[]::new);
            return sHookTweaks.length > 0;
        }
        return false;
    }

    private static void loadHook() {
        if (sHookTweaks.length == 0) {
            return;
        }
        long startTime = 0;
        if (BuildConfig.DEBUG) {
            startTime = System.currentTimeMillis();
        }

        for (Method method : sToHookMethods) {
            HookMethod hookAnnotation = method.getAnnotation(HookMethod.class);
            String hooker = StringUtils.capitalize(method.getName());
            boolean isStatic = hookAnnotation.isStatic();
            Logger.v("hookMethod " + hooker + "#" + hookAnnotation.method());

            try {
                Class hookerCls = ReflectUtils.findClass("li.lingfeng.magi.tweaks.hook."
                        + hooker, Loader.class.getClassLoader());
                Method[] methods = hookerCls.getDeclaredMethods();
                Method hookMethod = methods[0].getName().equals("hook") ? methods[0] : methods[1];
                Method backupMethod = methods[0] == hookMethod ? methods[1] : methods[0];
                if (BuildConfig.DEBUG) {
                    if (!hookMethod.getName().equals("hook") || !backupMethod.getName().equals("backup")) {
                        throw new Exception("Expect " + hookerCls + " hook/backup methods, but " + ArrayUtils.toString(methods));
                    }
                }

                Class[] hookTypes = method.getParameterTypes();
                Annotation[][] annotationArrays = method.getParameterAnnotations();
                for (int i = 0; i < annotationArrays.length; ++i) {
                    Annotation[] annotations = annotationArrays[i];
                    if (annotations.length == 1) {
                        Type type = (Type) annotations[0];
                        hookTypes[i] = ReflectUtils.findClass(type.name());
                    }
                }

                Class[] targetTypes;
                if (isStatic) {
                    targetTypes = hookTypes;
                } else {
                    targetTypes = new Class[hookTypes.length - 1];
                    System.arraycopy(hookTypes, 1, targetTypes, 0, targetTypes.length);
                }

                Class targetCls = ReflectUtils.findClass(hookAnnotation.cls());
                Method targetMethod = targetCls.getDeclaredMethod(hookAnnotation.method(), targetTypes);
                if (BuildConfig.DEBUG) {
                    if (targetMethod.getReturnType() != hookAnnotation.returnType()) {
                        throw new Exception("return type not match, " + targetMethod.getReturnType()
                                + ", " + hookAnnotation.returnType());
                    }
                }
                hookMethod(targetMethod, hookMethod, backupMethod, hookerCls);
            } catch (Throwable e) {
                Logger.e("Failed to hook " + hookAnnotation.cls() + "#" + hookAnnotation.method()
                        + ", hooker " + hooker, e);
            }
        }
        if (BuildConfig.DEBUG) {
            Logger.d("loadHook took " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    public static TweakBase[] getTweaks() {
        return sTweaks;
    }

    public static TweakBase[] getHookTweaks() {
        return sHookTweaks;
    }

    public static void onApplicationReady() {
        if (!sOnAppReady) {
            sOnAppReady = true;
            loadHook();
        }
    }

    // TODO: add a timeout to stop
    /*private static void onApplicationReady(Runnable runnable, String niceName) {
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
    }*/

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

    private static void hookMethod(Method targetMethod, Method hookMethod, Method backupMethod, Class hookerCls) throws Throwable {
        // backup method may become privat after replacement.
        backupMethod.setAccessible(true);
        sBackups.put(hookerCls, backupMethod);
        Loader.nativeHookMethod(targetMethod, hookMethod, backupMethod);
    }

    public static Object invokeOriginalMethod(Class hookerCls, Object thisObject, Object... args) throws Throwable {
        Method backup = sBackups.get(hookerCls);
        if (backup == null) {
            throw new Exception("invokeOriginalMethod no backup for " + hookerCls + "?");
        }
        return backup.invoke(thisObject, args);
    }

    public static native void nativeHookMethod(Method target, Method hook, Method backup);
}
