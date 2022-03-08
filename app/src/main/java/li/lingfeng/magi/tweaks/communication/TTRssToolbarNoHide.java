package li.lingfeng.magi.tweaks.communication;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.TT_RSS, pref = "ttrss_toolbar_no_hide")
public class TTRssToolbarNoHide extends TweakBase {

    private static final String MASTER_ACTIVITY = "org.fox.ttrss.MasterActivity";
    private static final String DETAIL_ACTIVITY = "org.fox.ttrss.DetailActivity";
    private static final int SCROLL_FLAG_SCROLL = 1;

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        Loader.getMainHandler().post(() -> {
            try {
                String clsName = activity.getClass().getName();
                if (clsName.equals(MASTER_ACTIVITY)) {
                    setToolbarScroll(activity);
                } else if (clsName.equals(DETAIL_ACTIVITY)) {
                    setToolbarScroll(activity);
                    setArticleHeaderScroll(activity);
                }
            } catch (Throwable e) {
                Logger.e("TTRssToolbarNoHide exception.", e);
            }
        });
    }

    private void setToolbarScroll(Activity activity) throws Throwable {
        ViewGroup toolbar = ViewUtils.findViewByName(activity, "toolbar");
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        ReflectUtils.callMethod(layoutParams, "setScrollFlags", new Object[] { 0 }, new Class[] { int.class });
    }

    private void setArticleHeaderScroll(Activity activity) throws Throwable {
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean end = true;
                try {
                    ViewGroup toolbar = ViewUtils.findViewByName(activity, "article_header");
                    if (toolbar != null) {
                        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
                        ReflectUtils.callMethod(layoutParams, "setScrollFlags", new Object[] { SCROLL_FLAG_SCROLL }, new Class[] { int.class });
                    } else {
                        end = false;
                    }
                } catch (Throwable e) {
                    Logger.e("setScrollFlags exception.", e);
                }
                if (end) {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }
}
