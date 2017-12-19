package com.wilddog.toolbar.util;


import android.util.Log;

import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by he on 2017/8/30.
 */

public class QiniuUtil {
    private static final String TAG = "QiniuUtil";
    //token 获取地址 http://courseware.wilddogapp.com/uptoken
    private String tokenUrl = "http://courseware.wilddogapp.com/uptoken";
    private static QiniuUtil instance;
    private UploadManager uploadManager;
    private String oldToken="Bo7KgOB3js5PUaZkFO-45byYGEjUMANo9mDhCm8S:Me2PtEndlpB0N6F5ZtD32_zFx8U=:eyJzY29wZSI6ImRvZGV0b3AiLCJkZWFkbGluZSI6MTYxMDA1ODMyMTU3OX0=";
    private String token=   "iuYyEIqScKViXRSolvrZ3ZIr-JMqka8LrWQTNvbl:o-2c0-RbNWYbKvZfrsF4hErvDi4=:eyJkZWxldGVBZnRlckRheXMiOjEsInJldHVybkJvZHkiOiJ7XCJrZXlcIjpcIiQoa2V5KVwiLFwibmFtZVwiOiAkKGZuYW1lKSxcInNpemVcIjogJChmc2l6ZSksXCJ3XCI6ICQoaW1hZ2VJbmZvLndpZHRoKSxcImhcIjogJChpbWFnZUluZm8uaGVpZ2h0KSxcImhhc2hcIjogJChldGFnKX0iLCJzY29wZSI6IndoaXRlYm9hcmQiLCJkZWFkbGluZSI6MTUxMzI0MDI4N30=";
    private String url;

    private QiniuUtil() {
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(tokenUrl).get().build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("qiniu","获取token失败,IOException"+e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
             if(response.isSuccessful()){
                  String result = response.body().string();
                  Log.e("qiniu",result);
                 try {
                     JSONObject jsonObject = new JSONObject(result);
                     token = jsonObject.getString("uptoken");
                 } catch (JSONException e) {
                     e.printStackTrace();
                     Log.e("qiniu","获取token成功,解析json失败,Exception:"+e);
                 }
             }else {
                 Log.e("qiniu","获取token失败,error:"+response.body().string());
             }
            }
        });
    }
    public static QiniuUtil getInstance() {
        if (instance == null) {
            synchronized (QiniuUtil.class) {
                if (instance == null) {
                    instance = new QiniuUtil();
                }
            }
        }
        return instance;
    }

    public String getToken() {
        return token;
    }

    public void init(String token, String url){
        this.token = token;
        this.url = url;
        uploadManager = new UploadManager();
    }

    public void init(String url){
        this.url=url;
        uploadManager = new UploadManager();
    }

    public void init(){
        uploadManager = new UploadManager();
    }

    public UploadManager getUploadManager() {
        return uploadManager;
    }

    public String getOldURL(String key) {
        return "https://oisha19l0.qnssl.com/"+key;
    }
    public String getURL(String key) {
        return "https://whiteboard-img.wdstatic.cn/"+key;
    }
}
