package com.example.rui.sight2.sight;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.rui.sight2.R;
import com.example.rui.sight2.sight.permission.PermissionHelper;
import com.example.rui.sight2.sight.permission.PermissionInterface;
import com.example.rui.sight2.sight.permission.PermissionUtil;
import com.example.rui.sight2.sight.view.CameraView;

import java.io.File;

/**
 * @author guanzhirui
 */
public class SightRecordActivity extends AppCompatActivity implements PermissionInterface {

    //要申请的权限
    private String[] mPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    private PermissionHelper permissionHelper;

    private CameraView mCameraView;
    private String TAG = "SightRecordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_sight_record);

        //初始化
        permissionHelper = new PermissionHelper(this, this);
        //申请权限
        permissionHelper.requestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] mPermissions, int[] grantResults) {
        if (permissionHelper.requestPermissionsResult(requestCode, mPermissions, grantResults)) {
            //权限请求结果，并已经处理了该回调
            return;
        }
        super.onRequestPermissionsResult(requestCode, mPermissions, grantResults);
    }

    @Override
    public int getPermissionsRequestCode() {
        return 0;
    }

    @Override
    public String[] getPermissions() {
        return mPermissions;
    }

    @Override
    public void requestPermissionsSuccess() {
        mCameraView = findViewById(R.id.cameraView);
        mCameraView.setAutoFocus(false);
        mCameraView.setSupportCapture(false);
        mCameraView.setMaxRecordDuration(15);

        String path = getIntent().getStringExtra("path");
        if (path == null){
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Sight" + File.separator;
        }
        File file = new File(path);
        if (!file.exists()){
            file.mkdirs();
        }
        mCameraView.setSaveVideoPath(path);

        mCameraView.setCameraViewListener(new CameraView.CameraViewListener() {
            @Override
            public void quit() {
                finish();
            }

            @Override
            public void captureSuccess(Bitmap bitmap) {
                Log.d(TAG,"抓图成功");
            }

            @Override
            public void recordSuccess(String s, int i) {
                Log.d(TAG,"拍摄成功");
                if (s.isEmpty()){
                    finish();
                }else {
                    Intent intent = new Intent();
                    intent.putExtra("url",s);
                    setResult(RESULT_OK,intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void requestPermissionsFail() {
        StringBuilder sb = new StringBuilder();
        mPermissions = PermissionUtil.getDeniedPermissions(this, mPermissions);
        for (String s : mPermissions) {
            if (s.equals(Manifest.permission.CAMERA)) {
                sb.append("相机权限(用于拍视频);\n");
            } else if (s.equals(Manifest.permission.RECORD_AUDIO)) {
                sb.append("麦克风权限(用于视频录音);\n");
            } else if (s.equals(Manifest.permission.READ_EXTERNAL_STORAGE) || s.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                sb.append("存储,读取权限(用于存储必要信息，缓存数据);\n");
            }
        }
        PermissionUtil.PermissionDialog(this, "程序运行需要如下权限：\n" + sb.toString() + "请在应用权限管理进行设置！");
    }
}
