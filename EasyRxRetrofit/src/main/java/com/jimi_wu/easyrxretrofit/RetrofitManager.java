package com.jimi_wu.easyrxretrofit;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.jimi_wu.easyrxretrofit.agent.AgentInterceptor;
import com.jimi_wu.easyrxretrofit.cookie.CookieManager;
import com.jimi_wu.easyrxretrofit.intorceptor.HttpCommonInterceptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by wzm on 2017/6/14.
 */

public class RetrofitManager {
    private boolean DEBUG = true;
    private String mAgent;
    private String mBaseUrl;
    private ArrayList<Converter.Factory> mConverterFactorys = new ArrayList<>();
    private ArrayList<CallAdapter.Factory> mCallAdapterFactory = new ArrayList<>();
    private ArrayList<Interceptor> mInterceptor = new ArrayList<>();
    private CookieJar mCookieJar;

    private OkHttpClient mOkHttpClient;
    private Retrofit mRetrofit;
    private OkHttpClient.Builder okHttpClientBuilder;

    private HttpCommonInterceptor.Builder commonInterceptorBuilder;

    private static final int DEFAULT_CONNECT_TIMEOUT = 30;
    private static final int DEFAULT_WRITE_TIMEOUT = 30;
    private static final int DEFAULT_READ_TIMEOUT = 30;

    private RetrofitManager() {
    }

    private static RetrofitManager mRetrofitManager;

    public synchronized static RetrofitManager getInstance() {
        if(mRetrofitManager == null)
            mRetrofitManager = new RetrofitManager();
        return mRetrofitManager;
    }

    public RetrofitManager setBaseUrl(String baseUrl) {
        this.mBaseUrl = baseUrl;
        return this;
    }

    public RetrofitManager setAgent(String agent) {
        this.mAgent = agent;
        return this;
    }

    public RetrofitManager addConverterFactory(Converter.Factory factory) {
        this.mConverterFactorys.add(factory);
        return this;
    }

    public RetrofitManager addCallAdapterFactory(CallAdapter.Factory factory) {
        this.mCallAdapterFactory.add(factory);
        return this;
    }

    public void init(Context context) {
        if (mBaseUrl == null) throw new IllegalArgumentException("base url is required.");

        mConverterFactorys.add(GsonConverterFactory.create());
        mCallAdapterFactory.add(RxJava2CallAdapterFactory.create());

        okHttpClientBuilder = new OkHttpClient.Builder();
        //
        if(mCookieJar == null) mCookieJar = new CookieManager(context);
        okHttpClientBuilder.cookieJar(mCookieJar);
        //
        if(mAgent != null) mInterceptor.add(new AgentInterceptor(mAgent));
        //
        if (commonInterceptorBuilder != null)
            mInterceptor.add(commonInterceptorBuilder.build());

        for(Interceptor interceptor : mInterceptor) {
            okHttpClientBuilder.addInterceptor(interceptor);
        }

        /**
         * 设置缓存-----------------------------------------------cache start
         * 参考-缓存配置：https://blog.csdn.net/qqyanjiang/article/details/51316116
         */
        final String CACHE_NAME = "zzz_cache";
        File cacheFile = new File(Environment.getExternalStorageDirectory(), CACHE_NAME);
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 50);

        final Interceptor cacheInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                if (NetworkUtils.isConnected()) {
                    Response response = chain.proceed(request);
                    // read from cache for 60 s
                    int maxAge = 0;
                    String cacheControl = request.cacheControl().toString();
                    Log.e("Tamic", "60s load cahe" + cacheControl);
                    return response.newBuilder()
                            .removeHeader("Pragma")
                            .removeHeader("Cache-Control")
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .build();
                } else {
                    Log.e("Tamic", " no network load cahe");
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                    Response response = chain.proceed(request);
                    //set cahe times is 3 days
                    int maxStale = 60 * 60 * 24 * 3;
                    return response.newBuilder()
                            .removeHeader("Pragma")
                            .removeHeader("Cache-Control")
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .build();
                }
            }
        };
        //----cache -end
        okHttpClientBuilder.cache(cache).addNetworkInterceptor(cacheInterceptor).addInterceptor(cacheInterceptor);

        //设置 Debug Log 模式
        if (DEBUG){
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpClientBuilder.addNetworkInterceptor(loggingInterceptor);
        }

        /**
         * 设置超时和重新连接
         */
        okHttpClientBuilder.connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        okHttpClientBuilder.readTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS);
        okHttpClientBuilder.writeTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS);

        //错误重连
        okHttpClientBuilder.retryOnConnectionFailure(true);

        //构建okhttpClient对象
        this.mOkHttpClient = okHttpClientBuilder.build();


        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
        retrofitBuilder.client(mOkHttpClient);
        retrofitBuilder.baseUrl(mBaseUrl);

        for (Converter.Factory factory : mConverterFactorys) {
            retrofitBuilder.addConverterFactory(factory);
        }
        for (CallAdapter.Factory factory : mCallAdapterFactory) {
            retrofitBuilder.addCallAdapterFactory(factory);
        }

        this.mRetrofit = retrofitBuilder.build();

//        RetrofitUtils.setRetrofitManager(this);
    }


    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    //封装额外请求参数
    public RetrofitManager setHttpcommonInterceptor(Map<String,String> requestParamMap){
        commonInterceptorBuilder = new HttpCommonInterceptor.Builder();
        Set<Map.Entry<String, String>> entries = requestParamMap.entrySet();
        Iterator<Map.Entry<String, String>> iterator = entries.iterator();
        while(iterator.hasNext()){
            Map.Entry<String, String> next = iterator.next();
            String key = next.getKey();
            String value = next.getValue();
            commonInterceptorBuilder.addHeaderParams(key,value);
        }
        return mRetrofitManager;
    }

    public RetrofitManager isDeBug(boolean isDeBug){
        this.DEBUG = isDeBug;
        return mRetrofitManager;
    }
}
