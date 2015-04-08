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

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class LogsTabHost extends TabActivity {
	
	@Override
	public void onCreate(Bundle saveInstanceState){
		super.onCreate(saveInstanceState);
		
		setContentView(R.layout.tab_host_activity);
		
		TabHost tabHost = getTabHost();
		TabSpec spec;
		Intent i;
		
		// Create an Intent to launch an Activity for the tab (to be reused)
		i = new Intent().setClass(this, ListLogFilesControl.class);
		
		// Initialize a TabSpec foe each tab and add it to the TabHost
		spec = tabHost.newTabSpec("html").setIndicator("List of logs").setContent(i);
		tabHost.addTab(spec);
		
		i = new Intent().setClass(this, AudioFilesList.class);
		spec = tabHost.newTabSpec("audio").setIndicator("Audio list").setContent(i);
		tabHost.addTab(spec);
	}
}
