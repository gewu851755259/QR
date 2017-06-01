package com.my51c.see51.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.my51c.see51.data.Device3GInfo;
import com.my51c.see51.data.Device3GShortParam;
import com.my51c.see51.data.DeviceLocalInfo;
import com.my51c.see51.listener.OnSet3GInfoListener;
import com.my51c.see51.listener.OnSetDevInfoListener;
import com.my51c.see51.service.AppData;
import com.my51c.see51.service.LocalService;
import com.my51c.see51.service.LocalService.OnSetDeviceListener;
import com.sdview.view.R;

public class Net3GSettingAcy extends Activity {

	DeviceLocalInfo localDeviceInfo;
	Device3GInfo localDevice3GInfo;
	Device3GShortParam remote3GInfo;
	LocalService localService;
	EditText  username;
	EditText  passwd;
	EditText  apncode;
	EditText  phonenum;	
	EditText  alertnum1;
	EditText  alertnum2;
	EditText  alertnum3;
	EditText  alertnum4;	
	
	private int whichItem;
	ProgressDialog pd;
	private TimeOutAsyncTask asyncTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.net3gsettingacy);
		
		setViewClick();
		//隐藏软键盘
        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//如果有改变，弹出对话框提示是否保存
		if (keyCode == KeyEvent.KEYCODE_BACK){
			if(LocalSettingActivity.isLocal)
			{
				if (localDevice3GInfo.equals(new Device3GInfo(LocalSettingActivity.mDevice.getLocalInfo().toByteBuffer()))){
					finish();                                                       
				}else{
					showRemainDialog();
				}
			}
			else
			{
				if(remote3GInfo.equals(LocalSettingActivity.mDevice.get3GParam()))
				{
					finish();
				}else{
					showRemainDialog();
				}
			}
						
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	public void showRemainDialog(){
		new AlertDialog.Builder(this).setMessage(R.string.net3gParameters)
		.setPositiveButton(R.string.continue_setting, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		})
		.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                /* User clicked cancel so do some stuff */
            	finish();
            }
        })
        .create()
        .show();		
	}
	
	private void setViewClick() {
		// TODO Auto-generated method stub
		whichItem = getIntent().getIntExtra("whichItem", 1);
		localService = ((AppData)getApplication()).getLocalService();
		
		if(LocalSettingActivity.isLocal)
    	{
    		LocalSettingActivity.is3Gor4G = true;
    		localDevice3GInfo = (Device3GInfo)LocalSettingActivity.mDevice.getLocal3GInfo();
    		if(localDevice3GInfo==null){
    			System.out.println("3G参数空");
    		}else{
    			System.out.println("3G参数获取成功");
    		}
    	}
    	else
    	{
    		remote3GInfo =  (Device3GShortParam)LocalSettingActivity.mDevice.get3GParam().clone();
    	}
    	
    	
		username = (EditText)findViewById(R.id.edt3GUser);
		if(LocalSettingActivity.isLocal)
		{
			username.setText(localDevice3GInfo.getsz3GUser());
		}
		else
		{
    		{
    			username.setText(remote3GInfo.getsz3GUser());	
    		}
		}
		username.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(username.getText().toString().length()<40)
				{
					
					if(LocalSettingActivity.isLocal)
		    		{
						localDevice3GInfo.setsz3GUser(username.getText().toString());
		    		}
		    		else
		    		{
		        		{
		        			remote3GInfo.setsz3GUser(username.getText().toString());
		        		}
		    		}
					
				}
				
			}
		});
    	
		passwd = (EditText)findViewById(R.id.edt3Gpasswd);
		if(LocalSettingActivity.isLocal)
		{
			String psw = localDevice3GInfo.getsz3GPWD();
//			if(psw.equals("null")){
//				psw = "";
//			}
			passwd.setText(psw);
			System.out.println(localDevice3GInfo.getsz3GPWD());
		}
		else
		{
    		{
    			passwd.setText(remote3GInfo.getsz3GPWD());
    		}
    			
		}
		
		passwd.addTextChangedListener(new TextWatcher() {
		
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(passwd.getText().toString().length()<40)
				{
					
					if(LocalSettingActivity.isLocal)
		    		{
						System.out.println("1");
						String psw = passwd.getText().toString();
						if(psw.equals("")){
							psw = "NULL";
						}
						localDevice3GInfo.setsz3GPWD(psw);
						System.out.println(passwd.getText().toString());
		    		}
		    		else
		    		{
		        		{
		        			remote3GInfo.setsz3GPWD(passwd.getText().toString());
		        		}
		    		}
					
				}		
			}
		});
    	
		apncode = (EditText)findViewById(R.id.edt3Gapn);
		
		if(LocalSettingActivity.isLocal)
		{
			apncode.setText(localDevice3GInfo.getsz3GAPN());
		}
		else
		{
    		{
    			apncode.setText(remote3GInfo.getsz3GAPN());
    		}
    			
		}
		
		
		apncode.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(apncode.getText().toString().length()<108)
				{
					if(LocalSettingActivity.isLocal)
		    		{
						localDevice3GInfo.setsz3GAPN(apncode.getText().toString());
		    		}
		    		else
		    		{
		        		{
		        			remote3GInfo.setsz3GAPN(apncode.getText().toString());
		        		}
		    		}
					
				}
			}
		});
    	
		phonenum = (EditText)findViewById(R.id.edt3Gphone);
		if(LocalSettingActivity.isLocal)
		{
			phonenum.setText(localDevice3GInfo.getszDialNum());
		}
		else
		{
    		{
    			phonenum.setText(remote3GInfo.getszDialNum());
    		}
    			
		}
		
		phonenum.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(phonenum.getText().toString().length()<24)
				{
					if(LocalSettingActivity.isLocal)
		    		{
						localDevice3GInfo.setszDialNum(phonenum.getText().toString());
		    		}
		    		else
		    		{
		        		{
		        			remote3GInfo.setszDialNum(phonenum.getText().toString());
		        		}
		    		}
					
				}
			}
		});
		
		alertnum1 = (EditText)findViewById(R.id.edtalarmnum1);
		
		if(LocalSettingActivity.isLocal)
		{
			alertnum1.setText(localDevice3GInfo.getszAlarmNum0());
		}
		else
		{
    		{
    			alertnum1.setText(remote3GInfo.getszAlarmNum0());
    		}
    			
		}
		
	
		alertnum1.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(alertnum1.getText().toString().length()<20)
				{	
					if(LocalSettingActivity.isLocal)
		    		{
						localDevice3GInfo.setszAlarmNum0(alertnum1.getText().toString());
		    		}
		    		else
		    		{
		        		{
		        			remote3GInfo.setszAlarmNum0(alertnum1.getText().toString());
		        		}
		    		}
					
					
				}
			}
		});
		
		alertnum2 = (EditText)findViewById(R.id.edtalarmnum2);
		
		if(LocalSettingActivity.isLocal)
		{
			alertnum2.setText(localDevice3GInfo.getszAlarmNum1());
		}
		else
		{
    		{
    			alertnum2.setText(remote3GInfo.getszAlarmNum1());
    		}
    			
		}
		
		alertnum2.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(alertnum2.getText().toString().length()<20)
				{
					if(LocalSettingActivity.isLocal)
		    		{
						localDevice3GInfo.setszAlarmNum1(alertnum2.getText().toString());
		    		}
		    		else
		    		{
		        		{
		        			remote3GInfo.setszAlarmNum1(alertnum2.getText().toString());
		        		}
		    		}
					
				}
			}
		});
		
		alertnum3 = (EditText)findViewById(R.id.edtalarmnum3);
		

		if(LocalSettingActivity.isLocal)
		{
			alertnum3.setText(localDevice3GInfo.getszAlarmNum2());    		
		}
		else
		{
    		{
    			alertnum3.setText(remote3GInfo.getszAlarmNum2());
    		}
    			
		}
		
		alertnum3.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(alertnum3.getText().toString().length()<20)
				{	
					if(LocalSettingActivity.isLocal)
		    		{
						localDevice3GInfo.setszAlarmNum2(alertnum3.getText().toString());
		    		}
		    		else
		    		{
		        		{
		        			remote3GInfo.setszAlarmNum2(alertnum3.getText().toString());
		        		}
		    		}
				   
				}
			}
		});
		
		
		alertnum4 = (EditText)findViewById(R.id.edtalarmnum4);
		if(LocalSettingActivity.isLocal)
		{
			alertnum4.setText(localDevice3GInfo.getszAlarmNum3());  		
		}
		else
		{
    		{
    			alertnum4.setText(remote3GInfo.getszAlarmNum3());
    		}
    			
		}
		
		
		alertnum4.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(alertnum4.getText().toString().length()<20)
				{	

					if(LocalSettingActivity.isLocal)
		    		{
						localDevice3GInfo.setszAlarmNum3(alertnum4.getText().toString());
		    		}
		    		else
		    		{
		        		{
		        			remote3GInfo.setszAlarmNum3(alertnum4.getText().toString());
		        		}
		    		}
				   
				}
			}
		});
    	
    	TextView yesButton = (TextView)findViewById(R.id.saveTx);
    	yesButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(LocalSettingActivity.isLocal)
				{
					localDeviceInfo = new DeviceLocalInfo(localDevice3GInfo.toByteBuffer());
					asyncTask = new TimeOutAsyncTask();
					showSettingDialog(localDeviceInfo, localService,null);
					
				}
				else
				{
					{
						asyncTask = new TimeOutAsyncTask();
						showSettingDialog(null, localService,remote3GInfo);
						
					}
				}
			}
		});
    	
    	LinearLayout noButton = (LinearLayout)findViewById(R.id.backLayout);
    	noButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			}
		});
	}
	
	public void showSettingDialog(final DeviceLocalInfo info,final LocalService localService,Device3GShortParam remote3GInfo){//设置设备参数
		
		//设置监听动作
		 OnSetDevInfoListener mOnSetDevInfoListener = new OnSetDevInfoListener() {
				
				@Override
				public void onSetDevInfoSuccess() {
					// TODO Auto-generated method stub
					handler.sendEmptyMessage(GeneralInfoAcy.MSG_SET_SUCESS);
				}
				
				@Override
				public void onSetDevInfoFailed() {
					// TODO Auto-generated method stub
					handler.sendEmptyMessage(GeneralInfoAcy.MSG_SET_FAILED);
				}
			};
			
			OnSet3GInfoListener mOnSet3GInfoListener = new OnSet3GInfoListener()
			{

				@Override
				public void onSet3GInfoFailed() {
					// TODO Auto-generated method stub
					handler.sendEmptyMessage(GeneralInfoAcy.MSG_SET_FAILED);
				}

				@Override
				public void onSet3GInfoSuccess() {
					// TODO Auto-generated method stub
					System.out.println("-----------mOnSet3GInfoListener:onSet3GInfoSuccess");
					handler.sendEmptyMessage(GeneralInfoAcy.MSG_SET_SUCESS);
				}
				
			};
			
			OnSetDeviceListener onSetlistener = new OnSetDeviceListener() {
				
				@Override
				public void onSetDeviceSucess(DeviceLocalInfo devInfo) {
					// TODO Auto-generated method stub
					//Log.d(TAG, " onSetDeviceSucess " );		
					if (devInfo.getCamSerial().equals(info.getCamSerial())) {
						handler.sendEmptyMessage(GeneralInfoAcy.MSG_SET_SUCESS);
					}
					
					{
						localService.search3g(devInfo);
					}
				}
				
				@Override
				public void onSetDeviceFailed(DeviceLocalInfo devInfo) {
					// TODO Auto-generated method stub
					if (devInfo.getCamSerial().equals(info.getCamSerial())) {
						handler.sendEmptyMessage(GeneralInfoAcy.MSG_SET_FAILED);
					}			        	
				}
			};
			
			//绑定监听
			if(LocalSettingActivity.isLocal)
        	{
				localService.setListener(onSetlistener);
        	}
			else
			{	
				
				LocalSettingActivity.mediastream.setOnSet3GInfoListener(mOnSet3GInfoListener);
			}
			
			//显示等待对话框
			pd = new ProgressDialog(this);
			pd.setTitle(R.string.sure);
			pd.setMessage(this.getString(LocalSettingActivity.settingParaMsg[whichItem]));
			
			pd.setCancelable(true);
			pd.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					if(LocalSettingActivity.isLocal)
					{	
						localService.setListener(null);
					}
					else
					{
						//mediastream.setOnSetDevInfoListener(null);
					}
				}
			});				
			pd.show();

			asyncTask.execute(0);
			
	}
	
	public  Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == GeneralInfoAcy.MSG_SET_SUCESS){		
				GeneralInfoAcy.showToast(LocalSettingActivity.setParaSuccessMsg[whichItem],getApplicationContext());
				if(LocalSettingActivity.isLocal)
	        	{
					localDevice3GInfo =  new Device3GInfo(localDeviceInfo.toByteBuffer());
	            	GeneralInfoAcy.refreshDevice3GLocalInfo(localDevice3GInfo);
	        	}
				else
				{
					GeneralInfoAcy.refreshDevice3GInfo(remote3GInfo);
				}
            	
	        	asyncTask.cancel(true);	
	        	pd.cancel();
	        	finish();
	        	overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }else if (msg.what == GeneralInfoAcy.MSG_SET_FAILED) {	
            	GeneralInfoAcy.showToast(LocalSettingActivity.setParaFailedMsg[whichItem],getApplicationContext());
	        	asyncTask.cancel(true);
	        	pd.cancel();
			}else if (msg.what == GeneralInfoAcy.MSG_SET_TIMEOUT) {
				if (whichItem != 2) {                     //设置无线参数时不检查超时
					GeneralInfoAcy.showToast(R.string.setFail,getApplicationContext());
				}				
	        	pd.cancel();
			} else if (msg.what == 3) {
				pd.show();
			}
		}
	};
	
	
	public  class TimeOutAsyncTask extends AsyncTask<Integer, Integer, String>{
			
     	/*
     	* 执行流程：
     	* 1.onPreExecute()
     	* 2.doInBackground()-->onProgressUpdate()
     	* 3.onPostExecute()
     	*/
		 
     	@Override  
	    protected void onPreExecute() {  
	            //第一个执行方法  
	            super.onPreExecute();  
	            if(LocalSettingActivity.isLocal)
	            {
	            	{
	            		System.out.println("TimeOutAsyncTask---mb3gdevice");

	            		 localService.setDevice3GParam(localDeviceInfo);
	            	}
	            }
	            else
	            {
	            	{
	            		LocalSettingActivity.mediastream.send3GDevInfo(remote3GInfo);
	            	}
	            	
	            }
	        } 
     		        	
			@Override
			protected String doInBackground(Integer... params) {
				try {
					if(LocalSettingActivity.isLocal)
					{
						Thread.sleep(5000);
					}
					else
					{
						Thread.sleep(10000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}

     	@Override   
         protected void onPostExecute(String result) {  
	            //第一个执行方法  
	            super.onPreExecute();
	            handler.sendEmptyMessage(GeneralInfoAcy.MSG_SET_TIMEOUT);
	        }
	} 
	
	
}
