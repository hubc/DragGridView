package com.yilong.newwidget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class DragAdapter extends BaseAdapter {

    /**
     * @param from
     * @param to
     * @描述:当从from排序被拖到to排序时的处理方式,请对相应的数据做处理。
     */
    public abstract void onDataModelMove(int from, int to);

    /**
     * 复制View使用的方法,默认直接使用getView方法获取
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View copyView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    /**
     * 是否启用copyView方法
     *
     * @return true 使用copyView复制 false 使用getView直接获取镜像
     */
    public boolean isUseCopyView() {
        return false;
    }

    public abstract Object getSwapData(int position);

    public abstract void removeData(int position);

    public abstract void addNewData(Object data);
}