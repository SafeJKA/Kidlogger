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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;

public class ListFileSync {
	
	private Context context;	
	
	public ListFileSync(Context context){
		this.context = context;
	}
	
	public synchronized void addFileName(String fileName, boolean photoList){
		File f = getListFile(photoList);
		String tmp = fileName + "~";
		
		try{
			FileWriter fw = new FileWriter(f, true);
			fw.write(tmp);
			fw.close();
		}catch(IOException e){
			
		}
	}
	
	public synchronized String getFileName(boolean photoList){
		String[] fName = getListOfFiles(photoList);
		if(fName != null)
			return fName[0];
		
		return null;
	}
	
	public synchronized void removeFileName(boolean photoList){
		String[] fName = getListOfFiles(photoList);
		String tmp = new String();
		
		if(fName != null){
			if(fName.length > 1){
				for(int i = 1; i < fName.length; i++)
					tmp += fName[i] + "~";
			}
			
			try{
				FileWriter fw = new FileWriter(getListFile(photoList), false);
				fw.write(tmp);
				fw.close();
			}catch(IOException e){
				
			}
		}
	}
	
	public synchronized String[] getListOfPhotos(){
		return getListOfFiles(true);
	}
	
	protected String[] getListOfFiles(boolean photoList){
		File f = getListFile(photoList);
		String tmp = new String();
		String[] fName;
		int i = 0;
		
		if(f.exists()){
			try{
				FileReader fr = new FileReader(f);
				while((i = fr.read()) != -1)
					tmp += (char)i;
			}catch(IOException e){
				
			}
			
			if(tmp.length() != 0){
				fName = tmp.split("~");
				return fName;
			}
		}
		
		return null;
	}
	
	private File getListFile(boolean photoList){
		File path = context.getFilesDir();		
		if(photoList)
			return new File(path, "listPhotos.txt");
		else
			return new File(path, "listFiles.txt");
	}
}
