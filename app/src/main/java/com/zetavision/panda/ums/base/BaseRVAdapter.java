package com.zetavision.panda.ums.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shopping on 2016/4/21.
 *
 * @author jiangxueping
 * @Date 2016-4-21 14:07
 */
public abstract class BaseRVAdapter<T, E extends ViewHolder<T>> extends RecyclerView.Adapter<E> {

    /**
     * 列表显示的数据
     */
    protected List<T> datas;

    protected Context mContext;

    protected OnItemClickListener onItemClickListener;

    public BaseRVAdapter(Context mContext, List<T> datas) {
        if (this.datas == null)
            this.datas = new ArrayList<>();
        if (datas != null)
            this.datas.addAll(datas);
        this.mContext = mContext;
    }

    /**
     * 每一个Item点击时执行
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public E onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), getLayoutId(), null);
        E holder = createHolder(view);
        return holder;
    }

    protected abstract E createHolder(View view);

    protected abstract int getLayoutId();

    @Override
    public void onBindViewHolder(E holder, final int position) {
        holder.setData(datas.get(position), position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (datas == null)
            return 0;
        return datas.size();
    }

    /**
     *
     * @param data      该界面显示的数据
     * @param itemView 当前位置显示的view
     */

    /**
     * 更新数据变化
     *
     * @param datas
     */
    public void updateDatas(List<T> datas) {
        if (datas != null) {
            if (this.datas != null) {
                this.datas.clear();
                this.datas.addAll(datas);
            } else {
                this.datas = datas;
            }
            notifyDataSetChanged();
        }
    }
}
