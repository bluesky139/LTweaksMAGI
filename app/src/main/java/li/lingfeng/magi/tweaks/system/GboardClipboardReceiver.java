package li.lingfeng.magi.tweaks.system;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;

import org.apache.commons.lang3.StringUtils;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.services.CopyToShareService;
import li.lingfeng.magi.tweaks.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.GBOARD, pref = "system_share_copy_to_share")
public class GboardClipboardReceiver extends TweakBase implements ClipboardManager.OnPrimaryClipChangedListener {

    private ClipboardManager mClipboardManager;

    @Override
    public void load(Application app) {
        super.load(app);
        listenClipboard();
    }

    private void listenClipboard() {
        Logger.i("Listen clipbaord in Gboard.");
        mClipboardManager = (ClipboardManager) mApp.getSystemService(Context.CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(this);
    }

    @Override
    public void onPrimaryClipChanged() {
        ClipData clipData = mClipboardManager.getPrimaryClip();
        if (clipData == null) {
            return;
        }
        final CharSequence text = clipData.getItemCount() > 0 ? clipData.getItemAt(0).getText() : null;
        if (StringUtils.isEmpty(text)) {
            return;
        }
        Logger.v("Text from clip: " + text);
        Intent intent = new Intent(CopyToShareService.CLIP_CHANGE_ACTION);
        intent.putExtra("text", text.toString());
        ClipDescription desc = clipData.getDescription();
        if (desc != null) {
            PersistableBundle extras = desc.getExtras();
            if (extras != null) {
                int referrer = extras.getInt("ltweaks_clip_uid");
                if (referrer > 0) {
                    intent.putExtra("ltweaks_clip_uid", referrer);
                }
            }
        }
        intent.setPackage(PackageNames.L_TWEAKS);
        mApp.sendBroadcast(intent);
    }
}
