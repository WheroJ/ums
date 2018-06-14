package com.zetavision.panda.ums.ui.formdownload;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
import com.zetavision.panda.ums.model.Shift;
import com.zetavision.panda.ums.model.SystemInfo;
import com.zetavision.panda.ums.model.Weather;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
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
    @BindView(R.id.fragmentDownload_etSearch)
    EditText etSearch;

    private boolean downLoadAll = false;
    private SystemSpinnerAdapter systemSpinnerAdapter;
    private ActionSpinnerAdapter actionSpinnerAdapter;
    private DownloadAdapter downLoadAdapter;
    private CompositeDisposable umsServiceDisposable;

    @Override
    public int getContentLayoutId() {
        return R.layout.fragment_download;
    }

    @OnClick(R.id.downloadAll)
    void downloadAll() {
        if (!NetUtils.INSTANCE.isNetConnect(getContext())) {
            ToastUtils.show(getContext().getString(R.string.connect_net2download));
            return;
        }
        if (umsService != null) {
            umsService.startDownloadAll();
            downLoadAll = true;
        }
    }

    @OnClick(R.id.pauseAll)
    void pauseAll() {
        if (umsService != null) {
            umsService.stopDownloadAll();
        }
    }

    @Override
    protected void init() {
        getHeader().setTitle(getString(R.string.form_download));
        // 绑定service
        IntentUtils.INSTANCE.bindService(getActivity(), connection);

        systemSpinnerAdapter = new SystemSpinnerAdapter(getContext());
        actionSpinnerAdapter = new ActionSpinnerAdapter(getContext());
        systemSpinner.setAdapter(systemSpinnerAdapter);
        actionSpinner.setAdapter(actionSpinnerAdapter);

        AdapterView.OnItemSelectedListener searchSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSelectedChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        systemSpinner.setOnItemSelectedListener(searchSelectedListener);
        actionSpinner.setOnItemSelectedListener(searchSelectedListener);

        downLoadAdapter = new DownloadAdapter(getContext(), this);
        listView.setAdapter(downLoadAdapter);

        progressBar.setProgress(0);
        progressText.setText(getString(R.string.have_finish, 0.0 + "%"));
        getData();
    }

    private void onSelectedChange() {
        progressBar.setProgress(0);
        progressText.setText(getString(R.string.have_finish, 0.0 + "%"));

        final SystemInfo systemInfo = (SystemInfo) systemSpinner.getSelectedItem();
        final ActionInfo actionInfo = (ActionInfo) actionSpinner.getSelectedItem();

        //根据您系统id 和 动作分类搜索
        if (!NetUtils.INSTANCE.isNetConnect(getContext())) {
            if (umsServiceDisposable != null) {
                umsServiceDisposable.dispose();
            }
            umsServiceDisposable = new CompositeDisposable();
            umsServiceDisposable.add(Observable.interval(500, 500, TimeUnit.MILLISECONDS)
                    .filter(new Predicate<Long>() {
                        @Override
                        public boolean test(Long aLong) throws Exception {
                            return umsService != null;
                        }
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            List<FormInfo> downloadList = loadLocalFormInfos(systemInfo, actionInfo);
                            if (downloadList != null && !downloadList.isEmpty()) {
                                umsService.setDownloadList(downloadList);
                            } else {
                                umsService.setDownloadList(new ArrayList<FormInfo>());
                                ToastUtils.show(R.string.no_local_data);
                            }
                            umsServiceDisposable.dispose();
                            umsServiceDisposable = null;
                        }
                    }));

        } else {
            if (systemInfo != null && actionInfo != null) {
                RxUtils.INSTANCE.acquireString(Client.getApi(UmsApi.class)
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
                                                formInfo.sopLocalPath = downloadList.get(indexOf).sopLocalPath;
                                                sortFormInfo.add(formInfo);
                                            } else {
                                                sortFormInfo.add(unDownCount, formInfo);
                                                unDownCount++;
                                            }
                                        }
                                    } else {
                                        umsService.setDownloadList(new ArrayList<FormInfo>());
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
            } else {
                ToastUtils.show(R.string.choose_systype_action);
            }
        }
    }

    @OnClick(R.id.search)
    void onSearch() {
        String search = etSearch.getText().toString().trim();
        if (!TextUtils.isEmpty(search)) {
            //根据输入的设备代码搜索
            List<FormInfo> formInfos = searchByCodeLocal(search);
            if (formInfos.isEmpty()) {
                searchByCodeNet(search);
            } else {
                FormInfo formInfo = formInfos.get(0);
                setSpinner(formInfo);
                umsService.setDownloadList(formInfos);
            }
        } else {
            ToastUtils.show(getString(R.string.search_not_empty));
        }
    }

    /**
     * 本地网络中搜索点检表单和保养表单
     * @param search  搜索的关键字
     * @return 返回搜索的结果
     */
    private void searchByCodeNet(String search) {
        if (NetUtils.INSTANCE.isNetConnect(getContext())) {

            final SystemInfo systemInfo = (SystemInfo) systemSpinner.getSelectedItem();
            final ActionInfo actionInfo = (ActionInfo) actionSpinner.getSelectedItem();

            if (systemInfo != null && actionInfo != null) {
                RxUtils.INSTANCE.acquireString(Client.getApi(UmsApi.class)
                        .searchForm(systemInfo.getUtilitySystemId(), actionInfo.getActionType(), search), new RxUtils.DialogListener() {
                    @Override
                    public void onResult(@NotNull Result result) {
                        List<FormInfo> formInfos = result.getList(FormInfo.class);
                        if (formInfos != null && !formInfos.isEmpty()) {
                            FormInfo formInfo = formInfos.get(0);
                            setSpinner(formInfo);
                            umsService.setDownloadList(formInfos);
                        } else {
                            umsService.setDownloadList(new ArrayList<FormInfo>());
                            ToastUtils.show(R.string.no_data);
                        }
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        super.onError(e);
                        umsService.setDownloadList(new ArrayList<FormInfo>());
                        ToastUtils.show(R.string.no_data);
                    }
                });
            }
        }
    }

    private void setSpinner(FormInfo formInfo) {
        if (systemSpinnerAdapter != null) {
            for (int i = 0; i < systemSpinnerAdapter.getCount(); i++) {
                SystemInfo item = systemSpinnerAdapter.getItem(i);
                if (item.getUtilitySystemId() == formInfo.utilitySystemId) {
                    systemSpinner.setSelection(i);
                    break;
                }
            }
        }

        if (actionSpinnerAdapter != null) {
            for (int i = 0; i < actionSpinnerAdapter.getCount(); i++) {
                ActionInfo item = actionSpinnerAdapter.getItem(i);
                if (item.getActionType().equals(formInfo.getActionType())) {
                    actionSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    /**
     * 本地搜索点检表单和保养表单
     * @param search  搜索的关键字
     * @return 返回搜索的结果
     */
    private List<FormInfo> searchByCodeLocal(String search) {
        List<FormInfo> formInfos = DataSupport.where("equipmentCode='" + search + "'").find(FormInfo.class);
        if (formInfos.isEmpty()) {
            formInfos = DataSupport.where("inspectRouteCode='" + search + "'").find(FormInfo.class);
        }
        if (!formInfos.isEmpty()) {
            FormInfo formInfo = formInfos.get(0);
            setSpinner(formInfo);
            umsService.setDownloadList(formInfos);
        }
        return formInfos;
    }

    private  List<FormInfo> loadLocalFormInfos(SystemInfo systemInfo, ActionInfo actionInfo) {
        if (systemInfo != null && actionInfo != null && umsService != null) {
            String where = "(utilitySystemId='" + systemInfo.getUtilitySystemId() + "' and actionType='" + actionInfo.getActionType() + "') ";
            List<FormInfo> formInfoList = DataSupport.where(where).find(FormInfo.class);
            return formInfoList;
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
            if (isAdded()) {
                dialog.show(getFragmentManager(), null);
            }

            getWeatherAndShift();
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
                    if (isAdded()) {
                        dialog.dismiss();
                    }
                    ToastUtils.show(throwable.getMessage());
                }
            }, new Action() {
                @Override
                public void run() throws Exception {
                    if (isAdded()) {
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    private void getWeatherAndShift() {
        Observable.zip(Client.getApi(UmsApi.class).queryWeather(), Client.getApi(UmsApi.class).queryShift(), new BiFunction<ResponseBody, ResponseBody, HashMap<String, Result>>() {
            @Override
            public HashMap<String, Result> apply(ResponseBody responseBody, ResponseBody responseBody2) throws Exception {
                HashMap<String, Result> map = new HashMap<>();

                JSONObject resultObject = new JSONObject(responseBody.string());
                Result resultWeather = new Result();
                resultWeather.setReturnCode(resultObject.optString("returnCode"));
                resultWeather.setReturnMessage(resultObject.optString("returnMessage"));
                resultWeather.setReturnData(resultObject.optString("returnData"));
                map.put("weather", resultWeather);

                resultObject = new JSONObject(responseBody2.string());
                Result resultShift = new Result();
                resultShift.setReturnCode(resultObject.optString("returnCode"));
                resultShift.setReturnMessage(resultObject.optString("returnMessage"));
                resultShift.setReturnData(resultObject.optString("returnData"));
                map.put("shift", resultShift);

                return map;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<HashMap<String, Result>>() {
                    @Override
                    public void accept(HashMap<String, Result> map) throws Exception {
                        Result weather = map.get("weather");
                        List<Weather> weatherList = weather.getList(Weather.class);
                        if (weatherList != null) {
                            for (int i = 0; i < weatherList.size(); i++) {
                                weatherList.get(i).saveOrUpdate("weather='" + weatherList.get(i).weather + "'");
                            }
                        }

                        Result shift = map.get("shift");
                        List<Shift> shiftList = shift.getList(Shift.class);
                        if (shiftList != null) {
                            for (int i = 0; i < shiftList.size(); i++) {
                                shiftList.get(i).saveOrUpdate("shift='" + shiftList.get(i).shift + "'");
                            }
                        }
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
            umsService.setDownloadListener(new UmsService.OnDownloadListener() {
                @SuppressLint("StringFormatInvalid")
                @Override
                public void onUpdate(List<FormInfo> list) {
                    if (isAdded()) {
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
                                DecimalFormat df = new DecimalFormat("#.00");
                                float ratio = count * 100.0F / list.size();
                                progressBar.setProgress((int) ratio);
                                progressText.setText(getString(R.string.have_finish, df.format(ratio) + "%"));
                            }
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
        if (umsServiceDisposable != null) umsServiceDisposable.dispose();
        super.onDestroyView();
    }
}
