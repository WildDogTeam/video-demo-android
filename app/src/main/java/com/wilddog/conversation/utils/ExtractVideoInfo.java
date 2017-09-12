package com.wilddog.conversation.utils;

import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by fly on 17-8-2.
 */

public class ExtractVideoInfo {
    private MediaMetadataRetriever mMetadataRetriever;
    private long fileLength = 0;//毫秒
    public ExtractVideoInfo(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException("path must be not null !");
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("path file not exists !");
        }
        if(file.length()==0){
            return;
        }
        mMetadataRetriever = new MediaMetadataRetriever();
        if(Build.VERSION.SDK_INT>=14){
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file.getAbsolutePath());
                mMetadataRetriever.setDataSource(inputStream.getFD());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
        mMetadataRetriever.setDataSource(file.getAbsolutePath());}
        String len = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        fileLength = TextUtils.isEmpty(len) ? 0 : Long.valueOf(len);
    }

    /***
     * 获取视频的长度时间
     *
     * @return String 毫秒
     */
    public String getVideoLength() {
        return String.valueOf(fileLength) ;
    }
    public void release() {
        if (mMetadataRetriever != null) {
            mMetadataRetriever.release();
        }
    }

}
