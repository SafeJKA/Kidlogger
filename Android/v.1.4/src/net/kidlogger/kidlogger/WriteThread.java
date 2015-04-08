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

public class WriteThread implements Runnable {
	private KLService.Synchronizer s;
	private String fileType;
	Thread thread;
	private byte[] buff;
	
	
	public WriteThread(KLService.Synchronizer s, String fileType, byte[] buff){
		this.s = s;
		this.fileType = fileType;
		this.buff = buff;
		thread = new Thread(this);
		thread.start();
	}
	
	public void run(){
		//Log.i("WriteThread", "start thread: " + fileType);
		//s.writeLog(fileType, buff);
		s.writeLog(new String(fileType), buff);
		//Log.i("WriteThread", "finish thread: " + fileType);
	}
}
