package com.github.aakumykov.simple_panorama_viewer.panorama_fragment;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUriBytesReader {

    public static byte[] readBytes(@NonNull ContentResolver contentResolver,
                                   Uri fileUri) throws IOException {

        try (ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(fileUri, "r")) {

            final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            final byte[] bytesArray;

            try (FileInputStream fileInputStream = new FileInputStream(fileDescriptor)) {
                bytesArray = new byte[fileInputStream.available()];
                fileInputStream.read(bytesArray);
                return bytesArray;
            }
        }
    }
}

