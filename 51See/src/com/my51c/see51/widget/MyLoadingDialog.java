package com.my51c.see51.widget;

import com.sdview.view.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyLoadingDialog extends Dialog {
	private TextView tv;

	public MyLoadingDialog(Context context) {
		super(context, R.style.loadingDialogStyle);
	}

	private MyLoadingDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_dialog);
		tv = (TextView)findViewById(R.id.tx);
		tv.setText(R.string.rf_loading_mention);
	}
	
	public void setContent(String str)
	{
		tv.setText(str);
	}
	
	public String getContent()
	{
		return tv.getText().toString();
	}
	
	public void setTextViewVisible(boolean bshow)
	{
		if(bshow)
		{
			tv.setVisibility(View.VISIBLE);
		}
		else
		{
			tv.setVisibility(View.INVISIBLE);
		}
	}
}

