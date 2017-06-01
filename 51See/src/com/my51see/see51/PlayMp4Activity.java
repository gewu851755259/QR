package com.my51see.see51;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;
import com.sdview.view.R;

public class PlayMp4Activity extends Activity {
	
	private String path;
	private Uri uri;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_mp4);
		Intent intent =getIntent();
		path=intent.getStringExtra("string");
		uri=Uri.parse(path.toString());
		VideoView videoView = (VideoView)this.findViewById(R.id.Mp4VideoPlayer);  
	    videoView.setMediaController(new MediaController(this));  
	    videoView.setVideoURI(uri);  
	    videoView.start();  
	    videoView.requestFocus();  
	}
}
