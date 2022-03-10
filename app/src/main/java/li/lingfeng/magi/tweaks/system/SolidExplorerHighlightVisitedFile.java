package li.lingfeng.magi.tweaks.system;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

import static li.lingfeng.magi.utils.ReflectUtils.findClass;

@AppLoad(packageName = PackageNames.SOLID_EXPLORER, pref = "solid_explorer_highlight_visited_files")
public class SolidExplorerHighlightVisitedFile extends TweakBase {

    private static final String MAIN_ACTIVITY = "pl.solidexplorer.SolidExplorer";
    private static final String PANEL_LAYOUT = "pl.solidexplorer.panel.ui.PanelLayout";
    private static final String SAFE_SWIPE_REFRESH_LAYOUT = "pl.solidexplorer.common.gui.SafeSwipeRefreshLayout";
    private static final String CHECKABLE_RELATIVE_LAYOUT = "pl.solidexplorer.common.gui.CheckableRelativeLayout";

    private Handler mHandler;
    private List<View> mHeaderViews;
    private DBHelper mDBHelper;

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();

        mHandler = new Handler();
        mDBHelper = new DBHelper();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    List<View> views = ViewUtils.findAllViewByType(rootView, (Class<View>) findClass(SAFE_SWIPE_REFRESH_LAYOUT));
                    mHeaderViews = ViewUtils.findAllViewByName(rootView, "smart_header");
                    if (views.size() == 2 && mHeaderViews.size() == 2) {
                        Logger.d("2 list.");
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        for (int i = 0; i < 2; ++i) {
                            handleRefreshLayout(views.get(i), i);
                        }

                        List<View> panelLayouts = ViewUtils.findAllViewByType(rootView, (Class<View>) findClass(PANEL_LAYOUT));
                        for (int i = 0; i < 2; ++i) {
                            final int panelId = i;
                            ((ViewGroup) panelLayouts.get(i)).setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                                @Override
                                public void onChildViewAdded(View parent, View child) {
                                    if (child.getClass().getName().equals(SAFE_SWIPE_REFRESH_LAYOUT)) {
                                        handleRefreshLayout(child, panelId);
                                    }
                                }

                                @Override
                                public void onChildViewRemoved(View parent, View child) {
                                }
                            });
                        }
                    }
                } catch (Throwable e) {
                    Logger.e("onGlobalLayout exception.", e);
                }
            }
        });
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (activity.getClass().getName().equals(MAIN_ACTIVITY)) {
            mHandler = null;
            mHeaderViews = null;
            mDBHelper = null;
        }
    }

    private void handleRefreshLayout(View layout, int panelId) {
        Logger.d("handleRefreshLayout " + panelId + ", " + layout);
        GridView gridView = layout.findViewById(android.R.id.list);
        AdapterView.OnItemClickListener originalItemClickListener = gridView.getOnItemClickListener();
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            originalItemClickListener.onItemClick(parent, view, position, id);
            fileClicked(view);
        });

        gridView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                try {
                    if (child.getClass().getName().equals(CHECKABLE_RELATIVE_LAYOUT)) {
                        TextView titleView = (TextView) ViewUtils.findViewByName((ViewGroup) child, "title");
                        checkTitle(titleView, panelId);

                        titleView.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                checkTitle(titleView, panelId);
                            }
                        });
                    }
                } catch (Throwable e) {
                    Logger.e("listView onChildViewAdded exception.", e);
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });
    }

    private void checkTitle(TextView titleView, int panelId) {
        mHandler.post(() -> {
            try {
                ViewGroup parent = (ViewGroup) titleView.getParent();
                TextView subtitleView = ViewUtils.findViewByName(parent, "subtitle1");
                if (subtitleView == null) {
                    Logger.w("No subtitle1, must not be list view, ignore.");
                    return;
                }
                String subtitle = subtitleView.getText().toString();
                if (subtitle.endsWith("B") && !subtitle.contains("/")) {
                    String title = titleView.getText().toString();
                    String folder = getFolderByPannelId(panelId);
                    if (title.equals(mDBHelper.getVisitedFile(folder))) {
                        Logger.v("Visited file " + folder + "/" + title);
                        parent.setBackgroundColor(Color.DKGRAY);
                    } else {
                        parent.setBackground(null);
                    }
                } else {
                    parent.setBackground(null);
                }
            } catch (Throwable e) {
                Logger.e("Check title exception.", e);
            }
        });
    }

    private void fileClicked(View view) {
        try {
            if (view.getClass().getName().equals(CHECKABLE_RELATIVE_LAYOUT)) {
                TextView titleView = ViewUtils.findViewByName((ViewGroup) view, "title");
                TextView subtitleView = ViewUtils.findViewByName((ViewGroup) view, "subtitle1");
                if (subtitleView == null) {
                    Logger.w("No subtitle1, must not be list view, ignore.");
                    return;
                }
                String subtitle = subtitleView.getText().toString();
                if (subtitle.endsWith("B") && !subtitle.contains("/")) {
                    String title = titleView.getText().toString();
                    String folder = getCurrentFolder();
                    Logger.v("Clicked file " + folder + "/" + title);
                    mDBHelper.fileVisited(folder, title);
                    refreshHighlight((GridView) view.getParent(), title);
                }
            }
        } catch (Throwable e) {
            Logger.e("File clicked exception.", e);
        }
    }

    private void refreshHighlight(GridView listView, String title) {
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View child = listView.getChildAt(i);
            if (child.getClass().getName().equals(CHECKABLE_RELATIVE_LAYOUT)) {
                TextView titleView = ViewUtils.findViewByName((ViewGroup) child, "title");
                if (titleView.getText().toString().equals(title)) {
                    child.setBackgroundColor(Color.DKGRAY);
                } else {
                    child.setBackground(null);
                }
            }
        }
    }

    private String getCurrentFolder() {
        int[] location = new int[2];
        mHeaderViews.get(0).getLocationOnScreen(location);
        return getFolderByPannelId(location[0] == 0  ? 0 : 1);
    }

    private String getFolderByPannelId(int panelId) {
        ViewGroup headerView = (ViewGroup) mHeaderViews.get(panelId);
        View rootSwitchView = ViewUtils.findViewByName(headerView, "root_switch");
        TextView rootTextView = (TextView) ViewUtils.nextView(rootSwitchView);
        String path = rootTextView.getText().toString();

        if (rootTextView.getCurrentTextColor() != 0xDEFFFFFF) {
            TextView textView = (TextView) ViewUtils.nextView((View) rootSwitchView.getParent());
            while (textView != null) {
                path += "/" + textView.getText();
                if (textView.getCurrentTextColor() == 0xDEFFFFFF) {
                    break;
                }
                textView = (TextView) ViewUtils.nextView(textView);
            }
        }
        return path;
    }

    class DBHelper extends SQLiteOpenHelper {

        private LruCache<String, String> cache = new LruCache<>(20);

        public DBHelper() {
            super(mApp, "ltweaks_highlight_visited_files", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE IF NOT EXISTS Visited (" +
                    "Folder TEXT PRIMARY KEY NOT NULL, " +
                    "File TEXT NOT NULL" +
                    ");";
            execSQL(db, sql);
            sql = "CREATE UNIQUE INDEX IF NOT EXISTS VisitedIndex ON Visited (Folder);";
            execSQL(db, sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        private void execSQL(SQLiteDatabase db, String sql) {
            Logger.d("execSQL: " + sql);
            db.execSQL(sql);
        }

        private Cursor rawQuery(SQLiteDatabase db, String sql) {
            //Logger.d("rawQuery: " + sql);
            return db.rawQuery(sql, null);
        }

        public void fileVisited(String folder, String file) {
            String sql = "REPLACE INTO Visited (Folder, File) VALUES ('%1$s', '%2$s');";
            execSQL(getWritableDatabase(), String.format(sql, folder.replace("'", "''"),
                    file.replace("'", "''")));
            cache.put(folder, file);
        }

        public String getVisitedFile(String folder) {
            String file = cache.get(folder);
            if (file != null) {
                return !file.isEmpty() ? file : null;
            }
            String sql = "SELECT File from Visited WHERE Folder='%1$s' LIMIT 1;";
            Cursor cursor = rawQuery(getWritableDatabase(), String.format(sql, folder.replace("'", "''")));
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                file = cursor.getString(0);
            }
            cursor.close();
            cache.put(folder, file != null ? file : "");
            return file;
        }
    }
}
