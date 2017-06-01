package com.my51c.see51.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.my51c.see51.data.DeviceLocalInfo;
import com.my51c.see51.guide.wifiListAdapter;
import com.my51c.see51.listener.OnSet3GInfoListener;
import com.my51c.see51.listener.OnSetDevInfoListener;
import com.my51c.see51.service.AppData;
import com.my51c.see51.service.LocalService;
import com.my51c.see51.service.LocalService.OnSetDeviceListener;
import com.my51c.see51.widget.NewSwitch;
import com.sdview.view.R;
import com.sdview.view.R.color;

public class WirelessSettingAcy extends Activity {

	DeviceLocalInfo localDeviceInfo;
	LocalService localService;
	
	NewSwitch wifiClient ;
	EditText ssid;
	EditText password;
	RelativeLayout wapLayout;
	TextView wapTx;
	
	NewSwitch dhcp;
	EditText ipAddr;
	EditText mask;
	EditText gateWay;
	EditText dns;		 
	ImageButton btnSelectWifi; 
	public ArrayAdapter<String> adapterWifi;
	
	private static final String [] encrypType ={
		"WEP","WPA-PSK","WPA2-PSK","NONE"
	};
	
	private ArrayList<String> encryTypeList = null;
	
	private int whichItem;
	ProgressDialog pd;
	private TimeOutAsyncTask asyncTask;
	private LinearLayout wifiDetailLayout;
	private LinearLayout detailLayout;
	private View lastCheckedOption = null;
	private RelativeLayout apPswLayout;
	private EditText apPswEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wirelesssettingacy);
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

	private void setViewClick() {
		// TODO Auto-generated method stub
		whichItem = getIntent().getIntExtra("whichItem", 1);
		localService = ((AppData)getApplication()).getLocalService();
		localDeviceInfo =  (DeviceLocalInfo) LocalSettingActivity.mDevice.getLocalInfo().clone();// clone 设备信息
		
		wifiDetailLayout = (LinearLayout)findViewById(R.id.wifiDetailLayout);
		detailLayout = (LinearLayout)findViewById(R.id.detailLayout);
		apPswLayout = (RelativeLayout)findViewById(R.id.ap_psw_layout);
    	apPswEdit = (EditText)findViewById(R.id.ap_psw_edit);
    	apPswEdit.setText(localDeviceInfo.getWanServerIP());
    	apPswEdit.addTextChangedListener(new TextWatcher() {
			
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
				localDeviceInfo.setWanServerIP(apPswEdit.getText().toString());
			}
		});
		wifiClient = (NewSwitch)findViewById(R.id.wifiSwitch);
    	wifiClient.setChecked(localDeviceInfo.getEnableWiFi()==1);
    	apPswLayout.setVisibility(wifiClient.isChecked()?View.GONE:View.VISIBLE);
    	wifiClient.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				ssid.setEnabled(isChecked);
				password.setEnabled(isChecked);
				localDeviceInfo.setEnableWiFi(isChecked?1:0);	
				apPswLayout.setVisibility(wifiClient.isChecked()?View.GONE:View.VISIBLE);
				if(isChecked){
					wifiDetailLayout.setVisibility(View.VISIBLE);
				}else{
					wifiDetailLayout.setVisibility(View.GONE);
				}
			}
		});
    	       	
    	ssid = (EditText)findViewById(R.id.ssidEditText);
    	ssid.setEnabled(localDeviceInfo.getEnableWiFi()==1?true:false);
    	ssid.setText(localDeviceInfo.getWiFiSSID());
    	ssid.addTextChangedListener(new TextWatcher() {
			
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
					localDeviceInfo.setWiFiSSID(ssid.getText().toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
    	

    	
    	encryTypeList = new ArrayList<String>();
    	for(int i=0; i<encrypType.length; i++){
    		encryTypeList.add(encrypType[i]);
    	}
    	
    	wapLayout = (RelativeLayout)findViewById(R.id.tableRow3);
    	wapTx = (TextView)findViewById(R.id.wpaTx);
    	wapTx.setText(encryTypeList.get((localDeviceInfo.getWiFiEncryMode()%4)));
    	wapLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showEncryTypeDialog();
			}
		});
    	
    	
    	
    	if(LocalSettingActivity.ssidList != null)
    	{
    		adapterWifi = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, LocalSettingActivity.ssidList);
    	}
    	
    	btnSelectWifi = (ImageButton)findViewById(R.id.btnSelectWifi);
    	btnSelectWifi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(!wifiClient.isChecked())
				{
					wifiClient.performClick();//模拟点击
				}
				final Dialog dialog = new Dialog(WirelessSettingAcy.this, R.style.Erro_Dialog);
				dialog.setContentView(R.layout.wifilist_dialog);
				Button cancelBtn = (Button)dialog.findViewById(R.id.cancleBtn);
				cancelBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						dialog.dismiss(); 
					}
				});
				ListView listView = (ListView)dialog.findViewById(R.id.wifiListView);
				wifiListAdapter.isSingleChoice = false;
				listView.setAdapter(new wifiListAdapter(LocalSettingActivity.ssidList,getApplicationContext(),R.drawable.ic_switch_wifi_on));
				listView.setOnItemClickListener(new ListView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,int whichButton, long arg3) {
						// TODO Auto-generated method stub
						ssid.setText(LocalSettingActivity.ssidList.get(whichButton));
						ssid.setTextColor(color.black);
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
    	
    	
    	
    	password = (EditText)findViewById(R.id.passwordEditText); 
    	password.setEnabled(localDeviceInfo.getEnableWiFi()==1?true:false);
    	password.setText(localDeviceInfo.getWiFiPwd());
    	password.addTextChangedListener(new TextWatcher() {
			
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
				localDeviceInfo.setWiFiPwd(password.getText().toString());
			}
		});
    	
    	
    	dhcp = (NewSwitch)findViewById(R.id.dhcpSwitch);
    	dhcp.setChecked( localDeviceInfo.getEnableWiFiDHCP()==1);
    	dhcp.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				localDeviceInfo.setEnableWiFiDHCP(isChecked?1:0);					
				ipAddr.setEnabled(!isChecked);
				mask.setEnabled(!isChecked);
				gateWay.setEnabled(!isChecked);
				dns.setEnabled(!isChecked);
				
				if(isChecked){
					detailLayout.setVisibility(View.GONE);
				}else{
					detailLayout.setVisibility(View.VISIBLE);
				}
			}
		});
    	
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            @Override
        	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
                    if (!resultingTxt.matches ("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) { 
                        return "";
                    } else {
                        String[] splits = resultingTxt.split("\\.");
                        for (int i=0; i<splits.length; i++) {
                            if (Integer.valueOf(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
            return null;
            }
        };
    	
    	
    	ipAddr = (EditText)findViewById(R.id.ipAddrEditText); 
    	ipAddr.setText(localDeviceInfo.getWiFiIP());
    	ipAddr.setFilters(filters);
    	ipAddr.setEnabled(!dhcp.isChecked());                   //dhcp打开 则无法配置ip地址
    	ipAddr.addTextChangedListener(new TextWatcher() {
			
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
					localDeviceInfo.setWiFiIP(ipAddr.getText().toString());
				} catch (Exception e) {
					// TODO: handle exception
				}					
			}
		});
    	
    	mask = (EditText)findViewById(R.id.maskEditText);
    	mask.setText(localDeviceInfo.getWiFiMasK());
    	mask.setFilters(filters);
    	mask.setEnabled(!dhcp.isChecked());
    	mask.addTextChangedListener(new TextWatcher() {
			
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
				localDeviceInfo.setWiFiMasK(mask.getText().toString());
			}
		});
    	
    	gateWay = (EditText)findViewById(R.id.gateWayEditText);
    	gateWay.setText(localDeviceInfo.getWiFiGateWay());
    	gateWay.setFilters(filters);
    	gateWay.setEnabled(!dhcp.isChecked());
    	gateWay.addTextChangedListener(new TextWatcher() {
			
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
				localDeviceInfo.setWiFiGateWay(gateWay.getText().toString());
			}
		});
    	
    	dns = (EditText)findViewById(R.id.dnsEditText);
    	dns.setText(localDeviceInfo.getWiFiDNS0());  
    	dns.setFilters(filters);
    	dns.setEnabled(!dhcp.isChecked());
    	dns.addTextChangedListener(new TextWatcher() {
			
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
				localDeviceInfo.setWiFiDNS0(dns.getText().toString());
			}
		});
    	
    	
    	if(wifiClient.isChecked()){
			wifiDetailLayout.setVisibility(View.VISIBLE);
			
		}else{
			wifiDetailLayout.setVisibility(View.GONE);
		}
    	
    	if(dhcp.isChecked()){
    		detailLayout.setVisibility(View.GONE);
		}else{
			detailLayout.setVisibility(View.VISIBLE);
		}
    	
    	TextView yesButton = (TextView)findViewById(R.id.saveTx);
    	yesButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				asyncTask = new TimeOutAsyncTask();
				showSettingDialog(localDeviceInfo, localService);
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
    
    public void showEncryTypeDialog(){
		
		final Dialog dialog = new Dialog(this, R.style.Erro_Dialog);
		dialog.setContentView(R.layout.filesize_dialog);
		final ListView sizeList = (ListView)dialog.findViewById(R.id.sizeList);
		final wifiListAdapter adapter = new wifiListAdapter(encryTypeList, getApplicationContext(),R.drawable.lin_bac);
		adapter.isSingleChoice = true;
		adapter.choiceItem = (localDeviceInfo.getWiFiEncryMode()%4);
		sizeList.setAdapter(adapter);
		sizeList.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				adapter.choiceItem = position;
				adapter.notifyDataSetChanged();
				localDeviceInfo.setWiFiEncryMode((byte) position);
				wapTx.setText(encryTypeList.get(position));
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
						localService.search();
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
				
//				if(LocalSettingActivity.mb3gdevice && which == 5)
//				{
//					LocalSettingActivity.mediastream.setOnSet3GInfoListener(mOnSet3GInfoListener);
//				}
//				else
				{
					LocalSettingActivity.mediastream.setOnSetDevInfoListener(mOnSetDevInfoListener);
				}
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
				
            	GeneralInfoAcy.refreshDeviceInfo(localDeviceInfo);
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
	            	localService.setDeviceParam(localDeviceInfo);
	            }
	            else
	            {
//	            	if(LocalSettingActivity.mb3gdevice && whichItem == 5)//only in Net3GSettingDialogFragment
//	            	{
//	            		LocalSettingActivity.mediastream.send3GDevInfo(device3GInfo);
//	            	}
//	            	else
	            	{
	            		LocalSettingActivity.mediastream.setDevInfo(localDeviceInfo);
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
