package net.kidlogger.kidlogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
	
	public void onReceive(Context context, Intent intent) {
		if(context == null || intent == null)
			return;
		
		String action = intent.getAction();
		if(action == null)
			return;
		
		if(action.equals(KLService.ALARM_ACTION)){
			TApplication app = (TApplication)context.getApplicationContext();
			if(app.mService != null){
				app.mService.doUploadData();
			}
		}		
	}

}
