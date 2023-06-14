package li.lingfeng.magi.tweaks.system;

import android.graphics.PointF;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.PIXEL_LAUNCHER, pref = "launcher_fix_recents", hook = true)
public class LauncherFixRecents extends TweakBase {

    private boolean mInHandleNormalGestureEnd = false;

    @Override
    public Result launcherHandleNormalGestureEnd(Object thisObject, float f3, boolean z2, PointF pointF, boolean z3) {
        return new Result().before(r -> {
            mInHandleNormalGestureEnd = true;
        }).after(r -> {
            mInHandleNormalGestureEnd = false;
        });
    }

    @Override
    public Result launcherGetDestinationPage(Object thisObject) {
        return new Result().after(r -> {
            if (mInHandleNormalGestureEnd && r.getIntResult() != 0) {
                Logger.v("In handleNormalGestureEnd, getDestinationPage return 0.");
                r.setResult(0);
            }
        });
    }
}
