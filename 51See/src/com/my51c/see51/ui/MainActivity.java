package com.my51c.see51.ui;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.method.wifilist.WifiAdmin;
import com.method.wifilist.WifiConstant;
import com.my51c.see51.adapter.SetListAdapter;
import com.my51c.see51.data.AccountInfo;
import com.my51c.see51.data.AlarmInfo;
import com.my51c.see51.data.Device;
import com.my51c.see51.data.DeviceList;
import com.my51c.see51.data.DeviceSee51Info;
import com.my51c.see51.data.Group;
import com.my51c.see51.data.GvapDBAdapter;
import com.my51c.see51.guide.DeviceIdActivity;
import com.my51c.see51.guide.GlassListActivity;
import com.my51c.see51.guide.GuidSmartId;
import com.my51c.see51.guide.Guide;
import com.my51c.see51.guide.ScanCodeActivity;
import com.my51c.see51.listener.OnRegisterSucessListener;
import com.my51c.see51.map.CameraLocation;
import com.my51c.see51.protocal.GvapCommand;
import com.my51c.see51.protocal.GvapPackage;
import com.my51c.see51.protocal.GvapXmlParser;
import com.my51c.see51.service.AppData;
import com.my51c.see51.service.GvapEvent;
import com.my51c.see51.service.GvapEvent.GvapEventListener;
import com.my51c.see51.ui.ChangeUserPasswdActivity.OnChangePasswdListener;
import com.my51c.see51.ui.LoginFragment.OnLoginListener;
import com.my51c.see51.ui.MyDialogFragmentActivity.MyAlertDialogFragment.OnRegisterListener;
import com.sdview.view.R;

