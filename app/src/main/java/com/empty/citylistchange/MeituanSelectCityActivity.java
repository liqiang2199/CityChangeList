package com.empty.citylistchange;

import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mcxtzhang.indexlib.IndexBar.bean.BaseIndexPinyinBean;
import com.mcxtzhang.indexlib.IndexBar.widget.IndexBar;
import com.mcxtzhang.indexlib.suspension.SuspensionDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 介绍： 高仿美团选择城市页面
 * 作者：zhangxutong
 * 邮箱：mcxtzhang@163.com
 * 主页：http://blog.csdn.net/zxt0601
 * 时间： 2016/11/7.
 */
public class MeituanSelectCityActivity extends AppCompatActivity {
    private Context mContext;
    private RecyclerView mRv;
    private MeituanAdapter mAdapter;
    private HeaderRecyclerAndFooterWrapperAdapter mHeaderAdapter;
    private LinearLayoutManager mManager;
    private EditText text_edit;

    //设置给InexBar、ItemDecoration的完整数据集
    private List<BaseIndexPinyinBean> mSourceDatas;
    //头部数据源
    private List<MeituanHeaderBean> mHeaderDatas;
    //主体部分数据源（城市数据）
    private List<MeiTuanBean> mBodyDatas;
    private List<MeiTuanBean> mBodySearchDatas;//要搜索的城市列表

    private SuspensionDecoration mDecoration;

    /**
     * 右侧边栏导航区域
     */
    private IndexBar mIndexBar;

    /**
     * 显示指示器DialogText
     */
    private TextView mTvSideBarHint;
    private int indexnow = 1;

