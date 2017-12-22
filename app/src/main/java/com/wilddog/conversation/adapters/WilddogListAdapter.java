package com.wilddog.conversation.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.wilddog.client.ChildEventListener;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.Query;
import com.wilddog.client.SyncError;
import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.Chat;
import com.wilddog.conversation.utils.SharedPreferenceTool;

import java.util.ArrayList;
import java.util.List;

public abstract class WilddogListAdapter<T> extends BaseAdapter {

    private Query ref;
    private Class<T> modelClass;
    private LayoutInflater inflater;
    private List<T> models;
    private List<String> keys;
    private ChildEventListener listener;
    private final String username;


    public WilddogListAdapter(Query ref, Class<T> modelClass, Context context) {
        this.ref = ref;
        this.modelClass = modelClass;
        username = SharedPreferenceTool.getUserId(context);
        inflater = LayoutInflater.from(context);
        models = new ArrayList<T>();
        keys = new ArrayList<String>();
        listener = this.ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                T model = (T) dataSnapshot.getValue(WilddogListAdapter.this.modelClass);
                String key = dataSnapshot.getKey();

                if (previousChildName == null) {
                    models.add(0, model);
                    keys.add(0, key);
                } else {
                    int previousIndex = keys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == models.size()) {
                        models.add(model);
                        keys.add(key);
                    } else {
                        models.add(nextIndex, model);
                        keys.add(nextIndex, key);
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                T newModel = (T) dataSnapshot.getValue(WilddogListAdapter.this.modelClass);
                int index = keys.indexOf(key);

                models.set(index, newModel);

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                String key = dataSnapshot.getKey();
                int index = keys.indexOf(key);

                keys.remove(index);
                models.remove(index);

                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

                String key = dataSnapshot.getKey();
                T newModel = (T) dataSnapshot.getValue(WilddogListAdapter.this.modelClass);
                int index = keys.indexOf(key);
                models.remove(index);
                keys.remove(index);
                if (previousChildName == null) {
                    models.add(0, newModel);
                    keys.add(0, key);
                } else {
                    int previousIndex = keys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == models.size()) {
                        models.add(newModel);
                        keys.add(key);
                    } else {
                        models.add(nextIndex, newModel);
                        keys.add(nextIndex, key);
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(SyncError syncError) {
                Log.e("WilddogListAdapter", "Listen was cancelled, no more updates will occur");
            }

        });
    }

    public void cleanup() {
        ref.removeEventListener(listener);
        models.clear();
        keys.clear();
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public Object getItem(int i) {
        return models.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        Chat msg = (Chat) models.get(position);
        if (msg.getUid().equals(username))
            return 1;
        else
            return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            if (getItemViewType(position) == 0) {
                convertView = inflater.inflate(R.layout.chat_left, null);
                viewHolder = new ViewHolder();
                viewHolder.author = (TextView) convertView.findViewById(R.id.author);
                viewHolder.message = (TextView) convertView.findViewById(R.id.message);
            } else {
                convertView = inflater.inflate(R.layout.chat_right, null);
                viewHolder = new ViewHolder();
                viewHolder.author = (TextView) convertView.findViewById(R.id.author);
                viewHolder.message = (TextView) convertView.findViewById(R.id.message);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        T model = models.get(position);
        populateView(viewHolder, model);
        return convertView;
    }

    static class ViewHolder {
        TextView author;
        TextView message;
    }

    protected abstract void populateView(ViewHolder holder, T model);
}
