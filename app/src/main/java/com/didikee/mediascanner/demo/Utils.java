package com.didikee.mediascanner.demo;

import android.content.res.AssetManager;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * user author: didikee
 * create time: 3/21/19 3:55 PM
 * description: 
 */
public class Utils {
//    private void copyAssets() {
//        AssetManager assetManager = getAssets();
//        String[] files = null;
//        try {
//            files = assetManager.list("");
//        } catch (IOException e) {
//            Log.e("tag", "Failed to get asset file list.", e);
//        }
//        if (files != null) {
//            for (String filename : files) {
//                InputStream in = null;
//                OutputStream out = null;
//                try {
//                    in = assetManager.open(filename);
//                    File outFile = new File(getExternalFilesDir(null), filename);
//                    out = new FileOutputStream(outFile);
//                    copyFile(in, out);
//                } catch (IOException e) {
//                    Log.e("tag", "Failed to copy asset file: " + filename, e);
//                } finally {
//                    if (in != null) {
//                        try {
//                            in.close();
//                        } catch (IOException e) {
//                            // NOOP
//                        }
//                    }
//                    if (out != null) {
//                        try {
//                            out.close();
//                        } catch (IOException e) {
//                            // NOOP
//                        }
//                    }
//                }
//            }
//        }
//    }

    public static void copyAssetFileTo(AssetManager assetManager, @NonNull String srcPath, @NonNull File outFile) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(srcPath);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
