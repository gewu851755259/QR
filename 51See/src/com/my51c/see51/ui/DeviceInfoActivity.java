package com.my51c.see51.ui;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.sdview.view.R;
import com.my51c.see51.data.Device;
import com.my51c.see51.data.Device3GShortParam;
import com.my51c.see51.data.DeviceList;
import com.my51c.see51.data.DeviceLocalInfo;
import com.my51c.see51.guide.PlatformActivity.MyLocationListenner;
import com.my51c.see51.listener.OnGet3GInfoListener;
import com.my51c.see51.listener.OnGetBLPInfoListener;
import com.my51c.see51.listener.OnGetCurtainInfoListener;
import com.my51c.see51.listener.OnGetDevInfoListener;
import com.my51c.see51.listener.OnGetRFInfoListener;
import com.my51c.see51.media.MediaStreamFactory;
import com.my51c.see51.media.RemoteInteractionStreamer;
import com.my51c.see51.protocal.GvapCommand;
import com.my51c.see51.protocal.RFPackage;
import com.my51c.see51.service.AppData;
import com.my51c.see51.service.GvapEvent;
import com.my51c.see51.service.GvapEvent.GvapEventListener;
import com.my51c.see51.service.LocalService;
import com.my51c.see51.widget.MyLoadingDialog;


/**
 * 
 * @Info 设备信息页面
 * @Date 2017-2-20 上午10:22:48
 * @Author ZXY
 */
public class DeviceInfoActivity extends SherlockFragmentActivity implements GvapEventListener, OnClickListener
{
	private static final String TAG = "DeviceInfoActivity";

	private String deviceID;
	private String version;
	private String name;
	private TextView nameTextView;
	private TextView idTextView;
	private TextView txtSoftVersion;
	private TextView txtHardwareVersion;
	private LinearLayout removeBtn;
	private ImageView img;
	private String snapFilePath;
	private LinearLayout backgroud;
	private LinearLayout sdcardBtn;
	private LinearLayout cloudrecordBtn;
	private LinearLayout rfdeviceBtn;
	private LinearLayout settingBtn;
	private LinearLayout btn_rtmp_setting;
	private String strModifyname;
	private LocalService localService;
	private String newlocationStr;
	boolean isLocal = true;
	private RemoteInteractionStreamer devInfoMediaStream;
	InputMethodManager inputMethodManager;
	private AppData appData;
	MyLoadingDialog waitdialog;
	TimeOutAsyncTask asyncTask;
	
	private static final int MSG_UNBINDED_SUCCESS = 0;
	private static final int MSG_UNBINDED_FAILED = 1;
	private static final int MSG_DEVICE_OFFLINE = 2;
	private static final int MSG_DEVICE_INFO_NOT_READY = 3;
	private static final int MSG_DEVICE_INFO_READY = 4;
	private static final int MSG_DEVICE_INFO_GET_SUCCESS = 5;
	private static final int MSG_DEVICE_INFO_GET_FAIL = 6;
	private static final int MSG_DEVICE_INFO_GET_OVERTIME = 7;
	private static final int MSG_DEVICE_NAME_MODIFY_SUCCESS = 8;
	private static final int MSG_DEVICE_NAME_MODIFY_FAILED = 9;
	private static final int MSG_DEVICE_RF_INFO_GET_FAIL=10;
	private static final int MSG_DEVICE_3G_INFO_GET_SUCCESS=11;
	private static final int MSG_DEVICE_3G_INFO_GET_FAIL=12;
	private static final int MSG_DEVICE_LOCATION_SET_SUCCESS=13;
	private static final int MSG_DEVICE_LOCATION_SET_FAIL=14;
	private Device devInfoDevice;
	
