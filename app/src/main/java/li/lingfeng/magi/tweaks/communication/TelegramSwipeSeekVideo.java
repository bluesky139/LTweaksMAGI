package li.lingfeng.magi.tweaks.communication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.reflect.Array;

import androidx.annotation.NonNull;
import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.ContextUtils;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ReflectUtils;
import li.lingfeng.magi.utils.ShareUtils;
import li.lingfeng.magi.utils.ViewUtils;

import static li.lingfeng.magi.utils.ContextUtils.dp2px;
import static li.lingfeng.magi.utils.ReflectUtils.findClass;

@AppLoad(packageName = PackageNames.TELEGRAM, pref = "telegram_swipe_seek_video")
public class TelegramSwipeSeekVideo extends TweakBase {

    private static final String PHOTO_VIEWER = "org.telegram.ui.PhotoViewer";
    private static final String PHOTO_VIEWER_FRAMELAYOUT = PHOTO_VIEWER + "$FrameLayoutDrawer";
    private static final String ACTION_BAR_MENU = "org.telegram.ui.ActionBar.ActionBarMenu";
    private static final int CORNER_DP = 80;
    private float mScrollDistance = 0f;
    private TextView mSeekTextView;
    private ImageButton mPlayButton;
    private boolean mIsPlaying;
    private boolean mInVideoViewer = false;

    @Override
    protected boolean shouldInterceptWindowManagerAddView() {
        return true;
    }

    @Override
    protected void windowManagerAddView(View view) {
        if (view.getClass().getName().startsWith(PHOTO_VIEWER)) {
            Loader.getMainHandler().post(() -> {
                try {
                    setVideoTouchListener((ViewGroup) view);
                } catch (Throwable e) {
                    Logger.e("setVideoTouchListener exception.", e);
                }
            });
        }
    }

