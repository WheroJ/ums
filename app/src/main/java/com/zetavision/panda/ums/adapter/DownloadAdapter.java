package com.zetavision.panda.ums.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.fragments.DownloadFragment;
import com.zetavision.panda.ums.model.FormInfo;
import com.zetavision.panda.ums.service.UmsService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DownloadAdapter extends BaseAdapter {

    private Context mContext;
    private List<FormInfo> list = new ArrayList<>();
    private DownloadFragment fragment;

    public DownloadAdapter(Context mContext, DownloadFragment fragment) {
        this.mContext = mContext;
        this.fragment = fragment;
    }

    public void notifyDataSetChanged(List<FormInfo> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public FormInfo getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getFormId();
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

        public void setData(FormInfo data) {
            this.data = data;

            form_number.setText(data.getFormCode());
            category.setText(data.getActionType());
            line_or_eqp.setText(data.getLineOrEqp());
            desc.setText(data.getDesc());
            status.setText(data.getStatus());

            switch (data.getDownload_status()) {
                case DONE:
                    downloadBtn.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    doneImg.setVisibility(View.VISIBLE);
                    textInfo.setText("完成");
                    break;
                case FAIL:
                    downloadBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    doneImg.setVisibility(View.GONE);
                    textInfo.setText("失败");
                    break;
                case PROGRESS:
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
                    textInfo.setText("下载");
            }

        }

        @OnClick(R.id.downloadBtn) void download() {
            if (fragment.umsService!=null) {
                fragment.umsService.startDownload(data);
            }
        }

        @OnClick(R.id.pauseBtn) void pause() {
            System.out.println("pause");
        }
    }
}
