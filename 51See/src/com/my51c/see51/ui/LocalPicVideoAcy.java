package com.my51c.see51.ui;


import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ.ShareParams;

import com.my51c.see51.adapter.FileAdapter;
import com.my51c.see51.data.LocalFileInfo;
import com.my51c.see51.data.ViewHolder;
import com.my51c.see51.widget.SweepLayout;
import com.my51c.see51.widget.SweepListView;
import com.my51c.see51.widget.ToastCommom;
import com.sdview.view.R;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzonePublish;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class LocalPicVideoAcy extends Activity implements OnClickListener,OnItemClickListener,PlatformActionListener{

	private final String TAG = "PicVideoAcy";
	private SweepListView listView;
	private RelativeLayout titleLayout;
	private LinearLayout backLayout;
	private TextView titleTx;
	private ArrayList<LocalFileInfo> localFileList,deleteList = null;
	private FileAdapter adapter;
	private TextView editTx;
	private TextView selectAll;
	private RelativeLayout bottomL;
	private Button cancelBtn,delBtn;
	private ToastCommom toast = new ToastCommom();
	private Tencent mTencent;
	String title;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pic_video_acy);
		title = getResources().getString(R.string.app_name);
		ShareSDK.initSDK(this);
		if (mTencent == null) {
	        mTencent = Tencent.createInstance("1105204735", this);
	    }
		findView();
		setListView();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	mTencent.onActivityResult(requestCode, resultCode, data);
    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ShareSDK.stopSDK(this); 
	}

	private void findView() {
		listView = (SweepListView)findViewById(R.id.pic_list);
		titleLayout = (RelativeLayout)findViewById(R.id.local_titleLayout);
		backLayout = (LinearLayout)findViewById(R.id.backLayout);
		titleTx = (TextView)findViewById(R.id.local_title);
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
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.backLayout:
			backMainActivity();
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
			for(int i=0;i<localFileList.size();i++){
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
		case R.id.share:
			int position = (Integer) view.getTag();
			int firVisible = listView.getFirstVisiblePosition();
			SweepLayout sweepLayout = (SweepLayout)listView.getChildAt(position-firVisible).findViewById(R.id.sweeplayout);
			sweepLayout.shrik(100);
			//showShareDialog(position);
			LocalFileInfo fileInfo = localFileList.get(position);
			ShareMore(fileInfo);
			break;
		case R.id.delete:
			int position1 = (Integer) view.getTag();
			int firVisible1 = listView.getFirstVisiblePosition();
			SweepLayout sweepLayout1 = (SweepLayout)listView.getChildAt(position1-firVisible1).findViewById(R.id.sweeplayout);
			sweepLayout1.shrik(100);
			deleteList.add(localFileList.get(position1));
			showDelDialog();
			break;

		default:
			break;
		}
	}
	
	public void showDelDialog(){
		final Dialog dialog = new Dialog(LocalPicVideoAcy.this, R.style.Erro_Dialog);
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
					File f = new File(deleteList.get(i).getPath());
					if (f.exists()) {
						f.delete();
					}else{
						Toast.makeText(getApplicationContext(), "文件删除失败",Toast.LENGTH_SHORT).show();
					}
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
	
	
	public void setListView(){
		final String videoPath,imgPath;
		
		boolean isSD = getIntent().getBooleanExtra("isSD", false);
		if(isSD){
			titleTx.setText(getIntent().getStringExtra("devName"));
			imgPath = videoPath = getIntent().getStringExtra("devPath");
			Log.i(TAG, "devPath:"+imgPath);
			isFolderExists(videoPath);
		}else{
			videoPath = Environment.getExternalStorageDirectory()+"/"+getResources().getString(R.string.app_name)+"/video";
			isFolderExists(videoPath);
			imgPath = Environment.getExternalStorageDirectory()+"/"+getResources().getString(R.string.app_name)+"/image";
			isFolderExists(imgPath);
		}
		deleteList = new ArrayList<LocalFileInfo>();
		localFileList = new ArrayList<LocalFileInfo>();
		GetFiles(this,videoPath, ".mp4", true);
		GetFiles(this,imgPath, ".jpg", true);
		adapter = new FileAdapter(getApplicationContext(), localFileList,this);
		listView.setOnItemClickListener(this);
		listView.setAdapter(adapter);
	}
	
	
	
	public void GetFiles(Context context,String Path, String Extension,boolean IsIterative) //搜索目录，扩展名，是否进入子文件夹
    {
        File[] files =new File(Path).listFiles();
        files = new File(Path).listFiles();
		Bitmap tempBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cam_default_icon);
        for (int i =0; i < files.length; i++)
        {
            File f = files[i];
            if (f.isFile())
            {
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension)) //判断扩展名
                {
                	if(f.getName().contains("jpg")){
                		tempBitmap = getImageThumbnail(f.getAbsolutePath(), 60, 60);
                		Log.i(TAG, "jpg"+i);
                	}else{
                		Log.i(TAG, "mp4"+i);
                		tempBitmap = getVideoThumbnail(f.getAbsolutePath(), 60, 60, MediaStore.Images.Thumbnails.MICRO_KIND);
                	}
                	LocalFileInfo info = new LocalFileInfo();
        			DecimalFormat df = new DecimalFormat();
        			String style = "0.00";//定义要显示的数字的格式
        			df.applyPattern(style);// 将格式应用于格式化器
        			DateFormat dfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        			Calendar date = Calendar.getInstance();
        			date.setTimeInMillis(f.lastModified());
        			info.setTime(dfTime.format(date.getTime()));
        			info.setMp4(f.getName().contains("mp4"));
        			info.setSize(df.format(f.length()/1024.0/1024.0)+"M");
        			info.setThumbnail(tempBitmap);
        			info.setName(f.getName());
        			info.setPath(f.getAbsolutePath());
        			info.setFlag(false);
        			localFileList.add(info);
                }
                if (!IsIterative)
                    break;
            }
            else if (f.isDirectory() && f.getPath().indexOf("/.") == -1) //忽略点文件（隐藏文件/文件夹）
                GetFiles(context,f.getPath(), Extension, IsIterative);
        }
    }
	
	public static void isFolderExists(String strFolder)
    {
        File file = new File(strFolder);
        
        if (!file.exists())
        {
            file.mkdirs();
            System.out.println("新建文件夹成功");
        }else{
        	System.out.println("文件夹已存在");
        }
    }
	
	public Bitmap getImageThumbnail(String imagePath, int width, int height) {  
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cam_default_icon);  
        BitmapFactory.Options options = new BitmapFactory.Options();  
        options.inJustDecodeBounds = true;  
        // 获取这个图片的宽和高，注意此处的bitmap为null  
        bitmap = BitmapFactory.decodeFile(imagePath, options);  
        options.inJustDecodeBounds = false; // 设为 false  
        // 计算缩放比  
        int h = options.outHeight;  
        int w = options.outWidth;  
        int beWidth = w / width;  
        int beHeight = h / height;  
        int be = 1;  
        if (beWidth < beHeight) {  
            be = beWidth;  
        } else {  
            be = beHeight;  
        }  
        if (be <= 0) {  
            be = 1;  
        }  
        options.inSampleSize = be;  
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false  
        bitmap = BitmapFactory.decodeFile(imagePath, options);  
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象  
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,  
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
        return bitmap;  
    }  
	
	public Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {  
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cam_default_icon);  
        // 获取视频的缩略图  
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);  
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
        return bitmap;  
    } 
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		// TODO Auto-generated method stub
		if(adapter.isChecked){
			ViewHolder holder = (ViewHolder)view.getTag();
			holder.checkBox.toggle();
			localFileList.get(position).setFlag(holder.checkBox.isChecked());
		}else{
			String path = localFileList.get(position).getPath();
			boolean isjpg = localFileList.get(position).getName().contains("jpg");
			Log.i(TAG, "onItemClick:"+path+"-"+isjpg);
			if(isjpg){
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(path)), "image/jpeg");
				startActivity(intent);
			}else{
				Intent intent = new Intent(LocalPicVideoAcy.this, PlayAcy.class);
				intent.putExtra("string", path);
				intent.putExtra("name", localFileList.get(position).getName());
				startActivity(intent);
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode == event.KEYCODE_BACK){
			backMainActivity();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		System.gc();
		super.onStop();
	}

	public void backMainActivity(){
		finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}

    public void showShareDialog(int position){
		
		final Dialog shareDialog = new Dialog(LocalPicVideoAcy.this, R.style.Erro_Dialog);
		shareDialog.setContentView(R.layout.dialog_share);
		Window window = shareDialog.getWindow();  
	    window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置  
	    window.setWindowAnimations(R.style.dialog_animation_style);  
	    /*设置dialog宽度占满屏幕*/
	    DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams lp = shareDialog.getWindow().getAttributes();
	    lp.width = (int)(dm.widthPixels); //设置宽度
	    shareDialog.getWindow().setAttributes(lp);
	    shareDialog.show();
	    
	    LinearLayout qqShare = (LinearLayout)shareDialog.findViewById(R.id.qq_share);
	    LinearLayout wechatShare = (LinearLayout)shareDialog.findViewById(R.id.wechat_share);
	    LinearLayout sinaShare = (LinearLayout)shareDialog.findViewById(R.id.sina_share);
	    LinearLayout moreShare = (LinearLayout)shareDialog.findViewById(R.id.more_share);
	    
	    final LocalFileInfo fileInfo = localFileList.get(position);
	    qqShare.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				QQshare(fileInfo);
				shareDialog.dismiss();
			}
		});
	    wechatShare.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				WechatShare(fileInfo);
				shareDialog.dismiss();
			}
		});
	    sinaShare.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SinaShare(fileInfo);
				shareDialog.dismiss();
			}
		});
	    moreShare.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ShareMore(fileInfo);
				shareDialog.dismiss();
			}
		});
	}
	
	public void ShareMore(LocalFileInfo fileInfo){
    	Intent intent=new Intent(Intent.ACTION_SEND);  
        if(fileInfo.isMp4()==false){
       	 	intent.setType("image/*");  
        }else{
       	 	intent.setType("video/*");  
        }
        
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(fileInfo.getPath())));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        startActivity(Intent.createChooser(intent, title));  
//        if(fileInfo.isMp4()==false){
//
//        	String imagePath = fileInfo.getPath();
//        	Uri imageUri = Uri.fromFile(new File(imagePath));
//
//        	Intent shareIntent = new Intent();
//        	shareIntent.setAction(Intent.ACTION_SEND);
//        	shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
//        	shareIntent.setType("image/*");
//        	startActivity(Intent.createChooser(shareIntent, "分享到"));
//        }else{
//        	String videoPath = fileInfo.getPath();
//        	Uri videoUri = Uri.fromFile(new File(videoPath));
//        	Intent shareIntent = new Intent();
//        	shareIntent.setAction(Intent.ACTION_SEND);
//        	shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
//        	shareIntent.setType("video/*");
//        	startActivity(Intent.createChooser(shareIntent, "分享到"));
//        }
    }
	
	public void WechatShare(LocalFileInfo fileInfo){
		ShareParams sp = new ShareParams();
		sp.setTitle(title);
		sp.setText(fileInfo.getName());
	    sp.setTitleUrl(fileInfo.getPath()); 
        sp.setImagePath(fileInfo.getPath());
	    sp.setSite(title);
	    sp.setSiteUrl(fileInfo.getPath());
		Platform qq = ShareSDK.getPlatform(getApplicationContext(), "Wechat");
	    qq.setPlatformActionListener(this);
	    qq.share(sp);
	}
	
	public void SinaShare(LocalFileInfo fileInfo){
		ShareParams sp = new ShareParams();
		sp.setTitle(title);
		sp.setText(fileInfo.getName());
	    sp.setTitleUrl(fileInfo.getPath()); 
        sp.setImagePath(fileInfo.getPath());
	    sp.setSite(title);
	    sp.setSiteUrl(fileInfo.getPath());
		Platform qq = ShareSDK.getPlatform(getApplicationContext(), "SinaWeibo");
	    qq.setPlatformActionListener(this);
	    qq.share(sp);
	}
	
	private void QQshare(LocalFileInfo fileInfo){
   	 	final Bundle params = new Bundle();
   	 	Message msg = mHandler.obtainMessage();
   	 	if(fileInfo.getName().contains(".jpg"))
   	 	{
   	 		msg.what = 1;
   	 		params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, fileInfo.getPath());
   	 		params.putString(QQShare.SHARE_TO_QQ_APP_NAME, title);
   	 		params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
   	 		params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, 0x00);
   	 	}
   	 	else{
   	 		msg.what = 2;
   	 		params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHVIDEO);
   	 		params.putString(QzonePublish.PUBLISH_TO_QZONE_VIDEO_PATH,fileInfo.getPath());
   	 		params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, title);
   	 	}
   	 	// QQ分享要在主线程做
 		msg.setData(params);
 		msg.sendToTarget();
   }
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 1:
				Bundle params = msg.getData();
				mTencent.shareToQQ(LocalPicVideoAcy.this, params, qqShareListener); 
				break;
			case 2:
				Bundle params1 = msg.getData();
				mTencent.publishToQzone(LocalPicVideoAcy.this, params1, qqShareListener); 
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	IUiListener qqShareListener = new IUiListener() {
        @Override
        public void onCancel() {
        	toast.ToastShow(getApplicationContext(), getString(R.string.share_cancel), 1000);
        }
        @Override
        public void onComplete(Object response) {
            // TODO Auto-generated method stub
        	toast.ToastShow(getApplicationContext(), getString(R.string.share_finish), 1000);
        }
        @Override
        public void onError(UiError e) {
            // TODO Auto-generated method stub
        	toast.ToastShow(getApplicationContext(), getString(R.string.share_erro), 1000);
        }
    };

	@Override
	public void onCancel(Platform arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(Platform arg0, int arg1, Throwable arg2) {
		// TODO Auto-generated method stub
		
	}	
}
