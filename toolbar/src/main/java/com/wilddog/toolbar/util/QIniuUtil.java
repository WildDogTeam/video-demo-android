package com.wilddog.toolbar.util;


import android.util.Log;

import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by he on 2017/8/30.
 */

public class QIniuUtil {
    private static final String TAG = "QIniuUtil";

    private static QIniuUtil instance;
    private UploadManager uploadManager;
    private String token="Bo7KgOB3js5PUaZkFO-45byYGEjUMANo9mDhCm8S:Me2PtEndlpB0N6F5ZtD32_zFx8U=:eyJzY29wZSI6ImRvZGV0b3AiLCJkZWFkbGluZSI6MTYxMDA1ODMyMTU3OX0=";
    private String url;

    private QIniuUtil() {
    }
    public static QIniuUtil getInstance() {
        if (instance == null) {
            synchronized (QIniuUtil.class) {
                if (instance == null) {
                    instance = new QIniuUtil();
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

    public String getURL(String key) {
        return "https://oisha19l0.qnssl.com/"+key;
    }
}
