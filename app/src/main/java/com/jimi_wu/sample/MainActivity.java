package com.jimi_wu.sample;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jimi_wu.easyrxretrofit.RetrofitUtils;
import com.jimi_wu.easyrxretrofit.subscriber.DownLoadSubscriber;
import com.jimi_wu.easyrxretrofit.subscriber.UploadSubscriber;
import com.jimi_wu.easyrxretrofit.subscriber.EasySubscriber;
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
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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
                RetrofitUtils
                        .createService(GetUserService.class)
                        .start()
                        .compose(RetrofitUtils.<UserBean>handleResult())
                        .subscribe(new EasySubscriber<UserBean>(this) {

                            @Override
                            protected void _onNext(UserBean userBean) {
                                Log.i("retrofit", "onNext=========>" + userBean.getUsername());
                                tv.setText("请求成功:"+userBean.getUsername());
                            }

                            @Override
                            protected void _onError(int errorCode, String msg) {
                                Log.i("retrofit", "onError=========>" + msg);
                                tv.setText("请求失败:"+msg);
                            }
                        });
                break;
            case R.id.btn_upload://上传单个文件
                upload(new File(Environment.getExternalStorageDirectory() + "/123.pdf"));
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
        RetrofitUtils
                .uploadFile(file,
                            FileUploadService.class,
                            "uploadFile2NetWorkForRxJava",
                             new Class[]{String.class},
                            new Object[]{"SessionExample/servlet/LoginServlet"})
                .safeSubscribe(new UploadSubscriber<FileBean>(this) {

                    @Override
                    public void showDialog() {
                        Toast.makeText(MainActivity.this,"开始上传文件，Loading!",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void dismissDialog() {
                        Toast.makeText(MainActivity.this,"完成上传文件，Dismiss!",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void cancelDialog() {
                        Toast.makeText(MainActivity.this,"取消上传文件，Cancel!",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void _onNext(FileBean result) {
                        tv.setText("上传完毕：" + result.getUrl());
                    }

                    @Override
                    protected void _onProgress(Integer percent) {
                        Log.i("retrofit", "onProgress======>"+percent);
                        tv.setText("上传中:"+percent);
                    }

                    @Override
                    protected void _onError(int errorCode, String msg) {
                        Log.i("retrofit", "onError======>"+msg);
                        tv.setText("上传失败:"+msg);
                    }
                });
    }


    /**
     * 多图上传
     */
    public void uploads(ArrayList<File> files) {
        RetrofitUtils
                .uploadFiles(files, FilesUploadService.class, "uploadMoreFile2NetWorkForRxJava",
                        new Class[]{String.class},
                        new Object[]{"SessionExample/servlet/LoginServlet"}
                        )
                .safeSubscribe(new UploadSubscriber<FileBean>(this) {

                    @Override
                    protected void _onNext(FileBean result) {
                        tv.setText("上传成功:"+result.getUrl());
                    }

                    @Override
                    protected void _onProgress(Integer percent) {
                        Log.i("retrofit", "onProgress=======>"+percent);
                        tv.setText("上传中:"+percent);
                    }

                    @Override
                    protected void _onError(int errorCode, String msg) {
                        Log.i("retrofit", "onError======>"+msg);
                        tv.setText("上传失败:"+msg);
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
