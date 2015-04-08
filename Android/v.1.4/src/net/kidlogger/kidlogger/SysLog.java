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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
//import android.util.Log;

import android.util.Log;

public class SysLog{
	
	private final String CR = "\r\n";
	
	protected synchronized void writeLog (File path, String method, String message){
		StringBuilder sb = new StringBuilder();
		sb.append("Method: " + method + CR);
		sb.append("Error: ");
		sb.append(message.length() > 200 ? message.substring(0, 199) : message + CR);
		sb.append("Date time: " + getCurDate() + CR + CR);
		
		String fName = "errors.txt";
		File f = new File(path, fName);
		
		byte[] buff = sb.toString().getBytes();
		if(f.exists()){			
			try{
				boolean append;
				// Rewrite file error.txt if size of the file is larger then 100K
				if(f.length() > (1024L * 100L))
					append = false;
				else
					append = true;
				FileOutputStream fos = new FileOutputStream(f, append);
				fos.write(buff);
				fos.close();
			}catch(IOException e){
				//Log.i("SysLog", "Error: " + e.toString());
			}
		}else{
			try{
				boolean created = f.createNewFile();
				if(created){
					FileOutputStream fos = new FileOutputStream(f, false);
					fos.write(buff);
					fos.close();
				}
			}catch(IOException e){
				//Log.i("SysLog", "Error: " + e.toString());
			}
		}
	}
	
	protected synchronized StringBuilder readLog(File file){
		if(!file.exists())
			return null;
		
		StringBuilder sb = new StringBuilder();
		FileReader fr;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while((line = br.readLine()) != null){
				sb.append(line);
				sb.append(CR);
			}
		} catch (IOException e) {
			Log.e("SysLog.readLog", "Error: " + e.toString());
			return null;
		}
		
		return sb;
	}
	
	private String getCurDate(){
		long d = System.currentTimeMillis();
		return String.format("%tF %tT", d, d);
	}
}
