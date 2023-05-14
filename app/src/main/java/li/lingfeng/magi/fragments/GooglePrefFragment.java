package li.lingfeng.magi.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.Preference;
import li.lingfeng.magi.R;
import li.lingfeng.magi.activities.ChromeIncognitoActivity;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

public class GooglePrefFragment extends BasePrefFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.pref_google, rootKey);
        checkAndWatchPrefForComponentState("chrome_incognito_search", ChromeIncognitoActivity.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference("chrome_custom_tab_shortcut").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ViewUtils.showEdittextDialog(getActivity(), R.string.chrome_enter_url, url -> {
                    try {
                        Logger.d("Create custom tab shortcut, url " + url);
                        CustomTabsIntent intent = new CustomTabsIntent.Builder()
                                .build();
                        intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.intent.setData(Uri.parse(url));

                        String id = "chrome_custom_tab_" + System.currentTimeMillis();
                        ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(getActivity(), id)
                                .setShortLabel(id)
                                .setIcon(IconCompat.createWithResource(getActivity(), R.mipmap.ic_launcher))
                                .setIntent(intent.intent)
                                .build();
                        ShortcutManagerCompat.requestPinShortcut(getActivity(), shortcut, null);
                    } catch (Throwable e) {
                        Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                        Logger.e("Error to create shortcut.", e);
                    }
                });
                return true;
            }
        });
    }
}
