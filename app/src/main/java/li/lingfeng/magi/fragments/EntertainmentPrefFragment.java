package li.lingfeng.magi.fragments;

import android.os.Bundle;

import li.lingfeng.magi.R;
import li.lingfeng.magi.utils.ComponentUtils;

public class EntertainmentPrefFragment extends BasePrefFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.pref_entertainment, rootKey);
        checkAndWatchPrefForComponentState("bilibili_search",
                "li.lingfeng.magi.activities.BilibiliProcessTextActivity");
    }
}
