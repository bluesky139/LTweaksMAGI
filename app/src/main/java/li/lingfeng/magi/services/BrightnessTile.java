package li.lingfeng.magi.services;

import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.topjohnwu.superuser.Shell;

import li.lingfeng.magi.prefs.PrefStore;
import li.lingfeng.magi.utils.Logger;

public class BrightnessTile extends TileService {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("BrightnessTile onCreate");
    }

    @Override
    public void onStartListening() {
        Logger.d("BrightnessTile onStartListening");
        int value = PrefStore.instance.getInt("quick_settings_tile_set_preconfigured_brightness", 0);
        if (value > 0) {
            updateTile(value);
        }
    }

    @Override
    public void onClick() {
        int value = PrefStore.instance.getInt("quick_settings_tile_set_preconfigured_brightness", 0);
        if (value > 0) {
            Logger.i("Set brightness " + value);
            Shell.su(
                    "settings put system screen_brightness_mode 0",
                    "settings put system screen_brightness " + value)
                    .submit();
            updateTile(value);
        }
    }

    private void updateTile(int value) {
        Tile tile = getQsTile();
        tile.setLabel(value + " brightness");
        try {
            tile.setState(Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE)
                    == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        } catch (Settings.SettingNotFoundException e) {
            tile.setState(Tile.STATE_UNAVAILABLE);
        }
        tile.updateTile();
    }
}
