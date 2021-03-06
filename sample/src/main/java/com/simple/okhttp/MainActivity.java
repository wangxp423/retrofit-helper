package com.simple.okhttp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.simple.converter.FileConverterFactory;
import com.simple.entity.Article;
import com.simple.entity.LoginInfo;
import com.simple.entity.WXArticle;
import com.xcheng.retrofit.Call2;
import com.xcheng.retrofit.CallManager;
import com.xcheng.retrofit.Callback2;
import com.xcheng.retrofit.HttpError;
import com.xcheng.retrofit.RetrofitFactory;
import com.xcheng.retrofit.progress.ProgressInterceptor;
import com.xcheng.retrofit.progress.ProgressListener;
import com.xcheng.view.EasyView;
import com.xcheng.view.controller.EasyActivity;
import com.xcheng.view.widget.ProgressView;

import java.io.File;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;

public class MainActivity extends EasyActivity {
    ProgressView progressView;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        progressView = findViewById(R.id.progress);
        progressView.setTextColor(Color.RED);
        progressView.setTextSize(30);
        progressView.setProgressViewTextGenerator(new ProgressView.ProgressViewTextGenerator() {
            @Override
            public String generateText(ProgressView progressBar, int value, int maxValue) {
                return String.valueOf(value) + "%";
            }
        });
    }

    public void login(View view) {
        RetrofitFactory.create(ApiService.class)
                .getLogin("singleman", "123456")
                .enqueue(hashCode(), new AnimCallback<LoginInfo>(this) {
                    @Override
                    public void onError(Call2<LoginInfo> call2, HttpError error) {
                        Toast.makeText(MainActivity.this, error.msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(Call2<LoginInfo> call2, LoginInfo response) {
                        Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void wxarticle(View view) {
        RetrofitFactory.create(ApiService.class)
                .getWXarticle()
                .enqueue(hashCode(), new AnimCallback<List<WXArticle>>(this) {
                    @Override
                    public void onError(Call2<List<WXArticle>> call2, HttpError error) {
                        Toast.makeText(MainActivity.this, error.msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(Call2<List<WXArticle>> call2, List<WXArticle> response) {
                        Toast.makeText(MainActivity.this, "获取公众号列表成功", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    public void article0(View view) {
        RetrofitFactory.create(ApiService.class)
                .getArticle0()
                .enqueue(hashCode(), new AnimCallback<List<Article>>(this) {
                    @Override
                    public void onError(Call2<List<Article>> call2, HttpError error) {
                        Toast.makeText(MainActivity.this, error.msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(Call2<List<Article>> call2, List<Article> response) {
                        Toast.makeText(MainActivity.this, "获取首页列表成功", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void progress(View view) {
        RetrofitFactory.create(ApiService.class)
                .getArticle0()
                .enqueue(hashCode(), new AnimCallback<List<Article>>(this) {
                    @Override
                    public void onError(Call2<List<Article>> call2, HttpError error) {
                        Toast.makeText(MainActivity.this, error.msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(Call2<List<Article>> call2, List<Article> response) {
                        Toast.makeText(MainActivity.this, "获取首页列表成功", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    static final String TAG_LOAD_APK = "loadApk";

    public void download(View view) {
        final Button button = (Button) view;
        if (button.getText().equals("取消下载")) {
            CallManager.getInstance().cancel(TAG_LOAD_APK);
            return;
        }

        String filePath = new File(getContext().getExternalCacheDir(), "test_douyin.apk").getPath();
        //构建可以监听进度的client
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addNetworkInterceptor(getProgressInterceptor()).build();

        //构建可以下载文件的client
        Retrofit retrofit = RetrofitFactory.DEFAULT
                .newBuilder()
                .callFactory(client)
                .addConverterFactory(new FileConverterFactory(filePath))
                .build();
        retrofit.create(ApiService.class)
                .loadDouYinApk()
                .enqueue(TAG_LOAD_APK, new Callback2<File>() {
                    @Override
                    public void onStart(Call2<File> call2) {
                        button.setText("取消下载");
                    }

                    @Override
                    public void onError(Call2<File> call2, HttpError error) {
                        progressView.setProgress(0);
                        Toast.makeText(MainActivity.this, error.msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(Call2<File> call2, File response) {

                    }

                    @Override
                    public void onCompleted(Call2<File> call2, @Nullable Throwable t, boolean canceled) {
                        if (canceled) {
                            progressView.setProgress(0);
                            button.setText("下载抖音apk文件");
                        } else {
                            button.setText("下载完成");
                        }
                    }
                });
    }

    private ProgressInterceptor getProgressInterceptor() {
        return new ProgressInterceptor(new ProgressListener() {
            @Override
            public void onUpload(Request request, long progress, long contentLength, boolean done) {

            }

            @Override
            public void onDownload(Request request, final long progress, final long contentLength, boolean done) {
                EasyView.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressView.setProgress((int) (progress * 100f / contentLength), false);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //hashCode() 能保证唯一性，取消当前页面所发起的所有请求，只要
        // enqueue(tag, callback2) 传入的是对应的hashCode() 即可
        CallManager.getInstance().cancel(hashCode());
        //取消下载文件
        CallManager.getInstance().cancel(TAG_LOAD_APK);
    }
}