package com.github.aakumykov.simple_panorama_viewer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class ErrorFragment extends Fragment {

    public static final String KEY_ERROR_MSG = "ERROR_MSG";

    public static ErrorFragment create(String errorMsg) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ERROR_MSG, errorMsg);
        ErrorFragment fragment = new ErrorFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
