package com.jimi_wu.easyrxretrofit.subscriber;

import android.app.ProgressDialog;
import android.content.Context;


import com.google.gson.Gson;
import com.jimi_wu.easyrxretrofit.exception.ExceptionHandle;
import com.jimi_wu.easyrxretrofit.exception.ServerException;
import com.jimi_wu.easyrxretrofit.interfaces.IShowProgressListener;
import com.jimi_wu.easyrxretrofit.model.BaseModel;
import com.jimi_wu.easyrxretrofit.utils.NetworkUtils;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Created by wzm on 2017/6/11.
 */

public abstract class UploadSubscriber<T> implements Subscriber<Object> ,IShowProgressListener{

    protected Context mContext;

    public UploadSubscriber(Context context) {
        this.mContext = context;
    }

    protected Subscription mSubscription;


    @Override
    public void onSubscribe(Subscription s) {
        this.mSubscription = s;
        mSubscription.request(1);
        //弹出对话框
        showDialog();
    }

    @Override
    public void onComplete() {
        //取消对话框
        cancelDialog();
    }

    @Override
    public void onNext(Object o) {//疑问：推送100%的时候，服务端是否能将返回结果推送回来？我的答案是“不能”
        if (o instanceof Integer) {
            Integer progress = (Integer) o;
            _onProgress(progress);
            if (progress == 100){
                //取消对话框
                dismissDialog();
            }
        }

        if(o instanceof BaseModel) {
            String json = new Gson().toJson(o);

            BaseModel baseModel = (BaseModel) o;
            if(baseModel.isError()) {
                _onError(baseModel.getErrorCode(),baseModel.getMsg());
            }
            else {
                if(baseModel.getResult() != null) {
                    _onNext((T)baseModel.getResult());
                }
            }
        }
        mSubscription.request(1);//这句话就可以控制接收服务端返回的数据了！
    }

    @Override
    public void onError(Throwable e) {
        //取消对话框
        dismissDialog();

        ExceptionHandle.ResponeThrowable responeThrowable = ExceptionHandle.handleException(e);
        _onError( responeThrowable.code,responeThrowable.message);

    }

    protected abstract void _onNext(T result);

    protected abstract void _onProgress(Integer percent);

    protected abstract void _onError(int errorCode,String msg);

    @Override
    public void showDialog() {

    }

    @Override
    public void dismissDialog() {

    }

    @Override
    public void cancelDialog() {

    }
}
