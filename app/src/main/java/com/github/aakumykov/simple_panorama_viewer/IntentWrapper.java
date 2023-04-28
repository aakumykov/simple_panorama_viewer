package com.github.aakumykov.simple_panorama_viewer;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

class IntentWrapper {

    @Nullable private final Intent mIntent;
    @Nullable private String mErrorMsg;
    @Nullable private Uri mDataURI;

    public IntentWrapper(@Nullable Intent intent) {
        mIntent = intent;
        processDataFromIntent();
    }


    public boolean hasData() {
        return null != mDataURI;
    }


    private void setError(String errorText) {
        mErrorMsg = errorText;
    }

    @Nullable
    public String getError() {
        return mErrorMsg;
    }


    @Nullable
    public Uri getDataURI() {
        return mDataURI;
    }


    private void processDataFromIntent() {
        if (null == mIntent) {
            setError("Intent is null");
            return;
        }

        final ClipData clipData = mIntent.getClipData();
        if (null == clipData) {
            setError("ClipData from Intent is null");
            return;
        }

        if (0 == clipData.getItemCount()) {
            setError("There is no items in ClipData");
            return;
        }

        mDataURI = clipData.getItemAt(0).getUri();
        if (null == mDataURI) {
            setError("Data URI is null");
            return;
        }
    }
}
