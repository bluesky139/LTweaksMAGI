package li.lingfeng.magi.tweaks.entertainment;

import android.app.AppGlobals;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.BILIBILI, pref = "bilibili_darken_splash")
public class BilibiliDarkenSplash extends TweakBase {

    @Override
    public void load() {
        super.load();
        try {
            Logger.d("Set bilibili splash theme.");
            AppGlobals.getPackageManager().setSplashScreenTheme(PackageNames.BILIBILI, "android:style/ThemeOverlay.Material.Dark", 0);
        } catch (Throwable e) {
            Logger.e("Exception on set bilibili splash theme", e);
        }
    }
}