    private void setVideoTouchListener(final ViewGroup view) throws Throwable {
        Logger.d("setVideoTouchListener " + view);
        final Object photoViewer = ReflectUtils.getSurroundingThis(view);
        final Object videoPlayer = ReflectUtils.getObjectField(photoViewer, "videoPlayer");
        if (videoPlayer == null) {
            view.setOnTouchListener(null);
            return;
        }
        final GestureDetector gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                try {
                    mScrollDistance += distanceX;
                    if (Math.abs(mScrollDistance) >= dp2px(12)) {
                        showSeekText(view);
                    }
                } catch (Throwable e) {
                    Logger.e("onScroll exception.", e);
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent event) {
                playPause(videoPlayer);
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                try {
                    if (event.getX() > view.getWidth() - dp2px(CORNER_DP) && event.getY() < dp2px(CORNER_DP)) {
                        Logger.i("Share image from video.");
                        TextureView videoTextureView = (TextureView) ReflectUtils.getObjectField(photoViewer, "videoTextureView");
                        Bitmap bitmap = videoTextureView.getBitmap();
                        ShareUtils.shareImage(view.getContext(), bitmap);
                        return true;
                    }

                    boolean isActionBarVisible = ReflectUtils.getBooleanField(photoViewer, "isActionBarVisible");
                    Logger.v("toggleActionBar " + !isActionBarVisible);
                    ReflectUtils.callMethod(photoViewer, "toggleActionBar",
                            new Object[] { !isActionBarVisible, true }, new Class[] { boolean.class, boolean.class });
                } catch (Throwable e) {
                    Logger.e("toggleActionBar exception.", e);
                }
                return true;
            }
        });

        view.setOnTouchListener((v, event) -> {
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                try {
                    if (Math.abs(mScrollDistance) < dp2px(12)) {
                        return true;
                    }
                    hideSeekText();
                    long second = -(long)(mScrollDistance / view.getWidth() * 90);
                    mScrollDistance = 0f;
                    if (second == 0) {
                        return true;
                    }
                    long pos = (long) ReflectUtils.callMethod(videoPlayer, "getCurrentPosition");
                    long newPos = pos + second * 1000L;
                    Logger.v("Seek from " + pos + " to " + newPos);
                    ReflectUtils.callMethod(videoPlayer, "seekTo", new Object[] { newPos }, new Class[] { long.class });
                } catch (Throwable e) {
                    Logger.e("Seek exception.", e);
                }
            }
            return true;
        });

        ReflectUtils.callMethod(videoPlayer, "setLooping", new Object[] { false }, new Class[] { boolean.class });

        if (mPlayButton == null) {
            Drawable _playDrawable = ContextUtils.getDrawable("s_play");
            Drawable _pauseDrawable = ContextUtils.getDrawable("s_pause");
            LevelListDrawable levelDrawable = new LevelListDrawable();
            levelDrawable.addLevel(3, 3, _playDrawable);
            levelDrawable.addLevel(4, 4, _pauseDrawable);

            mPlayButton = new ImageButton(view.getContext());
            mPlayButton.setBackgroundColor(Color.TRANSPARENT);
            mPlayButton.setImageDrawable(levelDrawable);

            View controlLayout = (View) ReflectUtils.getObjectField(photoViewer, "videoPlayerControlFrameLayout");
            int height = controlLayout.getHeight();
            ((FrameLayout.LayoutParams) controlLayout.getLayoutParams()).leftMargin = height;
            ViewGroup controlParent = (ViewGroup) controlLayout.getParent();
            controlParent.addView(mPlayButton, new FrameLayout.LayoutParams(height, height, Gravity.LEFT | Gravity.BOTTOM));

            Object photoProgressViews = ReflectUtils.getObjectField(photoViewer, "photoProgressViews");
            Object photoProgressView = Array.get(photoProgressViews, 0);
            Drawable playPauseDrawable = (Drawable) ReflectUtils.getObjectField(photoProgressView, "playPauseDrawable");
            Drawable playDrawable = (Drawable) ReflectUtils.getObjectField(photoProgressView, "playDrawable");
            ReflectUtils.setObjectField(playDrawable, "background", new ColorDrawable(Color.TRANSPARENT));
            ReflectUtils.setObjectField(playDrawable, "icon", new ColorDrawable(Color.TRANSPARENT) {
                @Override
                public void draw(Canvas canvas) {
                    try {
                        mIsPlaying = ReflectUtils.getBooleanField(playPauseDrawable, "pause");
                        if (!mIsPlaying) {
                            mPlayButton.setImageLevel(3);
                            mIsPlaying = true;
                        } else {
                            mPlayButton.setImageLevel(4);
                            mIsPlaying = false;
                        }
                    } catch (Throwable e) {
                        Logger.e("Play/Pause from icon draw exception.", e);
                    }
                }
            });

            View actionBar = (View) ViewUtils.findViewByType(view, findClass(ACTION_BAR_MENU)).getParent();
            controlParent.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (mInVideoViewer) {
                    Loader.getMainHandler().postDelayed(() -> {
                        mPlayButton.setVisibility(actionBar.getVisibility());
                    }, 200);
                }
            });
        } else {
            mPlayButton.setVisibility(View.VISIBLE);
        }
        mPlayButton.setImageLevel(4);
        mPlayButton.setOnClickListener(v -> {
            playPause(videoPlayer);
        });
        mIsPlaying = true;
        mInVideoViewer = true;

        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                Logger.d("onViewDetachedFromWindow " + view);
                mPlayButton.setVisibility(View.INVISIBLE);
                mPlayButton.setOnClickListener(null);
                mInVideoViewer = false;
                view.setOnTouchListener(null);
                view.removeOnAttachStateChangeListener(this);
            }
        });
    }

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    @Override
    public void onActivityPreDestroyed(@NonNull Activity activity) {
        try {
            if (mPlayButton != null && mPlayButton.getContext() == activity) {
                Logger.d("Destroy mPlayButton");
                mPlayButton = null;
                mInVideoViewer = false;
                ReflectUtils.setStaticObjectField(findClass(PHOTO_VIEWER), "progressDrawables", null);
            }
        } catch (Throwable e) {
            Logger.e("Destroy mPlayButton exception.", e);
        }
    }

    private void showSeekText(View view) throws Throwable {
        if (mSeekTextView == null) {
            View view2 = ViewUtils.findViewByType((ViewGroup) view, findClass(PHOTO_VIEWER_FRAMELAYOUT));
            mSeekTextView = new TextView(view.getContext());
            mSeekTextView.setTextSize(26);
            mSeekTextView.setTextColor(Color.LTGRAY);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            ((ViewGroup) view2).addView(mSeekTextView, layoutParams);
        }
        int second = -(int)(mScrollDistance / view.getWidth() * 90);
        mSeekTextView.setText("[" + (second > 0 ? "+" : "") + second + "]");
    }

    private void hideSeekText() throws Throwable {
        if (mSeekTextView != null) {
            ViewUtils.removeView(mSeekTextView);
            mSeekTextView = null;
        }
    }

    private void playPause(Object videoPlayer) {
        try {
            if ((boolean) ReflectUtils.callMethod(videoPlayer, "isPlaying")) {
                Logger.v("Pause.");
                ReflectUtils.callMethod(videoPlayer, "pause");
            } else {
                Logger.v("Play.");
                ReflectUtils.callMethod(videoPlayer, "play");
            }
        } catch (Throwable e) {
            Logger.e("Play/Pause exception.", e);
        }
    }
}
