package li.lingfeng.magi.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import li.lingfeng.magi.utils.ComponentUtils;
import li.lingfeng.magi.utils.Logger;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i("Boot completed.");
        if (ComponentUtils.isComponentEnabled(CopyToShareService.class)) {
            context.startService(new Intent(context, CopyToShareService.class));
        }
    }
}
