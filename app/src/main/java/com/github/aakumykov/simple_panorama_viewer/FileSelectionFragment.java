package com.github.aakumykov.simple_panorama_viewer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.aakumykov.simple_panorama_viewer.databinding.FragmentFileSelectionBinding;

public class FileSelectionFragment extends Fragment {

    private FragmentFileSelectionBinding mBinding;

    public static FileSelectionFragment create() {
        return new FileSelectionFragment();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentFileSelectionBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
