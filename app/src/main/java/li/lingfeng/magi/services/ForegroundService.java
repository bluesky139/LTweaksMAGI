package li.lingfeng.magi.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import li.lingfeng.magi.R;
import li.lingfeng.magi.utils.Logger;

public abstract class ForegroundService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.v(getClass().getSimpleName() + " onCreate.");
        setupNotification();
    }

    private void setupNotification() {
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(getClass().getName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            String title = getClass().getSimpleName();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setWhen(0)
                    .setOngoing(true)
                    .setTicker(title)
                    .setDefaults(0)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setContentTitle(title)
                    .setContentText(title)
                    .setContentIntent(pendingIntent)
                    .setChannelId(title)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            NotificationChannel channel = new NotificationChannel(title, title, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
            Notification notification = builder.build();
            startForeground(getNotificationId(), notification);
        } catch (Throwable e) {
            Logger.e("Can't set notification for " + getClass().getSimpleName(), e);
        }
    }

    protected abstract int getNotificationId();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra("stop", false)) {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.v(getClass().getSimpleName() + " onDestroy.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