    List<String> list = new ArrayList<>();
    private Object searchLock = new Object();
    private SearchListTask mSearchListTask;
    private String searchString;
    boolean inSearchMode = false;
    List<String> recentCitys;//最近访问的城市
    List<String> hotCitys;//热门城市
    private ResolveCity mResolveCity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meituan);
        mContext = this;
        mBodySearchDatas = new ArrayList<>();

        mRv = (RecyclerView) findViewById(R.id.rv);
        mRv.setLayoutManager(mManager = new LinearLayoutManager(this));
        text_edit = (EditText) findViewById(R.id.text_edit);

        mSourceDatas = new ArrayList<>();
        mHeaderDatas = new ArrayList<>();
        mBodyDatas = new ArrayList<>();
        mResolveCity = new ResolveCity(this);
        mResolveCity.initJsonData();

        Init_List();

        //使用indexBar
        mTvSideBarHint = (TextView) findViewById(R.id.tvSideBarHint);//HintTextView
        mIndexBar = (IndexBar) findViewById(R.id.indexBar);//IndexBar
        mAdapter = new MeituanAdapter(this, R.layout.meituan_item_select_city, mBodyDatas);
        mRv.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));

        Init_CityList();
        Init_Search();

        mRv.addItemDecoration(mDecoration = new SuspensionDecoration(this, mSourceDatas)
                .setmTitleHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics()))
                .setColorTitleBg(0xffefefef)
                .setTitleFontSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()))
                .setColorTitleFont(mContext.getResources().getColor(android.R.color.black))
                .setHeaderViewCount(mHeaderAdapter.getHeaderViewCount() - mHeaderDatas.size()));

        initDatas(getResources().getStringArray(R.array.provinces));
    }

    private void Init_List(){
        List<String> locationCity = new ArrayList<>();
        locationCity.add("定位中");
        mHeaderDatas.add(new MeituanHeaderBean(locationCity, "定位城市", "定"));
        List<String> recentCitys = new ArrayList<>();
        mHeaderDatas.add(new MeituanHeaderBean(recentCitys, "最近访问城市", "近"));
        List<String> hotCitys = new ArrayList<>();
        mHeaderDatas.add(new MeituanHeaderBean(hotCitys, "热门城市", "热"));
        mSourceDatas.addAll(mHeaderDatas);
    }

    private void Init_CityList(){

        mHeaderAdapter = new HeaderRecyclerAndFooterWrapperAdapter(mAdapter) {
            @Override
            protected void onBindHeaderHolder(final ViewHolder holder, int headerPos, int layoutId, Object o) {
                switch (layoutId) {
                    case R.layout.meituan_item_header:
                        final MeituanHeaderBean meituanHeaderBean = (MeituanHeaderBean) o;
                        //网格
                        RecyclerView recyclerView = holder.getView(R.id.rvCity);
                        recyclerView.setAdapter(
                                new CommonAdapter<String>(mContext, R.layout.meituan_item_header_item, meituanHeaderBean.getCityList()) {
                                    @Override
                                    public void convert(ViewHolder holder, final String cityName) {
                                        holder.setText(R.id.tvName, cityName);
                                        holder.getConvertView().setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Toast.makeText(mContext, "cityName:" + cityName, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
                        break;
                    case R.layout.meituan_item_header_top:
                        final MeituanTopHeaderBean meituanTopHeaderBean = (MeituanTopHeaderBean) o;
                        holder.setText(R.id.tvCurrent, meituanTopHeaderBean.getTxt());
                        holder.getView(R.id.onclick_more).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //网格
                                RecyclerView recyclerView = holder.getView(R.id.rvCity);
                                if (indexnow == 1){
                                    Map<String, String[]> mAreaDataslist = mResolveCity.mAreaDatasMap;
                                    String[] nameKey = mAreaDataslist.get("绵阳");
                                    for (String citylist :nameKey){
                                        list.add(citylist);
                                    }
                                    indexnow = 2;
                                }else{
                                    indexnow = 1;
                                    list.clear();
                                }
                                recyclerView.setAdapter(
                                        new CommonAdapter<String>(mContext, R.layout.meituan_item_header_item, meituanTopHeaderBean.getCityList()) {
                                            @Override
                                            public void convert(ViewHolder holder, final String cityName) {
                                                holder.setText(R.id.tvName, cityName);
                                                holder.getConvertView().setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Toast.makeText(mContext, "cityName:" + cityName, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });
                                recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));

                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        };

        mHeaderAdapter.setHeaderView(0, R.layout.meituan_item_header_top, new MeituanTopHeaderBean("当前：上海市",list));
        mHeaderAdapter.setHeaderView(1, R.layout.meituan_item_header, mHeaderDatas.get(0));
        mHeaderAdapter.setHeaderView(2, R.layout.meituan_item_header, mHeaderDatas.get(1));
        mHeaderAdapter.setHeaderView(3, R.layout.meituan_item_header, mHeaderDatas.get(2));


        mRv.setAdapter(mHeaderAdapter);



        //使用indexBar
//        mTvSideBarHint = (TextView) findViewById(R.id.tvSideBarHint);//HintTextView
//        mIndexBar = (IndexBar) findViewById(R.id.indexBar);//IndexBar

        mIndexBar.setmPressedShowTextView(mTvSideBarHint)//设置HintTextView
                .setNeedRealIndex(true)//设置需要真实的索引
                .setmLayoutManager(mManager)//设置RecyclerView的LayoutManager
                .setHeaderViewCount(mHeaderAdapter.getHeaderViewCount() - mHeaderDatas.size());


    }

    /**
     * 组织数据源
     *
     * @param data
     * @return
     */
    private void initDatas(final String[] data) {
        //延迟两秒 模拟加载数据中....
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {


                mBodyDatas.clear();
                for (int i = 0; i < data.length; i++) {
                    MeiTuanBean cityBean = new MeiTuanBean();
                    cityBean.setCity(data[i]);//设置城市名称
                    mBodyDatas.add(cityBean);

                }
                //先排序
                mIndexBar.getDataHelper().sortSourceDatas(mBodyDatas);
                Log.v("thiscity",mBodyDatas.get(0).getSuspensionTag()+"         城市收索       "+mBodyDatas.get(0).getCity());
                mAdapter.setDatas(mBodyDatas);
                mHeaderAdapter.notifyDataSetChanged();
                mSourceDatas.addAll(mBodyDatas);

                mIndexBar.setmSourceDatas(mSourceDatas)//设置数据
                        .invalidate();
                mDecoration.setmDatas(mSourceDatas);
            }
        }, 1000);

        //延迟两秒加载头部
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                MeituanHeaderBean header1 = mHeaderDatas.get(0);
                header1.getCityList().clear();
                header1.getCityList().add("上海");

                MeituanHeaderBean header2 = mHeaderDatas.get(1);
                recentCitys = new ArrayList<>();
                recentCitys.add("成都");
                recentCitys.add("绵阳");
                header2.setCityList(recentCitys);

                MeituanHeaderBean header3 = mHeaderDatas.get(2);
                hotCitys = new ArrayList<>();
                hotCitys.add("上海");
                hotCitys.add("北京");
                hotCitys.add("杭州");
                hotCitys.add("广州");
                hotCitys.add("成都");
                hotCitys.add("绵阳");
                header3.setCityList(hotCitys);

                mHeaderAdapter.notifyItemRangeChanged(1, 3);

            }
        }, 2000);

    }

    private void Init_Search(){
        text_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0){
                    searchString = text_edit.getText().toString().trim().toUpperCase();

                    if (mSearchListTask != null
                            && mSearchListTask.getStatus() != AsyncTask.Status.FINISHED)
                    {
                        try
                        {
                            mSearchListTask.cancel(true);
                        } catch (Exception e)
                        {
                            Log.i("thiscity", "Fail to cancel running search task");
                        }

                    }
                    mSearchListTask = new SearchListTask();
                    mSearchListTask.execute(searchString);
                }else{
                    Init_List();
                    mAdapter = new MeituanAdapter(MeituanSelectCityActivity.this, R.layout.meituan_item_select_city, mBodyDatas);

                    Init_CityList();
//                    //先排序
//                    mIndexBar.getDataHelper().sortSourceDatas(mBodyDatas);
//
                    mAdapter.setDatas(mBodyDatas);
                    mHeaderAdapter.notifyDataSetChanged();
                    mSourceDatas.addAll(mBodyDatas);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            mIndexBar.setmSourceDatas(mSourceDatas)//设置数据
                                    .invalidate();
                            mDecoration.setmDatas(mSourceDatas);
                        }
                    },200);



                    MeituanHeaderBean header1 = mHeaderDatas.get(0);
                    header1.getCityList().clear();
                    header1.getCityList().add("上海");

                    MeituanHeaderBean header2 = mHeaderDatas.get(1);
                    header2.setCityList(recentCitys);

                    MeituanHeaderBean header3 = mHeaderDatas.get(2);
                    header3.setCityList(hotCitys);

                    mHeaderAdapter.notifyItemRangeChanged(1, 3);
                }
            }
        });
    }

    private class SearchListTask extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... params)
        {
            mBodySearchDatas.clear();

            String keyword = params[0];

            inSearchMode = (keyword.length() > 0);

            if (inSearchMode)
            {
                // get all the items matching this
                for (MeiTuanBean item : mBodyDatas)
                {
//                    CityItem contact = (CityItem) item;

                    boolean isPinyin = item.getSuspensionTag().toUpperCase().indexOf(keyword) > -1;
                    boolean isChinese = item.getCity().indexOf(keyword) > -1;

                    if (isPinyin || isChinese)
                    {
                        mBodySearchDatas.add(item);
                    }

                }

            }
            return null;
        }

        protected void onPostExecute(String result)
        {

            synchronized (searchLock)
            {

                if (inSearchMode)
                {
                    mHeaderDatas.clear();
                    mSourceDatas.clear();
                    mAdapter = new MeituanAdapter(MeituanSelectCityActivity.this, R.layout.meituan_item_select_city, mBodySearchDatas);
                    mRv.setAdapter(mAdapter);
//                    mRv.addItemDecoration(mDecoration = new SuspensionDecoration(MeituanSelectCityActivity.this, mSourceDatas)
//                            .setmTitleHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics()))
//                            .setColorTitleBg(0xffefefef)
//                            .setTitleFontSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()))
//                            .setColorTitleFont(mContext.getResources().getColor(android.R.color.black))
//                            .setHeaderViewCount(mHeaderAdapter.getHeaderViewCount() - mHeaderDatas.size()));
//                    mRv.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
//
//
//
//                    mIndexBar.setmPressedShowTextView(mTvSideBarHint)//设置HintTextView
//                            .setNeedRealIndex(true)//设置需要真实的索引
//                            .setmLayoutManager(mManager)//设置RecyclerView的LayoutManager
//                            .setHeaderViewCount(mHeaderAdapter.getHeaderViewCount() - mHeaderDatas.size());
                } else
                {

                }
            }

        }
    }

}
