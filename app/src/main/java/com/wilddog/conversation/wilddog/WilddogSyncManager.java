package com.wilddog.conversation.wilddog;

import android.util.Log;

import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.ValueEventListener;
import com.wilddog.client.WilddogSync;
import com.wilddog.conversation.ConversationApplication;
import com.wilddog.conversation.bean.UserInfo;

/**
 * Created by fly on 17-8-16.
 */

public class WilddogSyncManager {
    private static SyncReference syncReference;
    private static WilddogSyncManager tool = new WilddogSyncManager();

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
        syncReference.child("onlineusers/" + uid).setValue(true, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError error, SyncReference ref) {
                if (error != null) {
                    Log.e("error", error.toString());
                }
            }
        });
        syncReference.child("onlineusers/" + uid).onDisconnect().removeValue();
    }

    public void writeToUserInfo(UserInfo info){
        syncReference.child("onlineusers/"+info.getUid()).setValue(info, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError error, SyncReference syncReference) {
                if (error != null) {
                    Log.e("error", error.toString());
                }
            }
        });
        syncReference.child("onlineusers/" + info.getUid()).onDisconnect().removeValue();
    }

    public void  removeUserInfo(String uid){
        syncReference.child("onlineusers/" + uid).removeValue();
    }

    public void getonlineUserInfos(ValueEventListener listener){
        syncReference.child("onlineusers").addListenerForSingleValueEvent(listener);
    }

}
