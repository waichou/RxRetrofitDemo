package com.jimi_wu.easyrxretrofit;

import android.support.annotation.NonNull;

import com.jimi_wu.easyrxretrofit.download.DownLoadService;
import com.jimi_wu.easyrxretrofit.download.DownLoadTransformer;
import com.jimi_wu.easyrxretrofit.exception.ExceptionHandle;
import com.jimi_wu.easyrxretrofit.exception.ServerException;
import com.jimi_wu.easyrxretrofit.model.BaseModel;
import com.jimi_wu.easyrxretrofit.upload.UploadOnSubscribe;
import com.jimi_wu.easyrxretrofit.upload.UploadRequestBody;
import com.jimi_wu.easyrxretrofit.utils.FileUtils;

import org.reactivestreams.Publisher;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * Created by zhouwei on 2018/8/15.
 */

public class RetrofitBase {
    protected   RetrofitManager mRetrofitManager = RetrofitManager.getInstance();

    /**
     * 创建请求
     */
    public  <T> T createService(final Class<T> service) {
        return mRetrofitManager.getRetrofit().create(service);
    }

    /**
     * 转换器
     * from BaseModel<T>
     * to T
     */
    public  <T> FlowableTransformer<BaseModel<T>, T> handleResult() {
        return new FlowableTransformer<BaseModel<T>, T>() {
            @Override
            public Publisher<T> apply(@NonNull Flowable<BaseModel<T>> upstream) {
                return upstream.flatMap(new Function<BaseModel<T>, Publisher<T>>() {
                    @Override
                    public Publisher<T> apply(@NonNull BaseModel<T> tBaseModel) throws Exception {
                        if (!tBaseModel.isError()) {
                            return createData(tBaseModel.getResult());
                        } else {
                            return Flowable.error(new ServerException(tBaseModel.getMsg(), tBaseModel.getErrorCode()));
                        }
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 创建Flowable<T>
     */
    public  <T> Flowable<T> createData(final T result) {
        return Flowable.create(new FlowableOnSubscribe<T>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<T> e) throws Exception {
                try {
                    e.onNext(result);
                    e.onComplete();
                } catch (Exception exception) {
                    e.onError(exception);
                }
            }
        }, BackpressureStrategy.BUFFER);
    }

    /**
     * 上传单个文件
     */
    public  <T> Flowable<Object> uploadSimpleFile(String url,File file,UploadFileForOnlyOneListener uploadFileForOnlyOne) {
        //进度Observable
        UploadOnSubscribe uploadOnSubscribe = new UploadOnSubscribe(file.length());
        Flowable<Integer> progressObservale = Flowable.create(uploadOnSubscribe, BackpressureStrategy.BUFFER);

        UploadRequestBody uploadRequestBody = new UploadRequestBody(file);
        //设置进度监听
        uploadRequestBody.setUploadOnSubscribe(uploadOnSubscribe);

        //创建表单主体
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("upload", file.getName(), uploadRequestBody);

        try {
            //调用上传接口
            Flowable uploadFlowable =  uploadFileForOnlyOne.uploadFile(url,filePart);

            //合并Observable
            return Flowable.merge(progressObservale, uploadFlowable)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        } catch (Exception e) {
            e.printStackTrace();
            return Flowable.error(new ExceptionHandle.ResponeThrowable(e));
        }
    }

    public interface UploadFileForOnlyOneListener{
        public Flowable uploadFile(String url,MultipartBody.Part filePart);

    }

    public interface UploadFileForMoreListener{
        public Flowable uploadFiles(String url,ArrayList<MultipartBody.Part> fileParts);

    }

    /**
     * 上传多个文件
     */
    public <T> Flowable<Object> uploadFileForMulit(String url,ArrayList<File> files,UploadFileForMoreListener uploadFileForMoreListener) {
        //总长度
        long sumLength = 0l;
        for (File file : files) {
            sumLength += file.length();
        }

        //进度Observable
        UploadOnSubscribe uploadOnSubscribe = new UploadOnSubscribe(sumLength);
        Flowable<Integer> progressObservale = Flowable.create(uploadOnSubscribe, BackpressureStrategy.BUFFER);

        ArrayList<MultipartBody.Part> fileParts = new ArrayList<>();

        for (File file : files) {
            UploadRequestBody uploadRequestBody = new UploadRequestBody(file);
            //设置进度监听
            uploadRequestBody.setUploadOnSubscribe(uploadOnSubscribe);

            fileParts.add(MultipartBody.Part.createFormData("upload", file.getName(), uploadRequestBody));
        }

        try {
                Flowable uploadFlowable = uploadFileForMoreListener.uploadFiles(url,fileParts);

                //合并Observable
                return Flowable.merge(progressObservale, uploadFlowable)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
        } catch (Exception e) {
            e.printStackTrace();
            return Flowable.error(e);
        }
    }

    //---------------------------

    /**
     * 下载文件[下载到默认的路径下：内部存储空间/Download/ , 文件名同url源文件名称]
     */
    public  Flowable<Object> downLoadFile(String url) {
        return downLoadFile(url, null, null);
    }

    /**
     * 下载文件
     */
    public  Flowable<Object> downLoadFile(String url, String savePath, String fileName) {
        //如果没有下载的保存路径 和 保存文件的文件名 ，则采用默认的路径和文件名
        if (savePath == null || savePath.trim().equals("")) {
            savePath = FileUtils.getDefaultDownLoadPath();
        }
        if (fileName == null || fileName.trim().equals("")) {
            fileName = FileUtils.getDefaultDownLoadFileName(url);
        }

//        下载监听
        DownLoadTransformer downLoadTransformer = new DownLoadTransformer(savePath, fileName);

        return Flowable
                .just(url)
                .flatMap(new Function<String, Publisher<ResponseBody>>() {
                    @Override
                    public Publisher<ResponseBody> apply(@NonNull String s) throws Exception {
                        DownLoadService downLoadService = RetrofitUtils.createService(DownLoadService.class);
                        return downLoadService.startDownLoad(s);
                    }
                })
                .compose(downLoadTransformer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private class HttpResponseFunc<T> implements Function<Throwable, Observable<T>> {

        @Override
        public Observable<T> apply(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
            return Observable.error(ExceptionHandle.handleException(throwable));
        }
    }

}
