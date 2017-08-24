package com.empty.citylistchange;

import android.app.Application;
import android.content.Context;

/**
 * Created by dawn on 2017/2/26.
 */

public class MyApplication  extends Application {
    public static Context mContext;

    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();
    }
}
