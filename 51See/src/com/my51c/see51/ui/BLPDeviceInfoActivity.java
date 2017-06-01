package com.my51c.see51.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sdview.view.R;
import com.my51c.see51.widget.DateWidgetDayCell.OnItemClick;


public class BLPDeviceInfoActivity extends Activity {
	
	private ArrayList<String> blpList = null;
	private ListView blpDevList;
	ArrayList<String> devNames = new ArrayList<String>();

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		70000004,20150829101802,01000000,5a3a5701,
//		设备ID，    测试日期，  	测试人员id,	高压低压心率正常
		blpList = getIntent().getStringArrayListExtra("blpList");
		setContentView(R.layout.blpdeviceinfo_activity);
		setView();
		ImageView addImg = (ImageView)findViewById(R.id.addImg);
		addImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.putExtra("id", RFDeviceInfoActivity.deviceID);
				if(RFDeviceInfoActivity.isLocal)
				{	
					intent.putExtra("isLocal", true);
				}
				else
				{
					intent.putExtra("isLocal", false);
				}
				intent.setClass(getApplicationContext(), RfDeviceAddActivity.class);
				startActivity(intent);
			}
		});
		LinearLayout backLayout = (LinearLayout)findViewById(R.id.rf_back_layout);
		backLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				backMainActivity();
			}
		});
	}

	public void setView(){
		blpDevList = (ListView)findViewById(R.id.blpDevList);
		if(blpList == null){
			
		}else{
			getDevNames();
			blpDevList.setAdapter(new BLPAdapter(devNames));
			blpDevList.setOnItemClickListener(new MyItemClickListener());
		}
	}
	
	private void getDevNames(){
		HashSet<String> devNameSet = new HashSet<String>();
		for(int i=0;i<blpList.size();i++){
			String[] infos = blpList.get(i).split(",");
			devNameSet.add(infos[0]);
		}
		Iterator<String> it = devNameSet.iterator();
		while(it.hasNext()){
			devNames.add(it.next());
		}
	}
	
	private class MyItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			// TODO Auto-generated method stub
			String devName = devNames.get(position);
			System.out.println(devNames.get(position));
			ArrayList<String> infoList = new ArrayList<String>();
			for(int i = 0;i<blpList.size();i++)
			{
				if (devName.equals(blpList.get(i).split(",")[0]))
				{
					infoList.add(blpList.get(i));
					System.out.println(blpList.get(i));
				}
			}
			Intent intent = new Intent(getApplicationContext(), BLPDetailActivity.class);
			intent.putExtra("infoList", infoList);
			startActivity(intent);
		}
	}
	
	private class BLPAdapter extends BaseAdapter{
		
		private ArrayList<String> devNames;
		
		public BLPAdapter(ArrayList<String> devNames){
			this.devNames = devNames;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return devNames.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.blpdevlist_item, null);
			TextView blpDevName = (TextView)convertView.findViewById(R.id.blpDevName);
			blpDevName.setText(devNames.get(position));
			return convertView;
		}
	}
	
	public void backMainActivity()
	{
		finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}
}
