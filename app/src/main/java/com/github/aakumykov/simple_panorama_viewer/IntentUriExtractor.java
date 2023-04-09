package com.github.aakumykov.simple_panorama_viewer;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

public class IntentUriExtractor {

    public static Uri getUriFromIntent(@Nullable Intent intent) throws IntentUriExtractorException {
        if (null == intent)
            throw new NoIntentException("Intent is null");

        // ==== Выделяю данные, похожие на URI из Intent ====
        // Первый способ
        Object imageUriObject = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        // Второй способ
        if (null == imageUriObject)
            imageUriObject = intent.getData();

        // Третий способ
        if (null == imageUriObject)
            imageUriObject = intent.getStringExtra(Intent.EXTRA_TEXT);

        if (null == imageUriObject)
            throw new NoUriInIntentException("Uri not found in Intent");


        // ==== Преобразую их в URI ====
        Uri resultURI;

        if (imageUriObject instanceof Uri) {
            resultURI = (Uri) imageUriObject;
        }
        else if (imageUriObject instanceof String) {
            try {
                resultURI = Uri.parse((String) imageUriObject);
            } catch (java.lang.Exception e) {
                throw new ParseErrorException(e);
            }
        }
        else {
            throw new IllegalIntentDataException("Data type in Intent cannot be converted to Uri");
        }

        return resultURI;
    }


    // Классы исключений
    public abstract static class IntentUriExtractorException extends java.lang.Exception {
        public IntentUriExtractorException(String message) {
            super(message);
        }

        public IntentUriExtractorException(Throwable cause) {
            super(cause);
        }
    }

    public static class NoIntentException extends IntentUriExtractorException {
        public NoIntentException(String message) {
            super(message);
        }
    }

    public static class NoUriInIntentException extends IntentUriExtractorException {
        public NoUriInIntentException(String message) {
            super(message);
        }
    }

    public static class IllegalIntentDataException extends IntentUriExtractorException {
        public IllegalIntentDataException(String message) {
            super(message);
        }
    }

    public static class ParseErrorException extends IntentUriExtractorException {
        public ParseErrorException(Throwable cause) {
            super(cause);
        }
    }
}
