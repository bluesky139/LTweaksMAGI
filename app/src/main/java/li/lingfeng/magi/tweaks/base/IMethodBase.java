package li.lingfeng.magi.tweaks.base;

import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

public abstract class IMethodBase {

    public Result attachApplication(IApplicationThread app, long startSeq) throws RemoteException {
        return null;
    }

    public Result getContentProvider(IApplicationThread caller, String callingPackage,
                                                    String name, int userId, boolean stable) throws RemoteException {
        return null;
    }

    public Result startActivity(IApplicationThread caller, String callingPackage,
                      String callingFeatureId, Intent intent, String resolvedType,
                      IBinder resultTo, String resultWho, int requestCode,
                      int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        return null;
    }

    public Result queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
        return null;
    }
}
