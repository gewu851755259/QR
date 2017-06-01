package com.my51c.see51.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.sdview.view.R;
import com.sdview.view.R.color;
import com.my51c.see51.data.Device;
import com.my51c.see51.data.Device3GInfo;
import com.my51c.see51.data.Device3GShortParam;
import com.my51c.see51.data.DeviceList;
import com.my51c.see51.data.DeviceLocalInfo;
import com.my51c.see51.listener.OnSet3GInfoListener;
import com.my51c.see51.listener.OnSetDevInfoListener;
import com.my51c.see51.media.RemoteInteractionStreamer;
import com.my51c.see51.service.AppData;
import com.my51c.see51.service.LocalService;
import com.my51c.see51.service.LocalService.OnSetDeviceListener;
import com.my51c.see51.widget.NewSwitch;

public class LocalSettingActivity extends SherlockFragmentActivity implements OnItemClickListener {
	
	private final int MAX_ITEMS = 6;         // 7��ѡ��
	private final static String TAG = "LocalSetting";
    private final static int[] itemImage = new int[]{
    		R.drawable.local_info_advanced_x2,
    		R.drawable.local_info_wired_x2,
    		R.drawable.local_info_wireless_x2,
    		R.drawable.local_info_video_x2,
    		R.drawable.param_nav_motion_x2,
    		R.drawable.local_info_wireless_x2
    		//R.drawable.param_nav_smtp_x2,
    		//R.drawable.param_nav_ftp_x2
    };
    
	private final static int[] itemsTitle = new int[]{
			R.string.general,
			R.string.wiredNetworkParameters,
			R.string.wirelessNetworkParameters,
			R.string.videoAudioParameters,
			R.string.setAlarm,
			R.string.net3gParameters
			//R.string.setSMTP,
			//R.string.setFTP
	};
	private final static int[] itemDescription = new int[]{
			R.string.setGeneralPara,
			R.string.setWiredParameter,
			R.string.setWirelessParameter,
			R.string.setMediaParameter,
			R.string.setAlarm,
			R.string.net3gParameters
			//R.string.setSMTP,
			//R.string.setFTP
	};
	
	public final static int[] settingParaMsg = new int[]{
			R.string.settingGeneralPara,
			R.string.settingWiredParameter,
			R.string.settingWirelessParameter,
			R.string.setttingMediaParameter,
			R.string.settingAlarm,
			R.string.net3gParameters
			//R.string.settingSMTP,
			//R.string.settingFTP
	};
	
	public final static int[] setParaSuccessMsg = new int[]{
			R.string.setGeneralParaSuccess,
			R.string.setWiredParaSuccess,
			R.string.setWirelessParaSuccess,
			R.string.setMediaParaSuccess,
			R.string.setAlarmSuccess,
			R.string.setGeneralParaSuccess
			//R.string.setSMTPSuccess,
			//R.string.setFTPSuccess
	};

	public final static int[] setParaFailedMsg = new int[]{
			R.string.setGeneralParaFailed,
			R.string.setWiredParaFailed,
			R.string.setWirelessParaFailed,
			R.string.setMediaParaFailed,
			R.string.setAlarmFailed,
			R.string.setGeneralParaFailed
			//R.string.setSMTPFailed,
			//R.string.setFTPFailed		
	};
	
