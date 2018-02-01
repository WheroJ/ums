package com.zetavision.panda.ums.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.zetavision.panda.ums.model.FormInfo;
import com.zetavision.panda.ums.model.FormInfoDetail;
import com.zetavision.panda.ums.model.FormItem;
import com.zetavision.panda.ums.model.Result;
import com.zetavision.panda.ums.utils.Constant;
import com.zetavision.panda.ums.utils.network.Client;
import com.zetavision.panda.ums.utils.network.RxUtils;
import com.zetavision.panda.ums.utils.network.UmsApi;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import okhttp3.MediaType;
import okhttp3.RequestBody;
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

        if (formInfoDetail != null && Constant.MAINT_FORM_STATUS_COMPLETED.equals(formInfoDetail.form.getStatus())) {
            JSONObject requestJson = new JSONObject();
            try {
                JSONArray forms = new JSONArray();
                JSONObject formDetail = new JSONObject();

                JSONObject form = new JSONObject();
                form.put("formId", formInfoDetail.form.getFormId());
                form.put("formCode", formInfoDetail.form.getFormCode());
                form.put("actionType", formInfoDetail.form.getActionType());
                form.put("weather", formInfoDetail.form.weather);
                form.put("shift", formInfoDetail.form.shift);
                form.put("startUser", formInfoDetail.form.startUser);
                form.put("startTime", formInfoDetail.form.startTime);
                form.put("completeUser", formInfoDetail.form.completeUser);
                form.put("completeTime", formInfoDetail.form.completeTime);
                form.put("fillinRemarks", formInfoDetail.form.fillinRemarks);

                JSONArray formItemList = new JSONArray();
                if (formInfoDetail.formItemList != null) {
                    for (int i = 0; i < formInfoDetail.formItemList.size(); i++) {
                        JSONObject formItem = new JSONObject();
                        FormItem item = formInfoDetail.formItemList.get(i);
                        formItem.put("formId", item.formId);
                        formItem.put("formItemId", item.formItemId);
                        formItem.put("result", item.presetValue);
                        formItem.put("remarks", item.remarks);
                        formItemList.put(formItem);
                    }
                }

                formDetail.put("form", form);
                formDetail.put("formItemList", formItemList);
                forms.put(formDetail);
                requestJson.put("forms", forms);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            if (formInfoDetail.isUpload == FormInfo.DONE
                    || formInfoDetail.isUpload == FormInfo.PROGRESS)
                return;
            else formInfoDetail.isUpload = FormInfo.PROGRESS;

            if (uploadListener != null) uploadListener.onUpdate(uploadList);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestJson.toString());
            Observable<ResponseBody> observable = Client.getApi(UmsApi.class).uploadForm(requestBody);
            RxUtils.INSTANCE.acquireString(observable, new RxUtils.DialogListener(){
                @Override
                public void onResult(@NotNull Result result) {
                    formInfoDetail.isUpload = FormInfo.DONE;
                    formInfoDetail.saveOrUpdate("(formId='" + formInfoDetail.formId + "')");
                    if (uploadListener != null) {
                        uploadListener.onUpdate(uploadList);
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
    }

    // 下载单个表单
    public void startDownload(String formId) {
        final FormInfo info = getFormInfo(formId);

        //已经下载过的表单无需再下载
        if (info.getDownload_status() == FormInfo.DONE
                || info.getDownload_status() == FormInfo.PROGRESS)
            return;
        else info.setDownload_status(FormInfo.PROGRESS);     // 开始下载

        if (downloadListener != null) downloadListener.onUpdate(downloadList);
        //是否下载过
        List<FormInfoDetail> formInfoDetails = DataSupport.where("formId='" + info.getFormId() + "'").find(FormInfoDetail.class);
        if (formInfoDetails == null || formInfoDetails.isEmpty()) {

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
        if (uploadList == null) return;
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

    public void stopUploadAll() {
        Iterator<String> iterator = disposableHashMap.keySet().iterator();
        while (iterator.hasNext()) {
            String formId = iterator.next();
            stopDownload(formId);
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
}
