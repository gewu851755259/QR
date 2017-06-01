package com.my51c.see51.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.inputmethodservice.Keyboard.Key;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sdview.view.R;
import com.my51c.see51.adapter.ExpandableAdapter;

public class LocalFileAcy extends Activity implements OnClickListener{
	
	private final String TAG = "LocalVideoAcy";
	public static final int UNDATE_GRIDVIEW = 0;
	private ImageView picImg, sd_picImg;
	private LinearLayout backLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_video_acy);
		findView();
	}

	private void findView() {
		
		picImg = (ImageView)findViewById(R.id.pic_preview);
		sd_picImg = (ImageView)findViewById(R.id.sd_pic_preview);
		backLayout = (LinearLayout)findViewById(R.id.backLayout);
		
		picImg.setOnClickListener(this);
		sd_picImg.setOnClickListener(this);
		backLayout.setOnClickListener(this);
	}

	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.pic_preview:
			startActivity(new Intent(this, LocalPicVideoAcy.class));
			overridePendingTransition(R.anim.slide_out_left , R.anim.slide_in_right);
			
			break;
		case R.id.sd_pic_preview:
			startActivity(new Intent(this, LocalDevListAcy.class));
			overridePendingTransition(R.anim.slide_out_left , R.anim.slide_in_right);
			break;
		case R.id.backLayout:
			finish();
//			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode == event.KEYCODE_BACK){
			finish();
//			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	
}
