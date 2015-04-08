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

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.os.Bundle;

public class AirplaneReceiver extends BroadcastReceiver {
	private KLService service;
	
	public AirplaneReceiver(KLService service){
		this.service = service;
	}
	
	public void onReceive(Context context, Intent intent){
		String action = intent.getAction();
		Bundle extras = intent.getExtras();
		
		if(action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)){
			boolean state = extras.getBoolean("state");
			service.runAirEvent(state);
		}
	}
}
