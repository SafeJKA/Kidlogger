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
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Images;
import android.util.Log;

public class PhotoObserver extends ContentObserver {
	
	private KLService service;
	private Context context;
	private int photos = 0; // present number photos in folder
	private Uri uri;
	
	public PhotoObserver(KLService service, Context context, Handler h){
		super(h);
		this.service = service;
		this.context = context;			
		uri = Images.Media.EXTERNAL_CONTENT_URI;
		photos = getNumPhotos();
		this.context.getContentResolver().registerContentObserver(uri, true, this);
	}
	
	@Override
	public boolean deliverSelfNotifications(){
		return false;
	}
	
	@Override
	public void onChange(boolean arg0){
		super.onChange(arg0);
		
		String selection = MediaColumns.MIME_TYPE + "=\'image/jpeg\'";
		Cursor cursor = context.getContentResolver().query(uri, null, selection, null, 
				"date_added DESC");
		if(cursor != null){
			int num = cursor.getCount();
			if(cursor.moveToNext() && photos != -1 && num > photos){				
				final String fPath = cursor.getString(cursor.getColumnIndex(
						MediaColumns.DATA));
				final String fName = cursor.getString(cursor.getColumnIndex(
						MediaColumns.DISPLAY_NAME));
				final String fMime = cursor.getString(cursor.getColumnIndex(
						MediaColumns.MIME_TYPE));
				
				// Check path if contains DCIM directory
				CharSequence cs = Environment.DIRECTORY_DCIM;
				if(fPath.contains(cs)){
					service.runPhotoEvent(fPath, fName, fMime);
				}
			}			
			cursor.close();
			photos = num;			
		}		
	}
	
	public void unregisterObserver(){
		context.getContentResolver().unregisterContentObserver(this);
	}
	
	private int getNumPhotos(){
		String selection = MediaColumns.MIME_TYPE + "=\'image/jpeg\'";
		Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
		int num = 0;
		if(cursor == null)
			return -1;
		
		num = cursor.getCount();
		cursor.close();
			
		return num;
	}
}
