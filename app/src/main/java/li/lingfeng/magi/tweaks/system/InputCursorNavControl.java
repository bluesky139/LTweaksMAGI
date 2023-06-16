package li.lingfeng.magi.tweaks.system;

import android.graphics.drawable.Drawable;

import java.lang.reflect.InvocationTargetException;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.tweaks.hook.KeyButtonViewSetImageDrawable;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;

@AppLoad(packageName = PackageNames.ANDROID_SYSTEM_UI, pref = "nav_bar_input_cursor_control", hook = true)
public class InputCursorNavControl extends TweakBase {

    private boolean mVisible = false;
    private Object mInflaterView;

    @Override
    public Result navBarViewSetNavigationIconHints(Object thisObject, int hints) {
        return new Result().after(r -> {
            if (mInflaterView != null) {
                updateVisibility(thisObject);
            }
        });
    }

    @Override
    public Result navBarInflaterViewGetDefaultLayout(Object thisObject) {
        return new Result().before(r -> {
            mInflaterView = thisObject;
            if (mVisible) {
                String dpadLeft = "content://li.lingfeng.magi.resourceProvider/raw/nav_dpad_left";
                String dpadRight = "content://li.lingfeng.magi.resourceProvider/raw/nav_dpad_right";
                String layout = "key(21:" + dpadLeft + ")[.5W],back[1WC];home;recent[1WC],key(22:" + dpadRight + ")[.5W]";
                r.setResult(layout);
            }
        });
    }

    @Override
    public Result keyButtonViewSetImageDrawable(Object thisObject, Drawable drawable) {
        return new Result().before(r -> {
            try {
                Loader.invokeOriginalMethod(KeyButtonViewSetImageDrawable.class, thisObject, drawable);
            } catch (Throwable e) {
                e = e instanceof InvocationTargetException ? ((InvocationTargetException) e).getCause() : e;
                Logger.w("Ignore KeyButtonView.setImageDrawable exception, " + e);
            }
            r.setResult(null);
        });
    }

    private void updateVisibility(Object navBarView) throws Throwable {
        int iconHints = ReflectUtils.getIntField(navBarView, "mNavigationIconHints");
        boolean visible = (iconHints & (1 << 0) /* NAVIGATION_HINT_BACK_ALT */) != 0;
        if (mVisible == visible) {
            return;
        }
        mVisible = visible;
        Logger.d(mVisible ? "Show nav cursor control." : "Hide nav cursor control.");
        ReflectUtils.callMethod(mInflaterView, "onLikelyDefaultLayoutChange");
    }
}
