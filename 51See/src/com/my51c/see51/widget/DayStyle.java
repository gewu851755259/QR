package com.my51c.see51.widget;

import java.util.Calendar;

import android.content.Context;


/**
 * 日历控件样式绘制�?
 * @Description: 日历控件样式绘制�?

 * @FileName: DayStyle.java 

 * @Package com.calendar.demo 
 * @Author Hanyonglu

 * @Date 2012-3-18 下午03:33:42 

 * @Version V1.0
 */
public class DayStyle {
	
	
	public static int getWeekDay(int index, int iFirstDayOfWeek) {
		int iWeekDay = -1;

		if (iFirstDayOfWeek == Calendar.MONDAY) {
			iWeekDay = index + Calendar.MONDAY;
			
			if (iWeekDay > Calendar.SATURDAY)
				iWeekDay = Calendar.SUNDAY;
		}

		if (iFirstDayOfWeek == Calendar.SUNDAY) {
			iWeekDay = index + Calendar.SUNDAY;
		}

		return iWeekDay;
	}
}
