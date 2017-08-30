package com.wilddog.conversation.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;

/**
 * Created by fly on 17-8-29.
 */

public class RingUtil {

    public static MediaPlayer mediaPlayer;
    public static AssetManager assetManager ;
    public static AssetFileDescriptor assetFileDescriptor;
    public static void paly( boolean isCalling,Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        assetManager = context.getAssets();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);
        mediaPlayer.reset();//重置为初始状态
        try {
            if(isCalling){
                assetFileDescriptor =assetManager.openFd("/");
            }else {
                assetFileDescriptor=assetManager.openFd("/");
            }
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if(mediaPlayer!=null){
            mediaPlayer.stop();
        }
    }


    public static void destory() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        assetManager = null;
        assetFileDescriptor = null;
    }


}
