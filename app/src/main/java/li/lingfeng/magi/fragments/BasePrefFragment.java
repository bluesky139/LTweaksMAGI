package li.lingfeng.magi.fragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import li.lingfeng.magi.prefs.PrefStore;
import li.lingfeng.magi.utils.Callback;
import li.lingfeng.magi.utils.ComponentUtils;

public abstract class BasePrefFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setPreferenceDataStore(PrefStore.instance);
    }

    protected void checkAndWatchPrefForComponentState(String key, Class componentCls) {
        checkAndWatchPrefForComponentState(key, componentCls, null);
    }

    protected void checkAndWatchPrefForComponentState(String key, Class componentCls, Callback.C1<Boolean> listener) {
        SwitchPreference preference = findPreference(key);
        preference.setChecked(ComponentUtils.isComponentEnabled(componentCls));
        preference.setOnPreferenceChangeListener((preference1, newValue) -> {
            ComponentUtils.enableComponent(componentCls, (Boolean) newValue);
            if (listener != null) {
                listener.onResult((Boolean) newValue);
            }
            return true;
        });
    }
}
