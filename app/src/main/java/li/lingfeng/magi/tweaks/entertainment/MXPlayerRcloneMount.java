package li.lingfeng.magi.tweaks.entertainment;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.MX_PLAYER_PRO, pref = "mxplayer_rclone_mount")
public class MXPlayerRcloneMount extends TweakBase {

    private static final String ACTIVITY_MEDIA_LIST = "com.mxtech.videoplayer.pro.ActivityMediaList";
    private File mSdcardDir;

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (!activity.getClass().getName().equals(ACTIVITY_MEDIA_LIST)) {
            return;
        }
        try {
            File dir = new File(getSdcardDir(), "cloud0");
            if (!dir.exists()) {
                dir.mkdir();
            }
            File cacheDir = new File(getSdcardDir(), "rclone_cache");
            if (!cacheDir.exists()) {
                cacheDir.mkdir();
            }
            File logFile = new File(getSdcardDir(), "rclone.log");
            if (logFile.exists()) {
                logFile.delete();
            }

            String cmd = "nohup rclone mount cloud0: " + dir.getPath() +
                    " --config /sdcard/rclone_mxplayer.conf" +
                    " --max-read-ahead 2M" +
                    " --buffer-size 32M" +
                    " --dir-cache-time 1h" +
                    " --poll-interval 5m" +
                    " --attr-timeout 1h" +
                    " --vfs-cache-mode full" +
                    " --vfs-read-ahead 32M" +
                    " --vfs-cache-max-age 72h0m0s" +
                    " --vfs-cache-max-size 5G" +
                    " --vfs-cache-poll-interval 10m0s" +
                    " --cache-dir=" + cacheDir.getPath() +
                    " --log-file " + logFile.getPath() +
                    " --allow-other --gid 1015" +
                    " --daemon > /dev/null 2>&1 &";
            Logger.d(cmd);
            Shell.su(cmd).submit();
            Toast.makeText(activity, "rclone mount", Toast.LENGTH_SHORT).show();
        } catch (Throwable e) {
            Logger.e("MXPlayerRcloneMount exception.", e);
        }
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (!activity.getClass().getName().equals(ACTIVITY_MEDIA_LIST)) {
            return;
        }
        String cmd = "ps -ef | grep 'rclone mount' | grep rclone_mxplayer.conf | awk '{print $2}' | xargs kill";
        Logger.d(cmd);
        Shell.su(cmd).submit();
        Toast.makeText(activity, "rclone umount", Toast.LENGTH_SHORT).show();
    }

    private File getSdcardDir() {
        if (mSdcardDir == null) {
            mSdcardDir = new File("/storage/emulated/0/Movies/mxplayer_rclone");
            if (!mSdcardDir.exists()) {
                mSdcardDir.mkdirs();
            }
            try {
                new File(mSdcardDir, ".nomedia").createNewFile();
            } catch (IOException e) {
            }
        }
        return mSdcardDir;
    }
}
