package com.my51c.see51.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sdview.view.R;
import com.my51c.see51.adapter.AlarmListAdapter.ViewHolder;
import com.my51c.see51.data.AlarmInfo;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WifiListAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater;
	private List<ScanResult> mWifiList;
	private OnClickListener mListener;
	public OnClickListener getmListener() {
		return mListener;
	}

	public void setmListener(OnClickListener mListener) {
		this.mListener = mListener;
	}

	private Context mContext;
	private static final int MIN_RSSI = -100;   
	private static final int MAX_RSSI = -55;  


	public static int calculateSignalLevel(int rssi, int numLevels) {
        if (rssi <= MIN_RSSI) {
            return 0;
        } else if (rssi >= MAX_RSSI) {
            return numLevels - 1;
        } else {
            int partitionSize = (MAX_RSSI - MIN_RSSI) / (numLevels - 1);
            return (rssi - MIN_RSSI) / partitionSize;
        }
    }
	
	public WifiListAdapter(Context context, List<ScanResult> wifiList, OnClickListener l)
	{
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mWifiList = wifiList;
		mListener = l;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mWifiList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mWifiList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public final class ViewHolder
	{
		public ImageView wifiimg;
		public TextView essid;	
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.wifi_list_adapter_item, null);
			holder.essid = (TextView) convertView.findViewById(R.id.tvessid);
			holder.wifiimg = (ImageView) convertView.findViewById(R.id.wifistrong);
			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.essid.setText(mWifiList.get(position).SSID);
		String capabilities = mWifiList.get(position).capabilities;
		int level = calculateSignalLevel(mWifiList.get(position).level, 4);
		if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
			switch (level) {
			case 0:
				holder.wifiimg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_lock_signal_1_light));
				break;
			case 1:
				holder.wifiimg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_lock_signal_2_light));
				break;
			case 2:
				holder.wifiimg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_lock_signal_3_light));
				break;
			case 3:
				holder.wifiimg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_lock_signal_4_light));
				break;

			default:
				break;
			}
		}
		else
		{ 
			switch (level) {
			case 0:
				holder.wifiimg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_signal_1_light));
				break;
			case 1:
				holder.wifiimg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_signal_2_light));
				break;
			case 2:
				holder.wifiimg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_signal_3_light));
				break;
			case 3:
				holder.wifiimg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_signal_4_light));
				break;

			default:
				break;
			}
		}
		
		holder.essid.setTag(mWifiList.get(position));

		return convertView;
	}
	

}
