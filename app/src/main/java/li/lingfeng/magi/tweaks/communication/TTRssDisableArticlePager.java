package li.lingfeng.magi.tweaks.communication;

import android.app.Activity;
import android.os.Bundle;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;

import static li.lingfeng.magi.utils.ReflectUtils.findClass;

@AppLoad(packageName = PackageNames.TT_RSS, pref = "ttrss_disable_article_pager")
public class TTRssDisableArticlePager extends TweakBase {

    private static final String DETAIL_ACTIVITY = "org.fox.ttrss.DetailActivity";
    private static final String ARTICLE_PAGER = "org.fox.ttrss.ArticlePager";
    private static final String ARTICLE_LIST = "org.fox.ttrss.types.ArticleList";

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        try {
            if (activity.getClass().getName().equals(DETAIL_ACTIVITY)) {
                Object fragmentManager = ReflectUtils.callMethod(activity, "getSupportFragmentManager");
                List pendingActions = (List) ReflectUtils.getObjectField(fragmentManager, "mPendingActions");
                if (pendingActions == null) {
                    Logger.w("No pending actions found in ArticlePager, should be resumed from saved state.");
                    return;
                }
                List ops = (List) ReflectUtils.getObjectField(pendingActions.get(0), "mOps");
                Object fragment = ops.stream()
                        .map(op -> {
                            try {
                                return ReflectUtils.getObjectField(op, "mFragment");
                            } catch (Throwable e) {
                                return null;
                            }
                        })
                        .filter(f -> f != null && f.getClass().getName().equals(ARTICLE_PAGER)).findFirst().get();

                Object article = ReflectUtils.getObjectField(fragment, "m_article");
                List articleList = (List) findClass(ARTICLE_LIST).newInstance();
                articleList.add(article);
                ReflectUtils.setObjectField(fragment, "m_articles", articleList);
            }
        } catch (Throwable e) {
            Logger.e("TTRssDisableArticlePager exception.", e);
        }
    }
}