	private String mID; 
	private AppData appData;
	private WifiManager mWifiManager;
	static public boolean isLocal = true;
	static public Device mDevice;                   //�ڸ�������ҳ����ͨ�����ȫ�ֶ���õ��豸��Ϣ
	static public RemoteInteractionStreamer mediastream;
	static public boolean mb3gdevice = false;
	public static ArrayList<String> ssidList;
	static public boolean is3Gor4G = false;
	public static String conRec;
	public static int screenWidth, screenHeight;
	public static boolean isWifiAnd4G = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_setting_layout);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		conRec = getString(R.string.continuous_recording);
		appData = (AppData) getApplication();
		//appData.addUIActivity(new WeakReference<Activity>(this));
		
		Bundle getData = getIntent().getExtras();
		mID = getData.getString("id");//�豸ID
		isLocal = getData.getBoolean("isLocal");
		
	   	ssidList = new ArrayList<String>();
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	
		List<ScanResult> wifiList = mWifiManager.getScanResults();
		if (wifiList != null)
		{
			for (ScanResult scanResult : wifiList)
			{
				ssidList.add(scanResult.SSID);
			}
		}
		
		mDevice = new Device();
		if(isLocal)
		{   
			mDevice = appData.getLocalList().getDevice(mID);
		}
		else
		{	
			mDevice = appData.getAccountInfo().getCurrentList().getDevice(mID);
			mediastream = appData.getRemoteInteractionStreamer();
		}
		
		LinearLayout backLayout = (LinearLayout)findViewById(R.id.back_layout);
		TextView titleName = (TextView)findViewById(R.id.titleName);
		
		backLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				LocalSettingActivity.this.finish();
				overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			}
		});
			
		parsedevicetype(mID);

		
		ListView lv = (ListView)findViewById(R.id.localSettingList);
		lv.setOnItemClickListener(this);
		SimpleAdapter adapter = new SimpleAdapter(
				this, getData(),
                R.layout.local_setting_item, new String[] { "img", "title", "info" },
                new int[] { R.id.localsettingItemImage, R.id.itemTitle, R.id.itemDescription });		
		lv.setAdapter(adapter);		
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenHeight = dm.heightPixels;
		screenWidth = dm.widthPixels;
		
	}
		
	private List<? extends Map<String, ?>> getData() {
		// TODO Auto-generated method stub
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		for(int li = 0;li < MAX_ITEMS-1; li++){
			
		     Map<String, Object> map = new HashMap<String, Object>();
		     map.put("img", itemImage[li]);
		     map.put("title", getString(itemsTitle[li]));
		     map.put("info", getString(itemDescription[li]));
		        
			if(isLocal)
			{
				if(mb3gdevice)
				{
					if(li == 2)
					{
						map.put("title", getString(itemsTitle[5]));
						map.put("info", getString(itemDescription[5]));//  3G/4G
					}
				}
				
			}
			else
			{
				if(mb3gdevice)
				{
					if(li == 1)
					{
						map.put("title", getString(itemsTitle[5]));
						map.put("info", getString(itemDescription[5]));//  3G/4G
					}
					
					if(li == 2)
					  continue;
				}
				else
				{
					if(li == 1 || li ==2)
						continue;
				}
			}
	   
	        list.add(map);	        
		}
		if(isWifiAnd4G){
			 Map<String, Object> map = new HashMap<String, Object>();
			if(mb3gdevice){//�����4Gѡ���� wifi
				map.put("img", itemImage[2]);
				map.put("title", getString(itemsTitle[2]));
				map.put("info", getString(itemDescription[2]));
			}else{			//���4G
				map.put("img", itemImage[2]);
				map.put("title", getString(itemsTitle[5]));
				map.put("info", getString(itemDescription[5]));
			}
			list.add(map);
		}
		return list;
	}
	
	@SuppressLint("NewApi") 
	public void parsedevicetype(String minID)
	{
		if(minID.isEmpty() || minID.equals(""))
		  return;
		
		String strType1 = minID.substring(0, 4);
		String strType2 = minID.substring(0, 3);
		if(strType1.equals("3321") || strType1.equals("3322") || strType1.equals("3421") || strType1.equals("3422")
		   || strType2.equals("a02") || strType2.equals("a42") || strType2.equals("a82") || strType2.equals("a83")|| strType2.equals("a84"))
		{
			mb3gdevice = true;
		}
		else
		{
			mb3gdevice = false;
		}
		boolean a = minID.substring(0, 1).equals("c")&&(Integer.parseInt(minID.substring(2, 3))>1);
		boolean b = minID.substring(0, 1).equals("a")&&(minID.substring(3, 4).equals("4"));
		if(a || b)
		{
			Log.i(TAG,"4G+wifi");
			isWifiAnd4G = true;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		junmpToActivity(position);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mDevice = null;
		isWifiAnd4G = false;
		mediastream = null;
		conRec = null;
		ssidList = null;
	}
	
	public void junmpToActivity(int which){
		switch (which) {
		case 0:
			Intent Intent0 = new Intent(this, GeneralInfoAcy.class);
			Intent0.putExtra("whichItem", which);
			Intent0.putExtra("isLocal", isLocal);
			startActivity(Intent0);
			break;
		case 1:
			
			Intent intent1;
			if(isLocal)
			{	
				//WiredSettingAcy  which
				intent1 = new Intent(this, WiredSettingAcy.class);
				intent1.putExtra("whichItem", which);
				startActivity(intent1);
			}
			else
			{	
				if(mb3gdevice)
				{
					//Net3GSettingAcy  5
					intent1 = new Intent(this, Net3GSettingAcy.class);
					intent1.putExtra("whichItem", 5);
					startActivity(intent1);
					break;
				}
				else
				{
					//AVSettingAcy   3
					intent1 = new Intent(this, AVSettingAcy.class);
					intent1.putExtra("whichItem", 3);
					startActivity(intent1);
				}
			}
			break;
		case 2:
			
			Intent intent2;
			if(isLocal)
			{	
				if(mb3gdevice)
				{
					//Net3GSettingAcy	5
					intent2 = new Intent(this, Net3GSettingAcy.class);
					intent2.putExtra("whichItem", 5);
					startActivity(intent2);
					break;
				}
				else
				{
					//WirelessSettingAcy	which
					intent2 = new Intent(this, WirelessSettingAcy.class);
					intent2.putExtra("whichItem", which);
					startActivity(intent2);
				}
				
			}
			else
			{	
				if(mb3gdevice)
				{
					//AVSettingAcy	3
					intent2 = new Intent(this, AVSettingAcy.class);
					intent2.putExtra("whichItem", 3);
					startActivity(intent2);
				}
				else
				{
					//AlarmCloudRecordAcy	4
					intent2 = new Intent(this, AlarmCloudRecordAcy.class);
					intent2.putExtra("whichItem", 4);
					startActivity(intent2);
				}
			
			}
			break;
		case 3:
			Intent intent3;
			if(isLocal)
			{
				//AVSettingAcy	which
				intent3 = new Intent(this, AVSettingAcy.class);
				intent3.putExtra("whichItem", which);
				startActivity(intent3);
			}
			else
			{
				if(mb3gdevice)
				{
					//AlarmCloudRecordAcy	4
					intent3 = new Intent(this, AlarmCloudRecordAcy.class);
					intent3.putExtra("whichItem", 4);
					startActivity(intent3);
				}else{
					intent2 = new Intent(this, Net3GSettingAcy.class);
					intent2.putExtra("whichItem", 5);
					startActivity(intent2);
				}
			}
			break;
		case 4://报警和录像设置
			//AlarmCloudRecordAcy	which
			Intent intent4 = new Intent(this, AlarmCloudRecordAcy.class);
			intent4.putExtra("whichItem", which);
			startActivity(intent4);
			break;
		case 5:
			Intent intent5;
			if(mb3gdevice)
			{
				//WirelessSettingAcy	which
				intent5 = new Intent(this, WirelessSettingAcy.class);
				intent5.putExtra("whichItem", which);
				startActivity(intent5);
			}else
			{
				//Net3GSettingAcy	5
				intent5 = new Intent(this, Net3GSettingAcy.class);
				intent5.putExtra("whichItem", 5);
				startActivity(intent5);
			}
			break;
		default:
			break;
		}
	}
	
	public void showToast(int resId) {
		Toast toast = Toast.makeText(this, getString(resId), Toast.LENGTH_LONG );
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();
	}
	
	public void refreshDeviceInfo(DeviceLocalInfo localInfo) {
		mDevice.setLocalInfo(localInfo);
	}
	
	public void refreshDevice3GInfo(Device3GShortParam remote3GInfo)
	{
		mDevice.set3GParam(remote3GInfo);
	}
}
