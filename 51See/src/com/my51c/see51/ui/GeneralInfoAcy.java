package com.my51c.see51.ui;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.my51c.see51.data.Device3GInfo;
import com.my51c.see51.data.Device3GShortParam;
import com.my51c.see51.data.DeviceLocalInfo;
import com.my51c.see51.guide.wifiListAdapter;
import com.my51c.see51.listener.OnSet3GInfoListener;
import com.my51c.see51.listener.OnSetDevInfoListener;
import com.my51c.see51.service.AppData;
import com.my51c.see51.service.LocalService;
import com.my51c.see51.service.LocalService.OnSetDeviceListener;
import com.sdview.view.R;

public class GeneralInfoAcy extends FragmentActivity {

	 private DeviceLocalInfo localDeviceInfo;		 
	 private LocalService localService;		 
	 EditText deviceName;
	 Spinner timeZone;
	 TextView dateEditText;
	 TextView timeEditText;		 
	 private ArrayAdapter<String> adapter;		 
	 private static final String [] utcTimeZone = {
		 "GMT-12","GMT-11","GMT-10","GMT-09","GMT-08","GMT-07","GMT-06","GMT-05",
		 "GMT-04","GMT-03","GMT-02","GMT-01","GMT+00","GMT+01","GMT+02","GMT+03",
		 "GMT+04","GMT+05","GMT+06","GMT+07","GMT+08","GMT+09","GMT+10","GMT+11","GMT+12"," GMT+13","GMT+14","GMT+0630",
		 "GMT+0530"
		 };
	 
	 public static final int MSG_SET_SUCESS = 0; 
	 public static final int MSG_SET_FAILED = 1;
	 public static final int MSG_SET_TIMEOUT = 2;
	 private int whichItem;
	 TimeOutAsyncTask asyncTask;
	 ProgressDialog pd;
	 public Dialog changePswDialog;
	 
