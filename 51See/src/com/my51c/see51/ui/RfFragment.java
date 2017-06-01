package com.my51c.see51.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.my51c.see51.listener.OnSetCurtainInfoListener;
import com.my51c.see51.listener.OnSetRFInfoListener;
import com.my51c.see51.media.RemoteInteractionStreamer;
import com.my51c.see51.protocal.RFPackage;
import com.my51c.see51.widget.NewSwitch;
import com.my51c.see51.widget.PageControlView;
import com.my51c.see51.widget.ScrollLayout;
import com.my51c.see51.widget.ScrollLayout.OnScreenChangeListenerDataLoad;
import com.my51c.see51.widget.ToastCommom;
import com.sdview.view.R;

public class RfFragment extends Fragment implements OnItemClickListener{
	private View view;
	private GridView gridView;
	int w,h;
	private static ArrayList<String> rfStrTypeList = new ArrayList<String>();
	private ArrayList<Integer> rfImgList_off = new ArrayList<Integer>();
	private ArrayList<Integer> rfImgList_on = new ArrayList<Integer>();
	private String[] rfStr = {"01","02","03","04","10","21","22","23","a0","a1","11","12","13","a2"};
	private int[] rfImg_off= {R.drawable.shap_controller, R.drawable.shap_doorsensor, R.drawable.shap_pir,
			R.drawable.shap_smoke, R.drawable.shap_smartplug, R.drawable.shap_sound,
			R.drawable.shap_doorcamera, R.drawable.shap_io,R.drawable.shap_curtain,R.drawable.shap_lock,R.drawable.shap_rf_switch,R.drawable.shap_rf_switch2,
			R.drawable.shap_rf_switch3,R.drawable.shap_rf_light};
	private int[] rfImg_on = {R.drawable.grid_controller_pre, R.drawable.grid_doorsensor_pre, R.drawable.grid_pirsensor_pre,
			R.drawable.grid_smokesensor_pre, R.drawable.grid_smartplug_pre, R.drawable.grid_soundlight_pre,
			R.drawable.grid_doorcamera_pre, R.drawable.grid_iosensor_pre,R.drawable.grid_curtain_pre,R.drawable.grid_lock_pre
			,R.drawable.rf_switch_pre,R.drawable.rf_switch2_pre,R.drawable.rf_switch3_pre,R.drawable.rf_light_pre};
	
	private List<Map<String, Object>> rfList = new ArrayList<Map<String,Object>>();
	private RemoteInteractionStreamer mediastream;
	private static final int MSG_SET_SUCESS = 0; 
	private static final int MSG_SET_FAILED = 1;
	private static final int MSG_SET_TIMEOUT = 2;
	private static final int MSG_SET_CURTAIN_FAILED = 3;
	private static final int MSG_SET_CURTAIN_SUCCESS = 4;
	public static final String UPDATE_ACTION = "UPDATE_ACTION";
	private RFPackage curRfpack;
	private Dialog waitDialog;
	private TimeOutAsyncTask asyncTask;
	private int switchNum;
	private ToastCommom toast = new ToastCommom();
	private GridAdapter adapter;
	private LinearLayout mainLayout;
	
	private RFPackage rfpack;
	private ScrollLayout mScrollLayout;
	private static final float APP_PAGE_SIZE = 8.0f;
	private PageControlView pageControl;
	private int curPageIndex = 0;
	private GestureDetector gestDetector;
	
	public RfFragment(List<Map<String, Object>> rfList,RemoteInteractionStreamer mediastream,RFPackage rfpack){
		this.rfList = rfList;
		this.mediastream = mediastream;
		this.rfpack = rfpack;
	}
	
