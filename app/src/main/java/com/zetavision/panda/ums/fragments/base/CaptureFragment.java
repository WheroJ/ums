package com.zetavision.panda.ums.fragments.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.zxing.Intents;
import com.zetavision.panda.ums.zxing.ViewfinderView;
import com.zetavision.panda.ums.zxing.camera.BeepManager;
import com.zetavision.panda.ums.zxing.camera.CameraManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import butterknife.BindView;

import static com.zetavision.panda.ums.utils.Constant.IS_DISABLE_AUTO_ORIENTATION;

public abstract class CaptureFragment extends BaseFragment implements SurfaceHolder.Callback, ViewTreeObserver.OnGlobalLayoutListener {

    private static final String TAG = CaptureFragment.class.getSimpleName();

    private CameraManager cameraManager;
    private BeepManager beepManager;
    private boolean hasSurface;
    private CaptureHandler handler;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType,?> decodeHints;
    private String characterSet;
    private Result savedResultToShow;

    @BindView(R.id.preview_view) public SurfaceView preview_view;
    @BindView(R.id.finderView) public ViewfinderView finderView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        hasSurface = false;
        beepManager = new BeepManager(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getActivity().getIntent();

        cameraManager = new CameraManager(getActivity().getApplication());

        finderView.setCameraManager(cameraManager);

        handler = null;
        decodeFormats = null;
        decodeHints = null;
        characterSet = null;

        if (IS_DISABLE_AUTO_ORIENTATION) {
            getActivity().setRequestedOrientation(getCurrentOrientation());
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

        SurfaceHolder surfaceHolder = preview_view.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }

        if (getView()!= null) {
            getView().getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                 handler = new CaptureHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }


    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("Sorry, the Android camera encountered a problem. You may need to restart the device.");
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });
        builder.show();
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
//        ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);

        boolean fromLiveScan = barcode != null;
        if (fromLiveScan) {
            // Then not from history, so beep/vibrate and we have an image to draw on
            beepManager.playBeepSoundAndVibrate();
            drawResultPoints(barcode, scaleFactor, rawResult);
        }

        // 停止预览
//        if (cameraManager!= null) {
//            cameraManager.stopPreview();
//        }

//        // 重新开始扫描
//        if (handler != null) {
//            handler.sendEmptyMessageDelayed(R.id.restart_preview, 1000L);
//        }

        // 结果
//        System.out.println(rawResult.getText());
//        System.out.println(barcode);
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
     *
     * @param barcode   A bitmap of the captured image.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param rawResult The decoded results which contains the points to draw.
     */
    private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_points, null));
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
            } else if (points.length == 4 &&
                    (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                            rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and metadata
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
                drawLine(canvas, paint, points[2], points[3], scaleFactor);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    if (point != null) {
                        canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
                    }
                }
            }
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
        if (a != null && b != null) {
            canvas.drawLine(scaleFactor * a.getX(),
                    scaleFactor * a.getY(),
                    scaleFactor * b.getX(),
                    scaleFactor * b.getY(),
                    paint);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public void drawViewfinder() {
        finderView.drawViewfinder();
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public CaptureHandler getHandler() {
        return handler;
    }

    private int getCurrentOrientation() {
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            switch (rotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            }
        } else {
            switch (rotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_270:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        WindowManager manager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        if (display != null && getView()!=null) {
            Point preview_size = new Point(getView().getWidth(), getView().getHeight());
            Point capture_size = new Point();
            display.getSize(capture_size);

            int left_right = - (capture_size.x - preview_size.x) / 2;
            int top_bottom = - (capture_size.y - preview_size.y) / 2;

            FrameLayout.LayoutParams find_params = new FrameLayout.LayoutParams(finderView.getLayoutParams());
            find_params.width = capture_size.x;
            find_params.height = capture_size.y;
            find_params.setMargins( left_right, top_bottom, left_right, top_bottom);
            finderView.setLayoutParams(find_params);

            FrameLayout.LayoutParams preview_params = new FrameLayout.LayoutParams(preview_view.getLayoutParams());
            preview_params.width = capture_size.x;
            preview_params.height = capture_size.y;
            preview_params.setMargins( left_right, top_bottom, left_right, top_bottom);
            preview_view.setLayoutParams(preview_params);
        }
    }

    @Override
    public void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        if (getView()!=null) {
            getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        beepManager.close();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = preview_view.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }
}
