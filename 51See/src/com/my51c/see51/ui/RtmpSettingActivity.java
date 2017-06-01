package com.my51c.see51.ui;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.my51c.see51.listener.OnGetDevRtmpListener;
import com.my51c.see51.media.RemoteInteractionStreamer;
import com.my51c.see51.service.AppData;
import com.my51c.see51.widget.ToastCommom;
import com.sdview.view.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;

public class RtmpSettingActivity extends SherlockFragmentActivity {
	
	private EditText editRtmpadress;
	private RemoteInteractionStreamer mediastream;
	private AppData appData;
	private LinearLayout btnBack;
	private ToastCommom toast = new ToastCommom();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rtmpsettingacy);
		appData = (AppData) getApplication();
		editRtmpadress=(EditText)findViewById(R.id.edit_rtmpadress);
		btnBack=(LinearLayout)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				RtmpSettingActivity.this.finish();
			}
		});
	}

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mediastream = appData.getRemoteInteractionStreamer();
		mediastream.setmOnGetDevRtmpListener(mOnGetDevRtmpListener);
	}
	
	/**
	 * 开
	 * 2017-3-2上午9:50:32
	 * @author ZXY
	 * @param v
	 */
	public void yesBtn(View v){
		String rtmpUrl=editRtmpadress.getText().toString().trim();
		//if(LocalSettingActivity.mediastream != null)
		if(rtmpUrl!=null){
			mediastream.setDevRtmp(true, rtmpUrl);
		}else{
			
		}
	}
	
	/**
	 * 关
	 * 2017-3-2上午9:50:57
	 * @author ZXY
	 * @param view
	 */
	public void noBtn(View view){
		mediastream.setDevRtmp(false, "");
	}
	private Handler mHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				toast.ToastShow(RtmpSettingActivity.this, "开启成功", 1000);
				break;

			case 1:
				toast.ToastShow(RtmpSettingActivity.this, "关闭成功", 1000);
				
				break;
			default:
				break;
			}
			
		};
	};
	
	private OnGetDevRtmpListener mOnGetDevRtmpListener=new OnGetDevRtmpListener(){

		@Override
		public void onGetDevRtmpFiled() {
			mHandler.sendEmptyMessage(1);
		}


		@Override
		public void onGetDevRtmpSuccess() {
			mHandler.sendEmptyMessage(0);
			
		}
		
	};
}
