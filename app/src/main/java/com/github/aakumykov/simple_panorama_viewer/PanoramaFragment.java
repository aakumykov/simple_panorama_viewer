package com.github.aakumykov.simple_panorama_viewer;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.aakumykov.simple_panorama_viewer.databinding.FragmentPanoramaBinding;
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils;
import com.panoramagl.PLICamera;
import com.panoramagl.PLImage;
import com.panoramagl.PLManager;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.utils.PLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import timber.log.Timber;

public class PanoramaFragment extends Fragment {

    private final static String KEY_FILE_URI_STRING = "FILE_URI";
    private static final String TAG = PanoramaFragment.class.getSimpleName();

    private FragmentPanoramaBinding mBinding;
    private final PanoramaViewer mPanoramaViewer = new PanoramaViewer();
    @Nullable private PLManager mPLManager;


    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentPanoramaBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        final Uri fileURI = arguments2fileURI(getArguments());
        if (null == fileURI) {
            showError(R.string.ERROR_no_input_data);
            return;
        }

        byte[] fileBytes = getImageFileBytes(fileURI);
        if (null == fileBytes) {
            showError(R.string.ERROR_no_data_readed_from_file);
            return;
        }

        mPanoramaViewer.displayPanorama(fileBytes);
    }

    private Uri arguments2fileURI(Bundle arguments) {
        try {
            return Uri.parse(arguments.getString(KEY_FILE_URI_STRING));
        } catch (NullPointerException e) {
            Timber.tag(TAG).e(ExceptionUtils.getErrorMessage(e));
            return null;
        }
    }

    private void showError(int errorStringRes) {
        Toast.makeText(requireContext(), getString(errorStringRes), Toast.LENGTH_SHORT).show();
    }

    @Nullable
    private byte[] getImageFileBytes(Uri imageFileURI) {

        final File file = new File(imageFileURI.getPath());

        final byte[] bytesArray;

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            bytesArray = new byte[fileInputStream.available()];
            fileInputStream.read(bytesArray);
        }
        catch (IOException e) {
            return null;
        }

        return bytesArray;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mPLManager)
            mPLManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mPLManager)
            mPLManager.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (null != mPLManager)
            mPLManager.onDestroy();

        mBinding = null;
    }

    private class PanoramaViewer {

        public void displayPanorama(byte[] fileBytes) {

            if (null == mPLManager)
                preparePanoramaManager();

            PLSphericalPanorama panorama = new PLSphericalPanorama();
            panorama.setImage(new PLImage(PLUtils.getBitmap(fileBytes), false));

            PLICamera camera = panorama.getCamera();
            camera.setZoomFactor(2f);
            camera.zoomIn(true);

            mPLManager.setPanorama(panorama);
        }

        private void preparePanoramaManager() {
            mPLManager = new PLManager(requireContext());
            mPLManager.setContentView(mBinding.panoramaView);
            mPLManager.setAcceleratedTouchScrollingEnabled(true);
            mPLManager.onCreate();
        }
    }

    public static Fragment create(@NonNull Uri fileURI) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_FILE_URI_STRING, fileURI.toString());

        PanoramaFragment panoramaFragment = new PanoramaFragment();
        panoramaFragment.setArguments(bundle);

        return panoramaFragment;
    }
}
