package com.empty.citylistchange;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by empty cup on 2017/8/24.
 */

public class ResolveCity {
    private Context context;
    private JSONObject mJsonObj;
    /**
     * key - 省 value - 市s
     */
    private Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
    /**
     * key - 市 values - 区s
     */
    public Map<String, String[]> mAreaDatasMap = new HashMap<String, String[]>();
    /**
     * 所有省
     */
    private String[] mProvinceDatas;

    public ResolveCity (Context context){
        this.context = context;
    }

    /**
     * 从文件中读取地址数据
     */
    public void initJsonData() {
        try {
            StringBuffer sb = new StringBuffer();
            InputStream is = context.getClass().getClassLoader().getResourceAsStream("assets/" + "city.json");
            int len = -1;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, len, "utf-8"));
            }
            is.close();
            mJsonObj = new JSONObject(sb.toString());
            initDatas();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析整个Json对象，完成后释放Json对象的内存
     */
    public void initDatas()
    {
        try
        {
            JSONArray jsonArray = mJsonObj.getJSONArray("citylist");
            mProvinceDatas = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonP = jsonArray.getJSONObject(i);// 每个省的json对象
                String province = jsonP.getString("p");// 省名字

                mProvinceDatas[i] = province;

                JSONArray jsonCs = null;
                try
                {
                    /**
                     * Throws JSONException if the mapping doesn't exist or is
                     * not a JSONArray.
                     */
                    jsonCs = jsonP.getJSONArray("c");
                } catch (Exception e1)
                {
                    continue;
                }
                String[] mCitiesDatas = new String[jsonCs.length()];
                for (int j = 0; j < jsonCs.length(); j++)
                {
                    JSONObject jsonCity = jsonCs.getJSONObject(j);
                    String city = jsonCity.getString("n");// 市名字
                    mCitiesDatas[j] = city;

                    JSONArray jsonAreas = null;
                    String[] mAreasDatas;
                    if (jsonCity.has("a")){
                        try
                        {
                            /**
                             * Throws JSONException if the mapping doesn't exist or
                             * is not a JSONArray.
                             */
                            jsonAreas = jsonCity.getJSONArray("a");
                        } catch (Exception e)
                        {
                            continue;
                        }

                        mAreasDatas = new String[jsonAreas.length()];// 当前市的所有区
                        for (int k = 0; k < jsonAreas.length(); k++)
                        {
                            String area = jsonAreas.getJSONObject(k).getString("s");// 区域的名称
                            mAreasDatas[k] = area;
                        }

                    }else{
                        //只有区没有  市下没有区
                        mAreasDatas = new String[1];
                        mAreasDatas[0] = "";
                    }
                    mAreaDatasMap.put(city, mAreasDatas);
                }

                mCitisDatasMap.put(province, mCitiesDatas);
            }

        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        mJsonObj = null;
    }
}
