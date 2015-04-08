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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SliceMultimediaFile {
	
	private final String CN = "SliceMultimediaFile.";
	private KLService service;
	private int bytesRead = 0;
	
	public SliceMultimediaFile(KLService service){
		this.service = service;
	}
	
	protected File getPiceOfFile(String file, long pointer){
		RandomAccessFile raf = null;
		File f = new File(file);
		if(!f.exists())
			return null;
		File out = new File(service.getBaseContext().getFilesDir(), f.getName());
		byte[] tmp = new byte[KLService.MEDIA_BUFF]; // 20k buffer 
		
		try{
			raf = new RandomAccessFile(f, "r");
			if(pointer != 0){
				raf.seek(pointer);
			}
			
			bytesRead = raf.read(tmp);
			if(bytesRead != -1){
				FileOutputStream fos = new FileOutputStream(out, false);
				fos.write(tmp, 0, bytesRead);
				fos.close();
			}			
			raf.close();
						
			if(bytesRead == -1){
				return null;
			}			
		}catch(IOException e){
			service.app.logError(CN + "getPiceOfFile", e.toString());
			return null;
		}		
		
		return out;
	}
	
	protected String getMimeType(String file){
		String ext = file.substring(file.length() - 3);
		if(ext.equalsIgnoreCase("jpg"))
			return "image/jpeg";
		else if(ext.equalsIgnoreCase("3gp"))
			return "audio/3gpp";
		else
			return null;		
	}
	
	protected long getBytesRead(){
		return (long)bytesRead;
	}
}
