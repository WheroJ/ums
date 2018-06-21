package com.zetavision.panda.ums.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.seuic.scanner.DecodeInfo;
import com.seuic.scanner.DecodeInfoCallBack;
import com.seuic.scanner.Scanner;
import com.seuic.scanner.ScannerFactory;
import com.seuic.scanner.ScannerKey;
import com.zetavision.panda.ums.utils.LogPrinter;

import org.greenrobot.eventbus.EventBus;

@SuppressWarnings("unused")
public class ScannerService extends Service implements DecodeInfoCallBack {
	static final String TAG = "ScannerService";
	Scanner scanner;
	private boolean isServiceDestory = false;

	@Override
	public void onCreate() {
		super.onCreate();
		scanner = ScannerFactory.getScanner(this);
		scanner.open();
		scanner.setDecodeInfoCallBack(this);

		new Thread(runnable).start();
		LogPrinter.i("Scan", "ScanService start");
	}

	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			int ret1 = ScannerKey.open();
			LogPrinter.i("Scan", "thread start");
			if (ret1 > -1) {
				while (!isServiceDestory) {
					int ret = ScannerKey.getKeyEvent();
					if (ret > -1) {
						switch (ret) {
						case ScannerKey.KEY_DOWN:
							scanner.startScan();
							break;
						case ScannerKey.KEY_UP:
							scanner.stopScan();
							break;
						}
					}
				}
			}
			LogPrinter.i("Scan", "thread stop");
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		isServiceDestory = false;
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		scanner.setDecodeInfoCallBack(null);
		scanner.setVideoCallBack(null);
		scanner.close();
		isServiceDestory = true;
		LogPrinter.i("Scan", "ScanService destory");
		super.onDestroy();
	}

	public static final String BAR_CODE = "barcode";
	public static final String CODE_TYPE = "codetype";
	public static final String LENGTH = "length";
	
	// 此处为自定义广播接收器action
	public static final String ACTION = "seuic.android.scanner.scannertestreciever";

	@Override
	public void onDecodeComplete(DecodeInfo info) {
		EventBus.getDefault().post(info);
	}
	
}
