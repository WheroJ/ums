package com.zetavision.panda.ums.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;


/**
 *
 * Created by Administrator on 2016/4/21.
 */
public abstract class  ViewHolder<T> extends RecyclerView.ViewHolder{

    public ViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(itemView);
    }

    public abstract void  setData(T data, int position);
}
