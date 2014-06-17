package com.src.zhang.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by levin on 6/13/14.
 */
public class UIHelper {

    /**
     * 发送App异常崩溃报告
     *
     * @param cont
     * @param crashReport
     */
    public static void sendAppCrashReport(final Context cont,
                                          final String crashReport) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.app_error);
        builder.setMessage(R.string.app_error_message);
        builder.setPositiveButton(R.string.submit_report,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 发送异常报告
                        Intent i = new Intent(Intent.ACTION_SEND);
                        // i.setType("text/plain"); //模拟器
                        i.setType("message/rfc822"); // 真机
                        i.putExtra(Intent.EXTRA_EMAIL,
                                new String[] { "badstyle@qq.com" });
                        i.putExtra(Intent.EXTRA_SUBJECT,
                                "赤壁乱舞客户端 - 错误报告");
                        i.putExtra(Intent.EXTRA_TEXT, crashReport);
                        cont.startActivity(Intent.createChooser(i, "发送错误报告"));
                        // 退出
                        AppManager.getAppManager().AppExit(cont);
                    }
                });
        builder.setNegativeButton(R.string.sure,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 退出
                        AppManager.getAppManager().AppExit(cont);
                    }
                });
        builder.show();
    }

    /**
     * 加载显示图片
     *
     * @param imgView
     * @param imgURL
     * @param errMsg
     */
    public static void showLoadImage(final ImageView imgView,
                                     final String imgURL, final String errMsg) {
        // 读取本地图片
        if (StringUtils.isEmpty(imgURL) || imgURL.endsWith("portrait.gif")) {
            Bitmap bmp = BitmapFactory.decodeResource(imgView.getResources(),
                    R.drawable.ic_launcher);
            imgView.setImageBitmap(bmp);
            return;
        }

        // 是否有缓存图片
        final String filename = FileUtils.getFileName(imgURL);
        // Environment.getExternalStorageDirectory();返回/sdcard
        String filepath = imgView.getContext().getFilesDir() + File.separator
                + filename;
        File file = new File(filepath);
        if (file.exists()) {
            Bitmap bmp = ImageUtils.getBitmap(imgView.getContext(), filename);
            imgView.setImageBitmap(bmp);
            return;
        }

        // 从网络获取&写入图片缓存
        String _errMsg = imgView.getContext().getString(
                R.string.msg_load_image_fail);
        if (!StringUtils.isEmpty(errMsg))
            _errMsg = errMsg;
        final String ErrMsg = _errMsg;
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1 && msg.obj != null) {
                    imgView.setImageBitmap((Bitmap) msg.obj);
                    try {
                        // 写图片缓存
                        ImageUtils.saveImage(imgView.getContext(), filename,
                                (Bitmap) msg.obj);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    ToastMessage(imgView.getContext(), ErrMsg);
                }
            }
        };
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    Bitmap bmp = ApiClient.getNetBitmap(imgURL);
                    msg.what = 1;
                    msg.obj = bmp;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * 弹出Toast消息
     *
     * @param msg
     */
    public static void ToastMessage(Context cont, String msg) {
        Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
    }

    public static void ToastMessage(Context cont, int msg) {
        Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
    }

    public static void ToastMessage(Context cont, String msg, int time) {
        Toast.makeText(cont, msg, time).show();
    }

}
