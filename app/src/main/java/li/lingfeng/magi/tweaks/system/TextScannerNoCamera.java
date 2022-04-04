package li.lingfeng.magi.tweaks.system;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;

import java.lang.reflect.Field;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;

@AppLoad(packageName = PackageNames.TEXT_SCANNER, pref = "text_scanner_no_camera")
public class TextScannerNoCamera extends TweakBase {

    private static final String CAMERA_ACTIVITY = "com.peace.TextScanner.CameraActivity";

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        Logger.d("onActivityPreCreated " + activity);
        if (activity.getClass().getName().equals(CAMERA_ACTIVITY)) {
            // Reset trial or manually clear data if you want.
            Logger.d("Reset trial.");
            activity.getSharedPreferences("info", 0).edit()
                    .putInt("activeCount", 0)
                    .putInt("count", 0)
                    .putInt("readCount", 0)
                    .putInt("freeScanNum", 20)
                    .commit();

        }
    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
        if (activity.getClass().getName().equals(CAMERA_ACTIVITY)
                && Intent.ACTION_SEND.equals(activity.getIntent().getAction())) {
            try {
                Logger.d("Release camera.");
                Field field = ReflectUtils.findFirstFieldByExactType(activity.getClass(), Camera.class);
                Camera camera = (Camera) field.get(activity);
                camera.release();
            } catch (Throwable e) {
                Logger.e("Can't release camera.", e);
            }
        }
    }

    @Override
    public void onActivityPostStopped(@NonNull Activity activity) {
        if (activity.getClass().getName().equals(CAMERA_ACTIVITY)
                && Intent.ACTION_SEND.equals(activity.getIntent().getAction())) {
            activity.finish();
        }
    }
}
