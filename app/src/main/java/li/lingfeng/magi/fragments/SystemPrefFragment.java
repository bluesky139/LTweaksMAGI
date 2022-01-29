package li.lingfeng.magi.fragments;

import android.content.Intent;
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
        checkAndWatchPrefForComponentState("system_share_copy_to_share", CopyToShareService.class, enabled -> {
            getActivity().startService(new Intent(getContext(), CopyToShareService.class));
        });
    }
}
