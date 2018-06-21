package com.zetavision.panda.ums.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.seuic.scanner.DecodeInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wheroj on 2018/3/9 16:11.
 *
 * @describe
 */

public class ScanReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        DecodeInfo decodeInfo = new DecodeInfo();
        String scannerdata = intent.getStringExtra("scannerdata");

        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(scannerdata);

        decodeInfo.barcode = m.replaceAll("");
//        decodeInfo.barcode = bundle.getString(ScannerService.BAR_CODE, "");
        decodeInfo.codetype = bundle.getString(ScannerService.CODE_TYPE, "");
//        decodeInfo.length = bundle.getInt(ScannerService.LENGTH, 0);
        EventBus.getDefault().post(decodeInfo);
    }
}
