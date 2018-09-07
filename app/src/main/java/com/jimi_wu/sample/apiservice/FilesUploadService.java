package com.jimi_wu.sample.apiservice;

import com.jimi_wu.sample.Constants;
import com.jimi_wu.sample.model.FileBean;
import com.jimi_wu.sample.model.ResultBean;

import java.util.ArrayList;

import io.reactivex.Flowable;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

/**
 * Created by wuzhiming on 2016/11/17.
 */

public interface FilesUploadService {

    //上传图片，txt ，音频，视频 文件 (都是单个|多个附件上传)
    @POST//@Body parameters cannot be used with form or multi-part encoding. (parameter #1)
    @Multipart //上传part的时候，如果不加此Mulitpart注解，则报错：Caused by: java.lang.IllegalArgumentException: @Part parameters can only be used with multipart encoding.
    Flowable<ResultBean<FileBean>> uploadMoreFile2NetWorkForRxJava(@Url String url, @Part ArrayList<MultipartBody.Part> part);


}
