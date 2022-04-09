package li.lingfeng.magi.tweaks.system;

import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.prefs.PrefStore;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.ANDROID_UI, pref = "system_share_filter")
public class ShareFilter extends TweakBase {

    @Override
    public Result queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
        return new Result().after((r) -> {
            if (!Intent.ACTION_SEND.equals(intent.getAction())) {
                return;
            }
            Set<String> filter = PrefStore.instance.getStringSet("system_share_filter", null);
            if (filter == null) {
                return;
            }
            List<ResolveInfo> infos = ((ParceledListSlice<ResolveInfo>) r.getResult()).getList();
            Iterator<ResolveInfo> it = infos.iterator();
            int i = 0;
            while (it.hasNext()) {
                ResolveInfo info = it.next();
                if (filter.contains(info.activityInfo.packageName + '/' + info.activityInfo.name)) {
                    it.remove();
                    ++i;
                }
            }
            Logger.v("ShareFilter removed " + i + " items.");
        });
    }
}
