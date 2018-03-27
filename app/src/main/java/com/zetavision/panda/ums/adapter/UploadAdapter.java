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
import com.zetavision.panda.ums.utils.LogPrinter;

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
            view = View.inflate(mContext, R.layout.list_upload_item, null);
            ButterKnife.bind(holder, view);//用butterKnife绑定
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }
        holder.setData(getItem(position), position);
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
        @BindView(R.id.form_sort) TextView formSort;

        private FormInfo data;
        private HashMap<String, String> statusMap;

        public ViewHolder() {
            statusMap = new HashMap<>();
            statusMap.put(Constant.FORM_STATUS_PLANNED, mContext.getString(R.string.status_planed));
            statusMap.put(Constant.FORM_STATUS_INPROGRESS, mContext.getString(R.string.status_inprogress));
            statusMap.put(Constant.FORM_STATUS_COMPLETED, mContext.getString(R.string.status_complete));
            statusMap.put(Constant.FORM_STATUS_CLOSED, mContext.getString(R.string.status_closed));
        }

        public void setData(FormInfoDetail formInfoDetail, int position) {
            this.data = formInfoDetail.form;

            form_number.setText(data.getFormCode());
            category.setText(data.actionTypeDescription);
            line_or_eqp.setText(data.getLineOrEqp());
            desc.setText(data.getDesc());
            status.setText(statusMap.get(data.getStatus()));
            formSort.setText(String.valueOf(position + 1));

            switch (formInfoDetail.isUpload) {
                case FormInfo.DONE:
                    downloadBtn.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    doneImg.setVisibility(View.VISIBLE);
                    textInfo.setText(mContext.getString(R.string.common_finish));
                    break;
                case FormInfo.FAIL:
                    downloadBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    doneImg.setVisibility(View.GONE);
                    textInfo.setText(mContext.getString(R.string.common_fail));
                    break;
                case FormInfo.PROGRESS:
                    downloadBtn.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    pauseBtn.setVisibility(View.VISIBLE);
                    doneImg.setVisibility(View.GONE);
                    textInfo.setText(mContext.getString(R.string.common_stop));
                    break;
                default:
                    downloadBtn.setVisibility(View.VISIBLE);
                    downloadBtn.setImageResource(R.mipmap.upload_circle);
                    progressBar.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    doneImg.setVisibility(View.GONE);
                    textInfo.setText(mContext.getString(R.string.common_upload));
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
            LogPrinter.i("UploadAdapter", "pause");
            UmsService umsService = ((UploadFragment) fragment).umsService;
            if (umsService != null) {
                umsService.stopUpload(data.getFormId());
            }
        }
    }
}
