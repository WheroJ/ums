package com.zetavision.panda.ums.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.base.BaseActivity;
import com.zetavision.panda.ums.utils.LogPrinter;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.UIUtils;
import com.zetavision.panda.ums.widget.BothWayProgressBar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static android.media.MediaPlayer.OnCompletionListener;
import static android.media.MediaPlayer.OnPreparedListener;

/**
 * Created by wheroj on 2018/2/6 14:57.
 *
 * @describe
 */

public class VideoActivity extends BaseActivity implements View.OnTouchListener, BothWayProgressBar.OnProgressEndListener{

    private static final int LISTENER_START = 200;

    private Camera mCamera;
    private CameraPreviewView cameraPreviewView;
    private PlayerView playerView;
    private MediaRecorder recorder;

    //进度条
    private BothWayProgressBar mProgressBar;

    private TextView mTvTip;
    //当前进度
    private float mProgress;
    private String outputMediaPath;
    private boolean isFocus = true;
    @BindView(R.id.activityVideo_flContent)
    FrameLayout preview;

    @Override
    public void onProgressEndListener() {
        releaseMediaRecoder(false);
    }

    @Override
    public void beforeInit() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public int getContentLayoutId() {
        return R.layout.activity_video;
    }

    @Override
    protected void init() {

        startCamera();

        View mStartButton = findViewById(R.id.activityVideo_rlStartRecord);
        mTvTip = findViewById(R.id.activityVideo_tvTip);

        mStartButton.setOnTouchListener(this);
        mProgressBar = findViewById(R.id.activityVideo_progressBar);
        mProgressBar.setOnProgressEndListener(this);
    }

    private int totalTime = 10*1000, currentTime = 0, interval = 50;
    private CompositeDisposable disposables;
    private boolean isRunning;
    //是否上滑取消
    private boolean isCancel;

    /**
     * 触摸事件的触发
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean ret = false;
        int action = event.getAction();
        float ey = event.getY();
        float ex = event.getX();
        //只监听中间的按钮处
        int vW = v.getWidth();
        int left = LISTENER_START;
        int right = vW - LISTENER_START;

        float downY = 0;

        switch (v.getId()) {
            case R.id.activityVideo_rlStartRecord: {
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        if (ex > left && ex < right) {
                            mProgressBar.setCancel(false);
                            //显示上滑取消
                            mTvTip.setVisibility(View.VISIBLE);
                            mTvTip.setText(R.string.up_to_cancel);
                            //记录按下的Y坐标
                            downY = ey;
                            mProgressBar.setVisibility(View.VISIBLE);
                            //开始录制
//                            ToastUtils.show(R.string.start_record);
                            if (prepareVideoRecorder()) {
                                recorder.start();
                            }

                            currentTime = 0; isRunning = true; mProgress = 0;
                            mProgressBar.setProgress(mProgress);
                            if (disposables != null) {
                                disposables.dispose();
                                disposables = null;
                            }
                            disposables = new CompositeDisposable();

                            disposables.add(Observable.interval(interval, interval, TimeUnit.MILLISECONDS)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .filter(new Predicate<Long>() {
                                        @Override
                                        public boolean test(Long aLong) throws Exception {
                                            currentTime += interval;
                                            if (isRunning) {
                                                if (currentTime <= totalTime) {
                                                    return true;
                                                } else {
                                                    isRunning = false;
                                                    releaseMediaRecoder(false);
                                                    disposables.dispose();
                                                    return false;
                                                }
                                            } else {
                                                disposables.dispose();
                                                return false;
                                            }
                                        }
                                    })
                                    .subscribe(new Consumer<Long>() {
                                        @Override
                                        public void accept(Long aLong) throws Exception {
                                            mProgress = currentTime*1f/totalTime;
                                            mProgressBar.setProgress(mProgress);
                                        }
                                    }));
                            ret = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (ex > left && ex < right && isRunning) {
                            mTvTip.setVisibility(View.INVISIBLE);
                            mProgressBar.setVisibility(View.INVISIBLE);
                            //判断是否为录制结束, 或者为成功录制(时间过短)
                            if (!isCancel) {
                                if (currentTime <= 1) {
                                    //时间太短不保存
                                    releaseMediaRecoder(true);
                                    ToastUtils.show(getString(R.string.time_short));
                                    break;
                                }
                                //停止录制
                                releaseMediaRecoder(false);
                            } else {
                                //现在是取消状态,不保存
                                releaseMediaRecoder(true);

                                isCancel = false;
                                mProgressBar.setCancel(false);
                                ToastUtils.show(getString(R.string.cancel_record));
                            }

                            ret = false;
                        }
                        isRunning = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (ex > left && ex < right) {
                            float currentY = event.getY();
                            if (downY - currentY > 10) {
                                isCancel = true;
                                mProgressBar.setCancel(true);
                            } else {
                                isCancel = false;
                                mProgressBar.setCancel(isCancel);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        releaseMediaRecoder(false);
                        isRunning = false;
                        break;
                }
                break;

            }

        }
        return ret;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFocus = false;
        stop();
        releaseMediaRecoder(false);
    }

    @OnClick({R.id.activityVideo_ivBack, R.id.activityVideo_ivFinish, R.id.activityVideo_ivAbandon})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activityVideo_ivBack:
                releaseMediaRecoder(true);
                setContentView(false);

                player = null;
                break;
            case R.id.activityVideo_ivFinish:
                //是否添加到相册
                ContentResolver localContentResolver = getContentResolver();
                ContentValues localContentValues = getVideoContentValues(new File(outputMediaPath), System.currentTimeMillis());
                localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);

                Intent intent = getIntent();
                intent.putExtra("videoPath", outputMediaPath);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.activityVideo_ivAbandon:
                finish();
                break;
        }
    }

    private void releaseMediaRecoder(boolean deleteFile){
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
            if (mCamera != null) mCamera.lock();
        }

        if (!TextUtils.isEmpty(outputMediaPath)) {
            File file = new File(outputMediaPath);
            if (file.exists()) {
                if (deleteFile) {
                    if (file.isFile() && file.exists()) file.delete();
                } else {
                    play();
                }
            }
        }
    }

    public static ContentValues getVideoContentValues(File videoFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", videoFile.getName());
        localContentValues.put("_display_name", videoFile.getName());
        localContentValues.put("mime_type", "video/mp4");
        localContentValues.put("datetaken", paramLong);
        localContentValues.put("date_modified", paramLong);
        localContentValues.put("date_added", paramLong);
        localContentValues.put("_data", videoFile.getAbsolutePath());
        localContentValues.put("_size", videoFile.length());
        return localContentValues;
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
        if (mCamera != null) {
            mCamera.unlock();
            recorder.setCamera(mCamera);

            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            // 从麦克采集音频信息
//            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
//            recorder.setVideoFrameRate(24);

            recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
            recorder.setVideoSize(720, 480);
            outputMediaPath = getOutputMediaPath();
            //解决录制视频, 播放器横向问题
            recorder.setOrientationHint(90);
//            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setOutputFile(outputMediaPath);

            recorder.setPreviewDisplay(cameraPreviewView.getHolder().getSurface());
            try {
                recorder.prepare();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * 获取输出video文件目录
     * @return
     */
    private String getOutputMediaPath() {
        java.util.Date date = new java.util.Date();
        String timeTemp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date.getTime());
        File mediaStorageDir = new File(UIUtils.getCachePath() + File.separator + "Video");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                LogPrinter.d(this.getLocalClassName(), "failed to create directory");
                return null;
            }
        }

        LogPrinter.d(this.getLocalClassName(), "getOutputMediaPath file path---"+
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
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                }
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

