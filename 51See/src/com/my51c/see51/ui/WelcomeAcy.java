package com.my51c.see51.ui;

import com.sdview.view.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class WelcomeAcy extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcomeacy);
		welcomeHandler.sendEmptyMessageDelayed(0, 2000);
	}

	Handler welcomeHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			startActivity(new Intent(WelcomeAcy.this, MainActivity.class));
			finish();
		}
	};
}
