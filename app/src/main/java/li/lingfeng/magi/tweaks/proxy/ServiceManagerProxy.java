package li.lingfeng.magi.tweaks.proxy;

import android.app.IActivityManager;
import android.app.IActivityTaskManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IServiceManager;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;

public class ServiceManagerProxy {

    private static String[] NAMES = new String[] {
            "activity",
            "activity_task"
    };
    private Map<String, Binder> mProxyMap = new HashMap<>(NAMES.length);
    private TweakBase[] mTweaks;

    public ServiceManagerProxy(TweakBase[] tweaks) {
        mTweaks = tweaks;
    }

    public void proxy() {
        try {
            Object original = ReflectUtils.callStaticMethod(ServiceManager.class, "getIServiceManager");
            Object proxy = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{IServiceManager.class}, (_proxy, method, args) -> {
                if ("getService".equals(method.getName())) {
                    String name = (String) args[0];
                    //Logger.d("getService " + name);
                    if (ArrayUtils.contains(NAMES, name)) {
                        synchronized (ServiceManagerProxy.this) {
                            IBinder o = (IBinder) method.invoke(original, args);
                            Binder p = mProxyMap.get(name);
                            if (p == null) {
                                Logger.v("New " + name + " service proxy.");
                                if ("activity".equals(name)) {
                                    IActivityManager.Stub serviceProxy = new IActivityManagerProxy(IActivityManager.Stub.asInterface(o), mTweaks);
                                    p = newServiceProxy(serviceProxy, o);
                                } else if ("activity_task".equals(name)) {
                                    IActivityTaskManager.Stub serviceProxy = new IActivityTaskManagerProxy(IActivityTaskManager.Stub.asInterface(o), mTweaks);
                                    p = newServiceProxy(serviceProxy, o);
                                }
                                mProxyMap.put(name, p);
                            }
                            return p;
                        }
                    }
                }
                return method.invoke(original, args);
            });
            ReflectUtils.setStaticObjectField(ServiceManager.class, "sServiceManager", proxy);
        } catch (Throwable e) {
            Logger.e("Failed to proxy ServiceManager.", e);
        }
    }

    private Binder newServiceProxy(Binder proxy, IBinder original) throws Throwable {
        Method[] methods = proxy.getClass().getDeclaredMethods();
        Set<Integer> transactCodes = new HashSet<>();
        for (Method m : methods) {
            Field code = proxy.getClass().getSuperclass().getDeclaredField("TRANSACTION_" + m.getName());
            code.setAccessible(true);
            int c = code.getInt(proxy);
            transactCodes.add(c);
        }
        return new Binder() {
            @Override
            protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
                //Logger.d("onTransact " + code + ", " + proxy.getClass().getSimpleName());
                if (transactCodes.contains(code)) {
                    //Logger.d("it's transactCodes " + code);
                    return proxy.transact(code, data, reply, flags);
                }
                return original.transact(code, data, reply, flags);
            }

            @Override
            public void attachInterface(@Nullable IInterface owner, @Nullable String descriptor) {
            }

            @Nullable
            @Override
            public IInterface queryLocalInterface(@NonNull String descriptor) {
                return null;
            }

            @Nullable
            @Override
            public String getInterfaceDescriptor() {
                try {
                    return original.getInterfaceDescriptor();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean pingBinder() {
                return original.pingBinder();
            }

            @Override
            public boolean isBinderAlive() {
                return original.isBinderAlive();
            }
        };
    }
}
