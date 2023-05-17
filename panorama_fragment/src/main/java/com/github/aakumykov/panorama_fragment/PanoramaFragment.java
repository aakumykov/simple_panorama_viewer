package com.github.aakumykov.panorama_fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

import com.github.aakumykov.panorama_fragment.databinding.FragmentPanoramaBinding;
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;
import com.panoramagl.PLImage;
import com.panoramagl.PLManager;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.utils.PLUtils;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Фрагмент или показывает панораму или выдаёт toast-сообщение об ошибке и закрывает сам себя.
 */
public class PanoramaFragment extends Fragment implements FullscreenController.Callback {

    public static final int CODE_OPEN_IMAGE = 10;
    private static final String FILE_URI_STRING = "FILE_URI_STRING";
    private static final String TAG = PanoramaFragment.class.getSimpleName();
    private static final String USER_INTERFACE_VISIBLE = "USER_INTERFACE_VISIBLE";

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private FragmentPanoramaBinding mBinding;
    private PLManager mPlManager;
    private FullscreenController mFullscreenController;
    private boolean mUserInterfaceVisible = false;

    private GestureDetectorCompat mGestureDetectorCompat;

    public static PanoramaFragment create(Uri fileURI) {

        PanoramaFragment panoramaFragment = new PanoramaFragment();

        if (null != fileURI) {
            Bundle bundle = new Bundle();
            bundle.putString(FILE_URI_STRING, fileURI.toString());
            panoramaFragment.setArguments(bundle);
        }

        return panoramaFragment;
    }

    public static Intent openImageIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        return intent;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentPanoramaBinding.inflate(inflater, container, false);

        mGestureDetectorCompat = new GestureDetectorCompat(requireContext(), new CustomGestureListener());

        mBinding.toggleFullscreenIcon.setOnClickListener(v -> mFullscreenController.enterFullScreen());
        mBinding.exitButton.setOnClickListener(v -> exitApp());
        mBinding.openButton.setOnClickListener(v -> openFile());

        mPlManager = new PLManager(requireContext());
        mPlManager.setContentView(mBinding.panoramaView);
        mPlManager.onCreate();

        mFullscreenController = new FullscreenController(requireActivity(), FullscreenController.SHOW_TRANSIENT_BARS_BY_SWIPE);
        mFullscreenController.setCallback(this);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        processInputData();
//        mFullscreenController.enterFullScreen();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(USER_INTERFACE_VISIBLE, mUserInterfaceVisible);
    }

    @Override
    public void onDestroyView() {
        mCompositeDisposable.dispose();
        mPlManager.onDestroy();
        mBinding = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPlManager.onResume();
    }

    @Override
    public void onPause() {
        mPlManager.onPause();
        super.onPause();
    }


    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetectorCompat.onTouchEvent(event))
            return true;
        return mPlManager.onTouchEvent(event);
    }


    private void processInputData() {

        final DisposableObserver<byte[]> disposableObserver =

                Observable.fromCallable(new Callable<byte[]>() {
                    @Override
                    public byte[] call() throws Exception {
                        return FileUriBytesReader.readBytes(
                                requireContext().getContentResolver(),
                                BundleReader.getArgument(getArguments(), FILE_URI_STRING));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

            .subscribeWith(new DisposableObserver<byte[]>() {
                @Override
                public void onNext(byte[] bytes) {
                    displayPanorama(bytes);
                }

                @Override
                public void onError(Throwable e) {
                    showErrorAndExit(e);
                }

                @Override
                public void onComplete() {

                }
            });

        mCompositeDisposable.add(disposableObserver);
    }


    private void displayPanorama(byte[] fileBytes) {

        PLImage plImage = new PLImage(PLUtils.getBitmap(fileBytes));

        PLSphericalPanorama panorama = new PLSphericalPanorama();
        panorama.getCamera().lookAt(30.0f, 90.0f);

        panorama.setImage(plImage);

        mPlManager.setPanorama(panorama);
    }

    @SuppressLint("LogNotTimber")
    private void showErrorAndExit(Throwable throwable) {
        final String errorMsg = ExceptionUtils.getErrorMessage(throwable);
        Toast.makeText(requireContext(), getString(R.string.ERROR_error, errorMsg), Toast.LENGTH_SHORT).show();
        Log.e(TAG, errorMsg);
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onEnterFullScreen() {
//        mBinding.rootView.setOnClickListener(v -> mFullscreenController.exitFullScreen());
        mBinding.toggleFullscreenIcon.setOnClickListener(v -> mFullscreenController.exitFullScreen());
        mBinding.toggleFullscreenIcon.setImageResource(R.drawable.ic_baseline_fullscreen_exit_24);
        hideUserInterface();
    }

    @Override
    public void onExitFullScreen() {
//        mBinding.rootView.setOnClickListener(v -> mFullscreenController.enterFullScreen());
        mBinding.toggleFullscreenIcon.setOnClickListener(v -> mFullscreenController.enterFullScreen());
        mBinding.toggleFullscreenIcon.setImageResource(R.drawable.ic_baseline_fullscreen_24);
        showUserInterface();
    }


    private void toggleUserInterface() {
        if (mUserInterfaceVisible)
            hideUserInterface();
        else
            showUserInterface();
    }

    private void exitApp() {
        requireActivity().finish();
    }

    private void openFile() {
        requireActivity().startActivityForResult(openImageIntent(), CODE_OPEN_IMAGE);
    }

    private void toggleFullscreen() {
        if (mFullscreenController.isFullScreen())
            mFullscreenController.exitFullScreen();
        else
            mFullscreenController.enterFullScreen();
    }


    private void showUserInterface() {
        showView(mBinding.openButton);
        showView(mBinding.exitButton);
        showView(mBinding.toggleFullscreenIcon);

        mUserInterfaceVisible = true;
    }

    private void hideUserInterface() {
        hideView(mBinding.openButton);
        hideView(mBinding.exitButton);
        hideView(mBinding.toggleFullscreenIcon);

        mUserInterfaceVisible = false;
    }

    private void showView(View view) {
        view.setVisibility(View.VISIBLE);
    }

    private void hideView(View view) {
        view.setVisibility(View.GONE);
    }


    public class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            if (mFullscreenController.isFullScreen())
                mFullscreenController.exitFullScreen();
            else
                mFullscreenController.enterFullScreen();
            return true;
        }
    }
}