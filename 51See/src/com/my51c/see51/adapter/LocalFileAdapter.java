package com.my51c.see51.adapter;


import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.my51c.see51.widget.ListViewCheckableItem;
import com.sdview.view.R;

public class LocalFileAdapter extends BaseAdapter{
		
		private Context context;
		private ArrayList<File> fileList;
		private ArrayList<Bitmap> thumbNailList;
		private ListViewCheckableItem item;
		private boolean isChcked;
		private final String TAG = "LocalFileAdapter";
		
		public LocalFileAdapter(Context context,ArrayList<File> fileList,ArrayList<Bitmap> thumbNailList,boolean isChecked){
			this.context = context;
			this.fileList = fileList;
			this.thumbNailList = thumbNailList;
			this.isChcked = isChecked;
		}
		
		public void setSelectImg(boolean visible){
				item.setSelected(visible);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fileList.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			try {
//				if(convertView == null){
					item = new ListViewCheckableItem(context);
//				}else{
//					item = (ListViewCheckableItem)convertView;
//				}
				if(isChcked){
					item.setSelectImg(true);
				}else{
					item.setSelectImg(false);
				}
				
				  
				File f = fileList.get(position);
				DecimalFormat df = new DecimalFormat();
				String style = "0.00";//定义要显示的数字的格式
				df.applyPattern(style);// 将格式应用于格式化器
				
				item.setVideoLogo(f.getName().contains("mp4"));
				
				item.setFileName(f.getName());
				item.setFileSize(df.format(f.length()/1024.0/1024.0)+"M");
				if(df.format(f.length()/1024.0/1024.0).equals("0.00")){
					System.out.println("--------------length:"+f.length());
				}
//			根据时间差计算当前时间
				DateFormat dfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar date = Calendar.getInstance();
				date.setTimeInMillis(f.lastModified());
				item.setFileTime(dfTime.format(date.getTime()));
				item.setPreViewBitmap(thumbNailList.get(position));
				Log.i(TAG,"------f.lastModFied:"+dfTime.format(date.getTime()));
				
			} catch (IndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				System.out.println("--------------"+e);
				e.printStackTrace();
			}
			return item;
		}
		
	}