package li.lingfeng.magi.tweaks.proxy;

import android.app.ContentProviderHolder;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import li.lingfeng.magi.tweaks.base.IMethodBase;
import li.lingfeng.magi.tweaks.base.Result;

public class IActivityManagerProxy extends IActivityManager.Stub {

    private IActivityManager mOriginal;
    private IMethodBase[] mImpls;

    public IActivityManagerProxy(IActivityManager original, IMethodBase[] impls) {
        mOriginal = original;
        mImpls = impls;
    }

    @Override
    public void attachApplication(IApplicationThread app, long startSeq) throws RemoteException {
        List<Result> results = new ArrayList<>();
        for (IMethodBase impl : mImpls) {
            Result result = impl.attachApplication(app, startSeq);
            if (result != null) {
                results.add(result);
            }
        }
        for (Result result : results) {
            result.hookBefore();
            if (result.hasResult()) {
                return;
            }
            if (result.args != null) {
                app = result.args.length > 0 && result.args[0] != null ? (IApplicationThread) result.args[0] : app;
                startSeq = result.args.length > 1 && result.args[1] != null ? (long) result.args[1] : startSeq;
            }
        }

        mOriginal.attachApplication(app, startSeq);
        for (Result result : results) {
            result.hookAfter();
            if (result.hasResult()) {
                return;
            }
        }
    }

    @Override
    public ContentProviderHolder getContentProvider(IApplicationThread caller, String callingPackage,
                                                    String name, int userId, boolean stable) throws RemoteException {
        List<Result> results = new ArrayList<>();
        for (IMethodBase impl : mImpls) {
            Result result = impl.getContentProvider(caller, callingPackage, name, userId, stable);
            if (result != null) {
                results.add(result);
            }
        }
        for (Result result : results) {
            result.hookBefore();
            if (result.hasResult()) {
                return (ContentProviderHolder) result.getResult();
            }
            if (result.args != null) {
                caller = result.args.length > 0 && result.args[0] != null ? (IApplicationThread) result.args[0] : caller;
                callingPackage = result.args.length > 1 && result.args[1] != null ? (String) result.args[1] : callingPackage;
                name = result.args.length > 2 && result.args[2] != null ? (String) result.args[2] : name;
                userId = result.args.length > 3 && result.args[3] != null ? (int) result.args[3] : userId;
                stable = result.args.length > 4 && result.args[4] != null ? (boolean) result.args[4] : stable;
            }
        }

        ContentProviderHolder ret = mOriginal.getContentProvider(caller, callingPackage, name, userId, stable);
        for (Result result : results) {
            result.hookAfter();
            if (result.hasResult()) {
                return (ContentProviderHolder) result.getResult();
            }
        }
        return ret;
    }
}