	 private RelativeLayout timeZoneLlayout;
	 private TextView timeZoneTx;
	 private ArrayList<String> timeZoneList = null;
	 private View lastCheckedOption = null;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.generalsettingacy);
		setViewClick();
		getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//如果有改变，弹出对话框提示是否保存
		if (keyCode == KeyEvent.KEYCODE_BACK){
			if (localDeviceInfo.equals(LocalSettingActivity.mDevice.getLocalInfo())){
				finish();
			}else{
				new AlertDialog.Builder(this).setMessage(R.string.settingDevPara)
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
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	public void setViewClick(){
		
		whichItem = getIntent().getIntExtra("whichItem", 1);
		isLocal = getIntent().getBooleanExtra("isLocal", false);
		localService = ((AppData)getApplication()).getLocalService();
		localDeviceInfo =  (DeviceLocalInfo) LocalSettingActivity.mDevice.getLocalInfo().clone();// clone 设备信息
		deviceName = (EditText)findViewById(R.id.deviceNameEditText1);
    	deviceName.setText(localDeviceInfo.getDeviceName());
    	deviceName.addTextChangedListener(new TextWatcher() {
			
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
				try {
					localDeviceInfo.setDeviceName(deviceName.getText().toString());
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		});
    	
    	RelativeLayout changePassword = (RelativeLayout) findViewById(R.id.changePassword);
    	changePassword.setOnClickListener(new TextView.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				changePswDialog = new Dialog(GeneralInfoAcy.this, R.style.Erro_Dialog);
				changePswDialog.setContentView(R.layout.change_psw_dialog);
				final EditText prePswEt = (EditText)changePswDialog.findViewById(R.id.prepsw);
				final EditText newPswEt = (EditText)changePswDialog.findViewById(R.id.newpsw);
				final EditText confirNewPsw = (EditText)changePswDialog.findViewById(R.id.confir_newpsw);
				Button ok = (Button)changePswDialog.findViewById(R.id.okBtn);
				Button cancle = (Button)changePswDialog.findViewById(R.id.cancleBtn);
				
				ok.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
                    	// 判断密码是否输入正确，然后设置新密码	                    	
                    	String psw = prePswEt.getText().toString();
                    	String new_psw = newPswEt.getText().toString();
                    	String psw_again = confirNewPsw.getText().toString();
                    	
                    	if (psw.equals(localDeviceInfo.getPwd())){
                    		if(new_psw.length()>4&&new_psw.length()<16){
                    			if(new_psw.equals(psw_again)){
                    				localDeviceInfo.setNewPwd(new_psw);
                    				localDeviceInfo.setChangePWD(1);
                    				changePswDialog.dismiss();
                    				Toast.makeText(getApplicationContext(), getString(R.string.changepasswordsuccess), Toast.LENGTH_LONG ).show();
                    			}else{
                    				Toast.makeText(getApplicationContext(), getString(R.string.passwordinmatch), Toast.LENGTH_LONG ).show();
                    			}
                    		}else{
                    			Toast.makeText(getApplicationContext(), getString(R.string.passwordLenRequire), Toast.LENGTH_LONG ).show();
                    		}
                    	}else{
                    		Toast.makeText(getApplicationContext(), getString(R.string.invalidPassword), Toast.LENGTH_LONG ).show();
                    	}	
					}
				});
				
				cancle.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						changePswDialog.dismiss();
					}
				});
				
				changePswDialog.show();
			}
		});
    	
    	TextView softwareVision = (TextView)findViewById(R.id.softwareVersionTextView);
    	softwareVision.setText(localDeviceInfo.getCameraVer());
    	
    	TextView serialTextView = (TextView)findViewById(R.id.serialIDTextView);
    	serialTextView.setText(localDeviceInfo.getCamSerial());
    	
    	timeZoneList = new ArrayList<String>();
    	for(int i=0; i<utcTimeZone.length; i++){
    		timeZoneList.add(utcTimeZone[i]);
    	}
    	timeZoneLlayout = (RelativeLayout)findViewById(R.id.tableRow7);
    	timeZoneTx = (TextView)findViewById(R.id.timeZoneTx);
    	timeZoneTx.setText(timeZoneList.get(localDeviceInfo.getNtp_timezone()));
    	timeZoneLlayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showTimeZoneDialog();
			}
		});
    	
    	dateEditText = (TextView)findViewById(R.id.dateEditText);       	
    	dateEditText.setText( new String()+localDeviceInfo.getnYear()
    			+"-"+(int)(localDeviceInfo.getnMon()&0x0f)
    			+"-"+(int)localDeviceInfo.getnDay());
    	
    	timeEditText = (TextView) findViewById(R.id.timeEditText);
    	timeEditText.setText(new String()+(int)localDeviceInfo.getnHour()
    			+":"+(int)localDeviceInfo.getnMin()+":"
    			+(int)localDeviceInfo.getnSec());
    	
    	Button getPhoneTime = (Button) findViewById(R.id.getMobileTime);
    	getPhoneTime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Calendar calendar = Calendar.getInstance();
				dateEditText.setText(new String()+ calendar.get(Calendar.YEAR)
						+ "-"+(calendar.get(Calendar.MONTH)+1) 
						+ "-"+(calendar.get(Calendar.DATE)));
				timeEditText.setText(new String()+(calendar.get(Calendar.HOUR_OF_DAY))+
						":" +(calendar.get(Calendar.MINUTE))+
						":"+(calendar.get(Calendar.SECOND)));
				localDeviceInfo.setnYear(calendar.get(Calendar.YEAR));
				localDeviceInfo.setnMon((byte)(calendar.get(Calendar.MONTH)+1));
				localDeviceInfo.setnDay((byte)calendar.get(Calendar.DATE));
				localDeviceInfo.setnHour((byte) calendar.get(Calendar.HOUR_OF_DAY));
				localDeviceInfo.setnMin((byte) calendar.get(Calendar.MINUTE));
				localDeviceInfo.setnSec((byte) calendar.get(Calendar.SECOND));	
			}
		});
    	
    	final Handler rebotHandler = new Handler(){
    		@Override
	        public void handleMessage(Message msg) {
	            switch(msg.what)
	            {
	            case 0:
	            	Toast.makeText(getApplicationContext(), getString(R.string.reboot), Toast.LENGTH_LONG ).show();
	            	break;
	            case 1:
	            	Toast.makeText(getApplicationContext(), getString(R.string.setFail), Toast.LENGTH_LONG ).show(); // 
	            	break;
	            }           
	        }
		};
    	final OnSetDeviceListener listener = new OnSetDeviceListener() {
			
			@Override
			public void onSetDeviceSucess(DeviceLocalInfo devInfo) {
				// TODO Auto-generated method stub
				if (devInfo.getCamSerial().equals(localDeviceInfo.getCamSerial())) {
					rebotHandler.sendEmptyMessage(0);
				}					
			}
			
			@Override
			public void onSetDeviceFailed(DeviceLocalInfo devInfo) {
				// TODO Auto-generated method stub
				if (devInfo.getCamSerial().equals(localDeviceInfo.getCamSerial())) {
					rebotHandler.sendEmptyMessage(1);
				}					
			}
		};
    	
    	Button reboot = (Button) findViewById(R.id.btnReboot);
    	reboot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//localDeviceInfo.setCmd(0x77);
				new AlertDialog.Builder(GeneralInfoAcy.this).setTitle(R.string.sure)
				.setMessage(R.string.reboot)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						localService.setListener(listener);
						localService.rebootDevice(localDeviceInfo);							
					}
				})	                
				.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked cancel so do some stuff */
                    }
                })
                .create()
                .show();
			}
		});
    	
    	Button defaultSetting = (Button) findViewById(R.id.btnDefaultSetting);
    	if(isLocal){
    		defaultSetting.setVisibility(View.VISIBLE);
    	}
    	defaultSetting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(GeneralInfoAcy.this).setTitle(R.string.sure)
				.setMessage(R.string.defaultSetting)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						localService.setDefaultPara(localDeviceInfo);							
					}
				})
				.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked cancel so do some stuff */
                    }
                })
                .create()
                .show();
			}
		});
    	
    	TextView yesButton = (TextView) findViewById(R.id.saveTx);
    	yesButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
