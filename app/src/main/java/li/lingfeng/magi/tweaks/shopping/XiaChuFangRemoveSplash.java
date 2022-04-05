package li.lingfeng.magi.tweaks.shopping;

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

@AppLoad(packageName = PackageNames.XIACHUFANG, pref = "xiachufang_remove_splash")
public class XiaChuFangRemoveSplash extends TweakBase {

    private static final String START_ACTIVITY = "com.xiachufang.startpage.ui.StartPageActivity";
    private static final String HOME_ACTIVITY = "com.xiachufang.activity.home.HomeActivity";

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (activity.getClass().getName().equals(START_ACTIVITY)) {
            Logger.d("Skip start activity.");
            Intent intent = new Intent();
            intent.setClassName(activity, HOME_ACTIVITY);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    @Override
    public Result startActivity(IApplicationThread caller, String callingPackage, String callingFeatureId, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        return new Result().before((r) -> {
            if (intent.getComponent() != null && START_ACTIVITY.equals(intent.getComponent().getClassName())) {
                Logger.d("No start activity start.");
                r.setResult(0);
            }
        });
    }
}
