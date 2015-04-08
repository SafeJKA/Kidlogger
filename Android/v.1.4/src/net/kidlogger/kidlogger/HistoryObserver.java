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
import android.os.Handler;
import android.provider.Browser;
import android.provider.Browser.BookmarkColumns;

public class HistoryObserver extends ContentObserver {
	private Context context;
	private String prevUrl = "";
	
	public HistoryObserver(Context context, Handler h){
		super(h);
		this.context = context;
		this.context.getContentResolver().registerContentObserver(Browser.BOOKMARKS_URI,
				true, this);
	}
	
	@Override
	public boolean deliverSelfNotifications(){
		return false;
	}
	
	@Override
	public void onChange(boolean arg){
		super.onChange(arg);
		
		//StringBuilder sb = new StringBuilder();
		
		// SELECT _id, url, visits, date, bookmark, title, favicon, thumbnail, 
		// touch_icon, user_entered, _id AS _id FROM bookmarks
		/*
		String selection = Browser.HISTORY_PROJECTION[9] + "=1"; // user_entered=1
		String order = Browser.HISTORY_PROJECTION[3] + " DESC";
		Cursor cur = context.getContentResolver().query(Browser.BOOKMARKS_URI, 
				Browser.HISTORY_PROJECTION, selection, null, order);
		if(cur.moveToFirst() && cur.getCount() > 0){
			String url = cur.getString(Browser.HISTORY_PROJECTION_URL_INDEX);
			
			cur.moveToFirst();
			String urls = "";
			do{
				urls += cur.getString(Browser.HISTORY_PROJECTION_URL_INDEX) + "\r\n";
			}while(cur.moveToNext());
			
			cur.close();			
			//Log.i("HObserver", urls);
			if(!prevUrl.equalsIgnoreCase(url)){
				prevUrl = url;
				((KLService)context).runHistoryEvent(url);
			}			
		}
		*/
		Cursor cr = context.getContentResolver().query(Browser.BOOKMARKS_URI, 
				new String[] {BookmarkColumns.URL, BookmarkColumns.DATE}, 
				"bookmark=0", null, BookmarkColumns.DATE + " DESC");
		
		if(cr.getCount() > 0){
			cr.moveToFirst();
			final String url = cr.getString(cr.getColumnIndex(BookmarkColumns.URL));			
			
			if(!prevUrl.equalsIgnoreCase(url)){
				//prevUrl = url;
				prevUrl = new String(url);
				new Thread(new Runnable(){
					public void run(){
						((KLService)context).runHistoryEvent(url);
					}
				}).start();
				//((KLService)context).runHistoryEvent(url);
			}
			/*long date = 0;
			do{
				ur = cr.getString(cr.getColumnIndex(BookmarkColumns.URL));
				date = cr.getLong(cr.getColumnIndex("date"));
				//Log.i("HObs", ur + " " + String.valueOf(date));
			}while(cr.moveToNext());*/
			
		}
		cr.close();
		
	}
	
	public void unregisterObserver(){
		context.getContentResolver().unregisterContentObserver(this);
	}
}
