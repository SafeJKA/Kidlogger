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
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.Bundle;
//import android.util.Log;

public class ListLogFilesControl extends ListActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.list_activity_view);		
		
		File dir = getFilesDir();
		FilenameFilter filter = new FilenameFilter(){
			public boolean accept(File dir, String name){
				return name.contains(".htm");
			}
		};
		
		File[] tmp = dir.listFiles(filter);
		ArrayList<String> names = new ArrayList<String>();
		String s;
		if(tmp != null){
			for(File f : tmp){
				s = f.getName();
				s += String.format(" (%.2f kb)", f.length()/1000F);		
				names.add(s);
			}
			
			String[] files = names.toArray(new String[names.size()]);
			Arrays.sort(files);
			
			// Show list of log files
			this.setListAdapter(new ArrayAdapter<String>(this,
					R.layout.one_line_list_item, R.id.file_name, files));
		}		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		super.onListItemClick(l, v, position, id);
		
		Object o = this.getListAdapter().getItem(position);
		Intent i = new Intent(this, ShowLogs.class);
		i.putExtra("logFile", o.toString());
		startActivity(i);
		
		//Log.i("ListView", o.toString());
	}
}
