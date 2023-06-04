package li.lingfeng.magi.tweaks.proxy;

import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.ParceledListSlice;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import li.lingfeng.magi.tweaks.base.IMethodBase;
import li.lingfeng.magi.tweaks.base.Result;

public class IPackageManagerProxy extends IPackageManager.Stub {

    private IPackageManager mOriginal;
    private IMethodBase[] mImpls;

    public IPackageManagerProxy(IPackageManager original, IMethodBase[] impls) {
        mOriginal = original;
        mImpls = impls;
    }

    @Override
    public void setSplashScreenTheme(String packageName, String themeName, int userId) throws RemoteException {
        mOriginal.setSplashScreenTheme(packageName, themeName, userId);
    }

    @Override
    public ParceledListSlice queryIntentActivities(Intent intent, String resolvedType, long flags, int userId) throws RemoteException {
        List<Result> results = new ArrayList<>();
        for (IMethodBase impl : mImpls) {
            Result result = impl.queryIntentActivities(intent, resolvedType, flags, userId);
            if (result != null) {
                results.add(result);
            }
        }
        for (Result result : results) {
            result.hookBefore();
            if (result.hasResult()) {
                return (ParceledListSlice) result.getResult();
            }
            if (result.args != null) {
                intent = result.args.length > 0 && result.args[0] != null ? (Intent) result.args[0] : intent;
                resolvedType = result.args.length > 1 && result.args[1] != null ? (String) result.args[1] : resolvedType;
                flags = result.args.length > 2 && result.args[2] != null ? (int) result.args[2] : flags;
                userId = result.args.length > 3 && result.args[3] != null ? (int) result.args[3] : userId;
            }
        }

        ParceledListSlice ret = mOriginal.queryIntentActivities(intent, resolvedType, flags, userId);
        for (Result result : results) {
            result.setResultSilently(ret);
            result.hookAfter();
            if (result.hasResult()) {
                return (ParceledListSlice) result.getResult();
            }
        }
        return ret;
    }
}
