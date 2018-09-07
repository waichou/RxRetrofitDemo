package com.jimi_wu.sample;


import com.jimi_wu.easyrxretrofit.RetrofitBase;
import com.jimi_wu.easyrxretrofit.RetrofitUtils;
import com.jimi_wu.easyrxretrofit.download.DownLoadService;
import com.jimi_wu.easyrxretrofit.subscriber.EasySubscriber;
import com.jimi_wu.easyrxretrofit.subscriber.UploadSubscriber;
import com.jimi_wu.sample.apiservice.FileUploadService;
import com.jimi_wu.sample.apiservice.FilesUploadService;
import com.jimi_wu.sample.apiservice.GetUserService;
import com.jimi_wu.sample.model.FileBean;
import com.jimi_wu.sample.model.ResultBean;
import com.jimi_wu.sample.model.UserBean;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import io.reactivex.Flowable;
import okhttp3.MultipartBody;

/**
 * Created by Administrator on 2016/9/5.
 */
public class RetrofitUtilsTest extends RetrofitBase {

    public static RetrofitUtilsTest mRetrofitTest = new RetrofitUtilsTest();

    //---------------------- 涉及到的请求接口 -----------------------------
    private GetUserService mGetUserService = createService(GetUserService.class);

    private FileUploadService mFileUploadService = createService(FileUploadService.class);

    private FilesUploadService mFilesUploadService = createService(FilesUploadService.class);

    private DownLoadService mDownloadService = createService(DownLoadService.class);


    //----------------------------------------------get----------------------------
    public void get(EasySubscriber<UserBean> subscriber) {
        mGetUserService.start()
                .compose(RetrofitUtils.<UserBean>handleResult())
                .subscribe(subscriber);
    }

    //----------------------------------------------get----------------------------

    //-------------------------------------------upload simple file start ------------------------
    /**
     * 上传单个文件
     */
    public void uploadFileForOnlyOne(String url, File file, UploadSubscriber uploadSubscriber) {
        Flowable<Object> objectFlowable = super.uploadSimpleFile(url, file, new UploadFileForOnlyOneListener() {
            @Override
            public Flowable uploadFile(String url, MultipartBody.Part filePart) {
                return mFileUploadService.uploadFile2NetWorkForRxJava(url,filePart);
            }
        });
        objectFlowable.subscribe(uploadSubscriber);
    }

    //-------------------------------------------upload simple file end ------------------------


    //-------------------------------------------download mulit file start ------------------------
    public void uploadMulitFile2Server(String url,ArrayList<File> files,UploadSubscriber uploadSubscriber){
        super.uploadFileForMulit(url, files, new UploadFileForMoreListener() {
            @Override
            public Flowable uploadFiles(String url, ArrayList<MultipartBody.Part> fileParts) {
                return (Flowable<ResultBean<FileBean>>) mFilesUploadService.uploadMoreFile2NetWorkForRxJava(url,fileParts);
            }
        }).subscribe(uploadSubscriber);
    }

    //-------------------------------------------download simple file start ------------------------

    public void downloadSimpleFile(String url, String savePath, String fileName){
        downLoadFile(url,savePath,fileName);
    }
}

