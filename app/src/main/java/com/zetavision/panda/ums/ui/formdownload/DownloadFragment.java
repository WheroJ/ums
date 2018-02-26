package com.zetavision.panda.ums.ui.formdownload;

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
import com.zetavision.panda.ums.adapter.DownloadAdapter;
import com.zetavision.panda.ums.adapter.SystemSpinnerAdapter;
import com.zetavision.panda.ums.fragments.base.BaseFragment;
import com.zetavision.panda.ums.model.ActionInfo;
import com.zetavision.panda.ums.model.FormInfo;
import com.zetavision.panda.ums.model.Result;
import com.zetavision.panda.ums.model.SystemInfo;
import com.zetavision.panda.ums.service.UmsService;
import com.zetavision.panda.ums.utils.IntentUtils;
import com.zetavision.panda.ums.utils.LoadingDialog;
import com.zetavision.panda.ums.utils.NetUtils;
import com.zetavision.panda.ums.utils.TimeUtils;
import com.zetavision.panda.ums.utils.ToastUtils;
import com.zetavision.panda.ums.utils.network.Client;
import com.zetavision.panda.ums.utils.network.RxUtils;
import com.zetavision.panda.ums.utils.network.UmsApi;

import org.jetbrains.annotations.NotNull;
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

public class DownloadFragment extends BaseFragment {

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

    private boolean downLoadAll = false;
    private SystemSpinnerAdapter systemSpinnerAdapter;
    private ActionSpinnerAdapter actionSpinnerAdapter;
    private DownloadAdapter downLoadAdapter;

    @Override
    public int getContentLayoutId() {
        return R.layout.fragment_download;
    }

    @OnClick(R.id.downloadAll)
    void downloadAll() {
        if (umsService != null) {
            umsService.startDownloadAll();
            downLoadAll = true;
        }
    }

    @Override
    protected void init() {
        getHeader().setTitle("表单下载");

        systemSpinnerAdapter = new SystemSpinnerAdapter(getContext());
        actionSpinnerAdapter = new ActionSpinnerAdapter(getContext());
        systemSpinner.setAdapter(systemSpinnerAdapter);
        actionSpinner.setAdapter(actionSpinnerAdapter);
        downLoadAdapter = new DownloadAdapter(getContext(), this);
        listView.setAdapter(downLoadAdapter);

        // 绑定service
        IntentUtils.INSTANCE.bindService(getActivity(), connection);
        getData();
    }

    @OnClick(R.id.search)
    void onSearch() {
        final SystemInfo systemInfo = (SystemInfo) systemSpinner.getSelectedItem();
        final ActionInfo actionInfo = (ActionInfo) actionSpinner.getSelectedItem();

        if (!NetUtils.INSTANCE.isNetConnect(getContext())) {
            List<FormInfo> downloadList = loadLocalFormInfos(systemInfo, actionInfo);
            if (!downloadList.isEmpty()) {
                umsService.setDownloadList(downloadList);
            } else {
                ToastUtils.show(R.string.no_local_data);
            }
        } else {
            RxUtils.INSTANCE.acquireString(
                    Client.getApi(UmsApi.class)
                            .queryPlannedForm(systemInfo.getUtilitySystemId(), actionInfo.getActionType())
                    , new RxUtils.DialogListener() {
                        @Override
                        public void onResult(@NotNull Result result) {
                            if (umsService != null) {
                                // 设置下载列表
                                List<FormInfo> formInfos = result.getList(FormInfo.class);

                                List<FormInfo> downloadList = loadLocalFormInfos(systemInfo, actionInfo);

                                ArrayList<FormInfo> sortFormInfo = new ArrayList<>();

                                int unDownCount = 0;
                                if (formInfos != null && !formInfos.isEmpty()) {
                                    for (int i = 0; i < formInfos.size(); i++) {
                                        FormInfo formInfo = formInfos.get(i);
                                        int indexOf = downloadList.indexOf(formInfo);
                                        if (indexOf != -1) {
                                            formInfo.setStatus(downloadList.get(indexOf).getStatus());
                                            formInfo.setDownload_status(FormInfo.DONE);
                                            sortFormInfo.add(formInfo);
                                        } else {
                                            sortFormInfo.add(unDownCount, formInfo);
                                            unDownCount++;
                                        }
                                    }
                                } else {
                                    ToastUtils.show(R.string.no_data);
                                }
                                umsService.setDownloadList(sortFormInfo);
                            }
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            super.onError(e);
                            ToastUtils.show(e.getMessage());
                            loadLocalFormInfos(systemInfo, actionInfo);
                        }
                    });
        }
    }

