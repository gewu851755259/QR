package com.my51c.see51.ui;

import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.sdview.view.R;
import com.my51c.see51.adapter.DeviceListAdapter;
import com.my51c.see51.adapter.DeviceListAdapter.ViewHolder;
import com.my51c.see51.data.Device;
import com.my51c.see51.data.DeviceList;
import com.my51c.see51.data.DeviceLocalInfo;
import com.my51c.see51.listener.DeviceListListener;
import com.my51c.see51.service.AppData;
import com.my51c.see51.service.LocalService;
import com.my51c.see51.widget.DeviceListView;
import com.my51c.see51.widget.DeviceListView.OnRefreshListener;

public class LocalFragment extends ListFragment implements DeviceListListener,
OnClickListener, OnRefreshListener {

	private final String TAG = "LocalFragment";
	private DeviceListView listView;
	private DeviceListAdapter adapter;
	private AppData appData;
	private DeviceList localList;
	private View progressView;
	private View waitTextView;
	private View emptyView;
	static final int MSG_UPDATE = 0;
	static final int MSG_ClEAR_PROGRESSBAR = 1;
	static final int MSG_CHECKTIME = 2;
	private boolean isChecked = false;
	private Timer timer;
	private TimerTask timerTask;
	private int listUpdateNum = 0;
	private LocalService localService;
	


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.layout_device_list, container, false);
		listView = (DeviceListView) v.findViewById(android.R.id.list);
		progressView = v.findViewById(R.id.progress_get_devices_image);
		waitTextView = v.findViewById(R.id.loading);
		emptyView = v.findViewById(R.id.emptyView);
		listView.setItemsCanFocus(true);
		listView.setonRefreshListener(this);
		return v;
	}	
	
	
	@Override
	public void onResume()
	{
		super.onResume();
		//Log.d("FavoriteActivity", "onResume");
		appData = (AppData) getActivity().getApplication();
		localService = appData.getLocalService();
		timer = new Timer();
		timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(MSG_ClEAR_PROGRESSBAR);
			}
		};

		localList = appData.getLocalList();
		if (localList.getDeviceCount() == 0)
		{
			progressView.setVisibility(View.VISIBLE);
			waitTextView.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.VISIBLE);
		}
		else {
			progressView.setVisibility(View.INVISIBLE);
			waitTextView.setVisibility(View.INVISIBLE);	
			emptyView.setVisibility(View.INVISIBLE);
		}
		adapter = new DeviceListAdapter(getActivity(), localList, this);
		localList.addListListener(this);
		setListAdapter(adapter);
		
		//checkDevTime();
	}
	
	public void checkDevTime(){
		try {
			if(!isChecked){
				if(localList.getDeviceCount()!=0){
					Calendar calendar = Calendar.getInstance();
					isChecked = true;
					for(Device device : localList){
						int year = device.getLocalInfo().getnYear();
						Log.i(TAG, "���ʱ�䣺"+device.getID()+"���:"+year);
						if(year < 2015){
							DeviceLocalInfo localDeviceInfo = device.getLocalInfo(); 
							localDeviceInfo.setnYear(calendar.get(Calendar.YEAR));
							localDeviceInfo.setnMon((byte)(calendar.get(Calendar.MONTH)+1));
							localDeviceInfo.setnDay((byte)calendar.get(Calendar.DATE));
							localDeviceInfo.setnHour((byte) calendar.get(Calendar.HOUR_OF_DAY));
							localDeviceInfo.setnMin((byte) calendar.get(Calendar.MINUTE));
							localDeviceInfo.setnSec((byte) calendar.get(Calendar.SECOND));	
							localDeviceInfo.setnMon0((byte)(calendar.get(Calendar.MONTH)+1));
							Log.i(TAG, "����ʱ�䣺"+device.getID()+"-"+Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND);
							localService.setDeviceParam(localDeviceInfo);
						}
					}
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}catch (ConcurrentModificationException e) {
			e.printStackTrace();
		}
	}
	
	@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            this.onRefresh();
	    }
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		timerTask.cancel();
		timer.cancel();
		localList.removeListener(this);
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		isChecked = false;
		localService = null;
	}

	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case MSG_UPDATE:
				adapter.updateDeviceData();
				adapter.notifyDataSetChanged();
				progressView.setVisibility(View.INVISIBLE);
				waitTextView.setVisibility(View.INVISIBLE);
				emptyView.setVisibility(View.INVISIBLE);
				mHandler.sendEmptyMessageDelayed(MSG_CHECKTIME, 3000);
				break;
			case MSG_ClEAR_PROGRESSBAR:
				progressView.setVisibility(View.INVISIBLE);
				waitTextView.setVisibility(View.INVISIBLE);
				emptyView.setVisibility(View.INVISIBLE);
			case MSG_CHECKTIME:
				checkDevTime();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		try {
			String Gro_Dev = ((ViewHolder) v.getTag()).Gro_Dev;
			if (Gro_Dev.equals("device"))
			{
				Device dev = (Device) ((ViewHolder) v.getTag()).info.getTag();
				if (dev != null && dev.getPlayURL()!= null)
				{
					Intent intent = new Intent(this.getActivity(),
							PlayerActivity.class);
					intent.putExtra("id", dev.getID());
					intent.putExtra("url", dev.getPlayURL());
					intent.putExtra("title",
							((ViewHolder) v.getTag()).title.getText());
					intent.putExtra("version", " " + " / " + dev.getLocalInfo().getCameraVer());
					intent.putExtra("name", dev.getLocalInfo().getDeviceName());
					intent.putExtra("isLocal", true);
					startActivity(intent);
				}
			}
		} catch (NullPointerException e) {
			
			e.printStackTrace();
		}
	}
	
	@Override
	public void onListUpdate() {
		
		mHandler.sendEmptyMessage(MSG_UPDATE);
		
		
		if (localList.getDeviceCount() > 0)
		{
			timer.cancel();
		} else
		{
			timerTask.cancel();
			timer.cancel();
			timer = new Timer();
			timerTask = new TimerTask()
			{

				@Override
				public void run()
				{
					
					mHandler.sendEmptyMessage(1);
				}
			};
			timer.schedule(timerTask, 10000);
		}
	}
	
	@Override
	public void onRefresh() {
		
		isChecked = false;
		listUpdateNum = 0;
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo == null)
		{
			return;
		}
		if (netinfo.isConnected())
		{
			int t = cm.getActiveNetworkInfo().getType();
			//Log.d("LocalFragment", "Network type = " + t);
			if (t == ConnectivityManager.TYPE_WIFI)
			{
				// ����wifi����ethernet�����������������豸
				appData.getLocalList().clear();
				appData.getLocalService().search();
				adapter.notifyDataSetChanged();
			}
		}	
	}

	@Override
	public void onClick(View v)
	{
		
		// ��Ӧinfo����
		String Gro_Dev = ((ViewHolder) v.getTag()).Gro_Dev;
		if (Gro_Dev.equals("device"))
		{
			Device dev = (Device) ((ViewHolder) v.getTag()).info.getTag();
			Intent intent = new Intent(this.getActivity(), DeviceInfoActivity.class);
			intent.putExtra("id", dev.getID());
			intent.putExtra("version", " " + " / " + dev.getLocalInfo().getCameraVer());
			intent.putExtra("name", dev.getLocalInfo().getDeviceName());
			intent.putExtra("isLocal", true);
			startActivity(intent);
			this.getActivity().overridePendingTransition(R.anim.slide_out_left , R.anim.slide_in_right);
		}
	}
}
