package li.lingfeng.magi.tweaks.communication;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.TELEGRAM, pref = "telegram_remove_ad", hook = true)
public class TelegramRemoveAd extends TweakBase {

    // https://github.com/shatyuka/Killergram/blob/master/app/src/main/java/com/shatyuka/killergram/MainHook.java
    @Override
    public Result telegramMessagesControllerGetSponsoredMessages(Object thisObject, long j) {
        return new Result().before(r -> {
            Logger.v("getSponsoredMessages return null.");
            r.setResult(null);
        });
    }
}
