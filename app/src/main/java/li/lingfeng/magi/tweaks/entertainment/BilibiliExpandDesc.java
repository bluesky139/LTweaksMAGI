package li.lingfeng.magi.tweaks.entertainment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.R;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.BILIBILI, pref = "bilibili_expand_desc")
public class BilibiliExpandDesc extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "com.bilibili.video.videodetail.VideoDetailsActivity";

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (activity.getClass().getName().equals(VIDEO_DETAILS_ACTIVITY)) {
            handleActivity(activity);
        }
    }

    private void handleActivity(Activity activity) {
        final ViewGroup rootView = activity.findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean end = false;
                try {
                    final View arrow = ViewUtils.findViewByName(rootView, "arrow");
                    if (arrow != null) {
                        new Handler().post(() -> {
                            try {
                                Logger.i("Expand desc.");
                                ViewGroup parent = (ViewGroup) arrow.getParent();
                                parent.performClick();
                                TextView desc = ViewUtils.findViewByName(parent, "desc");
                                desc.setTextColor(0xFFA3A3A3);
                            } catch (Throwable e) {
                                Logger.e("handleDesc error, " + e);
                            }
                        });
                        end = true;
                    }
                } catch (Throwable e) {
                    Logger.e("Find desc error, " + e);
                    end = true;
                } finally {
                    if (end) {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            }
        });
    }
}
