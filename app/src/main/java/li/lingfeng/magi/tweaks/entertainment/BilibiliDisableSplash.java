package li.lingfeng.magi.tweaks.entertainment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.BILIBILI, pref = "bilibili_disable_splash")
public class BilibiliDisableSplash extends TweakBase {

    private static final String MAIN_ACTIVITY = "tv.danmaku.bili.MainActivityV2";

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
}
