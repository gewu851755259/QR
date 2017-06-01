package com.my51c.see51.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.my51c.see51.data.LocalFileInfo;
import com.my51c.see51.data.ViewHolder;
import com.sdview.view.R;

public class FileAdapter extends BaseAdapter{

	private Context context;
	public boolean isChecked = false;
	public ArrayList<LocalFileInfo> fileList;
	public OnClickListener l;
	
	public FileAdapter(Context context,ArrayList<LocalFileInfo> fileList,OnClickListener l){
		this.context = context;
		this.fileList = fileList;
		this.l = l;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fileList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return fileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.localvideo_item, null);
			holder.timeTx = (TextView)convertView.findViewById(R.id.local_filetime);
			holder.sizeTx = (TextView)convertView.findViewById(R.id.local_filesize);
			holder.nameTx = (TextView)convertView.findViewById(R.id.local_filename);
			holder.thumbNail = (ImageView)convertView.findViewById(R.id.local_filepreview);
			holder.mp4Logo = (ImageView)convertView.findViewById(R.id.local_video_logo);
			holder.checkBox = (CheckBox)convertView.findViewById(R.id.checkBox);
			holder.shareLayout = (LinearLayout)convertView.findViewById(R.id.share);
			holder.delLayout = (LinearLayout)convertView.findViewById(R.id.delete);
			holder.shareLayout.setTag(position);
			holder.delLayout.setTag(position);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		holder.shareLayout.setOnClickListener(l);
		holder.delLayout.setOnClickListener(l);
		holder.timeTx.setText(fileList.get(position).getTime());
		holder.nameTx.setText(fileList.get(position).getName());
		holder.sizeTx.setText(fileList.get(position).getSize());
		holder.thumbNail.setImageBitmap(fileList.get(position).getThumbnail());
		holder.mp4Logo.setVisibility(fileList.get(position).isMp4() ? View.VISIBLE:View.GONE);
		holder.checkBox.setVisibility(isChecked ? View.VISIBLE:View.GONE);
		holder.checkBox.setChecked(fileList.get(position).isFlag());
		return convertView;
	}

}
