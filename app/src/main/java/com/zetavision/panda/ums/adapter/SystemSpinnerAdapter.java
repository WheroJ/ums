package com.zetavision.panda.ums.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;


import com.zetavision.panda.ums.R;
import com.zetavision.panda.ums.model.SystemInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SystemSpinnerAdapter extends BaseAdapter implements SpinnerAdapter{

    private List<SystemInfo> list = new ArrayList<>();
    private Context mContext;


    public SystemSpinnerAdapter(Context context) {
        this.mContext = context;
    }

    public void notifyDataSetChanged(List<SystemInfo> list) {
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
    public SystemInfo getItem(int position) {
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
            view = View.inflate(mContext, R.layout.spinner_simple_item, null);
            ButterKnife.bind(holder, view);//用butterKnife绑定
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }
        holder.setData(getItem(position));
        return view;
    }

    class ViewHolder {
        @BindView(R.id.textView) TextView textView;
        public void setData(SystemInfo data) {
            textView.setText(data.getUtilitySystemName());
        }
    }
}