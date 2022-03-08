package li.lingfeng.magi.tweaks.communication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;
import li.lingfeng.magi.utils.ViewUtils;

import static li.lingfeng.magi.utils.ReflectUtils.findClass;

@AppLoad(packageName = PackageNames.TELEGRAM, pref = "telegram_remove_floating_button")
public class TelegramRemoveFloatingButton extends TweakBase {

    private static final String LAUNCH_ACTIVITY = "org.telegram.ui.LaunchActivity";
    private static final String ACTION_BAR_LAYOUT = "org.telegram.ui.ActionBar.ActionBarLayout";
    private static final String DIALOGS_ACTIVITY = "org.telegram.ui.DialogsActivity"; // main fragment
    private static final String DIALOGS_ACTIVITY_VIEW = "org.telegram.ui.DialogsActivity$ContentView";

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (!activity.getClass().getName().equals(LAUNCH_ACTIVITY)) {
            return;
        }
        Loader.getMainHandler().post(() -> {
            try {
                ViewGroup actionBarLayout = (ViewGroup) ViewUtils.findViewByType(activity, findClass(ACTION_BAR_LAYOUT));
                removeFloatingButton(actionBarLayout);

                ViewGroup containerViewBack = (ViewGroup) ReflectUtils.getObjectField(actionBarLayout, "containerViewBack");
                containerViewBack.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                    @Override
                    public void onChildViewAdded(View parent, View child) {
                        if (child.getClass().getName().equals(DIALOGS_ACTIVITY_VIEW)) {
                            try {
                                removeFloatingButton(actionBarLayout);
                            } catch (Throwable e) {
                                Logger.e("removeFloatingButton exception.", e);
                            }
                            containerViewBack.setOnHierarchyChangeListener(null);
                        }
                    }

                    @Override
                    public void onChildViewRemoved(View parent, View child) {
                    }
                });
            } catch (Throwable e) {
                Logger.e("TelegramRemoveFloatingButton exception.", e);
            }
        });
    }

    private void removeFloatingButton(ViewGroup actionBarLayout) throws Throwable {
        ArrayList fragmentsStack = (ArrayList) ReflectUtils.getObjectField(actionBarLayout, "fragmentsStack");
        Object fragment = fragmentsStack.stream()
                .filter(f -> f.getClass().getName().equals(DIALOGS_ACTIVITY))
                .findFirst().get();
        Logger.d("fragment " + fragment);

        View floatingButton = (View) ReflectUtils.getObjectField(fragment, "floatingButtonContainer");
        Logger.d("floatingButton " + floatingButton);
        if (floatingButton != null && floatingButton.getParent() != null) {
            ViewUtils.removeView(floatingButton);
        }
    }
}

