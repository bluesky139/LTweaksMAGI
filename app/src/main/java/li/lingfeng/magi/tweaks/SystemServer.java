package li.lingfeng.magi.tweaks;

import android.app.Application;
import android.os.SystemProperties;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

//@AppLoad(packageName = PackageNames.ANDROID, pref = "")
public class SystemServer extends TweakBase {

    /*@Override
    public void load(Application app) {
        super.load(app);
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
    }*/
}
