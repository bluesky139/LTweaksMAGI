package li.lingfeng.magi.tweaks.base;

import android.app.Activity;
import android.app.Application;
import android.app.IApplicationThread;
import android.os.Bundle;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.tweaks.base.IMethodBase;
import li.lingfeng.magi.utils.Logger;

public abstract class TweakBase extends IMethodBase implements Application.ActivityLifecycleCallbacks {

    protected Application mApp;

    public void load() {
    }

    protected boolean shouldRegisterActivityLifecycle() {
        return false;
    }

    // This method should not be override.
    @Override
    public Result getContentProvider(IApplicationThread caller, String callingPackage, String name, int userId, boolean stable) throws RemoteException {
        return new Result().before(() -> {
            if (mApp == null) {
                mApp = Loader.getApplication();
                if (mApp == null) {
                    return;
                }
                Logger.v("Load " + getClass().getSimpleName() + " for " + mApp.getPackageName());
                load();
                if (shouldRegisterActivityLifecycle()) {
                    mApp.registerActivityLifecycleCallbacks(this);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    public Class[] getServiceProxyClasses() {
        return null;
    }
}
