package com.my51c.see51.ui;


import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.my51c.see51.listener.OnSetCurtainInfoListener;
import com.my51c.see51.listener.OnSetRFInfoListener;
import com.my51c.see51.media.RemoteInteractionStreamer;
import com.my51c.see51.protocal.RFPackage;
import com.my51c.see51.service.AppData;
import com.my51c.see51.widget.NewSwitch;
import com.my51c.see51.widget.SweepLayout;
import com.my51c.see51.widget.SweepListView;
import com.my51c.see51.widget.ToastCommom;
import com.sdview.view.R;

public class RfDeviceListActivity extends Activity implements OnClickListener,OnItemClickListener{
	private final String TAG = "RfDeviceListActivity";
	private int itemnum;
	private RFPackage rfpack;
	private ArrayList<Map<String, Object>> mRFList;
	private MyAdapter adapter = null;
	private RemoteInteractionStreamer mediastream;
	private AppData appData;
	private SweepListView rfListView;
	//private SwipeRefreshLayout mSwipeLayout;
	private static String curid = "";
	private boolean isCurtain = false;
	private boolean isSwitch = false;
	private int[] rfimagelog = {R.drawable.grid_controller_pre, R.drawable.grid_doorsensor_pre, R.drawable.grid_pirsensor_pre,
			R.drawable.grid_smokesensor_pre, R.drawable.grid_smartplug_pre, R.drawable.grid_soundlight_pre,
			R.drawable.grid_doorcamera_pre, R.drawable.grid_iosensor_pre,R.drawable.grid_blp_pre,R.drawable.grid_curtain_pre,R.drawable.grid_lock_pre
			,R.drawable.rf_switch_pre,R.drawable.rf_switch2_pre,R.drawable.rf_switch3_pre,R.drawable.rf_light_pre};
	private int[] rfimagelog_off= {R.drawable.shap_controller, R.drawable.shap_doorsensor, R.drawable.shap_pir,
			R.drawable.shap_smoke, R.drawable.shap_smartplug, R.drawable.shap_sound,
			R.drawable.shap_doorcamera, R.drawable.shap_io,R.drawable.shap_blp,
			R.drawable.shap_curtain,R.drawable.shap_lock,R.drawable.shap_rf_switch,R.drawable.shap_rf_switch2,
			R.drawable.shap_rf_switch3,R.drawable.shap_rf_light};
	private Dialog waitDialog;
	private static final int MSG_SET_SUCESS = 0; 
	private static final int MSG_SET_FAILED = 1;
	private static final int MSG_SET_TIMEOUT = 2;
	private static final int MSG_SET_CURTAIN_FAILED = 3;
	private static final int MSG_SET_CURTAIN_SUCCESS = 4;
	public static final String UPDATE_ACTION = "UPDATE_ACTION";
	TimeOutAsyncTask asyncTask;
	private RFPackage curRfPackage;
	private ToastCommom toast = new ToastCommom();
	private TextView titleName;
	int switchNum = 0x00;
	private Dialog renameDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rfdevice_list);
		rfListView = (SweepListView)findViewById(R.id.rfInfoList);
		titleName = (TextView)findViewById(R.id.titleName);
		ImageView addImg = (ImageView)findViewById(R.id.addImg);
		LinearLayout backLayout = (LinearLayout)findViewById(R.id.rf_back_layout);
		
		itemnum = getIntent().getIntExtra("itemnum",0);
		titleName.setText(RFDeviceInfoActivity.typeDeviceName[itemnum]);
		rfListView.setOnItemClickListener(this);
		addImg.setOnClickListener(this);
		backLayout.setOnClickListener(this);
	}
	
	@Override
	public void onResume() {//初始化rfdev
		try {
			rfpack = RFDeviceInfoActivity.mDevice.getRFInfo();									//RFPackage
			appData = (AppData) getApplication();
			mediastream = appData.getRemoteInteractionStreamer();
			mediastream.setOnSetRFInfoListener(mOnSetRFInfoListener);
			mediastream.setOnSetCurtainInfoListener(mOnSetCurtainInfoListener);
			ParseDevice(itemnum);										//初始化 mRFList
			adapter = new MyAdapter(getApplicationContext());
			rfListView.setAdapter(adapter);
			rfListView.setOnItemClickListener(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onResume();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.deleteL:
			int position = (Integer)v.getTag();
			RFPackage rpackcloneother = new RFPackage();
			curid = (String) mRFList.get(position).get("MY51CRFID");
			rpackcloneother.parseArrayList(rfpack.getRFDevList());
        	Map<String, Object> mTempInfo= mRFList.get(position);
			rpackcloneother.RemoveId((String)mTempInfo.get("MY51CRFID"));
        	showWaitDialog(position, rpackcloneother);
        	int firVisible = rfListView.getFirstVisiblePosition();
			SweepLayout sweepLayout = (SweepLayout)rfListView.getChildAt(position-firVisible).findViewById(R.id.sweeplayout);
			sweepLayout.shrik(100);
			break;
		case R.id.rf_back_layout:
			backMainActivity();
			break;
		case R.id.addImg:
			Intent intent = new Intent();
			intent.putExtra("id", RFDeviceInfoActivity.deviceID);
			intent.putExtra("isLocal", RFDeviceInfoActivity.isLocal);
			intent.setClass(getApplicationContext(), RfDeviceAddActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}
	
	public void onRefreshRFDevice()//刷新RF设备
	{
		rfpack = RFDeviceInfoActivity.mDevice.getRFInfo();
		appData = (AppData)getApplication();
		mediastream = appData.getRemoteInteractionStreamer();
		ParseDevice(itemnum);
		if(adapter == null)
		{
			adapter = new MyAdapter(getApplicationContext());
			rfListView.setAdapter(adapter);
		}
		adapter.notifyDataSetChanged();
	}
	
    public void showWaitDialog(int which, RFPackage inPack){
    	waitDialog = new Dialog(RfDeviceListActivity.this, R.style.Login_Dialog);
    	waitDialog.setContentView(R.layout.login_dialog);
    	TextView dialogTx = (TextView)waitDialog.findViewById(R.id.tx);
    	dialogTx.setText(getString(R.string.rf_setting));
    	waitDialog.show();
    	asyncTask = new TimeOutAsyncTask(inPack);//请求数据任务
		asyncTask.execute(0);
		waitDialog.setOnKeyListener(new Dialog.OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if(keyCode == event.KEYCODE_BACK){
					if (!asyncTask.isCancelled()){
		        		asyncTask.cancel(true);
		        	}
				}
				return false;
			}
		});
    }
    
    /**设置RF设备时显示ProgressDialog*/
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
    
    private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_SET_SUCESS:
				if(waitDialog!=null){
					waitDialog.dismiss();
				}
				rfpack = curRfPackage;
				RFDeviceInfoActivity.mDevice.setRFInfo(rfpack);//设置命令发送成功，reset--RFpackage
				onRefreshRFDevice();
            	toast.ToastShow(getApplicationContext(), getString(R.string.rfsettingsuccess), 1000);
	        	asyncTask.cancel(true);	
				break;
			case MSG_SET_FAILED:
				if(waitDialog!=null){
					waitDialog.dismiss();
				}
				toast.ToastShow(getApplicationContext(), getString(R.string.rfsettingfail), 1000);
	        	asyncTask.cancel(true);
				break;
			case MSG_SET_TIMEOUT:
				if(waitDialog!=null){
					waitDialog.dismiss();
				}
				toast.ToastShow(getApplicationContext(), getString(R.string.rfsettingfail), 1000);
				break;
			case MSG_SET_CURTAIN_FAILED:
				toast.ToastShow(getApplicationContext(), getString(R.string.rfsettingfail), 1000);
				break;
			case MSG_SET_CURTAIN_SUCCESS:
				toast.ToastShow(getApplicationContext(), getString(R.string.rfsettingsuccess), 1000);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	 private class TimeOutAsyncTask extends AsyncTask<Integer, Integer, String>{
			
		 	private RFPackage mRFPack;
		 	public TimeOutAsyncTask(RFPackage mRFPack){
		 		this.mRFPack = mRFPack;
		 	}
		 	@Override  
	        protected void onPreExecute() {  
	            super.onPreExecute();  
	            curRfPackage = mRFPack;
	            mediastream.sendRFDevInfo(mRFPack,curid);
	            curid = "";
	        } 
     		        	
			@Override
			protected String doInBackground(Integer... params) {
				// TODO Auto-generated method stub
				try {
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
	            mHandler.sendEmptyMessage(MSG_SET_TIMEOUT);
	        }
     }
    
   
	
	private void ParseDevice(int nItem)
	{	
		if(mRFList == null)
		{
			mRFList = new ArrayList<Map<String,Object>>();
		}
		
		mRFList.clear();
		
		if(rfpack == null)
		{
			return;
		}
		
		
		List<Map<String, Object>> mRFTotalList = rfpack.getRFDevList();//获取包含所有rf设备信息的list
		if(mRFTotalList == null)
		{	
			return;
		}
		
		for(int i=0; i< mRFTotalList.size(); i++)//遍历rfinfoList，根据gridview--position取出对应map
		{
			HashMap<String, Object> map =  (HashMap<String, Object>) mRFTotalList.get(i);
			String strID = (String)map.get("MY51CRFID");
			String strType = strID.substring(0,2);
			if(nItem == 0)
			{
				if(strType.equals("01"))
				{
					mRFList.add(map);
				}
			}
			else if(nItem == 1)
			{
				if(strType.equals("02"))
				{
					mRFList.add(map);
				}
			}
			else if(nItem == 2)
			{
				if(strType.equals("03"))
				{
					mRFList.add(map);
				}
			}
			else if(nItem == 3)
			{
				if(strType.equals("04"))
				{
					mRFList.add(map);
				}
			}
			else if(nItem == 4)
			{
				if(strType.equals("10"))
				{
					mRFList.add(map);
				}
			}
			else if(nItem == 5)
			{
				if(strType.equals("21"))
				{
					mRFList.add(map);
				}
			}
			else if(nItem == 6)
			{
				if(strType.equals("22"))
				{
					mRFList.add(map);
				}
			}
			else if(nItem == 7)
			{
				if(strType.equals("23"))
				{
					mRFList.add(map);
				}
			}
			else if(nItem == 9)
			{
				if(strType.equals("a0"))
				{
					isCurtain = true;
					mRFList.add(map);
				}
			}
			else if(nItem == 10)
			{
				if(strType.equals("a1"))
				{
					mRFList.add(map);
				}
			}
			else if(nItem == 11)
			{
				if(strType.equals("11"))
				{
					isSwitch = true;
					mRFList.add(map);
				}
			}
			else if(nItem == 12)
			{
				if(strType.equals("12"))
				{
					isSwitch = true;
					mRFList.add(map);
				}
			}
			else if(nItem == 13)
			{
				if(strType.equals("13"))
				{
					isSwitch = true;
					mRFList.add(map);
				}
			}
			else if(nItem == 14)
			{
				if(strType.equals("a2"))
				{
					mRFList.add(map);
				}
			}
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		System.out.println("=====click"+position);
		if(isCurtain){
    		Intent intent = new Intent(RfDeviceListActivity.this, RfCurtainSwitchActivity.class);
    		intent.putExtra("isCurtain", true);
    		intent.putExtra("idType", "");
    		intent.putExtra("curtainID", (String) mRFList.get(position).get("MY51CRFID"));
    		intent.putExtra("curtainName", (String)mRFList.get(position).get("name"));
    		startActivity(intent);
    	}else if(isSwitch){
    		String idType = ((String) mRFList.get(position).get("MY51CRFID")).substring(0,2);
    		Intent intent = new Intent(RfDeviceListActivity.this, RfCurtainSwitchActivity.class);
    		intent.putExtra("isCurtain", false);
    		intent.putExtra("mRFList", mRFList);
    		intent.putExtra("position", position);
    		intent.putExtra("idType", idType);
    		intent.putExtra("curtainID", (String) mRFList.get(position).get("MY51CRFID"));
    		intent.putExtra("curtainName", (String)mRFList.get(position).get("name"));
    		startActivity(intent);
    	}else{
    		showRenameDialog(position);
    	}
	}
	
	private void showRenameDialog(final int position){
		renameDialog =new Dialog(RfDeviceListActivity.this, R.style.Erro_Dialog);
		renameDialog.setContentView(R.layout.rename_dialog);
		final EditText rename = (EditText)renameDialog.findViewById(R.id.rename_Edit);
		Button ok = (Button)renameDialog.findViewById(R.id.rename_ok);
		Button cancel = (Button)renameDialog.findViewById(R.id.rename_cancel);
		final RFPackage rpackclone = new RFPackage();
	    rpackclone.parseArrayList(rfpack.getRFDevList());
	    rename.setText((String)mRFList.get(position).get("name"));
	    rename.setSelection(rename.length());
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String curtainNameStr = rename.getText().toString();
				if(rename.getText().toString().trim().equals(""))
				{
					toast.ToastShow(getApplicationContext(), getString(R.string.newdevicenameincomplete), 1000);
				}else if(rename.getText().toString().equals((String)mRFList.get(position).get("name"))){
					renameDialog.dismiss();
					toast.ToastShow(getApplicationContext(), getString(R.string.rfsettingsuccess), 1000);
				}else{
					try {
						curtainNameStr = URLDecoder.decode(rename.getText().toString(), "UTF-8");
						rpackclone.setValue((String) mRFList.get(position).get("MY51CRFID"), "name", curtainNameStr);
						showWaitDialog(position,rpackclone);
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
	
	public static class ViewHolder{
        public ImageView camimage;
        public TextView rfname;
        public TextView rfid;
        public ImageView editBtn;
        public LinearLayout deleteL;
        public SweepLayout sweepLayout;
        public NewSwitch mSwitch;
        public RelativeLayout bntLayout;
    }
     
     
    public class MyAdapter extends BaseAdapter{//rfinfolist---adapter---listFragment
 
        private LayoutInflater mInflater;
         
        public MyAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mRFList.size();
        }
 
        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }
 
        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }
 
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
             
        	ViewHolder holder = null;
            if (convertView == null) {
                holder=new ViewHolder();  
                convertView = mInflater.inflate(R.layout.rf_list_item, null);
                holder.camimage = (ImageView) convertView.findViewById(R.id.cam_image);
                holder.rfname = (TextView)convertView.findViewById(R.id.rfdevicename);
                holder.rfid = (TextView)convertView.findViewById(R.id.rfdeviceid);
                holder.editBtn = (ImageView)convertView.findViewById(R.id.rfdevice_edit);
                holder.deleteL = (LinearLayout)convertView.findViewById(R.id.deleteL);
                holder.sweepLayout = (SweepLayout)convertView.findViewById(R.id.sweeplayout);
                holder.mSwitch = (NewSwitch)convertView.findViewById(R.id.mSwitch);
                holder.bntLayout = (RelativeLayout)convertView.findViewById(R.id.btnLayout);
                convertView.setTag(holder);
            }
            else 
            {
                holder = (ViewHolder)convertView.getTag();
            }
             
            holder.rfid.setText((String) mRFList.get(position).get("MY51CRFID"));
	        holder.rfname.setText((String)mRFList.get(position).get("name"));
	        holder.editBtn.setBackgroundResource(R.drawable.setting_style);
	        holder.deleteL.setTag(position);
	        holder.mSwitch.setTag(position);
	        holder.deleteL.setOnClickListener(RfDeviceListActivity.this);
	       
            if(isCurtain || isSwitch){
	        	holder.mSwitch.setVisibility(View.INVISIBLE);
	        	holder.bntLayout.setVisibility(View.INVISIBLE);
	        	holder.camimage.setBackgroundResource(rfimagelog[itemnum]);
	        }else{
	        	final Map<String, Object> mRFDevceInfo;
	            mRFDevceInfo  = mRFList.get(position);
	            final RFPackage rpackclone = new RFPackage();
	        	rpackclone.parseArrayList(rfpack.getRFDevList());
	        	final String status = (String) mRFDevceInfo.get("status");
	            holder.bntLayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Log.i(TAG, "click switch");
						if(!status.equals("on")){
							rpackclone.setValue((String)mRFDevceInfo.get("MY51CRFID"), "status","on");
						}else{
							rpackclone.setValue((String)mRFDevceInfo.get("MY51CRFID"), "status","off");
						}
						curid = (String)mRFDevceInfo.get("MY51CRFID");
						showWaitDialog(position,rpackclone);
					}
				});
	        	if(status.equals("on"))
	        	{
	        		holder.mSwitch.setChecked(true);
	        		holder.camimage.setBackgroundResource(rfimagelog[itemnum]);
	        	}
	        	else
	        	{
	        		holder.mSwitch.setChecked(false);
	        		holder.camimage.setBackgroundResource(rfimagelog_off[itemnum]);
	        	}
	        }
            return convertView;
        }    
    }
    
    private void setReceiver(){
    	try {
			unregisterReceiver(receiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	IntentFilter intentFilter = new IntentFilter(UPDATE_ACTION);
    	registerReceiver(receiver, intentFilter);
    }
    
    private BroadcastReceiver receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			System.out.println("收到刷新广播");
			onRefreshRFDevice();
		}
    };
    
    public void backMainActivity()
	{
		finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}

}
