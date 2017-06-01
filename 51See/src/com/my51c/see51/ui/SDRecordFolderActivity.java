package com.my51c.see51.ui;

import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.my51c.see51.adapter.SDRecordFolderListAdapter;
import com.my51c.see51.data.SDFileInfo;
import com.my51c.see51.listener.OnDeleteSDFileListener;
import com.my51c.see51.listener.OnGetSDFileListListener;
import com.my51c.see51.media.RemoteInteractionStreamer;
import com.my51c.see51.service.AppData;
import com.my51c.see51.widget.DeviceListView;
import com.my51c.see51.widget.DeviceListView.OnRefreshListener;
import com.my51c.see51.widget.ToastCommom;
import com.sdview.view.R;

public class SDRecordFolderActivity extends SherlockActivity implements  OnRefreshListener {
	
	private final String TAG = "SDRecordFolderActivity";
	private DeviceListView listViewSDRecord;
	private View progressView;
	private View waitTextView;
	private View emptyView;
	private AppData appData;
	private RemoteInteractionStreamer mediaStreamer;
	public  static String fileDate;
	private ArrayList<SDFileInfo> mFileList;
	private ArrayList<SDFileInfo> mFolderist = null;
	private ArrayList<String> folderNameList;
	private SDRecordFolderListAdapter folderAdapter;
	DataOutputStream dos;
	private String deviceID;
	private String url;
	
	static final int MSG_UPDATE = 0;

