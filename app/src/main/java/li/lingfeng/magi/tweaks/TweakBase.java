package li.lingfeng.magi.tweaks;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TweakBase implements Application.ActivityLifecycleCallbacks {

    protected Application mApp;

    public void load(Application app) {
        mApp = app;
        if (shouldRegisterActivityLifecycle()) {
            app.registerActivityLifecycleCallbacks(this);
        }
    }

    protected boolean shouldRegisterActivityLifecycle() {
        return false;
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
}
