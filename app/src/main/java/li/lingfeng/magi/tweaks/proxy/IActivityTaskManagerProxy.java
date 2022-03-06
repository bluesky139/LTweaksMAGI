package li.lingfeng.magi.tweaks.proxy;

import android.app.IActivityManager;
import android.app.IActivityTaskManager;
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

public class IActivityTaskManagerProxy extends IActivityTaskManager.Stub {

    private IActivityTaskManager mOriginal;
    private IMethodBase[] mImpls;

    public IActivityTaskManagerProxy(IActivityTaskManager original, IMethodBase[] impls) {
        mOriginal = original;
        mImpls = impls;
    }

    @Override
    public int startActivity(IApplicationThread caller, String callingPackage, String callingFeatureId,
                             Intent intent, String resolvedType, IBinder resultTo, String resultWho,
                             int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        List<Result> results = new ArrayList<>();
        for (IMethodBase impl : mImpls) {
            Result result = impl.startActivity(caller, callingPackage, callingFeatureId, intent, resolvedType,
                    resultTo, resultWho, requestCode, flags, profilerInfo, options);
            if (result != null) {
                results.add(result);
            }
        }
        for (Result result : results) {
            result.hookBefore();
            if (result.hasResult()) {
                return (int) result.getResult();
            }
            if (result.args != null) {
                caller = result.args.length > 0 && result.args[0] != null ? (IApplicationThread) result.args[0] : caller;
                callingPackage = result.args.length > 1 && result.args[1] != null ? (String) result.args[1] : callingPackage;
                callingFeatureId = result.args.length > 2 && result.args[2] != null ? (String) result.args[2] : callingFeatureId;
                intent = result.args.length > 3 && result.args[3] != null ? (Intent) result.args[3] : intent;
                resolvedType = result.args.length > 4 && result.args[4] != null ? (String) result.args[4] : resolvedType;
                resultTo = result.args.length > 5 && result.args[5] != null ? (IBinder) result.args[5] : resultTo;
                resultWho = result.args.length > 6 && result.args[6] != null ? (String) result.args[6] : resultWho;
                requestCode = result.args.length > 7 && result.args[7] != null ? (int) result.args[7] : requestCode;
                flags = result.args.length > 8 && result.args[8] != null ? (int) result.args[8] : flags;
                profilerInfo = result.args.length > 9 && result.args[9] != null ? (ProfilerInfo) result.args[9] : profilerInfo;
                options = result.args.length > 10 && result.args[10] != null ? (Bundle) result.args[10] : options;
            }
        }

        int ret = mOriginal.startActivity(caller, callingPackage, callingFeatureId, intent, resolvedType,
                resultTo, resultWho, requestCode, flags, profilerInfo, options);
        for (Result result : results) {
            result.hookAfter();
            if (result.hasResult()) {
                return (int) result.getResult();
            }
        }
        return ret;
    }
}
