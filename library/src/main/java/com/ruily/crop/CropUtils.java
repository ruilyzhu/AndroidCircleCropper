package com.ruily.crop;

import android.support.annotation.Nullable;

import java.io.Closeable;

/**
 * Created by Ruily on 16/1/4.
 */
public class CropUtils {
    public static void closeSilently(@Nullable Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // Do nothing
        }
    }
}
