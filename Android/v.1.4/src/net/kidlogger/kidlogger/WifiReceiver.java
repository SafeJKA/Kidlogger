/**********************************************************************
** Copyright (C) 2005-2011 Tesline-Service S.R.L.  All rights reserved.
**
** KidLogger - user activity monitoring software.
** 
**
** This file may be distributed and/or modified under the terms of the
** GNU General Public License version 2 as published by the Free Software
** Foundation and appearing in the file LICENSE.GPL included in the
** packaging of this file.
**
** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
**
** See http://www.kidlogger.net for GPL licensing information and terms of service
**
** Contact help@kidlogger.net if any conditions of this licensing are
** not clear to you.
**
**********************************************************************/
package net.kidlogger.kidlogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
//import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
//import android.net.wifi.ScanResult;
import android.net.NetworkInfo;
//import android.util.Log;

//import java.util.List;

public class WifiReceiver extends BroadcastReceiver {
	
	private KLService service;
	private String prevState = new String();
	
	//private String oldSsid = new String();
	//private WifiManager wifiMng = null;
	//private List<WifiConfiguration> wifiConfig;
	
	public WifiReceiver(KLService service){
		this.service = service;
	}
	
	public void onReceive(Context context, Intent intent){
		String action = intent.getAction();
		
		if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){			
			NetworkInfo ni = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if(ni != null){
				if(ni.getState() == NetworkInfo.State.CONNECTED){
					if(!prevState.equals("connected")){
						// Get SSID
						String ssid = new String();
						WifiManager wm = (WifiManager)service.getSystemService(Context.WIFI_SERVICE);
						if(wm != null){
							WifiInfo wi = wm.getConnectionInfo();
							ssid = wi.getSSID();
							if(ssid != null)
								ssid.replace("\"", "\\\"");
							else
								ssid = "undefined";
						}else
							ssid = "undefined";
						
						prevState = "connected";
						//sendEvent("Wifi " + prevState + ", SSID: " + ssid, false);
						sendEvent(new String(prevState), ssid);
					}										
				}else if(ni.getState() == NetworkInfo.State.DISCONNECTED){
					/*if(!prevState.equals("disconnected")){
						prevState = "disconnected";
						//sendEvent("Wifi " + prevState, false);
						sendEvent(new String(prevState));
					}*/					
				}
			}		
		}
				
		/*if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
			String bssId;
			
			NetworkInfo ni = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if(ni == null)
				return;
			else if(ni.isConnected()){
				bssId = intent.getStringExtra(WifiManager.EXTRA_BSSID);
				if(bssId != null)
					state = "Wifi is connected, BSSID: " + bssId;
				else
					state = "Wifi is connected";
			}else{
				state = "Wifi is disconnected";
			}
			
			sendEvent(state, false);
			
			getWifi(context);
			boolean wifiEnabled = wifiMng.isWifiEnabled();
			String wifiSummary = null;
			if(wifiEnabled){
				WifiInfo info = wifiMng.getConnectionInfo();
				if(info.getBSSID() != null){
					int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
					int speed = info.getLinkSpeed();
					String units = WifiInfo.LINK_SPEED_UNITS;
					String ssid = info.getSSID();
					wifiSummary = String.format("Connected to %s at %s%s. Strength %s/5",
							ssid, speed, units, strength);
				}
			}else{
				wifiSummary = "Disconnected.";
			}
			wifiMng = null;
			
			//NetworkInfo netInfo = (NetworkInfo)intent.getParcelableExtra(
			//		WifiManager.EXTRA_NETWORK_INFO);
			//if(netInfo.getState().equals(NetworkInfo.State.CONNECTED))
			//	service.runWifiEvent("WiFi is connected");
			//else
			//	service.runWifiEvent("WiFi id disconnected");
			
			//service.runWifiEvent(wifiSummary);
		}else if(action.equals(WifiManager.NETWORK_IDS_CHANGED_ACTION)){
					
		}else if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
			WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			if(wm != null){
				List<ScanResult> sr = wm.getScanResults();
				if(sr.size() > 0){
					String result = new String();
					String ssid = new String();
					for(ScanResult r : sr){
						result += "BSSID: " + r.BSSID + "|";
						result += "SSID: " + r.SSID + "|";
						ssid = r.SSID;
						//result += "Capabilities: " + r.capabilities + "|";
						//result += "Frequency: " + r.frequency + " MHz|";
						//result += "Level: " + r.level + " dBm|";
					}
					if(!ssid.equals(oldSsid)){
						oldSsid = ssid;
						sendEvent(result, true);						
					}					
				}
			}				
		}else if(action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)){
			//String state = new String();
			boolean connected = intent
				.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
			if(connected)
				state = "WiFi connected by user";
			else
				state = "WiFi disconnected by user";
			
			sendEvent(state, false);	
		}else if(action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
			
		}else if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
			int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
			
			if(wifiState != -1){
				String strState = new String();
				switch(wifiState){
				case WifiManager.WIFI_STATE_DISABLED:
					strState = "WiFi disabled";
					break;
				case WifiManager.WIFI_STATE_DISABLING:
					//strState = "WiFi is disabling";
					break;
				case WifiManager.WIFI_STATE_ENABLED:
					strState = "WiFi enabled";
					break;
				case WifiManager.WIFI_STATE_ENABLING:
					//strState = "WiFi is enabling";
					break;
				case WifiManager.WIFI_STATE_UNKNOWN:
					//strState = "WiFi state is unknown";
					break;
				}
				
				sendEvent(strState, false);
			}		
		}*/
	}
	
	/*private void sendEvent(final String state){
		new Thread(new Runnable(){
			public void run(){
				service.runWifiEvent(state);
			}
		}).start();
	}*/
	
	private void sendEvent(final String state, final String point){
		service.runWifiEvent(state, point);
	}
	
	/*protected void getWifi(Context context){
		if(wifiMng == null){
			wifiMng = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			wifiConfig = wifiMng.getConfiguredNetworks();
		}
	}*/
}
