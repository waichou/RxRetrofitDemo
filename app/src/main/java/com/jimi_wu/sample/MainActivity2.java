package com.jimi_wu.sample;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.jimi_wu.easyrxretrofit.RetrofitUtils;
import com.jimi_wu.easyrxretrofit.subscriber.DownLoadSubscriber;
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

/**
 * 正常移动客户端发起请求，上传文件时，先是将文件流媒体加载到内存中，之后才服务端开始接收请求，之后处理文件的读写操作，
 * 文件的读写要保证网络畅通，之后在服务端写完流文件之后，返回结果给客户端
 *
 * 对于ResultBean 返回类型而言，如果没有特殊的返回字段，则无需拓展，否则，可以继承ResultBean拓展属性
 */
public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {

    private static int REQUEST_CODE_UPLOAD = 1;
    private static int REQUEST_CODE_UPLOADS = 2;

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(this);
        findViewById(R.id.btn_upload).setOnClickListener(this);
        findViewById(R.id.btn_uploads).setOnClickListener(this);
        findViewById(R.id.btn_download).setOnClickListener(this);
        tv = (TextView) findViewById(R.id.tv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn://对文本数据类型的请求方式
                RetrofitUtilsTest
                        .mRetrofitTest
                        .get(new EasySubscriber<UserBean>(this) {

                            @Override
                            protected void _onNext(UserBean userBean) {
                                tv.setText("获取结果：" + userBean.getUsername()+"," + userBean.getPassword());
                            }

                            @Override
                            protected void _onError(int errorCode, String msg) {
                                tv.setText("发生错误："+ errorCode +"," + msg);
                            }
                        });
                break;



            case R.id.btn_upload://上传单个文件
                upload(new File(Environment.getExternalStorageDirectory() + "/123.png"));
                break;
            case R.id.btn_uploads://多个文件上传
                ArrayList<File> files = new ArrayList<>();
                files.add(new File(Environment.getExternalStorageDirectory() + "/123.png"));
                files.add(new File(Environment.getExternalStorageDirectory() + "/text.txt"));
                uploads(files);
                break;
            case R.id.btn_download:
                downLoad();
                break;
        }
    }

    /**
     * 单图上传
     */
    public void upload(File file) {
        RetrofitUtilsTest
                .mRetrofitTest
                .uploadFileForOnlyOne(Constants.GET_USER_URL, file, new UploadSubscriber<FileBean>(this) {
                    @Override
                    protected void _onNext(FileBean result) {
                        tv.setText("上传结束："+ result.getUrl());
                    }

                    @Override
                    protected void _onProgress(Integer percent) {
                        if (percent<=100) {
                            tv.setText("准备数据进度：" + percent);
                        }
                        if (percent == 100){
                            tv.setText("开始上传文件中！");
                        }
                    }

                    @Override
                    protected void _onError(int errorCode, String msg) {
                        tv.setText("发生错误："+ errorCode +"," + msg);
                    }
                });
    }

    /**
     * 多图上传
     */
    public void uploads(ArrayList<File> files) {
        RetrofitUtilsTest
                .mRetrofitTest
                .uploadMulitFile2Server(Constants.GET_USER_URL, files, new UploadSubscriber<FileBean>(this) {

                    @Override
                    protected void _onNext(FileBean result) {
                        tv.setText("上传结束："+ result.getUrl());
                    }

                    @Override
                    protected void _onProgress(Integer percent) {
                        if (percent<=100) {
                            tv.setText("准备数据进度：" + percent);
                        }
                        if (percent == 100){
                            tv.setText("开始上传文件中！");
                        }
                    }

                    @Override
                    protected void _onError(int errorCode, String msg) {
                        tv.setText("发生错误："+ errorCode +"," + msg);
                    }
                });
    }

    /**
     * 文件下载
     */
    public void downLoad() {
        RetrofitUtils
                .downLoadFile("SessionExample/image/123.zip")
                .subscribe(new DownLoadSubscriber(this) {
                    @Override
                    protected void _onNext(String result) {
                        Log.i("retrofit", "onNext=======>"+result);
                        tv.setText("下载成功:"+result);
                    }

                    @Override
                    protected void _onProgress(Integer percent) {
                        Log.i("retrofit", "onProgress=======>"+percent);
                        tv.setText("下载中:"+percent);
                    }

                    @Override
                    protected void _onError(int errorCode, String msg) {
                        Log.i("retrofit", "onProgress=======>"+msg);
                        tv.setText("下载失败:"+msg);
                    }
                });
    }


}
