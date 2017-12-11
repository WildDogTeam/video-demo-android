package com.wilddog.conversation.wilddog;

import android.util.Log;

import com.wilddog.client.DataSnapshot;
import com.wilddog.client.MutableData;
import com.wilddog.client.ServerValue;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.Transaction;
import com.wilddog.client.ValueEventListener;
import com.wilddog.client.WilddogSync;
import com.wilddog.conversation.ConversationApplication;
import com.wilddog.conversation.bean.Callback;
import com.wilddog.conversation.bean.ErrorCode;
import com.wilddog.conversation.bean.UserInfo;
import com.wilddog.conversation.bean.VideoError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fly on 17-8-16.
 */

public class WilddogSyncManager {
    private static SyncReference syncReference;
    private static WilddogSyncManager tool = new WilddogSyncManager();
    public static String ONLINEUSER = "users/";
    public static String ROOM = "room/";

    private WilddogSyncManager() {

    }

    public static WilddogSyncManager getWilddogSyncTool() {
        if (!WilddogManager.IsInit()) {
            WilddogManager.initWilddog(ConversationApplication.getContext().getApplicationContext());
        }
        if (syncReference == null) {
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

    public void writeToUserInfo(UserInfo info) {
        Map user = new HashMap();
        user.put("faceurl", info.getFaceurl());
        user.put("nickname", info.getNickname());
        user.put("deviceid", info.getDeviceid());
        //TODO deviceid
        syncReference.child(ONLINEUSER + info.getUid()).setValue(user, new SyncReference.CompletionListener() {
            @Override
            public void onComplete(SyncError error, SyncReference syncReference) {
                if (error != null) {
                    Log.e("error", error.toString());
                }
            }
        });
        syncReference.child(ONLINEUSER + info.getUid()).onDisconnect().removeValue();
    }

    public void removeUserInfo(String uid) {
        syncReference.child(ONLINEUSER + uid).removeValue();
    }

    public void getonlineUserInfos(ValueEventListener listener) {
        syncReference.child(ONLINEUSER).addListenerForSingleValueEvent(listener);
    }

    public void writeServerTimeStamp(final String roomId, final Callback<Boolean> callback) {
        syncReference.child(ROOM+roomId+"/users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null){
                    // 写time
                    syncReference.child(ROOM + roomId).removeValue();
                  /*  syncReference.child(ROOM + roomId + "/time").setValue(ServerValue.TIMESTAMP, new SyncReference.CompletionListener() {
                        @Override
                        public void onComplete(SyncError syncError, SyncReference syncReference) {
                            if(syncError==null){
                                callback.onSuccess(true);
                            }else {
                                callback.onFailed(new VideoError(syncError.getErrCode(),syncError.getMessage()));
                            }
                        }
                    });*/
                    syncReference.child(ROOM + roomId + "/time").runTransaction(new Transaction.Handler() {

                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            if (mutableData.getValue() == null) {
                                mutableData.setValue(ServerValue.TIMESTAMP);
                            }else {
                                Transaction.abort();
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(SyncError syncError, boolean b, DataSnapshot dataSnapshot) {
                            if (b) {
                                // 写入成功
                                if (callback != null) {
                                    callback.onSuccess(true);
                                }
                            } else {
                                if (syncError != null) {
                                    // 写入发生错误
                                    if (callback != null) {
                                        callback.onFailed(new VideoError(syncError.getErrCode(), syncError.getMessage()));
                                    }
                                } else {
                                    // 写入失败
                                    if (callback != null) {
                                        callback.onFailed(new VideoError(ErrorCode.SET_VALUE_FAILED, "write time failed"));
                                    }
                                }
                            }
                        }
                    });
                }else {
                    if(callback!=null){
                        callback.onSuccess(true);
                    }
                }
            }

            @Override
            public void onCancelled(SyncError syncError) {

            }
        });

    }

    public void getServerTimeStamp(String roomId, final Callback<Long> callback) {
        syncReference.child(ROOM + roomId + "/time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    long timeStamp = (long) dataSnapshot.getValue();
                    if (callback != null) {
                        callback.onSuccess(timeStamp);
                    }
                }
            }

            @Override
            public void onCancelled(SyncError syncError) {
                if (callback != null) {
                    callback.onFailed(new VideoError(syncError.getErrCode(), syncError.getMessage()));
                }
            }
        });
    }

    public void writeRoomUsers(String roomId, String userId,String userName) {
        syncReference.child(ROOM + roomId + "/users/" + userId+"/name").setValue(userName);
        syncReference.child(ROOM + roomId + "/users/" + userId).onDisconnect().removeValue();
    }

    public void removeRoomUsers(String roomId, String userId) {
        syncReference.child(ROOM + roomId + "/users/" + userId).removeValue();
    }

    public void judgeAndRemoveTime(final String roomId, final Callback<String> callback) {
        syncReference.child(ROOM + roomId + "/users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    if (dataSnapshot.getChildrenCount() == 1) {
                        removeRoomStartTimeStamp(roomId);
                        removeBoardAndChat(roomId);
                    }
                }
                if (callback != null) {
                    callback.onSuccess("");
                }
            }

            @Override
            public void onCancelled(SyncError syncError) {

            }
        });
    }

    public void removeRoomStartTimeStamp(String roomId) {
        syncReference.child(ROOM + roomId + "/time").removeValue();
    }

    public void removeBoardAndChat(String roomId) {
        syncReference.child(ROOM + roomId + "/board").removeValue();
        syncReference.child(ROOM + roomId + "/chat").removeValue();
    }
}
