package com.wilddog.conversation.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wilddog.conversation.ConversationApplication;
import com.wilddog.conversation.bean.BlackUser;
import com.wilddog.conversation.bean.ConversationRecord;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Administrator on 2016/1/28.
 */

public class MyOpenHelper extends SQLiteOpenHelper {
    private String historyTableName ="conversation_history";

    private static final String DBNAME="conversation.db";
    private static MyOpenHelper openHelper=new MyOpenHelper(ConversationApplication.getContext());
    private String blackListTableName="black_list";

    public static MyOpenHelper getInstance(){
        return openHelper;
    }

    private MyOpenHelper(Context context) {
        super(context, DBNAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists " + historyTableName + "("
                + "id integer primary key,"
                + "remoteid text,"
                + "localid text,"
                + "nickname text,"
                + "photoUrl text,"
                + "timestamp text,"
                + "duration text)");
        //  缓存视频通话信息
        db.execSQL("CREATE TABLE if not exists " + blackListTableName + "("
                + "id integer primary key,"
                + "remoteid text,"
                + "localid text,"
                + "nickname text,"
                + "photoUrl text,"
                + "timestamp text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public List selectConversationRecords(String localId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List list=new ArrayList();
        String limit = "30";
        Cursor cursor = db
                .query(historyTableName, null, "localid=?", new String[]{localId}, null, null, "timestamp desc",limit);
        if(cursor!=null){
            while (cursor.moveToNext()){
                ConversationRecord conversationRecord = new ConversationRecord();
                conversationRecord.setLocalId(cursor.getString(cursor.getColumnIndex("localid")));
                conversationRecord.setPhotoUrl(cursor.getString(cursor.getColumnIndex("photoUrl")));
                conversationRecord.setDuration(cursor.getString(cursor.getColumnIndex("duration")));
                conversationRecord.setTimeStamp(cursor.getString(cursor.getColumnIndex("timestamp")));
                conversationRecord.setRemoteId(cursor.getString(cursor.getColumnIndex("remoteid")));
                conversationRecord.setNickName(cursor.getString(cursor.getColumnIndex("nickname")));
            list.add(conversationRecord);}
        }
        cursor.close();
        return list;
    }
    public ConversationRecord selectConversationRecord(String localId,String remoteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ConversationRecord conversationRecord = null;
        Cursor cursor = db
                .query(historyTableName, null, "localid = ?and remoteid = ?", new String[]{localId,remoteId}, null, null, null);
        if(cursor!=null&&cursor.moveToNext()){
            conversationRecord = new ConversationRecord();
            conversationRecord.setLocalId(cursor.getString(cursor.getColumnIndex("localid")));
            conversationRecord.setPhotoUrl(cursor.getString(cursor.getColumnIndex("photoUrl")));
            conversationRecord.setDuration(cursor.getString(cursor.getColumnIndex("duration")));
            conversationRecord.setTimeStamp(cursor.getString(cursor.getColumnIndex("timestamp")));
            conversationRecord.setRemoteId(cursor.getString(cursor.getColumnIndex("remoteid")));
            conversationRecord.setNickName(cursor.getString(cursor.getColumnIndex("nickname")));
        }
        cursor.close();
        return conversationRecord;
    }



    //增加操作
    public long insertRecord(ConversationRecord conversationRecord)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("localid",conversationRecord.getLocalId());
        cv.put("remoteid",conversationRecord.getRemoteId());
        cv.put("nickname",conversationRecord.getNickName());
        cv.put("photoUrl",conversationRecord.getPhotoUrl());
        cv.put("timestamp",conversationRecord.getTimeStamp());
        cv.put("duration",conversationRecord.getDuration());
        long row = db.insert(historyTableName, null, cv);
        return row;
    }



    //修改操作
    public void updateRecord(String localId, String remoteId,ConversationRecord conversationRecord)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "localid=? and remoteid =?";
        String[] whereValue = { localId, remoteId};

        ContentValues cv = new ContentValues();
        cv.put("timestamp", conversationRecord.getTimeStamp());
        cv.put("nickname",conversationRecord.getNickName());
        cv.put("photoUrl",conversationRecord.getPhotoUrl());
        cv.put("duration",conversationRecord.getDuration());
        db.update(historyTableName, cv, where, whereValue);
    }

    //黑名单增加操作
    public boolean insertBlackList(BlackUser blackUser)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("localid",blackUser.getLocalId());
        cv.put("remoteid",blackUser.getRemoteId());
        cv.put("nickname",blackUser.getNickName());
        cv.put("photoUrl",blackUser.getFaceurl());
        cv.put("timestamp",blackUser.getTimeStamp());
        long row = db.insert(blackListTableName, null, cv);
        return row > -1;
    }

    public List<String> selectBlackIds(String localId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List list=new ArrayList();
        Cursor cursor = db
                .query(blackListTableName, null, "localid=?", new String[]{localId}, null, null, "timestamp desc",null);
        if(cursor!=null){
            while (cursor.moveToNext()){
                list.add(cursor.getString(cursor.getColumnIndex("remoteid")));
            }
        }
        cursor.close();
        return list;
    }
    public List<BlackUser> selectBlackUsers(String localId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List list=new ArrayList();
        Cursor cursor = db
                .query(blackListTableName, null, "localid=?", new String[]{localId}, null, null, "timestamp desc",null);
        if(cursor!=null){
            while (cursor.moveToNext()){
                BlackUser blackUser = new BlackUser();
                blackUser.setLocalId(cursor.getString(cursor.getColumnIndex("localid")));
                blackUser.setTimeStamp(cursor.getString(cursor.getColumnIndex("timestamp")));
                blackUser.setRemoteId(cursor.getString(cursor.getColumnIndex("remoteid")));
                blackUser.setNickName(cursor.getString(cursor.getColumnIndex("nickname")));
                blackUser.setFaceurl(cursor.getString(cursor.getColumnIndex("photoUrl")));
                list.add(blackUser);
            }
        }
        cursor.close();
        return list;
    }

    public boolean deleteConversationRecord(String localId,String blackUserId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int delete = db.delete(historyTableName, "localid=? and remoteid =?", new String[]{localId, blackUserId});
        return delete>0;
    }

    public boolean deleteBlackUser(BlackUser blackUser) {
        SQLiteDatabase db = this.getWritableDatabase();
        int delete = db.delete(blackListTableName, "remoteid =?", new String[]{blackUser.getRemoteId()});
        return delete>0;
    }
}
