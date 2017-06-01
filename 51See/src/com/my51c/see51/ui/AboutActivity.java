package com.my51c.see51.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.sdview.view.R;
import com.my51c.see51.service.AppData;

public class AboutActivity extends SherlockFragmentActivity {
	
	private AppData appData;
	private LinearLayout back;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.about_content);
//		setActionBar();
		appData = (AppData) this.getApplication();
		//appData.addUIActivity(new WeakReference<Activity>(this));
		back = (LinearLayout)findViewById(R.id.backLayout);
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				AboutActivity.this.finish();
			}
		});
	}
	
	public void setActionBar(){
		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		Drawable titleDrawable = getResources().getDrawable(R.drawable.title_bg);
		actionBar.setBackgroundDrawable(titleDrawable);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// TODO Auto-generated method stub
		switch (item.getItemId())
		{

		case android.R.id.home:
			// Do whatever you want, e.g. finish()
			AboutActivity.this.finish();
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
