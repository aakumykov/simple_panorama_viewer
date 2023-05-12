package com.github.aakumykov.simple_panorama_viewer;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.aakumykov.panorama_fragment.PanoramaFragment;
import com.github.aakumykov.simple_panorama_viewer.databinding.ActivityMainBinding;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    public static final int CODE_OPEN_IMAGE = 10;
    private static final String TAG = MainActivity.class.getSimpleName();
    private FragmentManager mFragmentManager;
    private FragmentManager.FragmentLifecycleCallbacks mFragmentLifecycleCallbacks;
    @Nullable private PanoramaFragment mPanoramaFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFragmentManager = getSupportFragmentManager();

        mFragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentDetached(fm, f);
                if (f instanceof PanoramaFragment)
                    mPanoramaFragment = null;
            }
        };

        mFragmentManager.registerFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks, false);

        mFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainerView, StartFragment.create(), null)
                .setReorderingAllowed(true)
                .commit();

        mFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, StartFragment.create(), null)
                .commit();

        askForPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragmentManager.unregisterFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (CODE_OPEN_IMAGE == requestCode)
            if (RESULT_OK == resultCode)
                processInputIntent(data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processInputIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != mPanoramaFragment)
            return mPanoramaFragment.onTouchEvent(event);
        else
            return super.onTouchEvent(event);
    }

    private void processInputIntent(Intent intent) {

        mPanoramaFragment = PanoramaFragment.create(intent);

        mFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainerView, mPanoramaFragment, null)
                .addToBackStack(null)
                .setReorderingAllowed(true)
                .commit();
    }


    private void askForPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            MainActivityPermissionsDispatcher.startWorkOldWithPermissionCheck(this);
        else
            MainActivityPermissionsDispatcher.startWorkNewWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE})
    void startWorkOld() {
        processInputIntent(getIntent());
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({Manifest.permission.READ_MEDIA_IMAGES})
    void startWorkNew() {
        processInputIntent(getIntent());
    }

}
