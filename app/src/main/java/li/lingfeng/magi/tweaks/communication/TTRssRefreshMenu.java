package li.lingfeng.magi.tweaks.communication;

import android.app.Activity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.R;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.ContextUtils;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.TT_RSS, pref = "ttrss_refresh_menu")
public class TTRssRefreshMenu extends TweakBase {

    private static final String MASTER_ACTIVITY = "org.fox.ttrss.MasterActivity";
    private static final int ITEM_ID = 10001;

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        Loader.getMainHandler().post(() -> {
            try {
                if (activity.getClass().getName().equals(MASTER_ACTIVITY)) {
                    ViewGroup rootView = activity.findViewById(android.R.id.content);
                    rootView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                        try {
                            View fab = ViewUtils.findViewByName(activity, "master_fab");
                            if (fab != null) {
                                Logger.v("Remove master_fab.");
                                ViewUtils.removeView(fab);
                            }
                        } catch (Throwable e) {
                            Logger.e("Hide headlines_fab exception.", e);
                        }
                    });

                    Object delegate = ReflectUtils.callMethod(activity, "getDelegate");
                    Object actionBar = ReflectUtils.getObjectField(delegate, "mActionBar");
                    Window.Callback original = (Window.Callback) ReflectUtils.getObjectField(actionBar, "mWindowCallback");
                    ReflectUtils.setObjectField(actionBar, "mWindowCallback", new Window.Callback() {

                        @Override
                        public boolean dispatchKeyEvent(KeyEvent event) {
                            return original.dispatchKeyEvent(event);
                        }

                        @Override
                        public boolean dispatchKeyShortcutEvent(KeyEvent event) {
                            return original.dispatchKeyShortcutEvent(event);
                        }

                        @Override
                        public boolean dispatchTouchEvent(MotionEvent event) {
                            return original.dispatchTouchEvent(event);
                        }

                        @Override
                        public boolean dispatchTrackballEvent(MotionEvent event) {
                            return original.dispatchTrackballEvent(event);
                        }

                        @Override
                        public boolean dispatchGenericMotionEvent(MotionEvent event) {
                            return original.dispatchGenericMotionEvent(event);
                        }

                        @Override
                        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
                            return original.dispatchPopulateAccessibilityEvent(event);
                        }

                        @Nullable
                        @Override
                        public View onCreatePanelView(int featureId) {
                            return original.onCreatePanelView(featureId);
                        }

                        @Override
                        public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
                            int idMenuGroup = ContextUtils.getIdId("menu_group_headlines");
                            MenuItem menuItem = menu.add(idMenuGroup, ITEM_ID, Menu.NONE, ContextUtils.getLString(R.string.ttrss_refresh));
                            menuItem.setIcon(ContextUtils.getDrawable("ic_refresh"));
                            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                            return original.onCreatePanelMenu(featureId, menu);
                        }

                        @Override
                        public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {
                            menu.findItem(ContextUtils.getIdId("headlines_select")).setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                            menu.findItem(ContextUtils.getIdId("headlines_toggle_sort_order")).setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                            return original.onPreparePanel(featureId, view, menu);
                        }

                        @Override
                        public boolean onMenuOpened(int featureId, @NonNull Menu menu) {
                            return original.onMenuOpened(featureId, menu);
                        }

                        @Override
                        public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
                            if (item.getItemId() == ITEM_ID) {
                                try {
                                    Logger.i("Refresh headlines.");
                                    Object fragmentManager = ReflectUtils.callMethod(activity, "getSupportFragmentManager");
                                    Object headlinesFragment = ReflectUtils.callMethod(fragmentManager, "findFragmentByTag", "headlines");
                                    ReflectUtils.callMethod(headlinesFragment, "refresh",
                                            new Object[]{false}, new Class[]{boolean.class});
                                } catch (Throwable e) {
                                    Logger.e("Exception on click refresh headline.", e);
                                }
                                return true;
                            } else {
                                return original.onMenuItemSelected(featureId, item);
                            }
                        }

                        @Override
                        public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
                            original.onWindowAttributesChanged(attrs);
                        }

                        @Override
                        public void onContentChanged() {
                            original.onContentChanged();
                        }

                        @Override
                        public void onWindowFocusChanged(boolean hasFocus) {
                            original.onWindowFocusChanged(hasFocus);
                        }

                        @Override
                        public void onAttachedToWindow() {
                            original.onAttachedToWindow();
                        }

                        @Override
                        public void onDetachedFromWindow() {
                            original.onDetachedFromWindow();
                        }

                        @Override
                        public void onPanelClosed(int featureId, @NonNull Menu menu) {
                            original.onPanelClosed(featureId, menu);
                        }

                        @Override
                        public boolean onSearchRequested() {
                            return original.onSearchRequested();
                        }

                        @Override
                        public boolean onSearchRequested(SearchEvent searchEvent) {
                            return original.onSearchRequested(searchEvent);
                        }

                        @Nullable
                        @Override
                        public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
                            return original.onWindowStartingActionMode(callback);
                        }

                        @Nullable
                        @Override
                        public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
                            return original.onWindowStartingActionMode(callback, type);
                        }

                        @Override
                        public void onActionModeStarted(ActionMode mode) {
                            original.onActionModeStarted(mode);
                        }

                        @Override
                        public void onActionModeFinished(ActionMode mode) {
                            original.onActionModeFinished(mode);
                        }
                    });
                }
            } catch (Throwable e) {
                Logger.e("TTRssRefreshMenu exception.", e);
            }
        });
    }
}
