package li.lingfeng.magi.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import li.lingfeng.magi.prefs.PrefStore;
import li.lingfeng.magi.utils.Logger;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i("LTweaks boot completed.");
        if (new PrefStore().getBoolean("system_share_copy_to_share", false)) {
            context.startForegroundService(new Intent(context, CopyToShareService.class));
        }
    }
}
