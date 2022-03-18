package li.lingfeng.magi.services;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import li.lingfeng.magi.prefs.NotificationId;
import li.lingfeng.magi.utils.Logger;

public class MXDanmakuService extends ForegroundService {

    public static final String ACTION_MX_DANMAKU_CONTROL = "li.lingfeng.magi.ACTION_MX_DANMAKU_CONTROL";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int op = intent.getIntExtra("op", -1);
            ContentValues values = intent.getParcelableExtra("values");
            Logger.d("MXDanmakuService onReceive " + op + ", " + values);
            context.getContentResolver().update(Uri.parse("content://li.lingfeng.mxdanmaku.MainController/" + op),
                    values, null, null);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mReceiver, new IntentFilter(ACTION_MX_DANMAKU_CONTROL));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected int getNotificationId() {
        return NotificationId.MX_DANMAKU_SERVICE;
    }
}
