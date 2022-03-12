package li.lingfeng.magi.tweaks.google;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.CHROME, pref = "chrome_incognito_search")
public class ChromeIncognitoSearch extends TweakBase {

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (activity.getIntent().getBooleanExtra("ltweaks_open_in_incognito", false)) {
            activity.getIntent().putExtra("ltweaks_open_in_incognito", false);
            String url = activity.getIntent().getStringExtra("ltweaks_url");
            Logger.d("Open in incognito " + url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setPackage(PackageNames.CHROME);
            intent.putExtra("com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB", true);
            intent.putExtra("com.android.browser.application_id", PackageNames.CHROME);
            PendingIntent pendingIntent = PendingIntent.getActivity(mApp, 0,
                    new Intent().setComponent(new ComponentName(mApp, "FakeClass")), PendingIntent.FLAG_IMMUTABLE);
            intent.putExtra("trusted_application_code_extra", pendingIntent);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            // must post, or duplicated tab will be opened.
            Loader.getMainHandler().postDelayed(() -> {
                activity.startActivity(intent);
            }, 500);
        }
    }
}
