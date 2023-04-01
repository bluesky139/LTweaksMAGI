package li.lingfeng.magi;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import li.lingfeng.magi.dex.Assets;
import li.lingfeng.magi.dex.Dex;
import li.lingfeng.magi.services.BootReceiver;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        new BootReceiver.Run().startServices(this);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            int status = new Dex().checkAndUpdate(getContext());
            int title;
            switch (status) {
                case Dex.STATUS_ALREADY_UPDATED:
                    title = R.string.status_updated_dex;
                    break;
                case Dex.STATUS_JUST_UPDATED:
                    title = R.string.status_reboot_dex;
                    break;
                case Dex.STATUS_ERROR:
                    title = R.string.status_error;
                    break;
                default:
                    throw new IllegalStateException("Unexpected dex status: " + status);
            }
            Preference preference = findPreference("status_dex");
            preference.setTitle(title);

            status = new Assets().checkAndUpdate(getContext());
            switch (status) {
                case Assets.STATUS_ALREADY_UPDATED:
                    title = R.string.status_updated_assets;
                    break;
                case Assets.STATUS_JUST_UPDATED:
                    title = R.string.status_reboot_assets;
                    break;
                case Assets.STATUS_ERROR:
                    title = R.string.status_error;
                    break;
                default:
                    throw new IllegalStateException("Unexpected assets status: " + status);
            }
            preference = findPreference("status_assets");
            preference.setTitle(title);
        }
    }
}