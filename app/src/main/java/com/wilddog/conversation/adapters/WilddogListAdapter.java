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
import com.wilddog.conversation.utils.SharedPereferenceTool;

import java.util.ArrayList;
import java.util.List;

public abstract class WilddogListAdapter<T> extends BaseAdapter {

    private Query mRef;
    private Class<T> mModelClass;
    private LayoutInflater mInflater;
    private List<T> mModels;
    private List<String> mKeys;
    private ChildEventListener mListener;
    private final String mUsername;


    public WilddogListAdapter(Query mRef, Class<T> mModelClass, Context context) {
        this.mRef = mRef;
        this.mModelClass = mModelClass;
        mUsername = SharedPereferenceTool.getUserId(context);
        mInflater = LayoutInflater.from(context);
        mModels = new ArrayList<T>();
        mKeys = new ArrayList<String>();
        mListener = this.mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                T model = (T) dataSnapshot.getValue(WilddogListAdapter.this.mModelClass);
                String key = dataSnapshot.getKey();

                if (previousChildName == null) {
                    mModels.add(0, model);
                    mKeys.add(0, key);
                } else {
                    int previousIndex = mKeys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(model);
                        mKeys.add(key);
                    } else {
                        mModels.add(nextIndex, model);
                        mKeys.add(nextIndex, key);
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                T newModel = (T) dataSnapshot.getValue(WilddogListAdapter.this.mModelClass);
                int index = mKeys.indexOf(key);

                mModels.set(index, newModel);

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                String key = dataSnapshot.getKey();
                int index = mKeys.indexOf(key);

                mKeys.remove(index);
                mModels.remove(index);

                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

                String key = dataSnapshot.getKey();
                T newModel = (T) dataSnapshot.getValue(WilddogListAdapter.this.mModelClass);
                int index = mKeys.indexOf(key);
                mModels.remove(index);
                mKeys.remove(index);
                if (previousChildName == null) {
                    mModels.add(0, newModel);
                    mKeys.add(0, key);
                } else {
                    int previousIndex = mKeys.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(newModel);
                        mKeys.add(key);
                    } else {
                        mModels.add(nextIndex, newModel);
                        mKeys.add(nextIndex, key);
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
        mRef.removeEventListener(mListener);
        mModels.clear();
        mKeys.clear();
    }

    @Override
    public int getCount() {
        return mModels.size();
    }

    @Override
    public Object getItem(int i) {
        return mModels.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        Chat msg = (Chat) mModels.get(position);
        if(msg.getUid().equals(mUsername))
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
        if(convertView == null){
            if(getItemViewType(position) == 0){
                convertView = mInflater.inflate(R.layout.chat_left,null);
                viewHolder = new ViewHolder();
                viewHolder.author = (TextView)convertView.findViewById(R.id.author);
                viewHolder.message = (TextView)convertView.findViewById(R.id.message);
            }else {
                convertView = mInflater.inflate(R.layout.chat_right,null);
                viewHolder = new ViewHolder();
                viewHolder.author = (TextView)convertView.findViewById(R.id.author);
                viewHolder.message = (TextView)convertView.findViewById(R.id.message);
            }
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        T model = mModels.get(position);
        populateView(viewHolder, model);
        return convertView;
    }
    static class ViewHolder{
        TextView author;
        TextView message;
    }
    protected abstract void populateView(ViewHolder holder, T model);
}
