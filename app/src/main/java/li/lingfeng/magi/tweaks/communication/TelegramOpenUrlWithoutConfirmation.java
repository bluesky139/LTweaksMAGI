package li.lingfeng.magi.tweaks.communication;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.prefs.ClassNames;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.TELEGRAM, pref = "telegram_open_url_without_confirmation")
public class TelegramOpenUrlWithoutConfirmation extends TweakBase {

    private static final String ALERT_DIALOG = "org.telegram.ui.ActionBar.AlertDialog";
    private static final String SPOILERS_TEXTVIEW = "org.telegram.ui.Components.spoilers.SpoilersTextView";

    @Override
    protected boolean shouldInterceptWindowManagerAddView() {
        return true;
    }

    @Override
    protected void windowManagerAddView(View view) {
        Loader.getMainHandler().post(() -> {
            try {
                if (view.getClass().getName().equals(ClassNames.DECOR_VIEW)) {
                    ViewGroup viewGroup = (ViewGroup) view;
                    viewGroup = ViewUtils.findViewByTypeStart(viewGroup, ALERT_DIALOG);
                    if (viewGroup == null) {
                        return;
                    }
                    TextView textView = ViewUtils.findViewByType(viewGroup, SPOILERS_TEXTVIEW);
                    if (textView == null) {
                        return;
                    }
                    String text = textView.getText().toString();
                    if (text.startsWith("Do you want to open")) {
                        Logger.v(text + " Yes.");
                        textView = ViewUtils.findTextViewByText(viewGroup, "OPEN");
                        textView.performClick();
                    }
                }
            } catch (Throwable e) {
                Logger.e("TelegramOpenUrlWithoutConfirmation exception.", e);
            }
        });
    }
}
