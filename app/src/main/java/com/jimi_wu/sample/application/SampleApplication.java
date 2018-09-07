package com.jimi_wu.sample.application;

import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.util.DeviceUtils;
import com.jimi_wu.easyrxretrofit.RetrofitManager;
import com.jimi_wu.sample.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/9.
 */
public class SampleApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        //添加公共参数
        Map<String,String> requestParamMap  = new HashMap<>();
        requestParamMap.put("deviceId", DeviceUtils.getAndroidID());

//        在此处初始化Retrofit
        RetrofitManager
                .getInstance()
                .isDeBug(true)
                .setBaseUrl(Constants.BASE_URL)   //设置baseUrl
                .setAgent("android mobile")  //设置agent
                .setHttpcommonInterceptor(requestParamMap)
                .init(this);

        mContext = this;
    }

    public static Context getAppContext(){
        return mContext;
    }


}
