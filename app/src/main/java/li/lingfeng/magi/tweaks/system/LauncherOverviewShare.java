package li.lingfeng.magi.tweaks.system;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.ContextUtils;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.PIXEL_LAUNCHER, pref = "launcher_overview_share")
public class LauncherOverviewShare extends TweakBase {

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        try {
            View view = ViewUtils.findViewByName(activity, "action_select");
            if (view != null) {
                Logger.i("Replace select button to share button.");;
                Button button = (Button) view;
                button.setText("Share");
                Drawable icon = ContextUtils.getDrawable("ic_share");
                button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                button.setOnClickListener(v -> {
                    Logger.d("Share button click.");
                    try {
                        View overviewPanel = ViewUtils.findViewByName(activity, "overview_panel");
                        int page = (int) ReflectUtils.callMethod(overviewPanel, "getNextPage");
                        if (page >= 0) {
                            View taskView = (View) ReflectUtils.callMethod(overviewPanel, "getTaskViewAt", new Object[] {
                                    page
                            }, new Class[] {
                                    int.class
                            });
                            View snapshotView = (View) ReflectUtils.callMethod(taskView, "getThumbnail");
                            Object overlay = ReflectUtils.callMethod(snapshotView, "getTaskOverlay");
                            Object imageApi = ReflectUtils.getObjectField(overlay, "mImageApi");
                            Object bitmapSupplier = ReflectUtils.getObjectField(imageApi, "mBitmapSupplier");
                            Bitmap bitmap = (Bitmap) ReflectUtils.callMethod(bitmapSupplier, "get");
                            ReflectUtils.callMethod(imageApi, "startShareActivity", new Object[] {
                                    new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight())
                            }, new Class[] {
                                    Rect.class
                            });
                        } else {
                            Logger.e("page " + page);
                        }
                    } catch (Throwable e) {
                        Logger.e("Share button click exception.", e);
                    }
                });
            }
        } catch (Throwable e) {
            Logger.e("LauncherOverviewShare exception.", e);
        }
    }
}
