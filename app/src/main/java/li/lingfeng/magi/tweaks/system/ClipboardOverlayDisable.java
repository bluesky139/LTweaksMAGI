package li.lingfeng.magi.tweaks.system;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.ANDROID_SYSTEM_UI, pref = "system_ui_clipboard_overlay_disable", hook = true)
public class ClipboardOverlayDisable extends TweakBase {

    @Override
    public Result systemUiClipboardListenerStart(Object thisObject) {
        return new Result().before(r -> {
            Logger.i("Disable SystemUI clipboard overlay.");
            r.setResult(null);
        });
    }
}
