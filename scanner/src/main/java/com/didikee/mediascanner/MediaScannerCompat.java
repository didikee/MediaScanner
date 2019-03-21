package com.didikee.mediascanner;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.util.ArrayList;

/**
 * user author: didikee
 * create time: 3/21/19 4:13 PM
 * description:
 * 通过操作 MediaStore 类。
 * 发送广播更新 MediaStore。
 * 通过操作 MediaScannerConnection 类。
 */
public final class MediaScannerCompat {
    private static final String TAG = "MediaScannerCompat";
    private static final String IMAGE = "image";
    private static final String VIDEO = "video";

    /**
     * https://stackoverflow.com/questions/8589645/how-to-determine-mime-type-of-file-in-android
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getMimeType(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }

        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            mimeType = getMimeType(uri.toString());
        }
        return mimeType;
    }

    public static String getMimeType(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
    }

    // MediaScanner ///////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////

    /**
     * 通知相册有新的媒体资源插入
     * 接收端为android sdk 中的 MediaScannerReceiver
     * (http://androidxref.com/6.0.0_r1/xref/packages/providers/MediaProvider/src/com/android/providers/media/MediaScannerReceiver.java)
     *
     * 注意：你的文件路径必须在{@link android.os.Environment#getExternalStorageDirectory()} 下
     *      不然无法插入
     * @param context context
     * @param uri 媒体文件（video/* 或者 image/*）
     */
    public static void insertByBroadcast(Context context, Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        context.sendBroadcast(intent);
    }

    /**
     * 删除在相册中存在的文件
     * 这个删除操作，不光包含java 的io 删除操作
     * 还包含了 通知android相册更新的通知
     * 所以，android中推荐使用这种方式删除图片和视频
     * @param context
     * @param mediaPath
     */
    public static boolean delete(Context context, String mediaPath) {
        if (context == null || TextUtils.isEmpty(mediaPath)) {
            return false;
        }
        String mimeType = getMimeType(mediaPath);
        if (!TextUtils.isEmpty(mimeType)) {
            String lowerCase = mimeType.toLowerCase();
            if (lowerCase.startsWith(IMAGE)) {
                // picture
                int delete = context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.Media.DATA + "=?",
                        new String[]{mediaPath});
                return delete != -1;
            } else if (lowerCase.startsWith(VIDEO)) {
                // video
                int delete = context.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Video.Media.DATA + "=?",
                        new String[]{mediaPath});
                return delete != -1;
            } else {
                Log.d(TAG, "mime type: " + mimeType);
                return false;
            }
        }
        return false;
    }

    public static void insert(Context context, final String[] medias) {
        insert(context, medias, ON_SCAN_COMPLETED_LISTENER);
    }

    public static void insert(Context context, final String[] medias, MediaScannerConnection.OnScanCompletedListener onScanCompletedListener) {
        if (context == null || medias == null) {
            return;
        }
        ArrayList<String> paths = new ArrayList<>();
        ArrayList<String> mimeTypes = new ArrayList<>();
        for (int i = 0; i < medias.length; i++) {
            String media = medias[i];
            if (!TextUtils.isEmpty(media)) {
                String mimeType = getMimeType(media);
                Log.d(TAG, "mimeType: " + mimeType);
                if (!TextUtils.isEmpty(mimeType)) {
                    paths.add(media);
                    mimeTypes.add(mimeType);
                }
            }
        }
        if (paths.size() == mimeTypes.size()) {
            MediaScannerConnection.scanFile(context,
                    paths.toArray(new String[paths.size()]),
                    mimeTypes.toArray(new String[mimeTypes.size()]),
                    onScanCompletedListener
            );
        } else {
            Log.d(TAG, "出现错误，无法执行操作");
        }
    }

    /**
     * 插入单个文件
     * @param context
     * @param mediaPath
     */
    public static void insert(Context context, final String mediaPath) {
        insert(context, mediaPath, ON_SCAN_COMPLETED_LISTENER);
    }

    public static void insert(Context context, final String mediaPath, MediaScannerConnection.OnScanCompletedListener onScanCompletedListener) {
        if (context == null || TextUtils.isEmpty(mediaPath)) {
            return;
        }
        String mimeType = getMimeType(mediaPath);
        MediaScannerConnection.scanFile(context, new String[]{mediaPath}, new String[]{mimeType}, onScanCompletedListener);
    }

    private static MediaScannerConnection.OnScanCompletedListener ON_SCAN_COMPLETED_LISTENER = new MediaScannerConnection.OnScanCompletedListener() {
        @Override
        public void onScanCompleted(String path, Uri uri) {
            if (uri == null) {
                Log.e(TAG, "Scan failed: " + path);
            } else {
                Log.d(TAG, "Scan success: " + path);
            }
        }
    };

}