//            try {
//                mCamera.setPreviewDisplay(holder);
//                mCamera.startPreview();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    public class PlayerView extends SurfaceView implements SurfaceHolder.Callback{
        private SurfaceHolder holder;
        private MediaPlayer player;

        public PlayerView(Context context, MediaPlayer player) {
            super(context);
            this.player = player;

            holder = getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            if (player != null) player.setDisplay(holder);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (player != null) {
                stop();
            }
        }
    }

    private MediaPlayer player;
    private boolean isPause = false;
    private void play() {
        if (isFocus) {
            if (isPause) {//如果是暂停状态下播放，直接start
                isPause = false;
                player.start();
                return;
            }

            File file = new File(outputMediaPath);
            if (!file.exists()) {//判断需要播放的文件路径是否存在，不存在退出播放流程
                ToastUtils.show(R.string.file_not_exist);
                return;
            }

            try {
                player = new MediaPlayer();
                player.setDataSource(file.getAbsolutePath());
                //将影像播放控件与媒体播放控件关联起来
//            player.setDisplay(playerView.getHolder());

                player.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {//视频播放完成后，释放资源
                        if (player != null) player.start();
                    }
                });
                player.setOnPreparedListener(new OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        //媒体播放器就绪后，设置进度条总长度，开启计时器不断更新进度条，播放视频
                        if (player != null) player.start();
                    }
                });

                player.prepareAsync();

                setContentView(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置界面显示
     * @param showPlayer  是否展示播放界面
     */
    private void setContentView(boolean showPlayer) {
        FrameLayout preview = findViewById(R.id.activityVideo_flContent);
        if (showPlayer) {
            preview.removeAllViews();
            playerView = new PlayerView(this, player);
            preview.addView(playerView);

            findViewById(R.id.activityVideo_rlPlayer).setVisibility(View.VISIBLE);
            findViewById(R.id.activityVideo_rlStartRecord).setVisibility(View.GONE);
        } else {
            preview.removeAllViews();
            cameraPreviewView = new CameraPreviewView(this, mCamera);
            preview.addView(cameraPreviewView);

            findViewById(R.id.activityVideo_rlPlayer).setVisibility(View.GONE);
            findViewById(R.id.activityVideo_rlStartRecord).setVisibility(View.VISIBLE);
        }
    }

    private void stop(){
        isPause = false;
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    public void stopPreview() {
        synchronized (this) {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }
    }

    private void startCamera() {
        if (!checkCameraHardware(this)) {
            return;
        }
        mCamera = getCameraInstance();
        cameraPreviewView = new CameraPreviewView(this, mCamera);
        preview.removeAllViews();
        preview.addView(cameraPreviewView);
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
        if (camera != null) {
            camera.setDisplayOrientation(90);
            Camera.Parameters parameters = camera.getParameters();
            //实现Camera自动对焦
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes != null) {
                for (String mode : focusModes) {
                    if (mode.contains("continuous-video")) {
                        parameters.setFocusMode("continuous-video");
                        break;
                    }
                }
            }
            camera.setParameters(parameters);
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
            return true;
        }
        else {
            return false;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
        releaseMediaRecoder(false);
    }
}
