package li.lingfeng.magi.tweaks.base;

import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import li.lingfeng.lib.HookMethod;

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

    public Result queryIntentActivities(Intent intent, String resolvedType, long flags, int userId) throws RemoteException {
        return null;
    }

    @HookMethod(
            cls = "com.android.quickstep.AbsSwipeUpHandler",
            method = "handleNormalGestureEnd"
    )
    public Result launcherHandleNormalGestureEnd(Object thisObject, float f3, boolean z2, PointF pointF, boolean z3) {
        return null;
    }

    @HookMethod(
            cls = "com.android.launcher3.PagedView",
            method = "getDestinationPage",
            returnType = int.class
    )
    public Result launcherGetDestinationPage(Object thisObject) {
        return null;
    }

    @HookMethod(
            cls = "com.android.systemui.navigationbar.NavigationBarView",
            method = "setNavigationIconHints"
    )
    public Result navBarViewSetNavigationIconHints(Object thisObject, int hints) {
        return null;
    }

    @HookMethod(
            cls = "com.android.systemui.navigationbar.NavigationBarInflaterView",
            method = "getDefaultLayout",
            returnType = String.class
    )
    public Result navBarInflaterViewGetDefaultLayout(Object thisObject) {
        return null;
    }

    @HookMethod(
            cls = "com.android.systemui.navigationbar.buttons.KeyButtonView",
            method = "setImageDrawable"
    )
    public Result keyButtonViewSetImageDrawable(Object thisObject, Drawable drawable) {
        return null;
    }
}
