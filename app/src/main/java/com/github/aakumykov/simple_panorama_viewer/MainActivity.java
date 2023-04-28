package com.github.aakumykov.simple_panorama_viewer;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.github.aakumykov.simple_panorama_viewer.databinding.ActivityMainBinding;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mFragmentManager = getSupportFragmentManager();

        /*getSupportFragmentManager().beginTransaction()
                .add(PanoramaFragment.create())
                .commit();*/

//        askForPermissions();
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



    public void askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            MainActivityPermissionsDispatcher.askFileReadingNewWithPermissionCheck(this);
        else
            MainActivityPermissionsDispatcher.askFileReadingOldWithPermissionCheck(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({Manifest.permission.READ_MEDIA_IMAGES})
    void askFileReadingNew() {}

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE})
    void askFileReadingOld() {}



    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE})
    void showNoPermissionErrorOld() {
        showNoPermissionFragment("Отсутствует разрешение READ_EXTERNAL_STORAGE");
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @OnPermissionDenied({Manifest.permission.READ_MEDIA_IMAGES})
    void showNoPermissionErrorNew() {
        showNoPermissionFragment("Отсутствует разрешение READ_MEDIA_IMAGES");
    }

    private void showNoPermissionFragment(String errorMsg) {
        mFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, ErrorFragment.create(errorMsg), null)
                .commit();
    }

}