package com.github.aakumykov.panorama_fragment;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
public class PanoramaFragment extends Fragment {

    private static final String FILE_URI_STRING = "FILE_URI_STRING";
    private static final String TAG = PanoramaFragment.class.getSimpleName();

    private FragmentPanoramaBinding mBinding;
    private PLManager mPlManager;
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();


    public static PanoramaFragment create(Uri fileURI) {

        PanoramaFragment panoramaFragment = new PanoramaFragment();

        if (null != fileURI) {
            Bundle bundle = new Bundle();
            bundle.putString(FILE_URI_STRING, fileURI.toString());
            panoramaFragment.setArguments(bundle);
        }

        return panoramaFragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentPanoramaBinding.inflate(inflater, container, false);

        mPlManager = new PLManager(requireContext());
        mPlManager.setContentView(mBinding.panoramaView);
        mPlManager.onCreate();

        return mBinding.getRoot();
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
    public void onStart() {
        super.onStart();
        processInputData();
    }

    @Override
    public void onPause() {
        mPlManager.onPause();
        super.onPause();
    }


    public boolean onTouchEvent(MotionEvent event) {
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
}