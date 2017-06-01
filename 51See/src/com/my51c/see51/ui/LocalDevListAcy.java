package com.my51c.see51.ui;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.my51c.see51.widget.ToastCommom;
import com.sdview.view.R;

public class LocalDevListAcy extends Activity implements OnItemClickListener,OnClickListener{
	
	private ListView devList;
	private LinearLayout backLayout;
	private ArrayList<File> fileList = null;
	private DevAdapter adapter;
	public  boolean isResume = false;
	private TextView editTx;
	private TextView selectAll;
	private RelativeLayout bottomL;
	private Button cancelBtn,delBtn;
	private ToastCommom toast = new ToastCommom();
	private ArrayList<SDFolderInfo> localFileList,deleteList = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sd_devlist_acy);
		findView();
		setDevList();
	}
	
	public void findView(){
		devList = (ListView)findViewById(R.id.dev_list);
		backLayout = (LinearLayout)findViewById(R.id.backLayout);
		editTx = (TextView)findViewById(R.id.edit);
		selectAll = (TextView)findViewById(R.id.selectAll);
		bottomL = (RelativeLayout)findViewById(R.id.bottomL);
		cancelBtn = (Button)findViewById(R.id.cancelBtn);
		delBtn = (Button)findViewById(R.id.okBtn);
		selectAll.setOnClickListener(this);
		editTx.setOnClickListener(this);
		backLayout.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		delBtn.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.backLayout:
			finish();
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			break;
		case R.id.edit:
			adapter.isChecked = true;
			bottomL.setVisibility(View.VISIBLE);
			editTx.setVisibility(View.GONE);
			selectAll.setVisibility(View.VISIBLE);
			for(int i=0;i<localFileList.size();i++){
				localFileList.get(i).setFlag(false);
			}
			adapter.notifyDataSetChanged();
			break;
		case R.id.selectAll:
			System.out.println("全选");
			for(int i=0;i<localFileList.size();i++){
				System.out.println("选择"+i);
				localFileList.get(i).setFlag(true);
			}
			adapter.notifyDataSetChanged();
			break;
		case R.id.cancelBtn:
			bottomL.setVisibility(View.GONE);
			editTx.setVisibility(View.VISIBLE);
			selectAll.setVisibility(View.GONE);
			adapter.isChecked = false;
			adapter.notifyDataSetChanged();
			break;
		case R.id.okBtn:
			deleteList = new ArrayList<SDFolderInfo>();
			for(int i=0;i<localFileList.size();i++){
				if(localFileList.get(i).isFlag()){
					deleteList.add(localFileList.get(i));
				}
			}
			if(deleteList.size()==0){
				toast.ToastShow(getApplicationContext(), getString(R.string._del_null), 1000);
			}else{
				showDelDialog();
			}
			break;

		default:
			break;
		}
	}
	
	public void showDelDialog(){
		final Dialog dialog = new Dialog(LocalDevListAcy.this, R.style.Erro_Dialog);
		dialog.setContentView(R.layout.del_dialog);
		Button sure = (Button)dialog.findViewById(R.id.del_ok);
		Button cancel = (Button)dialog.findViewById(R.id.del_cancel);
		TextView delTx = (TextView)dialog.findViewById(R.id.erroTx);
		delTx.setText(getResources().getString(R.string.delete_for_sure));
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		
		sure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				for(int i=0;i<deleteList.size();i++){
					deleteFolderFile(deleteList.get(i).getPath(), true);
				}
				localFileList.removeAll(deleteList);
				deleteList.clear();
				editTx.setVisibility(View.VISIBLE);
				selectAll.setVisibility(View.GONE);
				adapter.isChecked = false;
				bottomL.setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
				toast.ToastShow(getApplicationContext(), getString(R.string._delete_success), 1000);
			}
		});
		dialog.show();
	}
	
	@Override
	protected void onResume() {
		
		setDevList();
		super.onResume();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode == event.KEYCODE_BACK){
			finish();
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		// TODO Auto-generated method stub
		if(adapter.isChecked){
			ViewHolder holder = (ViewHolder)view.getTag();
			holder.checkBox.toggle();
			localFileList.get(position).setFlag(holder.checkBox.isChecked());
		}else{
			Intent intent = new Intent(LocalDevListAcy.this, LocalPicVideoAcy.class);
			intent.putExtra("isSD", true);
			intent.putExtra("devName", localFileList.get(position).getDevName());
			intent.putExtra("devPath", localFileList.get(position).getPath());
			startActivity(intent);
			overridePendingTransition(R.anim.slide_out_left , R.anim.slide_in_right);
		}
	}
	
	public class SDFolderInfo {
		private String path;
		private boolean Flag = false;
		private String devName;
		private int count;
		
		
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public boolean isFlag() {
			return Flag;
		}
		public void setFlag(boolean flag) {
			Flag = flag;
		}
		public String getDevName() {
			return devName;
		}
		public void setDevName(String devName) {
			this.devName = devName;
		}
	}
	
	private class ViewHolder{
		private CheckBox checkBox;
		private TextView name,count;
		private ImageView arrow;
	}
	
	public void setDevList(){
		localFileList = new ArrayList<SDFolderInfo>();
		devList.setOnItemClickListener(this);
		fileList = new ArrayList<File>();
		String path = Environment.getExternalStorageDirectory()+"/"+getResources().getString(R.string.app_name)+"/SDRecord";
		LocalPicVideoAcy.isFolderExists(path);
		File[] files = new File(path).listFiles();
		if(files.length!=0){
			for(int i=0;i<files.length;i++){
				if(files[i].isDirectory()){
					fileList.clear();
					GetFiles(getApplicationContext(), files[i].getAbsolutePath(), ".jpg", true);
					GetFiles(getApplicationContext(), files[i].getAbsolutePath(), ".mp4", true);
					if(fileList.size()==0){
						deleteFolderFile(files[i].getAbsolutePath(), true);
					}else{
						SDFolderInfo info = new SDFolderInfo();
						info.setDevName(files[i].getName());
						info.setPath(files[i].getAbsolutePath());
						info.setFlag(false);
						info.setCount(fileList.size());
						localFileList.add(info);
					}
				}
			}
		}
		adapter = new DevAdapter();
		devList.setAdapter(adapter);
	}
	
	
	private class DevAdapter extends BaseAdapter{

		private boolean isChecked = false;
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return localFileList.size();
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
			ViewHolder holder;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.local_folder_item, null);
				holder.name = (TextView)convertView.findViewById(R.id.local_folder_filename);
				holder.count = (TextView)convertView.findViewById(R.id.local_folder_count);
				holder.arrow = (ImageView)convertView.findViewById(R.id.arrow);
				holder.checkBox = (CheckBox)convertView.findViewById(R.id.checkBox);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
			}
			
			holder.name.setText(localFileList.get(position).getDevName());
			String path = localFileList.get(position).getPath();
			holder.checkBox.setVisibility(isChecked? View.VISIBLE:View.GONE);
			holder.checkBox.setChecked(localFileList.get(position).isFlag());
			holder.arrow.setVisibility(isChecked? View.GONE:View.VISIBLE);
			holder.count.setText(localFileList.get(position).getCount()+"");
			return convertView;
		}
		
	}
	public void GetFiles(Context context,String Path, String Extension,boolean IsIterative) //搜索目录，扩展名，是否进入子文件夹
    {
        try {
			File[] files =new File(Path).listFiles();
			files = new File(Path).listFiles();
			for (int i =0; i < files.length; i++)
			{
			    File f = files[i];
			    if (f.isFile())
			    {
			        if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension)) //判断扩展名
			        {
			        	fileList.add(f);
			        }
			        if (!IsIterative)
			            break;
			    }
			    else if (f.isDirectory() && f.getPath().indexOf("/.") == -1) //忽略点文件（隐藏文件/文件夹）
			        GetFiles(context,f.getPath(), Extension, IsIterative);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	 /**   
     * 删除指定目录下文件及目录    
     * @param deleteThisPath   
     * @param filepath   
     * @return    
     */     
    public void deleteFolderFile(String filePath, boolean deleteThisPath) {     
        if (!TextUtils.isEmpty(filePath)) {     
            try {  
                File file = new File(filePath);     
                if (file.isDirectory()) {// 处理目录     
                    File files[] = file.listFiles();     
                    for (int i = 0; i < files.length; i++) {     
                        deleteFolderFile(files[i].getAbsolutePath(), true);     
                    }      
                }     
                if (deleteThisPath) {     
                    if (!file.isDirectory()) {// 如果是文件，删除     
                        file.delete();     
                    } else {// 目录     
                   if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除     
                            file.delete();     
                        }     
                    }     
                }  
            } catch (Exception e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
            }     
        }     
    } 
    
}
