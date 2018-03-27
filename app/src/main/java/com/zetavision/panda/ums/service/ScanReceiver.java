package com.zetavision.panda.ums.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by wheroj on 2018/3/9 16:11.
 *
 * @describe
 */

public class ScanReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        String barCode = bundle.getString(ScannerService.BAR_CODE, "");
        EventBus.getDefault().post(barCode);
    }
}
