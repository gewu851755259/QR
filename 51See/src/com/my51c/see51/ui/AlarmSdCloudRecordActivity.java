package com.my51c.see51.ui;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.my51c.see51.adapter.CloudRecordFileListAdapter;
import com.my51c.see51.adapter.SDRecordFileListAdapter;
import com.my51c.see51.adapter.SDRecordFileListAdapter.ViewHolder;
import com.my51c.see51.data.CloudFileInfo;
import com.my51c.see51.data.CloudHandle;
import com.my51c.see51.data.Device;
import com.my51c.see51.data.SDFileInfo;
import com.my51c.see51.listener.OnDeleteSDFileListener;
import com.my51c.see51.listener.OnGetFileDataOverListener;
import com.my51c.see51.listener.OnGetSDFileDataListener;
import com.my51c.see51.listener.OnGetSDFileListListener;
import com.my51c.see51.media.MediaStreamFactory;
import com.my51c.see51.media.RemoteInteractionStreamer;
import com.my51c.see51.media.cloudsdk;
import com.my51c.see51.service.AppData;
import com.my51c.see51.ui.PlayAcy;
import com.my51c.see51.ui.SDRecordFileActivity;
import com.my51c.see51.ui.SDRecordFolderActivity;
import com.my51c.see51.widget.ScollListView;
import com.my51c.see51.widget.ToastCommom;
import com.sdview.view.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

public class AlarmSdCloudRecordActivity extends Activity {

	private static final String TAG = "AlarmSdCloudRecord";
	private String deviceID;
	private String alarmTime;
	private String alarmUrl;
	private ScollListView sdlist;
	private ScollListView cloudlist;
	private View progressView;
	private View waitTextView;
	private View emptyView;
	private AppData appData;
	private cloudsdk csdk;
	private CloudHandle chParam;
	private String startTime;
	private String endTime;
	public static String cloudStartTime;
	public static String cloudEndTime;
	private RemoteInteractionStreamer mediaStreamer;
	private String url;  
	private String dataHour;
	private boolean isDownLoading = false;
	private ArrayList<SDFileInfo> mFileList;
	private ArrayList<CloudFileInfo> mcloudFileList;
	private ArrayList<SDFileInfo> dlInfoList;
	private ArrayList<ViewHolder> dlHolderList;
	private SDFileInfo mSelSDInfo;
	private CloudFileInfo mSelCloudInfo;
	private ViewHolder mSelholder;
	private com.my51c.see51.adapter.CloudRecordFileListAdapter.ViewHolder mCloudholder;
	private String strFileList = null;
	private SDRecordFileListAdapter msdAdapter;
	private CloudRecordFileListAdapter mcloudAdapter;
	DataOutputStream dos;
	DataOutputStream clouddos;
	private boolean isDlRemove = false;
	private File mFileDownload;
	private File mCloudFileDownload;
	private boolean bdownload = false;
	private boolean clouddownload = false;
	private ToastCommom toast = new ToastCommom();
	private static final int MSG_UPDATE_SDDATA = 0;
	private static final int MSG_SDREFRESH = 1;
	protected static final int MSG_SDUPDATE = 2;
	protected static final int MSG_CLOUDUPDATE = 3;
	static final int MSG_PERCENT_PROCESSBAR = 4;
	static final int MSG_NEXT_DOWNLOAD = 5;
	static final int MSG_STOP_CLOUDDOWNLOAD = 6;
	static final int MSG_CLOUD_PERCENT_PROCESSBAR = 7;
	static final int MSG_INVISABLEPROCESSBAR = 8;
	static final int MSG_DELETE_SUCCESS= 10;
	
	
	static final int GHDSC_OK = 0;
	static final int GHDSC_ERROR = -1;
	static final int GHDSC_ERROR_NET_INIT = -2;
	static final int GHDSC_ERROR_INVALID_PARAM = -3;
	static final int GHDSC_ERROR_LACK_DEPENDENCY = -4;
	static final int GHDSC_ERROR_INIT_DEPENDENCY = -5;
	static final int GHDSC_ERROR_CONNECT_FAILED = -6;
	static final int GHDSC_ERROR_LOGIN_FAILED = -7;
	static final int GHDSC_ERROR_NOT_EXIST = -8;
	
