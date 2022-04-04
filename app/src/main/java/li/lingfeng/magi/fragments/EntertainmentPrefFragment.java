package li.lingfeng.magi.fragments;

import android.app.AppGlobals;
import android.os.Bundle;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import androidx.preference.Preference;
import li.lingfeng.magi.R;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.services.MXDanmakuService;
import li.lingfeng.magi.utils.Logger;

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

        findPreference("bilibili_darken_splash").setOnPreferenceClickListener(preference -> {
            try {
                Logger.d("Set bilibili splash theme.");
                AppGlobals.getPackageManager().setSplashScreenTheme(PackageNames.BILIBILI, "android:style/ThemeOverlay.Material.Dark", 0);
                Toast.makeText(getActivity(), R.string.set, Toast.LENGTH_SHORT).show();
            } catch (Throwable e) {
                Logger.e("Exception on set bilibili splash theme", e);
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }
}
