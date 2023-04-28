package com.github.aakumykov.simple_panorama_viewer;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.aakumykov.simple_panorama_viewer.databinding.ActivityMainBinding;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private FragmentManager mFragmentManager;
    @Nullable private IntentWrapper mIntentProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mFragmentManager = getSupportFragmentManager();

        startToWork(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        startToWork(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // TODO: unpause pano view
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO: pause panoramic view
    }



    public void startToWork(@Nullable Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            MainActivityPermissionsDispatcher.startToWorkNewWithPermissionCheck(this, intent);
        else
            MainActivityPermissionsDispatcher.startToWorkOldWithPermissionCheck(this, intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({Manifest.permission.READ_MEDIA_IMAGES})
    void startToWorkNew(@Nullable Intent intent) {
        processInputIntent(intent);
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE})
    void startToWorkOld(@Nullable Intent intent) {
        processInputIntent(intent);
    }

    
    private void processInputIntent(@Nullable Intent intent) {

        IntentWrapper intentWrapper = new IntentWrapper(intent);

        if (intentWrapper.hasError()) {
            showErrorFragment(intentWrapper.getError());
            return;
        }

        if (intentWrapper.hasData())
            showPanoramaFragment(intentWrapper.getDataURI());
        else
            showFileSelectionFragment();
    }

    private void showPanoramaFragment(Uri fileURI) {
        setFragment(PanoramaFragment.create(fileURI));
    }

    private void showFileSelectionFragment() {
        setFragment(FileSelectionFragment.create());
    }


    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE})
//    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE})
    void showNoPermissionErrorOld() {
        showErrorFragment("Отсутствует разрешение READ_EXTERNAL_STORAGE");
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnPermissionDenied({Manifest.permission.READ_MEDIA_IMAGES})
    void showNoPermissionErrorNew() {
        showErrorFragment("Отсутствует разрешение READ_MEDIA_IMAGES");
    }

    private void showErrorFragment(@StringRes int errorMsgStringRes) {
        showErrorFragment(getString(errorMsgStringRes));
    }

    private void showErrorFragment(String errorMsg) {
        mFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, ErrorFragment.create(errorMsg), null)
                .commit();
    }

    private void setFragment(Fragment fragment) {
        mFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment, null)
                .commit();
    }
}