public class MainActivity extends SlidingFragmentActivity implements
		OnLoginListener, GvapEventListener, OnRegisterListener,
		OnItemClickListener, OnCheckedChangeListener, OnChangePasswdListener,
		android.view.View.OnClickListener {

	public static final int DIALOG_ID_LOGIN = 0;
	public static final int DIALOG_ID_REGISTER = 1;
	public static final String PREFS_NAME = "MyPrefsFile";
	public static final String FIRST_RUN = "first";
	public static final String ALARM_NOTIFICATION = "alarmNotification";
	private String TAG = "MainActivity";
	public static boolean isLocalList = false;
	private AppData appData;
	public static AccountInfo account;
	private GvapDBAdapter dbAdapter;
	// private Class<FavoriteDeviceFragment> backParentListener;
	private NotificationManager notificationManager;
	private SharedPreferences preferences;
	private boolean displayHome;
	public static Boolean isLogin = false;
	private Boolean isAlarmNotification;
	ActionBar actionBar;
	private SlidingMenu menu;
	private Fragment mContent;
	private SetFragment setFragment;
	private OnRegisterSucessListener onRegisterSucessListener;
	private Thread reLoginThread = null;
	private boolean bReLoginFlag = true;// no use now, by marshal
	private int tryLoginTimes = 0;
	IntentFilter intentFilter = new IntentFilter();
	private boolean bNetConnnect = false;
	public static Button menuBtn;
	public static Button menuBack;
	public static SlidingMenu sm;
	public Dialog mDialog;
	public static int openNum = 0;
	public static boolean canRefresh = true;
	public static boolean FromPlatAcy = false;
	public static boolean fromFav = false;
	public static boolean isLoginClicked = false;
	private Button mapBtn;
	private boolean isMapMode = false;
	private boolean isMapShowed = false;
	private TextView conentTitle;
	private GestureDetector gestDetector;
	private boolean isFinish = false;
	private boolean isDestory = false;
	private int code;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_frame);
		isDestory = false;
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(connectionReceiver, intentFilter);
		Intent intent = getIntent();
		// code=intent.getIntExtra("code", 0);
		menuBtn = (Button) findViewById(R.id.menuBtn);
		menuBack = (Button) findViewById(R.id.menuBack);
		mapBtn = (Button) findViewById(R.id.mapBtn);
		conentTitle = (TextView) findViewById(R.id.content_title);
		gestDetector = new GestureDetector(this, new SingleGestureListener());
		menuBtn.setOnClickListener(this);
		menuBack.setOnClickListener(this);
		mapBtn.setOnClickListener(this);

		if (findViewById(R.id.menu_frame) == null) {
			setBehindContentView(R.layout.menu_frame);
			getSlidingMenu().setSlidingEnabled(true);
			getSlidingMenu()
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		} else {
			View v = new View(this);
			setBehindContentView(v);
			getSlidingMenu().setSlidingEnabled(false);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}

		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(
					savedInstanceState, "mContent");
		if (mContent == null)
			mContent = new LoginFragment();
		if (setFragment == null)
			// 侧滑页面fragment
			setFragment = new SetFragment();
		// 填充到content_frame布局中
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, mContent).commit();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, setFragment).commit();
		sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);

		sm.setBackgroundResource(R.drawable.slide_menu_bg);
		// ����
		/*
		 * sm.setBehindCanvasTransformer(new SlidingMenu.CanvasTransformer() {
		 * 
		 * @Override public void transformCanvas(Canvas canvas, float
		 * percentOpen) { float scale = (float) (percentOpen * 0.25 + 0.75);
		 * canvas.scale(scale, scale, -canvas.getWidth() / 2, canvas.getHeight()
		 * / 2); } });
		 * 
		 * sm.setAboveCanvasTransformer(new SlidingMenu.CanvasTransformer() {
		 * 
		 * @Override public void transformCanvas(Canvas canvas, float
		 * percentOpen) { float scale = (float) (1 - percentOpen * 0.25);
		 * canvas.scale(scale, scale, 0, canvas.getHeight() / 2); } });
		 */

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		isAlarmNotification = settings.getBoolean(ALARM_NOTIFICATION, false);
		// ��ʼ��appdata
		appData = (AppData) getApplication();
		if (!appData.getGVAPService().isRunning()) {
			if (!appData.init()) {

			}
		}
		appData.getGVAPService().addGvapEventListener(this);
		appData.setMainActivity(this);

		notificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add("menu").setIcon(R.drawable.menu)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}

	private final BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo mobNetInfo = connectMgr
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo wifiNetInfo = connectMgr
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
				// //Log.i("TestConect", "unconnect");
				setNetStatus(false);

			} else {
				// //Log.i("TestConect", "connect");
				setNetStatus(true);
			}
		}
	};

	public void setOnRegisterSucessListener(OnRegisterSucessListener listener) {
		onRegisterSucessListener = listener;
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			GvapCommand cmd = (GvapCommand) msg.obj;
			switch (msg.what) {
			case 0:
				handleSucess(cmd);
				break;
			case 1:
				handleFailed(cmd);
				break;
			case 2:
				if (isRootDirectory()) {
					moveTaskToBack(false);
				} else {
					try {
						backParent();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			case 3:
				// waitConnect.dismiss();
				showAddDialog();
				break;
			default:
				break;
			}
		}

		@SuppressWarnings("deprecation")
		private void handleSucess(GvapCommand commandID) {

			switch (commandID) {
			case CMD_LOGIN:
				try {
					appData.setAccountInfo(account);
					appData.getAccountInfo().setLogined(true);
					appData.getGVAPService().setUserServerLoginStatus(true);
					if (!appData.getAccountInfo().isGuest()) {
						mDialog.dismiss();
						isLogin = true;
						conentTitle.setText(getString(R.string.userdevice));
						setFragment.setIsLoginBtnName(isLogin);
						getSupportFragmentManager()
								.beginTransaction()
								.replace(R.id.content_frame,
										new FavoriteFragment())
								.commitAllowingStateLoss();
						setFragment.setLoginState(account.getUsername());
						isLocalList = false;
						mapBtn.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case CMD_REGISTER:
				isRegistered = true;
				onRegisterSucessListener.onRegisterSucess();
				mDialog.dismiss();
				Toast toast = Toast.makeText(getApplicationContext(),
						getString(R.string.registersuccess), Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				Intent intent = new Intent(LoginFragment.LOGIN_FRA_ACTION);
				intent.putExtra("ret", 1);
				sendBroadcast(intent);
				break;
			case CMD_UPDATE_USERINFO: {
				Toast.makeText(getApplicationContext(),
						getString(R.string.changepasswordsuccess),
						Toast.LENGTH_LONG).show();
			}
				break;
			default:
				break;
			}
		}

		@SuppressWarnings("deprecation")
		private void handleFailed(GvapCommand commandID) {
			// TODO Auto-generated method stub
			// Log.d(TAG, "handleFailed");
			if (commandID == null) {
				return;
			}
			switch (commandID) {
			case CMD_LOGIN:
				// Log.d(TAG, "CMD_LOGIN");
				if (!account.getUsername().equals("guest")) {
					try {
						mDialog.dismiss();

					} catch (IllegalArgumentException e) {
					}
					try {
						if (!FromPlatAcy) {
							showLoginErroDialog();
						}
					} catch (Exception e) {

						e.printStackTrace();
					}
				}
				break;
			case CMD_REGISTER:
				if (mDialog != null) {
					mDialog.dismiss();
				}
				Toast.makeText(getApplicationContext(), errorString,
						Toast.LENGTH_LONG).show();
				break;
			case CMD_UPDATE_USERINFO: {
				Toast.makeText(getApplicationContext(),
						getString(R.string.changepasswordfailed),
						Toast.LENGTH_LONG).show();
			}
			default:
				break;
			}
		}
	};
	private int devCount;

	public void showLoginErroDialog() {
		final Dialog dialog = new Dialog(MainActivity.this, R.style.Erro_Dialog);
		dialog.setContentView(R.layout.login_erro_dialog);
		TextView erroTx = (TextView) dialog.findViewById(R.id.erroTx);
		Button reTry = (Button) dialog.findViewById(R.id.erro_reTry);
		Button cancel = (Button) dialog.findViewById(R.id.erro_cancel);
		erroTx.setText(getString(R.string.invalidPassword));
		reTry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onLoginClicked(account);// ִ��һ�ε�¼����
				dialog.dismiss();
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public void showAddDialog() {
		final Dialog dialog = new Dialog(MainActivity.this, R.style.Erro_Dialog);
		dialog.setContentView(R.layout.add_dev_dialog);
		LinearLayout addInstall = (LinearLayout) dialog
				.findViewById(R.id.add_install);
		LinearLayout idAdd = (LinearLayout) dialog.findViewById(R.id.addbyid);
		LinearLayout voiceAdd = (LinearLayout) dialog
				.findViewById(R.id.addbyvoice);
		LinearLayout cancel = (LinearLayout) dialog.findViewById(R.id.cancel);
		LinearLayout scanInstall = (LinearLayout) dialog
				.findViewById(R.id.scan_install);

		scanInstall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
				startActivity(new Intent(MainActivity.this,
						ScanCodeActivity.class));

			}
		});
		addInstall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.cancel();
				// onJumpGuideActivity("complex");
				// 设备wifi设置
				Intent intent = new Intent(MainActivity.this,
						GlassListActivity.class);
				startActivity(intent);
			}
		});

		idAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// if (appData.getAccountInfo() == null
				// || !appData.getAccountInfo().isLogined()
				// || appData.getAccountInfo().isGuest())
				// {
				// conentTitle.setText(getString(R.string.login));
				// getSupportFragmentManager().beginTransaction()
				// .replace(R.id.content_frame, new LoginFragment()).commit();
				// dialog.dismiss();
				// getSlidingMenu().showContent();
				// Toast.makeText(getApplicationContext(),
				// getString(R.string.pleaseLoginFirst), Toast.LENGTH_LONG
				// ).show();
				// }
				// else
				{
					// 绑定（设备已联网）
					dialog.cancel();
					onJumpGuideActivity("simple");
				}

			}
		});
		voiceAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.cancel();
				Intent intent = new Intent(MainActivity.this, GuidSmartId.class);
				startActivity(intent);
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.cancel();
			}
		});
		dialog.show();
	}

	private void setNetStatus(boolean bNet) {
		bNetConnnect = bNet;
		if (appData != null) {
			appData.setNetStatus(bNet);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (AppData.isSetWifi) {
			AppData.isSetWifi = false;
			// showConnectwifiDialog();
			// if(code==9){
			//
			// }
			// if(code==0){
			mHandler.sendMessageDelayed(mHandler.obtainMessage(3), 100);
			// }
		}

		// SharedPreferences sp =
		// this.getSharedPreferences("config",Context.MODE_PRIVATE);
		// devCount=appData.getAccountInfo().getDevList().getTotalCount();
		// int num=sp.getInt("devCount",0);
		// if(devCount>num){
		// Uri notification =
		// RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		// if (notification == null) return;
		// Ringtone r = RingtoneManager.getRingtone(this, notification);
		// r.play();
		// }
		// System.out.println("设备数量"+devCount);

	}

	private void showConnectwifiDialog() {
		waitConnect = new Dialog(this, R.style.Login_Dialog);
		waitConnect.setContentView(R.layout.waiting_dialog);
		waitConnect.setCanceledOnTouchOutside(false);
		waitConnect.show();
	}

	@Override
	protected void onStop() {
		super.onStop();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		Boolean first = settings.getBoolean(FIRST_RUN, true);
		if (first) {
			editor.putBoolean(FIRST_RUN, false);
		}
		editor.commit();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (connectionReceiver != null) {
			unregisterReceiver(connectionReceiver);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onLoginClicked(AccountInfo account) {
		// TODO Auto-generated method stub
		appData.getGVAPService().logout();
		appData.getGVAPService().login(account);
		// appData.setAccountInfo(account);
		MainActivity.account = account;
		tryLoginTimes = 0;
		// showDialog(DIALOG_ID_LOGIN);
		showLoginDialog(DIALOG_ID_LOGIN);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRegisterClicked(AccountInfo account) {
		// TODO Auto-generated method stub
		appData.getGVAPService().removeGvapEventListener(this);
		appData.getGVAPService().restartRegServer();
		appData.getGVAPService().addGvapEventListener(this);
		appData.getGVAPService().register(account);
		registerAccountInfo = account;
		isRegistered = false;
		registRetryTimes = 0;
		// showDialog(DIALOG_ID_REGISTER);
		showLoginDialog(DIALOG_ID_REGISTER);
	}

	private void onGetVersionsSuccess(GvapPackage response) {
		if (!appData.getAccountInfo().isGuest()) {
			int ver = response.getIntegerParamWithDefault("dev-version", -1);
			if (ver != appData.getAccountInfo().getDevList().getVersion()) {
				appData.getGVAPService().getDeviceList();
				appData.getAccountInfo().getDevList().setServeVersion(ver); // �����б�ķ������汾
			} else
			// ���ذ汾��������汾��ͬ
			{
				appData.getGVAPService().getDeviceStatus(
						appData.getAccountInfo().getDevList());
			}
		}
		int ver = response.getIntegerParamWithDefault("pub-version", -1);
		if (ver != appData.getPublicList().getVersion()) {
			// ���ذ汾��������汾��ͬ����ȡ���°汾
			appData.getGVAPService().getPublicList();
			appData.getPublicList().setServeVersion(ver); // �����б�ķ������汾
		} else
		// ���ذ汾��������汾��ͬ,ֱ�ӻ�ȡ�豸״̬
		{
			appData.getGVAPService().getDeviceStatus(appData.getPublicList());
		}
	}

	private void updateDeviceInfo(GvapPackage request, GvapPackage response) // �����豸״̬����Ϣ
	{
		if (!appData.getAccountInfo().isGuest()) {
			String groupId = request.getParent_group();
			if (groupId == null) {
				GvapXmlParser.parseDevInfo(new String(response.getContent()),
						appData.getAccountInfo().getDevList());
				appData.getAccountInfo().getDevList().listUpdated();
			} else {
				GvapXmlParser.parseDevInfo(new String(response.getContent()),
						appData.getAccountInfo().getDevList(groupId));
				appData.getAccountInfo().getDevList(groupId).listUpdated();
			}

		}
		GvapXmlParser.parseDevInfo(new String(response.getContent()),
				appData.getPublicList());
		appData.getPublicList().listUpdated();
	}

	private void onOperationSuccess(GvapCommand cmd, GvapPackage response,
			GvapPackage request) {
		if (!isDestory) {
			Message msg;
			// Log.i(TAG, "onOperationSuccess  cmd == " + cmd);
			// Log.d(TAG, "onOperationSuccess retcode: " +
			// response.getStatusCode());
			switch (cmd) {
			case CMD_LOGIN: {
				if (account != null) {
					account.setLogined(true);
					appData.setAccountInfo(account);
				}
				appData.getAccountInfo().setLogined(true);
				appData.getGVAPService().getVersions(); // ��ȡ�汾��Ϣ
				// �����˻���Ϣ
				// dbAdapter.saveAccount(appData.getAccountInfo(), true);
				// isLogin = true;
				if (appData.isReLogin()) {
					appData.setReLogin(false);
				} else {
					msg = mHandler.obtainMessage(0, GvapCommand.CMD_LOGIN);
					mHandler.sendMessage(msg);
				}
				appData.getGVAPService().setHeartBeat_expire(
						Integer.parseInt(response.getParam("expire")));
				break;
			}
			case CMD_GET_VERSIONS: {
				onGetVersionsSuccess(response);
				break;
			}
			case CMD_GET_DEVLIST: {
				String parent_group = request.getParent_group();
				if (parent_group != null)// �����б�
				{
					onGetDeviListSuccess(response, appData.getAccountInfo()
							.getDevList(parent_group));

				} else // ��Ŀ¼�б�
				{
					onGetDeviListSuccess(response, appData.getAccountInfo()
							.getDevList());
				}
				break;
			}
			case CMD_GET_PUBLIST: {
				String parent_group = request.getParent_group();
				if (parent_group != null) {
					onGetDeviListSuccess(response,
							appData.getDevList(parent_group));
				} else
					onGetDeviListSuccess(response, appData.getPublicList());
				break;
			}
			case CMD_GET_DEVSTATUS: // ��ȡ�豸����״̬
				String groupId = request.getParent_group();
				if (!appData.getAccountInfo().isGuest()) {
					if (groupId != null) {
						appData.getGVAPService().getDeviceInfo(
								appData.getAccountInfo().getDevList(groupId));
					} else {
						appData.getGVAPService().getDeviceInfo(// ��ȡ�б����豸��ϸ��Ϣ��CMD_GET_DEVINFO��
								appData.getAccountInfo().getDevList());
					}
				}
				appData.getGVAPService().getDeviceInfo(appData.getPublicList());
			case CMD_GET_DEVINFO: {
				updateDeviceInfo(request, response);// ����xml
				// �����б���Ϣ
				if (!appData.getAccountInfo().isGuest()) {
					// dbAdapter.saveDeviceList(appData.getPublicList(),
					// appData.getAccountInfo().getUsername());
				}
				// dbAdapter.saveDeviceList(appData.getPublicList(), null);
				break;
			}

			case CMD_UPDATE_USERINFO: {
				msg = mHandler
						.obtainMessage(0, GvapCommand.CMD_UPDATE_USERINFO);
				mHandler.sendMessage(msg);
			}
				break;
			case CMD_GET_USRINFO:
			case CMD_UPDATE_DEVINFO:
				// ����gps�ɹ�
				// //Log.i("Main", "����GPS�ɹ�");
				// Toast.makeText(getApplicationContext(), "����GPA�ɹ�",
				// Toast.LENGTH_LONG).show();
				break;
			case CMD_REGISTER:
				msg = mHandler.obtainMessage(0, GvapCommand.CMD_REGISTER);
				mHandler.sendMessage(msg);
				break;
			case CMD_BIND:
				break;
			case CMD_UNBIND:
				msg = mHandler.obtainMessage(0, GvapCommand.CMD_UNBIND);
				mHandler.sendMessage(msg);
				break;
			case CMD_HB:
			case CMD_NOTIFY_DEVSTATUS: {
				break;
			}
			}
		}
	}

	/** getParamList��ȡ�б� */
	private void onGetDeviListSuccess(GvapPackage response, DeviceList devList) {
		if (fromFav) {
			devList.clear();
			fromFav = false;
		}
		List<String> list = response.getParamList("device-id");
		List<String> listGro = response.getParamList("group-id");
		if (listGro != null)// ��ȡ�������
		{
			for (String gro : listGro) {
				Group group = new Group(gro);
				devList.addGroup(group);
			}
			devList.updateSuccess();// �����б���³ɹ���־
			appData.getGVAPService().getGroupInfo(devList);
		}
		if (list != null)// ��ȡ�豸����
		{
			// Log.d("GVAPService", "CMD_GET_DEVLIST: list != null ");
			Group group = devList.getParent_group();
			if (group != null)
				group.setDevCount(list.size());
			for (String dev : list) {
				DeviceSee51Info info = new DeviceSee51Info(dev);
				devList.addBySee51Info(info); // �����µ��豸
			}
			devList.updateSuccess(); // �����б���³ɹ���־
			appData.getGVAPService().getDeviceStatus(devList);
		}
	}

	private String errorString;

	private void onOperationFailed(GvapCommand cmd, GvapPackage response) {
		Message msg;
		if (cmd == null) {
			// Log.i(TAG, "onOperationFalse cmd == null");
			return;
		}
		// Log.d(TAG, "onOperationFalse  cmd == " + cmd);
		// Log.d(TAG, "onOperationFalse retcode: " + response.getStatusCode());
		int retcode = response.getStatusCode();
		switch (cmd) {
		case CMD_LOGIN:

			switch (retcode) {
			default:
			case 403:
				// Log.i(TAG, "����ʧ�ܣ�relogin");
				appData.setReLogin(true);
				appData.getGVAPService().logout();
				appData.getGVAPService().login(account);
				break;

			case 405:
			case 406:
				// Log.i(TAG, "����ʧ�ܣ�handleFailed");
				msg = mHandler.obtainMessage(1, GvapCommand.CMD_LOGIN);
				mHandler.sendMessage(msg);
				break;
			}
			break;
		case CMD_REGISTER:
			// Log.i(TAG, "����ʧ�ܣ�CMD_REGISTER");
			switch (retcode) {
			case 409:
				errorString = getString(R.string.username_has_beenused);
				msg = mHandler.obtainMessage(1, GvapCommand.CMD_REGISTER);
				mHandler.sendMessage(msg);
				break;

			default:
				errorString = getString(R.string.registerfailed);
				msg = mHandler.obtainMessage(1, GvapCommand.CMD_REGISTER);
				mHandler.sendMessage(msg);
				appData.getGVAPService().restartRegServer();
				break;
			}

			break;

		case CMD_UPDATE_USERINFO: {
			// Log.i(TAG, "����ʧ�ܣ�CMD_UPDATE_USERINFO");
			msg = mHandler.obtainMessage(1, GvapCommand.CMD_UPDATE_USERINFO);
			mHandler.sendMessage(msg);
		}
			break;

		case CMD_BIND:
			break;
		case CMD_UNBIND:
			break;
		default:
			// Log.i(TAG, "����ʧ�ܣ�default��relogin");
			appData.setReLogin(true);
			appData.getGVAPService().logout();
			appData.getGVAPService().login(account);

			break;
		}
	}

	public static AccountInfo registerAccountInfo;
	public static boolean isRegistered;

	private int registRetryTimes = 0;

	public void onOperationTimeout(GvapCommand cmd, GvapPackage response) {
		Message msg;
		if (cmd == null) {
			// Log.i(TAG, "onOperationTimeout cmd == null");
			return;
		}
		// Log.d(TAG, "onOperationTimeout  cmd == " + cmd);
		switch (cmd) {
		case CMD_LOGIN:
			if (appData.isReLogin()) {
				// Log.i(TAG, "--850 appData.isReLogin");
				break;
			} else {
				// Log.i(TAG, "--855 tryLoginTimes++");
				tryLoginTimes++;
				if (tryLoginTimes < 4) {
					appData.getGVAPService().logout();
					appData.getGVAPService().login(account);
					// appData.setAccountInfo(account);
					// MainActivity.account = account;
					break;
				}
			}
			// msg = mHandler.obtainMessage(1,
			// GvapCommand.CMD_LOGIN);//2015.10.09
			// mHandler.sendMessage(msg);
			break;
		case CMD_REGISTER:
			appData.getGVAPService().removeGvapEventListener(this);
			appData.getGVAPService().restartRegServer();
			appData.getGVAPService().addGvapEventListener(this);
			if (registerAccountInfo != null && registRetryTimes < 4) {
				appData.getGVAPService().register(registerAccountInfo);
				registRetryTimes++;
			} else {
				errorString = getString(R.string.registerfailed);
				msg = mHandler.obtainMessage(1, GvapCommand.CMD_REGISTER);
				mHandler.sendMessage(msg);
			}
			break;

		case CMD_UPDATE_USERINFO: {
			msg = mHandler.obtainMessage(1, GvapCommand.CMD_UPDATE_USERINFO);
			mHandler.sendMessage(msg);
		}
			break;
		case CMD_UNBIND:
			break;
		case CMD_GET_DEVLIST:
			// Log.i(TAG, "��ȡԶ���б?ʱ");
			break;
		default:
			break;
		}
	}

	public void onConnectReset() {
		boolean i = reLoginThread == null;
		// Log.i(TAG, "--reloginThread=null?��"+i+"--isLogin:"+isLogin);
		if (reLoginThread == null && isLogin) {
			// Log.i(TAG, "--run reLoginRunnable");
			reLoginThread = new Thread(reLoginRunnable);
			reLoginThread.start();
		}
	}

	Runnable reLoginRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (isLogin) // always runing check if need reloagin
			{
				// Log.i(TAG, "--isLogin:always runing check if need relogin");
				if (!bNetConnnect)// bNetConnnect����������״̬
				{
					// Log.i(TAG, "--����������");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.d(TAG, "5s continue");
					continue;

				} else {
					// Log.i(TAG, "--����������");
				}

				if (!appData.getAccountInfo().isLogined()) {
					// Log.i(TAG, "--�û�δ��¼-ConnectReset");
					ConnectReset();
				} else {
					// Log.i(TAG, "--�û��ѵ�¼");
				}
				try {
					// Log.i(TAG, "--reLoginRunnable sleep 15s");
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	};
	private String sss;
	private Dialog waitConnect;

	public void ConnectReset() {
		if (appData.getAccountInfo() != null) {
			// Log.i(TAG, "--ConnectReset-relogin�ǳ�����");
			// Log.d(TAG, "relogin");
			appData.setReLogin(true);
			appData.getGVAPService().logout();
			appData.getAccountInfo().setLogined(false);
			appData.getGVAPService().login(account);
		}
	}

	public void onConnectFailed() {
		// Log.d(TAG, "onConnectFailed");
		if (appData.getAccountInfo() != null
				&& appData.getAccountInfo().isLogined()) {
			// Log.i(TAG, "---------onConnectFailed--->relogin");
			appData.setReLogin(true);
			appData.getGVAPService().logout();
			appData.getAccountInfo().setLogined(false);
			appData.getGVAPService().login(account);
		}
	}

	private void onNetworkError() {
		// Log.i(TAG, "onConnectFailed");
		appData.getGVAPService().setUserServerLoginStatus(false);
		if (appData.getAccountInfo() != null
				&& appData.getAccountInfo().isLogined()) {
			// Log.i(TAG, "relogin");
			appData.setReLogin(true);
			appData.getGVAPService().logout();
			appData.getAccountInfo().setLogined(false);
			appData.getGVAPService().login(account);
		}
	}

	private void onGetServerRequest(GvapPackage response) {
		String alarmtype = response.getResourceName();
		System.out.println("-----------------------响应为:" + alarmtype);
		if (alarmtype.equals("alarm")) {
			String deviceId = response.getParam("device-id");
			Device device = appData.getAccountInfo().getDevList()
					.getDevice(deviceId);
			String devName = "ipcamera";
			if (device != null) {
				devName = device.getSee51Info().getDeviceName();
			}

			AlarmInfo alarmInfo = new AlarmInfo(deviceId, devName,
					response.getParam("message"), response.getParam("title"),
					response.getParam("alarmtime"),
					response.getParam("dataurl"));
			Log.d(TAG, "alarmtime" + response.getParam("alarmtime"));// alarmtime1483495911
			// Log.d(TAG,"message"+response.getParam("message")+"\n"+"title"+response.getParam("title"));
			appData.getAccountInfo().addAlarmInfo(alarmInfo);
			addNotificaction();
		}
		if (alarmtype.equals("bind")) {
			Intent intent = new Intent(this, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					intent, PendingIntent.FLAG_ONE_SHOT);
			Notification notification = new Notification.Builder(this)
					.setAutoCancel(true)
					.setContentTitle(getString(R.string.bind))
					.setContentText(getString(R.string.bindSuccess))
					.setContentIntent(pendingIntent)
					.setSmallIcon(R.drawable.ic_launcher)
					.setWhen(System.currentTimeMillis()).build();
			notification.defaults = Notification.DEFAULT_SOUND;
			notificationManager.notify(1, notification);
		}
	}

	@Override
	public void onGvapEvent(GvapEvent event) {

		switch (event) {
		case OPERATION_SUCCESS:
			// Log.i(TAG,"-----------�����ɹ�");
			onOperationSuccess(event.getCommandID(),// �������request.getCommandID()
					(GvapPackage) event.attach(),// �������ظ���
					(GvapPackage) event.getRequest());// �����from
														// sendOverList��
			break;
		case OPERATION_FAILED:
			// Log.i(TAG,"-----------����ʧ��");
			onOperationFailed(event.getCommandID(),
					(GvapPackage) event.attach());
			break;
		case OPERATION_TIMEOUT:
			// Log.i(TAG,"-----------������ʱ");
			onOperationTimeout(event.getCommandID(),
					(GvapPackage) event.attach());
			break;
		case CONNECTION_RESET:
			// Log.i(TAG,"-----------��������");
			onConnectReset();
			break;
		case CONNECT_TIMEOUT:
			// Log.i(TAG,"-----------���ӳ�ʱ");
			break;
		case CONNECT_FAILED:
			// Log.i(TAG,"-----------����ʧ��");
			onConnectFailed();
			break;
		case NETWORK_ERROR:
			// Log.i(TAG,"-----------�������");
			onNetworkError();
			break;
		case SERVER_REQUEST:
			// Log.i(TAG,"-----------SERVER_REQUEST-����");
			onGetServerRequest((GvapPackage) event.attach());
			break;
		case OPEN_FAILED:
			// Log.i(TAG,"-----------OPEN_FAILED");
			openNum++;
			appData.getGVAPService().stop();
			appData.getGVAPService().start();
			break;

		}
	}

	protected void onQuit() {
		AlertDialog.Builder builder = new Builder(MainActivity.this);
		builder.setMessage(R.string.quit);
		builder.setTitle(android.R.string.dialog_alert_title);
		builder.setPositiveButton(android.R.string.ok,
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						try {
							if (isLogin) {
								isLogin = false;
								bReLoginFlag = false;
								reLoginThread = null;
								setFragment.setIsLoginBtnName(isLogin);
								appData.getGVAPService().logout();
								appData.getAccountInfo().setLogined(false);
								appData.setAccountInfo(null);
								FragmentManager fm = MainActivity.this
										.getSupportFragmentManager();
								fm.findFragmentByTag("favorite").onDestroy();
								// actionBar.removeTabAt(1);
								// Log.d(TAG, "removeTabAt(1)  onQuit()");
								// actionBar.addTab(mLoginTab, 1, true);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						appData.release();
						android.os.Process.killProcess(android.os.Process
								.myPid());
						System.exit(0);

						MainActivity.this.finish();

					}
				});
		builder.setNegativeButton(android.R.string.cancel,
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	protected void onLogout() {
		getSlidingMenu().showContent();
		if (isLogin) {
			isLogin = false;
			bReLoginFlag = false;
			reLoginThread = null;
			setFragment.setIsLoginBtnName(isLogin);
			appData.getGVAPService().logout();
			appData.getAccountInfo().setLogined(false);
			appData.setAccountInfo(null);
			setFragment.setLoginState(getString(R.string.notLogedIn));
			conentTitle.setText(getString(R.string.login));
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.content_frame, mContent).commit();
			mapBtn.setVisibility(View.GONE);
			// Log.d(TAG, "onLogout()");
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (!isFinish) {
				isFinish = true;
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						isFinish = false;
						mHandler.sendEmptyMessage(2);
					}
				}, 500);
				return true;
			} else {
				isDestory = true;
				finish();
			}
		}
		return false;
	}

	private class SingleGestureListener extends
			android.view.GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onSingleTapConfirmed(e);
		}
	}

	public boolean isRootDirectory() {
		if (appData.getAccountInfo() != null) {
			DeviceList currentList = appData.getAccountInfo().getCurrentList();
			if (currentList != null) {
				if (currentList.getParent_group() != null) {
					return false;
				} else
					return true;
			}
		}
		return true;
	}

	public void backParent() {
		if (appData.getAccountInfo() != null) {
			DeviceList currentList = appData.getAccountInfo().getCurrentList();
			String grandParent_group = currentList.getGrandParent_group();
			DeviceList devList;
			if (grandParent_group != null) {
				devList = appData.getAccountInfo()
						.getDevList(grandParent_group);
				appData.getAccountInfo().setCurrentList(devList);
			} else {

				MainActivity.menuBtn.setVisibility(View.VISIBLE);
				MainActivity.menuBack.setVisibility(View.GONE);
				devList = appData.getAccountInfo().getDevList(); // ������
				appData.getAccountInfo().setCurrentList(devList);
				displayHomeButton(false);
			}
			FavoriteFragment.adapter.setDeviceList(devList);
			devList.listUpdated();
		}
	}

	public void displayHomeButton(boolean isDisplay) {
		if (isDisplay) {
			if (!displayHome) {
				displayHome = true;
			}
		} else {
			if (displayHome) {
				displayHome = false;
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		ProgressDialog dialog = new ProgressDialog(this);
		switch (id) {
		case DIALOG_ID_LOGIN:
			dialog.setTitle(R.string.login);
			dialog.setMessage(getString(R.string.userlogining));
			dialog.setCancelable(true);
			break;
		case DIALOG_ID_REGISTER:
			dialog.setTitle(R.string.register);
			dialog.setMessage(getString(R.string.registerring));
			dialog.setCancelable(true);
		default:
			break;
		}
		return dialog;
	}

	public void showLoginDialog(int id) {
		mDialog = new Dialog(MainActivity.this, R.style.Login_Dialog);
		mDialog.setContentView(R.layout.login_dialog);
		TextView tx = (TextView) mDialog.findViewById(R.id.tx);
		if (id == DIALOG_ID_LOGIN)
			tx.setText(getString(R.string.userlogining));
		else if (id == DIALOG_ID_REGISTER)
			tx.setText(getString(R.string.registerring));
		mDialog.show();
	}

	@SuppressWarnings("deprecation")
	public void addNotificaction() {
		// Log.d(TAG, "addNotificaction");
		if (isAlarmNotification) {
			Intent intent = new Intent(this, ActionAlarmActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					intent, PendingIntent.FLAG_ONE_SHOT);
			Notification notification = new Notification.Builder(this)
					.setAutoCancel(true)
					.setContentTitle(getString(R.string.alarm))
					.setContentText(
							getString(R.string.strangers_illegal_instrusion))
					.setContentIntent(pendingIntent)
					.setSmallIcon(R.drawable.ic_launcher)
					.setWhen(System.currentTimeMillis()).build();
			notification.defaults = Notification.DEFAULT_SOUND;
			notificationManager.notify(1, notification);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// Log.d(TAG, "onItemClicked");
		// TODO Auto-generated method stub
		int listItemId = arg1.getId();
		switch (listItemId) {
		case SetListAdapter.ALRAMHISTORY:
			Intent intent = new Intent(this, ActionAlarmActivity.class);
			startActivity(intent);
			break;
		case SetListAdapter.SWITCHACCOUNT:
			onLogout();
			break;
		case SetListAdapter.EDITPASSWD: {

			if (appData.getAccountInfo() == null
					|| !appData.getAccountInfo().isLogined()
					|| appData.getAccountInfo().isGuest()) {

				conentTitle.setText(getString(R.string.login));
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.content_frame, new LoginFragment())
						.commit();

				Toast.makeText(this, getString(R.string.pleaseLoginFirst),
						Toast.LENGTH_LONG).show();

			} else {
				conentTitle.setText(getString(R.string.changepassword));
				getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.content_frame,
								new ChangeUserPasswdActivity()).commit();
			}

			getSlidingMenu().showContent();
		}
			break;

		case SetListAdapter.ABOUT: {
			Intent intentabout = new Intent(this, AboutActivity.class);
			startActivity(intentabout);
		}
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		if (isChecked) {
			editor.putBoolean(ALARM_NOTIFICATION, true);
			isAlarmNotification = true;
		} else {
			editor.putBoolean(ALARM_NOTIFICATION, false);
			isAlarmNotification = false;
		}
		editor.commit();
	}

	public void onJumpGuideActivity(String strType) {
		String inBindType = "complex";
		inBindType = strType;

		Intent intent;
		// if (appData.getAccountInfo() == null
		// || !appData.getAccountInfo().isLogined()
		// || appData.getAccountInfo().isGuest())
		// {
		// /*intent = new Intent(MainActivity.this, Guide.class);
		// intent.putExtra("BindStyle", inBindType);*/
		// getSupportFragmentManager().beginTransaction()
		// .replace(R.id.content_frame, mContent).commit();
		// sm.toggle();
		// Toast.makeText(getApplicationContext(),
		// getResources().getString(R.string.wififerro),
		// Toast.LENGTH_SHORT).show();
		//
		// } else
		{
			// 设备已联网的activity
			intent = new Intent(MainActivity.this, DeviceIdActivity.class);
			intent.putExtra("BindStyle", inBindType);
			startActivity(intent);
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.menuBack:
			backParent();
			break;
		case R.id.menuBtn:
			sm.toggle();
			break;
		case R.id.btnNewCamera:// 安装摄像头
			/*
			 * {
			 * 
			 * if (appData.getAccountInfo() == null ||
			 * !appData.getAccountInfo().isLogined() ||
			 * appData.getAccountInfo().isGuest()) {
			 * conentTitle.setText(getString(R.string.login));
			 * getSupportFragmentManager().beginTransaction()
			 * .replace(R.id.content_frame, new LoginFragment()).commit();
			 * getSlidingMenu().showContent(); Toast.makeText(this,
			 * getString(R.string.pleaseLoginFirst), Toast.LENGTH_LONG ).show();
			 * } else { showAddDialog(); } }
			 */
			if (isLogin) {
				showAddDialog();
			} else {
				conentTitle.setText(getString(R.string.login));
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.content_frame, new LoginFragment())
						.commit();
				getSlidingMenu().showContent();
			}
			break;

		case R.id.btnLocalCamera:// 本地设备
			conentTitle.setText(getString(R.string.landev));
			mapBtn.setVisibility(View.GONE);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.content_frame, new LocalFragment()).commit();
			isLocalList = true;
			getSlidingMenu().showContent();
			break;

		case R.id.btnMyCamera:// 远程设备
			if (isLogin) {
				conentTitle.setText(getString(R.string.userdevice));
				mapBtn.setVisibility(View.VISIBLE);
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.content_frame, new FavoriteFragment())
						.commit();

			} else {
				conentTitle.setText(getString(R.string.login));
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.content_frame, new LoginFragment())
						.commit();
			}

			isLocalList = false;
			getSlidingMenu().showContent();
			break;
		case R.id.btnProgramExit:
			mapBtn.setVisibility(View.GONE);
			mapBtn.setVisibility(View.GONE);
			onQuit();
			break;

		case R.id.btnLogin:// 登录
			if (isLogin) {
				onLogout();
				setFragment.setIsLoginBtnName(isLogin);
			} else {
				conentTitle.setText(getString(R.string.login));
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.content_frame, new LoginFragment())
						.commit();
				getSlidingMenu().showContent();

			}

			break;
		case R.id.showWarn:
			// onLogout();
			break;
		case R.id.warnMsg:// 报警历史
			Intent intent = new Intent(this, ActionAlarmActivity.class);
			startActivity(intent);
			break;
		case R.id.changePsw:// 修改密码
		{
			mapBtn.setVisibility(View.GONE);
			if (appData.getAccountInfo() == null
					|| !appData.getAccountInfo().isLogined()
					|| appData.getAccountInfo().isGuest()) {

				conentTitle.setText(getString(R.string.login));
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.content_frame, new LoginFragment())
						.commit();

				Toast.makeText(this, getString(R.string.pleaseLoginFirst),
						Toast.LENGTH_LONG).show();

			} else {
				conentTitle.setText(getString(R.string.changepassword));
				getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.content_frame,
								new ChangeUserPasswdActivity()).commit();
			}

			getSlidingMenu().showContent();
		}
			break;
		case R.id.about:// 关于
		{
			Intent intentabout = new Intent(this, AboutActivity.class);
			startActivity(intentabout);
			overridePendingTransition(R.anim.slide_out_left,
					R.anim.slide_in_right);
		}
			break;

		case R.id.loginLayout:// 去登陆
			if (isLogin) {
				onLogout();
				setFragment.setIsLoginBtnName(isLogin);
			} else {
				conentTitle.setText(getString(R.string.login));
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.content_frame, new LoginFragment())
						.commit();
				getSlidingMenu().showContent();

			}
			break;
		case R.id.local_video:// 本地文件
			Intent intentabout = new Intent(this, LocalFileAcy.class);
			startActivity(intentabout);
			overridePendingTransition(R.anim.slide_out_left,
					R.anim.slide_in_right);
			break;
		case R.id.mapBtn:
			startActivity(new Intent(MainActivity.this, CameraLocation.class));
			break;
		}
	}

	@Override
	public void onChangePasswdClicked(String strOldPasswd, String strNewPasswd,
			String strNewPasswdAgain) {
		// TODO Auto-generated method stub

		if (strOldPasswd.equals(appData.getAccountInfo().getPassword())) {
			if (strNewPasswd.length() >= 6 && strNewPasswd.length() <= 16) {
				if (strNewPasswd.equals(strNewPasswdAgain)) {
					appData.getGVAPService().removeGvapEventListener(this);
					appData.getGVAPService().restartRegServer();
					appData.getGVAPService().addGvapEventListener(this);
					appData.getGVAPService().changePassword(
							appData.getAccountInfo(), strNewPasswd);
				} else {
					Toast.makeText(getApplicationContext(),
							getString(R.string.passwordinmatch),
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.passwordLenRequire),
						Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(getApplicationContext(),
					getString(R.string.invalidPassword), Toast.LENGTH_LONG)
					.show();
		}
	}

}
