 package com.my51c.see51.ui;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.sdview.view.R;
import com.my51c.see51.adapter.DeviceListAdapter;
import com.my51c.see51.adapter.DeviceListAdapter.ViewHolder;
import com.my51c.see51.data.AccountInfo;
import com.my51c.see51.data.Device;
import com.my51c.see51.data.DeviceList;
import com.my51c.see51.data.Group;
import com.my51c.see51.guide.PlatformActivity;
import com.my51c.see51.listener.DeviceListListener;
import com.my51c.see51.protocal.GvapCommand;
import com.my51c.see51.protocal.GvapPackage;
import com.my51c.see51.protocal.GvapXmlParser;
import com.my51c.see51.service.AppData;
import com.my51c.see51.service.GVAPService;
import com.my51c.see51.service.GvapEvent;
import com.my51c.see51.service.GvapEvent.GvapEventListener;
import com.my51c.see51.service.GvapServer;
import com.my51c.see51.widget.DeviceListView;
import com.my51c.see51.widget.DeviceListView.OnRefreshListener;

public class FavoriteFragment extends ListFragment implements DeviceListListener,
		OnClickListener, OnRefreshListener,GvapEventListener
{

	private static String TAG = "FavoriteFragment";
	private DeviceListView listView;
	static DeviceListAdapter adapter;
	private AppData appData;
	private DeviceList myList;

	public static View progressView;
	public static View waitTextView;
	public static View emptyView;

	static final int MSG_UPDATE = 0;

	static final int MSG_ClEAR_PROGRESSBAR = 1;

	private Timer timer;
	private TimerTask timerTask;
	
	private GVAPService gvapService = null;
	public static ImageView refreshImg;
	private Group parent_group = null;
	View v;
	private String manBinDevId;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//Log.d("FavoriteActivity", "onCreate");
		setHasOptionsMenu(true);
	
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		//Log.d("FavoriteActivity", "onActivityCreated");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		
		if(v!=null){
			ViewGroup parent = (ViewGroup) v.getParent();
			if (null != parent) {
				parent.removeView(v);
			}
		}else{
			Log.i("FavoriteActivity", "onCreateView");
			v = inflater.inflate(R.layout.layout_device_list, container, false);
			listView = (DeviceListView) v.findViewById(android.R.id.list);
			listView.setItemsCanFocus(true);
			listView.setonRefreshListener(this);

			progressView = v.findViewById(R.id.progress_get_devices_image);
			waitTextView = v.findViewById(R.id.loading);
			emptyView = v.findViewById(R.id.emptyView);
			
			refreshImg = (ImageView) v.findViewById(R.id.refreshImg);
			refreshImg.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					refreshDevice();
				}
			});
//			new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
					gvapService = new GVAPService();
					gvapService.init();
					gvapService.start();
//				}
//			}).start();
			
			gvapService.bNetStatus = true;
			
			
			if(MainActivity.isLoginClicked){
				mHandler.sendEmptyMessageDelayed(7,5000);  
				MainActivity.isLoginClicked = false;
			}
			
			appData = (AppData) getActivity().getApplication();

			timer = new Timer();
			timerTask = new TimerTask()
			{
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					mHandler.sendEmptyMessage(MSG_ClEAR_PROGRESSBAR);
				} 
			};
			myList = appData.getAccountInfo().getCurrentList();
