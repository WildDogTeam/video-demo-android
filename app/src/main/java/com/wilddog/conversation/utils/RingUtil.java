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
    public static boolean isRing = false;
    public static void paly( boolean isCalling,Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        assetManager = context.getAssets();
        mediaPlayer.reset();//重置为初始状态
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);

        try {
            if(isCalling){
                assetFileDescriptor =assetManager.openFd("gc.mp3");
            }else {
                assetFileDescriptor=assetManager.openFd("gd.mp3");
            }
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),assetFileDescriptor.getStartOffset(),assetFileDescriptor.getLength());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    isRing = true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            isRing = false;
        }
    }

    public static boolean isRing(){
        return isRing;
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
