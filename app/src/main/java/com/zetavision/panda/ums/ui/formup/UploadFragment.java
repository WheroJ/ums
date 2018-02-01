package com.zetavision.panda.ums.ui.formup;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.adapter.ActionSpinnerAdapter;
import com.zetavision.panda.ums.adapter.SystemSpinnerAdapter;
import com.zetavision.panda.ums.adapter.UploadAdapter;
import com.zetavision.panda.ums.fragments.base.BaseFragment;
import com.zetavision.panda.ums.model.ActionInfo;
import com.zetavision.panda.ums.model.FormInfo;
import com.zetavision.panda.ums.model.FormInfoDetail;
import com.zetavision.panda.ums.model.Result;
import com.zetavision.panda.ums.model.SystemInfo;
import com.zetavision.panda.ums.service.UmsService;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.LoadingDialog;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.network.Client;
import com.zetavision.panda.ums.utils.network.RxUtils;
import com.zetavision.panda.ums.utils.network.UmsApi;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class UploadFragment extends BaseFragment{

    @BindView(R.id.systemSpinner)
    Spinner systemSpinner;
    @BindView(R.id.actionSpinner)
    Spinner actionSpinner;
    @BindView(R.id.listView)
    ListView listView;

    private SystemSpinnerAdapter systemSpinnerAdapter;
    private ActionSpinnerAdapter actionSpinnerAdapter;
    private UploadAdapter uploadAdapter;

    @Override
    public int getContentLayoutId() {
        return R.layout.fragment_upload;
    }

    @OnClick(R.id.uploadAll)
    void uploadAll() {
        if (umsService != null) {
            umsService.startUploadAll();
        }
    }

    @Override
    protected void init() {
        getHeader().setTitle("表单下载");

        systemSpinnerAdapter = new SystemSpinnerAdapter(getContext());
        actionSpinnerAdapter = new ActionSpinnerAdapter(getContext());
        systemSpinner.setAdapter(systemSpinnerAdapter);
        actionSpinner.setAdapter(actionSpinnerAdapter);
        uploadAdapter = new UploadAdapter(getContext(), this);
        listView.setAdapter(uploadAdapter);

        // 绑定service
        IntentUtils.INSTANCE.startDownloadService(getActivity(), connection);
        getData();
    }


    //TODO 需要获取上传表单的逻辑
    @OnClick(R.id.search)
    void onSearch() {
        SystemInfo systemInfo = (SystemInfo) systemSpinner.getSelectedItem();
        ActionInfo actionInfo = (ActionInfo) actionSpinner.getSelectedItem();

        RxUtils.INSTANCE.acquireString(
                Client.getApi(UmsApi.class)
                        .queryPlannedForm(systemInfo.getUtilitySystemId(), actionInfo.getActionType())
                , new RxUtils.DialogListener() {
                    @Override
                    public void onResult(@NotNull Result result) {
                        if (umsService != null) {
                            // 设置下载列表
                            List<FormInfo> formInfos = result.getList(FormInfo.class);

                            if (formInfos != null && !formInfos.isEmpty()) {
                                for (int i = 0; i < formInfos.size(); i++) {
                                    FormInfo formInfo = formInfos.get(i);
                                    List<FormInfoDetail> formInfoDetails = DataSupport.where("formId='" + formInfo.getFormId() + "'").find(FormInfoDetail.class);
                                    if (formInfoDetails != null && !formInfoDetails.isEmpty()) {
                                        formInfo.setDownload_status(FormInfo.DONE);
                                        Spinner spinner = new Spinner(getContext());
                                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {

                                            }
                                        });
                                    }
                                }
                            }
                            umsService.setDownloadList(formInfos);
                        }
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        super.onError(e);
                        ToastUtils.show(e.getMessage());
                    }
                });
    }

    private void getData() {
        final LoadingDialog dialog = new LoadingDialog();
        dialog.show(getFragmentManager(), null);

        Observable<Result> queryUtilitySystem = Client.getApi(UmsApi.class).queryUtilitySystem().map(new Function<ResponseBody, Result>() {
            @Override
            public Result apply(ResponseBody responseBody) throws Exception {
                JSONObject resultObject = new JSONObject(responseBody.string());
                Result result = new Result();
                result.setReturnCode(resultObject.getInt("returnCode"));
                result.setReturnMessage(resultObject.getString("returnMessage"));
                result.setReturnData(resultObject.getString("returnData"));
                return result;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        Observable<Result> queryActionType = Client.getApi(UmsApi.class).queryActionType().map(new Function<ResponseBody, Result>() {
            @Override
            public Result apply(ResponseBody responseBody) throws Exception {
                JSONObject resultObject = new JSONObject(responseBody.string());
                Result result = new Result();
                result.setReturnCode(resultObject.getInt("returnCode"));
                result.setReturnMessage(resultObject.getString("returnMessage"));
                result.setReturnData(resultObject.getString("returnData"));
                return result;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        Observable.zip(queryUtilitySystem, queryActionType, new BiFunction<Result, Result, HashMap<String, Result>>() {
            @Override
            public HashMap<String, Result> apply(Result result, Result result2) throws Exception {
                HashMap<String, Result> hashMap = new HashMap<>();
                hashMap.put("SystemInfo", result);
                hashMap.put("Action", result2);
                return hashMap;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<HashMap<String, Result>>() {
            @Override
            public void accept(HashMap<String, Result> hashMap) throws Exception {
                List<SystemInfo> systemInfos = hashMap.get("SystemInfo").getList(SystemInfo.class);
                systemSpinnerAdapter.notifyDataSetChanged(systemInfos);
                List<ActionInfo> actions = hashMap.get("Action").getList(ActionInfo.class);
                actionSpinnerAdapter.notifyDataSetChanged(actions);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                dialog.dismiss();
                ToastUtils.show(throwable.getMessage());
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                dialog.dismiss();
            }
        });
    }

    public UmsService umsService;
    // service连接
    public ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UmsService.MyBinder binder = (UmsService.MyBinder) service;
            umsService = binder.getService();
            umsService.setUploadListener(new UmsService.OnUploadListener() {
                @Override
                public void onUpdate(List<FormInfoDetail> list) {
                    if (list != null) {
                        uploadAdapter.notifyDataSetChanged(list);
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //当Service服务被意外销毁时，
        }
    };

    @Override
    public void onDestroyView() {
        getActivity().unbindService(connection);
        super.onDestroyView();
    }

}
