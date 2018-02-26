package com.zetavision.panda.ums.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.model.FormInfo;
import com.zetavision.panda.ums.model.FormInfoDetail;
import com.zetavision.panda.ums.model.FormItem;
import com.zetavision.panda.ums.model.Result;
import com.zetavision.panda.ums.utils.Constant;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.LogPrinter;
import com.zetavision.panda.ums.utils.NetUtils;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.UIUtils;
import com.zetavision.panda.ums.utils.UserUtils;
import com.zetavision.panda.ums.utils.network.Client;
import com.zetavision.panda.ums.utils.network.RxUtils;
import com.zetavision.panda.ums.utils.network.UmsApi;
import com.zetavision.panda.ums.utils.network.UploadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class UmsService extends Service {

    private List<FormInfo> downloadList;   // 当前下载列表
    private List<FormInfoDetail> uploadList;   // 当前上传列表
    private OnDownloadListener downloadListener; // 下载监听
    private OnUploadListener uploadListener; // 上传监听

    private HashMap<String, Disposable> disposableHashMap ;

    public void setDownloadList(List<FormInfo> downloadList) {
        this.downloadList = downloadList;
        if (downloadList != null) {
            downloadListener.onUpdate(this.downloadList);
        }
    }

    public void setUploadList(List<FormInfoDetail> uploadList) {
        this.uploadList = uploadList;
        if (uploadListener != null)
            uploadListener.onUpdate(uploadList);
    }

    public void setDownloadListener(OnDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    public void setUploadListener(OnUploadListener UploadListener) {
        this.uploadListener = UploadListener;
    }

    public void startUpload(String formId) {
        final FormInfoDetail formInfoDetail = getFormInfoDetail(formId);

        if (formInfoDetail != null && Constant.FORM_STATUS_COMPLETED.equals(formInfoDetail.form.getStatus())) {
            if (formInfoDetail.isUpload == FormInfo.DONE
                    || formInfoDetail.isUpload == FormInfo.PROGRESS)
                return;
            else formInfoDetail.isUpload = FormInfo.PROGRESS;

            if (formInfoDetail.formItemList != null) {
                ArrayList<String> photoPaths = new ArrayList<>();
                for (int i = 0; i < formInfoDetail.formItemList.size(); i++) {
                    FormItem item = formInfoDetail.formItemList.get(i);
                    if (item.photoPaths != null && !item.photoPaths.isEmpty()) {
                        photoPaths.addAll(item.photoPaths);
                    }
                }

                if (!photoPaths.isEmpty()) {
                    UploadUtils.INSTANCE.upload(new UploadUtils.UploadListener() {
                        @Override
                        public void onResult(@NotNull Result result) {
                            super.onResult(result);
                            parseUrlMap(result, formInfoDetail);
                            upload(formInfoDetail);
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            super.onError(e);
                            ToastUtils.show(e.getMessage());
                        }
                    }, photoPaths);
                } else upload(formInfoDetail);
            }
        }
    }

    private void upload(final FormInfoDetail formInfoDetail) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONArray forms = new JSONArray();
        try {
            JSONObject formDetail = new JSONObject();

            JSONObject form = new JSONObject();
            form.put("formId", formInfoDetail.form.getFormId());
            form.put("formCode", formInfoDetail.form.getFormCode());
            form.put("actionType", formInfoDetail.form.getActionType());
            form.put("weather", formInfoDetail.form.weather);
            form.put("shift", formInfoDetail.form.shift);
            form.put("startUser", formInfoDetail.form.startUser);
            Date date = new Date();
            date.setTime(formInfoDetail.form.startTime*1000);
            form.put("startTime", simpleDateFormat.format(date));
            form.put("completeUser", formInfoDetail.form.completeUser);
            date.setTime(formInfoDetail.form.completeTime*1000);
            form.put("completeTime", simpleDateFormat.format(date));
            form.put("fillinRemarks", formInfoDetail.form.fillinRemarks == null?"":formInfoDetail.form.fillinRemarks);

            JSONArray formItemList = new JSONArray();
            if (formInfoDetail.formItemList != null) {
                for (int i = 0; i < formInfoDetail.formItemList.size(); i++) {
                    JSONObject formItem = new JSONObject();
                    FormItem item = formInfoDetail.formItemList.get(i);
                    formItem.put("formId", item.formId);
                    formItem.put("formItemId", item.formItemId);
                    formItem.put("result", item.result);
                    formItem.put("remarks", item.remarks == null?"":item.remarks);
                    if (FormInfo.ACTION_TYPE_P.equals(formInfoDetail.actionType)) {
                        formItem.put("photoUrls", item.photoUrls);
                    }
                    formItemList.put(formItem);
                }
            }

            formDetail.put("form", form);
            formDetail.put("formItemList", formItemList);
            forms.put(formDetail);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (uploadListener != null) uploadListener.onUpdate(uploadList);
        Observable<ResponseBody> observable = Client.getApi(UmsApi.class).uploadForm(forms.toString());
        RxUtils.INSTANCE.acquireString(observable, new RxUtils.DialogListener(){
            @Override
            public void onResult(@NotNull Result result) {
                try {
                    JSONObject jsonObject = new JSONObject(result.getReturnData());
                    JSONArray duplicatedFormIds = jsonObject.optJSONArray("duplicatedFormIds");
                    JSONArray wrongTypeFormIdss = jsonObject.optJSONArray("wrongTypeFormIdss");
                    if (duplicatedFormIds != null
                            && duplicatedFormIds.length() > 0) {
                        formInfoDetail.isUpload = FormInfo.FAIL;
                        formInfoDetail.delete();
                        if (uploadListener != null) {
                            uploadListener.onUpdate(uploadList);
                        }
                        ToastUtils.show(R.string.error_uploaded);
                    } else if (wrongTypeFormIdss != null && wrongTypeFormIdss.length() > 0) {
                        formInfoDetail.isUpload = FormInfo.WAIT;
                        if (uploadListener != null) {
                            uploadListener.onUpdate(uploadList);
                        }
                        ToastUtils.show(R.string.error_formaction);
                    } else {
                        formInfoDetail.isUpload = FormInfo.DONE;
                        formInfoDetail.delete();
                        if (uploadListener != null) {
                            uploadListener.onUpdate(uploadList);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(@NotNull Throwable e) {
                super.onError(e);
                formInfoDetail.isUpload = FormInfo.FAIL;     // 下载失败
                if (downloadListener != null) {
                    downloadListener.onUpdate(downloadList);
                }
            }

            @Override
            public void onStart(@NotNull Disposable d) {
                super.onStart(d);
                disposableHashMap.put(formInfoDetail.formId, d);
            }
        });
    }

    private void parseUrlMap(@NotNull Result result, FormInfoDetail formInfoDetail) {
        try {
            JSONObject jsonObject = new JSONObject(result.getReturnData());
            JSONObject urlMap = jsonObject.optJSONObject("urlMap");
            for (int i = 0; i < formInfoDetail.formItemList.size(); i++) {
                FormItem formItem = formInfoDetail.formItemList.get(i);
                if (formItem.photoPaths != null) {
                    for (int j = 0; j < formItem.photoPaths.size(); j++) {
                        String path = formItem.photoPaths.get(j);
                        if (!TextUtils.isEmpty(path)) {
                            int indexOf = path.lastIndexOf(File.separator);
                            if (indexOf != -1) {
                                String fileName = path.substring(indexOf + 1);
                                String url = urlMap.optString(fileName);
                                if (!TextUtils.isEmpty(url)) {
                                    if (formItem.photoUrls == null)
                                        formItem.photoUrls = new ArrayList<>();
                                    formItem.photoUrls.add(url);
                                }
                            }
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 下载单个表单
    public void startDownload(String formId) {
        final FormInfo info = getFormInfo(formId);

        //是否下载过
        List<FormInfoDetail> formInfoDetails = DataSupport.where("formId='" + info.getFormId() + "'").find(FormInfoDetail.class);
        if (formInfoDetails == null || formInfoDetails.isEmpty()) {

            //已经下载过的表单无需再下载
            if (info.getDownload_status() == FormInfo.DONE
                    || info.getDownload_status() == FormInfo.PROGRESS)
                return;
            else info.setDownload_status(FormInfo.PROGRESS);     // 开始下载

            if (downloadListener != null) downloadListener.onUpdate(downloadList);

            Observable<ResponseBody> observable;
            if (FormInfo.ACTION_TYPE_M.equals(info.getActionType())) {
                //保养表单下载
                observable = Client.getApi(UmsApi.class).downloadMaintForm(formId);
            } else {
                //点检表单下载
                observable = Client.getApi(UmsApi.class).downloadInspectForm(info.getFormId());
            }
            RxUtils.INSTANCE.acquireString(observable, new RxUtils.DialogListener() {
                        @Override
                        public void onResult(@NotNull Result result) {

                            List<FormInfoDetail> formInfoDetails = result.getList(FormInfoDetail.class);
                            if (formInfoDetails != null && !formInfoDetails.isEmpty()) {
                                FormInfoDetail formInfoDetail = formInfoDetails.get(0);
                                formInfoDetail.formId = formInfoDetail.form.getFormId();
                                formInfoDetail.equipmentCode = formInfoDetail.form.getEquipmentCode();
                                formInfoDetail.inspectRouteCode = formInfoDetail.form.getInspectRouteCode();
                                formInfoDetail.actionType = formInfoDetail.form.getActionType();
                                formInfoDetail.utilitySystemId = formInfoDetail.form.getUtilitySystemId();
                                formInfoDetail.form.setDownload_status(FormInfo.DONE);
                                formInfoDetail.form.saveOrUpdate("(formId='" + formInfoDetail.form.getFormId() + "')");
                                DataSupport.saveAll(formInfoDetail.formItemList);
                                formInfoDetail.saveOrUpdate("(formId='" + formInfoDetail.formId + "')");
                            }

                            // 保存完改变状态
                            info.setDownload_status(FormInfo.DONE);     // 下载完成
                            if (downloadListener != null) {
                                downloadListener.onUpdate(downloadList);
                            }
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            super.onError(e);
                            info.setDownload_status(FormInfo.FAIL);     // 下载失败
                            if (downloadListener != null) {
                                downloadListener.onUpdate(downloadList);
                            }
                        }

                        @Override
                        public void onStart(@NotNull Disposable d) {
                            super.onStart(d);
                            disposableHashMap.put(info.getFormId(), d);
                        }
                    });
        } else {
            info.setDownload_status(FormInfo.DONE);
            if (downloadListener != null) {
                downloadListener.onUpdate(downloadList);
            }
        }
    }

    /**
     * 下载全部表单
     */
    public void startDownloadAll() {
        if (downloadList == null) return;
        Flowable.fromIterable(downloadList)
                .filter(new Predicate<FormInfo>() {           //过滤下载完成、正在下载项
                    @Override
                    public boolean test(FormInfo info) throws Exception {
                        return info.getDownload_status() == FormInfo.WAIT
                                || info.getDownload_status() == FormInfo.FAIL;
                    }
                })
                .subscribe(new Consumer<FormInfo>() {
                    @Override
                    public void accept(FormInfo info) throws Exception {
                        startDownload(info.getFormId());
                    }
                });
    }

    public void startUploadAll() {
        if (uploadList == null || uploadList.isEmpty()) return;
        Flowable.fromIterable(uploadList)
                .filter(new Predicate<FormInfoDetail>() {           //过滤下载完成、正在下载项
                    @Override
                    public boolean test(FormInfoDetail info) throws Exception {
                        return info.isUpload == FormInfo.WAIT
                                || info.isUpload == FormInfo.FAIL;
                    }
                })
                .subscribe(new Consumer<FormInfoDetail>() {
                    @Override
                    public void accept(FormInfoDetail info) throws Exception {
                        startUpload(info.formId);
                    }
                });
    }

    /**
     * 停止下载单个表单
     * @param formId
     */
    public void stopDownload(String formId) {
        Disposable disposable = disposableHashMap.get(formId);
        if (disposable != null) {
            disposable.dispose();

            FormInfo formInfo = getFormInfo(formId);
            if (formInfo != null) {
                if (formInfo.getDownload_status() == FormInfo.PROGRESS) {
                    formInfo.setDownload_status(FormInfo.WAIT);
                }
                if (downloadListener != null)
                    downloadListener.onUpdate(downloadList);
            }
        }
    }

    public void stopUpload(String formId) {
        Disposable disposable = disposableHashMap.get(formId);
        if (disposable != null) {
            disposable.dispose();

            FormInfoDetail formInfoDetail = getFormInfoDetail(formId);
            if (formInfoDetail != null) {
                if (formInfoDetail.form.getDownload_status() == FormInfo.PROGRESS) {
                    formInfoDetail.form.setDownload_status(FormInfo.WAIT);
                }
                if (downloadListener != null)
                    uploadListener.onUpdate(uploadList);
            }
        }
    }

    public void stopUploadAll() {
        Iterator<String> iterator = disposableHashMap.keySet().iterator();
        while (iterator.hasNext()) {
            String formId = iterator.next();
            stopUpload(formId);
        }
    }

    /**
     * 停止下载所有表单
     */
    public void stopDownloadAll() {
        Iterator<String> iterator = disposableHashMap.keySet().iterator();
        while (iterator.hasNext()) {
            String formId = iterator.next();
            stopDownload(formId);
        }
    }

    public FormInfo getFormInfo(String formId) {
        if (downloadList != null && !downloadList.isEmpty()) {
            for (FormInfo formInfo : downloadList) {
                if (!TextUtils.isEmpty(formId) && formId.equals(formInfo.getFormId())) {
                   return formInfo;
                }
            }
        }
        return null;
    }

    public FormInfoDetail getFormInfoDetail(String formId) {
        if (uploadList != null && !uploadList.isEmpty()) {
            for (FormInfoDetail formInfoDetail : uploadList) {
                if (!TextUtils.isEmpty(formId) && formId.equals(formInfoDetail.formId)) {
                    return formInfoDetail;
                }
            }
        }
        return null;
    }

    public interface OnDownloadListener {
        void onUpdate(List<FormInfo> list);
    }

    public interface OnUploadListener {
        void onUpdate(List<FormInfoDetail> list);
    }

    @Override
    public IBinder onBind(Intent intent) {
        disposableHashMap = new HashMap<>();
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

    private CompositeDisposable compositeDisposable;
    private ReLoginReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.RELOGINACTION);
        intentFilter.addAction(Constant.REGETTOKEN);
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new ReLoginReceiver();
        registerReceiver(receiver, intentFilter);

        if (!NetUtils.INSTANCE.isNetConnect(getApplication()) && (compositeDisposable == null || compositeDisposable.size() == 0 )) {
            addLoginTimeObserver();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean stopService(Intent name) {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        return super.stopService(name);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String str) {
        switch (str) {
            case Constant.NET_CONNECT:
                dispose();
                break;
            case Constant.NET_DISCONNECT:
                if (compositeDisposable == null || compositeDisposable.size() == 0) {
                    addLoginTimeObserver();
                }
                break;
        }
    }

    public void dispose() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable.clear();
            compositeDisposable = new CompositeDisposable();
            LogPrinter.i("UmsService", "dispose...........");
        }
    }

    public void addLoginTimeObserver() {
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(Observable.interval(0, 1, TimeUnit.MINUTES, Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        /*if (UserUtils.INSTANCE.isTokenGoingOutOfDate() && !UserUtils.INSTANCE.isTokenOutOfDate()) {
                            Intent intent = new Intent();
                            intent.setAction(Constant.REGETTOKEN);
                            sendBroadcast(intent);
                        } else */
                        if (UserUtils.INSTANCE.isTokenOutOfDate()) {
                            Intent intent = new Intent();
                            intent.setAction(Constant.RELOGINACTION);
                            sendBroadcast(intent);
                            IntentUtils.INSTANCE.stopServcie(UIUtils.getContext());
                            compositeDisposable.dispose();
                        }
                    }
                }));
        LogPrinter.i("UmsService", "addLoginTimeObserver...........");
    }
}