//			SharedPreferences sp = getActivity().getSharedPreferences("config",Context.MODE_PRIVATE);
//			manBinDevId=sp.getString("manBindId","").toString();
////			new Thread(new Runnable() {
////				
////				@Override
////				public void run() {
////					gvapService.manBind(appData.getAccountInfo(),"1",manBinDevId);	
//					gvapService.bind(appData.getAccountInfo(), manBinDevId);
//				}
//			}).start();
			
			if (myList.getTotalCount() == 0)
			{
				progressView.setVisibility(View.VISIBLE);
				waitTextView.setVisibility(View.VISIBLE);
				emptyView.setVisibility(View.VISIBLE);
				timer.schedule(timerTask, 5000);  
			}
			else 
			{
				progressView.setVisibility(View.INVISIBLE);
				waitTextView.setVisibility(View.INVISIBLE);	
				emptyView.setVisibility(View.INVISIBLE);
			}
			try {
				adapter = new DeviceListAdapter(getActivity(), myList, this);
			} catch (OutOfMemoryError e) {
				// TODO Auto-generated catch block
				Log.i(TAG, "-"+e);
			}
			myList.addListListener(this);
			setListAdapter(adapter);

		}
		return v;
	}

	
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
	}

	@Override
	public void onResume()
	{
		super.onResume();
//		onRefresh();
		
		try {
			adapter = new DeviceListAdapter(getActivity(), appData.getAccountInfo().getCurrentList(), this);
		} catch (OutOfMemoryError e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "-"+e);
		}
		myList.addListListener(this);
		setListAdapter(adapter);	
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		timerTask.cancel();
		timer.cancel();
		myList.removeListener(this);
	}

	@Override
	    public void setUserVisibleHint(boolean isVisibleToUser) {
	        super.setUserVisibleHint(isVisibleToUser);
	        if (isVisibleToUser) {
	            this.onRefresh();
	    }
	}

	@Override
	public void onRefresh()
	{
		 
		if(MainActivity.canRefresh)
		{
			emptyView.setVisibility(View.GONE);
			refreshDevice();
			MainActivity.canRefresh = false;
			new Timer().schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					MainActivity.canRefresh = true;
					Log.i(TAG, "refreshTimer:"+MainActivity.canRefresh);
				}
			}, 2000);
		}else{
			Log.i(TAG,"-------------hold on 5s--------------");
		}
	}

	public void refreshDevice()
	{
		MainActivity.fromFav = true;
		
		if (!appData.getAccountInfo().isLogined()
				|| appData.getAccountInfo().isGuest())
		{
			Log.i(TAG, "--hasn't login");
			return;
		}
		mHandler.sendEmptyMessageDelayed(MSG_ClEAR_PROGRESSBAR, 15000);

			/*update start*/
			DeviceList currentList = appData.getAccountInfo().getCurrentList();
			parent_group = currentList.getParent_group();
			if (parent_group != null)							//��ȡ������Ϣ
			{
				String groupId = parent_group.getGroupID();
				appData.getGVAPService().getDeviceList(groupId);
				Log.i(TAG, "get parent_group:"+groupId);
			} else												//��ȡ��Ŀ¼��Ϣ
			{
				appData.getGVAPService().getDeviceList();			//���ͻ�ȡ�豸�б�ָ��
			}
			
			/*update end*/
	}

	@Override
	public void onListUpdate()
	{
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessage(MSG_UPDATE);
		if (myList.getDeviceCount() > 0)
		{
			timer.cancel();
		} else
		{
			timerTask.cancel();
			timer.cancel();
			timer = new Timer();
			timerTask = new TimerTask()
			{

				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					mHandler.sendEmptyMessage(MSG_ClEAR_PROGRESSBAR);
				}
			};
			timer.schedule(timerTask, 8000);
		}
	}
	
	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case MSG_UPDATE:
				adapter.updateDeviceData();
				adapter.notifyDataSetChanged();
				progressView.setVisibility(View.INVISIBLE);
				waitTextView.setVisibility(View.INVISIBLE);
				emptyView.setVisibility(View.INVISIBLE);
				break;
			case MSG_ClEAR_PROGRESSBAR:
				progressView.setVisibility(View.INVISIBLE);
				waitTextView.setVisibility(View.INVISIBLE);
				emptyView.setVisibility(View.INVISIBLE);
				break;
			case 7:
				try {
					if(appData.getAccountInfo().getDevList().getTotalCount()==0)
					{
						Log.i(TAG, "--�״ε�½δ��ʾˢ��");
						refreshDevice();
					}
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		//Log.i("FragmentList", "Item clicked: id " + id);
		try {
			String Gro_Dev = ((ViewHolder) v.getTag()).Gro_Dev;
			String Status = (String) ((ViewHolder) v.getTag()).title.getTag();
			if (Gro_Dev.equals("device")&&Status.equals(ViewHolder.GET_SUCCESS))
			{
				Device dev = (Device) ((ViewHolder) v.getTag()).info.getTag();
				if (dev != null && dev.getPlayURL()!= null)
				{
					Intent intent = new Intent(this.getActivity(),PlayerActivity.class);
					intent.putExtra("id", dev.getID());
					intent.putExtra("url", dev.getPlayURL());
					intent.putExtra("title",((ViewHolder) v.getTag()).title.getText());
					intent.putExtra("version", dev.getSee51Info().getHwVersion() + " / " + dev.getSee51Info().getSwVersion());
					intent.putExtra("name", dev.getSee51Info().getDeviceName());
					intent.putExtra("isLocal", false);
					startActivity(intent);
				}
			} else if (Gro_Dev.equals("group"))
			{
				showSubList(v);
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		String Gro_Dev = ((ViewHolder) v.getTag()).Gro_Dev;
		if (Gro_Dev.equals("device"))
		{
			Device dev = (Device) ((ViewHolder) v.getTag()).info.getTag();
			Intent intent = new Intent(this.getActivity(), DeviceInfoActivity.class);
			intent.putExtra("id", dev.getID());
			intent.putExtra("version", dev.getSee51Info().getHwVersion() + " / " + dev.getSee51Info().getSwVersion());
			intent.putExtra("name", dev.getSee51Info().getDeviceName());
			intent.putExtra("isLocal", false);
			startActivity(intent);
			this.getActivity().overridePendingTransition(R.anim.slide_out_left , R.anim.slide_in_right);
		}else if (Gro_Dev.equals("group"))
		{
			showSubList(v);
		}
	}

	public void showSubList(View v)
	{
		MainActivity.menuBtn.setVisibility(View.GONE);
		MainActivity.menuBack.setVisibility(View.VISIBLE);
		Group parent_group = ((ViewHolder) v.getTag()).group;
		String groupId = parent_group.getGroupID();
		String grandParent_group = ((ViewHolder) v.getTag()).grandParent_group;
		if (appData.getAccountInfo().getDevList(groupId) == null)
		{
			appData.getAccountInfo().addList(parent_group,
					grandParent_group);
			appData.getGVAPService().getDeviceList(groupId);
		}
		DeviceList devList = appData.getAccountInfo().getDevList(groupId);
		appData.getAccountInfo().setCurrentList(devList);
		myList = appData.getAccountInfo().getCurrentList();
		myList.addListListener(this);
		adapter.setDeviceList(myList);
		myList.listUpdated();
		MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.displayHomeButton(true);
	}
	
	static public class GvapDeviceInfoDialogFragment extends
			SherlockDialogFragment
	{
		private EditText name;
		private EditText remark;

		private TextView id;
		private TextView softversion;
		private TextView hardversion;
		private Button btnUnbind;
		private AppData appData;

		private GvapEventListener gl;
		private ProgressDialog pd;
		private String mID;
		static Device device;

		Handler mHandler;

		/*
		 * static class GvapHandler extends Handler{
		 * GvapHandler(GvapDeviceInfoDialogFragment fm){ new
		 * WeakReference<GvapDeviceInfoDialogFragment>(fm); //�����������ڻ����ڴ� } }
		 */

		public static GvapDeviceInfoDialogFragment newInstance(
				Device devResponse)
		{
			GvapDeviceInfoDialogFragment fm = new GvapDeviceInfoDialogFragment();
			Bundle args = new Bundle();
			device = devResponse;
			fm.setArguments(args);
			return fm;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)
		{
			getDialog().setTitle(R.string.deviceinfomation);
			View v = inflater.inflate(R.layout.gvap_device_info, container,
					false);
			name = (EditText) v.findViewById(R.id.GvapDeviceName);
			remark = (EditText) v.findViewById(R.id.GvapDeviceRemark);
			id = (TextView) v.findViewById(R.id.serialNumberGvap);
			softversion = (TextView) v.findViewById(R.id.softwareversion);
			hardversion = (TextView) v.findViewById(R.id.hardwareversion);
			btnUnbind = (Button) v.findViewById(R.id.btnunBind);

			return v;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onActivityCreated(savedInstanceState);
			mID = device.getID();

			name.setText(device.getSee51Info().getDeviceName());
			id.setText(device.getID());
			softversion.setText(device.getSee51Info().getSwVersion());
			hardversion.setText(device.getSee51Info().getHwVersion());

			mHandler = new Handler()
			{
				@Override
				public void handleMessage(Message msg)
				{
					GvapCommand cmd = (GvapCommand) msg.obj;
					switch (msg.what)
					{
					case 0:
						if (cmd.equals(GvapCommand.CMD_UNBIND))
						{
							pd.cancel();
							Toast.makeText(getActivity(),
									getString(R.string.unbindSuccess),
									Toast.LENGTH_LONG).show();
							appData.getAccountInfo().getDevList().clear();
							appData.getGVAPService().getDeviceList();
						}
						break;
					case 1:
						if (cmd.equals(GvapCommand.CMD_UNBIND))
						{
							pd.cancel();
							Toast.makeText(getActivity(),
									getString(R.string.unbindFailed),
									Toast.LENGTH_LONG).show();
						}
						break;
					default:
						break;
					}
					try
					{
						GvapDeviceInfoDialogFragment.this.getDialog().dismiss();
					} catch (NullPointerException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};

			btnUnbind.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					// TODO Auto-generated method stub
					gl = new GvapEventListener()
					{

						@Override
						public void onGvapEvent(GvapEvent event)
						{
							// TODO Auto-generated method stub
							switch (event)
							{
							case OPERATION_SUCCESS:
							{
								if (event.getCommandID() == GvapCommand.CMD_UNBIND)
								{
									Message msg = mHandler.obtainMessage(0,
											event.getCommandID());
									mHandler.sendMessage(msg);
								}
								break;
							}
							case OPERATION_TIMEOUT:
							case OPERATION_FAILED:
							case CONNECTION_RESET:
							case CONNECT_TIMEOUT:
							case CONNECT_FAILED:
							case NETWORK_ERROR:
							{
								if (event.getCommandID() == GvapCommand.CMD_UNBIND)
								{
									Message msg = mHandler.obtainMessage(1,event.getCommandID());
									mHandler.sendMessage(msg);
								}
							}
								break;
							}
						}
					};

					appData.getGVAPService().removeGvapEventListener(gl);
					appData.getGVAPService().restartRegServer(); // ��������ע���������socket
					appData.getGVAPService().addGvapEventListener(gl);
					appData.getGVAPService().unbind(
							appData.getAccountInfo(),
							appData.getAccountInfo().getDevList()
									.getDevice(mID));

					pd = ProgressDialog.show(getActivity(), "",
							getString(R.string.unbinding));
					pd.setCancelable(true);
				}
			});

		}

		@Override
		public void onAttach(Activity activity)
		{
			super.onAttach(activity);
			appData = (AppData) activity.getApplication();
		}

		@Override
		public void onStop()
		{
			super.onStop();
			if (gl != null)
			{
				appData.getGVAPService().removeGvapEventListener(gl);
			}
		}
	}

	@Override
	public void onGvapEvent(GvapEvent event) {
		
		switch (event) {
		case OPERATION_SUCCESS://成功
			onOperationSuccess(event.getCommandID(), (GvapPackage) event.attach());
			break;

		case OPERATION_FAILED://失败
			System.out.println("失败");
			break;
		case CONNECT_TIMEOUT:
			Log.i(TAG, "--连接服务器超时");
			Log.i(TAG, "--PlatformActivity操作超时--CONNECT_TIMEOUT");
			break;
		case OPERATION_TIMEOUT: 
			Log.i(TAG, "--操作超时，一般是网络原因");
			Log.i(TAG, "--PlatformActivity操作超时--OPERATION_TIMEOUT");
			break;
		case CONNECT_FAILED:
			Log.i(TAG, "--连接服务器失败");
			Log.i(TAG, "--PlatformActivity操作超时--CONNECT_FAILED");
			break;
		case NETWORK_ERROR:
		{
			Log.i(TAG, "--网络错误");
			Log.i(TAG, "--PlatformActivity操作超时--NETWORK_ERROR");
			break;
		}
		}
	}
	
	private void onOperationSuccess(GvapCommand cmd, GvapPackage response){
		switch (cmd) {
		case CMD_GET_DEVSTATUS:
			
			break;

		default:
			break;
		}
	}
}
