package com.my51c.see51.ui;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.my51c.see51.data.Device;
import com.my51c.see51.media.RemoteInteractionStreamer;
import com.my51c.see51.protocal.RFPackage;
import com.my51c.see51.service.AppData;
import com.my51c.see51.widget.BadgeView;
import com.sdview.view.R;

public class RFDeviceInfoActivity extends SherlockFragmentActivity implements OnItemClickListener{
	public static Device mDevice = null;
	public static String deviceID;
	public static boolean isLocal = false;
	private AppData appData;
	public final static String RFDEVICEINFO_ACY_ACTION = "com.rfdeviceinfo.activity";
	private ArrayList<String> blpList = new ArrayList<String>();
	private ArrayList<String> curtainList = new ArrayList<String>();
	public static int typeDeviceName[] = {R.string.rfcontrol, R.string.rfdoorsensor, R.string.rfpir, R.string.rfsmoke, 
			   R.string.rfsmartplug, R.string.rfsoundlight, R.string.rfdoorcamera, R.string.rfioalarm,R.string.blpSTR
			   ,R.string.rf_curtain,R.string.rf_lock,R.string.rf_switch1,R.string.rf_switch2,R.string.rf_switch3,
			   R.string.rf_light};
	private GridView  gridview;
	private int[] rfdevnum = new int[15];
	private int[] rfimagelog_null = {R.drawable.shap_controller, R.drawable.shap_doorsensor, R.drawable.shap_pir,
		R.drawable.shap_smoke, R.drawable.shap_smartplug, R.drawable.shap_sound,
		R.drawable.shap_doorcamera, R.drawable.shap_io,R.drawable.shap_blp,
		R.drawable.shap_curtain,R.drawable.shap_lock,R.drawable.shap_rf_switch,R.drawable.shap_rf_switch2,
		R.drawable.shap_rf_switch3,R.drawable.shap_rf_light};
	private int[] rfimagelog = {R.drawable.grid_controller_pre, R.drawable.grid_doorsensor_pre, R.drawable.grid_pirsensor_pre,
			R.drawable.grid_smokesensor_pre, R.drawable.grid_smartplug_pre, R.drawable.grid_soundlight_pre,
			R.drawable.grid_doorcamera_pre, R.drawable.grid_iosensor_pre,R.drawable.grid_blp_pre,R.drawable.grid_curtain_pre,R.drawable.grid_lock_pre
			,R.drawable.rf_switch_pre,R.drawable.rf_switch2_pre,R.drawable.rf_switch3_pre,R.drawable.rf_light_pre};
	private RFPackage rfpack;
	private ImageAdapter imgadapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rfdeviceinfo);
		gridview = (GridView)findViewById(R.id.gridviewType);
		Bundle bundle = getIntent().getExtras();
		blpList = bundle.getStringArrayList("blpList");
		curtainList = bundle.getStringArrayList("curtainList");
		deviceID = bundle.getString("id");
		isLocal = bundle.getBoolean("isLocal");
		appData = (AppData) getApplication();
		//appData.addUIActivity(new WeakReference<Activity>(this));
		if(isLocal)
		{
			mDevice = appData.getLocalList().getDevice(deviceID);
		}
		else
		{
			mDevice = appData.getAccountInfo().getCurrentList().getDevice(deviceID);
		}
		
		LinearLayout backLayout = (LinearLayout)findViewById(R.id.rf_back_layout);
		backLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				backMainActivity();
			}
		});
		
		ImageView addImg = (ImageView)findViewById(R.id.addImg);
		addImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.putExtra("id", deviceID);
				if(isLocal)
				{	
					intent.putExtra("isLocal", true);
				}
				else
				{
					intent.putExtra("isLocal", false);
				}
				
				intent.setClass(RFDeviceInfoActivity.this, RfDeviceAddActivity.class);
				startActivity(intent);
			}
		});
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if(mDevice != null)
		{
			rfpack = mDevice.getRFInfo();//get RFPackage
		}
		
		computerfnum(rfpack);
		
		imgadapter = new ImageAdapter(getApplicationContext());
	    gridview.setAdapter(imgadapter);
	    gridview.setOnItemClickListener(this);
		super.onResume();
	}

	private void computerfnum(RFPackage inpack)
	{	
		
		for(int i =0; i<15; i++ )
		{
			rfdevnum[i] = 0;
		}
		
		rfdevnum[8] = getBlpNum();;
		
		if(inpack != null)
		{
			ArrayList<Map<String, Object>> mData = inpack.getRFDevList();
			int nCount = mData.size();
			
			for(int i = 0; i < nCount; i++)
			{
				HashMap<String, Object> map =  (HashMap<String, Object>) mData.get(i);
				String strID = (String)map.get("MY51CRFID");
				String strType = strID.substring(0,2);
				if(strType.equals("01"))
				{
					rfdevnum[0] ++;
				}
				else if(strType.equals("02"))
				{
					rfdevnum[1] ++;
				}
				else if(strType.equals("03"))
				{
					rfdevnum[2] ++;
				}
				else if(strType.equals("04"))
				{
					rfdevnum[3] ++;
				}
				else if(strType.equals("10"))
				{
					rfdevnum[4] ++;
				}
				else if(strType.equals("21"))
				{
					rfdevnum[5] ++;
				}
				else if(strType.equals("22"))
				{
					rfdevnum[6] ++;
				}
				else if(strType.equals("23"))
				{
					rfdevnum[7] ++;
				}
				else if(strType.equals("a0"))
				{
					rfdevnum[9] ++;
				}
				else if(strType.equals("a1"))
				{
					rfdevnum[10] ++;
				}
				else if(strType.equals("11"))
				{
					rfdevnum[11] ++;
				}
				else if(strType.equals("12"))
				{
					rfdevnum[12] ++;
				}
				else if(strType.equals("13"))
				{
					rfdevnum[13] ++;
				}
				else if(strType.equals("a2"))
				{
					rfdevnum[14] ++;
				}
			}
		}
	}
	
	public Device getParseDevice()
	{
		return mDevice;
	}
	
	public ArrayList<String> getblpList(){
		return blpList;
	}
	
	public int getBlpNum(){
		int num;
		if(blpList == null){
			num = 0;
		}else{
			HashSet<String> set = new HashSet<String>();
			for(int i=0;i<blpList.size();i++){
				String[] infos = blpList.get(i).split(",");
				set.add(infos[0]);
			}
			num = set.size();
		}
		return num;
	}
	
	public int getCurtainNum(){
		int num;
		if(curtainList == null){
			num = 0;
		}else{
			HashSet<String> set = new HashSet<String>();
			for(int i=0;i<curtainList.size();i++){
				String[] infos = curtainList.get(i).split(",");
				set.add(infos[0]);
			}
			num = set.size();
		}
		
		return num;
	}
	
	public void backMainActivity()
	{
		RFDeviceInfoActivity.this.finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		 if(position == 8){
	            Intent intent = new Intent(getApplicationContext(), BLPDeviceInfoActivity.class);
	            intent.putStringArrayListExtra("blpList", blpList);
	            startActivity(intent);
	         }else{
	        	 Intent intent = new Intent(RFDeviceInfoActivity.this, RfDeviceListActivity.class);
	        	 intent.putExtra("itemnum", position);
	        	 startActivity(intent);
	        }
	}
	
	public class ImageAdapter extends BaseAdapter {
        private Context mContext;  
        private LayoutInflater inflater; 
 
        private class GridHolder {
            ImageView rfitemImage;  
            TextView rfitemText;  
            RelativeLayout subView;
        }
        
        public ImageAdapter(Context c){  
            mContext = c;  
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        }

        @Override  
        public int getCount() {  
            return typeDeviceName.length;  
        }
  
        @Override  
        public Object getItem(int position) {  
            return position;  
        }
  
        @Override  
        public long getItemId(int position) {  
            return position;  
        }
  
        @Override  
        public View getView(int position, View convertView, ViewGroup parent) {  
            GridHolder holder;  
            if(convertView == null) {  
                convertView = inflater.inflate(R.layout.rfdevice_nine_item, null);  
                holder = new GridHolder();  
                holder.rfitemImage = (ImageView) convertView.findViewById(R.id.rfitemImage);  
                holder.rfitemText = (TextView) convertView.findViewById(R.id.rfitemText);  
                holder.subView = (RelativeLayout) convertView.findViewById(R.id.subcontent);
                convertView.setTag(holder);  
            } else {  
                holder = (GridHolder) convertView.getTag();  
            }
            
            BadgeView badge = new BadgeView(getApplicationContext(), holder.subView);
            int num = 0;
            num += rfdevnum[position];
            badge.setText(""+num);
            badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            badge.show();
            
            if(num == 0){
            	holder.rfitemImage.setImageResource(rfimagelog_null[position]);
            }else{
            	holder.rfitemImage.setImageResource(rfimagelog[position]);
            }
            holder.rfitemText.setText(getString(typeDeviceName[position]));  
            return convertView;  
        }  
    }
	
}
