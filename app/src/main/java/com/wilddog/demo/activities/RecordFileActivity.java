package com.wilddog.demo.activities;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wilddog.demo.R;
import com.wilddog.demo.bean.RecordFileData;
import com.wilddog.demo.utils.AlertMessageUtil;
import com.wilddog.demo.utils.ConvertUtil;
import com.wilddog.demo.utils.ExtractVideoInfoUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecordFileActivity extends AppCompatActivity {
     private ImageView ivCancel;
    private ListView lvRecordFile;
    private List<RecordFileData> files= new ArrayList();
     private BaseAdapter adapter;
    private File file;
    private File[] recordFiles;
    private MediaMetadataRetriever  metadataRetriever = new MediaMetadataRetriever();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_file);
        ivCancel = (ImageView) findViewById(R.id.iv_cancel);
        lvRecordFile = (ListView) findViewById(R.id.lv_record_file);
         ivCancel.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 finish();
             }
         });
        file= getFile();
        recordFiles = file.listFiles();
        initData();
         adapter= new MyAdapter(files,this);
        lvRecordFile.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private File getFile(){
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "wilddog");
        if (!file.exists()) {
            boolean a = file.mkdirs();
        }
        return file;

    }

    private void initData(){
        if(recordFiles.length<=0){return;}
       for(File file:recordFiles){
           if(file.getName().endsWith(".mp4")){
               RecordFileData fileData = new RecordFileData();
               fileData.setFileName(file.getName());
               ExtractVideoInfoUtil extractVideoInfoUtil = new ExtractVideoInfoUtil(file.getAbsolutePath());
               fileData.setDuration(convertToSeconds(extractVideoInfoUtil.getVideoLength()));
               extractVideoInfoUtil.release();
               files.add(fileData);
           }
       }

    }


    private String convertToSeconds(String time){
        long duration = Long.parseLong(time);
       return ConvertUtil.secToTime((int)duration/1000);
    }

    class MyAdapter extends BaseAdapter {
        private List<RecordFileData> mList = new ArrayList<>();
        private LayoutInflater mInflater;

        MyAdapter(List<RecordFileData> userList, Context context) {
            mList = userList;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            MyAdapter.ViewHolder v;
            if(view==null) {
                view = mInflater.inflate(R.layout.list_record_file_item, null);
                v=new MyAdapter.ViewHolder();
                v.fileName = (TextView) view.findViewById(R.id.widget_channel_name);
                v.duration = (TextView) view.findViewById(R.id.widget_channel_time);
                v.delete = (Button) view.findViewById(R.id.widget_channel_delete);
                view.setTag(v);
            }else {
                v= (MyAdapter.ViewHolder) view.getTag();
            }
            String name = mList.get(i).getFileName();
            v.fileName.setText(name.substring(0,name.indexOf(".mp4")));
            v.duration.setText(mList.get(i).getDuration());
            v.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO 删除操作
                    AlertMessageUtil.showShortToast("删除文件");
                    deleteFile(mList.get(i).getFileName());
                    mList.remove(i);
                    notifyDataSetChanged();

                }
            });
            return view;
        }

        public class ViewHolder{
            public TextView fileName;
            public TextView duration;
            public Button delete;

        }

        private void deleteFile(String fileName){
            File file = new File(getFile().getAbsolutePath()+"/"+fileName);
            file.delete();
        }

    }



}
