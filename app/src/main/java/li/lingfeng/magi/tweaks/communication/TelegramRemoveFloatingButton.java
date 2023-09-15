package li.lingfeng.magi.tweaks.communication;

import android.content.Context;
import android.view.View;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.TELEGRAM, pref = "telegram_remove_floating_button", hook = true)
public class TelegramRemoveFloatingButton extends TweakBase {

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return false;
    }

    @Override
    public Result telegramDialogsActivityCreateView(Object thisObject, Context context) {
        return new Result().after(r -> {
            for (String name : new String[] {
                    "floatingButtonContainer", "floatingButton2Container"
            }) {
                View floatingButton = (View) ReflectUtils.getObjectField(thisObject, name);
                Logger.d(name + " " + floatingButton);
                if (floatingButton != null && floatingButton.getParent() != null) {
                    ViewUtils.removeView(floatingButton);
                }
            }
        });
    }
}

