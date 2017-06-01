package com.method.wifilist;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

public class WifiAdmin {  
    // 定义WifiManager对象   
    public static WifiManager mWifiManager;  
    // 定义WifiInfo对象   
    private WifiInfo mWifiInfo;  
    // 扫描出的网络连接列表   
    private List<ScanResult> mWifiList;  
    // 网络连接列表   
    private List<WifiConfiguration> mWifiConfiguration;  
    // 定义一个WifiLock   
    WifiLock mWifiLock;  
 
  
    // 构造器   
    public WifiAdmin(Context context) {  
        // 取得WifiManager对象   
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
        // 取得WifiInfo对象   
        mWifiInfo = mWifiManager.getConnectionInfo();  
    }  
  
    // 打开WIFI   
    public void openWifi() {  
        if (!mWifiManager.isWifiEnabled()) {  
            mWifiManager.setWifiEnabled(true);  
        }  
    }  
  
    // 关闭WIFI   
    public void closeWifi() {  
        if (mWifiManager.isWifiEnabled()) {  
            mWifiManager.setWifiEnabled(false);  
        }  
    }  
  
    // 检查当前WIFI状态   
    public int checkState() {  
        return mWifiManager.getWifiState();  
    }  
    
    // 创建一个WifiLock   
    public void creatWifiLock() {  
        mWifiLock = mWifiManager.createWifiLock("Test");  
    }
  
    // 锁定WifiLock   
    public void acquireWifiLock() {  		//如果应用程序想在屏幕被关掉后继续使用WiFi，
    	mWifiLock.acquire();				//则可以调用 acquireWifiLock来锁住WiFi，该操作会阻止WiFi进入睡眠状态。
    }  
  
    // 解锁WifiLock   
    public void releaseWifiLock() {  		//当应用程序不再使用WiFi时需要调用 releaseWifiLock来释放WiFi。
    	// 判断时候锁定   						//之后WiFi可以进入睡眠状态以节省电源
        if (mWifiLock.isHeld()) {  
            mWifiLock.acquire();  
        }  
    } 						 		
         
    // 得到配置好的网络   
    public List<WifiConfiguration> getConfiguration() {  
        return mWifiConfiguration;  
    }  
  
    // 指定配置好的网络进行连接   
    public void connectConfiguration(int index) {  
        // 索引大于配置好的网络索引返回   
        if (index > mWifiConfiguration.size()) {  
            return;  
        }  
        // 连接配置好的指定ID的网络   
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);  
    }  
  
    public void startScan() {  
        mWifiManager.startScan();  
        // 得到扫描结果   
        mWifiList = mWifiManager.getScanResults();  
        // 得到配置好的网络连接   
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();  
    }  
  
    // 得到网络列表   
    public List<ScanResult> getWifiList() {  
        return mWifiList;  
    }  
  
    // 查看扫描结果   
    public StringBuilder lookUpScan(List<ScanResult> list) {  
        StringBuilder stringBuilder = new StringBuilder();  
        for (int i = 0; i < list.size(); i++) {  
            stringBuilder.append("Index_" + new Integer(i + 1).toString() + ":");  
            // 将ScanResult信息转换成一个字符串包   
            // 其中把包括：BSSID、SSID、capabilities、frequency、level   
            stringBuilder.append((list.get(i)).toString()); 
            stringBuilder.append("/n");  
        }  
        return stringBuilder;  
    } 
  
    // 得到MAC地址   
    public String getMacAddress() {  
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();  
    }  
  
    // 得到接入点的BSSID   
    public String getBSSID() {  
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();  
    }  
    
    public String getSSID(){
    	return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID(); 
    }
  
    // 得到IP地址   
    public int getIPAddress() {  
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();  
    }  
  
    // 得到连接的ID   
    public int getNetworkId() {  
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();  
    }  
  
    // 得到WifiInfo的所有信息包   
    public String getWifiInfo() {  
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();  
    }  
    
    public WifiManager getWifiManager(){
    	return mWifiManager;
    }
  
    // 添加一个网络并连接   
    public void addNetwork(WifiConfiguration wcg) {  
     int wcgID = mWifiManager.addNetwork(wcg);  
     boolean b =  mWifiManager.enableNetwork(wcgID, true);  
     System.out.println("a--" + wcgID); 
     System.out.println("b--" + b); 
    }  
  
    // 断开指定ID的网络   
    public void disconnectWifi(int netId) {  
        mWifiManager.disableNetwork(netId);  
        mWifiManager.disconnect();  
    }  

}