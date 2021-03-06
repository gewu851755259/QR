package com.my51c.see51.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.my51c.see51.data.AlarmInfo;
import com.sdview.view.R;

@SuppressLint("ResourceAsColor")
public class AlarmListAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	private List<Map<String, Object>> mData;
	private ArrayList<AlarmInfo> alarmList;
	private OnClickListener mListener;
	private Context context;

	private final String TAG = "AlarmListAdapter";

	public AlarmListAdapter(Context context, ArrayList<AlarmInfo> alarmList, OnClickListener l)
	{
		this.mInflater = LayoutInflater.from(context);
		this.alarmList = alarmList;
		this.mListener = l;
		this.context = context;
		updateDeviceData();
		// TODO Test code here
		//Log.d(TAG, "constructor");
	}

	protected void updateDeviceData()
	{
		//Log.d(TAG, "getAlarmInormation");
		if (alarmList == null)
		{
			//Log.d("alarmListAdapter", "deviceList == null");
			return;
		}
		if (mData == null)
		{
			mData = new ArrayList<Map<String, Object>>();
		}
		mData.clear();
		for (int i = 0; i < alarmList.size(); i++)
		{
			AlarmInfo alarmInfo = alarmList.get(i);
			if (alarmInfo != null)
			{
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("devname", alarmInfo.getDevName());
				map.put("title", alarmInfo.getTitle());
				map.put("message", alarmInfo.getMessage());
				map.put("deviceId", alarmInfo.getDeviceId());
				/*Time currentTime = new Time();
				currentTime.setToNow();
				String alarmTime = alarmInfo.getAlarmTime();
				String alarmTimeShow;
				if(currentTime.toMillis(false) - Long.parseLong(alarmTime)<= 86400000)
				{
					String hour = String.valueOf(alarmTime.hour);
					String minute = String.valueOf(alarmTime.minute);
					if(alarmTime.hour < 10)
						hour = "0"+hour;
					if(alarmTime.minute < 10)
						minute = "0"+minute; 
					alarmTimeShow = hour+":"+minute;
				}
				else if(currentTime.toMillis(false) - alarmTime.toMillis(false) <= 172800000)
					alarmTimeShow = context.getString(R.string.yesterday);
				else
					alarmTimeShow = alarmTime.year+"-"+alarmTime.month+"-"+alarmTime.monthDay;*/
				SimpleDateFormat format = new SimpleDateFormat("HH:mm");
				String alarmTime = format.format(new Date(Long.valueOf(alarmInfo.getAlarmTime())*1000));
				map.put("alarmData",alarmTime);
				map.put("img", R.drawable.attention);
				map.put("AlarmInfo", alarmInfo);
				mData.add(map);
			}
		}
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public final class ViewHolder
	{
		public TextView deviceId;
		public TextView alarmInfo;
		public TextView timeTx;
		public TextView alarmType;
		
	}

	@SuppressLint("ResourceAsColor")
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.alarm_list_item, null);
			holder.deviceId = (TextView) convertView.findViewById(R.id.devId);
			holder.alarmInfo = (TextView) convertView.findViewById(R.id.alarmInfo);
			holder.timeTx =  (TextView) convertView.findViewById(R.id.timeTx);
			holder.alarmType = (TextView) convertView.findViewById(R.id.alarmType);
			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.deviceId.setText((String) mData.get(position).get("devname") +"("+(String) mData.get(position).get("deviceId")+")");
		holder.alarmInfo.setText((String) mData.get(position).get("title"));
		holder.timeTx.setText((String) mData.get(position).get("alarmData"));
		holder.alarmType.setText((String) mData.get(position).get("message"));
		holder.deviceId.setTag(mData.get(position).get("AlarmInfo"));
		return convertView;

	}
//	holder.img.setBackgroundResource((Integer) mData.get(position).get("img"));
//	holder.deviceId.setText((String) mData.get(position).get("devname") +"("+(String) mData.get(position).get("deviceId")+")");
//	holder.title.setText((String) mData.get(position).get("title"));
//	holder.message.setText((String) mData.get(position).get("message"));
//	holder.alarmData.setText((String) mData.get(position).get("alarmData"));

}
