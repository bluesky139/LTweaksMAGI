package li.lingfeng.magi.tweaks.system;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;
import li.lingfeng.magi.utils.ViewUtils;

import static li.lingfeng.magi.utils.ReflectUtils.findClass;

@AppLoad(packageName = PackageNames.ANDROID_UI, pref = "direct_share_disable")
public class DirectShareDisable extends TweakBase {

    private static final String CHOOSER_ACTIVITY = "com.android.internal.app.ChooserActivity";

    @Override
    public void load() {
        super.load();
        try {
            ReflectUtils.setStaticBooleanField(findClass("com.android.internal.app.ChooserFlags"), "USE_PREDICTION_MANAGER_FOR_DIRECT_TARGETS", false);
            ReflectUtils.setStaticBooleanField(findClass(CHOOSER_ACTIVITY), "USE_PREDICTION_MANAGER_FOR_SHARE_ACTIVITIES", false);
        } catch (Throwable e) {
            Logger.e("Exception on ChooserFlags", e);
        }
    }

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (!activity.getClass().getName().equals(CHOOSER_ACTIVITY)) {
            return;
        }
        ViewGroup pager = ViewUtils.findViewByName(activity, "profile_pager");
        if (pager == null) {
            return;
        }
        pager.getViewTreeObserver().addOnGlobalLayoutListener(() -> Loader.getMainHandler().post(() -> {
            View view = ViewUtils.findViewByName(pager, "chooser_row_text_option");
            if (view != null) {
                View v = ((ViewGroup) ((ViewGroup) view.getParent()).getParent());
                if (v.getLayoutParams().height != 0) {
                    Logger.v("Try set direct share row to 0 height.");
                    v.getLayoutParams().height = 0;
                    v.requestLayout();
                }
            }
        }));
    }
}
