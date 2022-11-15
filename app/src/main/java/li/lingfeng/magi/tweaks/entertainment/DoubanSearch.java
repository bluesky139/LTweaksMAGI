package li.lingfeng.magi.tweaks.entertainment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.apache.commons.lang3.StringUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;

@AppLoad(packageName = PackageNames.DOUBAN, pref = "douban_search")
public class DoubanSearch extends TweakBase {

    public static final String STATUS_SEARCH_ACTIVITY = "com.douban.frodo.activity.MixSearchActivity";
    private static final String SEARCH_ACTIVITY = "com.douban.frodo.search.activity.NewSearchActivity";

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        String text = activity.getIntent().getStringExtra("ltweaks_search_text");
        if (!StringUtils.isEmpty(text)) {
            Logger.v("Search " + text);
            Intent intent = new Intent();
            intent.setClassName(PackageNames.DOUBAN, SEARCH_ACTIVITY);
            intent.putExtra("query", text);
            intent.putExtra("from_ltweaks", true);
            activity.startActivity(intent);
            activity.finish();
        }
    }
}
