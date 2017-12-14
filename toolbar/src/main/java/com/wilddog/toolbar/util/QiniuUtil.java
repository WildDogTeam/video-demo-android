package com.wilddog.toolbar.util;


import com.qiniu.android.storage.UploadManager;

/**
 * Created by he on 2017/8/30.
 */

public class QiniuUtil {
    private static final String TAG = "QiniuUtil";

    private static QiniuUtil instance;
    private UploadManager uploadManager;
    private String oldToken="Bo7KgOB3js5PUaZkFO-45byYGEjUMANo9mDhCm8S:Me2PtEndlpB0N6F5ZtD32_zFx8U=:eyJzY29wZSI6ImRvZGV0b3AiLCJkZWFkbGluZSI6MTYxMDA1ODMyMTU3OX0=";
    private String token=   "qSIHb2rWYZNiE80039Uyk7PpiaWTNwdPnRsvzmM-:k9AqGu8yaLNIv-4VpJxZbVc2SMs=:eyJkZWxldGVBZnRlckRheXMiOjEsInJldHVybkJvZHkiOiJ7XCJrZXlcIjpcIiQoa2V5KVwiLFwibmFtZVwiOiAkKGZuYW1lKSxcInNpemVcIjogJChmc2l6ZSksXCJ3XCI6ICQoaW1hZ2VJbmZvLndpZHRoKSxcImhcIjogJChpbWFnZUluZm8uaGVpZ2h0KSxcImhhc2hcIjogJChldGFnKX0iLCJzY29wZSI6IndoaXRlYm9hcmQiLCJkZWFkbGluZSI6MTUxMzE0OTI0Mn0=";
    private String url;

    private QiniuUtil() {
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

    public String getURL(String key) {
        return "https://oisha19l0.qnssl.com/"+key;
    }
}
