package li.lingfeng.magi.tweaks;

import android.os.SystemProperties;

import li.lingfeng.magi.Loader;
import li.lingfeng.magi.utils.Logger;

public class SystemServer {

    public void load() {
        Loader.onMainReady(new Loader.OnReadyRunnable() {
            @Override
            public boolean run() {
                Logger.v("Check boot_completed.");
                return SystemProperties.getBoolean("sys.boot_completed", false);
            }

            @Override
            public void onReady() {
                Logger.i("System boot completed.");
                onBootCompleted();
            }
        }, 5000);
    }

    private void onBootCompleted() {
    }
}
