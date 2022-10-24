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

@AppLoad(packageName = PackageNames.COMIC_SCREEN, pref = "comic_screen_rclone_mount")
public class ComicScreenRcloneMount extends TweakBase {

    private static final String COMIC_LIST_ACTIVITY = "com.viewer.comicscreen.ListActivity";
    private File mSdcardDir;

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (!activity.getClass().getName().equals(COMIC_LIST_ACTIVITY)) {
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
                    " --config /sdcard/rclone_comic_screen.conf" +
                    " --max-read-ahead 512K" +
                    " --buffer-size 32M" +
                    " --dir-cache-time 1h" +
                    " --poll-interval 5m" +
                    " --attr-timeout 1h" +
                    " --vfs-cache-mode full" +
                    " --vfs-read-ahead 8M" +
                    " --vfs-cache-max-age 72h0m0s" +
                    " --vfs-cache-max-size 512M" +
                    " --vfs-cache-poll-interval 10m0s" +
                    " --cache-dir=" + cacheDir.getPath() +
                    " --log-file " + logFile.getPath() +
                    " --allow-other --gid 1015" +
                    " --daemon > /dev/null 2>&1 &";
            Logger.d(cmd);
            Shell.su(cmd).submit();
            Toast.makeText(activity, "rclone mount", Toast.LENGTH_SHORT).show();
        } catch (Throwable e) {
            Logger.e("ComicScreenRcloneMount exception.", e);
        }
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (!activity.getClass().getName().equals(COMIC_LIST_ACTIVITY)) {
            return;
        }
        String cmd = "ps -ef | grep 'rclone mount' | grep rclone_comic_screen.conf | awk '{print $2}' | xargs kill";
        Logger.d(cmd);
        Shell.su(cmd).submit();
        Toast.makeText(activity, "rclone umount", Toast.LENGTH_SHORT).show();
    }

    private File getSdcardDir() {
        if (mSdcardDir == null) {
            mSdcardDir = new File("/storage/emulated/0/Pictures/comic_screen_rclone");
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
