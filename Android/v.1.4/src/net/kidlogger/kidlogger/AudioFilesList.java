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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class AudioFilesList extends ListActivity {
	
	private File currentDir;
	private String rootDir;
	private FileArrayAdapter adapter;
	
	@Override
	public void onCreate(Bundle saveInstanceState){
		super.onCreate(saveInstanceState);
		
		rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() +
			"/.callrecords/";
		currentDir = new File(rootDir);
		fill(currentDir);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id){
		Option o = adapter.getItem(position);
		if(o.getData().equalsIgnoreCase("Folder") ||
				o.getData().equalsIgnoreCase("Up a level")){
			currentDir = new File(o.getPath());
			fill(currentDir);
		}else{
			playFile(new File(o.getPath()));
			//Log.i("AFL", "path: " + o.getPath());
		}
	}
	
	private void playFile(File file){
		if(!file.exists())
			return;
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "audio/3gpp");
		
		try{
			startActivity(intent);
		}catch(ActivityNotFoundException e){
			
		}
	}
	
	private void fill(File file){
		File[] dirs = file.listFiles();
		
		List<Option> dir = new ArrayList<Option>();
		List<Option> fls = new ArrayList<Option>();
		
		try{
			for(File f : dirs){
				if(f.isDirectory())
					dir.add(new Option(f.getName(), "Folder", f.getAbsolutePath()));
				else
					fls.add(new Option(f.getName(), "File size: " + f.length(),
							f.getAbsolutePath()));
			}
		}catch(Exception e){
			
		}
		
		Collections.sort(dir);
		Collections.sort(fls);
		
		dir.addAll(fls);
		
		if(!file.getName().equalsIgnoreCase(".callrecords"))
			dir.add(0, new Option("..", "Up a level", file.getParent()));
		
		adapter = new FileArrayAdapter(AudioFilesList.this, R.layout.file_view, dir);
		this.setListAdapter(adapter);
	}
}
