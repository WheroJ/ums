package com.zetavision.panda.ums.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.model.FormInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadAdapter extends BaseAdapter {

    private Context mContext;
    private List<FormInfo> list = new ArrayList<>();

    public DownloadAdapter(Context mContext) {
        this.mContext = mContext;
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

        public void setData(FormInfo data) {
            form_number.setText(data.getFormCode());
            category.setText(data.getActionType());
            line_or_eqp.setText(data.getLineOrEqp());
            desc.setText(data.getDesc());
            status.setText(data.getStatus());
        }
    }
}
