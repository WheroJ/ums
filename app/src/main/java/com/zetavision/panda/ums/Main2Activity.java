package com.zetavision.panda.ums;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.zetavision.panda.ums.utils.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by wheroj on 2018/2/6 14:57.
 *
 * @describe
 */

public class Main2Activity extends Activity {
    private Camera mCamera;
    private CameraPreviewView previewView;
    private MediaRecorder recorder;
    private boolean isrecording = false;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        if (!checkCameraHardware(this)) {
            return;
        }
        mCamera = getCameraInstance();
        previewView = new CameraPreviewView(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(previewView);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isrecording) {
                    releaseMediaRecoder();
                    //mCamera.lock();
                    btn.setText("Capture");
                    isrecording = false;
                }
                else {
                    if (prepareVideoRecorder()) {
                        recorder.start();
                        btn.setText("Stop");
                        isrecording = true;
                    }
                    else {
                        releaseMediaRecoder();
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecoder();
        releaseCamera();
    }

    private void releaseMediaRecoder(){
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
            mCamera.lock();
        }
    }

    private void releaseCamera(){
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 创建MediaRecorder实例，并为之设定基本属性
     * @return
     */
    private boolean prepareVideoRecorder(){
        recorder = new MediaRecorder();
        mCamera.unlock();
        recorder.setCamera(mCamera);

        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        recorder.setOutputFile(getOutputMediaPath());

        recorder.setPreviewDisplay(previewView.getHolder().getSurface());
        try {
            recorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 获取输出video文件目录
     * @return
     */
    private String getOutputMediaPath() {
        java.util.Date date = new java.util.Date();
        String timeTemp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date.getTime());
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        Log.e("Error", "getOutputMediaPath file path---"+
                mediaStorageDir.getPath() + File.separator +
                "VID_" + timeTemp+ ".mp4");
        return mediaStorageDir.getPath() + File.separator +
                "VID_" + timeTemp + ".mp4";
    }

    public class CameraPreviewView extends SurfaceView implements SurfaceHolder.Callback{
        private SurfaceHolder holder;
        private Camera mCamera;

        public CameraPreviewView(Context context, Camera camera) {
            super(context);
            this.mCamera = camera;

            holder = getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (holder.getSurface() == null) {
                return;
            }

            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 获取camera实例
     * @return
     */
    public static Camera getCameraInstance(){
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }

    /**
     * 检测手机有无摄像头
     * @param context
     * @return
     */
    private boolean checkCameraHardware(Context context){
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            ToastUtils.show("successfully detact camera");
            return true;
        }
        else {
            ToastUtils.show("not detact camera!!!");
            return false;
        }
    }
}
