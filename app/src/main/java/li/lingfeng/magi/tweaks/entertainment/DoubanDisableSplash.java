package li.lingfeng.magi.tweaks.entertainment;

import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.DOUBAN, pref = "douban_disable_splash")
public class DoubanDisableSplash extends TweakBase {

    private static final String SPLASH_ACTIVITY = "com.douban.frodo.splash.SplashActivityHot";

    @Override
    public Result startActivity(IApplicationThread caller, String callingPackage, String callingFeatureId, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        return new Result().before((r) -> {
            if (intent.getComponent() != null && intent.getComponent().getClassName().equals(SPLASH_ACTIVITY)) {
                Logger.v("Disable " + SPLASH_ACTIVITY);
                r.setResult(0);
            }
        });
    }
}
