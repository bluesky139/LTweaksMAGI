package li.lingfeng.magi.tweaks.entertainment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

@AppLoad(packageName = PackageNames.BILIBILI, pref = "bilibili_danmaku_off")
public class BilibiliDanmakuOff extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "com.bilibili.video.videodetail.VideoDetailsActivity";
    private static final String BANGUMI_DETAIL_ACTIVITY = "com.bilibili.bangumi.ui.page.detail.BangumiDetailActivityV3";
    private SharedPreferences mSharedPreferences;

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        String name = activity.getClass().getName();
        if (name.equals(VIDEO_DETAILS_ACTIVITY) || name.equals(BANGUMI_DETAIL_ACTIVITY)) {
            Loader.getMainHandler().post(() -> {
                try {
                    handleActivityCreate(activity);
                } catch (Throwable e) {
                    Logger.e("BilibiliDanmakuOff exception.", e);
                }
            });
        }
    }

    private void handleActivityCreate(Activity activity) {
        View v = ViewUtils.findViewByName(activity, "new_danmaku_switch");
        if (v != null) {
            Logger.v("Danmaku off at activity create.");
            v.performClick();
        }
    }

    @Override
    public void onActivityPreDestroyed(@NonNull Activity activity) {
        String name = activity.getClass().getName();
        if (name.equals(VIDEO_DETAILS_ACTIVITY) || name.equals(BANGUMI_DETAIL_ACTIVITY)) {
            try {
                handleActivityDestroy(activity);
            } catch (Throwable e) {
                Logger.e("BilibiliDanmakuOff exception.", e);
            }
        }
    }

    private void handleActivityDestroy(Activity activity) throws Throwable {
        View v = ViewUtils.findViewByName(activity, "new_danmaku_switch");
        if (v != null) {
            if (mSharedPreferences == null) {
                Class cls = findClass("com.bilibili.lib.blkv.internal.sp.BLPrefManager");
                Field field = ReflectUtils.findFirstFieldByExactType(cls, cls);
                Object prefManager = field.get(null);
                Method method = ReflectUtils.findFirstMethodByTypes(cls, new Class[] {
                        Context.class, File.class, boolean.class
                }, SharedPreferences.class);
                File dir = mApp.getDir("blkv", 0);
                mSharedPreferences = (SharedPreferences) method.invoke(prefManager,
                        mApp, new File(dir, "biliplayer.blkv"), false);
            }
            if (mSharedPreferences.getBoolean("danmaku_switch", true)) {
                Logger.v("Danmaku off at activity destroy.");
                v.performClick();
            }
        } else {
            Logger.e("Can't find new_danmaku_switch.");
        }
    }
}
