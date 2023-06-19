package com.github.aakumykov.simple_panorama_viewer.panorama_fragment;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BundleReader {

    @Nullable
    public static Uri getUri(Bundle bundle, @NonNull String key) throws IllegalArgumentException {

        if (null == bundle)
            throw new IllegalArgumentException("Arguments bundle is null");

        String uriString = bundle.getString(key);

        if (null == uriString)
            throw new IllegalArgumentException("Uri string from arguments is null");

        return Uri.parse(uriString);
    }
}
