package li.lingfeng.magi.tweaks.base;

import android.app.Activity;
import android.app.Application;
import android.app.IApplicationThread;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.WindowManagerGlobal;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;

public abstract class TweakBase extends IMethodBase implements Application.ActivityLifecycleCallbacks {

    protected Application mApp;
    private static List<TweakBase> sWindowManagerTweaks;

    public void load() {
        interceptWindowManager();
    }

    protected boolean shouldRegisterActivityLifecycle() {
        return false;
    }

    // This method should not be override.
    @Override
    public Result getContentProvider(IApplicationThread caller, String callingPackage, String name, int userId, boolean stable) throws RemoteException {
        return new Result().before((r) -> {
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

    protected boolean shouldInterceptWindowManagerAddView() {
        return false;
    }

    private void interceptWindowManager() {
        if (!shouldInterceptWindowManagerAddView()) {
            return;
        }
        if (sWindowManagerTweaks == null) {
            sWindowManagerTweaks = new ArrayList<>();
            try {
                ArrayList<View> views = (ArrayList<View>) ReflectUtils.getObjectField(WindowManagerGlobal.getInstance(), "mViews");
                if (views.size() > 0) {
                    Logger.e("WindowManagerGlobal exist views " + views);
                    return;
                }
                WindowViewList windowViewList = new WindowViewList();
                ReflectUtils.setObjectField(WindowManagerGlobal.getInstance(), "mViews", windowViewList);
            } catch (Throwable e) {
                Logger.e("Exception on WindowManagerGlobal.mViews", e);
            }
        }
        sWindowManagerTweaks.add(this);
    }

    static class WindowViewList extends ArrayList<View> {
        @Override
        public boolean add(View view) {
            sWindowManagerTweaks.forEach(tweak -> tweak.windowManagerAddView(view));
            return super.add(view);
        }
    }

    protected void windowManagerAddView(View view) {
    }
}
