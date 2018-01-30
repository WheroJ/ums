package com.zetavision.panda.ums.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.zetavision.panda.ums.Utils.Api;
import com.zetavision.panda.ums.model.FormInfo;
import com.zetavision.panda.ums.model.Result;


import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public class UmsService extends Service {

    private List<FormInfo> downloadList;   // 当前下载列表
    private OnDownloadListener downloadListener; // 下载监听

    public void setDownloadList(List<FormInfo> downloadList) {
        this.downloadList = downloadList;
        if (downloadList != null) {
            downloadListener.onUpdate(this.downloadList);
        }
    }

    public void setDownloadListener(OnDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    // 下载单个表单
    public void startDownload(final FormInfo info) {
        info.setDownload_status(FormInfo.DOWNLOAD_STATUS.PROGRESS);     // 开始下载
        Api api = new Api(getApplicationContext());
        Flowable<Result> flowable = api.get("downloadForm.mobile?inspectFormId="+info.getFormId());
        flowable.subscribe(new Consumer<Result>() {
            @Override
            public void accept(Result result) throws Exception {
                // TODO 根据不同表单 保存到 本地 sqlite
                System.out.println(result.getReturnData());
                // 保存完改变状态
                info.setDownload_status(FormInfo.DOWNLOAD_STATUS.DONE);     // 下载完成
                if (downloadListener != null) {
                    downloadListener.onUpdate(downloadList);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                info.setDownload_status(FormInfo.DOWNLOAD_STATUS.FAIL);     // 下载失败
                if (downloadListener != null) {
                    downloadListener.onUpdate(downloadList);
                }
            }
        });
    }

    // 下载全部表单
    public void startDownloadAll() {
        if (downloadList == null) return;
        Flowable.fromIterable(downloadList)
//                .filter(new Predicate<FormInfo>() {           //过滤下载完成、正在下载项
//                    @Override
//                    public boolean test(FormInfo info) throws Exception {
//                        return false;
//                    }
//                })
                .subscribe(new Consumer<FormInfo>() {
                    @Override
                    public void accept(FormInfo info) throws Exception {
                        startDownload(info);
                    }
                });
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
