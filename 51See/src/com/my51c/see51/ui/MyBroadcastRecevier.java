package com.my51c.see51.ui;

import com.my51c.see51.service.AppData;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 监听退出广播
 * @author 51c
 *
 */
public class MyBroadcastRecevier extends BroadcastReceiver {

	   private static final String LOG_TAG = "HomeReceiver";
	    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
	    private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
	    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
	    private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
	    private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";
	@Override
	public void onReceive(Context context, Intent intent) {

		AppData appData=(AppData)context.getApplicationContext();
		String action=intent.getAction();
		System.out.println("收到的action"+action);
		if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
			 String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
			   if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
	                // 短按Home键
	                Log.i(LOG_TAG, "homekey");
	                
	        		appData.exit();

	            }
	            else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
	                // 长按Home键 或者 activity切换键
	                Log.i(LOG_TAG, "long press home key or activity switch");

	            }
	            else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
	                // 锁屏
	                Log.i(LOG_TAG, "lock");
	            }
	            else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
	                // samsung 长按Home键
	                Log.i(LOG_TAG, "assist");
	            }
		}
		
		
	}

}
