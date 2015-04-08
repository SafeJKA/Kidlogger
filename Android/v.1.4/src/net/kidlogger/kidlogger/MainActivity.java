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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
//import android.util.Log;

import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements OnClickListener {
	
	static final String DIALOG_ID = "dialog_id";
	static final int NO_DIALOG = -1;
	static final int NOTICE_DIALOG = 1;
	static final int LICENSE_DIALOG = 2;
	static final int PICK_SETTINGS = 3;
	protected static boolean prefChanged = false;
	
	private TextView logState;
	private TextView licenseLink;
    private ImageView showLog;
    private Button startButton;
    private View setButton;
    private int currentDialogId = NO_DIALOG;
    
    private boolean isRunning = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check the password if is sent
        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null){
        	String pass = bundle.getString("password");
        	if(!checkPassword(pass))
        		finish();
        }
        
        setContentView(R.layout.main); 
        
        logState = (TextView)findViewById(R.id.service_state);        
                
        // Set up listener for all buttons
        startButton = (Button)findViewById(R.id.start_button);
        startButton.setOnClickListener(this);
        
        setButton = findViewById(R.id.set_button);
        setButton.setOnClickListener(this);
        
        View exitButton = findViewById(R.id.preview_button);
        exitButton.setOnClickListener(this);
        
        View aboutButton = findViewById(R.id.about_button);
        aboutButton.setOnClickListener(this);
        
        showLog = (ImageView)findViewById(R.id.show_logging);
        showLog.setImageResource(R.drawable.logging);
        
        // Create clickable text for TextView
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(getString(R.string.license_link));
        ssb.setSpan(new ClickableSpan(){
        	public void onClick(View view){
        		MainActivity.this.showDialog(LICENSE_DIALOG);
        	}
        }, 0, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        licenseLink = (TextView)findViewById(R.id.license);
        licenseLink.setText(ssb);
        licenseLink.setMovementMethod(LinkMovementMethod.getInstance());
        licenseLink.setLinkTextColor(Color.BLUE);
        
        //licenseLink.setOnClickListener(this);
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	showServiceStatus();
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    }
    
    public void onClick(View v){
    	switch(v.getId()){
    	case R.id.start_button:
    		if(isRunning)
    			stopLogging();
    		else
    			startLogging();
    		break;
    	case R.id.set_button:
    		startActivityForResult(new Intent(this, Settings.class), PICK_SETTINGS);
    		break;
    	case R.id.preview_button:
    		//Intent i = new Intent(this, ListLogFilesControl.class);
    		Intent i = new Intent(this, LogsTabHost.class);
    		startActivity(i);
    		break;    		
    	case R.id.about_button:
    		Intent a = new Intent(this, About.class);
    		startActivity(a);
    		break; 
    	case R.id.license:
    		showDialog(LICENSE_DIALOG);
    		//showLicense();
    		break;
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case R.id.troubleshoot:
    		startActivity(new Intent(this, ShowSysLogs.class));
    	}
    	return false;
    }
    
    // Gets information back from Settings.class 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);   	
    	if(requestCode == PICK_SETTINGS){
    		if(prefChanged && isRunning){
    			prefChanged = false;
    			showDialog(NOTICE_DIALOG);
    			//String ms = getString(R.string.kl_dialog_message);
    			//alertDialog(ms);
    		}else{
    			if(prefChanged)
    				prefChanged = false;
    		}
    	}
    }
    
    @Override
    protected Dialog onCreateDialog(int id){
    	switch(id){
    	case NOTICE_DIALOG:
    		return new AlertDialog.Builder(MainActivity.this)
    			.setTitle(getString(R.string.dialog_title_warning))
    			.setMessage("")
    			.setIcon(android.R.drawable.ic_dialog_alert)
    			.setPositiveButton("OK", new DialogInterface.OnClickListener(){
    				public void onClick(DialogInterface dialog, int id){
    					MainActivity.this.removeCurrentDialog();
    				}
    			})
    			.setOnCancelListener(new DialogInterface.OnCancelListener(){
    				public void onCancel(DialogInterface dialog){
    					MainActivity.this.removeCurrentDialog();
    				}
    			})
    			.create();
    		
    	case LICENSE_DIALOG:    		
    		TextView text = new TextView(this);
    		text.setAutoLinkMask(Linkify.ALL);
    		text.setPadding(10, 10, 10, 10);
    		try{
    			text.setText(readLicenseFile());
    		}catch(IOException e){
    			text.setText("Couldn't read license file.");
    		}
    		ScrollView sView = new ScrollView(this);
    		sView.addView(text);
    		
    		return new AlertDialog.Builder(MainActivity.this)
    			.setTitle("License Agreement")
    			.setIcon(android.R.drawable.ic_dialog_info)
    			.setView(sView)
    			.setOnCancelListener(new DialogInterface.OnCancelListener(){
    				public void onCancel(DialogInterface dialog){
    					MainActivity.this.removeCurrentDialog();   					
    				}
    			})
    			.create();
    	}
    	
    	return null;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle){    	
    	switch(id){
    	case NOTICE_DIALOG:
    		((AlertDialog)dialog).setMessage(getString(R.string.kl_dialog_message));
    		break;
    	}
    	currentDialogId = id;
    	//Log.i("KidLogger", "prepareDialog, id: " + id);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
    	super.onSaveInstanceState(savedInstanceState);
    	
    	if(currentDialogId != NO_DIALOG){
    		removeDialog(currentDialogId);
    		if(savedInstanceState != null)
    			savedInstanceState.putInt(DIALOG_ID, currentDialogId);
    	}
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
    	super.onRestoreInstanceState(savedInstanceState);
    	  	
    	if(savedInstanceState != null)
    		currentDialogId = savedInstanceState.getInt(DIALOG_ID);
    	
    	if(currentDialogId != NO_DIALOG){
    		showDialog(currentDialogId);
    	}
    }
    
    protected void removeCurrentDialog(){
    	removeDialog(currentDialogId);
    	currentDialogId = NO_DIALOG;
    }
    
    private boolean checkPassword(String checkWord){
    	String password = Settings.accessKey(this);
    	if(checkWord.equals(password))
    		return true;
    	else
    		return false;
    }
    
	private void startLogging() {
		setUserControl();
		startService(new Intent(this, KLService.class));		
		showServiceStatus();
		TApplication app = (TApplication)getApplication();
		app.startCheckService();		
	}
	
	private void stopLogging() {
		setUserControl();
		stopService(new Intent(this, KLService.class));		
		showServiceStatus();
		TApplication app = (TApplication)getApplication();
		app.stopCheckService();
	}
	
	// Declare that service is controlled by user
	private void setUserControl(){
		SharedPreferences.Editor pref = this.getSharedPreferences(KLService.PREF_NAME, 0).edit();
		pref.putBoolean("user_control", true);
		pref.commit();
	}
	
	private void showServiceStatus(){
		// Show the current service state        
        if(isServiceRunning(this)){
        	logState.setText(getResources().getText(R.string.service_started));
        	startButton.setText(this.getString(R.string.stop_label));
        	isRunning = true;
        }else{
        	logState.setText(getResources().getText(R.string.service_stopped));
        	startButton.setText(this.getString(R.string.start_label));
        	isRunning = false;
        }
	}
	
	private String readLicenseFile() throws IOException {
		AssetManager am = getAssets();
		InputStreamReader isr = new InputStreamReader(am.open("license.txt"));
		StringBuilder sb = new StringBuilder();
		int c;
		
		while((c = isr.read()) != -1)
			sb.append((char)c);
			
		return sb.toString();
	}
	
	private boolean isServiceRunning(Context context){
		ActivityManager aMan = 
			(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> services = 
			aMan.getRunningServices(Integer.MAX_VALUE);
		for(ActivityManager.RunningServiceInfo service : services){
			if(".KLService".equals(service.service.getShortClassName())){
				return true;
			}
		}
		return false;
	}
}