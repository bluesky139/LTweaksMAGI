package li.lingfeng.magi.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import static li.lingfeng.magi.utils.ContextUtils.dp2px;

/**
 * Created by smallville on 2017/2/12.
 */

public class SimpleSnackbar extends LinearLayout {

    public static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    public static final int LENGTH_LONG  = 3500;
    public static final int LENGTH_SHORT = 2000;

    protected Context mContext;
    protected Handler mHandler;

    protected TextView mTextView;
    protected int mDuration;

    public static SimpleSnackbar make(Context context, String mainText, int duration) {
        SimpleSnackbar snackbar = new SimpleSnackbar(context, mainText, duration);
        return snackbar;
    }

    private SimpleSnackbar(Context context, String mainText, int duration) {
        super(context);
        mContext = context;
        mDuration = duration;
        mHandler = new Handler();
        setBackgroundColor(0xFF303030);
        setOrientation(LinearLayout.HORIZONTAL);
        setOnTouchListener((v, event) -> true);
        createTextView(mainText);
    }

    protected void createTextView(String mainText) {
        mTextView = new TextView(getContext());
        mTextView.setPadding(dp2px(12f), dp2px(14f), dp2px(12f), dp2px(14f));
        mTextView.setTextSize(14f);
        mTextView.setText(mainText);
        mTextView.setTextColor(Color.WHITE);
        mTextView.setEllipsize(TextUtils.TruncateAt.END);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_START);
        }

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT | Gravity.START;
        params.weight = 1;
        addView(mTextView, params);
    }

    public SimpleSnackbar setAction(String buttonText, OnClickListener clickListener) {
        createButton(buttonText, clickListener);
        return this;
    }

    protected void createButton(String buttonText, final OnClickListener clickListener) {
        Button button = new Button(getContext());
        button.setMinWidth(dp2px(48f));

        StateListDrawable bgColor = new StateListDrawable();
        bgColor.setExitFadeDuration(250);
        GradientDrawable pressedDrawable = new GradientDrawable();
        pressedDrawable.setColor(0xFF464646);
        pressedDrawable.setStroke(dp2px(4f), 0xFF303030);
        bgColor.addState(new int[] { android.R.attr.state_pressed }, pressedDrawable);
        bgColor.addState(new int[] {}, new ColorDrawable(0xFF303030));
        button.setBackgroundDrawable(bgColor);

        button.setTextColor(0xFFFF4081);
        button.setText(buttonText);
        button.setOnClickListener((v) -> {
            Logger.i("SimpleSnackbar onClick.");
            dismiss();
            clickListener.onClick(v);
        });

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT | Gravity.END;
        params.rightMargin = dp2px(6f);
        addView(button, params);
    }

    public SimpleSnackbar setAction(Drawable drawable, OnClickListener clickListener) {
        createButton(drawable, clickListener);
        return this;
    }

    protected void createButton(Drawable drawable, final OnClickListener clickListener) {
        Button button = new Button(getContext());
        button.setMinWidth(dp2px(48f));

        drawable.setTintMode(PorterDuff.Mode.SRC_ATOP);
        drawable.setTint(0xFFFF4081);

        StateListDrawable bgColor = new StateListDrawable();
        bgColor.setExitFadeDuration(250);
        GradientDrawable pressedDrawable = new GradientDrawable();
        pressedDrawable.setColor(0xFF464646);
        pressedDrawable.setStroke(dp2px(4f), 0xFF303030);
        bgColor.addState(new int[] { android.R.attr.state_pressed }, pressedDrawable);
        bgColor.addState(new int[] {}, drawable);
        button.setBackgroundDrawable(bgColor);

        button.setOnClickListener((v) -> {
            Logger.i("SimpleSnackbar onClick.");
            dismiss();
            clickListener.onClick(v);
        });

        LayoutParams params = new LayoutParams(dp2px(20f), dp2px(20f));
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT | Gravity.END;
        params.leftMargin = dp2px(10f);
        params.rightMargin = dp2px(10f);
        addView(button, params);
    }

    public void show() {
        if (!(mContext instanceof Activity)) {
            Logger.e("SimpleSnackbar context is not activity, " + mContext);
            return;
        }

        Logger.i("SimpleSnackbar show.");
        Activity activity = (Activity) mContext;
        View view = activity.findViewById(android.R.id.content);
        if (view == null) {
            Logger.w("android.R.id.content is null.");
            return;
        }
        ViewGroup rootView = (ViewGroup) view.getRootView();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;

        float yPos = rootView.getY() + rootView.getHeight();
        int windowHeight = ViewUtils.getWindowHeight(activity);
        Logger.d("yPos " + yPos + ", windowHeight " + windowHeight);
        if (yPos > windowHeight) {
            params.bottomMargin = ViewUtils.getWindowHeightWithNavigator(activity) - windowHeight;
            Logger.d("params.bottomMargin " + params.bottomMargin);
        }
        setAlpha(0f);
        rootView.addView(this, params);

        mHandler.post(() -> {
            ViewCompat.setTranslationY(SimpleSnackbar.this, SimpleSnackbar.this.getHeight());
            ViewCompat.animate(SimpleSnackbar.this)
                    .translationY(0f)
                    .alpha(1f)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setDuration(250)
                    .start();
        });
        scheduleDismiss();
    }

    public void dismiss() {
        Logger.i("SimpleSnackbar dismiss.");
        mHandler.removeCallbacksAndMessages(null);
        ViewCompat.animate(this)
                .translationY(getHeight())
                .alpha(0f)
                .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                .setDuration(250)
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        try {
                            SimpleSnackbar.this.setVisibility(View.INVISIBLE);
                            ((ViewGroup) SimpleSnackbar.this.getParent()).removeView(SimpleSnackbar.this);
                        } catch (Throwable e) {}
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                    }
                })
                .start();
    }

    protected void scheduleDismiss() {
        mHandler.postDelayed(() -> dismiss(), mDuration);
    }
}
