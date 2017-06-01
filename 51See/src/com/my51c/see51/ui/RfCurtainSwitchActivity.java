package com.my51c.see51.ui;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.my51c.see51.listener.OnSetCurtainInfoListener;
import com.my51c.see51.listener.OnSetRFInfoListener;
import com.my51c.see51.media.RemoteInteractionStreamer;
import com.my51c.see51.protocal.RFPackage;
import com.my51c.see51.service.AppData;
import com.my51c.see51.widget.NewSwitch;
import com.my51c.see51.widget.ToastCommom;
import com.sdview.view.R;

public class RfCurtainSwitchActivity extends Activity implements OnClickListener{

	private ToastCommom toast = new ToastCommom();
	private static final int MSG_SET_CURTAIN_FAILED = 5;
	private static final int MSG_SET_CURTAIN_SUCCESS = 6;
	private LinearLayout back;
	private TextView title;
	private ProgressBar openLoading,closeLoading;
	private Button open,close,pause;
	public RemoteInteractionStreamer mediastream;
	private Dialog waitDialog;
	private AppData appData;
	private String curtainID = "";
	private String curtainNameStr = "";
	private ImageView pauseImg;
	private RelativeLayout changeDevName;
	private TextView mentionTx,curtainName;
	private TimeOutAsyncTask timeOutTask = null;
	private LinearLayout switchLayout,switchGroup1,switchGroup2,switchGroup3;
	private RelativeLayout curtainLayout;
	private NewSwitch mSwitch1,mSwitch2,mSwitch3;
	private LinearLayout mSwitchL1,mSwitchL2,mSwitchL3;
	private boolean isCurtain = false;
	private String idType = "";
	private int position;
	private RFPackage rfpack,curRfpack,switchPack;
	private ArrayList<Map<String, Object>> mRFList = new ArrayList<Map<String,Object>>();
	private Map<String, Object> mRFDevceInfo = null;
	private int switchNum;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rf_curtain_activity);
		curtainNameStr = getIntent().getStringExtra("curtainName");
		curtainID = getIntent().getStringExtra("curtainID");
		isCurtain = getIntent().getBooleanExtra("isCurtain", false);
		idType = getIntent().getStringExtra("idType");
		position = getIntent().getIntExtra("position", 0);
		appData = (AppData) getApplication();
		rfpack = RFDeviceInfoActivity.mDevice.getRFInfo();		
		mediastream = appData.getRemoteInteractionStreamer();
		mediastream.setOnSetRFInfoListener(mOnSetRFInfoListener);
    	mediastream.setOnSetCurtainInfoListener(mOnSetCurtainInfoListener);
		findView();
    	setView();
	}
	private void findView() {
		back = (LinearLayout)findViewById(R.id.curtain_back_layout);
		title = (TextView)findViewById(R.id.curtain_titleName);
		openLoading = (ProgressBar)findViewById(R.id.open_progress);
		closeLoading = (ProgressBar)findViewById(R.id.close_progress);
		open = (Button)findViewById(R.id.open);
		close = (Button)findViewById(R.id.close);
		pause = (Button)findViewById(R.id.pause);
		pauseImg = (ImageView)findViewById(R.id.pauseImg);
		changeDevName = (RelativeLayout)findViewById(R.id.changeDevName);
		mentionTx = (TextView)findViewById(R.id.mentionTx);
		curtainName = (TextView)findViewById(R.id.curtainName);
		curtainLayout = (RelativeLayout)findViewById(R.id.curtainLayout);
		switchLayout = (LinearLayout)findViewById(R.id.switchLayout);
		mSwitch1 = (NewSwitch)findViewById(R.id.mSwitch1);
		mSwitch2 = (NewSwitch)findViewById(R.id.mSwitch2);
		mSwitch3 = (NewSwitch)findViewById(R.id.mSwitch3);
		switchGroup1 = (LinearLayout)findViewById(R.id.swtichGroup1);
		switchGroup2 = (LinearLayout)findViewById(R.id.swtichGroup2);
		switchGroup3 = (LinearLayout)findViewById(R.id.swtichGroup3);
		mSwitchL1 = (LinearLayout)findViewById(R.id.mSwitchL1);
		mSwitchL2 = (LinearLayout)findViewById(R.id.mSwitchL2);
		mSwitchL3 = (LinearLayout)findViewById(R.id.mSwitchL3);
		
		open.setOnClickListener(this);
		close.setOnClickListener(this);
		pause.setOnClickListener(this);
		back.setOnClickListener(this);
		changeDevName.setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	public void setView(){
		mentionTx.setText("("+mentionTx.getText().toString()+")");
		title.setText(curtainNameStr);
		curtainName.setText(curtainNameStr);
		if(isCurtain){
			curtainLayout.setVisibility(View.VISIBLE);
			switchLayout.setVisibility(View.GONE);
		}else{
			curtainLayout.setVisibility(View.GONE);
			switchLayout.setVisibility(View.VISIBLE);
			if(idType.equals("12")){
				switchGroup2.setVisibility(View.VISIBLE);
	    	}
	    	if(idType.equals("13")){
	    		switchGroup2.setVisibility(View.VISIBLE);
	    		switchGroup3.setVisibility(View.VISIBLE);
	    	}
	    	if(idType.equals("11")){
	    		switchGroup2.setVisibility(View.GONE);
	    		switchGroup3.setVisibility(View.GONE);
	    	}
	    	
	    	getSwitchStatus();
	    	
	    	mSwitchL1.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					boolean switch1 = ((switchNum & 0x01)==1);
					if(!switch1){
						switchNum |= 0x01;
					}else{
						switchNum &= 0xFE;
					}
					String str ="0"+ String.valueOf(switchNum);
					switchPack.setValue((String)mRFDevceInfo.get("MY51CRFID"), "status",str);
					showWaitDialog(switchPack);
				}
			});
			mSwitchL2.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					boolean switch2 = (((switchNum & 0x02) >>1) ==1);
					if(!switch2){
						switchNum |= 0x02;
					}else{
						switchNum &= 0xfd;
					}
					String str = "0"+String.valueOf(switchNum);
					System.out.println("-----"+str);
					switchPack.setValue((String)mRFDevceInfo.get("MY51CRFID"), "status",str);
					showWaitDialog(switchPack);
				}
			});
			mSwitchL3.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					boolean switch3 = (((switchNum & 0x04) >>2)==1);
					if(!switch3){
						switchNum |= 0x04;
					}else{
						switchNum &= 0xfb;
					}
					String str = "0"+String.valueOf(switchNum);
					System.out.println("-----"+str);
					switchPack.setValue((String)mRFDevceInfo.get("MY51CRFID"), "status",str);		
					showWaitDialog(switchPack);
				}
			});
				
		}
	}

	public void getSwitchStatus(){
		switchPack = new RFPackage();
		switchPack.parseArrayList(rfpack.getRFDevList());
		
		List<Map<String, Object>> mRFTotalList = rfpack.getRFDevList();//获取包含所有rf设备信息的list
		if(mRFTotalList == null)
		{	
			return;
		}
		mRFList.clear();
		for(int i=0; i< mRFTotalList.size(); i++)
		{
			HashMap<String, Object> map =  (HashMap<String, Object>) mRFTotalList.get(i);
			String strID = (String)map.get("MY51CRFID");
			String strType = strID.substring(0,2);
			if(strType.equals(idType))
			{
				mRFList.add(map);
			}
		}
	    mRFDevceInfo = mRFList.get(position);
	    String status = (String) mRFDevceInfo.get("status");
		switchNum = Integer.parseInt(status);
		boolean switch1 = ((switchNum & 0x01)==1);
    	boolean switch2 = (((switchNum & 0x02) >>1) ==1);
    	boolean switch3 = (((switchNum & 0x04) >>2)==1);
    	mSwitch1.setChecked(switch1);
    	mSwitch2.setChecked(switch2);
    	mSwitch3.setChecked(switch3);
	}
	
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.open:
			mediastream.setCurtainInfo(curtainID+"2001");
			openLoading.setVisibility(View.VISIBLE);
			closeLoading.setVisibility(View.GONE);
			pauseImg.setVisibility(View.GONE);
			open.setBackgroundResource(R.drawable.shap_curtain_btn_close);
			close.setBackgroundResource(R.drawable.shap_curtain_btn);
			pause.setBackgroundResource(R.drawable.shap_curtain_btn);
			break;
		case R.id.close:
			mediastream.setCurtainInfo(curtainID+"2003");
			openLoading.setVisibility(View.GONE);
			closeLoading.setVisibility(View.VISIBLE);
			pauseImg.setVisibility(View.GONE);
			close.setBackgroundResource(R.drawable.shap_curtain_btn_close);
			open.setBackgroundResource(R.drawable.shap_curtain_btn);
			pause.setBackgroundResource(R.drawable.shap_curtain_btn);
			break;
		case R.id.pause:
			mediastream.setCurtainInfo(curtainID+"2002");
			openLoading.setVisibility(View.GONE);
			closeLoading.setVisibility(View.GONE);
			pauseImg.setVisibility(View.VISIBLE);
			pause.setBackgroundResource(R.drawable.shap_curtain_btn_close);
			open.setBackgroundResource(R.drawable.shap_curtain_btn);
			close.setBackgroundResource(R.drawable.shap_curtain_btn);
			break;
		case R.id.curtain_back_layout:
			finish();
			break;
		case R.id.changeDevName:
			showRenameDialog();
			break;
		default:
			break;
		}
	}
	
	private OnSetCurtainInfoListener mOnSetCurtainInfoListener = new OnSetCurtainInfoListener() {
			
			@Override
			public void OnSetCurtainInfoSuccess() {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(MSG_SET_CURTAIN_SUCCESS);
			}
			
			@Override
			public void OnSetCurtainInfoFailed() {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(MSG_SET_CURTAIN_FAILED);
			}
		};
	private OnSetRFInfoListener mOnSetRFInfoListener = new OnSetRFInfoListener()
		{

			@Override
			public void onSetRFInfoFailed() {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(MSG_SET_CURTAIN_FAILED);
			}

			@Override
			public void onSetRFInfoSuccess() {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(MSG_SET_CURTAIN_SUCCESS);
			}
		};
	
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_SET_CURTAIN_FAILED:
				if(waitDialog!=null){
					waitDialog.dismiss();
				}
				toast.ToastShow(getApplicationContext(), getString(R.string.rfsettingfail), 1000);
				break;
			case MSG_SET_CURTAIN_SUCCESS:
				if(waitDialog!=null){
					waitDialog.dismiss();
					rfpack = curRfpack;
					RFDeviceInfoActivity.mDevice.setRFInfo(rfpack);
					sendBroadcast(new Intent(RfDeviceListActivity.UPDATE_ACTION));
					if(!isCurtain){
						getSwitchStatus();
					}
					title.setText(curtainNameStr);
					curtainName.setText(curtainNameStr);
					if(timeOutTask!=null){
						timeOutTask.cancel(true);
						timeOutTask = null;
					}
				}
				toast.ToastShow(getApplicationContext(), getString(R.string.rfsettingsuccess), 1000);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	private void showRenameDialog(){
		final Dialog renameDialog =new Dialog(RfCurtainSwitchActivity.this, R.style.Erro_Dialog);
		renameDialog.setContentView(R.layout.rename_dialog);
		final EditText rename = (EditText)renameDialog.findViewById(R.id.rename_Edit);
		Button ok = (Button)renameDialog.findViewById(R.id.rename_ok);
		Button cancel = (Button)renameDialog.findViewById(R.id.rename_cancel);
		final RFPackage rpackclone = new RFPackage();
	    rpackclone.parseArrayList(rfpack.getRFDevList());
	    
	    rename.setText(curtainNameStr);
	    rename.setSelection(rename.length());
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(rename.getText().toString().trim().equals(""))
				{
					toast.ToastShow(getApplicationContext(), getString(R.string.newdevicenameincomplete), 1000);
				}else if(rename.getText().toString().equals(curtainNameStr)){
					renameDialog.dismiss();
					toast.ToastShow(getApplicationContext(), getString(R.string.rfsettingsuccess), 1000);
				}
				else{
					try {
						curtainNameStr = URLDecoder.decode(rename.getText().toString(), "UTF-8");
						rpackclone.setValue(curtainID, "name", curtainNameStr);
						showWaitDialog(rpackclone);
						renameDialog.dismiss();
					} catch (Exception e) {
						// TODO: handle exception
					}	
				}
			}
		});
		 cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				renameDialog.dismiss();
			}
		});    
		  
		renameDialog.show();
	}
	
	 public void showWaitDialog(RFPackage pac){
	    	waitDialog = new Dialog(RfCurtainSwitchActivity.this, R.style.Login_Dialog);
	    	waitDialog.setContentView(R.layout.login_dialog);
	    	TextView dialogTx = (TextView)waitDialog.findViewById(R.id.tx);
	    	dialogTx.setText(getString(R.string.rf_setting));
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
	            mediastream.sendRFDevInfo(pac,curtainID);//请求数据
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
	            mHandler.sendEmptyMessage(MSG_SET_CURTAIN_FAILED);
	        }
	 }
}
