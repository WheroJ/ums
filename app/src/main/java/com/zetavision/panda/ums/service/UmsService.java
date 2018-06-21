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
import com.zetavision.panda.ums.model.SopMap;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class UmsService extends Service {

    private final String TAG = "UmsService";
    private List<FormInfo> downloadList;   // 当前下载列表
    private List<FormInfoDetail> uploadList;   // 当前上传列表

    /**
     * 记录所有上传表单的utils
     */
    private HashMap<String, UploadUtils> uploadUtilsMap;

    /**
     * 记录所有 表单的下载状态
     */
    private HashMap<String, Integer> downloadStatusMap;
    private OnDownloadListener downloadListener; // 下载监听
    private OnUploadListener uploadListener; // 上传监听

    private HashMap<String, CompositeDisposable> disposableHashMap;

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

    public void startUpload(final String formId) {
        final FormInfoDetail formInfoDetail = getFormInfoDetail(formId);

        if (formInfoDetail != null && Constant.FORM_STATUS_COMPLETED.equals(formInfoDetail.form.getStatus())) {
            if (setUploadStatus2Progress(formInfoDetail)) return;

            if (formInfoDetail.formItemList != null) {
                ArrayList<String> photoPaths = new ArrayList<>();
                for (int i = 0; i < formInfoDetail.formItemList.size(); i++) {
                    FormItem item = formInfoDetail.formItemList.get(i);
                    if (item.photoPaths != null && !item.photoPaths.isEmpty()) {
                        photoPaths.addAll(item.photoPaths);
                    }
                }

                if (!photoPaths.isEmpty()) {
                    final UploadUtils uploadUtils = new UploadUtils();
                    uploadUtils.startUpload();

                    uploadUtils.uploadImageAndVideo(new UploadUtils.UploadListener() {
                        @Override
                        public void onResult(@NotNull Result result) {
                            super.onResult(result);
                            uploadUtilsMap.remove(formId);
                            if (formInfoDetail.isUpload == FormInfo.PROGRESS) {
                                parseUrlMap(result, formInfoDetail);
                                upload(formInfoDetail);
                            }
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            super.onError(e);
                            disposableHashMap.remove(formId);
                            uploadUtilsMap.remove(formId);
                            formInfoDetail.isUpload = FormInfo.FAIL;     // 下载失败
                            if (uploadListener != null) {
                                uploadListener.onUpdate(uploadList);
                            }
                            if (uploadUtils.getIsStopUpload()) {
                                ToastUtils.show(e.getMessage());
                            }
                        }

                        @Override
                        public void onStart(@NotNull Disposable d) {
                            super.onStart(d);
                            CompositeDisposable compositeDisposable = new CompositeDisposable();
                            compositeDisposable.add(d);
                            if (disposableHashMap == null) {
                                disposableHashMap = new HashMap<>();
                            }
                            disposableHashMap.put(formId, compositeDisposable);
                            if (uploadUtilsMap == null) {
                                uploadUtilsMap = new HashMap<>();
                            }
                            uploadUtilsMap.put(formId, uploadUtils);
                        }
                    }, photoPaths, formInfoDetail.form.getFormCode());
                } else {
                    upload(formInfoDetail);
                }
            }
        }
    }

    private boolean setUploadStatus2Progress(FormInfoDetail formInfoDetail) {
        if (formInfoDetail.isUpload == FormInfo.DONE
                || formInfoDetail.isUpload == FormInfo.PROGRESS)
            return true;
        else formInfoDetail.isUpload = FormInfo.PROGRESS;
        if (uploadListener != null) uploadListener.onUpdate(uploadList);
        return false;
    }

    private void upload(final FormInfoDetail formInfoDetail) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
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
                    formItem.put("isStartingUp", item.isStartingUp);

                    if (FormInfo.ACTION_TYPE_P.equals(formInfoDetail.actionType)
                            && item.photoUrls != null
                            && !item.photoUrls.isEmpty()) {
                        JSONArray photoUrls = new JSONArray();
                        JSONArray videoUrls = new JSONArray();
                        for (int n = 0; n < item.photoUrls.size(); n++) {
                            String url = item.photoUrls.get(n);
                            int lastIndexOf = url.lastIndexOf(".");
                            if ("JPG".equalsIgnoreCase(url.substring(lastIndexOf + 1))
                                    || "PNG".equalsIgnoreCase(url.substring(lastIndexOf + 1))) {
                                photoUrls.put(url);
                            } else {
                                videoUrls.put(url);
                            }
                        }
                        formItem.put("photoUrls", photoUrls);
                        formItem.put("videoUrls", videoUrls);
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


        if (uploadListener != null)
            uploadListener.onUpdate(uploadList);

        RequestBody requestBody = null;
        try {
            JSONObject formsJson = new JSONObject();
            formsJson.put("forms", forms);
            requestBody = RequestBody.create(MediaType.parse("application/json"), forms.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Observable<ResponseBody> observable = Client.getApi(UmsApi.class).uploadForm(requestBody);
        RxUtils.INSTANCE.acquireString(observable, new RxUtils.DialogListener(){
            @Override
            public void onResult(@NotNull Result result) {
                try {
                    disposableHashMap.remove(formInfoDetail.formId);
                    if (formInfoDetail.isUpload == FormInfo.PROGRESS) {
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

                            String where;
                            if (FormInfo.ACTION_TYPE_M.equals(formInfoDetail.actionType)) {
                                where = "flowCode = '" + formInfoDetail.form.maintFlowCode + "'";
                            } else {
                                where = "flowCode = '" + formInfoDetail.form.inspectFlowCode + "'";
                            }
                            SopMap sopMap = DataSupport.where(where).findFirst(SopMap.class);
                            if (sopMap != null) {
                                --sopMap.useCount;
                                if (sopMap.useCount == 0) {
                                    sopMap.delete();
                                } else sopMap.updateAll(where);
                            }

                            formInfoDetail.delete();
                            if (uploadListener != null) {
                                uploadListener.onUpdate(uploadList);
                            }
                            EventBus.getDefault().post(Constant.UPDATE_WAIT_UPLOAD_COUNT);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(@NotNull Throwable e) {
                super.onError(e);
                disposableHashMap.remove(formInfoDetail.formId);
                formInfoDetail.isUpload = FormInfo.FAIL;     // 下载失败
                if (uploadListener != null) {
                    uploadListener.onUpdate(uploadList);
                }
            }

            @Override
            public void onStart(@NotNull Disposable d) {
                super.onStart(d);
                CompositeDisposable disposable = new CompositeDisposable();
                disposable.add(d);
                if (disposableHashMap == null) {
                    disposableHashMap = new HashMap<>();
                }
                disposableHashMap.put(formInfoDetail.formId, disposable);
            }
        });
    }

    private void parseUrlMap(@NotNull Result result, FormInfoDetail formInfoDetail) {
        try {
            JSONObject jsonObject = new JSONObject(result.getReturnData());
            JSONObject urlMap = jsonObject.optJSONObject("urlMap");
            ArrayList<Integer> changeIndexList = new ArrayList<>();
            for (int i = 0; i < formInfoDetail.formItemList.size(); i++) {
                FormItem formItem = formInfoDetail.formItemList.get(i);
                if (formItem.photoPaths != null) {
                    ArrayList<String> copyPaths = new ArrayList<>();
                    copyPaths.addAll(formItem.photoPaths);
                    for (int j = 0; j < copyPaths.size(); j++) {
                        String path = copyPaths.get(j);
                        if (!TextUtils.isEmpty(path)) {
                            int indexOf = path.lastIndexOf(File.separator);
                            if (indexOf != -1) {
                                String fileName = path.substring(indexOf + 1);
                                String url = urlMap.optString(fileName);
                                if (!TextUtils.isEmpty(url)) {
                                    if (formItem.photoUrls == null)
                                        formItem.photoUrls = new ArrayList<>();
                                    formItem.photoUrls.add(url);
                                    formItem.photoPaths.remove(path);
                                    if (!changeIndexList.contains(i))
                                        changeIndexList.add(i);
                                }
                            }
                        }
                    }
                }
            }

            for (int index: changeIndexList) {
                FormItem formItem = formInfoDetail.formItemList.get(index);
                formItem.saveOrUpdate("formItemId= '" + formItem.formItemId + "'");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载单个表单
     * @param formId 表单id
     */
    public void startDownload(final String formId) {
        final FormInfo info = getFormInfo(formId);

        FormInfoDetail formInfoDetail = DataSupport.where("formId='" + info.getFormId() + "' and actionType='" + info.getActionType() + "'").findFirst(FormInfoDetail.class, true);
        if (!judgeIsDown(formInfoDetail)) {
            setStatus2Progress(info);

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
                            if (downloadStatusMap != null) {
                                Integer savedFormId = downloadStatusMap.get(formId);
                                if (savedFormId != null &&  savedFormId == FormInfo.PROGRESS) {
                                    List<FormInfoDetail> formInfoDetails = result.getList(FormInfoDetail.class);
                                    if (formInfoDetails != null && !formInfoDetails.isEmpty()) {
                                        final FormInfoDetail formInfoDetail = formInfoDetails.get(0);
                                        formInfoDetail.formId = formInfoDetail.form.getFormId();
                                        formInfoDetail.equipmentCode = formInfoDetail.form.getEquipmentCode();
                                        formInfoDetail.inspectRouteCode = formInfoDetail.form.getInspectRouteCode();
                                        formInfoDetail.actionType = formInfoDetail.form.getActionType();
                                        formInfoDetail.utilitySystemId = formInfoDetail.form.getUtilitySystemId();

//                                    LogPrinter.i(TAG, "startDownload formId=" + formId + ", equipmentCode = " + formInfoDetail.formItemList.get(0).equipmentCode + "+++++++++++++++++++download done");

                                        saveAndUpdateStatus(formInfoDetail, info, true, FormInfo.DONE, -1);
                                        startDownloadSop(formInfoDetail);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            super.onError(e);
                            updateSingleDownStatus(info, FormInfo.FAIL);
                            changeDownloadStatusMap(DELETE, formId, -1);
                        }

                        @Override
                        public void onStart(@NotNull Disposable d) {
                            super.onStart(d);
                            CompositeDisposable disposable = new CompositeDisposable();
                            disposable.add(d);
                            if (disposableHashMap == null)
                                disposableHashMap = new HashMap<>();
                            disposableHashMap.put(info.getFormId(), disposable);
                            changeDownloadStatusMap(ADD, formId, info.getDownload_status());
                        }
                    });
        } else {
            if (!TextUtils.isEmpty(formInfoDetail.form.sopUrl)) {
                if (TextUtils.isEmpty(formInfoDetail.form.sopLocalPath)) {
                    startDownloadSop(formId);
                }
            } else {
                updateSingleDownStatus(info, FormInfo.DONE);
            }
        }
    }

    private void updateSingleDownStatus(FormInfo info, int status) {
        info.setDownload_status(status);
        if (downloadListener != null) {
            downloadListener.onUpdate(downloadList);
        }
    }

    private void setStatus2Progress(FormInfo info) {
        //已经下载过的表单无需再下载
        if (info.getDownload_status() == FormInfo.DONE
                || info.getDownload_status() == FormInfo.PROGRESS)
            return;
        else info.setDownload_status(FormInfo.PROGRESS);     // 开始下载

        if (downloadListener != null) downloadListener.onUpdate(downloadList);
    }

    /**
     * 是否下载过
     * @param formInfoDetail 表单详情
     * @return 返回是否已经在本地下载
     */
    private boolean judgeIsDown(FormInfoDetail formInfoDetail) {
        return !(formInfoDetail == null || formInfoDetail.formItemList == null || formInfoDetail.formItemList.isEmpty() || formInfoDetail.form == null);
    }

    private void saveAndUpdateStatus(final FormInfoDetail formInfoDetail, final FormInfo info
            , final boolean saveItems, final int infoStatus, final int sopStatus) {
        System.out.println("saveAndUpdateStatus begin start infoStatus=" + infoStatus + ", sopStatus = " + sopStatus);
        Observable.create(new ObservableOnSubscribe<List<FormInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<FormInfo>> emitter) throws Exception {
                System.out.println("saveAndUpdateStatus formId=" + info.getFormId() + " =============update formInfo status = " + getFormInfo(formInfoDetail.formId).getDownload_status());

                ThreadLocal<Integer> infoStatusLocal = new ThreadLocal<>();
                infoStatusLocal.set(infoStatus);
                ThreadLocal<Integer> sopStatusLocal = new ThreadLocal<>();
                sopStatusLocal.set(sopStatus);

                if (saveItems) {
                    for (FormItem formItem : formInfoDetail.formItemList) {
                        Integer status = downloadStatusMap.get(formInfoDetail.formId);
                        if (status != null) {
                            if (status == FormInfo.PROGRESS) {
                                formItem.saveOrUpdate("formItemId= '" + formItem.formItemId + "'");
                            } else if (status == FormInfo.WAIT){
                                if (formInfoDetail.form.getDownload_status() != FormInfo.DONE) {
                                    getFormInfo(formInfoDetail.formId).setDownload_status(FormInfo.WAIT);
                                    deleteData(formInfoDetail);
//                                    changeDownloadStatusMap(DELETE, formInfoDetail.formId, -1);
                                    emitter.onNext(downloadList);
                                    System.out.println("saveAndUpdateStatus formId=" + info.getFormId() + " =============update return status = " + getFormInfo(formInfoDetail.formId).getDownload_status());
                                    return;
                                }
                            }
                        }
                    }
                }
                if (infoStatus != -1) formInfoDetail.form.setDownload_status(infoStatusLocal.get());
                if (sopStatus != -1) formInfoDetail.form.sop_download_status = sopStatusLocal.get();
                formInfoDetail.form.saveOrUpdate("(formId='" + formInfoDetail.form.getFormId() + "')");
                formInfoDetail.saveOrUpdate("(formId='" + formInfoDetail.formId + "')");

                if (infoStatus != -1) info.setDownload_status(infoStatusLocal.get());
                if (sopStatus != -1) info.sop_download_status = sopStatusLocal.get();

                if (infoStatusLocal.get() == FormInfo.DONE) {
                    //表单 已经下载  更新首页显示
                    EventBus.getDefault().post(Constant.UPDATE_DOWN_COUNT);
                    changeDownloadStatusMap(DELETE, formInfoDetail.formId, -1);
                    System.out.println("saveAndUpdateStatus formId=" + info.getFormId() + " =============update onNext status = " + getFormInfo(formInfoDetail.formId).getDownload_status());
                    emitter.onNext(downloadList);
                } else {
                    if (infoStatusLocal.get() != -1) {
                        changeDownloadStatusMap(CHANGE, formInfoDetail.formId, infoStatusLocal.get());
                    }
                }

                System.out.println("saveAndUpdateStatus formId=" + info.getFormId() + " =============update finish formInfo status = " + getFormInfo(formInfoDetail.formId).getDownload_status());
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<FormInfo>>() {
            @Override
            public void accept(List<FormInfo> formInfos) throws Exception {
                if (downloadListener != null) {
                    downloadListener.onUpdate(formInfos);
                    LogPrinter.i(TAG, "saveAndUpdateStatus done~~~~~~~~~~~~~~~~~~ \n");
//                    StringBuilder builder = new StringBuilder();
//                    for (FormInfo formInfo : formInfos) {
//                        builder.append("formId = ").append(formInfo.getFormId()).append(", downloadStatus = ").append(formInfo.getDownload_status()).append(", sopDownloadStatus = ").append(formInfo.sop_download_status);
//                        builder.append("\n");
//                    }
//                    LogPrinter.i(TAG, "saveAndUpdateStatus done~~~~~~~~~~~~~~~~~~ \n"  + builder.toString());
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        }, new Action() {
            @Override
            public void run() throws Exception {

            }
        }, new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                CompositeDisposable compositeDisposable = new CompositeDisposable();
                compositeDisposable.add(disposable);
                disposableHashMap.put(info.getFormId(), compositeDisposable);
            }
        });
    }

    /**
     * 删除已下载数据
     * @param formInfoDetail 需要删除的表单详情
     */
    private void deleteData(FormInfoDetail formInfoDetail) {
        formInfoDetail.delete();
        formInfoDetail.form.delete();
        for (FormItem item: formInfoDetail.formItemList) {
            item.delete();
        }
    }

    /**
     * 下载全部表单  当将值设置为-1时，标明为停止下载
     */
    private static int downIndex = 0;
    public void startDownloadAll() {
        if (downloadList == null) return;

        downIndex = 0;
        if (downloadStatusMap != null) downloadStatusMap.clear();

        final CompositeDisposable disposables = new CompositeDisposable();
        disposables.add(Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<Long>() {
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        LogPrinter.i(TAG, "interval time seconds : " + aLong);
                        if (downloadStatusMap != null) {
                            int downingCount = 0;
                            for (String formId : downloadStatusMap.keySet()) {
                                if (downloadStatusMap.get(formId) == FormInfo.PROGRESS)
                                    downingCount++;
                            }
                            if (downIndex != -1) {
                                if (downingCount >= Constant.MAX_CONCURRENT) {
                                    return false;
                                } else if (downIndex >= downloadList.size()) {
                                    // 已经下载 所有item
                                    disposables.dispose();
                                    compositeDisposable.remove(disposables);
                                    return false;
                                }
                            } else {
                                // 点击了全部停止下载按钮
                                disposables.dispose();
                                compositeDisposable.remove(disposables);
                                return false;
                            }
                        }
                        return true;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (downIndex < downloadList.size()) {
                            Flowable.just(downloadList.get(downIndex))
                                    .filter(new Predicate<FormInfo>() {           //过滤下载完成、正在下载项
                                        @Override
                                        public boolean test(FormInfo info) throws Exception {
                                            if (downIndex != -1) {
                                                downIndex = downloadList.indexOf(info) + 1;
                                            }
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
                    }
                }));
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposables);
    }


    private static int uploadIndex = 0;
    public void startUploadAll() {
        if (uploadList == null || uploadList.isEmpty()) return;

        uploadIndex = 0;
        final CompositeDisposable disposables = new CompositeDisposable();
        disposables.add(Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<Long>() {
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        LogPrinter.i(TAG, "uploading interval time seconds : " + aLong + ", uploadIndex = " + uploadIndex);
                        int uploadingCount = 0;

                        for (FormInfoDetail formInfoDetail : uploadList) {
                            if (formInfoDetail.isUpload == FormInfo.PROGRESS)
                                uploadingCount++;
                        }
                        if (uploadIndex != -1) {
                            if (uploadingCount >= Constant.MAX_CONCURRENT) {
                                return false;
                            } else if (uploadIndex >= uploadList.size()) {
                                // 已经下载 所有item
                                disposables.dispose();
                                compositeDisposable.remove(disposables);
                                return false;
                            } else return true;
                        } else {
                            // 点击了全部停止下载按钮
                            disposables.dispose();
                            compositeDisposable.remove(disposables);
                            return false;
                        }
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (uploadIndex < uploadList.size()) {
                            Flowable.just(uploadList.get(uploadIndex))
                                    .filter(new Predicate<FormInfoDetail>() {           //过滤下载完成、正在下载项
                                        @Override
                                        public boolean test(FormInfoDetail info) throws Exception {
                                            if (uploadIndex != -1) {
                                                uploadIndex = uploadList.indexOf(info) + 1;
                                            }
                                            System.out.println("upload info.isUpload = " + info.isUpload);
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
                    }
                }));
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposables);
    }

    private final int CHANGE = 1;
    private final int ADD = 2;
    private final int DELETE = 3;
    private void changeDownloadStatusMap(int operate, String formId, int status) {
        synchronized (this) {
            switch (operate) {
                case ADD:
                    if (downloadStatusMap == null)
                        downloadStatusMap = new HashMap<>();
                case CHANGE:
                    if (downloadStatusMap != null)
                        downloadStatusMap.put(formId, status);
                    break;
                case DELETE:
                    if (downloadStatusMap != null)
                        downloadStatusMap.remove(formId);
                    break;
            }
        }
    }

    /**
     * 停止下载单个表单
     * @param formId 表单id
     */
    public void stopDownload(String formId) {
        CompositeDisposable disposable = disposableHashMap.get(formId);
        if (disposable != null) {
            disposable.dispose();
            disposableHashMap.remove(formId);
        }

        Integer status = downloadStatusMap.get(formId);
        FormInfo formInfo = getFormInfo(formId);
        if (status != null && status == FormInfo.PROGRESS && formInfo.getDownload_status() == FormInfo.PROGRESS) {
            changeDownloadStatusMap(CHANGE, formId, FormInfo.WAIT);
            formInfo.setDownload_status(FormInfo.WAIT);
            if (downloadListener != null)
                downloadListener.onUpdate(downloadList);
        }
    }

    public void stopUpload(String formId) {
        CompositeDisposable disposable = disposableHashMap.get(formId);
        if (disposable != null) {
            disposable.dispose();
            disposableHashMap.remove(formId);
        }

        FormInfoDetail formInfoDetail = getFormInfoDetail(formId);
        if (formInfoDetail != null) {
            if (formInfoDetail.isUpload == FormInfo.PROGRESS) {
                formInfoDetail.isUpload = FormInfo.WAIT;
            }
            if (downloadListener != null)
                uploadListener.onUpdate(uploadList);
        }
        if (uploadUtilsMap != null) {
            UploadUtils uploadUtils = uploadUtilsMap.get(formId);
            if (uploadUtils != null) {
                uploadUtils.stopUpload();
            }
        }
    }

    public void stopUploadAll() {
        uploadIndex = -1;
        if (uploadList != null) {
            for (FormInfoDetail formInfoDetail : uploadList) {
                if (formInfoDetail.isUpload == FormInfo.PROGRESS) {
                    stopUpload(formInfoDetail.formId);
                }
            }
        }
    }

    /**
     * 停止下载所有表单
     */
    public void stopDownloadAll() {
        if (downloadStatusMap != null) {
            downIndex = -1;
            for (String formId : downloadStatusMap.keySet()) {
                stopDownload(formId);
            }
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

    public void startDownloadSop(final FormInfoDetail formInfoDetail) {

        if (formInfoDetail == null) return;
        final FormInfo formInfo = getFormInfo(formInfoDetail.formId);
//        formInfoDetail.form.getDownload_status() == FormInfo.DONE
        if (TextUtils.isEmpty(formInfoDetail.form.sopUrl)) {
            saveAndUpdateStatus(formInfoDetail, formInfo, false, -1, -1);
        } else {

            final String flowCode;
            if (FormInfo.ACTION_TYPE_M.equals(formInfo.getActionType())) {
                flowCode = formInfo.maintFlowCode;
            } else {
                flowCode = formInfo.inspectFlowCode;
            }

            String where = "flowCode = '" + flowCode + "'";
            SopMap sopMap = DataSupport.where(where).findFirst(SopMap.class);

            if (sopMap != null) {
                formInfoDetail.form.sopLocalPath = sopMap.sopLocalPath;
                sopMap.useCount++;
                sopMap.updateAll(where);

                saveAndUpdateStatus(formInfoDetail, formInfo, false, -1, FormInfo.DONE);
            } else {
                if (setSopDownStatus2Progress(formInfo)) return;
                File saveFile = new File(UIUtils.getCachePath(), formInfoDetail.form.sopFileName);
                try {
                    if (!saveFile.exists()) {
                        saveFile.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (saveFile.exists()) {
                        Observable<ResponseBody> downLoadObserve = Client.getApi(UmsApi.class).downloadFile(formInfoDetail.form.sopUrl
                                , formInfoDetail.form.sopFileName, "Y");
                        RxUtils.INSTANCE.download(downLoadObserve, saveFile, new RxUtils.ProgressListener() {

                            @Override
                            public void onStart(@NotNull Disposable d) {
                                super.onStart(d);
                                CompositeDisposable compositeDisposable = new CompositeDisposable();
                                compositeDisposable.add(d);
                                disposableHashMap.put(formInfo.sopUrl, compositeDisposable);
                            }

                            @Override
                            public void onResult(@NotNull Result result) {
                                Disposable disposable;
                                if (disposableHashMap != null && (disposable = disposableHashMap.get(formInfo.sopUrl)) != null) {
                                    if (!disposable.isDisposed()) {
                                        // 保存完改变状态
                                        formInfoDetail.form.sopLocalPath = result.getReturnData();

                                        SopMap map = new SopMap();
                                        map.useCount = 1;
                                        map.sopLocalPath = result.getReturnData();
                                        map.flowCode = flowCode;
                                        map.actonCode = formInfoDetail.actionType;
                                        map.save();

                                        saveAndUpdateStatus(formInfoDetail, formInfo, false, -1, FormInfo.DONE);
                                    }
                                }
                            }

                            @Override
                            public void onUpdate(float progress) {
                            }

                            @Override
                            public void onError(@NotNull Throwable e) {
                                super.onError(e);
                                formInfo.sop_download_status = FormInfo.FAIL;
                                if (downloadListener != null) {
                                    downloadListener.onUpdate(downloadList);
                                }
                            }
                        });
                    } else {
                        stopDownloadSop(formInfo.getFormId(), formInfo.sopUrl);
                        ToastUtils.show(getString(R.string.data_exception));
                    }
                }
            }
        }
    }

    /**
     * 设置sop的下载状态为“正在下载”
     * @param formInfo 表单信息
     * @return   是否已经下载过
     */
    private boolean setSopDownStatus2Progress(FormInfo formInfo) {
        //已经下载过的表单无需再下载
        if (formInfo.sop_download_status == FormInfo.DONE
                || formInfo.sop_download_status == FormInfo.PROGRESS)
            return true;
        else formInfo.sop_download_status = FormInfo.PROGRESS;     // 开始下载

        if (downloadListener != null) downloadListener.onUpdate(downloadList);
        return false;
    }

    public void startDownloadSop(String formId) {
        final FormInfoDetail formInfoDetail = DataSupport.where("(formId = '" + formId + "')").findFirst(FormInfoDetail.class, true);
        startDownloadSop(formInfoDetail);
    }

    public void stopDownloadSop(String formId, String sopUrl) {
        CompositeDisposable disposable = disposableHashMap.get(sopUrl);
        if (disposable != null) {
            disposable.dispose();
            disposableHashMap.remove(formId);
        }

        FormInfo formInfo = getFormInfo(formId);
        if (formInfo != null) {
            if (formInfo.sop_download_status == FormInfo.PROGRESS) {
                formInfo.sop_download_status = FormInfo.WAIT;
            }
            if (downloadListener != null)
                downloadListener.onUpdate(downloadList);
        }
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
        if (compositeDisposable != null)
            compositeDisposable.dispose();
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
