package com.my51c.see51.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.my51c.see51.adapter.AlarmListAdapter;
import com.my51c.see51.adapter.AlarmListAdapter.ViewHolder;
import com.my51c.see51.data.AlarmInfo;
import com.my51c.see51.service.AppData;
import com.my51c.see51.widget.DeviceListView;
import com.my51c.see51.widget.DeviceListView.OnRefreshListener;
import com.my51c.see51.ui.AlarmSdCloudRecordActivity;
import com.sdview.view.R;

public class ActionAlarmActivity extends ListActivity implements OnClickListener,OnRefreshListener
{
	private final String TAG = "ActionAlarmActivity";
	private DeviceListView listView;
	private AppData appData;
	private AlarmListAdapter adapter;
	private ArrayList<AlarmInfo> alarmList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//Log.d(TAG, "onCreate");
		this.setContentView(R.layout.alarm_list);
		listView = (DeviceListView) findViewById(android.R.id.list);
		listView.setItemsCanFocus(true);
		listView.setonRefreshListener(this);
		
		
		LinearLayout backLayout = (LinearLayout)findViewById(R.id.alram_back_layout);
		backLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ActionAlarmActivity.this.finish();
			}
		});
		
		appData = (AppData) this.getApplication();
	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		//Log.d(TAG, "onDestroy");
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		//Log.d(TAG, "onResume");
	
		if (appData.getAccountInfo() != null)
		{
			alarmList = appData.getAccountInfo().getAlarmList();
			if (alarmList != null)
				adapter = new AlarmListAdapter(this, alarmList, this);
			listView.setAdapter(adapter);
		}
	}


	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
		//Log.d(TAG, "onStart");
		appData = (AppData) this.getApplication();
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		if (appData.getAccountInfo() != null)
		{
			alarmList = appData.getAccountInfo().getAlarmList();
			if (alarmList != null){
				adapter = new AlarmListAdapter(ActionAlarmActivity.this, alarmList, this);
				listView.setAdapter(adapter);
				try {
					adapter.notifyDataSetChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		ViewHolder view = (ViewHolder) v.getTag();
		AlarmInfo am = (AlarmInfo)(view.deviceId.getTag());
		String[] url = new String[2];
		String devid = am.getDeviceId();
		String alarmTime = am.getAlarmTime();
		String alarmUrl = am.getDataurl();
		url =alarmUrl.split(";");
		alarmUrl = url[1].substring(8);
		Intent intent = new Intent(this,AlarmSdCloudRecordActivity.class);
		intent.putExtra("alarmTime", alarmTime);
		intent.putExtra("devid", devid);
		intent.putExtra("alarmUrl", alarmUrl);
		startActivity(intent);		
	}

}
