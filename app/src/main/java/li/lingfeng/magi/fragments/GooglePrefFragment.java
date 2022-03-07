package li.lingfeng.magi.fragments;

import android.os.Bundle;

import li.lingfeng.magi.R;
import li.lingfeng.magi.activities.ChromeIncognitoActivity;

public class GooglePrefFragment extends BasePrefFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.pref_google, rootKey);
        checkAndWatchPrefForComponentState("chrome_incognito_search", ChromeIncognitoActivity.class);
    }
}
