package li.lingfeng.magi.fragments;

import android.os.Bundle;

import com.topjohnwu.superuser.Shell;

import li.lingfeng.magi.R;
import li.lingfeng.magi.services.MXDanmakuService;

public class EntertainmentPrefFragment extends BasePrefFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.pref_entertainment, rootKey);
        checkAndWatchPrefForComponentState("bilibili_search",
                "li.lingfeng.magi.activities.BilibiliProcessTextActivity");
        checkAndWatchPrefForComponentState("douban_search",
                "li.lingfeng.magi.activities.DoubanProcessTextActivity");
        watchPrefForService("mxplayer_danmaku", MXDanmakuService.class, (preference, newValue) -> {
            if ((boolean) newValue) {
                Shell.su("settings put global block_untrusted_touches 0").submit();
            }
            return true;
        });
    }
}
