package com.zetavision.panda.ums.ui.formup;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.zetavision.panda.ums.ui.MainActivity;
import com.zetavision.panda.ums.utils.Constant;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.LoadingDialog;
import com.zetavision.panda.ums.utils.NetUtils;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.network.Client;
import com.zetavision.panda.ums.utils.network.UmsApi;

import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
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
    @BindView(R.id.progressText)
    TextView progressText;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private boolean uploadAll = false;
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
            uploadAll = true;
            umsService.startUploadAll();
        }
    }

    @Override
    protected void init() {
        getHeader().setTitle(getString(R.string.form_upload));

        systemSpinnerAdapter = new SystemSpinnerAdapter(getContext());
        actionSpinnerAdapter = new ActionSpinnerAdapter(getContext());
        systemSpinner.setAdapter(systemSpinnerAdapter);
        actionSpinner.setAdapter(actionSpinnerAdapter);
        uploadAdapter = new UploadAdapter(getContext(), this);
        listView.setAdapter(uploadAdapter);

        // 绑定service
        IntentUtils.INSTANCE.bindService(getActivity(), connection);
        getData();
    }


    @OnClick(R.id.search)
    void onSearch() {
        SystemInfo systemInfo = (SystemInfo) systemSpinner.getSelectedItem();
        ActionInfo actionInfo = (ActionInfo) actionSpinner.getSelectedItem();

        if (systemInfo != null && actionInfo != null) {
            List<FormInfoDetail> formInfoDetails = DataSupport.findAll(FormInfoDetail.class, false);

            ArrayList<FormInfoDetail> uploadList = new ArrayList<>();
            for (int i = 0; i < formInfoDetails.size(); i++) {
                FormInfoDetail formInfoDetail = formInfoDetails.get(i);
                if (systemInfo.getUtilitySystemId() == -1 && TextUtils.isEmpty(actionInfo.getActionType())) {
                    if (formInfoDetail.isUpload == FormInfo.WAIT) {
                        findFormInfoDetail(uploadList, formInfoDetail.formId);
                    }
                } else if (systemInfo.getUtilitySystemId() == -1) {
                    if (formInfoDetail.actionType.equals(actionInfo.getActionType())
                            && formInfoDetail.isUpload == FormInfo.WAIT) {
                        findFormInfoDetail(uploadList, formInfoDetail.formId);
                    }
                } else if (TextUtils.isEmpty(actionInfo.getActionType())) {
                    if (formInfoDetail.utilitySystemId == systemInfo.getUtilitySystemId()
                            && formInfoDetail.isUpload == FormInfo.WAIT) {
                        findFormInfoDetail(uploadList, formInfoDetail.formId);
                    }
                } else {
                    if (formInfoDetail.utilitySystemId == systemInfo.getUtilitySystemId()
                            && formInfoDetail.actionType.equals(actionInfo.getActionType())
                            && formInfoDetail.isUpload == FormInfo.WAIT) {
                        findFormInfoDetail(uploadList, formInfoDetail.formId);
                    }
                }
            }

            if (uploadList.isEmpty()) {
                umsService.setUploadList(new ArrayList<FormInfoDetail>());
                ToastUtils.show(R.string.no_data);
            } else {
                umsService.setUploadList(uploadList);
            }
        }
    }

    private void findFormInfoDetail(ArrayList<FormInfoDetail> uploadList, String formId) {
        FormInfoDetail infoDetail = DataSupport.where("formId='" + formId + "'").findFirst(FormInfoDetail.class, true);
        if (infoDetail.form.getStatus().equals(Constant.FORM_STATUS_COMPLETED))
            uploadList.add(infoDetail);
    }

    private void getData() {
        if (!NetUtils.INSTANCE.isNetConnect(getContext())) {
            List<SystemInfo> systemInfos = DataSupport.findAll(SystemInfo.class);
            addFirstSystemItem(systemInfos);
            List<ActionInfo> actions = DataSupport.findAll(ActionInfo.class);
            addFirstActionItem(actions);
            systemSpinnerAdapter.notifyDataSetChanged(systemInfos);
            actionSpinnerAdapter.notifyDataSetChanged(actions);
        } else {
            final LoadingDialog dialog = new LoadingDialog();
            dialog.show(getFragmentManager(), null);

            Observable<Result> queryUtilitySystem = Client.getApi(UmsApi.class).queryUtilitySystem().map(new Function<ResponseBody, Result>() {
                @Override
                public Result apply(ResponseBody responseBody) throws Exception {
                    JSONObject resultObject = new JSONObject(responseBody.string());
                    Result result = new Result();
                    result.setReturnCode(resultObject.optString("returnCode"));
                    result.setReturnMessage(resultObject.optString("returnMessage"));
                    result.setReturnData(resultObject.optString("returnData"));
                    return result;
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

            Observable<Result> queryActionType = Client.getApi(UmsApi.class).queryActionType().map(new Function<ResponseBody, Result>() {
                @Override
                public Result apply(ResponseBody responseBody) throws Exception {
                    JSONObject resultObject = new JSONObject(responseBody.string());
                    Result result = new Result();
                    result.setReturnCode(resultObject.optString("returnCode"));
                    result.setReturnMessage(resultObject.optString("returnMessage"));
                    result.setReturnData(resultObject.optString("returnData"));
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
                    if (systemInfos != null) {
                        for (int i = 0; i < systemInfos.size(); i++) {
                            systemInfos.get(i).saveOrUpdate("utilitySystemId=" + systemInfos.get(i).getUtilitySystemId());
                        }
                    }
                    addFirstSystemItem(systemInfos);
                    systemSpinnerAdapter.notifyDataSetChanged(systemInfos);

                    List<ActionInfo> actions = hashMap.get("Action").getList(ActionInfo.class);
                    if (actions != null) {
                        for (int i = 0; i < actions.size(); i++) {
                            actions.get(i).saveOrUpdate("actionType='" + actions.get(i).getActionType() + "'");
                        }
                    }
                    addFirstActionItem(actions);
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
    }

    private void addFirstActionItem(List<ActionInfo> actions) {
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setActionType("");
        actionInfo.setDescription(getString(R.string.all));
        actions.add(0, actionInfo);
    }

    private void addFirstSystemItem(List<SystemInfo> systemInfos) {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setUtilitySystemId(-1);
        systemInfo.setUtilitySystemCode("");
        systemInfo.setUtilitySystemName(getString(R.string.all));
        systemInfos.add(0, systemInfo);
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
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).updateUnUploadCount();
                        }
                        if (uploadAll) {
                            int count = 0, size = list.size();
                            for (int i = 0; i < size; i++) {
                                if (list.get(i).isUpload == FormInfo.DONE) {
                                    count++;
                                }
                            }

                            if (count == list.size()) uploadAll = false;
                            double ratio = count * 100.0 / list.size();
                            progressBar.setProgress((int) (ratio / 100));
                            progressText.setText(getString(R.string.have_finish) + ratio + "%");
                        }
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
