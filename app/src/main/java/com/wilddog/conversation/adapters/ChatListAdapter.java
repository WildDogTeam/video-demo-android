package com.wilddog.conversation.adapters;

import android.content.Context;

import com.wilddog.client.Query;
import com.wilddog.conversation.bean.Chat;

public class ChatListAdapter extends WilddogListAdapter<Chat> {

    private String mUsername;

    public ChatListAdapter(Query ref, Context context, String mUsername) {
        super(ref, Chat.class, context);
        this.mUsername = mUsername;
    }

    @Override
    protected void populateView(ViewHolder holder, Chat chat) {
        holder.author.setText(chat.getAuthor());
        holder.message.setText(chat.getMessage());
    }
}
