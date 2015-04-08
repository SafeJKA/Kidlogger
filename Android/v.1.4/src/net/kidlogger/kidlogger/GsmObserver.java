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

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.gsm.GsmCellLocation;

public class GsmObserver extends PhoneStateListener {
	
	private final String CN = "GsmObserver";
	
	private KLService service;
	private String state;
	private String operator;
	private int pCell = -1;
	private int pLac = -1;
	
	
	public GsmObserver(KLService service){
		this.service = service;
	}
	
	@Override
	public void onServiceStateChanged(ServiceState sState){
		if(sState != null){
			super.onServiceStateChanged(sState);
			state = new String();
			operator = new String();
			int st = sState.getState();
			switch(st){
			case ServiceState.STATE_IN_SERVICE:
				String name = sState.getOperatorAlphaLong();
				state = "in service";
				if(name == null)
					operator = "undefined";
				else
					operator = name;			
				break;
			case ServiceState.STATE_OUT_OF_SERVICE:
				state = "out of service";
				break;
			case ServiceState.STATE_EMERGENCY_ONLY:
				state = "emergency only";
				break;
			case ServiceState.STATE_POWER_OFF:
				state = "explicitly powered off";
				break;
			}
			
			service.runGsmEvent(new String(state), new String(operator));			
		}		
	}
	
	@Override
	public void onCellLocationChanged(CellLocation location){
		super.onCellLocationChanged(location);
		
		if(Settings.loggingCell(service.getApplicationContext())){
			try{
				if(location instanceof GsmCellLocation){
					GsmCellLocation gsmLoc = (GsmCellLocation)location;
					if(checkLocation(gsmLoc))
						service.saveCellLoc(String.valueOf(pCell), String.valueOf(pLac));
				}
			}catch(Exception e){
				service.app.logError(CN + "onCellLocationChanged", e.toString());
			}
		}				
	}
	
	private boolean checkLocation(GsmCellLocation location){
		int cell = -1;
		int lac = -1;
		try{
			cell = location.getCid();
			lac = location.getLac();
		}catch(Exception e){
			cell = -1;
			lac = -1;
			service.app.logError(CN + "checkLocation", e.toString());
		}
		
		if(cell > 0 && lac > 0){
			if(cell != pCell || lac != pLac){
				pCell = cell;
				pLac = lac;
				return true;
			}else
				return false;
		}
		
		return false;
	}
}
