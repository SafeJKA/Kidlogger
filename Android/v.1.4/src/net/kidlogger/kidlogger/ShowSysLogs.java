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
import java.io.FileInputStream;
import java.io.IOException;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
//import android.util.Log;

public class ShowSysLogs extends Activity {
	
	protected String content;
	TextView output;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.show_sys_logs);		
		output = (TextView)findViewById(R.id.logs_view);
		Button sendLogs = (Button)findViewById(R.id.send_logs_button);
		sendLogs.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				String subject = "Android " + Build.VERSION.RELEASE + " (" + 
					Settings.getApiVersion(ShowSysLogs.this) + ") SysLog";
				File f = new File(getFilesDir(), "errors.txt");
				final Intent email = new Intent(Intent.ACTION_SEND);
				email.setType("text/plain");
				email.putExtra(Intent.EXTRA_EMAIL, new String[]{"help@kidlogger.net"});
				//email.putExtra(Intent.EXTRA_EMAIL, new String[]{"yurasr@yahoo.com"});
				email.putExtra(Intent.EXTRA_SUBJECT, subject);
				email.putExtra(Intent.EXTRA_TEXT, ShowSysLogs.this.content);
				//email.putExtra(Intent.EXTRA_STREAM, 
				//		Uri.parse("file:///" + f.getAbsoluteFile().toString()));				
				ShowSysLogs.this.startActivity(Intent.createChooser(email,
						"Send mail ..."));
				
				output.setText("");
				f.delete();
			}
		});
		
		String subject = "Android " + Build.VERSION.RELEASE + " (" + 
			Settings.getApiVersion(this) + ") Log";
		//Log.i("SSL", subject);		
		
		sendLogs.setEnabled(showLogs());
	}	
	
	private boolean showLogs(){		
		TApplication app = (TApplication)getApplication();
		
		content = app.readLog();
		if(content == null)
			return false;
		
		output.setText(content);
		
		//File f = new File(getFilesDir(), "errors.txt");
		
		/*if(f.exists()){
			content = new StringBuffer("");
			int ch;
			try{
				FileInputStream fis = new FileInputStream(f);
				while((ch = fis.read()) != -1){
					content.append((char)ch);
				}
				fis.close();
				output.setText(content.toString());
			}catch(IOException e){
				//Log.i("SSL", "Error: " + e.toString());
			}
		}else{
			return false;
		}*/
		
		return true;
	}
}
