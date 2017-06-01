package com.my51c.see51.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ScollListView extends ListView {
	public ScollListView(Context context, AttributeSet attrs)

	{

	super(context, attrs);

	}

	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)

	{

	int mExpandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,

	MeasureSpec.AT_MOST);

	super.onMeasure(widthMeasureSpec, mExpandSpec);

	} 

}