//				localService.setDeviceParam(localDeviceInfo);
				//点击确定按钮同步当前时间
				Calendar calendar = Calendar.getInstance();
				localDeviceInfo.setnYear(calendar.get(Calendar.YEAR));
				localDeviceInfo.setnMon((byte)(calendar.get(Calendar.MONTH)+1));
				localDeviceInfo.setnDay((byte)calendar.get(Calendar.DATE));
				localDeviceInfo.setnHour((byte) calendar.get(Calendar.HOUR_OF_DAY));
				localDeviceInfo.setnMin((byte) calendar.get(Calendar.MINUTE));
				localDeviceInfo.setnSec((byte) calendar.get(Calendar.SECOND));					
//				发送指令，设置设备信息
				asyncTask = new TimeOutAsyncTask();
				showSettingDialog(localDeviceInfo,localService);
			}
		});
    	
    	LinearLayout noButton = (LinearLayout) findViewById(R.id.backLayout);
    	noButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				finish();
				overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			}
		});        	
	}
	
	public void showTimeZoneDialog(){
		
		final Dialog dialog = new Dialog(this, R.style.Erro_Dialog);
		dialog.setContentView(R.layout.filesize_dialog);
		final ListView sizeList = (ListView)dialog.findViewById(R.id.sizeList);
		final wifiListAdapter adapter = new wifiListAdapter(timeZoneList, getApplicationContext(),R.drawable.icon_menu_time);
		adapter.isSingleChoice = true;
		adapter.choiceItem = localDeviceInfo.getNtp_timezone();
		sizeList.setAdapter(adapter);
		sizeList.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				adapter.choiceItem = position;
				adapter.notifyDataSetChanged();
				localDeviceInfo.setNtp_timezone((byte) position);
				timeZoneTx.setText(timeZoneList.get(position));
				dialog.dismiss();
			}
		});
		
		ImageView closeImg = (ImageView)dialog.findViewById(R.id.closeImg);
		closeImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		
		dialog.show();
	}
	
	
	public void showSettingDialog(final DeviceLocalInfo info,final LocalService localService){//设置设备参数
		
		//设置监听动作
		 OnSetDevInfoListener mOnSetDevInfoListener = new OnSetDevInfoListener() {
				
				@Override
				public void onSetDevInfoSuccess() {
					// TODO Auto-generated method stub
					handler.sendEmptyMessage(MSG_SET_SUCESS);
				}
				
				@Override
				public void onSetDevInfoFailed() {
					// TODO Auto-generated method stub
					handler.sendEmptyMessage(MSG_SET_FAILED);
				}
			};
			
			OnSet3GInfoListener mOnSet3GInfoListener = new OnSet3GInfoListener()
			{

				@Override
				public void onSet3GInfoFailed() {
					// TODO Auto-generated method stub
					handler.sendEmptyMessage(MSG_SET_FAILED);
				}

				@Override
				public void onSet3GInfoSuccess() {
					// TODO Auto-generated method stub
					System.out.println("-----------mOnSet3GInfoListener:onSet3GInfoSuccess");
					handler.sendEmptyMessage(MSG_SET_SUCESS);
				}
				
			};
			
			OnSetDeviceListener onSetlistener = new OnSetDeviceListener() {
				
				@Override
				public void onSetDeviceSucess(DeviceLocalInfo devInfo) {
					// TODO Auto-generated method stub
					//Log.d(TAG, " onSetDeviceSucess " );		
					if (devInfo.getCamSerial().equals(info.getCamSerial())) {
						handler.sendEmptyMessage(MSG_SET_SUCESS);
					}
					
					if(LocalSettingActivity.mb3gdevice)
					{
						localService.search3g(devInfo);
					}
					else
					{
						localService.search();
					}
				}
				
				@Override
				public void onSetDeviceFailed(DeviceLocalInfo devInfo) {
					// TODO Auto-generated method stub
					if (devInfo.getCamSerial().equals(info.getCamSerial())) {
						handler.sendEmptyMessage(MSG_SET_FAILED);
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
				{
					LocalSettingActivity.mediastream.setOnSetDevInfoListener(mOnSetDevInfoListener);
				}
			}
			
			//显示等待对话框
			pd = new ProgressDialog(GeneralInfoAcy.this);
			pd.setTitle(R.string.sure);
			pd.setMessage(GeneralInfoAcy.this.getString(LocalSettingActivity.settingParaMsg[whichItem]));
			
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
			if (msg.what == MSG_SET_SUCESS){		
            	showToast(LocalSettingActivity.setParaSuccessMsg[whichItem],getApplicationContext());
            	refreshDeviceInfo(localDeviceInfo);
	        	asyncTask.cancel(true);	
	        	pd.cancel();
	        	finish();
	        	overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }else if (msg.what == MSG_SET_FAILED) {	
            	showToast(LocalSettingActivity.setParaFailedMsg[whichItem],getApplicationContext());
	        	asyncTask.cancel(true);
	        	pd.cancel();
			}else if (msg.what == MSG_SET_TIMEOUT) {
				if (whichItem != 2) {                     //设置无线参数时不检查超时
					showToast(R.string.setFail,getApplicationContext());
				}				
	        	pd.cancel();
			} else if (msg.what == 3) {
				pd.show();
			}
		}
	};
	private boolean isLocal;
	
	
	public  class TimeOutAsyncTask extends AsyncTask<Integer, Integer, String>{
			
     	@Override  
	    protected void onPreExecute() {  
	            super.onPreExecute();  
	            if(LocalSettingActivity.isLocal)
	            {
	            	if(LocalSettingActivity.mb3gdevice)//ONLY IN Net3GSettingDialogFragment
	            	{
	            		System.out.println("TimeOutAsyncTask---mb3gdevice");

	            		if(LocalSettingActivity.is3Gor4G){//ONLY IN Net3GSettingDialogFragment
	 	            	   localService.setDevice3GParam(localDeviceInfo);
	 	            	   LocalSettingActivity.is3Gor4G = false;
	            		}else{
	            			localService.setDeviceParam(localDeviceInfo);
	            		}
	            	}
	            	else
	            	{
	            		localService.setDeviceParam(localDeviceInfo);
	            	}
	            }
	            else
	            {
	            	LocalSettingActivity.mediastream.setDevInfo(localDeviceInfo);
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
	            handler.sendEmptyMessage(MSG_SET_TIMEOUT);
	        }
	} 
	
	
	public static void showToast(int resId,Context context) {
		Toast toast = Toast.makeText(context, context.getString(resId), Toast.LENGTH_LONG );
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.show();
	}
	
	public static void refreshDeviceInfo(DeviceLocalInfo localInfo) {
		 LocalSettingActivity.mDevice.setLocalInfo(localInfo);
	}
	
	public static void refreshDevice3GLocalInfo(Device3GInfo local3GInfo)
	{
		 LocalSettingActivity.mDevice.setLocal3GInfo(local3GInfo);
	}
	
	public static void refreshDevice3GInfo(Device3GShortParam remote3GInfo)
	{
		 LocalSettingActivity.mDevice.set3GParam(remote3GInfo);
	}
}
