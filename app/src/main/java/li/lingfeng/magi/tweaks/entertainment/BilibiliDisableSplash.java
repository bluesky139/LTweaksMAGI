package li.lingfeng.magi.tweaks.entertainment;

import android.app.Activity;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.BILIBILI, pref = "bilibili_disable_splash")
public class BilibiliDisableSplash extends TweakBase {

    private static final String MAIN_ACTIVITY = "tv.danmaku.bili.MainActivityV2";
    private static final String HOT_SPLASH_ACTIVITY = "tv.danmaku.bili.ui.splash.ad.page.HotSplashActivity";

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (!activity.getClass().getName().equals(MAIN_ACTIVITY)) {
            return;
        }
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ViewGroup adView = ViewUtils.findViewByName(rootView, "splash_layout");
                if (adView != null) {
                    Logger.d("hide splash_layout.");
                    adView.setVisibility(View.GONE);
                    adView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                        @Override
                        public void onChildViewAdded(View parent, View child) {
                            Logger.d("remove " + child);
                            ViewUtils.removeView(child);
                            View skipButton = ViewUtils.findViewByName((ViewGroup) child, "count_down");
                            if (skipButton != null) {
                                Logger.d("click skip " + skipButton);
                                skipButton.performClick();
                            }
                        }

                        @Override
                        public void onChildViewRemoved(View parent, View child) {
                        }
                    });
                }

                View splashView = ViewUtils.findViewByName(rootView, "splash_container");
                if (splashView != null) {
                    Logger.d("hide splash_container.");
                    splashView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public Result startActivity(IApplicationThread caller, String callingPackage, String callingFeatureId, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        return new Result().before(r -> {
            if (intent.getComponent() != null && HOT_SPLASH_ACTIVITY.equals(intent.getComponent().getClassName())) {
                Logger.d("No hot splash activity.");
                r.setResult(0);
            }
        });
    }
}
