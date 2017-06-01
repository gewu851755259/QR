package com.my51c.see51.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.sdview.view.R;
import com.sdview.view.R.color;
import com.my51c.see51.adapter.SetListAdapter;
import com.my51c.see51.widget.MySwitch;
import com.my51c.see51.widget.NewSwitch;

public class SetFragment extends SherlockFragment 
{
	private final String TAG = "SetActivity" ;
	private SetListAdapter adapter;
	private ArrayList<HashMap<String, String>> map_SetList = null;
	private ListView setListView;
	
	private Button btnProgramExit;
	
	public static TextView userTx ;
	private ImageView imgUser;
	
	
	
	private Button btnLogin;
	
	private LinearLayout btnNewCamera;
	private LinearLayout btnMyCameraList;
	private LinearLayout btnLocalList;
	private TextView txLocalList;
	private LinearLayout showWarn, warnMsg, changePsw, about,localVideo;
	private LinearLayout loginLayout;
	private TextView loginLayoutTx;
	private ImageView loginLayoutImg;
	private NewSwitch warnSwitch;
	
	
	public void setIsLoginBtnName(boolean isLogin) {
		try {
			if (isLogin) {
				loginLayoutTx.setText(getString(R.string.logout));
				loginLayoutImg.setBackgroundResource(R.drawable.exit_img);
				btnLogin.setText(R.string.logout);
//			userTx.setTextColor(color.white);//ʧЧ
				userTx.setTextColor(getResources().getColor(R.color.menu_tx_color));
			}else {
				loginLayoutTx.setText(getString(R.string._clicktologin));
				userTx.setTextColor(getResources().getColor(R.color.menu_tx_color));
				loginLayoutImg.setBackgroundResource(R.drawable.gointo_img);
				btnLogin.setText(R.string.login);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override 
	public void onCreate(Bundle arg0)
	{
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		//Log.d(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		//Log.d(TAG, "onCreateView");
		View v = inflater.inflate(R.layout.set_frame, container,
				false);
		Intent intent=getActivity().getIntent();
		
		map_SetList = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> mapAlarm = new HashMap<String, String>();
		String alarmTitle = getResources().getString(R.string.alarmNotification);
		mapAlarm.put("item",alarmTitle);
		map_SetList.add(mapAlarm);
		HashMap<String, String> mapHistory = new HashMap<String, String>();
		String historyTitle = getResources().getString(R.string.alarmNotification);
		mapHistory.put("item",historyTitle);
		map_SetList.add(mapHistory);
		
		setListView = (ListView) v.findViewById(R.id.setListView);
		btnNewCamera = (LinearLayout) v.findViewById(R.id.btnNewCamera);
		btnProgramExit = (Button) v.findViewById(R.id.btnProgramExit);
		btnLocalList = (LinearLayout) v.findViewById(R.id.btnLocalCamera);
		btnMyCameraList = (LinearLayout) v.findViewById(R.id.btnMyCamera);
		btnLogin = (Button) v.findViewById(R.id.btnLogin);
		txLocalList = (TextView)v.findViewById(R.id.txLocalList);
		txLocalList.setText(R.string.landev);
		
		
		userTx = (TextView) v.findViewById(R.id.userTx);
		imgUser = (ImageView) v.findViewById(R.id.imgUser);
		showWarn = (LinearLayout)v.findViewById(R.id.showWarn);
		warnMsg = (LinearLayout)v.findViewById(R.id.warnMsg);
		changePsw = (LinearLayout)v.findViewById(R.id.changePsw);
		about = (LinearLayout)v.findViewById(R.id.about);
		localVideo = (LinearLayout)v.findViewById(R.id.local_video);
		loginLayout = (LinearLayout)v.findViewById(R.id.loginLayout);
		loginLayoutTx = (TextView)v.findViewById(R.id.loginLayoutTx);
		loginLayoutImg = (ImageView)v.findViewById(R.id.loginLayoutImg);
		warnSwitch = (NewSwitch)v.findViewById(R.id.showWarnSwitch);
		warnSwitch.setChecked(false);
		
		
		showWarn.setOnClickListener((MainActivity)getActivity());
		warnMsg.setOnClickListener((MainActivity)getActivity());
		changePsw.setOnClickListener((MainActivity)getActivity());
		about.setOnClickListener((MainActivity)getActivity());
		localVideo.setOnClickListener((MainActivity)getActivity());
		loginLayout.setOnClickListener((MainActivity)getActivity());
		warnSwitch.setOnCheckedChangeListener((MainActivity)getActivity());
		
		
		
		btnNewCamera.setOnClickListener((MainActivity)getActivity());
		btnProgramExit.setOnClickListener((MainActivity)getActivity());
		btnLocalList.setOnClickListener((MainActivity)getActivity());
		btnMyCameraList.setOnClickListener((MainActivity)getActivity());
		btnLogin.setOnClickListener((MainActivity)getActivity());
//		int num=intent.getIntExtra("flag",0);
//		if(num==1){
//			btnMyCameraList.performClick();
//			setIsLoginBtnName(MainActivity.isLogin);
//		}
		setIsLoginBtnName(MainActivity.isLogin);
		return v;
	}
	@Override
	public void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		//Log.d(TAG, "onResume");
		SharedPreferences sp = getActivity().getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
		boolean isswich = sp.getBoolean("alarmNotification", false);
		warnSwitch.setChecked(isswich);
		adapter = new SetListAdapter(getActivity(),(MainActivity)getActivity());
		MainActivity mainactivity = (MainActivity)getActivity();
		setListView.setOnItemClickListener(mainactivity);	
		
		setIsLoginBtnName(mainactivity.isLogin);
		setListView.setAdapter(adapter);
	}
	
	public void setLoginState(String user)
	{
		userTx.setText(user);
		userTx.setTextColor(getResources().getColor(R.color.menu_tx_color));
		userTx.invalidate();
		if(user.equals(getString(R.string.notLogedIn)))
			imgUser.setBackgroundResource(R.drawable.vcard);
		else
			imgUser.setBackgroundResource(R.drawable.vcard);
	}

	public void onSaveInstanceState(Bundle outState) {  
        super.onSaveInstanceState(outState);  
        setUserVisibleHint(true);
	}
}