	static final int MSG_ClEAR_PROGRESSBAR = 1;
	static final int MSG_START_DOWNLOAD = 2;
	static final int MSG_STOP_DOWNLOAD = 3;
	static final int MSG_PERCENT_PROCESSBAR = 4;
	static final int MSG_FOLDER_UPDATE = 5;
	static final int MSG_UPDATE_DATA= 6;
	static final int MSG_DELETE_SUCCESS= 9;
	private boolean isFolder = false;
	public static boolean isLocal = false;
	private String strFileList = null;
	private boolean isNVR = false;
	private String nvrDeviceId = "";
	private TextView title;
	ToastCommom toast = new ToastCommom();
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		Log.i(TAG, "--------onCreate----------");
		setContentView(R.layout.sdrecord_activity);
		LinearLayout backLayout = (LinearLayout)findViewById(R.id.sd_back_layout);
		ImageView searchImg = (ImageView)findViewById(R.id.sd_search_img);
		backLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				backMainActivity();
			}
		});
		searchImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SDRecordFolderActivity.this, SDCalendarActivity.class);
				intent.putExtra("nvrDeviceId", nvrDeviceId);
				intent.putExtra("isAllClick", false);
				startActivity(intent);
			}
		});
		
		title = (TextView)findViewById(R.id.sd_titleName);
		listViewSDRecord = (DeviceListView) findViewById(android.R.id.list);
		listViewSDRecord.setItemsCanFocus(true);
		listViewSDRecord.setonRefreshListener(this);
		listViewSDRecord.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String date = folderNameList.get(position-1).substring(0, 8);
				showSettingDialog(date+"/"+folderNameList.get(position-1));
				return true;
			}
		});
		
		mFileList = new ArrayList<SDFileInfo>();
		mFolderist = new ArrayList<SDFileInfo>();
		folderNameList = new ArrayList<String>();
		folderAdapter = new SDRecordFolderListAdapter(getApplicationContext(), folderNameList);
		
		progressView = findViewById(R.id.progress_get_devices_image);
		waitTextView = findViewById(R.id.loading);
		emptyView    = findViewById(R.id.emptyView);
		
		appData = (AppData) getApplication();
		isNVR =  getIntent().getBooleanExtra("isNVR",false);
		if(isNVR){
			nvrDeviceId = getIntent().getStringExtra("nvrDeviceId");
			title.setText(nvrDeviceId);
		}
		deviceID = getIntent().getStringExtra("id");
		url = getIntent().getStringExtra("url");
		isLocal =  getIntent().getBooleanExtra("isLocal",false);
		Log.i(TAG, "url:"+url);
		fileDate = getnowdate(); 
		mediaStreamer = appData.getRemoteInteractionStreamer();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		
		if(!PlayAcy.isPlayBack)
		{
			Log.i(TAG, "!PlayAcy back, refresh"+fileDate);
			if(mediaStreamer != null )
			{
			   mediaStreamer.setOnGetSDFileListListener(mOnGetSDFileListListener);
			   mediaStreamer.setmOnDeleteSDFileListener(mOnDeleteSDFileListener);
			   mediaStreamer.getSDFileListByDate(fileDate,nvrDeviceId);
			   //refreshsdfile();
			}
		}else{
			Log.i(TAG, "PlayAcy back");
			PlayAcy.isPlayBack = false;
		}
		super.onResume();
	}
	
	@Override
	protected void onStop() {
//		listViewSDRecord.removeAllViews();
		super.onStop();
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		refreshsdfile();
	}
	
	public void refreshsdfile()
	{
		progressView.setVisibility(View.VISIBLE);
		waitTextView.setVisibility(View.VISIBLE);
		folderNameList.clear();
		folderAdapter.notifyDataSetChanged();
		if(mediaStreamer != null)
		{
			mediaStreamer.getSDFileListByDate(fileDate,nvrDeviceId);
		}
	}
	
	private OnDeleteSDFileListener mOnDeleteSDFileListener = new OnDeleteSDFileListener() {
		
		@Override
		public void OnDeleteSDFileSuccess() {
			// TODO Auto-generated method stub
			mHandler.sendEmptyMessage(MSG_DELETE_SUCCESS);
		}
		
		@Override
		public void OnDeleteSDFileFailed() {
			// TODO Auto-generated method stub
			
		}
	};
	
	private OnGetSDFileListListener mOnGetSDFileListListener = new OnGetSDFileListListener() {//getInfo监听
		@Override
		public void onGetFileList(byte[] devbuf) {
			// TODO Auto-generated method stub
			Log.i(TAG, "--mOnGetSDFileListListener:"+devbuf.length+"------devbuf:"+devbuf);
			strFileList = byteToString(devbuf);
			Log.i(TAG, "--mOnGetSDFileListListener:"+strFileList);
			mHandler.sendEmptyMessage(MSG_UPDATE_DATA);
		}	
	};
	
	
	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case MSG_FOLDER_UPDATE:
				listViewSDRecord.setAdapter(folderAdapter);
				listViewSDRecord.setOnItemClickListener(new onFolderItemClick());
				folderAdapter.notifyDataSetChanged();
				progressView.setVisibility(View.INVISIBLE);
				waitTextView.setVisibility(View.INVISIBLE);
				emptyView.setVisibility(View.INVISIBLE);
				break;
				
			case MSG_ClEAR_PROGRESSBAR:
				progressView.setVisibility(View.INVISIBLE);
				waitTextView.setVisibility(View.INVISIBLE);
				emptyView.setVisibility(View.INVISIBLE);
				break;
			
			case MSG_UPDATE_DATA:
				mFileList.clear();
	 			folderNameList.clear();
	 			mFolderist.clear();
				String[] strItem = strFileList.split("\\|");
				Log.i(TAG, "--strItem.length:"+strItem.length);
				if(strItem!=null)
				{
					if(strItem.length<2){
						return;
					}else{
						{
							Log.i(TAG, "--folder");
							isFolder = true;
							for(int i=1; i<strItem.length; i++)
							{
								String []itemText = strItem[strItem.length-i].split(",");
								
								if(itemText.length != 2)
									continue;
								folderNameList.add(itemText[0]);
							}
							mHandler.sendEmptyMessage(MSG_FOLDER_UPDATE);
						}
					}
				}
				break;
			case MSG_DELETE_SUCCESS:
				toast.ToastShow(getApplicationContext(), getString(R.string._delete_success), 1000);
				refreshsdfile();
				break;
			default:
				break;
			}
		}
	};
	
	protected String byteToString(byte[] src)
	{
		int len = 0;
		for (; len < src.length; len++)
		{
			if (src[len] == 0)
			{
				break;
			}
		}
		return new String(src, 0, src.length);
	}
	
	public String getnowdate()
	{	
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");    
		return sdf.format(new java.util.Date()); 
	}
	
	private class onFolderItemClick implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(SDRecordFolderActivity.this, SDRecordFileActivity.class);
			intent.putExtra("dateHour", folderNameList.get(position-1));
			intent.putExtra("nvrDeviceId", nvrDeviceId);
			intent.putExtra("url", url);
			intent.putExtra("id", deviceID);
			if(position == 1){
				intent.putExtra("isFirstItem", true);
			}
			startActivity(intent);
			overridePendingTransition(R.anim.slide_out_left , R.anim.slide_in_right);
		}
	}
	
	//还需添加删除超时和删除失败处理
	public void showSettingDialog(final String cmdStr){
		final Dialog dialog = new Dialog(SDRecordFolderActivity.this, R.style.Erro_Dialog);
		dialog.setContentView(R.layout.dialog_sd_del_share);
		
		RelativeLayout share = (RelativeLayout)dialog.findViewById(R.id.share);
		RelativeLayout delete = (RelativeLayout)dialog.findViewById(R.id.delete);
		RelativeLayout cancel = (RelativeLayout)dialog.findViewById(R.id.cancel);
		share.setVisibility(View.GONE);
		
		share.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				dialog.dismiss();
			}
		});
		
		delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showConfirmDialog(cmdStr);
				dialog.dismiss();
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	public void showConfirmDialog(final String cmdStr){
		final Dialog delDialog = new Dialog(SDRecordFolderActivity.this, R.style.Erro_Dialog);
		delDialog.setContentView(R.layout.del_dialog);
		TextView infoTx = (TextView)delDialog.findViewById(R.id.erroTx);
		Button sure = (Button)delDialog.findViewById(R.id.del_ok);
		Button cancel = (Button)delDialog.findViewById(R.id.del_cancel);
		
		infoTx.setText(getString(R.string.delete_for_sure));
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				delDialog.dismiss();
			}
		});
		
		sure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mediaStreamer.deleteSDFile(cmdStr);
				delDialog.dismiss();
			}
		});
		delDialog.show();
	}
	
	public void backMainActivity()
	{
		SDRecordFolderActivity.this.finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}
}
