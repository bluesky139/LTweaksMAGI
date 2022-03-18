package li.lingfeng.magi.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.magi.R;
import li.lingfeng.magi.activities.SelectableTextActivity;
import li.lingfeng.magi.services.CopyToShareService;

public class SystemPrefFragment extends BasePrefFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.pref_system, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkAndWatchPrefForComponentState("text_selectable_text", SelectableTextActivity.class);
        watchPrefForService("system_share_copy_to_share", CopyToShareService.class);

        updateSummary("quick_settings_tile_set_preconfigured_brightness");
        findPreference("quick_settings_tile_set_preconfigured_brightness").setOnPreferenceChangeListener((preference, _newValue) -> {
            String newValue = (String) _newValue;
            if (newValue.isEmpty()) {
                preference.setSummary("");
                return true;
            }
            try {
                int value = Integer.parseInt(newValue);
                if (value > 0 && value < 255) {
                    preference.setSummary(newValue);
                    return true;
                }
            } catch (Throwable e) {}
            return false;
        });
    }
}
