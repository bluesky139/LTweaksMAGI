package li.lingfeng.magi.tweaks.entertainment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.DOUBAN, pref = "douban_disable_splash")
public class DoubanDisableSplash extends TweakBase {

    private static final String SPLASH_ACTIVITY = "com.douban.frodo.activity.SplashActivity";
    private static final String MAIN_ACTIVITY = "com.douban.frodo.MainActivity";

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (activity.getClass().getName().equals(SPLASH_ACTIVITY)) {
            Logger.v("Skip splash.");
            Intent intent = new Intent();
            intent.setClassName(activity, MAIN_ACTIVITY);
            activity.startActivity(intent);
            activity.finish();
        }
    }
}
