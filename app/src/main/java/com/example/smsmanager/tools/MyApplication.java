package com.example.smsmanager.tools;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        //获取Context
        context = getApplicationContext();
    }

    /**
     * 返回全局context
     * @return 全局context
     */
    public static Context getContextObject(){
        return context;
    }
}
