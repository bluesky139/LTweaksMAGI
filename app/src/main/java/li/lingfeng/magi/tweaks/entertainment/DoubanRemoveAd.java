package li.lingfeng.magi.tweaks.entertainment;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.DOUBAN, pref = "douban_remove_ad", hook = true)
public class DoubanRemoveAd extends TweakBase {

    @Override
    public Result doubanFeedAdViewHolderBind(Object thisObject, int i2, Object feedAd, Object feedAdAdapterInterface, Object feedAdCallback) {
        return new Result().before(r -> {
            Logger.v("No FeedAdViewHolder.");
            r.setResult(null);
        });
    }
}
