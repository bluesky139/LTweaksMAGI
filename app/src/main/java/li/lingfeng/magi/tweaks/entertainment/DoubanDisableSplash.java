package li.lingfeng.magi.tweaks.entertainment;

import android.app.Activity;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.DOUBAN, pref = "douban_disable_splash")
public class DoubanDisableSplash extends TweakBase {

    private static final String SPLASH_ACTIVITY = "com.douban.frodo.activity.SplashActivity";
    private static final String SPLASH_ACTIVITY_HOT = "com.douban.frodo.splash.SplashActivityHot";

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (activity.getClass().getName().equals(SPLASH_ACTIVITY)) {
            Logger.v("Douban disable splash.");
            Intent intent = activity.getIntent();
            intent.putExtra("show_main", true);
            intent.putExtra("no_splash", true);
        }
    }

    @Override
    public Result startActivity(IApplicationThread caller, String callingPackage, String callingFeatureId, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        return new Result().before(r -> {
            if (intent.getComponent() != null && SPLASH_ACTIVITY_HOT.equals(intent.getComponent().getClassName())) {
                Logger.d("No hot splash activity.");
                r.setResult(0);
            }
        });
    }


}
