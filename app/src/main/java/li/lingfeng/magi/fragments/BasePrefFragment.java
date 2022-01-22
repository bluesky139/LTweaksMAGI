package li.lingfeng.magi.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import li.lingfeng.magi.prefs.PrefStore;

public abstract class BasePrefFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setPreferenceDataStore(new PrefStore());
    }
}
