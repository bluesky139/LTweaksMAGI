package li.lingfeng.magi.tweaks.communication;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;

@AppLoad(packageName = PackageNames.TELEGRAM, pref = "telegram_dismiss_allow_background")
public class TelegramDismissAllowBackground extends TweakBase {

    @Override
    public void load() {
        super.load();
        mApp.getSharedPreferences("background_activity", 0)
                .edit()
                .putLong("last_checked", System.currentTimeMillis())
                .apply();
    }
}
