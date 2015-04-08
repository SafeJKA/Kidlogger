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
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;
//import android.util.Log;

public class CallsReceiver extends BroadcastReceiver {
	
	protected final static String NEW_CALL = "New call: ";
	
	private final String PHONE_STATE = "android.intent.action.PHONE_STATE";
	private KLService service;
	private String number = new String();
	private String callType = new String();
	private boolean inCall = false;
	private boolean offhook = false;
	//private boolean logAudio = false;
	private boolean recInCalls = false;
	//private boolean recOutCalls = false;
	
	public CallsReceiver(KLService service){
		this.service = service;
		Context context = service.getApplicationContext();
		//logAudio = Settings.loggingAudioCalls(context);
		recInCalls = Settings.recordInCalls(context);
		//recOutCalls = Settings.recordOutCalls(context);
	}
	
	public void onReceive(Context context, Intent intent){
		String action = intent.getAction();	
		
		if(action.equals(PHONE_STATE)){
			Bundle bundle = intent.getExtras();
			if(bundle == null)
				return;			
			String state = bundle.getString(TelephonyManager.EXTRA_STATE);
			if(state == null)
				return;
			
			if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)){
				number = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
				inCall = true;
				//Log.i("CallReceiver", "Ringing, number: " + number);
			}else if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)){
				//Log.i("CallReceiver", "start event EXTRA_STATE_OFFHOOK");
				offhook = true;
				if(inCall && recInCalls){
					if(!service.recording){
						service.startRecord("in_", new String(number));
					}
				}
				//Log.i("CallReceiver", "finish event EXTRA_STATE_OFFHOOK");
				//Log.i("CallReceiver", "Off hook");
			}else if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)){
				//Log.i("CallReceiver", "start event EXTRA_STATE_IDLE");
				// Stop recording 
				if(service.recording){
					service.stopRecord();
				}
				
				if(inCall){
					if(offhook){
						callType = "Incoming call: ";
						offhook = false;
					}else
						callType = "Missed call: ";
					
					inCall = false;
					service.runCallEvent(new String(callType), new String(number), true);				
				}else{
					service.stopLogNewCall();			
				}
				//Log.i("CallReceiver", "finish event EXTRA_STATE_IDLE");
				//Log.i("CallReceiver", "Idle");
			}
		}else if(action.equals(Intent.ACTION_NEW_OUTGOING_CALL)){
			//Log.i("CallsReceiver", "start event NEW_OUTGOING_CALL");
			number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			if(number != null){
				if(number.equals(KLService.INTENT_NUMBER)){
					// End call
					setResultData(null);
					//service.runCallEvent(null, number, false);
					service.runCallEvent(null, new String(number), false);
				}else{
					callType = NEW_CALL;
					service.runCallEvent(new String(callType), new String(number), false);				
				}
			}
			//Log.i("CallReceiver", "finish event NEW_OUTGOING_CALL");
		}
	}
}
