package li.lingfeng.magi;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import li.lingfeng.magi.dex.Dex;

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
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            int status = new Dex().checkAndUpdate(getContext());
            int title;
            switch (status) {
                case Dex.STATUS_ALREADY_UPDATED:
                    title = R.string.status_updated;
                    break;
                case Dex.STATUS_JUST_UPDATED:
                    title = R.string.status_reboot;
                    break;
                case Dex.STATUS_ERROR:
                    title = R.string.status_error;
                    break;
                default:
                    throw new IllegalStateException("Unexpected dex status: " + status);
            }
            Preference preference = findPreference("status");
            preference.setTitle(title);
        }
    }
}