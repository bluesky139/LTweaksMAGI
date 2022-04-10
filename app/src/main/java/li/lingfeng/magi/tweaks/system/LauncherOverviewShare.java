package li.lingfeng.magi.tweaks.system;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.PIXEL_LAUNCHER, pref = "launcher_overview_share")
public class LauncherOverviewShare extends TweakBase {

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        View view = ViewUtils.findViewByName(activity, "action_share");
        if (view != null) {
            Logger.i("Set " + view + " to be visible.");
            view.setVisibility(View.VISIBLE);
            view = ViewUtils.findViewByName(activity, "oav_three_button_space");
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
        }
    }
}
