package li.lingfeng.magi.tweaks.communication;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.TELEGRAM, pref = "telegram_hide_stories", hook = true)
public class TelegramHideStories extends TweakBase {

    @Override
    public Result telegramUpdateStoriesVisibility(Object thisObject, boolean z) {
        return new Result().before(r -> {
            //Logger.v("Hide stories.");
            r.setResult(null);
        });
    }
}
