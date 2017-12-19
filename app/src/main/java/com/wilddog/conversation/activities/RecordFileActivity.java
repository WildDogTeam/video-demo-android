package com.wilddog.conversation.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wilddog.conversation.R;
import com.wilddog.conversation.bean.RecordFileData;
import com.wilddog.conversation.utils.AlertMessageUtil;
import com.wilddog.conversation.utils.ConvertUtil;
import com.wilddog.conversation.utils.ExtractVideoInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecordFileActivity extends AppCompatActivity {
    private ImageView ivCancel;
    private ListView lvRecordFile;
    private List<RecordFileData> files = new ArrayList();
    private BaseAdapter adapter;
    private File file;
    private File[] recordFiles;
    private RelativeLayout rlNoRecord;
    private RelativeLayout rlListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_file);
        ivCancel = (ImageView) findViewById(R.id.iv_cancel);
        lvRecordFile = (ListView) findViewById(R.id.lv_record_file);

        rlNoRecord = (RelativeLayout) findViewById(R.id.rl_no_file);
        rlListView = (RelativeLayout) findViewById(R.id.rl_listview);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        file = getFile();
        recordFiles = file.listFiles();
        adapter = new MyAdapter(files, this);
        lvRecordFile.setAdapter(adapter);
        initData();
    }

    private File getFile() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "wilddog");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private void initData() {
        if (recordFiles == null || recordFiles.length <= 0) {
            rlNoRecord.setVisibility(View.VISIBLE);
            rlListView.setVisibility(View.GONE);
            return;
        }
        rlListView.setVisibility(View.VISIBLE);
        rlNoRecord.setVisibility(View.GONE);
        for (File file : recordFiles) {
            if (file.getName().endsWith(".mp4")) {
                RecordFileData fileData = new RecordFileData();
                fileData.setFileName(file.getName());
                ExtractVideoInfo extractVideoInfo = new ExtractVideoInfo(fileData.getFileName(),file.getAbsolutePath());
                fileData.setDuration(convertToSeconds(extractVideoInfo.getVideoLength()));
                extractVideoInfo.release();
                files.add(fileData);
            }
        }
        adapter.notifyDataSetChanged();

    }


    private String convertToSeconds(String time) {
        long duration = Long.parseLong(time);
        return ConvertUtil.secToTime((int) duration / 1000);
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
            if (view == null) {
                view = mInflater.inflate(R.layout.item_record_file, null);
                v = new MyAdapter.ViewHolder();
                v.llRecord = (LinearLayout) view.findViewById(R.id.ll_record);
                v.fileName = (TextView) view.findViewById(R.id.widget_channel_name);
                v.duration = (TextView) view.findViewById(R.id.widget_channel_time);
                v.delete = (Button) view.findViewById(R.id.widget_channel_delete);
                view.setTag(v);
            } else {
                v = (MyAdapter.ViewHolder) view.getTag();
            }
            final String name = mList.get(i).getFileName();
            v.fileName.setText(name.substring(0, name.indexOf(".mp4")));
            v.duration.setText(mList.get(i).getDuration());
            v.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO 删除操作
                    AlertMessageUtil.showShortToast("删除文件");
                    deleteFile(mList.get(i).getFileName());
                    mList.remove(i);
                    notifyDataSetChanged();
                    if(mList.size()==0){
                        rlNoRecord.setVisibility(View.VISIBLE);
                        rlListView.setVisibility(View.GONE);
                    }else {
                        lvRecordFile.setVisibility(View.VISIBLE);
                        rlListView.setVisibility(View.GONE);
                    }

                }
            });
            v.llRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uriForFile;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uriForFile = FileProvider.getUriForFile(RecordFileActivity.this, "com.wilddog.conversation.fileprovider", recordFiles[i]);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }else {
                        String bpath = "file://" + recordFiles[i].getPath();
                        uriForFile=Uri.parse(bpath);
                    }
                    intent.setDataAndType(uriForFile, "video/*");///storage/emulated/0/Movies/wilddog/wilddog-1508740600970.mp4
                    startActivity(intent);
                }
            });
            return view;
        }

        public class ViewHolder {
            public TextView fileName;
            public TextView duration;
            public Button delete;

            public LinearLayout llRecord;
        }

        private void deleteFile(String fileName) {
            File file = new File(getFile().getAbsolutePath() + "/" + fileName);
            file.delete();
        }

    }


}