	static final int HDS_EVENT = 0;
	static final int HDS_VIDEO = 1;
	
	private Timer timer;
	private TimerTask timerTask;
	private int nFilePer = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_sd_cloud_record);
		findview();
		setOnItemClick();
		getBundle();
		setSdsearchTime();
		initData();
		setCloudTime();
	}
	
	private void setOnItemClick() {
		// TODO Auto-generated method stub
		sdlist.setOnItemClickListener(new SdItemClickListener());
		cloudlist.setOnItemClickListener(new CloudItemClickListener());
	}

	private void setCloudTime() {
		// TODO Auto-generated method stub
		cloudStartTime = Long.toString((Long.parseLong(alarmTime)-30));
		cloudEndTime = Long.toString((Long.parseLong(alarmTime)+30));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
	}

	@Override
	protected void onResume() {
		msdAdapter.notifyDataSetChanged();
		mcloudAdapter.notifyDataSetChanged();
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mediaStreamer!=null){
			mediaStreamer.close();
			mediaStreamer.setOnGetSDFileListListener(null);
			mediaStreamer.setOnGetSDFileDataListener(null);
			mediaStreamer.setOnGetFileDataOverListener(null);
			mediaStreamer = null;
		}
	}

	private void setSdsearchTime() {
		new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
					SimpleDateFormat curformat = new SimpleDateFormat("yyyyMMddHH");
					dataHour = curformat.format(new Date(Long.valueOf(alarmTime)*1000)); 
					startTime = Long.toString((Long.parseLong(alarmTime)-30));
					endTime = Long.toString((Long.parseLong(alarmTime)+30));
					SimpleDateFormat format = new SimpleDateFormat("HHmmss");
					startTime = format.format(new Date(Long.valueOf(startTime)*1000));
					endTime = format.format(new Date(Long.valueOf(endTime)*1000));
					Log.d(TAG, dataHour+"---"+startTime +"---"+endTime);
			}
					
		}.start();
		
	}

	private void initData() {
		
		appData = (AppData) getApplication();
		Device dev = appData.getAccountInfo().getCurrentList().getDevice(deviceID);
		url = dev.getPlayURL();
		createRemoteOperaction();
		if(mediaStreamer != null)
		{	
			mediaStreamer.setOnGetSDFileDataListener(mOnGetSDFileDataListener);
			mediaStreamer.setOnGetFileDataOverListener(mOnGetFileDataOverListener);
			mediaStreamer.setmOnDeleteSDFileListener(mOnDeleteSDFileListener);
			mediaStreamer.setOnGetSDFileListListener(mOnGetSDFileListListener);
			mediaStreamer.getSDFileListByDate(dataHour, startTime, endTime, deviceID);
			mHandler.sendEmptyMessageDelayed(MSG_SDREFRESH, 800);
		}
		String clouurl = alarmUrl;
		csdk = new cloudsdk();
		chParam = new CloudHandle();
		chParam.setStrADKPath("/temp/");
		csdk.Native_GHDSCClient_Init(chParam);
		csdk.Native_GHDSCClient_Create(chParam);
		
		String paramaddr = "AddrRemote[=]";
		paramaddr += clouurl;
		paramaddr += "[|]TOConnect[=]60[|]TORW[=]60[|]";
		chParam.setStrParam(paramaddr);
		chParam.setStrUsername("guest");
		chParam.setStrPassword("guest");
		chParam.setStrSN(deviceID);
		new Thread()
		{	
			@Override
			public void run()
			{
				
				int nRet = csdk.Native_GHDSCClient_Connect(chParam);
				if(nRet == GHDSC_OK)
				{	
					queryvideoinfo(cloudStartTime, cloudEndTime);
				}
				
				mHandler.sendEmptyMessage(MSG_CLOUDUPDATE);
				//mHandler.sendEmptyMessage(MSG_SDREFRESH);
			}
		}.start();
	}

	private void createRemoteOperaction() {
		// TODO Auto-generated method stub
		Map<String, String> paramp = new HashMap<String, String>();
		paramp.put("UserName", "admin"); 
		paramp.put("Password", "admin"); 
		paramp.put("Id", deviceID);	
		Log.i(TAG, "createRemoteOperaction:deviceID"+deviceID+"-url:"+url);
		if(SDRecordFolderActivity.isLocal)
		{  
			mediaStreamer = new RemoteInteractionStreamer(url, paramp);	
		}
		else
		{
			mediaStreamer = MediaStreamFactory.createRemoteInteractionMediaStreamer(url, paramp);
		}
			
		if(mediaStreamer != null)
		{   
			//appData.setRemoteInteractionStreamer(mediaStreamer);
			mediaStreamer.open();
			mediaStreamer.setDevId(deviceID);
		}
		else
		{
			Log.i(TAG,"--mediaStreamer == null");
			//appData.setRemoteInteractionStreamer(null);
		}
	}

	private void findview() {
		progressView = findViewById(R.id.progress_get_devices_image);
		waitTextView = findViewById(R.id.loading);
		emptyView    = findViewById(R.id.emptyView);
		LinearLayout backLayout = (LinearLayout)findViewById(R.id.back_layout);
        backLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				backMainActivity();
			}
		});
		sdlist =  (ScollListView) findViewById(R.id.sdlist);
		cloudlist =  (ScollListView) findViewById(R.id.cloudlist);
		mFileList = new ArrayList<SDFileInfo>();
		mcloudFileList = new ArrayList<CloudFileInfo>();
		dlInfoList = new ArrayList<SDFileInfo>();
		dlHolderList = new ArrayList<SDRecordFileListAdapter.ViewHolder>();
		msdAdapter = new SDRecordFileListAdapter(getApplicationContext(), mFileList);
		sdlist.setAdapter(msdAdapter);
		mcloudAdapter = new CloudRecordFileListAdapter(getApplicationContext(), mcloudFileList);
		cloudlist.setAdapter(mcloudAdapter);	
	}

	private void getBundle() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		deviceID = intent.getStringExtra("devid");
		alarmTime = intent.getStringExtra("alarmTime");
		alarmUrl = intent.getStringExtra("alarmUrl");
	}
	
	private OnGetSDFileListListener mOnGetSDFileListListener = new OnGetSDFileListListener() {
		
		@Override
		public void onGetFileList(byte[] devbuf) {
			// TODO Auto-generated method stub
			synchronized (mFileList) {
				mFileList.clear();
			}
			strFileList = byteToString(devbuf);
			Log.d(TAG, strFileList);
			mHandler.sendEmptyMessage(MSG_UPDATE_SDDATA);
		}
	};
	
	public static String byteToString(byte[] src)
	{
		int len = 0;
		for (; len < src.length; len++)
		{
			if (src[len] == 0)
			{
				break;
			}
		}
		return new String(src, 0, len);
	}
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case MSG_SDUPDATE :
				msdAdapter.notifyDataSetChanged();
				progressView.setVisibility(View.INVISIBLE);
				waitTextView.setVisibility(View.INVISIBLE);
				emptyView.setVisibility(View.INVISIBLE);
				break;
				
			case MSG_CLOUDUPDATE :
				mcloudAdapter.notifyDataSetChanged();
				progressView.setVisibility(View.INVISIBLE);
				waitTextView.setVisibility(View.INVISIBLE);
				emptyView.setVisibility(View.INVISIBLE);
				break;
			case MSG_UPDATE_SDDATA :
				if(strFileList!=null){
					String []strItem = strFileList.split("\\|");
					Log.i(TAG, "--count of file:"+strFileList);
					
						for(int i=1; i<strItem.length; i++)
						{
							String []itemText = strItem[i].split(",");
							
							if(itemText.length != 2)
								continue;
							SDFileInfo tmp = new SDFileInfo();
							tmp.setSzDeviceid(deviceID);
							tmp.setSzFileName(itemText[0]);
			 				tmp.setnFileSize(Integer.parseInt(itemText[1]));
			 				if(!itemText[0].contains("tmp")){
			 					synchronized (mFileList) {
			 						mFileList.add(0, tmp);
								}
			 				}
						}
						mHandler.sendEmptyMessage(MSG_SDUPDATE);
				}
				break;
			case MSG_SDREFRESH:
				refreshsdfile();
				break;
			case MSG_PERCENT_PROCESSBAR:
			{	
				computepercent(msg.arg1);
			}
				break;	
			case MSG_CLOUD_PERCENT_PROCESSBAR:
				if(nFilePer <90)
					nFilePer +=10;
				else if(nFilePer < 99){
					nFilePer += 2;
				}else if(nFilePer>98){
					mCloudholder.numbar.setVisibility(View.GONE);
				}
				computecloudpercent(nFilePer);	
				
				//Log.d("Cloud Percent", ""+nFilePer);
				break;
			case MSG_NEXT_DOWNLOAD:
			{
				mcloudAdapter.notifyDataSetChanged();
				nextDownload();
			}
				break;
			case MSG_INVISABLEPROCESSBAR:
				mSelholder.numbar.setVisibility(View.VISIBLE);
				break;
			case MSG_DELETE_SUCCESS:
				toast.ToastShow(getApplicationContext(), getString(R.string._delete_success), 1000);
				refreshsdfile();
				break;	
			case MSG_STOP_CLOUDDOWNLOAD:
				isDownLoading =false;
				cloudfilestopdownload();		
				break;
			default:
				break;
			}
			
		}

		private void computecloudpercent(int nAddSize) {
			// TODO Auto-generated method stub
			mCloudholder.numbar.setProgress((int)nAddSize);
		}
		
	};
	
	public void refreshsdfile()
	{
		for(SDFileInfo info : dlInfoList){
			info.setWaitingForDl(false);
		}
		dlInfoList.clear();
		dlHolderList.clear();
		if(isDownLoading){
			removeDownload();
			isDownLoading = false;
		}
		progressView.setVisibility(View.VISIBLE);
		waitTextView.setVisibility(View.VISIBLE);
//		mAdapter.notifyDataSetChanged();
		if(mediaStreamer != null)
		{
			mediaStreamer.getSDFileListByDate(dataHour, startTime, endTime, deviceID);
			
		}
	}
	
	private void queryvideoinfo(String strStart, String strEnd)
	{
		String strWhere;
		strWhere = "video_time >= ";
		strWhere += strStart;
		strWhere += " and video_time <= ";
		strWhere += strEnd;
		
		chParam.setStrQueryVideoCountWhere(strWhere);
		if(GHDSC_OK == csdk.Native_GHDSCClient_Query_Count_Video(chParam))
		{	
			if(chParam.getlQueryVideoCount() != 0)
			{
				chParam.setStrQueryVideoDataWhere(strWhere);
				chParam.setStrQueryVideoDataColumn("video_time,video_type,video_status,video_size");
				chParam.setlQueryVideoDataPos(0);
				chParam.setlQueryVideoDataCount(chParam.getlQueryVideoCount());
				
				if(GHDSC_OK == csdk.Native_GHDSCClient_Query_Data_Video(chParam))
				{
					DealWithXml(HDS_VIDEO, chParam.getbQueryVideoData());
				}
			}
		}
	}
	
	void DealWithXml(int nType, byte[] buf)
	{
		switch(nType)
		{
			case HDS_EVENT:
				break;
			case HDS_VIDEO:
			{	
				parseVideoXml(buf);
			}
				break;
		
		}
	}
	
	void parseVideoXml(byte[] buf)
	{
		Log.i("AlamCloudRecordActivity", byteToString(buf));
		mFileList.clear();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
	    DocumentBuilder builder = null;
	    Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		String orgstring = null;
		try {
			orgstring = new String(buf,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String s = orgstring.substring(orgstring.length()/2);
		Log.i("AlamCloudRecordActivity",s );
		String xmlstring = "<?xml version=\"1.0\" encoding=\"utf-8\"?><videos>";
		xmlstring += orgstring;
		xmlstring += "</videos>";
		
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(xmlstring.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  
		
			try {
				doc = builder.parse(is);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		Element rootElement = null;
		if(doc != null)
		{  
			rootElement = doc.getDocumentElement();  
		}
		
		
	     NodeList items = null;
	     if(rootElement != null)
	     {	 
	    	 items =  rootElement.getElementsByTagName("video");  
	     }
	     
	     if(items == null)
	    	return;
	     
	     for (int i = 0; i < items.getLength(); i++) {  
	            CloudFileInfo info = new CloudFileInfo(); 
	            info.setSzDeviceid(deviceID);
	            Node item = items.item(i);  
	            NodeList properties = item.getChildNodes();  
	            for (int j = 0; j < properties.getLength(); j++) {  
	                Node property = properties.item(j);  
	                String nodeName = property.getNodeName();  
	                if (nodeName.equals("video_time")) {  
	                	info.setlTime(Long.parseLong(property.getFirstChild().getNodeValue()));
	                } else if (nodeName.equals("video_type")) {  
	                	info.setlType(Integer.parseInt(property.getFirstChild().getNodeValue()));
	                } else if (nodeName.equals("video_status")) {  
	                	info.setlStatus(Integer.parseInt(property.getFirstChild().getNodeValue()));
	                } else if (nodeName.equals("video_size")) {  
	                	info.setnFileSize(Long.parseLong(property.getFirstChild().getNodeValue()));
		            }   
	            }  
	            if(info.getnFileSize()/1024>20){
	            	 mcloudFileList.add(info);  
	            }
	        }  
	     
	     Collections.sort(mcloudFileList, new Comparator<CloudFileInfo>() {  
	            /** 
	             *  
	             * @param lhs 
	             * @param rhs 
	             * @return an integer < 0 if lhs is less than rhs, 0 if they are 
	             *         equal, and > 0 if lhs is greater than rhs,�Ƚ���ݴ�Сʱ,����ȵ���ʱ�� 
	             */  
	            @Override  
	            public int compare(CloudFileInfo lhs, CloudFileInfo rhs) {  
	                long date1 = lhs.getlTime(); 
	                long date2 = rhs.getlTime();   
	                // �������ֶν��������������ɲ���after����  
	                if (date1 > date2) {  
	                    return -1;  
	                }  
	                
	                if (date1 == date2) { 
	                	return 0;
	                }
	                return 1;  
	            }  
	        });  
	     mHandler.sendEmptyMessage(MSG_CLOUDUPDATE);
	}
	
	public void backMainActivity()
	{
		finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}
	
	private class SdItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			if(!isDownLoading){
			try {
				ViewHolder vhold = (ViewHolder)view.getTag();
				SDFileInfo tmp = (SDFileInfo)vhold.tvfilename.getTag();
				Log.i(TAG, "���item��"+position);	
				if(vhold.ivdownload.getVisibility() == View.VISIBLE)
				{
					Log.i(TAG, "�Ѽ��������б?"+position);
					msdAdapter.showbuttonType(vhold, 2);
					dlInfoList.add(tmp);
					dlHolderList.add(vhold);
					tmp.setWaitingForDl(true);
						isDownLoading = true;
						vhold.numbar.setVisibility(View.VISIBLE);
						new Thread(){
							@Override
							public void run() {
								// TODO Auto-generated method stub
								startdownload();	
							}
							
						}.start();
						
					return;
					
				}
				
				if(vhold.ivcanceldownload.getVisibility() == View.VISIBLE)
				{
					Log.i(TAG, "���Ƴ������б?"+position);
					msdAdapter.showbuttonType(vhold, 1);
					tmp.setWaitingForDl(false);
					if(tmp.getSzFileName().equals(mSelSDInfo.getSzFileName())){//ɾ���������ص��ļ�
						Log.i(TAG, "");
						removeDownload();
					}else{														//δ���ص��ļ��Ƴ����ض���
						dlInfoList.remove(tmp);
						dlHolderList.remove(vhold);
						tmp.setWaitingForDl(false);
					}
					
					return;
				}
				
				if(vhold.ivplay.getVisibility() == View.VISIBLE)
				{
					playVideo(tmp.getRealPath() + tmp.getSzFileName(),tmp.getSzFileName());
					msdAdapter.showbuttonType(vhold, 3);
					return;
				}
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
	}  
	
	private class CloudItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			if(!isDownLoading){
			com.my51c.see51.adapter.CloudRecordFileListAdapter.ViewHolder vhold = (com.my51c.see51.adapter.CloudRecordFileListAdapter.ViewHolder) view.getTag();
			mCloudholder = vhold;
			CloudFileInfo tmp = (CloudFileInfo)vhold.tvfilename.getTag();
			mSelCloudInfo = tmp;
			
			if(vhold.ivdownload.getVisibility() == View.VISIBLE)
			{
				isDownLoading=true;
				cloudfileStartdownload();
				mcloudAdapter.showbuttonType(vhold, 2);
				return;
			}
			
			if(vhold.ivcanceldownload.getVisibility() == View.VISIBLE)
			{
				cloudfilestopdownload();
				mcloudAdapter.showbuttonType(vhold, 1);
				return;
			}
			
			if(vhold.ivplay.getVisibility() == View.VISIBLE)
			{
				playVideo(tmp.getRealPath() + tmp.getRealFileName(),tmp.getRealFileName());
				mcloudAdapter.showbuttonType(vhold, 3);
				return;
			}
		}
		}
	}
	
	public void cloudfilestopdownload() {
		// TODO Auto-generated method stub
		if(clouddos != null)
		{
			try {
				clouddos.flush();
				clouddos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				clouddos = null;
				e.printStackTrace();
			}
		}
		stoptimer();
		clouddownload = false;
		mCloudholder.numbar.setVisibility(View.GONE);
		mSelCloudInfo.setbDown(clouddownload);
	
	}

	public void cloudfileStartdownload() {
		// TODO Auto-generated method stub
		cloudfilestopdownload();
		
		if(mSelCloudInfo == null)
		{
			return;
		}
		
		String videopath = mSelCloudInfo.getRealPath();
		File mVideoPath = new File(videopath);
		if(!mVideoPath.exists())
		{
			mVideoPath.mkdirs();
		}
		
		mCloudFileDownload = new File(mVideoPath, mSelCloudInfo.getRealFileName());
		chParam.setlDownloadVideoInfoTime(mSelCloudInfo.getlTime());
		chParam.setiDownloadVideoInfoType(mSelCloudInfo.getlType());
		
		
		try {
			clouddos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mCloudFileDownload)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			clouddos = null;
			e.printStackTrace();
		}
		clouddownload = true;
		mCloudholder.numbar.setVisibility(View.VISIBLE);
		mSelCloudInfo.setbDown(clouddownload);
		starttimer();
		new Thread()
		{	
			@Override
			public void run()
			{
	
				int nRet = csdk.Native_GHDSCClient_Download_Info_Video(chParam);
				if(nRet == GHDSC_OK)
				{				
					chParam.setlDownloadVideoDataContex(chParam.getlDownloadVideoInfoContex());
					chParam.setlDownloadVideoDataTime(mSelCloudInfo.getlTime());
					chParam.setiDownloadVideoDataType(mSelCloudInfo.getlType());
					chParam.setlDownloadVideoDataPos(0);
					chParam.setlDownloadVideoDataCount(mSelCloudInfo.getnFileSize());
					if(GHDSC_OK == csdk.Native_GHDSCClient_Download_data_Video(chParam))
					{	
						try {
							clouddos.write(chParam.getbDownloadVideoData());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mHandler.sendEmptyMessage(MSG_STOP_CLOUDDOWNLOAD);
						
					}
					
				}
				mHandler.sendEmptyMessage(MSG_CLOUDUPDATE);
			
			}
		}.start();
	

	}
	
	public void startdownload()
	{
		
		if(isDlRemove){
			createRemoteOperaction();
			if(mediaStreamer != null)
			{
				Log.i(TAG, "���´���TCP���ӣ��󶨼���");
				mediaStreamer.setOnGetSDFileListListener(mOnGetSDFileListListener);
				mediaStreamer.setOnGetSDFileDataListener(mOnGetSDFileDataListener);
				mediaStreamer.setOnGetFileDataOverListener(mOnGetFileDataOverListener);
			}
			isDlRemove = false;
		}
		mSelholder = dlHolderList.get(0);
		mSelSDInfo = dlInfoList.get(0);
		mSelSDInfo.setnCurSize(0);
		delNonCompleteVideo(mSelSDInfo);
		Log.i(TAG, "��ʼ���أ�"+mSelSDInfo.getnCurSize()*100/mSelSDInfo.getnFileSize());
		if(mSelSDInfo == null)
		{
			return;
		}
		String videopath = mSelSDInfo.getRealPath();
		File mVideoPath = new File(videopath);
		if(!mVideoPath.exists())
		{
			mVideoPath.mkdirs();
		}
		
		mFileDownload = new File(mVideoPath, mSelSDInfo.getSzFileName());
		if(mediaStreamer != null)
		{
			Log.i(TAG, "���������ļ���"+mSelSDInfo.getSzFileName());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mediaStreamer.getSDFileData( mSelSDInfo.getSzFileName(),deviceID, 0);
		}
		
		try {
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mFileDownload)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			dos = null;
			e.printStackTrace();
		}
		bdownload = true;
		mSelSDInfo.setbDown(bdownload);
		//mHandler.sendEmptyMessage(MSG_INVISABLEPROCESSBAR);
		//mSelholder.numbar.setVisibility(View.VISIBLE);
	}
	
	public void nextDownload()
	{	
		try {
			if(dos != null)
			{
				try {
					dos.flush();
					dos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					dos = null;
					e.printStackTrace();
				}
			}
			bdownload = false;
			mSelSDInfo.setbDown(bdownload);
			mSelholder.numbar.setVisibility(View.GONE);
			msdAdapter.showbuttonType(mSelholder, 3);
			if(dlInfoList.size()!=0){
				dlInfoList.remove(0);
				dlHolderList.remove(0);
			}
			if(dlInfoList.size()!=0){	
				mSelholder = dlHolderList.get(0);
				mSelholder.numbar.setVisibility(View.VISIBLE);
				new Thread(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
						startdownload();
					}
					
				}.start();
				isDownLoading = true;
			}else{
				Log.i(TAG, "ȫ��������ɣ������б�Ϊ��");
				isDownLoading = false;
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IndexOutOfBoundsException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}	
	
	public void removeDownload()
	{	
		isDlRemove = true;
		mediaStreamer.close();
		mediaStreamer.setOnGetSDFileListListener(null);
		mediaStreamer.setOnGetSDFileDataListener(null);
		mediaStreamer.setOnGetFileDataOverListener(null);
		mediaStreamer = null;
		if(dos != null)
		{
			try {
				dos.flush();
				dos.close();
				dos = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				dos = null;
				e.printStackTrace();
			}
		}
		bdownload = false;
		mSelSDInfo.setbDown(bdownload);
		mSelholder.numbar.setVisibility(View.GONE);
		delNonCompleteVideo(mSelSDInfo);
		if(dlInfoList.size()!=0){
			dlInfoList.remove(0);
			dlHolderList.remove(0);
		}
		if(dlInfoList.size()!=0){
			startdownload();
			isDownLoading = true;
		}else{
			Log.i(TAG, "ȫ��������ɣ������б�Ϊ��");
			isDownLoading = false;
		}
	}	
	
	private void playVideo(String fileName,String name) {
			
			//Log.d("open file", fileName);
			File file = new File(fileName);
		
			if (!file.exists()) {
				return;
			}
			if (fileName.endsWith(".jpg")) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), "image/jpeg");
				startActivity(intent);
			} else {
				Intent intent  = new Intent(this, PlayAcy.class);
				intent.putExtra("isFromSD", true);
				intent.putExtra("string", fileName);
				intent.putExtra("name", name);
				startActivity(intent);
			}
		}
	
	private OnGetSDFileDataListener mOnGetSDFileDataListener = new OnGetSDFileDataListener() {
	
		@Override
		public void onGetFileDataPiece(byte[] devbuf, long nPos) {
			// TODO Auto-generated method stub
			if(!isDlRemove){
				if(dos != null)
				{
					try {
						dos.write(devbuf);
						Message msg = mHandler.obtainMessage();
						msg.what = MSG_PERCENT_PROCESSBAR;
						msg.arg1 = devbuf.length;
						mHandler.sendMessage(msg);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	};
	
		private OnGetFileDataOverListener mOnGetFileDataOverListener = new OnGetFileDataOverListener() {
		
			@Override
			public void onGetFileDataOver() {
				// TODO Auto-generated method stub
				if(!isDlRemove){
					Log.i(TAG, "�������");
					mSelSDInfo.setWaitingForDl(false);
					mHandler.sendEmptyMessage(MSG_NEXT_DOWNLOAD);//nextDownload();	mAdapter.showbuttonType(mSelholder, 3);
				}
			}
		};
		
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

	public void delNonCompleteVideo(SDFileInfo mSDFileInfo){
		
		File f = new File(mSDFileInfo.getRealPath()+"/"+mSDFileInfo.getSzFileName());
		if (f.exists()) {
			f.delete();
			Log.i(TAG, "ɾ��δ��������ļ���"+mSDFileInfo.getSzFileName());
		}else{
			Log.i(TAG, "�ļ�������");
		}
	}

	public void computepercent(int nAddSize)//		buf+curSizw/totall size
	{
		long nTotalSize = mSelSDInfo.getnFileSize();
		long nCurSize   = mSelSDInfo.getnCurSize() + nAddSize;
		mSelSDInfo.setnCurSize(nCurSize);
		double dper = (double)nCurSize /nTotalSize*100;
		mSelholder.numbar.setProgress((int)dper);
	}
	
	private void starttimer()
	{	
		if(timer == null)
		{
			timer = new Timer();
		}
		
		if(timerTask == null)
		{
			timerTask = new TimerTask()
			{
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					mHandler.sendEmptyMessage(MSG_CLOUD_PERCENT_PROCESSBAR);
					//Log.d("Cloud Percent", "MSG_PERCENT_PROCESSBAR");
				}
			};
		}
		
		
	    if(timer != null && timerTask != null )  
	    	timer.schedule(timerTask, 0, 1000); 
	    
	    nFilePer = 0;
	}
	
	private void stoptimer()
	{
		if (timer != null) {  
			timer.cancel();  
			timer = null;  
        }  
  
        if (timerTask != null) {  
        	timerTask.cancel();  
        	timerTask = null;  
        }  

	}
}
