package com.zetavision.panda.ums.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.fragments.base.BaseFragment;
import com.zetavision.panda.ums.model.FormInfo;
import com.zetavision.panda.ums.model.FormInfoDetail;
import com.zetavision.panda.ums.service.UmsService;
import com.zetavision.panda.ums.ui.formdownload.DownloadFragment;
import com.zetavision.panda.ums.ui.formup.UploadFragment;
import com.zetavision.panda.ums.utils.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UploadAdapter extends BaseAdapter {

    private Context mContext;
    private List<FormInfoDetail> list = new ArrayList<>();
    private BaseFragment fragment;

    public UploadAdapter(Context mContext, BaseFragment fragment) {
        this.mContext = mContext;
        this.fragment = fragment;
    }

    public void notifyDataSetChanged(List<FormInfoDetail> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    @Override
    public FormInfoDetail getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(mContext, R.layout.list_download_item, null);
            ButterKnife.bind(holder, view);//用butterKnife绑定
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }
        holder.setData(getItem(position));
        return view;
    }

    class ViewHolder {
        @BindView(R.id.form_number) TextView form_number;
        @BindView(R.id.category) TextView category;
        @BindView(R.id.line_or_eqp) TextView line_or_eqp;
        @BindView(R.id.desc) TextView desc;
        @BindView(R.id.status) TextView status;

        @BindView(R.id.downloadBtn) ImageView downloadBtn;
        @BindView(R.id.progressBar) ProgressBar progressBar;
        @BindView(R.id.pauseBtn) ImageView pauseBtn;
        @BindView(R.id.doneImg) ImageView doneImg;
        @BindView(R.id.textInfo) TextView textInfo;

        private FormInfo data;
        private FormInfoDetail formInfoDetail;

        private HashMap<String, String> statusMap;

        public ViewHolder() {
            statusMap = new HashMap<>();
            statusMap.put(Constant.MAINT_FORM_STATUS_PLANNED, "已计划");
            statusMap.put(Constant.MAINT_FORM_STATUS_INPROGRESS, "进行中");
            statusMap.put(Constant.MAINT_FORM_STATUS_COMPLETED, "已完成");
            statusMap.put(Constant.MAINT_FORM_STATUS_CLOSED, "已结束");
        }

        public void setData(FormInfoDetail formInfoDetail) {
            this.data = formInfoDetail.form;
            this.formInfoDetail = formInfoDetail;

            form_number.setText(data.getFormCode());
            category.setText(data.getActionType());
            line_or_eqp.setText(data.getLineOrEqp());
            desc.setText(data.getDesc());
            status.setText(statusMap.get(data.getStatus()));

            switch (data.getDownload_status()) {
                case FormInfo.DONE:
                    downloadBtn.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    doneImg.setVisibility(View.VISIBLE);
                    textInfo.setText("完成");
                    break;
                case FormInfo.FAIL:
                    downloadBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    doneImg.setVisibility(View.GONE);
                    textInfo.setText("失败");
                    break;
                case FormInfo.PROGRESS:
                    downloadBtn.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    pauseBtn.setVisibility(View.VISIBLE);
                    doneImg.setVisibility(View.GONE);
                    textInfo.setText("暂停");
                    break;
                default:
                    downloadBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    doneImg.setVisibility(View.GONE);
                    textInfo.setText("上传");
            }

        }

        @OnClick(R.id.downloadBtn) void download() {
            if (fragment instanceof DownloadFragment) {
                UmsService umsService = ((DownloadFragment) fragment).umsService;
                if (umsService != null) {
                    umsService.startDownload(data.getFormId());
                }
            } else if (fragment instanceof UploadFragment) {
                UmsService umsService = ((UploadFragment) fragment).umsService;
                if (umsService != null) {
                    umsService.startUpload(data.getFormId());
                }
            }
        }

        @OnClick(R.id.pauseBtn) void pause() {
            System.out.println("pause");
        }
    }
}
