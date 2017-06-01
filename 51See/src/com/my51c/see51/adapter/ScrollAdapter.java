package com.my51c.see51.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.my51c.see51.ui.PlayerActivity;
import com.sdview.view.R;

public class ScrollAdapter extends BaseAdapter {
	
	private static ArrayList<String> rfStrTypeList = new ArrayList<String>();
	private ArrayList<Integer> rfImgList_off = new ArrayList<Integer>();
	private ArrayList<Integer> rfImgList_on = new ArrayList<Integer>();
	private String[] rfStr = {"01","02","03","04","10","21","22","23","a0","a1","11","12","13","a2"};
	private int[] rfImg_off= {R.drawable.shap_controller, R.drawable.shap_doorsensor, R.drawable.shap_pir,
			R.drawable.shap_smoke, R.drawable.shap_smartplug, R.drawable.shap_sound,
			R.drawable.shap_doorcamera, R.drawable.shap_io,R.drawable.grid_curtain_pre,R.drawable.shap_lock,R.drawable.rf_switch_pre,R.drawable.rf_switch2_pre,R.drawable.rf_switch3_pre,R.drawable.shap_rf_light};
	private int[] rfImg_on = {R.drawable.grid_controller_pre, R.drawable.grid_doorsensor_pre, R.drawable.grid_pirsensor_pre,
			R.drawable.grid_smokesensor_pre, R.drawable.grid_smartplug_pre, R.drawable.grid_soundlight_pre,
			R.drawable.grid_doorcamera_pre, R.drawable.grid_iosensor_pre,R.drawable.grid_curtain_pre,R.drawable.grid_lock_pre
			,R.drawable.rf_switch_pre,R.drawable.rf_switch2_pre,R.drawable.rf_switch3_pre,R.drawable.rf_light_pre};
	
	private List<Map<String, Object>> mList;
	private Context context;
	public static final int APP_PAGE_SIZE = 8;

	public ScrollAdapter(Context context,List<Map<String, Object>> list,int page)
	{
		this.context = context;
		int i = page * APP_PAGE_SIZE;
		mList = new ArrayList<Map<String,Object>>();
		int iEnd = i+APP_PAGE_SIZE;
		while ((i<list.size()) && (i<iEnd)) {
			mList.add(list.get(i));
			i++;
		}
		transRfData();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, null);
		convertView.setLayoutParams(new LayoutParams((PlayerActivity.w-20)/4, LayoutParams.WRAP_CONTENT));
		ImageView rfImg = (ImageView)convertView.findViewById(R.id.rfitemImage);
		TextView rfTx = (TextView) convertView.findViewById(R.id.rfitemText);
		
		HashMap<String, Object> map = (HashMap<String, Object>)mList.get(position);
		String strID = (String)map.get("MY51CRFID");
		String status = (String)map.get("status");
		String strType = strID.substring(0,2);
		rfTx.setText((String)map.get("name"));
		if(status.equals("on")){
			rfImg.setBackgroundResource(rfImgList_on.get(rfStrTypeList.indexOf(strType)));
		}else{
			rfImg.setBackgroundResource(rfImgList_off.get(rfStrTypeList.indexOf(strType)));
		}
		return convertView;
	}

	public void transRfData(){
		for(int i=0;i<rfStr.length;i++)
		{
			rfStrTypeList.add(rfStr[i]);
		}
		for(int i=0;i<rfImg_off.length;i++)
		{
			rfImgList_off.add(rfImg_off[i]);
		}
		for(int i=0;i<rfImg_on.length;i++)
		{
			rfImgList_on.add(rfImg_on[i]);
		}
	}
	
	public ArrayList<String> getRfStrTypeList(){
		return rfStrTypeList;
	}
}
