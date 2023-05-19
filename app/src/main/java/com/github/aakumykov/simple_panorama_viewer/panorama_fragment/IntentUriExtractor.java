package com.github.aakumykov.simple_panorama_viewer.panorama_fragment;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IntentUriExtractor {

    @Nullable
    public static Uri getUri(final @Nullable Intent intent, final String logTag) {

        if (null == intent) {
            w(logTag, "Intent is null");
            return null;
        }

        if (null != intent.getData())
            return intent.getData();
        else
            return getUriFromClipData(intent, logTag);
    }

    @Nullable
    private static Uri getUriFromClipData(final @NonNull Intent intent, final String logTag) {

        final ClipData clipData = intent.getClipData();
        if (null == clipData) {
            w(logTag, "ClipData is null");
            return null;
        }

        final int itemsCount = clipData.getItemCount();
        if (0 == itemsCount) {
            Log.w(logTag, "Intent's ClipData has no items.");
            return null;
        }

        if (itemsCount > 1)
            w(logTag, "Multiple items " + itemsCount + "in Intent's ClipData.");

        final ClipData.Item item = clipData.getItemAt(0);
        if (null == item) {
            Log.w(logTag, "ClipData.Item from Intent is null.");
            return null;
        }

        final Uri uri = item.getUri();
        if (null == uri) {
            Log.w(logTag, "Uri from ClipData.Item is null.");
            return null;
        }

        return uri;
    }

    private static void w(String logTag, String warningMsg) {
        Log.w(logTag, warningMsg);
    }
}

