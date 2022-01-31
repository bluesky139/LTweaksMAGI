package li.lingfeng.magi.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import li.lingfeng.magi.R;
import li.lingfeng.magi.prefs.NotificationId;
import li.lingfeng.magi.utils.ContextUtils;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ShareUtils;
import li.lingfeng.magi.utils.SimpleSnackbar;

import static li.lingfeng.magi.utils.SimpleSnackbar.FAST_OUT_SLOW_IN_INTERPOLATOR;

public class CopyToShareService extends ForegroundService {

    public static final String CLIP_CHANGE_ACTION = "li.lingfeng.magi.CLIP_CHANGE_ACTION";
    private Handler mHandler;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    private SimpleSnackbar mSnackbar;
    private BroadcastReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.BOTTOM;
        mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        prepareClipboardListener();
    }

    @Override
    protected int getNotificationId() {
        return NotificationId.COPY_TO_SHARE_SERVICE;
    }

    private void prepareClipboardListener() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onClipChanged(intent.getStringExtra("text"),
                        intent.getIntExtra("ltweaks_clip_uid", 0));
            }
        };
        IntentFilter filter = new IntentFilter(CLIP_CHANGE_ACTION);
        registerReceiver(mReceiver, filter);
    }

    public void onClipChanged(String text, int referrer) {
        try {
            dismiss();
            Logger.d("Show snackbar for text: " + text);
            mSnackbar = SimpleSnackbar.make(this, "Got text", SimpleSnackbar.LENGTH_LONG)
                    .setAction(ContextUtils.getLDrawable(R.drawable.ic_search), (v) -> ShareUtils.searchText(this, text, referrer))
                    .setAction(ContextUtils.getLDrawable(R.drawable.ic_incognito), (v) -> ShareUtils.incognitoText(this, text))
                    .setAction(ContextUtils.getLDrawable(R.drawable.ic_edit), (v) -> ShareUtils.selectText(this, text, referrer))
                    .setAction(ContextUtils.getLDrawable(R.drawable.abc_ic_menu_share_mtrl_alpha), (v) -> ShareUtils.shareText(this, text, referrer));
            mWindowManager.addView(mSnackbar, mLayoutParams);
            mSnackbar.setAlpha(0f);
            mHandler.post(() -> {
                ViewCompat.setTranslationY(mSnackbar, mSnackbar.getHeight());
                ViewCompat.animate(mSnackbar)
                        .translationY(0f)
                        .alpha(1f)
                        .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                        .setDuration(250)
                        .start();
            });
            scheduleDismiss();
        } catch (Throwable e) {
            Logger.e("Share clip with overlay window exception.", e);
        }
    }

    private void scheduleDismiss() {
        mHandler.postDelayed(() -> {
            dismiss();
        }, SimpleSnackbar.LENGTH_LONG);
    }

    private void dismiss() {
        mHandler.removeCallbacksAndMessages(null);
        if (mSnackbar != null && mSnackbar.getParent() != null) {
            ViewCompat.animate(mSnackbar)
                    .translationY(mSnackbar.getHeight())
                    .alpha(0f)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setDuration(250)
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            view.setAlpha(0f);
                            mWindowManager.removeView(view);
                        }

                        @Override
                        public void onAnimationCancel(View view) {
                        }
                    })
                    .start();
        }
        mSnackbar = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
