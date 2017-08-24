package com.empty.citylistchange;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Administrator on 2017/8/23.
 */

public class CityGridAdapter extends BaseAdapter {

    private List<String> list;
    private Context context;

    public CityGridAdapter(Context context,List<String> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.meituan_item_header_item,null);
        }
        return convertView;
    }
}
