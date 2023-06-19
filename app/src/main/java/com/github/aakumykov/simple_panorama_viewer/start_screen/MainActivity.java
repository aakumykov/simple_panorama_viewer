package com.github.aakumykov.simple_panorama_viewer.start_screen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.aakumykov.simple_panorama_viewer.PanoramaFragmentGVR;
import com.github.aakumykov.simple_panorama_viewer.R;
import com.github.aakumykov.simple_panorama_viewer.databinding.ActivityMainBinding;
import com.github.aakumykov.simple_panorama_viewer.panorama_fragment.IntentUriExtractor;
import com.github.aakumykov.simple_panorama_viewer.panorama_fragment.PanoramaFragment;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private FragmentManager mFragmentManager;
    private FragmentManager.FragmentLifecycleCallbacks mFragmentLifecycleCallbacks;
    @Nullable private PanoramaFragmentGVR mPanoramaFragmentGVR;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFragmentManager = getSupportFragmentManager();

        mFragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
                if (f instanceof HasCustomTitle)
                    showTitleBar(((HasCustomTitle) f).getTitle());
                else
                    hideTitleBar();
            }

            @Override
            public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentDetached(fm, f);
                // Обуляю поле mPanoramaFragment, только если открывается начальный экран (не панорамный).
//                if (!(f instanceof PanoramaFragment)/* && newFragmentIsStartingFragment()*/)
//                    mPanoramaFragment = null;
            }
        };

        mFragmentManager.registerFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks, false);

//        askForPermissions();
        processInputIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragmentManager.unregisterFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (PanoramaFragment.CODE_OPEN_IMAGE == requestCode)
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
        /*if (null != mPanoramaFragmentGVR)
            return mPanoramaFragmentGVR.onTouchEvent(event);
        else*/
            return super.onTouchEvent(event);
    }

    private void processInputIntent(Intent intent) {

        final Uri fileUri = IntentUriExtractor.getUri(intent, TAG);

        if (null != fileUri) {
            mPanoramaFragmentGVR = PanoramaFragmentGVR.Companion.create(fileUri);
            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, mPanoramaFragmentGVR, null)
                    .commit();
        }
        else {
            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, StartFragment.create(), null)
                    .commit();
        }
    }


    private void showTitleBar(@StringRes int title) {
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setTitle(title);
            actionBar.show();
        }
    }

    private void hideTitleBar() {
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar)
            actionBar.hide();
    }


    public void selectImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            MainActivityPermissionsDispatcher.selectImageOldWithPermissionCheck(this);
        else
            MainActivityPermissionsDispatcher.selectImageNewWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE})
    void selectImageOld() {
        openFileSelectionDialog();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @NeedsPermission({Manifest.permission.READ_MEDIA_IMAGES})
    void selectImageNew() {
        openFileSelectionDialog();
    }

    private void openFileSelectionDialog() {
        startActivityForResult(PanoramaFragment.openImageIntent(), PanoramaFragment.CODE_OPEN_IMAGE);
    }
}
