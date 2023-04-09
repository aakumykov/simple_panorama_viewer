package com.github.aakumykov.simple_panorama_viewer;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;
import com.panoramagl.PLICamera;
import com.panoramagl.PLImage;
import com.panoramagl.PLManager;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.utils.PLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PanoViewer {

    private static final String TAG = PanoViewer.class.getSimpleName();
    private final PLManager mPlManager;
    @Nullable private Callback mCallback;

    public PanoViewer(@NonNull Context context) {
        mPlManager = new PLManager(context);
//        plManager.activateOrientation();
        mPlManager.setAcceleratedTouchScrollingEnabled(false);
        mPlManager.setAccelerometerEnabled(true);
        mPlManager.setResetEnabled(false);
    }


    public void setTargetView(@NonNull ViewGroup viewGroup) {
        mPlManager.setContentView(viewGroup);
        mPlManager.onCreate();
    }

    public void displayImage(File imageFile) {

        PLSphericalPanorama panorama = new PLSphericalPanorama();

        final PLICamera camera = panorama.getCamera();
        camera.lookAt(0.0f, 0.0f);
        camera.setZoomFactor(2f);
        camera.zoomIn(true);

        try {
            final byte[] bytesArray = file2bytes(imageFile);
            panorama.setImage(new PLImage(PLUtils.getBitmap(bytesArray), false));
            mPlManager.setPanorama(panorama);
        }
        catch (IOException e) {
            Log.e(TAG, ExceptionUtils.getErrorMessage(e), e);
            if (null != mCallback)
                mCallback.onError(ExceptionUtils.getErrorMessage(e));
        }
    }


    public void setCallback(@NonNull Callback callback) {
        mCallback = callback;
    }

    public void unsetCallback() {
        mCallback = null;
    }


    public void release() {
        mPlManager.onDestroy();
    }

    public void pause() {
        mPlManager.onPause();
    }

    public void resume() {
        mPlManager.onResume();
    }

    /**
     * Нужно вызывать в Activity.onTouchEvent(...), чтобы картинка реагировала на прокрутку пальцами.
     * @param event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        return mPlManager.onTouchEvent(event);
    }


    private byte[] file2bytes(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        final byte[] bytesArray = new byte[fileInputStream.available()];
        fileInputStream.read(bytesArray);
        return bytesArray;
    }


    public interface Callback {
        default void onError(String errorMsg) {}
    }
}
