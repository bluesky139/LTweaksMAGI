package li.lingfeng.magi.tweaks.google;

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

@AppLoad(packageName = PackageNames.GMS, pref = "gms_no_update_popup")
public class GMSNoUpdatePopup extends TweakBase {

    private static final String UPDATE_POPUP = "com.google.android.gms.update.phone.PopupDialog";
    private static final String ACTION_UPDATE = "android.settings.SYSTEM_UPDATE_COMPLETE";

    @Override
    public Result startActivity(IApplicationThread caller, String callingPackage, String callingFeatureId, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        return new Result().before((r) -> {
            Logger.d("GMS startActivity.");
            Logger.intent(intent);
            if (ACTION_UPDATE.equals(intent.getAction())
                    || (intent.getComponent() != null && intent.getComponent().getClassName().equals(UPDATE_POPUP))) {
                Logger.v("No update popup.");
                r.setResult(0);
            }
        });
    }
}
