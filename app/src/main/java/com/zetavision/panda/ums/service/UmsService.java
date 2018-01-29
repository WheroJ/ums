package com.zetavision.panda.ums.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.zetavision.panda.ums.model.FormInfo;

import java.util.List;


public class UmsService extends Service {

    private List<FormInfo> downloadList;   // 当前下载列表
    private OnDownloadListener downloadListener; // 下载监听

    public void setDownloadList(List<FormInfo> downloadList) {
        this.downloadList = downloadList;
    }

    // 下载单个表单
    public void startDownload(FormInfo info) {

    }

    // 下载全部表单
    public void startDownloadAll() {

    }

    // 停止下载单个表单
    public void stopDownload(FormInfo info) {

    }

    // 停止下载所有表单
    public void stopDownloadAll() {

    }

    public interface OnDownloadListener {
        void onUpdate(List<FormInfo> list);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //通过binder实现调用者client与Service之间的通信
    private MyBinder binder = new MyBinder();

    //client 可以通过Binder获取Service实例
    public class MyBinder extends Binder {
        public UmsService getService() {
            return UmsService.this;
        }
    }
}
