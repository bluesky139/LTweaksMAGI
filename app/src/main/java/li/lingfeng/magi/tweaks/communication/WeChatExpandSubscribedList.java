package li.lingfeng.magi.tweaks.communication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

import static li.lingfeng.magi.utils.ReflectUtils.findClass;

@AppLoad(packageName = PackageNames.WE_CHAT, pref = "wechat_expand_subscribed_list")
public class WeChatExpandSubscribedList extends TweakBase {

    private static final String BIZ_TIMELINE_UI = "com.tencent.mm.plugin.brandservice.ui.timeline.BizTimeLineUI";
    private static final String STORY_LIST_VIEW = "com.tencent.mm.plugin.bizui.widget.StoryListView";

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (activity.getClass().getName().equals(BIZ_TIMELINE_UI)) {
            Loader.getMainHandler().post(() -> {
                try {
                    handleList(activity);
                } catch (Throwable e) {
                    Logger.e("Handle StoryListView exception.", e);
                }
            });
        }
    }

    private void handleList(Activity activity) throws Throwable {
        ViewGroup listView = (ViewGroup) ViewUtils.findViewByType(activity, findClass(STORY_LIST_VIEW));
        for (int i = 0; i < listView.getChildCount(); ++i) {
            checkListItem(listView.getChildAt(i));
        }
        listView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                checkListItem(child);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });
    }

    private void checkListItem(View view) {
        try {
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                view = viewGroup.getChildAt(viewGroup.getChildCount() - 1);
                if (view instanceof ViewGroup) {
                    ViewGroup expandableView = (ViewGroup) view;
                    TextView textView = ViewUtils.findViewByType(expandableView, TextView.class);
                    String text = textView.getText().toString();
                    if (text.startsWith("余下") && text.endsWith("篇")) {
                        Loader.getMainHandler().post(() -> {
                            Logger.v("Expand article list.");
                            expandableView.performClick();
                        });
                    }
                }
            }
        } catch (Throwable e) {
            Logger.e("checkListItem exception.", e);
        }
    }
}
