package com.method.wifilist;

import java.util.List;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

public class ConnectWifi {
	
	WifiManager wifiManager;
	
	//构造函数
	 public ConnectWifi(WifiManager wifiManager)
	 {
		 this.wifiManager = wifiManager;
	 }

	// 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
	 public enum WifiCipherType {
		 WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	 }

	 // 提供一个外部接口，传入要连接的无线网ssid，password，
	 public void connect(String ssid, String password, WifiCipherType type) {
		 Thread thread = new Thread(new ConnectRunnable(ssid, password, type));
		 thread.start();
	 }

	 // 打开wifi功能
	 private boolean openWifi() {
		 boolean bRet = true;
		 if (!wifiManager.isWifiEnabled()) {
			 bRet = wifiManager.setWifiEnabled(true);
		 }
		 return bRet;
	 }
	    //启动一个新线程打开wifi
	 class ConnectRunnable implements Runnable {
		 private String ssid;

		 private String password;

		 private WifiCipherType type;

		 public ConnectRunnable(String ssid, String password, WifiCipherType type) {
			 this.ssid = ssid;
			 this.password = password;
			 this.type = type;
		 }

	  @Override
	  public void run() {
		  // 打开wifi
		  openWifi();
		  // 开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
		  // 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
		  while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
			  try {
				  // 为了避免程序一直while循环，让它睡个100毫秒检测……
				  Thread.sleep(100);
			  } catch (InterruptedException ie) {
			  }
		  }

		WifiConfiguration wifiConfig;
		//如果连接，将会断开重连
	   	WifiConfiguration tempConfig = isExsits(ssid);

	   	if (tempConfig == null) {
	   		wifiConfig = createWifiInfo(ssid, password, type);
	   		int netID = wifiManager.addNetwork(wifiConfig);
	   		boolean enabled = wifiManager.enableNetwork(netID, true);//第二个参数true表示如果当前已经有连上一个wifi,
																	//要强制连到自己设定的wifi上，此参数必须为true
																	//否则连上的还是原来的wifi.
	   	}else{
	   		wifiConfig = tempConfig;
	   		boolean b = wifiManager.enableNetwork(wifiConfig.networkId, true);
	   	}
	   	
//	   	boolean connected = wifiManager.reconnect();
//	   	Log.d(TAG, "enableNetwork connected=" + connected);
	  	}
	 }
	// 查看以前是否也配置过这个网络
		 private WifiConfiguration isExsits(String SSID) {
			 List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
			 for (WifiConfiguration existingConfig : existingConfigs) {
				 if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
					 return existingConfig;
				 }
			 }
			 return null;
		 }

		 private WifiConfiguration createWifiInfo(String SSID, String Password,WifiCipherType Type) {
			 WifiConfiguration config = new WifiConfiguration();
			 config.allowedAuthAlgorithms.clear();
			 config.allowedGroupCiphers.clear();
			 config.allowedKeyManagement.clear();
			 config.allowedPairwiseCiphers.clear();
			 config.allowedProtocols.clear();
			 config.SSID = "\"" + SSID + "\"";
		  // nopass
			 if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
				 config.wepKeys[0] = "";
				 config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				 config.wepTxKeyIndex = 0;
			 }
		  // wep
			 if (Type == WifiCipherType.WIFICIPHER_WEP) {
				 if (!TextUtils.isEmpty(Password)) {
					 if (isHexWepKey(Password)) {
						 config.wepKeys[0] = Password;
					 } else {
						 config.wepKeys[0] = "\"" + Password + "\"";
					 }
				 }
				 config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
				 config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
				 config.allowedKeyManagement.set(KeyMgmt.NONE);
				 config.wepTxKeyIndex = 0;
			 }
			 if (Type == WifiCipherType.WIFICIPHER_WPA) {
				 config.preSharedKey = "\"" + Password + "\"";
				 config.hiddenSSID = true;
				 config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
				 config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
				 config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				 config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
				 // 此处需要修改否则不能自动重联
				 // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
				 config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
				 config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
				 config.status = WifiConfiguration.Status.ENABLED;
			 }
			 return config;
		 }
	 
	 private static boolean isHexWepKey(String wepKey) {
		 final int len = wepKey.length();

		 // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
		 if (len != 10 && len != 26 && len != 58) {
			 	return false;
		 }

		 return isHex(wepKey);
	 }

	 private static boolean isHex(String key) {
		 for (int i = key.length() - 1; i >= 0; i--) {
			 final char c = key.charAt(i);
			 if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
					 && c <= 'f')) {
				 return false;
			 }
		 }

		 return true;
	 	}
}
