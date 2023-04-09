package com.github.aakumykov.simple_panorama_viewer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.aakumykov.simple_panorama_viewer.databinding.ActivityPanoramaBinding;
import com.panoramagl.PLManager;

public class PanoramaActivity extends AppCompatActivity {

    private ActivityPanoramaBinding mBinding;
    private PLManager mPLManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mBinding = ActivityPanoramaBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        preparePanoramaManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayPanorama();
    }

    private void displayPanorama() {
        final Intent intent = getIntent();
        
    }

    private void preparePanoramaManager() {
        mPLManager = new PLManager(this);
        mPLManager.setContentView(mBinding.panoramaView);
        mPLManager.onCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPLManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPLManager.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPLManager.onDestroy();
    }
}