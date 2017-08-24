package com.empty.citylistchange;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.List;


/**
 * Created by zhangxutong .
 * Date: 16/08/28
 */

public class MeituanAdapter extends CommonAdapter<MeiTuanBean> {
    private Context context;
    public MeituanAdapter(Context context, int layoutId, List<MeiTuanBean> datas) {
        super(context, layoutId, datas);
        this.context = context;
    }

    @Override
    public void convert(ViewHolder holder, final MeiTuanBean cityBean) {
        holder.setText(R.id.tvCity, cityBean.getCity());
        holder.getConvertView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,cityBean.getCity(),Toast.LENGTH_LONG).show();
            }
        });

    }

}