	private boolean bdevclick = false;
	private boolean brfclick = false;
	private boolean mb3gdevice = false;
	private LinearLayout backLayout,renameLayout;
	public ArrayList<String> blpList = null;
	public ArrayList<String> curtainList = null;
	private LinearLayout locationBtn;
	 //����
    private double latitude;
    //γ��
    private double longitude;
    private String locationStr;
    private LocationClient mLocClient;
    private boolean isLocationGeted = false;
    public MyLocationListenner myListener;
    private boolean isNameChange = false;
    private ImageView editImg;
	private boolean isWifiAnd4G;
    

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.devinfo_acy);
		findView();
		Bundle bundle = getIntent().getExtras();
		deviceID = bundle.getString("id");
		name = bundle.getString("name");
		isLocal = bundle.getBoolean("isLocal");
		version = bundle.getString("version");
		String[] versions = version.split("/");
		txtHardwareVersion.setText(getString(R.string.hardwareversion)+versions[0]);
		txtSoftVersion.setText(getString(R.string.softwareversion)+versions[1]);
		nameTextView.setText(name);
		idTextView.setText(deviceID);
		
		localService = ((AppData) getApplication()).getLocalService();
		appData = (AppData) getApplication();
		
//		��������ͼ
		snapFilePath = AppData.getWokringPath();
		snapFilePath += "snapshot" + File.separator + deviceID + ".jpg";
		File file = new File(snapFilePath);
		if (file.exists())
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			options.inPurgeable = true;
			options.inInputShareable = true;
			Bitmap bitmap = BitmapFactory.decodeFile(snapFilePath, options);
			img.setImageBitmap(bitmap);
		}
		
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);//���������
		waitdialog = new MyLoadingDialog(this);
		waitdialog.setCancelable(true);
		waitdialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
			}
		});
		initLocation();
	}
	

	public void findView(){
		nameTextView = (TextView) findViewById(R.id.devinfo_devName);
		renameLayout = (LinearLayout)findViewById(R.id.renameLayout);
		backgroud = (LinearLayout) findViewById(R.id.deviceInfo_panel);
		idTextView = (TextView) findViewById(R.id.serialNumberGvap);
		editImg = (ImageView)findViewById(R.id.editImg);
		removeBtn = (LinearLayout) findViewById(R.id.btn_removeDevice);
		txtSoftVersion = (TextView) findViewById(R.id.txtSoftVersion);
		txtHardwareVersion = (TextView) findViewById(R.id.txtHardwareVersion);
		sdcardBtn = (LinearLayout) findViewById(R.id.btn_sdcard_record);
		cloudrecordBtn = (LinearLayout) findViewById(R.id.btn_cloud_record);
		rfdeviceBtn = (LinearLayout) findViewById(R.id.btn_rfdevice);
		settingBtn = (LinearLayout) findViewById(R.id.btn_settings);
		backLayout = (LinearLayout)findViewById(R.id.back_layout);
		btn_rtmp_setting=(LinearLayout)findViewById(R.id.btn_rtmp_setting);
		img = (ImageView) findViewById(R.id.snapshot);
		locationBtn = (LinearLayout)findViewById(R.id.btn_update_location);
						
		backgroud.setOnClickListener(this);
		idTextView.setOnClickListener(this);
		removeBtn.setOnClickListener(this);
		sdcardBtn.setOnClickListener(this);
		cloudrecordBtn.setOnClickListener(this);
		rfdeviceBtn.setOnClickListener(this);
		settingBtn.setOnClickListener(this);
		backLayout.setOnClickListener(this);
		locationBtn.setOnClickListener(this);
		renameLayout.setOnClickListener(this);
		btn_rtmp_setting.setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		parsedevicetype(deviceID);
		
		if(isLocal){
			removeBtn.setVisibility(View.GONE);
			locationBtn.setVisibility(View.GONE); 
			editImg.setVisibility(View.GONE);
			devInfoDevice = appData.getLocalList().getDevice(deviceID);
		}else{
			removeBtn.setVisibility(View.VISIBLE);
			locationBtn.setVisibility(View.VISIBLE);
			editImg.setVisibility(View.VISIBLE);
			nameTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
			devInfoDevice = appData.getAccountInfo().getCurrentList().getDevice(deviceID);
		}
	
		devInfoMediaStream = appData.getRemoteInteractionStreamer();
		if(devInfoMediaStream == null)
		{	
			createRemoteOperaction();
		}
		else{
			if(!deviceID.equals(devInfoMediaStream.getDevId()))
			{
				createRemoteOperaction();
			}
		}
		
		if(devInfoMediaStream != null)
		{
			if(!isLocal)
			{
				devInfoMediaStream.setOnGetDevInfoListener(mOnGetDevInfoListener);
				if(mb3gdevice || isWifiAnd4G)
				{
					devInfoMediaStream.setOnGet3GInfoListener(mOnGet3GInfoListener);
				}
			}
			
			devInfoMediaStream.setOnGetRFInfoListener(mOnGetRFInfoListener);
			devInfoMediaStream.setOnGetBLPInfoListener(mOnGetBlpInfoListener);
			//devInfoMediaStream.getRFDeviceInfo();
			//devInfoMediaStream.getDevInfo();
		}
		
		bdevclick = false;
		brfclick = false;

	}
	
	private void createRemoteOperaction()//��ȡ�����������
	{	
		if(devInfoDevice == null)
		   return; 
		
		Map<String, String> paramp = new HashMap<String, String>();
		paramp.put("UserName", "admin"); 
		paramp.put("Password", "admin"); 
		paramp.put("Id", devInfoDevice.getID());	
		
		Log.i(TAG, "createRemoteOperaction:deviceID"+deviceID+"-url:"+devInfoDevice.getPlayURL());
		
		if(isLocal)
		{  
			//getPlayURL(): 192.168.1.197
			devInfoMediaStream = new RemoteInteractionStreamer(devInfoDevice.getPlayURL(), paramp);	
		}
		else
		{
			//getPlayURL(): ssp://120.25.155.92:5552;cloud://123.57.15.129:5556
			devInfoMediaStream = MediaStreamFactory.createRemoteInteractionMediaStreamer(devInfoDevice.getPlayURL(), paramp);
		}
			
		if(devInfoMediaStream != null)
		{   
			appData.setRemoteInteractionStreamer(devInfoMediaStream);
			devInfoMediaStream.open();
			devInfoMediaStream.setDevId(deviceID);
		}
		else
		{
			appData.setRemoteInteractionStreamer(null);
		}
	}
	
	@SuppressLint("NewApi") 
	public void parsedevicetype(String minID)
	{
		if(minID.isEmpty() || minID.equals(""))
		  return;
		
		String strType1 = minID.substring(0, 4);
		String strType2 = minID.substring(0, 3);
		if(strType1.equals("3321") || strType1.equals("3322") 
		   || strType1.equals("3421") || strType1.equals("3422")
		   || strType2.equals("a02") || strType2.equals("a42")
		   || strType2.equals("a82") || strType2.equals("a83")
		   || strType2.equals("a84"))
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
		return super.onCreateOptionsMenu(menu);
	}
	
	private void startwaitdialog()
	{
		asyncTask = new TimeOutAsyncTask();//sleep20s,��ʾ���ӳ�ʱ
		asyncTask.execute(0);
		waitdialog.show();
	}
	
	private void cancelwaitdialog()
	{
		if (asyncTask != null && !asyncTask.isCancelled()){
    		asyncTask.cancel(true);
    	}
		//pd.dismiss();
		waitdialog.dismiss();
	}
	
	
	private OnGetDevInfoListener mOnGetDevInfoListener = new OnGetDevInfoListener(){

		@Override
		public void onGetDevInfoFailed() {
			// TODO Auto-generated method stub
			cancelwaitdialog();
			devInfoHandler.sendEmptyMessage(MSG_DEVICE_INFO_GET_FAIL);
			bdevclick = false;
		}

		@Override
		public void onGetDevInfoSuccess(byte[] devbuf) {
			// TODO Auto-generated method stub
			
			if(devInfoDevice != null)
			{	
				ByteBuffer tempinfo = ByteBuffer.wrap(devbuf, 0, devbuf.length);
				devInfoDevice.setLocalInfo(new DeviceLocalInfo(tempinfo));
			}
			
			if(mb3gdevice == false)
			{
				if(bdevclick == true)
				{
					jumptodevinfo();
				}
				
				bdevclick = false;
				cancelwaitdialog();
			}
			else
			{
				if(devInfoDevice.get3GParam() != null)
				{
					if(bdevclick == true)
					{
						jumptodevinfo();
					}
					
					bdevclick = false;
					cancelwaitdialog();
				}
				else
				{
					devInfoMediaStream.get3GDeviceInfo();
				}
			}
		}
	};
	
	protected String byteToString(byte[] src)
	{
		int len = 0;
		for (; len < src.length; len++)
		{
			if (src[len] == 0)
			{
				break;
			}
		}
		return new String(src, 0, len);
	}
	
	private OnGetRFInfoListener mOnGetRFInfoListener = new OnGetRFInfoListener(){

		@Override
		public void onGetRFInfoFailed() {
			cancelwaitdialog();
			devInfoHandler.sendEmptyMessage(MSG_DEVICE_RF_INFO_GET_FAIL);
		}
		
		@Override
		public void onGetRFInfoSuccess(byte[] devbuf) {
			// TODO Auto-generated method stub
		
			if(devInfoDevice != null)
			{
				String parsestr;
				parsestr = byteToString(devbuf);
				Log.i(TAG, "rf��Ϣ��"+parsestr);
				devInfoDevice.setRFInfo(new RFPackage(parsestr));
			}
			
			if(brfclick == true)
			{
				jumptorfdevice();
			}
			brfclick = false;
			cancelwaitdialog();
		}
	};
	
	private OnGetBLPInfoListener mOnGetBlpInfoListener = new OnGetBLPInfoListener() {
		
		@Override
		public void onGetBLPInfoFailed() {
		
		}
		@Override
		public void onGetBLPInfoSuccess(byte[] devbuf) {
			// TODO Auto-generated method stub
			if(devInfoDevice != null)
			{
				String parsestr;
				parsestr = byteToString(devbuf);
				Log.i(TAG, "Ѫѹ����Ϣ��"+parsestr);
				parseBLPstr(parsestr);
				for(int i=0;i<blpList.size();i++){
					System.out.println(blpList.get(i));
				}
			}
		}
	};
	
	
	public void parseBLPstr(String buf){
		blpList = new ArrayList<String>();
		String tempStr = new String(buf);
		String[] lineArray = tempStr.split("#");
		for(int i=0; i< lineArray.length; i++)
		{	
			blpList.add(lineArray[i]);
		}	
	}
	
	private OnGet3GInfoListener mOnGet3GInfoListener = new OnGet3GInfoListener(){

		@Override
		public void onGet3GInfoFailed() {
			// TODO Auto-generated method stub
			cancelwaitdialog();
			devInfoHandler.sendEmptyMessage(MSG_DEVICE_3G_INFO_GET_FAIL);
		}

		@Override
		public void onGet3GInfoSuccess(byte[] devbuf) {
			// TODO Auto-generated method stub
			if(devInfoDevice != null)
			{
			   devInfoDevice.set3GParam(new Device3GShortParam(ByteBuffer.wrap(devbuf)));			
			}
			
			if(devInfoDevice.getLocalInfo() == null )
			{
				devInfoMediaStream.getDevInfo();
				return;
			}
			
			if(bdevclick == true)
			{	
				jumptodevinfo();
				bdevclick = false;
				cancelwaitdialog();
			}
			System.out.println("--------��ȡ3G��Ϣ�ɹ�-------");
		}	
	};
	
	
	public void jumptodevinfo()
	{

		Intent intent = new Intent();
		intent.putExtra("id", deviceID);
		intent.setClass(DeviceInfoActivity.this, LocalSettingActivity.class);
		if(isLocal)
		{	
			intent.putExtra("isLocal", true);
		}
		else
		{	
			intent.putExtra("isLocal", false);
		}
		
		startActivity(intent);
		overridePendingTransition(R.anim.slide_out_left , R.anim.slide_in_right);
	}
	
	public void jumptorfdevice()
	{
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
		intent.setClass(DeviceInfoActivity.this, RFDeviceInfoActivity.class);
		intent.putExtra("blpList", blpList);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// TODO Auto-generated method stub
		switch (item.getItemId())
		{

		case android.R.id.home:
			// Do whatever you want, e.g. finish()
			backMainActivity();
			break;
			
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			backMainActivity();
		}
		return false;
	}

	public void backMainActivity()
	{
		if(devInfoMediaStream!=null)
		{
			devInfoMediaStream.close();
			devInfoMediaStream = null;
		}
		appData.setRemoteInteractionStreamer(null);
		
		DeviceInfoActivity.this.finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}
	
	private void onRemoveDevice()
	{
		final Dialog delDialog = new Dialog(DeviceInfoActivity.this, R.style.Erro_Dialog);
		delDialog.setContentView(R.layout.del_dialog);
		Button sure = (Button)delDialog.findViewById(R.id.del_ok);
		sure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(isLocal)
				{
				
				}
				else
				{
					appData.getGVAPService().removeGvapEventListener(DeviceInfoActivity.this);
					appData.getGVAPService().restartRegServer(); 
					appData.getGVAPService().addGvapEventListener(DeviceInfoActivity.this);
					appData.getGVAPService().unbind(
							appData.getAccountInfo(),
							appData.getAccountInfo().getCurrentList()
									.getDevice(deviceID));
				}
				delDialog.dismiss();
			}
		});
		Button cancel = (Button)delDialog.findViewById(R.id.del_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				delDialog.dismiss();
			}
		});
		delDialog.show();
	}
	
	@Override
	public void onClick(View view)
	{
		//Log.d("DeviceInfoActivity", "onClick " + view.getId());
		// TODO Auto-generated method stub
		
		switch (view.getId())
		{
		case R.id.back_layout://退出按钮
			backMainActivity();
			break;
		case R.id.renameLayout://重新命名设备名称按钮
		{
			if(isLocal){
				return;
			}
			try {
				appData.getGVAPService().removeGvapEventListener(DeviceInfoActivity.this);
				appData.getGVAPService().restartRegServer(); 
				appData.getGVAPService().addGvapEventListener(DeviceInfoActivity.this);
				  final Dialog renameDialog =new Dialog(DeviceInfoActivity.this, R.style.Erro_Dialog);
				  renameDialog.setContentView(R.layout.rename_dialog);
				  final EditText rename = (EditText)renameDialog.findViewById(R.id.rename_Edit);
				  Button ok = (Button)renameDialog.findViewById(R.id.rename_ok);
				  Button cancel = (Button)renameDialog.findViewById(R.id.rename_cancel);
				  ok.setOnClickListener(new OnClickListener() 
				  {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						strModifyname = rename.getText().toString();
				      	if(strModifyname.equals(name))
				      	{
				      		Toast.makeText(getApplicationContext(), getString(R.string.changedevnamesuccess), 1000).show();
				      	}
				      	else
				      	{	
				      		isNameChange = true;
				      		appData.getGVAPService().removeGvapEventListener(DeviceInfoActivity.this);
							appData.getGVAPService().restartRegServer(); 
							appData.getGVAPService().addGvapEventListener(DeviceInfoActivity.this);
				      		appData.getGVAPService().changeDevName(deviceID, strModifyname, appData.getAccountInfo());
				      	}
				      	renameDialog.dismiss();
					}
					
				});
				  cancel.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						renameDialog.dismiss();
					}
				});
				  
				  renameDialog.show();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
			break;
		case R.id.btn_removeDevice://删除设备
			onRemoveDevice();
			break;
		case R.id.btn_sdcard_record://sd卡录像
		{
			if(devInfoMediaStream == null)
			{
				devInfoHandler.sendEmptyMessage(MSG_DEVICE_OFFLINE);
				return;
			}
			
			Intent intent = new Intent();
			if(deviceID.substring(0,1).equals("d")){
				intent.setClass(DeviceInfoActivity.this, SDRecordNVRActivity.class);
			}else{
				intent.putExtra("isNVR", false);
				intent.setClass(DeviceInfoActivity.this, SDRecordFolderActivity.class);
			}
			intent.putExtra("isLocal", isLocal);
			intent.putExtra("id", deviceID);
			intent.putExtra("url", devInfoDevice.getPlayURL());
			startActivity(intent);
			overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);	
		}
			break;
		case R.id.btn_cloud_record://云存储
		{	
			if(isLocal)
			  break;
			
			Intent intent = new Intent();
			intent.putExtra("id", deviceID);
			intent.setClass(DeviceInfoActivity.this, CloudRecordActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
		}
			break;
		case R.id.btn_rfdevice://rf设备
		{	
			brfclick = true;
			Intent intent = new Intent();
			intent.putExtra("id", deviceID);
			if(isLocal){	
				intent.putExtra("isLocal", true);
			}else{
				intent.putExtra("isLocal", false);
			}
			intent.putExtra("blpList", blpList);
			intent.setClass(DeviceInfoActivity.this, RFDeviceInfoActivity.class);
			
			if(devInfoMediaStream == null){
				devInfoHandler.sendEmptyMessage(MSG_DEVICE_OFFLINE);
				return;
			}
			else if(devInfoDevice.getRFInfo() == null){	
				devInfoMediaStream.getRFDeviceInfo();//request RFPackage
				devInfoHandler.sendEmptyMessage(MSG_DEVICE_INFO_NOT_READY);
				return;
			}
			if(blpList == null){
				devInfoMediaStream.getBLPDeviceInfo();//request RFPackage(BLPPackage)
			}
			if(curtainList == null){
				devInfoMediaStream.getCurtainInfo();
			}
			startActivity(intent);
			overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
		}
			break;
		case R.id.btn_settings://设置
			{	
				Intent intent = new Intent();
				intent.putExtra("id", deviceID);
				intent.setClass(DeviceInfoActivity.this, LocalSettingActivity.class);
				if(isLocal)
				{	
					intent.putExtra("isLocal", true);
				}
				else
				{	
					bdevclick = true;
					if(devInfoMediaStream == null)
					{
						devInfoHandler.sendEmptyMessage(MSG_DEVICE_OFFLINE);
						return;
					}
					else if(devInfoDevice.getLocalInfo() == null  || (mb3gdevice && devInfoDevice.get3GParam() == null) || isWifiAnd4G)
					{	
						devInfoMediaStream.getDevInfo();
						devInfoMediaStream.get3GDeviceInfo();
						devInfoHandler.sendEmptyMessage(MSG_DEVICE_INFO_NOT_READY);
						return;
					}
					else 
					{	
						intent.putExtra("isLocal", false);
					}
				}
				
				startActivity(intent);
				overridePendingTransition(R.anim.slide_out_left , R.anim.slide_in_right);
				
			}
			break;
		case R.id.btn_rtmp_setting://rtmp设置
			if(devInfoMediaStream == null) 
			{
				devInfoHandler.sendEmptyMessage(MSG_DEVICE_OFFLINE);
				return;
			}else{
			startActivity(new Intent(DeviceInfoActivity.this,RtmpSettingActivity.class));
			overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
			}
			break;
		case R.id.btn_update_location://更新位置
			showLocationDialog();
			break;
		default:
			// finishEditName();
			break;
		}
	}

	private void showLocationDialog()
	{
		final Dialog delDialog = new Dialog(DeviceInfoActivity.this, R.style.Erro_Dialog);
		delDialog.setContentView(R.layout.del_dialog);
		TextView tx = (TextView)delDialog.findViewById(R.id.erroTx);
		tx.setText(getString(R.string.location_mention));
		Button sure = (Button)delDialog.findViewById(R.id.del_ok);
		sure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				appData.getGVAPService().removeGvapEventListener(DeviceInfoActivity.this);
				appData.getGVAPService().restartRegServer(); 
				appData.getGVAPService().addGvapEventListener(DeviceInfoActivity.this);
				appData.getGVAPService().setLocation(deviceID, newlocationStr, appData.getAccountInfo());
				delDialog.dismiss();
			}
		});
		Button cancel = (Button)delDialog.findViewById(R.id.del_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				delDialog.dismiss();
			}
		});
		delDialog.show();
	}
	
	public boolean isLocalDevice(String deviceId)
	{
		DeviceList localDevList = ((AppData)this.getApplication()).getLocalList();
		Device device = localDevList.getDevice(deviceId);
		if(device != null)
		{
			localService = ((AppData)this.getApplication()).getLocalService();
			return true;
		}
		else 
			return false;
	}
	
	@Override
	public void onGvapEvent(GvapEvent event) {
		// TODO Auto-generated method stub
		switch (event) {
		
		case OPERATION_SUCCESS:
			if (event.getCommandID() == GvapCommand.CMD_UNBIND)
			{
				appData.getAccountInfo().getDevList().clear();
				appData.getGVAPService().getDeviceList();
				devInfoHandler.sendEmptyMessage(MSG_UNBINDED_SUCCESS);
			}
			
			if(event.getCommandID() == GvapCommand.CMD_UPDATE_DEVINFO)
			{
				Log.i(TAG, "CMD_UPDATE_DEVINFO");
				appData.getAccountInfo().getDevList().clear();
				appData.getGVAPService().getDeviceList();
				if(isNameChange){
					devInfoHandler.sendEmptyMessage(MSG_DEVICE_NAME_MODIFY_SUCCESS);
				}else{
					devInfoHandler.sendEmptyMessage(MSG_DEVICE_LOCATION_SET_SUCCESS);
				}
				isNameChange = false;
				
			}
			
			break;
		case OPERATION_TIMEOUT:
		case OPERATION_FAILED:
		case CONNECTION_RESET:
		case CONNECT_TIMEOUT:
		case CONNECT_FAILED:
		case NETWORK_ERROR:
			if (event.getCommandID() == GvapCommand.CMD_UNBIND)
			{
				devInfoHandler.sendEmptyMessage(MSG_UNBINDED_FAILED);
			}
			
			if(event.getCommandID() == GvapCommand.CMD_UPDATE_DEVINFO)
			{
				if(isNameChange){
					devInfoHandler.sendEmptyMessage(MSG_DEVICE_NAME_MODIFY_FAILED);
				}else{
					devInfoHandler.sendEmptyMessage(MSG_DEVICE_LOCATION_SET_FAIL);
				}
				isNameChange = false;
			}
			
			////Log.d("GVAPService", event.getCommandID().getCmdLine());
			break;

		default:
			break;
		}
	}
	
	private Handler devInfoHandler = new Handler(){
		@Override
        public void handleMessage(Message message) {
            switch (message.what) {
			case MSG_UNBINDED_SUCCESS:
				Toast.makeText(getApplicationContext(),
						getString(R.string.unbindSuccess),
						Toast.LENGTH_SHORT).show();
				finish();
				break;
			case MSG_UNBINDED_FAILED:
				Toast.makeText(getApplicationContext(),
						getString(R.string.unbindFailed),
						Toast.LENGTH_SHORT).show();
				break;
			case MSG_DEVICE_INFO_NOT_READY:
			{	
				startwaitdialog();
				//Toast.makeText(getApplicationContext(), getString(R.string.msgdeviceinfonotready), 1000).show();
			}
				break;
			case MSG_DEVICE_RF_INFO_GET_FAIL:
			{
				if(brfclick == true)
				{   
					Toast.makeText(getApplicationContext(), getString(R.string.msgdeviceinfogetfail), 1000).show();
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
					intent.setClass(DeviceInfoActivity.this, RFDeviceInfoActivity.class);
					
					startActivity(intent);
					overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
				}
				brfclick = false;
					
			}
				break;
				
			case MSG_DEVICE_INFO_GET_FAIL:
			{
				if(bdevclick == true)
				{   
					Toast.makeText(getApplicationContext(), getString(R.string.msgdeviceinfogetfail), 1000).show();
				}
				bdevclick = false;
			}
				break;
			case MSG_DEVICE_OFFLINE:
			{
				Toast.makeText(getApplicationContext(), getString(R.string.msgdeviceoffline), 1000).show();
			}
				break;
			case MSG_DEVICE_INFO_GET_OVERTIME:
			{	
				if(bdevclick == true)
				{   
					Toast.makeText(getApplicationContext(), getString(R.string.msgdeviceovertime), 1000).show();//��ȡ�豸��Ϣ��ʱ!
				}
				bdevclick = false;
				cancelwaitdialog();
				try {
					devInfoMediaStream.setbConnected(false);
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
				break;
			case MSG_DEVICE_NAME_MODIFY_SUCCESS:
			{	
				name = strModifyname;
				nameTextView.setText(name);
				Toast.makeText(getApplicationContext(), getString(R.string.changedevnamesuccess), 1000).show();
			}	
				break;
			case MSG_DEVICE_NAME_MODIFY_FAILED:
			{
				Toast.makeText(getApplicationContext(), getString(R.string.changedevnamefailed), 1000).show();
			}
				break;
			case MSG_DEVICE_LOCATION_SET_SUCCESS:
			{
				Toast.makeText(getApplicationContext(), getString(R.string.update_location_success), 1000).show();
			}
				break;
			case MSG_DEVICE_3G_INFO_GET_FAIL:
			{
				Toast.makeText(getApplicationContext(), getString(R.string.update_location_failed), 1000).show();
			}
			
				break;
			default:
				break;
			}
            
        }
	};
	
	class TimeOutAsyncTask extends AsyncTask<Integer, Integer, String>{
		
    	@Override  
        protected void onPreExecute() {  
            
            super.onPreExecute(); 
            //Log.d(TAG, " onPreExecute " );
        } 
    		        	
		@Override
		protected String doInBackground(Integer... params) {
			// TODO Auto-generated method stub

			try {
				//Log.d(TAG, " sleep " );
				Thread.sleep(20000);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//Log.d(TAG, " InterruptedException " );
				e.printStackTrace();
			}
			return null;
		}

    	@Override   
        protected void onPostExecute(String result) {  
           
            super.onPreExecute();
            devInfoHandler.sendEmptyMessage(MSG_DEVICE_INFO_GET_OVERTIME);
        }
    }
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		//devInfoMediaStream.close();
		//devInfoMediaStream = null;
		super.onStop();
		cancelwaitdialog();
	}
	
	public class MyLocationListenner implements BDLocationListener {

		

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			
			if (!isLocationGeted) {
				locationStr = latitude+","+longitude;
				newlocationStr=locationStr.toString();
				Log.i(TAG, "��ȡ��γ�ȣ�"+locationStr);
				isLocationGeted = true;
			}
		}
	}
	
	private void initLocation(){
		myListener = new MyLocationListenner();
    	mLocClient=new LocationClient(getApplicationContext());
    	mLocClient.registerLocationListener(myListener);
    	LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 打开GPS
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000); // 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true); // 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true); // 返回的定位结果包含手机机头的方向
		
		mLocClient.setLocOption(option);

        
//        LocationClientOption option = new LocationClientOption();
//		option.setOpenGps(true); // 打开GPS
//		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
//		option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度,默认值gcj02
//		option.setScanSpan(5000); // 设置发起定位请求的间隔时间为5000ms
//		option.setIsNeedAddress(true); // 返回的定位结果包含地址信息
//		option.setNeedDeviceDirect(true); // 返回的定位结果包含手机机头的方向
//		
//		locationClient.setLocOption(option);
        
        mLocClient.start();
        if(mLocClient!=null||mLocClient.isStarted()){
            mLocClient.requestLocation();
        }
    }
}
