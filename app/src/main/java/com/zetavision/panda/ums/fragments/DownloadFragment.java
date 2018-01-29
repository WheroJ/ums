package com.zetavision.panda.ums.fragments;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.Utils.Api;
import com.zetavision.panda.ums.Utils.LoadingDialog;
import com.zetavision.panda.ums.adapter.ActionSpinnerAdapter;
import com.zetavision.panda.ums.adapter.DownloadAdapter;
import com.zetavision.panda.ums.adapter.SystemSpinnerAdapter;
import com.zetavision.panda.ums.fragments.base.BaseFragment;
import com.zetavision.panda.ums.model.ActionInfo;
import com.zetavision.panda.ums.model.FormInfo;
import com.zetavision.panda.ums.model.Result;
import com.zetavision.panda.ums.model.SystemInfo;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;

public class DownloadFragment extends BaseFragment{

    @BindView(R.id.systemSpinner) Spinner systemSpinner;
    @BindView(R.id.actionSpinner) Spinner actionSpinner;
    @BindView(R.id.listView) ListView listView;

    private SystemSpinnerAdapter systemSpinnerAdapter;
    private ActionSpinnerAdapter actionSpinnerAdapter;
    private DownloadAdapter adapter;

    @Override
    protected View createView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_download, null);
    }

    @Override
    protected void init() {
        systemSpinnerAdapter = new SystemSpinnerAdapter(getContext());
        actionSpinnerAdapter = new ActionSpinnerAdapter(getContext());
        systemSpinner.setAdapter(systemSpinnerAdapter);
        actionSpinner.setAdapter(actionSpinnerAdapter);

        adapter = new DownloadAdapter(getContext());
        listView.setAdapter(adapter);

        getData();
    }

    @OnClick(R.id.search) void onSearch() {
        SystemInfo systemInfo = (SystemInfo) systemSpinner.getSelectedItem();
        ActionInfo actionInfo = (ActionInfo) actionSpinner.getSelectedItem();
        final LoadingDialog dialog = new LoadingDialog();
        dialog.show(getFragmentManager(), null);
        Api api = new Api(getContext());
        api.get("queryPlannedForm.mobile?utilitySystemId="+systemInfo.getUtilitySystemId() + "&actionType=" + actionInfo.getActionType()).subscribe(new Consumer<Result>() {
            @Override
            public void accept(Result result) throws Exception {
                adapter.notifyDataSetChanged(result.getList(FormInfo.class));
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                dialog.dismiss();
                Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                dialog.dismiss();
            }
        });
    }

    private void getData() {
        final LoadingDialog dialog = new LoadingDialog();
        dialog.show(getFragmentManager(), null);
        Api api = new Api(getContext());

        Flowable<Result> flowable1 = api.get("queryUtilitySystem.mobile");
        Flowable<Result> flowable2 = api.get("queryActionType.mobile");

        Flowable.zip(flowable1, flowable2, new BiFunction<Result, Result, HashMap<String, Result>>() {
            @Override
            public HashMap<String, Result> apply(Result result, Result result2) throws Exception {
                HashMap<String, Result> hashMap = new HashMap<>();
                hashMap.put("SystemInfo", result);
                hashMap.put("Action", result2);
                return hashMap;
            }
        }).subscribe(new Consumer<HashMap<String, Result>>() {
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
                Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                dialog.dismiss();
            }
        });
    }
}
