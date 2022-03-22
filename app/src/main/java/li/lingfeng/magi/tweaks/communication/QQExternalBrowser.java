package li.lingfeng.magi.tweaks.communication;

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
import li.lingfeng.magi.utils.ContextUtils;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.Utils;

@AppLoad(packageName = PackageNames.QQ, pref = "qq_external_browser")
public class QQExternalBrowser extends TweakBase {

    private static final String BROWSER_DELEGATED_ACTIVITY = "com.tencent.mobileqq.activity.QQBrowserDelegationActivity";

    @Override
    public Result startActivity(IApplicationThread caller, String callingPackage, String callingFeatureId, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        return new Result().before((r) -> {
            if (intent.getComponent() != null && intent.getComponent().getClassName().equals(BROWSER_DELEGATED_ACTIVITY)) {
                String url = intent.getStringExtra("url");
                if (Utils.isUrl(url)) {
                    Logger.i("QQ url " + url);
                    ContextUtils.startBrowser(mApp, url);
                    r.setResult(0);
                }
            }
        });
    }
}
