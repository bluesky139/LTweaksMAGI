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

@AppLoad(packageName = PackageNames.BILIBILI, pref = "bilibili_disable_teen_mode_hint")
public class BilibiliDisableTeenModeHint extends TweakBase {

    private static final String TEEN_MODE_DIALOG_ACTIVITY = "com.bilibili.teenagersmode.ui.TeenagersModeDialogActivity";

    @Override
    public Result startActivity(IApplicationThread caller, String callingPackage, String callingFeatureId, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        return new Result().before((r) -> {
            if (intent.getComponent() != null && intent.getComponent().getClassName().equals(TEEN_MODE_DIALOG_ACTIVITY)) {
                Logger.v("Disable teen mode dialog hint.");
                r.setResult(0);
            }
        });
    }
}
