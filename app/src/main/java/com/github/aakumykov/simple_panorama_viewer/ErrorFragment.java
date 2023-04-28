package com.github.aakumykov.simple_panorama_viewer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.github.aakumykov.simple_panorama_viewer.databinding.FragmentErrorBinding;

public class ErrorFragment extends Fragment {

    public static final String KEY_ERROR_MSG = "ERROR_MSG";
    private static final String TAG = ErrorFragment.class.getSimpleName();
    private FragmentErrorBinding mBinding;

    public static ErrorFragment create(String errorMsg) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ERROR_MSG, errorMsg);
        ErrorFragment fragment = new ErrorFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentErrorBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        displayError(getArguments());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }


    private void displayError(@Nullable Bundle arguments) {

        if (null == arguments) {
            showUnknownError("arguments is null");
            return;
        }

        final String errorMsg = arguments.getString(KEY_ERROR_MSG);
        if (null == errorMsg) {
            showUnknownError("errorMsg argument is null");
            return;
        }

        showError(errorMsg);
    }


    private void showUnknownError(String logMsg) {
        Log.e(TAG, logMsg);
        showError(R.string.ERROR_no_error_data);
    }

    private void showError(@StringRes int errorStringRes) {
        showError(getString(errorStringRes));
    }

    private void showError(String errorMsg) {
        mBinding.errorMessageView.setText(errorMsg);
    }
}
