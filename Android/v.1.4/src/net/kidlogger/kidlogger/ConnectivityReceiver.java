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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
//import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {
	
	private KLService service;
	//private long startThread = 0;
	
	public ConnectivityReceiver(KLService service){
		this.service = service;
	}
	
	// Implementation of onReceive method
	public void onReceive(Context context, Intent intent){
		//Log.i("ConnReceiver", "An action");
		if(intent == null){
			//Log.i("ConnReceiver", "Intent is null");
			return;
		}
			
		String action = intent.getAction();
		if(action == null){
			//Log.i("ConnReceiver", "Action is null");
			return;
		}			
		
		if(action.equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)){			
			NetworkInfo ni = (NetworkInfo)intent.getParcelableExtra(
					ConnectivityManager.EXTRA_NETWORK_INFO);
			if(ni == null){
				//Log.i("ConnReceiver", "NetworkInfo is null");
				return;
			}
			
			//Log.i("ConnReceiver", "Roaming is " + ni.isRoaming());
			if(ni.isRoaming()){
				service.mIsRoaming = true;
				service.runRoamingEvent();
			}else{
				service.mIsRoaming = false;
			}
			
			if(ni.getState() == NetworkInfo.State.CONNECTED){
				service.mNoConnectivity = false;				
				service.app.logError("Connectivity", "Internet connected");
				/*new Thread(new Runnable(){
				public void run(){
					long currTime = System.currentTimeMillis();
					long period = currTime - startThread;
					if(period > 180000L){
						startThread = currTime;
						service.doSendMedia();
						Log.i("ConnRec", "start thread");
					}
				}
				}).start();*/							
							
				/*int type = ni.getType();
				if(type == ConnectivityManager.TYPE_WIFI){
					
				}else if(type == ConnectivityManager.TYPE_MOBILE){
					
				}*/				
			}else if(ni.getState() == NetworkInfo.State.DISCONNECTED){
				service.mNoConnectivity = intent.getBooleanExtra(
						ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
				service.app.logError("Connectivity", "Internet disconnected");
			}
		}
	}
}
