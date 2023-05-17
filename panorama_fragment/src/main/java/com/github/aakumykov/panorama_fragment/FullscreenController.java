package com.github.aakumykov.panorama_fragment;

import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class FullscreenController implements View.OnApplyWindowInsetsListener {

    public static final int SHOW_TRANSIENT_BARS_BY_SWIPE = 1;
    public static final int SHOW_BARS_BY_TOUCH = 2;
    public static final int SHOW_BARS_BY_SWIPE = 3;

    private final WindowInsetsControllerCompat mWindowInsetsControllerCompat;
    private final int mShowBarsBehaviour;

    @Nullable private Callback mCallback;
    private boolean mIsFullScreen = false;


    public FullscreenController(Activity activity, int showSystemBarsBehaviour) {

        mShowBarsBehaviour = showSystemBarsBehaviour;

        final Window window = activity.getWindow();
        View decorView = window.getDecorView();

        decorView.setOnApplyWindowInsetsListener(this);
        mWindowInsetsControllerCompat = WindowCompat.getInsetsController(window, decorView);

        mWindowInsetsControllerCompat.setSystemBarsBehavior(systemBarsBehaviour());
    }


    public void enterFullScreen() {
        mWindowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars());
    }

    public void exitFullScreen() {
        mWindowInsetsControllerCompat.show(WindowInsetsCompat.Type.systemBars());
    }


    public void setCallback(@NonNull Callback callback) {
        mCallback = callback;
    }

    public void unsetCallback() {
        mCallback = null;
    }


    public boolean isFullScreen() {
        return mIsFullScreen;
    }

    @Override
    public WindowInsets onApplyWindowInsets(View v, WindowInsets windowInsets) {

        WindowInsetsCompat windowInsetsCompat = WindowInsetsCompat.toWindowInsetsCompat(windowInsets);

        if (windowInsetsCompat.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsetsCompat.isVisible(WindowInsetsCompat.Type.statusBars())) {
            mIsFullScreen = false;
            if (null != mCallback)
                mCallback.onExitFullScreen();
        }
        else {
            mIsFullScreen = true;
            if (null != mCallback)
                mCallback.onEnterFullScreen();
        }

        return v.onApplyWindowInsets(windowInsets);
    }


    public interface Callback {
        void onEnterFullScreen();
        void onExitFullScreen();
    }


    private int systemBarsBehaviour() {
        switch (mShowBarsBehaviour) {
            case SHOW_BARS_BY_TOUCH:
                return WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH;
            case SHOW_BARS_BY_SWIPE:
                return WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE;
            case SHOW_TRANSIENT_BARS_BY_SWIPE:
                return WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;
            default:
                throw new IllegalArgumentException("Неизвестное значение: "+mShowBarsBehaviour);
        }
    }
}
