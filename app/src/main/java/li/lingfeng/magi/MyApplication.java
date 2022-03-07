package li.lingfeng.magi;

import android.app.Application;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HiddenApiBypass.setHiddenApiExemptions("");
    }
}
