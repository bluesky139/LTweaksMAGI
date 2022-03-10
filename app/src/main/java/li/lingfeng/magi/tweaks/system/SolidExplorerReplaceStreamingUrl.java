package li.lingfeng.magi.tweaks.system;

import android.app.Activity;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.prefs.PrefStore;
import li.lingfeng.magi.tweaks.base.Result;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.SOLID_EXPLORER, pref = "solid_explorer_replace_streaming_url")
public class SolidExplorerReplaceStreamingUrl extends TweakBase {

    private static final String MAIN_ACTIVITY = "pl.solidexplorer.SolidExplorer";
    private static final String STREAMING_SERVICE = "pl.solidexplorer.files.stream.MediaStreamingService";
    private List<View> mActionBars;
    private Map<String, String> mServerMap;

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (!activity.getClass().getName().equals(MAIN_ACTIVITY)) {
            return;
        }
        String value = PrefStore.instance.getString("solid_explorer_replace_streaming_url", "");
        String[] values = StringUtils.split(value, ',');
        if (values.length == 0) {
            return;
        }
        mServerMap = new HashMap<>(values.length);
        for (int i = 0; i < values.length; ++i) {
            String[] map = StringUtils.split(values[i], ':');
            mServerMap.put(map[0], map[1]);
            Logger.i("Streaming url replace: " + map[0] + " -> " + map[1]);
        }

        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    mActionBars = ViewUtils.findAllViewByName(rootView, "action_bar_panel");
                    if (mActionBars.size() == 2) {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
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
            mActionBars = null;
            mServerMap = null;
        }
    }

    @Override
    public Result startActivity(IApplicationThread caller, String callingPackage, String callingFeatureId, final Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options) throws RemoteException {
        return new Result().before((r) -> {
            if (mServerMap == null) {
                return;
            }
            if (intent.getBooleanExtra("streaming", false)) {
                String title = getCurrentTitle();
                String server = mServerMap.get(title);
                if (server != null) {
                    String url = intent.getDataString();
                    String newUrl = url.replaceFirst("^http:\\/\\/127\\.0\\.0\\.1:\\d+\\/", "http://" + server + "/");
                    intent.setData(Uri.parse(newUrl));
                    Logger.v("Streaming url: " + newUrl);

                    // Stop streaming service
                    Loader.getMainHandler().post(() -> {
                        Intent intent2 = new Intent();
                        intent2.setClassName(PackageNames.SOLID_EXPLORER, STREAMING_SERVICE);
                        intent2.putExtra("extra_id", 1);
                        Loader.getApplication().startService(intent2);
                    });
                }
            }
        });
    }

    private String getCurrentTitle() {
        int[] location = new int[2];
        mActionBars.get(0).getLocationOnScreen(location);
        return getTitleByPannelId(location[0] == 0  ? 0 : 1);
    }

    private String getTitleByPannelId(int panelId) {
        ViewGroup actionBar = (ViewGroup) mActionBars.get(panelId);
        TextView titleView = (TextView) ViewUtils.findViewByName(actionBar, "ab_title");
        return titleView.getText().toString();
    }
}
