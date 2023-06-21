package li.lingfeng.magi.tweaks.communication;

import java.util.List;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.TELEGRAM, pref = "telegram_disable_reaction_menu", hook = true)
public class TelegramReactionMenuDisable extends TweakBase {

    @Override
    public Result telegramReactionMenuDisable(Object thisObject, List list) {
        return new Result().before(r -> {
            Logger.v("Disable reaction menu.");
            r.setResult(null);
        });
    }
}
