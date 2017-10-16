package com.wilddog.conversation.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.BlackUser;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.ImageManager;
import com.wilddog.conversation.utils.MyOpenHelper;
import com.wilddog.conversation.utils.SharedpereferenceTool;
import com.wilddog.conversation.view.CircleImageView;

import java.util.ArrayList;
import java.util.List;

public class BlackUsersActivity extends AppCompatActivity {

    private ListView lv_blacklist;
    private RelativeLayout rlNoBlackUser;
    private BlackUserAdapter adapter;
    private List<BlackUser> blackUsers=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_users);

        initView();
        initData();
    }

    private void initData() {
        blackUsers = MyOpenHelper.getInstance().selectBlackUsers(SharedpereferenceTool.getUserId(this));
        updateView();
    }

    private void updateView() {
        if(blackUsers.isEmpty()){
            lv_blacklist.setVisibility(View.GONE);
            rlNoBlackUser.setVisibility(View.VISIBLE);
        }else {
            lv_blacklist.setVisibility(View.VISIBLE);
            rlNoBlackUser.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    private void initView() {
        lv_blacklist = (ListView) findViewById(R.id.lv_blacklist);
        rlNoBlackUser = (RelativeLayout) findViewById(R.id.rl_no_blackuser);
        adapter = new BlackUserAdapter(this);
        lv_blacklist.setAdapter(adapter);
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    class BlackUserAdapter extends BaseAdapter{

        private Context context;

        public BlackUserAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return blackUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return blackUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_blacklist, null);
                holder = new ViewHolder();
                holder.uid = (TextView) convertView.findViewById(R.id.tv_nickname);
                holder.civPhoto = (CircleImageView) convertView.findViewById(R.id.civ_photo);
                holder.delete= (Button) convertView.findViewById(R.id.widget_channel_delete);
                convertView.setTag(holder);
            }else {
                holder= (ViewHolder) convertView.getTag();
            }

            holder.uid.setText(blackUsers.get(position).getNickName());
            ImageManager.Load(blackUsers.get(position).getFaceurl(),holder.civPhoto);

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertMessageUtil.showShortToast("移除用户");
                    BlackUser blackUser = blackUsers.remove(position);
                    MyOpenHelper.getInstance().deleteBlackUser(blackUser);
                    updateView();
                }
            });
            return convertView;
        }

        class ViewHolder{

            public TextView uid;
            public CircleImageView civPhoto;
            public Button delete;
        }
    }
}
