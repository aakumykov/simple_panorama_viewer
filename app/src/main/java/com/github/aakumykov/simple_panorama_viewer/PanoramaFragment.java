package com.github.aakumykov.simple_panorama_viewer;

import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import timber.log.Timber;

public class PanoramaFragment extends Fragment {

    private final static String KEY_FILE_URI_STRING = "FILE_URI";
    private static final String TAG = PanoramaFragment.class.getSimpleName();

    private final PanoramaViewer mPanoramaViewer = new PanoramaViewer();
    private final ArgumentsReader mArgumentsReader = new ArgumentsReader();
    private final MyFileReader mFileReader = new MyFileReader();

    private FragmentPanoramaBinding mBinding;
    @Nullable private PLManager mPLManager;


    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentPanoramaBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        final Uri fileURI = mArgumentsReader.fileURI();
        byte[] fileBytes = mFileReader.getBytesFromFile(fileURI);
        mPanoramaViewer.displayPanorama(fileBytes);
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


    private void showDataError() {
        showError(R.string.ERROR_data_error);
    }

    private void showFileReadingError() {
        showError(R.string.ERROR_reading_file);
    }

    private void showError(int errorStringRes) {
        Toast.makeText(requireContext(), getString(errorStringRes), Toast.LENGTH_SHORT).show();
    }

    private void logError(String errorMsg) {
        Timber.tag(TAG).e(errorMsg);
    }

    private void logError(Exception e) {
        logError(ExceptionUtils.getErrorMessage(e));
    }


    public static PanoramaFragment create(@NonNull Uri fileURI) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_FILE_URI_STRING, fileURI.toString());

        PanoramaFragment panoramaFragment = new PanoramaFragment();
        panoramaFragment.setArguments(bundle);

        return panoramaFragment;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (null != mPLManager)
            return mPLManager.onTouchEvent(event);
        return false;
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

    private class ArgumentsReader {

        @Nullable
        public Uri fileURI() {

            Bundle arguments = getArguments();
            if (null == arguments) {
                logError("Arguments bundle is null");
                showDataError();
                return null;
            }

            String uriString = arguments.getString(KEY_FILE_URI_STRING);
            if (null == uriString) {
                logError("Uri string from arguments is null");
                showDataError();
                return null;
            }

            return Uri.parse(uriString);
        }
    }

    private class MyFileReader {

        public byte[] getBytesFromFile(Uri fileURI) {

            try (ParcelFileDescriptor parcelFileDescriptor = requireContext().getContentResolver().openFileDescriptor(fileURI, "r")) {

                final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                final byte[] bytesArray;

                try (FileInputStream fileInputStream = new FileInputStream(fileDescriptor)) {
                    bytesArray = new byte[fileInputStream.available()];
                    fileInputStream.read(bytesArray);
                    return bytesArray;
                }
            }
            catch (IOException e) {
                logError(e);
                showFileReadingError();
            }

            return null;
        }
    }

}
