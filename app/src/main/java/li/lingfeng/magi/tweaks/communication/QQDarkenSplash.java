package li.lingfeng.magi.tweaks.communication;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.QQ, pref = "qq_darken_splash")
public class QQDarkenSplash extends TweakBase {

    private static final String SPLASH_ACTIVITY = "com.tencent.mobileqq.activity.SplashActivity";
    private static final String JUMP_ACTIVITY = "com.tencent.mobileqq.activity.JumpActivity";

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        String name = activity.getClass().getName();
        if (name.equals(SPLASH_ACTIVITY) || name.equals(JUMP_ACTIVITY)) {
            ViewUtils.findAllViewByType(activity, ImageView.class).forEach(imageView -> {
                Logger.d("Set black to " + imageView + " in " + activity);
                imageView.setImageDrawable(new ColorDrawable(Color.BLACK));
            });
        }
    }
}
