package com.didikee.mediascanner.demo;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.didikee.mediascanner.MediaScannerCompat;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.util.List;

/**这是几个比较典型的关于相册刷新的讨论
 * https://juejin.im/post/5ae0541df265da0b9d77e45a
 * https://zhuanlan.zhihu.com/p/46533159
 * https://zhuanlan.zhihu.com/p/25634793
 * https://blog.csdn.net/chendong_/article/details/52290329
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.bt_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertMediaToSDCard(false);
            }
        });

        findViewById(R.id.bt_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertMediaToSDCard(true);
            }
        });
    }


    private void insertMediaToSDCard(final boolean video) {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        new AsyncTask<Boolean, Void, String>() {
                            @Override
                            protected String doInBackground(Boolean... booleans) {
                                return copyAssetsToSDCard(booleans[0]);
                            }

                            @Override
                            protected void onPostExecute(String path) {
                                super.onPostExecute(path);
                                notifyGalleryUpdate(path);
                            }
                        }.execute(video);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        Toast.makeText(MainActivity.this, "权限被拒绝！", Toast.LENGTH_SHORT).show();
                    }
                }).start();

    }

    private void notifyGalleryUpdate(String mediaPath) {
        File mediaFile = new File(mediaPath);
        if (!mediaFile.exists()) {
            Toast.makeText(this, "文件不存在，可能复制文件失败了，请查看日志！", Toast.LENGTH_LONG).show();
            return;
        }
        //TODO 更新相册
        MediaScannerCompat.insert(this, mediaFile.getAbsolutePath(), new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(final String path, final Uri uri) {
                if (uri == null) {
                    Log.e(TAG, "Scan failed: " + path);
                } else {
                    Log.d(TAG, "Scan success: " + path);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String result = uri == null ? "失败" : "成功";
                        String title = "扫描结果：" + result;
                        StringBuilder msgBuilder = new StringBuilder();
                        msgBuilder.append("文件路径：" + path).append("\n\n");
                        msgBuilder.append("手机厂商：").append(Build.MANUFACTURER).append("\n");
                        msgBuilder.append("系统品牌：").append(Build.BRAND).append("\n");
                        msgBuilder.append("系统型号：").append(Build.BOARD).append("\n");
                        msgBuilder.append("系统版本：").append(Build.VERSION.RELEASE).append("\n");
                        msgBuilder.append("系统SDK：").append(Build.VERSION.SDK_INT).append("\n");
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(title)
                                .setMessage(msgBuilder.toString())
                                .setPositiveButton("确定", null)
                                .show();
                    }
                });

            }
        });
    }


    private String copyAssetsToSDCard(boolean video) {
        String srcPath;
        String filename;
        long currentTimeMillis = System.currentTimeMillis();
        if (video) {
            srcPath = "test_video.mp4";
            filename = currentTimeMillis + ".mp4";
        } else {
            srcPath = "test_photo.jpg";
            filename = currentTimeMillis + ".jpg";
        }

        File outFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), filename);

        Utils.copyAssetFileTo(getAssets(), srcPath, outFile);
        return outFile.getAbsolutePath();
    }
}