	public enum RFType{
		control(rfStrTypeList.get(0)),
		door(rfStrTypeList.get(1)),
		pir(rfStrTypeList.get(2)),
		smoke(rfStrTypeList.get(3)),
		plug(rfStrTypeList.get(4)),
		siren(rfStrTypeList.get(5)),
		door_camera(rfStrTypeList.get(6)),
		io(rfStrTypeList.get(7)),
		curtain(rfStrTypeList.get(8)),
		lock(rfStrTypeList.get(9)),
		switch1(rfStrTypeList.get(10)),
		switch2(rfStrTypeList.get(11)),
		switch3(rfStrTypeList.get(12)),
		light(rfStrTypeList.get(13));
    	String rfStr;
    	RFType(String rfStr){
    		this.rfStr = rfStr;
    	}
    	public String getrfStr(){
    		return this.rfStr;
    	}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		w = dm.widthPixels;
		h = dm.heightPixels;
		transRfData();
		view = inflater.inflate(R.layout.rf_fragment,null);
		mScrollLayout = (ScrollLayout)view.findViewById(R.id.ScrollLayoutTest);
		pageControl = (PageControlView)view.findViewById(R.id.pageControl);
		gestDetector = new GestureDetector(getActivity(), getstureListener);
		setGridData();
		setListener();
		return view;
	}
	public void setGridData(){
		int pageCount = (int)Math.ceil( rfList.size()/APP_PAGE_SIZE);
		for (int i = 0; i < pageCount; i++) {
			GridView gridView = new GridView(getActivity());
			adapter = new GridAdapter(getActivity(), rfList, i);
			gridView.setAdapter(adapter);
			gridView.setNumColumns(4);
			gridView.setHorizontalSpacing((int)getActivity().getResources().getDimension(R.dimen.horizontalSpacing));
			gridView.setVerticalSpacing((int)getActivity().getResources().getDimension(R.dimen.verticalSpacing));
			//gridView.setSelector(null);
			gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			gridView.setOnItemClickListener(this);
			mScrollLayout.addView(gridView);
		}
		pageControl.bindScrollViewGroup(mScrollLayout);
		mScrollLayout.setOnScreenChangeListenerDataLoad(new OnScreenChangeListenerDataLoad() {
			public void onScreenChange(int currentIndex) {
				// TODO Auto-generated method stub
			}
		});
		mScrollLayout.setOnScreenChangeListener(new ScrollLayout.OnScreenChangeListener() {
			
			@Override
			public void onScreenChange(int currentIndex) {
				// TODO Auto-generated method stub
				curPageIndex = currentIndex;
				mScrollLayout.setCurrentScreenIndex(currentIndex);
			}
		});
		mScrollLayout.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gestDetector.onTouchEvent(event);
				return false;
			}
		});
	}
	
	private SimpleOnGestureListener getstureListener = new SimpleOnGestureListener() {
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			
			if(distanceX<0 && curPageIndex==0){
				System.out.println("distanceX<0");
				Intent intent = new Intent(PlayerActivity.UI_ACTION);
				intent.putExtra("cmdType", "viewPager_scroll");
				getActivity().sendBroadcast(intent);
			}
			return false;
		}
	};

	public void setListener(){
		mediastream.setOnSetRFInfoListener(mOnSetRFInfoListener);
		mediastream.setOnSetCurtainInfoListener(mOnSetCurtainInfoListener);
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		// TODO Auto-generated method stub
		String curid = (String)rfList.get(position+(curPageIndex*8)).get("MY51CRFID");
		String rfStrType = curid.substring(0, 2);
		RFType rfType = null;
		for(RFType r : RFType.values()){
			if(r.getrfStr().equals(rfStrType))
				rfType = r;
		}
		switch (rfType) {
		case control:
		case door:
		case pir:
		case smoke:
		case plug:
		case siren:
		case door_camera:
		case io:
		case lock:
		case light:
			RFPackage rfpackclone = new RFPackage();
			rfpackclone.parseArrayList(rfpack.getRFDevList());
			String status = (String)rfList.get(position).get("status");
			if(!status.equals("on")){
				rfpackclone.setValue(curid, "status","on");
			}else{
				rfpackclone.setValue(curid, "status","off");
			}
			showWaitDialog(curid,rfpackclone);
			break;
		case curtain:
			showCurtainDialog(curid);
			break;
		case switch1:
		case switch2:
		case switch3:
			RFPackage rfpackclone1 = new RFPackage();
			rfpackclone1.parseArrayList(rfpack.getRFDevList());
			String switchStatus = (String)rfList.get(position).get("status");
			switchNum = Integer.parseInt(switchStatus);
			showSwitchDialog(curid,rfpackclone1);
			break;
		default:
			break;
		}
	}
	
	
	public void showCurtainDialog(final String curid){
		final Dialog curtainDialog = new Dialog(getActivity(), R.style.rf_control_dialog);
		curtainDialog.setContentView(R.layout.dialog_curtain);
		Window window = curtainDialog.getWindow();  
	    window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置  
	    window.setWindowAnimations(R.style.dialog_animation_style);  
	    /*设置dialog宽度占满屏幕*/
        WindowManager.LayoutParams lp = curtainDialog.getWindow().getAttributes();
	    lp.width = (int)(w); //设置宽度
	    curtainDialog.getWindow().setAttributes(lp);
	    curtainDialog.show();
	    
	    final ProgressBar openLoading = (ProgressBar)curtainDialog.findViewById(R.id.open_progress);
	    final ProgressBar closeLoading = (ProgressBar)curtainDialog.findViewById(R.id.close_progress);
	    final Button open = (Button)curtainDialog.findViewById(R.id.open);
	    final Button close = (Button)curtainDialog.findViewById(R.id.close);
	    final Button pause = (Button)curtainDialog.findViewById(R.id.pause);
	    final ImageView pauseImg = (ImageView)curtainDialog.findViewById(R.id.pauseImg);
	    open.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mediastream.setCurtainInfo(curid+"2001");
				openLoading.setVisibility(View.VISIBLE);
				closeLoading.setVisibility(View.GONE);
				pauseImg.setVisibility(View.GONE);
				open.setBackgroundResource(R.drawable.shap_curtain_btn_close);
				close.setBackgroundResource(R.drawable.shap_curtain_btn);
				pause.setBackgroundResource(R.drawable.shap_curtain_btn);
			}
		});
		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mediastream.setCurtainInfo(curid+"2003");
				openLoading.setVisibility(View.GONE);
				closeLoading.setVisibility(View.VISIBLE);
				pauseImg.setVisibility(View.GONE);
				close.setBackgroundResource(R.drawable.shap_curtain_btn_close);
				open.setBackgroundResource(R.drawable.shap_curtain_btn);
				pause.setBackgroundResource(R.drawable.shap_curtain_btn);// TODO Auto-generated method stub
				
			}
		});
		pause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mediastream.setCurtainInfo(curid+"2002");
				openLoading.setVisibility(View.GONE);
				closeLoading.setVisibility(View.GONE);
				pauseImg.setVisibility(View.VISIBLE);
				pause.setBackgroundResource(R.drawable.shap_curtain_btn_close);
				open.setBackgroundResource(R.drawable.shap_curtain_btn);
				close.setBackgroundResource(R.drawable.shap_curtain_btn);
			}
		});
	}
	
	public void showSwitchDialog(final String curid,final RFPackage switchPack){
		final Dialog switchDialog = new Dialog(getActivity(), R.style.rf_control_dialog);
		switchDialog.setContentView(R.layout.dialog_switch);
		Window window = switchDialog.getWindow();  
	    window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置  
	    window.setWindowAnimations(R.style.dialog_animation_style);  
	    /*设置dialog宽度占满屏幕*/
        WindowManager.LayoutParams lp = switchDialog.getWindow().getAttributes();
	    lp.width = (int)(w); //设置宽度
	    switchDialog.getWindow().setAttributes(lp);
	    switchDialog.show();
	    
	    NewSwitch mSwitch1 = (NewSwitch)switchDialog.findViewById(R.id.mSwitch1);
	    NewSwitch mSwitch2 = (NewSwitch)switchDialog.findViewById(R.id.mSwitch2);
	    NewSwitch mSwitch3 = (NewSwitch)switchDialog.findViewById(R.id.mSwitch3);
	    LinearLayout switchGroup1 = (LinearLayout)switchDialog.findViewById(R.id.swtichGroup1);
	    LinearLayout switchGroup2 = (LinearLayout)switchDialog.findViewById(R.id.swtichGroup2);
	    LinearLayout switchGroup3 = (LinearLayout)switchDialog.findViewById(R.id.swtichGroup3);
	    LinearLayout mSwitchL1 = (LinearLayout)switchDialog.findViewById(R.id.mSwitchL1);
	    LinearLayout mSwitchL2 = (LinearLayout)switchDialog.findViewById(R.id.mSwitchL2);
	    LinearLayout mSwitchL3 = (LinearLayout)switchDialog.findViewById(R.id.mSwitchL3);
	    String idType = curid.substring(0, 2);
	    if(idType.equals("12")){
			switchGroup2.setVisibility(View.VISIBLE);
			switchGroup3.setVisibility(View.GONE);
    	}
    	if(idType.equals("13")){
    		switchGroup2.setVisibility(View.VISIBLE);
    		switchGroup3.setVisibility(View.VISIBLE);
    	}
    	if(idType.equals("11")){
    		switchGroup2.setVisibility(View.GONE);
    		switchGroup3.setVisibility(View.GONE);
    	}
    	boolean switch1 = ((switchNum & 0x01)==1);
    	boolean switch2 = (((switchNum & 0x02) >>1) ==1);
    	boolean switch3 = (((switchNum & 0x04) >>2)==1);
    	mSwitch1.setChecked(switch1);
    	mSwitch2.setChecked(switch2);
    	mSwitch3.setChecked(switch3);
    	
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
				switchPack.setValue(curid, "status",str);
				showWaitDialog(curid,switchPack);
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
				switchPack.setValue(curid, "status",str);
				showWaitDialog(curid,switchPack);
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
				switchPack.setValue(curid, "status",str);
				showWaitDialog(curid,switchPack);
			}
		});
	}
	
	public void showWaitDialog(String curid,RFPackage mRfPack){
    	waitDialog = new Dialog(getActivity(), R.style.rf_control_dialog);
    	waitDialog.setContentView(R.layout.login_dialog);
    	TextView dialogTx = (TextView)waitDialog.findViewById(R.id.tx);
    	dialogTx.setText(getActivity().getResources().getString(R.string.rf_setting));
    	waitDialog.show();
    	asyncTask = new TimeOutAsyncTask(curid,mRfPack);//请求数据任务
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
	
	private class TimeOutAsyncTask extends AsyncTask<Integer, Integer, String>{
		
	 	private String curid;
	 	private RFPackage mRfPack;
	 	public TimeOutAsyncTask(String curid,RFPackage mRfPack){
	 		this.curid = curid;
	 		this.mRfPack = mRfPack;
	 	}
	 	@Override  
        protected void onPreExecute() {  
            super.onPreExecute();  
            curRfpack = new RFPackage();
            curRfpack.parseArrayList(mRfPack.getRFDevList());
            mediastream.sendRFDevInfo(curRfpack,curid);//请求数据
        } 
		        	
		@Override
		protected String doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
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
	
	private OnSetRFInfoListener mOnSetRFInfoListener = new OnSetRFInfoListener()
	{

		@Override
		public void onSetRFInfoFailed() {
			// TODO Auto-generated method stub
			mHandler.sendEmptyMessage(MSG_SET_FAILED);	
		}

		@Override
		public void onSetRFInfoSuccess() {
			if(curRfpack==null){
				System.out.println("onSetRFInfoSuccess:curRfpack==null");
			}else{
				mHandler.sendEmptyMessage(MSG_SET_SUCESS);
			}
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
			switch (msg.what) {
			case MSG_SET_SUCESS:
				if(waitDialog!=null){
					waitDialog.dismiss();
				}
				rfpack = curRfpack;
				if(RFDeviceInfoActivity.mDevice!=null){
					RFDeviceInfoActivity.mDevice.setRFInfo(rfpack);
				}
				List<Map<String, Object>> newRfList = null;
				newRfList = rfpack.getRFDevList();
				for(int i=-0;i<rfList.size();i++)
				{
					String rfid = (String)rfList.get(i).get("MY51CRFID");
					for(int n=0;n<newRfList.size();n++)
					{
						String newRfid = (String)newRfList.get(n).get("MY51CRFID");
						Map<String, Object> map = newRfList.get(n);
						if(newRfid.equals(rfid))
							rfList.set(i, map);
					}
				}
				adapter.notifyDataSetChanged();
            	toast.ToastShow(getActivity(), getActivity().getResources().getString(R.string.rfsettingsuccess), 1000);
	        	asyncTask.cancel(true);	
				break;
			case MSG_SET_FAILED:
				if(waitDialog!=null){
					waitDialog.dismiss();
				}
				toast.ToastShow(getActivity(), getActivity().getResources().getString(R.string.rfsettingfail), 1000);
	        	asyncTask.cancel(true);
				break;
			case MSG_SET_TIMEOUT:
				if(waitDialog!=null){
					waitDialog.dismiss();
				}
				toast.ToastShow(getActivity(), getActivity().getResources().getString(R.string.rfsettingfail), 1000);
				break;
			case MSG_SET_CURTAIN_FAILED:
				toast.ToastShow(getActivity(), getActivity().getResources().getString(R.string.rfsettingfail), 1000);
				break;
			case MSG_SET_CURTAIN_SUCCESS:
				toast.ToastShow(getActivity(), getActivity().getResources().getString(R.string.rfsettingsuccess), 1000);
				break;

			default:
				break;
			}
		}
	 }; 
	 
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
	
	@Override
	public void onDestroyView() {
		if(asyncTask!=null&&!asyncTask.isCancelled()){
			asyncTask.cancel(true);
		}
		super.onDestroyView();
	}

	private class GridAdapter extends BaseAdapter{

    	private List<Map<String, Object>> mList;
    	private Context context;
    	public static final int APP_PAGE_SIZE = 8;
    	public GridAdapter(Context context,List<Map<String, Object>> list,int page)
    	{
    		this.context = context;
    		mList = new ArrayList<Map<String,Object>>();
    		int i = page * APP_PAGE_SIZE;
    		int iEnd = i+APP_PAGE_SIZE;
    		while ((i<list.size()) && (i<iEnd)) {
    			mList.add(list.get(i));
    			i++;
    		}
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
			convertView.setLayoutParams(new LayoutParams((w-20)/4, LayoutParams.WRAP_CONTENT));
			ImageView rfImg = (ImageView)convertView.findViewById(R.id.rfitemImage);
			TextView rfTx = (TextView) convertView.findViewById(R.id.rfitemText);
			
			HashMap<String, Object> map = (HashMap<String, Object>)mList.get(position);
			String strID = (String)map.get("MY51CRFID");
			String status = (String)map.get("status");
			String strType = strID.substring(0,2);
			
			rfTx.setText(strID);
			if(status.equals("on")){
				rfImg.setBackgroundResource(rfImgList_on.get(rfStrTypeList.indexOf(strType)));
			}else{
				rfImg.setBackgroundResource(rfImgList_off.get(rfStrTypeList.indexOf(strType)));
			}
			return convertView;
		}
    }
}
