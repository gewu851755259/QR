package com.my51c.see51.listener;

public interface OnGetVideoConnectionStatusListener {
	public void onVideoConnection(String deviceid, int nIndex);
	public void onVideoDisconnection(String deviceid, int nIndex);
}
