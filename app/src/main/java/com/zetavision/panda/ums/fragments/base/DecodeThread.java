package com.zetavision.panda.ums.fragments.base;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;
import com.zetavision.panda.ums.zxing.camera.DecodeFormatManager;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.zetavision.panda.ums.utils.Constant.IS_DECODE_1D_INDUSTRIAL;
import static com.zetavision.panda.ums.utils.Constant.IS_DECODE_1D_PRODUCT;
import static com.zetavision.panda.ums.utils.Constant.IS_DECODE_AZTEC;
import static com.zetavision.panda.ums.utils.Constant.IS_DECODE_DATA_MATRIX;
import static com.zetavision.panda.ums.utils.Constant.IS_DECODE_PDF417;
import static com.zetavision.panda.ums.utils.Constant.IS_DECODE_QR;

public class DecodeThread extends Thread {
    public static final String BARCODE_BITMAP = "barcode_bitmap";
    public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";

    private final CaptureFragment fragment;
    private final Map<DecodeHintType,Object> hints;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    DecodeThread(CaptureFragment fragment,
                 Collection<BarcodeFormat> decodeFormats,
                 Map<DecodeHintType,?> baseHints,
                 String characterSet,
                 ResultPointCallback resultPointCallback) {

        this.fragment = fragment;
        handlerInitLatch = new CountDownLatch(1);

        hints = new EnumMap<>(DecodeHintType.class);
        if (baseHints != null) {
            hints.putAll(baseHints);
        }

        // The prefs can't change while the thread is running, so pick them up once here.
        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
            if (IS_DECODE_1D_PRODUCT) {
                decodeFormats.addAll(DecodeFormatManager.PRODUCT_FORMATS);
            }
            if (IS_DECODE_1D_INDUSTRIAL) {
                decodeFormats.addAll(DecodeFormatManager.INDUSTRIAL_FORMATS);
            }
            if (IS_DECODE_QR) {
                decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
            }
            if (IS_DECODE_DATA_MATRIX) {
                decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
            }
            if (IS_DECODE_AZTEC) {
                decodeFormats.addAll(DecodeFormatManager.AZTEC_FORMATS);
            }
            if (IS_DECODE_PDF417) {
                decodeFormats.addAll(DecodeFormatManager.PDF417_FORMATS);
            }
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        if (characterSet != null) {
            hints.put(DecodeHintType.CHARACTER_SET, characterSet);
        }
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
        Log.i("DecodeThread", "Hints: " + hints);
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(fragment, hints);
        handlerInitLatch.countDown();
        Looper.loop();
    }
}
