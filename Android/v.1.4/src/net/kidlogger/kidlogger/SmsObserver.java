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
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.telephony.SmsMessage;

//import android.util.Log;

public class SmsObserver extends ContentObserver {
	private Context mContext;
	private static final Uri SMS_URI = Uri.parse("content://sms/");
	private final int SMS_SENT = 2;
	private final int ADDRESS = 0;
	private final int TYPE = 1;
	private final int BODY = 2;
	
	private boolean incomingSms = false;
	
	public SmsObserver(Context context, Handler h){
		super(h);
		mContext = context;
		mContext.getContentResolver().registerContentObserver(SMS_URI, true, this);
	}
	
	@Override
	public boolean deliverSelfNotifications(){
		return false;
	}
	
	@Override
	public void onChange(boolean arg0){
		super.onChange(arg0);
		//Log.i("SmsObserver", "onChange");
		if(!incomingSms){			
			new Thread(new Runnable(){
				public void run(){
					String[] columns = new String[]{"address", "type", "body"};
					Cursor cur = mContext.getContentResolver().query(SMS_URI, columns, null, null, null);
					if(cur != null){
						cur.moveToNext();
						//String protocol = cur.getString(cur.getColumnIndex("protocol"));
						int type = cur.getInt(TYPE);
						//Log.i("SmsObserver", "type: " + type);
						if(type == SMS_SENT){
							String address = cur.getString(ADDRESS);
							String body = cur.getString(BODY);
							((KLService)mContext).runSmsEvent(address, body, false);				
						}
						cur.close();
					}
				}
			}).start();			
		}else
			incomingSms = false;
						
	}
	
	public void unregisterObserver(){
		mContext.getContentResolver().unregisterContentObserver(this);
	}
	
	// Define a BroadcastReceiver to detect incoming SMS
	public BroadcastReceiver inSms = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			final Bundle bundle = intent.getExtras();		
			
			if(action.equals(KLService.SMS_RECEIVED)){
				//Log.i("SmsObserver", "onReceive");
				incomingSms = true;
				if(bundle != null){
					new Thread(new Runnable(){
						public void run(){
							try{
								Object[] pdus = (Object[])bundle.get("pdus");
								for(int i = 0; i < pdus.length; i++){
									SmsMessage messages = SmsMessage.createFromPdu((byte[])pdus[i]);
									String address = messages.getOriginatingAddress();
									String body = messages.getDisplayMessageBody();
									//writeSmsLog(address, body);
									((KLService)mContext).runSmsEvent(address, body, true);
								}
							}catch(NullPointerException e){
								((KLService)mContext).app.logError("SmsObserver.onReceive", e.toString());
							}
						}
					}).start();															 
				}
			}
		}
	};
}
