package li.lingfeng.magi.fragments;

import android.os.Bundle;

import li.lingfeng.magi.R;

public class CommunicationPrefFragment extends BasePrefFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.pref_communication, rootKey);
    }
}
