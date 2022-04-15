package li.lingfeng.magi.tweaks.entertainment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.services.MXDanmakuService;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.ContextUtils;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.Utils;
import li.lingfeng.magi.utils.ViewUtils;

@AppLoad(packageName = PackageNames.MX_PLAYER_PRO, pref = "mxplayer_danmaku")
public class MXPlayerDanmaku extends TweakBase {

    public static final int OP_CREATE       = 0;
    public static final int OP_SHOW_CONTROL = 1;
    public static final int OP_HIDE_CONTROL = 2;
    public static final int OP_SHOW_ALL     = 3;
    public static final int OP_HIDE_ALL     = 4;
    public static final int OP_SEEK_TO      = 5;
    public static final int OP_RESUME       = 6;
    public static final int OP_PAUSE        = 7;
    public static final int OP_DESTROY      = 8;

    private static final String ACTIVITY_SCREEN = "com.mxtech.videoplayer.pro.ActivityScreen";

    private boolean mCreated = false;
    private boolean mPlaying;
    private int mControllerId = 0;
    private ImageButton mPlayButton;

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (!activity.getClass().getName().equals(ACTIVITY_SCREEN)) {
            return;
        }
        try {
            mPlaying = true;
            mControllerId = ContextUtils.getIdId("controller");

            TextView durationText = ViewUtils.findViewByName(activity, "durationText");
            durationText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!mCreated) {
                        if (activity.findViewById(mControllerId).getVisibility() == View.VISIBLE) {
                            if (createControlIfNot(activity, s.toString())) {
                                durationText.removeTextChangedListener(this);
                            }
                        }
                    } else {
                        durationText.removeTextChangedListener(this);
                    }
                }
            });

            View controller = ViewUtils.findViewByName(activity, "controller");
            controller.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (!mCreated) {
                    String durationString = durationText.getText().toString();
                    if (!durationString.isEmpty()) {
                        createControlIfNot(activity, durationString);
                    }
                } else {
                    if (controller.getVisibility() == View.VISIBLE) {
                        sendCommand(OP_SHOW_CONTROL);
                    } else {
                        sendCommand(OP_HIDE_CONTROL);
                    }
                    checkPlaying();
                }
            });

            TextView posText = ViewUtils.findViewByName(activity, "posText");
            posText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String pos = s.toString();
                    Logger.v("Update pos " + pos);
                    ContentValues values = new ContentValues(1);
                    values.put("seconds", Utils.stringTimeToSeconds(pos));
                    sendCommand(OP_SEEK_TO, values);
                }
            });

            mPlayButton = ViewUtils.findViewByName(activity, "playpause");
            View.OnClickListener originalListener = ViewUtils.getViewClickListener(mPlayButton);
            mPlayButton.setOnClickListener(v -> {
                originalListener.onClick(v);
                checkPlaying();
            });

            View uiLayout = ViewUtils.findViewByName(activity, "ui_layout");
            View.OnTouchListener originalTouchListener = ViewUtils.getViewTouchListener(uiLayout);
            uiLayout.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Loader.getMainHandler().post(() -> {
                        checkPlaying();
                    });
                }
                return originalTouchListener.onTouch(v, event);
            });
        } catch (Throwable e) {
            Logger.e("MXPlayerDanmaku exception.", e);
        }
    }

    private boolean createControlIfNot(Activity activity, String durationString) {
        if (!mCreated) {
            View subNavBar = ViewUtils.findViewByName(activity, "subNaviBar");
            int duration = Utils.stringTimeToSeconds(durationString);
            if (duration > 0 && subNavBar.getVisibility() == View.VISIBLE) {
                Logger.v("Create control.");
                mCreated = true;
                ContentValues values = new ContentValues(2);
                values.put("file_path", activity.getIntent().getDataString());
                values.put("video_duration", duration);
                sendCommand(OP_CREATE, values);
            }
        }
        return mCreated;
    }

    private void checkPlaying() {
        boolean playing = mPlayButton.getDrawable().getLevel() == 2;
        if (mPlaying != playing) {
            mPlaying = playing;
            Logger.v("Play/Pause changed, mPlaying " + mPlaying);
            sendCommand(mPlaying ? OP_RESUME : OP_PAUSE);
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (activity.getClass().getName().equals(ACTIVITY_SCREEN)) {
            Logger.v("In MX Player.");
            sendCommand(OP_SHOW_ALL);
            if (mPlaying) {
                sendCommand(OP_RESUME);
            }
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (activity.getClass().getName().equals(ACTIVITY_SCREEN)) {
            Logger.v("Not in MX Player.");
            sendCommand(OP_HIDE_ALL);
            if (mPlaying) {
                sendCommand(OP_PAUSE);
            }
        }
    }

    @Override
    public void onActivityPreDestroyed(@NonNull Activity activity) {
        if (activity.getClass().getName().equals(ACTIVITY_SCREEN)) {
            sendCommand(OP_DESTROY);
            mCreated = false;
            mPlayButton = null;
        }
    }

    private void sendCommand(int op) {
        sendCommand(op, new ContentValues());
    }

    private void sendCommand(int op, ContentValues values) {
        Intent intent = new Intent(MXDanmakuService.ACTION_MX_DANMAKU_CONTROL);
        intent.putExtra("op", op);
        intent.putExtra("values", values);
        intent.setPackage(PackageNames.L_TWEAKS);
        mApp.sendBroadcast(intent);
    }
}
