package li.lingfeng.magi.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import com.topjohnwu.superuser.Shell;

import li.lingfeng.magi.services.BrightnessTile;
import li.lingfeng.magi.utils.Logger;

public class TileActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComponentName componentName = getIntent().getParcelableExtra(Intent.EXTRA_COMPONENT_NAME);
        String clsName = componentName.getClassName();
        Logger.d(clsName + " long click.");
        if (clsName.equals(BrightnessTile.class.getName())) {
            brightnessTileLongClick();
        }
        finish();
    }

    private void brightnessTileLongClick() {
        Logger.i("Set auto brightness.");
        Shell.su("settings put system screen_brightness_mode 1").submit();
    }
}
