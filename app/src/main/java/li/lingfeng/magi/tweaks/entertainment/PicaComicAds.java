package li.lingfeng.magi.tweaks.entertainment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.PICA_COMIC, pref = "pica_comic_remove_ads")
public class PicaComicAds extends TweakBase {

    private static final String MAIN_ACTIVITY = "com.picacomic.fregata.activities.MainActivity";
    private static final String COMIC_VIEWER_ACTIVITY = "com.picacomic.fregata.activities.ComicViewerActivity";
    private static final String SQUARE_WEBVIEW = "com.picacomic.fregata.utils.views.SquareWebview";
    private WeakReference<ViewGroup> mComicList;

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        String className = activity.getClass().getName();
        if (className.equals(MAIN_ACTIVITY)) {
            new Handler().post(() -> {
                try {
                    ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();

                    Logger.v("Remove popupWebview bannerWebview");
                    View popupWebview = ViewUtils.findViewByName(rootView, "popupWebview");
                    ViewUtils.removeView(popupWebview);
                    View bannerWebview = ViewUtils.findViewByName(rootView, "bannerWebview");
                    ViewUtils.removeView(bannerWebview);

                    rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                        try {
                            ViewGroup comicList = ViewUtils.findViewByName(rootView, "recyclerView_comic_list");
                            if (comicList != null && (mComicList == null || mComicList.get() != comicList)) {
                                Logger.d("comicList " + comicList);
                                mComicList = new WeakReference<>(comicList);
                                for (int i = 0; i < comicList.getChildCount(); ++i) {
                                    removeAdFromChild(comicList.getChildAt(i));
                                }
                                comicList.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                                    @Override
                                    public void onChildViewAdded(View parent, View child) {
                                        removeAdFromChild(child);
                                    }

                                    @Override
                                    public void onChildViewRemoved(View parent, View child) {
                                    }
                                });
                            }

                            View squareWebview = ViewUtils.findViewByType(rootView, ReflectUtils.findClass(SQUARE_WEBVIEW));
                            if (squareWebview != null) {
                                Logger.v("Remove " + squareWebview);
                                ViewUtils.removeView(squareWebview);
                            }
                        } catch (Throwable e) {
                            Logger.e("Exception in MainActivity global layout listener.", e);
                        }
                    });
                } catch (Throwable e) {
                    Logger.e("Remove ads in MainActivity exception.", e);
                }
            });

        } else if (className.equals(COMIC_VIEWER_ACTIVITY)) {
            new Handler().post(() -> {
                try {
                    ViewGroup listView = ViewUtils.findViewByName(activity, "recyclerView_comic_viewer");
                    for (int i = 0; i < listView.getChildCount(); ++i) {
                        View child = listView.getChildAt(i);
                        removeAdFromChild(child);
                    }
                    listView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                        @Override
                        public void onChildViewAdded(View parent, View child) {
                            removeAdFromChild(child);
                        }

                        @Override
                        public void onChildViewRemoved(View parent, View child) {
                        }
                    });
                } catch (Throwable e) {
                    Logger.e("Remove ads from comic viewer exception.", e);
                }
            });
        }
    }

    private void removeAdFromChild(View child) {
        if (child instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) child;
            child = viewGroup.getChildAt(0);
            if (child instanceof WebView) {
                Logger.v("Remove " + child);
                ViewUtils.removeView(child);
                TextView textView = new TextView(child.getContext());
                textView.setText("   ↓");
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(24);
                viewGroup.addView(textView);
            }
        }
    }
}
