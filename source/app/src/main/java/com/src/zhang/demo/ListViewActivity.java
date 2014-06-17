package com.src.zhang.demo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ExpandableListView;

import com.google.gson.Gson;
import com.src.zhang.demo.bean.ChannelGroup;
import com.src.zhang.demo.bean.MiscData;

/**
 * 
 * 
 * @author Administrator
 * 
 */
public class ListViewActivity extends Activity
{
	ExpandableListView expandableListView;

	ListViewAdapter treeViewAdapter;

    AppContext ac;

    private MiscData miscData;
    private ArrayList<ChannelGroup> channelGroups;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        ac = AppContext.Instance;

        miscData = new MiscData();
        channelGroups = miscData.getKeywords();

		treeViewAdapter = new ListViewAdapter(this,-26,channelGroups);
		expandableListView = (ExpandableListView) this
				.findViewById(R.id.expandableListView);
		expandableListView.setAdapter(treeViewAdapter);

        this.initData();
	}
    private void initData(){

        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String val = data.getString("data");
                //Log.i("mylog", "请求结果-->" + val);
                treeViewAdapter.UpdateTreeNode(channelGroups);
                treeViewAdapter.notifyDataSetChanged();
                //默认展开listview
                for(int i = 0; i < channelGroups.size(); i++){

                    expandableListView.expandGroup(i);

                }
            }
        };

        new Thread(){
            public void run() {
                String data0 = "";
                miscData = new MiscData();
                try {
                    data0 = ac.getChannelListData(true);
                    data0 = data0.substring(0,data0.lastIndexOf("<!--"));
                    Gson gson = new Gson();
                    miscData = gson.fromJson(data0,MiscData.class);
                    channelGroups = miscData.getKeywords();

                } catch (AppException e) {
                    e.printStackTrace();
                }
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("data",data0);
                data.putSerializable("obj",miscData);
                msg.setData(data);
                handler.sendMessage(msg);
            }
        }.start();

    }



}