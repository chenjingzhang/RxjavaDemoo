package com.dxt2.rxjavandretro.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2018/4/27 0027.
 */

public class MyApplication extends Application{
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
