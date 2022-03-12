package li.lingfeng.magi.tweaks.entertainment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.BILIBILI, pref = "bilibili_remove_author_ad")
public class BilibiliRemoveAuthorAd extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "com.bilibili.video.videodetail.VideoDetailsActivity";

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (activity.getClass().getName().equals(VIDEO_DETAILS_ACTIVITY)) {
            Loader.getMainHandler().post(() -> {
                View adContainer = ViewUtils.findViewByName(activity, "upper_ad_container");
                if (adContainer != null) {
                    Logger.v("Remove " + adContainer);
                    adContainer.setVisibility(View.GONE);
                }
            });
        }
    }
}
