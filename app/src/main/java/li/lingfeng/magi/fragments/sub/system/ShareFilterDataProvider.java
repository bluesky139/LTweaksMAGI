package li.lingfeng.magi.fragments.sub.system;

import android.content.Intent;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import li.lingfeng.magi.R;
import li.lingfeng.magi.activities.ListCheckActivity;
import li.lingfeng.magi.prefs.PrefStore;
import li.lingfeng.magi.utils.Logger;


/**
 * Created by sv on 18-2-17.
 */

public class ShareFilterDataProvider extends ListCheckActivity.DataProvider {

    public class ActivityInfo {
        public ResolveInfo mInfo;
        public boolean mDisabled;

        public ActivityInfo(ResolveInfo info, boolean disabled) {
            mInfo = info;
            mDisabled = disabled;
        }
    }

    private Map<String, ActivityInfo> mMapAllInfos = new TreeMap<>();
    private Map<String, ActivityInfo> mMapDisabledInfos = new TreeMap<>();
    private Map<String, ActivityInfo> mMapEnabledInfos = new TreeMap<>();
    private ArrayList<ActivityInfo> mAllInfos;
    private ArrayList<ActivityInfo> mDisabledInfos;
    private ArrayList<ActivityInfo> mEnabledInfos;
    private ArrayList<String> mDisabledActivities;
    private boolean mNeedReload = true;

    public ShareFilterDataProvider(ListCheckActivity activity) {
        super(activity);
        mDisabledActivities = new ArrayList<>(PrefStore.instance.getStringSet("system_share_filter", new HashSet<>(0)));
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        List<ResolveInfo> infos = mActivity.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : infos) {
            String fullActivityName = info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.name;
            ActivityInfo activityInfo = new ActivityInfo(info, mDisabledActivities.contains(fullActivityName));
            mMapAllInfos.put(fullActivityName, activityInfo);
            if (activityInfo.mDisabled) {
                mMapDisabledInfos.put(fullActivityName, activityInfo);
            } else {
                mMapEnabledInfos.put(fullActivityName, activityInfo);
            }
        }
        reload();
    }

    @Override
    protected String getActivityTitle() {
        return mActivity.getString(R.string.pref_system_share_filter);
    }

    @Override
    protected String[] getTabTitles() {
        return new String[] {
                mActivity.getString(R.string.all),
                mActivity.getString(R.string.disabled),
                mActivity.getString(R.string.enabled)
        };
    }

    @Override
    protected int getListItemCount(int tab) {
        if (tab == 0) {
            return mAllInfos.size();
        } else if (tab == 1) {
            return mDisabledInfos.size();
        } else if (tab == 2) {
            return mEnabledInfos.size();
        } else {
            throw new RuntimeException("Unknown tab " + tab);
        }
    }

    @Override
    protected ListItem getListItem(int tab, int position) {
        List<ActivityInfo> infos;
        if (tab == 0) {
            infos = mAllInfos;
        } else if (tab == 1) {
            infos = mDisabledInfos;
        } else if (tab == 2) {
            infos = mEnabledInfos;
        } else {
            throw new RuntimeException("Unknown tab " + tab);
        }

        ListItem item = new ListItem();
        final ActivityInfo activityInfo = infos.get(position);
        item.mData = activityInfo;
        item.mIcon = activityInfo.mInfo.loadIcon(mActivity.getPackageManager());
        item.mTitle = activityInfo.mInfo.activityInfo.applicationInfo.loadLabel(mActivity.getPackageManager());
        item.mDescription = activityInfo.mInfo.loadLabel(mActivity.getPackageManager());
        item.mChecked = activityInfo.mDisabled;
        return item;
    }

    @Override
    protected boolean reload() {
        if (!mNeedReload) {
            return false;
        }

        mNeedReload = false;
        mAllInfos = new ArrayList<>(mMapAllInfos.values());
        mDisabledInfos = new ArrayList<>(mMapDisabledInfos.values());
        mEnabledInfos = new ArrayList<>(mMapEnabledInfos.values());
        Logger.d("mAllInfos " + mAllInfos.size() + ", mDisabledInfos " + mDisabledInfos.size() + ", mEnabledInfos " + mEnabledInfos.size());
        return true;
    }

    @Override
    public void onCheckedChanged(ListItem item, Boolean isChecked) {
        ActivityInfo activityInfo = (ActivityInfo) item.mData;
        String fullActivityName = activityInfo.mInfo.activityInfo.applicationInfo.packageName + "/" + activityInfo.mInfo.activityInfo.name;
        Logger.i((isChecked ? "Disabled" : "Enabled") + " share activity " + fullActivityName);

        activityInfo.mDisabled = isChecked;
        if (isChecked) {
            mMapDisabledInfos.put(fullActivityName, activityInfo);
            mMapEnabledInfos.remove(fullActivityName);
            mDisabledActivities.add(fullActivityName);
        } else {
            mMapDisabledInfos.remove(fullActivityName);
            mMapEnabledInfos.put(fullActivityName, activityInfo);
            mDisabledActivities.remove(fullActivityName);
        }
        mNeedReload = true;

        PrefStore.instance.putStringSet("system_share_filter", new HashSet<>(mDisabledActivities));
    }
}
