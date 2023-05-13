package com.github.aakumykov.simple_panorama_viewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.aakumykov.simple_panorama_viewer.databinding.FragmentStartBinding;

public class StartFragment extends Fragment implements HasCustomTitle {

    public static StartFragment create() {
        return new StartFragment();
    }

    private FragmentStartBinding mBinding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentStartBinding.inflate(inflater, container, false);
        mBinding.textView.setOnClickListener(this::onSelectPanoramaClicked);
        mBinding.imageView.setOnClickListener(this::onSelectPanoramaClicked);
        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void onSelectPanoramaClicked(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        requireActivity().startActivityForResult(intent, MainActivity.CODE_OPEN_IMAGE);
    }

    @Override
    public int getTitle() {
        return R.string.app_name;
    }
}
