package com.empty.citylistchange;

import java.util.List;

/**
 * 介绍：美团最顶部Header
 * 作者：zhangxutong
 * 邮箱：mcxtzhang@163.com
 * CSDN：http://blog.csdn.net/zxt0601
 * 时间： 16/11/28.
 */

public class MeituanTopHeaderBean {
    private String txt;
    private List<String> cityList;

    public List<String> getCityList() {
        return cityList;
    }

    public void setCityList(List<String> cityList) {
        this.cityList = cityList;
    }

    public MeituanTopHeaderBean(String txt,List<String> cityList) {
        this.txt = txt;
        this.cityList = cityList;
    }

    public String getTxt() {
        return txt;
    }

    public MeituanTopHeaderBean setTxt(String txt) {
        this.txt = txt;
        return this;
    }

}