    private  List<FormInfo> loadLocalFormInfos(SystemInfo systemInfo, ActionInfo actionInfo) {
        if (systemInfo != null && actionInfo != null && umsService != null) {
            String where = "(utilitySystemId='" + systemInfo.getUtilitySystemId() + "' and actionType='" + actionInfo.getActionType() + "') ";
            List<FormInfo> formInfos = DataSupport.where(where).find(FormInfo.class);
            return formInfos;
        }
        return null;
    }

    private void sortByPlanDate(ArrayList<FormInfo> sortFormInfo) {
//        planDate倒序  formCode升序
        int sortSize = 0;
        for (int i = 0; i < sortFormInfo.size(); i++) {
            if (sortFormInfo.get(i).getDownload_status() == FormInfo.DONE) {
                sortSize = i + 1;
                break;
            }
        }

        for (int i = 0; i < sortSize - 1; i++) {
            for (int j = i + 1; j < sortSize; j++) {
                FormInfo formInfo = sortFormInfo.get(i);
                FormInfo formInfoJ = sortFormInfo.get(j);
                long second = TimeUtils.INSTANCE.getSecond(formInfo.getPlanDate());
                long secondJ = TimeUtils.INSTANCE.getSecond(formInfoJ.getPlanDate());
                if (second < secondJ) {
                    sortFormInfo.set(i, formInfoJ);
                    sortFormInfo.set(j, formInfo);
                } else if (second == secondJ) {
                    String formCode = formInfo.getFormCode();
                    String formCodeJ = formInfoJ.getFormCode();
                    if (!TextUtils.isEmpty(formCode) && !TextUtils.isEmpty(formCodeJ)) {
                        int indexOf = formCode.lastIndexOf("-");
                        int indexOfJ = formCodeJ.lastIndexOf("-");
                        if (indexOf != -1 && indexOfJ != -1) {
                            String sortCode = formCode.substring(indexOf + 1);
                            String sortCodeJ = formCodeJ.substring(indexOfJ + 1);
                            try {
                                int sort = Integer.parseInt(sortCode);
                                int sortJ = Integer.parseInt(sortCodeJ);
                                if (sort > sortJ) {
                                    sortFormInfo.set(i, formInfoJ);
                                    sortFormInfo.set(j, formInfo);
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void getData() {
        if (!NetUtils.INSTANCE.isNetConnect(getContext())) {
            List<SystemInfo> systemInfos = DataSupport.findAll(SystemInfo.class);
            List<ActionInfo> actions = DataSupport.findAll(ActionInfo.class);
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

                    List<ActionInfo> actions = hashMap.get("Action").getList(ActionInfo.class);
                    if (actions != null) {
                        for (int i = 0; i < actions.size(); i++) {
                            actions.get(i).saveOrUpdate("actionType='" + actions.get(i).getActionType() + "'");
                        }
                    }

                    systemSpinnerAdapter.notifyDataSetChanged(systemInfos);
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

    public UmsService umsService;
    // service连接
    public ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UmsService.MyBinder binder = (UmsService.MyBinder) service;
            umsService = binder.getService();
            umsService.setDownloadListener(new UmsService.OnDownloadListener() {
                @Override
                public void onUpdate(List<FormInfo> list) {
                    if (list != null) {
                        downLoadAdapter.notifyDataSetChanged(list);
                        if (downLoadAll) {
                            int count = 0, size = list.size();
                            for (int i = 0; i < size; i++) {
                                if (list.get(i).getDownload_status() == FormInfo.DONE) {
                                    count++;
                                }
                            }

                            if (count == size) downLoadAll = false;
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
