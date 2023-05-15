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

    private final WindowInsetsControllerCompat mWindowInsetsControllerCompat;
    private final ShowSystemBarsBehaviour mShowBarsBehaviour;
    @Nullable private Callback mCallback;
    private boolean mIsFullScreen = false;

    public FullscreenController(Activity activity, ShowSystemBarsBehaviour showSystemBarsBehaviour) {

        mShowBarsBehaviour = showSystemBarsBehaviour;

        final Window window = activity.getWindow();
        final View decorView = window.getDecorView();

        decorView.setOnApplyWindowInsetsListener(this);

        mWindowInsetsControllerCompat = WindowCompat.getInsetsController(window, decorView);

        mWindowInsetsControllerCompat.setSystemBarsBehavior(systemBarsBehaviour());
    }


    public void enterFullScreen() {
        if (null != mWindowInsetsControllerCompat)
            mWindowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars());
    }

    public void exitFullScreen() {
        if (null != mWindowInsetsControllerCompat)
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

    public enum ShowSystemBarsBehaviour {
        SHOW_BY_TOUCH,
        SHOW_BY_SWIPE,
        SHOW_TRANSIENT_BY_SWIPE
    }

    private int systemBarsBehaviour() {
        switch (mShowBarsBehaviour) {
            case SHOW_BY_TOUCH:
                return WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH;
            case SHOW_BY_SWIPE:
                return WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE;
            case SHOW_TRANSIENT_BY_SWIPE:
                return WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;
            default:
                throw new IllegalArgumentException("Неизвестное значение: "+mShowBarsBehaviour);
        }
    }
}
