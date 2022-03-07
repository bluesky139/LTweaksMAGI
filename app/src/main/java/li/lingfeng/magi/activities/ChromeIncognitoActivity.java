package li.lingfeng.magi.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import li.lingfeng.magi.R;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.Utils;

public class ChromeIncognitoActivity extends Activity {

    public static final String ACTION_CHROME_INCOGNITO = "li.lingfeng.magi.ACTION_CHROME_INCOGNITO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();
        String url = null;
        if (Intent.ACTION_PROCESS_TEXT.equals(action)) {
            String text = getIntent().getStringExtra(Intent.EXTRA_PROCESS_TEXT);
            if (Utils.isUrl(text)) {
                Logger.i("Incognito url: " + text);
                url = text;
            } else {
                Logger.i("Incognito text search: " + text);
                url = "https://www.google.com/search?gws_rd=cr&q=" + Uri.encode(text);
            }
        } else if (Intent.ACTION_VIEW.equals(action) || ACTION_CHROME_INCOGNITO.equals(action)) {
            url = getIntent().getDataString();
            Logger.i("Incognito url: " + url);
        }

        if (StringUtils.isEmpty(url)) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Intent intent = getPackageManager().getLaunchIntentForPackage(PackageNames.CHROME);
        intent.putExtra("ltweaks_open_in_incognito", true);
        intent.putExtra("ltweaks_url", url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}
