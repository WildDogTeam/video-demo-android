package com.wilddog.conversation.wilddog;

import android.util.Log;

import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.ValueEventListener;
import com.wilddog.client.WilddogSync;
import com.wilddog.conversation.ConversationApplication;
import com.wilddog.conversation.bean.UserInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fly on 17-8-16.
 */

public class WilddogSyncManager {
    private static SyncReference syncReference;
    private static WilddogSyncManager tool = new WilddogSyncManager();
    public static String ONLINEUSER = "users/";
    private WilddogSyncManager(){

    }

    public static WilddogSyncManager getWilddogSyncTool(){
        if(!WilddogManager.IsInit()){
            WilddogManager.initWilddog(ConversationApplication.getContext().getApplicationContext());
        }
        if(syncReference==null){
            WilddogSync wilddogSync = WilddogSync.getInstance();
             syncReference = wilddogSync.getReference();
        }
        return tool;
    }


    public void writeToUser(String uid) {
        syncReference.child(ONLINEUSER + uid).setValue(true, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError error, SyncReference ref) {
                if (error != null) {
                    Log.e("error", error.toString());
                }
            }
        });
        syncReference.child(ONLINEUSER + uid).onDisconnect().removeValue();
    }

    public void writeToUserInfo(UserInfo info){
        Map user = new HashMap();
        user.put("faceurl",info.getFaceurl());
        user.put("nickname",info.getNickname());
        user.put("deviceid",info.getDeviceid());
        //TODO deviceid
        syncReference.child(ONLINEUSER+info.getUid()).setValue(user, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError error, SyncReference syncReference) {
                if (error != null) {
                    Log.e("error", error.toString());
                }
            }
        });
        syncReference.child(ONLINEUSER + info.getUid()).onDisconnect().removeValue();
    }

    public void  removeUserInfo(String uid){
        syncReference.child(ONLINEUSER + uid).removeValue();
    }

    public void getonlineUserInfos(ValueEventListener listener){
        syncReference.child(ONLINEUSER).addListenerForSingleValueEvent(listener);
    }

}
