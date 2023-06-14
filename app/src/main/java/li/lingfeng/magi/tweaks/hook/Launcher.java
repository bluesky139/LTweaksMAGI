package li.lingfeng.magi.tweaks.hook;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

import li.lingfeng.lib.Hooker;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.tweaks.base.IMethodBase;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.utils.Logger;

public class Launcher {

    @Hooker(cls = "com.android.quickstep.AbsSwipeUpHandler", method = "handleNormalGestureEnd")
    public static class HandleNormalGestureEnd {

        public static void hook(Object thisObject, float f3, boolean z2, PointF pointF, boolean z3) {
            List<Result> results = new ArrayList<>();
            for (IMethodBase impl : Loader.getHookTweaks()) {
                Result result = impl.launcherHandleNormalGestureEnd(thisObject, f3, z2, pointF, z3);
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
                    f3 = result.args.length > 0 && result.args[0] != null ? (float) result.args[0] : f3;
                    z2 = result.args.length > 1 && result.args[1] != null ? (boolean) result.args[1] : z2;
                    pointF = result.args.length > 2 && result.args[2] != null ? (PointF) result.args[2] : pointF;
                    z3 = result.args.length > 3 && result.args[3] != null ? (boolean) result.args[3] : z3;
                }
            }

            Object[] args = new Object[] {
                    f3, z2, pointF, z3
            };
            try {
                Loader.invokeOriginalMethod(HandleNormalGestureEnd.class, thisObject, args);
            } /*catch (InvocationTargetException e) {
                throw ((InvocationTargetException) e).getCause();
            }*/ catch (Throwable e) {
                Logger.e("Error to invoke original method for HandleNormalGestureEnd, skip call after.", e);
                return;
            }
            for (Result result : results) {
                result.hookAfter();
                if (result.hasResult()) {
                    return;
                }
            }
        }

        public static void backup(Object thisObject, float f3, boolean z2, PointF pointF, boolean z3) {
            throw new RuntimeException("in backup.");
        }
    }

    @Hooker(cls = "com.android.launcher3.PagedView", method = "getDestinationPage")
    public static class GetDestinationPage {

        public static int hook(Object thisObject) {
            List<Result> results = new ArrayList<>();
            for (IMethodBase impl : Loader.getHookTweaks()) {
                Result result = impl.launcherGetDestinationPage(thisObject);
                if (result != null) {
                    results.add(result);
                }
            }
            for (Result result : results) {
                result.hookBefore();
                if (result.hasResult()) {
                    return (int) result.getResult();
                }
            }

            int ret;
            try {
                ret = (int) Loader.invokeOriginalMethod(GetDestinationPage.class, thisObject);
            } /*catch (InvocationTargetException e) {
                throw ((InvocationTargetException) e).getCause();
            }*/ catch (Throwable e) {
                Logger.e("Error to invoke original method for GetDestinationPage, skip call after.", e);
                return 0;
            }
            for (Result result : results) {
                result.setResultSilently(ret);
                result.hookAfter();
                if (result.hasResult()) {
                    return (int) result.getResult();
                }
            }
            return ret;
        }

        public static int backup(Object thisObject) {
            throw new RuntimeException("in backup.");
        }
    }
}
