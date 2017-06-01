package com.my51c.see51.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.my51c.see51.guide.DimensionActivity;
import com.my51c.see51.listener.OnSetRFInfoListener;
import com.my51c.see51.media.RemoteInteractionStreamer;
import com.my51c.see51.protocal.RFPackage;
import com.my51c.see51.service.AppData;
import com.my51c.see51.widget.ToastCommom;
import com.sdview.view.R;

public class RfDeviceAddActivity extends SherlockFragmentActivity implements OnClickListener {
	
	private String TAG = "RfDeviceAddActivity";
	private EditText edtRFID;
	private ImageButton imgBtnDimension;
	private Button btnAddRFDevice;
	static public RemoteInteractionStreamer mediastream;
	private AppData appData;
	private RFPackage rfclone;
	private String rfid;
	private Dialog waitDialog;
	private TimeOutAsyncTask timeOutTask = null;
	private ToastCommom toast = new ToastCommom();
	private static final int MSG_SET_SUCESS = 0; 
	private static final int MSG_SET_FAILED = 1;
	private static final int MSG_SET_TIMEOUT = 2;
	private RFPackage rfpack,curRfpack;
	private LinearLayout backLayout;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.rfdevice_guide);
		appData = (AppData) getApplication();
		findview();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		imgBtnDimension.setOnClickListener(this);
		btnAddRFDevice.setOnClickListener(this);
		
		
		if(RFDeviceInfoActivity.mDevice != null)
		{
			rfpack = RFDeviceInfoActivity.mDevice.getRFInfo();
		}
		
		String rfdeviceId = DimensionActivity.deviceId;
		
		if (rfdeviceId != null)
		{	
			if(rfdeviceId.length() == 8)
			{	
				edtRFID.setText(rfdeviceId);
				edtRFID.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
				btnAddRFDevice.setVisibility(View.VISIBLE);
			}
			else
			{	
				rfdeviceId = "";
				Toast toastQRcode = Toast.makeText(getApplicationContext(), getString(R.string.guideQRCodeError), Toast.LENGTH_SHORT);
				toastQRcode.setGravity(Gravity.CENTER, 0, 0);
				toastQRcode.show();
			}	
		}
		
		edtTextchangeListener();
		mediastream = appData.getRemoteInteractionStreamer();
		mediastream.setOnSetRFInfoListener(mOnSetRFInfoListener);
	}
	
	public void backMainActivity()
	{
		RfDeviceAddActivity.this.finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}
	
	public void edtTextchangeListener()
	{
		edtRFID.addTextChangedListener(new TextWatcher()
		{
			private int selectionStart;
			private int selectionEnd;

			@Override
			public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3)
			{
			}

			@Override
			public void afterTextChanged(Editable s)
			{
				String temp = s.toString();
				selectionStart = edtRFID.getSelectionStart();
				selectionEnd = edtRFID.getSelectionEnd();
				//Log.i("gongbiao1", "" + selectionStart);
				if (temp.length() == 8)
				{
					edtRFID.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
					btnAddRFDevice.setVisibility(View.VISIBLE);
				} else if (temp.length() > 8)
				{	
					edtRFID.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
					btnAddRFDevice.setVisibility(View.VISIBLE);
					s.delete(selectionStart - 1, selectionEnd);
					int tempSelection = selectionStart;
					edtRFID.setText(s);
					edtRFID.setSelection(tempSelection);
					
				} else if (temp.length() < 8)
				{
					Drawable drawable = getResources().getDrawable(R.drawable.error);
					edtRFID.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
					btnAddRFDevice.setVisibility(View.GONE);
				}
			}
		});
	}
	
	private void findview()
	{
		edtRFID = (EditText)findViewById(R.id.edtRFId);
		imgBtnDimension = (ImageButton) findViewById(R.id.imgBtnDimension);
		btnAddRFDevice = (Button) findViewById(R.id.btnAddRFDevice);
		backLayout = (LinearLayout)findViewById(R.id.add_back_layout);
		backLayout.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.imgBtnDimension:
		{
			Intent intent = new Intent();
			intent.setClass(RfDeviceAddActivity.this, DimensionActivity.class);
			intent.putExtra("isguide", false);
			RfDeviceAddActivity.this.startActivity(intent);
		}
			break;
		
		case R.id.btnAddRFDevice:
		{
			addNewRFInfo();
		}
			break;
		case R.id.add_back_layout:
			backMainActivity();
			break;
		default:
			break;	
			
		}
	}
	
	public void addNewRFInfo()
	{
		rfclone = new RFPackage();
		if(rfpack != null)
		{
			rfclone.parseArrayList(rfpack.getRFDevList());
		}
		rfid = edtRFID.getText().toString();
		if(rfid.equals(""))
		{
			return;
		}
		rfclone.addId(rfid);
		
		String strType = rfid.substring(0,2);
		if(strType.equals("01"))
		{
			rfclone.setValue(rfid, "name", "controller");
		}
		else if(strType.equals("02"))
		{
			rfclone.setValue(rfid, "name", "door");
		}
		else if(strType.equals("03"))
		{
			rfclone.setValue(rfid, "name", "pir");
		}
		else if(strType.equals("04"))
		{
			rfclone.setValue(rfid, "name", "smoking");
		}
		else if(strType.equals("10"))
		{
			rfclone.setValue(rfid, "name", "smart socket");
		}
		else if(strType.equals("21"))
		{
			rfclone.setValue(rfid, "name", "alarmer");
		}
		else if(strType.equals("22"))
		{
			rfclone.setValue(rfid, "name", "bell");
		}
		else if(strType.equals("23"))
		{
			rfclone.setValue(rfid, "name", "out_io");
		}
		else if(strType.equals("23"))
		{
			rfclone.setValue(rfid, "name", "out_io");
		}
		else if(strType.equals("a0"))
		{
			rfclone.setValue(rfid, "name", "curtain");
		}
		else if(strType.equals("a1"))
		{
			rfclone.setValue(rfid, "name", "Anti-theft lock");
		}
		else if(strType.equals("11"))
		{
			rfclone.setValue(rfid, "name", "Switch1");
			rfclone.setValue(rfid, "status", "01");
		}
		else if(strType.equals("12"))
		{
			rfclone.setValue(rfid, "name", "Switch2");
			rfclone.setValue(rfid, "status", "03");
		}
		else if(strType.equals("13"))
		{
			rfclone.setValue(rfid, "name", "Switch3");
			rfclone.setValue(rfid, "status", "07");
		}
		else if(strType.equals("a2"))
		{
			rfclone.setValue(rfid, "name", "Light");
		}
	
		showWaitDialog(rfclone);
		
	}
	
	public void showWaitDialog(RFPackage pac){
    	waitDialog = new Dialog(RfDeviceAddActivity.this, R.style.Login_Dialog);
    	waitDialog.setContentView(R.layout.login_dialog);
    	TextView dialogTx = (TextView)waitDialog.findViewById(R.id.tx);
    	dialogTx.setText(getString(R.string.rf_add_mention));
    	waitDialog.show();
    	curRfpack = pac;
    	timeOutTask = new TimeOutAsyncTask(pac);
    	timeOutTask.execute(0);
	}
	
	private class TimeOutAsyncTask extends AsyncTask<Integer, Integer, String>{
		
	 	private RFPackage pac;
	 	public TimeOutAsyncTask(RFPackage pac){
	 		this.pac = pac;
	 	}
	 
	 	@Override  
        protected void onPreExecute() {  
            super.onPreExecute();  
            mediastream.sendRFDevInfo(pac,rfid);//ÇëÇóÊý¾Ý
        } 
		        	
		@Override
		protected String doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(10000);
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
            mHandler.sendEmptyMessage(MSG_SET_FAILED);
        }
	}
	
	private Handler mHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		if (msg.what == MSG_SET_SUCESS){	
    			rfpack = curRfpack;
            	RFDeviceInfoActivity.mDevice.setRFInfo(rfpack);
            	toast.ToastShow(getApplicationContext(), getString(R.string.rfsettingsuccess), 1000);
            	if(timeOutTask!=null){
					timeOutTask.cancel(true);
					timeOutTask = null;
				}
            	if(waitDialog!=null){
					waitDialog.dismiss();
				}

            }else if (msg.what == MSG_SET_FAILED) {			            	
            	
            	toast.ToastShow(getApplicationContext(), getString(R.string.rfsettingfail), 1000);
            	timeOutTask.cancel(true);
            	if(waitDialog!=null){
					waitDialog.dismiss();
				}
    		}else if (msg.what == MSG_SET_TIMEOUT) {
    			toast.ToastShow(getApplicationContext(), getString(R.string.rfsettingfail), 1000);
    			if(waitDialog!=null){
					waitDialog.dismiss();
				}
    		} 
    	};
    };
    
	private OnSetRFInfoListener mOnSetRFInfoListener = new OnSetRFInfoListener()
	{

		@Override
		public void onSetRFInfoFailed() {
			// TODO Auto-generated method stub
			mHandler.sendEmptyMessage(MSG_SET_FAILED);
		}

		@Override
		public void onSetRFInfoSuccess() {
			// TODO Auto-generated method stub
			mHandler.sendEmptyMessage(MSG_SET_SUCESS);
		}
	};
}
