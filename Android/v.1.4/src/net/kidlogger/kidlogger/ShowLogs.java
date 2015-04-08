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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.widget.TextView;
import android.widget.Toast;
//import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
//import android.util.Log;

public class ShowLogs extends Activity {
	
	private final int MENU_ITEM_SEND = 0;
	private final int MENU_ITEM_PREVIEW = 1;
	
	WebView webContent;	
	private String file;
	private String logFile;
		
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_logs);		
		
		
		webContent = (WebView)findViewById(R.id.web_content);		
		registerForContextMenu(webContent);
		
		Bundle extras = getIntent().getExtras();
		file = extras.getString("logFile");
		
		TextView fileName = (TextView)findViewById(R.id.file_name);
		fileName.setText(file);
		
		// Get name of html file show in webview
		logFile = file.substring(0, file.indexOf(" ("));
		
		String f = "file://" + getFilesDir().getAbsolutePath();
		f += "/" + logFile;
		readLogFile(f);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		webContent.clearCache(true);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		
		menu.setHeaderTitle(getString(R.string.webview_context_menu_title));
		menu.add(0, v.getId(), MENU_ITEM_PREVIEW, 
				getString(R.string.webview_context_menu_view_html));
		menu.add(0, v.getId(), MENU_ITEM_SEND, 
				getString(R.string.webview_context_menu_send_email));
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		if(item.getOrder() == MENU_ITEM_PREVIEW)
			previewHtml();
		else if(item.getOrder() == MENU_ITEM_SEND)
			sendEmail();
		else
			return false;
		
		return true;
	}
	
	private void sendEmail(){
		String subject = "Android " + Build.VERSION.RELEASE + " (" +
			Settings.getApiVersion(this) + ") Log file";		
		File f = new File(getApplicationContext().getFilesDir()
				.getAbsolutePath() + "/" + logFile);
		try{
			String body = logFile + "\r\n\r\n" + readFile(getApplicationContext()
					.getFilesDir().getAbsolutePath() + "/" + logFile);
			final Intent email = new Intent(Intent.ACTION_SEND);
			email.setType("text/plain");
			email.putExtra(Intent.EXTRA_EMAIL, new String[]{"help@kidlogger.net"});			
			email.putExtra(Intent.EXTRA_SUBJECT, subject);
			email.putExtra(Intent.EXTRA_TEXT, body);			
			startActivity(Intent.createChooser(email, "Send mail..."));			
		}catch(IOException e){
			Toast.makeText(this, "Error: " + e, Toast.LENGTH_LONG).show();
		}
	}
	
	private void previewHtml(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		try{
			builder.setMessage(readFile(getApplicationContext().getFilesDir()
					.getAbsolutePath() + "/" + logFile))
					.setCancelable(true)
					.setPositiveButton("OK", new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int id){
							dialog.cancel();
						}
					}).show();
		}catch(IOException e){
			Toast.makeText(this, "Error: " + e, Toast.LENGTH_LONG).show();
		}
	}

	private void readLogFile(String fn){
		File css = new File(getFilesDir().getAbsolutePath(), "style.css");
		if(!css.exists()){
			copyCss(css.getName());
			//Log.i("SLogs", "copy css");
		}
		webContent.loadUrl(fn);
	}
	
	// Copy CSS file from assets
	private void copyCss(String fileName){
		AssetManager am = getAssets();
		InputStream in = null;
		OutputStream out = null;
		byte[] buff = new byte[1024];
		int read = 0;
		try{
			in = am.open(fileName);
			out = new FileOutputStream(getFilesDir().getAbsolutePath() + "/" + fileName);
			while((read = in.read(buff)) != -1){
				out.write(buff, 0, read);
			}
			in.close();
			out.flush();
			out.close();			
		}catch(IOException e){
			TApplication app = (TApplication)getApplication();
			app.logError("SLogs", e.toString());
		}
	}
	
	private String readFile(String path) throws IOException {
		FileInputStream stream = new FileInputStream(new File(path));
		try{
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			return Charset.defaultCharset().decode(bb).toString();
		}finally{
			stream.close();
		}
	}
}
