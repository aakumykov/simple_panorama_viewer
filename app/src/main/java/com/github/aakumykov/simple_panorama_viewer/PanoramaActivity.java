package com.github.aakumykov.simple_panorama_viewer;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.aakumykov.simple_panorama_viewer.databinding.ActivityPanoramaBinding;
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;
import com.panoramagl.PLICamera;
import com.panoramagl.PLImage;
import com.panoramagl.PLManager;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.utils.PLUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class PanoramaActivity extends AppCompatActivity {

    private static final String TAG = PanoramaActivity.class.getSimpleName();
    private ActivityPanoramaBinding mBinding;
    private PLManager mPLManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityPanoramaBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.errorActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitApp();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exitApp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PanoramaActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != mPLManager)
            return mPLManager.onTouchEvent(event);
        else
            return super.onTouchEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PanoramaActivityPermissionsDispatcher.processInputIntentWithPermissionCheck(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mPLManager)
            mPLManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mPLManager)
            mPLManager.onPause();
    }

    @Override
    protected void onDestroy() {
        releasePLManager();
        super.onDestroy();
    }



    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void processInputIntent() {

        final Intent intent = getIntent();
        final ClipData clipData = intent.getClipData();

        if (null == clipData) {
            nothingToShowError();
            return;
        }

        if (0 == clipData.getItemCount()) {
            nothingToShowError();
            return;
        }

        final Uri dataUri = /*intent.getData()*/
        intent.getClipData().getItemAt(0).getUri();

        if (null == dataUri) {
            nothingToShowError();
            return;
        }

        try (ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(dataUri, "r")) {
            final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            final byte[] bytesArray;
            try (FileInputStream fileInputStream = new FileInputStream(fileDescriptor)) {
                bytesArray = new byte[fileInputStream.available()];
                fileInputStream.read(bytesArray);

                preparePanoramaManager();
                hideToolbar();
                showPanoramicImage(bytesArray);
                showView(mBinding.panoramaView);
            }
        }
        catch (FileNotFoundException e) {
            displayError(new Exception(getString(R.string.error_file_not_found, dataUri.toString())));
        }
        catch (IOException e) {
            displayError(new Exception(getString(R.string.error_reading_file, dataUri.toString())));
        }
    }

    private void displayPanorama(@NonNull Uri imageUri) {
        preparePanoramaManager();
        hideError();
        hideToolbar();
        showPanoramicImage(imageUri);
    }

    private void showPanoramicImage(@NonNull Uri uri) {

        final File imageFile = new File(uri.getPath());

        try {
            byte[] imageBytes = file2bytes(imageFile);
            showPanoramicImage(imageBytes);
        }
        catch (FileNotFoundException e) {
            displayError(new Exception(getString(R.string.error_file_not_found, imageFile.getAbsolutePath())));
        }
        catch (IOException e) {
            displayError(e);
        }
    }

    private void showPanoramicImage(byte[] bytes) {
        PLSphericalPanorama panorama = new PLSphericalPanorama();
        panorama.setImage(new PLImage(PLUtils.getBitmap(bytes), false));

        final PLICamera pliCamera = panorama.getCamera();
        pliCamera.setZoomFactor(2f);
        pliCamera.zoomIn(true);

        mPLManager.setPanorama(panorama);
    }

    private void preparePanoramaManager() {
        mPLManager = new PLManager(this);
        mPLManager.setContentView(mBinding.panoramaView);
        mPLManager.onCreate();
    }




    private byte[] file2bytes(File file) throws IOException {
        final byte[] bytesArray;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            bytesArray = new byte[fileInputStream.available()];
            fileInputStream.read(bytesArray);
        }
        return bytesArray;
    }


    private void displayError(Exception e) {
        hidePanorama();
        showError(e);
    }

    private void showError(Exception e) {
        mBinding.errorMessageView.setText(ExceptionUtils.getErrorMessage(e));
        Log.e(TAG, ExceptionUtils.getErrorMessage(e), e);
        showView(mBinding.errorGroup);
    }

    private void hideError() {
        hideView(mBinding.errorGroup);
    }

    private void hidePanorama() {
        releasePLManager();
        hideView(mBinding.panoramaView);
    }


    private void showView(View view) {
        if (null != view)
            view.setVisibility(View.VISIBLE);
    }

    private void hideView(View view) {
        if (null != view)
            view.setVisibility(View.GONE);
    }

    private void releasePLManager() {
        if (null != mPLManager)
            mPLManager.onDestroy();
    }

    private void exitApp() {
        finish();
    }

    private void hideToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar)
            actionBar.hide();
    }

    private void nothingToShowError() {
        displayError(new Exception(getString(R.string.error_nothing_to_display)));
    }
}