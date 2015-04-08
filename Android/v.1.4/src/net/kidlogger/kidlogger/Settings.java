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

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodInfo;
import android.text.Html;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.text.method.LinkMovementMethod;
import android.widget.EditText;
import android.widget.TextView;
//import android.util.Log;
import android.util.TypedValue;


public class Settings extends PreferenceActivity implements 
	OnSharedPreferenceChangeListener{	
	
	@Override
	protected void onCreate(Bundle saveInstanceState){
		super.onCreate(saveInstanceState);
		addPreferencesFromResource(R.xml.settings);		
		
		// Set summary for EditTextPreference
		setSummary("refreshGPS", getGpsUpdatesTime(this) + " min");
		setSummary("minDist", getMinDistance(this) + " m");
		//setSummary("maxTimeRec", (getMaxTimeRec(this) / 1000) + " sec");
		float sizeRec = (float)(getMaxSizeRec(this) / 1000000.0f);
		//setSummary("maxSizeRec", String.format("%.3f Mb", sizeRec));
		//setSummary("keepRecTime", getKeepRecTime(this) + " days");
		setSummary("freqSend", getFreqSend(this) + " min");
		setSummary("accessKey", accessKey(this));
		//setSummary("maxMediaUpload", String.format("%.1f Mb", getMaxMediaUpload(this)));
				
		String id = getDeviceField(getBaseContext());				
		if(!id.equals("") && !id.equals("undefined"))
			setSummary("deviceField", id);
		
		// Set OnPreferenceClickListener 
		Preference loggKey = (Preference)findPreference("logKey");
		loggKey.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			public boolean onPreferenceClick(Preference preference){
				InputMethodManager im = 
					(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				List<InputMethodInfo> mInf = im.getInputMethodList();
				String api = "net.kidlogger.kidloggerkeyboard.SoftKeyboardPRO";
				for(InputMethodInfo imi : mInf){
					if(api.equals(imi.getServiceName()))
						return true;
				}
				showDialog();
				return true;
			}
		});
		
		// Set OnClickListener for goToSite button
		Preference goToSite = (Preference)findPreference("goToSite");
		goToSite.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			public boolean onPreferenceClick(Preference preference){
				String url = "http://www.kidlogger.net/";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				
				return true;
			}
		});
		
		// Set fields only for digits
		EditTextPreference gps = (EditTextPreference)findPreference("refreshGPS");
		EditText digGps = (EditText)gps.getEditText();
		digGps.setKeyListener(DigitsKeyListener.getInstance(false, true));
		
		EditTextPreference dist = (EditTextPreference)findPreference("minDist");
		EditText digDist = (EditText)dist.getEditText();
		digDist.setKeyListener(DigitsKeyListener.getInstance(false, true));
		
		EditTextPreference freq = (EditTextPreference)findPreference("freqSend");
		EditText digFreq = (EditText)freq.getEditText();
		digFreq.setKeyListener(DigitsKeyListener.getInstance(false, true));
		
		EditTextPreference access = (EditTextPreference)findPreference("accessKey");
		EditText accessKey = (EditText)access.getEditText();
		accessKey.setKeyListener(DigitsKeyListener.getInstance(false, true));
		
		//EditTextPreference maxUpload = (EditTextPreference)findPreference("maxMediaUpload");
		//EditText maxMediaUpload = (EditText)maxUpload.getEditText();
		//maxMediaUpload.setKeyListener(DigitsKeyListener.getInstance(false, true));
		
		// Set max length
		InputFilter[] filter = new InputFilter[1];
		filter[0] = new InputFilter.LengthFilter(6);
		accessKey.setFilters(filter);
		
		// Check version of Android
		checkSysVersion();
	}
	
	@Override
	protected void onResume(){
		super.onResume();		
		getPreferenceScreen().getSharedPreferences()
			.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		setResult(0);
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		
		getPreferenceScreen().getSharedPreferences()
			.unregisterOnSharedPreferenceChangeListener(this);		
	}
	
	// Method implementation
	public void onSharedPreferenceChanged(SharedPreferences sp, String key){
		MainActivity.prefChanged = true;
		if(key.equals("deviceField")){
			String val = getDeviceField(this);
			if(val.equals("") || val.equals("undefined"))
				setSummary(key, getString(R.string.s_summary_text));
			else
				setSummary(key, val);
		}else if(key.equals("refreshGPS")){
			setSummary(key, getGpsUpdatesTime(this) + " min");
		}else if(key.equals("minDist")){
			setSummary(key, getMinDistance(this) + " m");
		}else if(key.equals("maxTimeRec")){
			setSummary(key, (getMaxTimeRec(this) / 1000) + " sec");
		}else if(key.equals("maxSizeRec")){
			float sizeRec = (float)(getMaxSizeRec(this) / 1000000.0f);
			setSummary(key, String.format("%.3f Mb", sizeRec));
		}else if(key.equals("keepRecTime")){
			setSummary(key, getKeepRecTime(this) + " days");
		}else if(key.equals("freqSend")){
			setSummary(key, getFreqSend(this) + " min");
		}else if(key.equals("accessKey")){
			setSummary(key, accessKey(this));
			TApplication app = (TApplication)getApplication();
			app.mAccessKey = accessKey(this);
		}else if(key.equals("maxMediaUpload")){
			setSummary(key, String.format("%.1f Mb", getMaxMediaUpload(this)));
		}
	}
	
	// Show dialog
	private void showDialog(){
		/*
		String mess = "This feature requires <a href=\"https://market.android.com";
		mess += "/details?id=net.kidlogger.kidloggerkeyboard&feature=search_result";
		mess +=	"&hl=en\">\"Soft Keyboard PRO\"</a> input method.<br><br>";
		mess += "1. Install and activate \"Soft Keyboard PRO\" as input method.";
		mess += "<br><br>2. Tap and hold (or long press) any text input field >";
		mess += "Input method >\"Soft Keyboard PRO\".";
		*/
		String mess = getString(R.string.s_dialog_message);
		TextView textView = new TextView(this);
		textView.setMovementMethod(LinkMovementMethod.getInstance());
		textView.setPadding(15, 15, 15, 15);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		textView.setText(Html.fromHtml(mess));
		
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setTitle(getString(R.string.dialog_title_warning));
		ad.setIcon(android.R.drawable.ic_dialog_alert);
		ad.setPositiveButton("OK", null);
		ad.setView(textView);		
		ad.show();		
	}
	
	private void setSummary(String key, String sum){
		Preference devId = (Preference)findPreference(key);
		devId.setSummary(sum);
	}
	
	private void checkSysVersion(){
		if(Build.VERSION.SDK_INT >= 11){ // Android 3.0, HONEYCOMB
			CheckBoxPreference wifi = (CheckBoxPreference)findPreference("logWifi");
			wifi.setEnabled(false);
		}else{			
			//Log.i("Settings", "Version: " + Build.VERSION.SDK_INT + ", " + Build.VERSION.RELEASE);
		}
		
	}
	
	public static String getDeviceField(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getString("deviceField", "undefined");
	}
	
	public static String getGpsUpdatesTime(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getString("refreshGPS", "5");
	}
	
	public static String getMinDistance(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getString("minDist", "20");
	}
	
	public static long getFreqSend(Context context){	
		String s = PreferenceManager.getDefaultSharedPreferences(context)
			.getString("freqSend", "10");
		long fs;
		try{			
			fs = Long.parseLong(s);				
		}catch(NumberFormatException e){
			fs = 10L;
		}
		return fs < 3L ? 3L : fs;
	}
	
	public static boolean loggingGps(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logGps", false);
	}
	
	public static boolean loggingCell(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logCell", true);
	}
	
	public static boolean loggingWifi(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logWifi", false);
	}
	
	public static boolean loggingSms(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logSms", false);
	}
	
	public static boolean loggingCalls(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logCalls", true);
	}
	
	public static boolean loggingContact(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logContact", true);
	}
	/*
	public static boolean loggingAudioCalls(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logAudioCalls", true);
	}
	*/
	public static boolean recordInCalls(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("recInCalls", false);
	}
	
	public static boolean recordOutCalls(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("recOutCalls", false);
	}
	
	public static boolean uploadRecords(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("uploadRec", true);
	}
	
	public static int getMaxTimeRec(Context context){
		String tmp = PreferenceManager.getDefaultSharedPreferences(context)
			.getString("maxTimeRec", "30");
		
		return Integer.valueOf(tmp) * 1000;
	}
	
	public static long getMaxSizeRec(Context context){
		String tmp = PreferenceManager.getDefaultSharedPreferences(context)
			.getString("maxSizeRec", "0.3");
		float size;
		try{
			size = Float.valueOf(tmp);
		}catch(NumberFormatException e){
			size = 0.3F;
		}
		
		
		return (long)(size * 1000000L);
	}
	/*
	public static boolean deleteRecords(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("delRec", true);
	}
	*/
	public static int getKeepRecTime(Context context){
		String tmp = PreferenceManager.getDefaultSharedPreferences(context)
			.getString("keepRecTime", "5");
		
		return Integer.valueOf(tmp);
	}
	
	public static boolean loggingIdle(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logIdle", false);
	}
	
	public static boolean loggingUrl(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logUrl", true);
	}
	
	public static boolean loggingUsb(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logUsb", false);
	}
	
	public static boolean loggingTasks(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logTasks", true);
	}
	
	public static boolean loggingClipboard(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logClipboard", false);
	}
	
	public static boolean loggingPower(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logPower", false);
	}
	
	public static boolean loggingMedia(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logMedia", false);
	}
	
	public static boolean loggingGsm(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logGsm", false);
	}
	
	public static boolean loggingAir(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logAir", false);
	}
	
	public static boolean loggingKey(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logKey", false);
	}
	
	public static boolean loggingPhotos(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("logPhotos", true);
	}
	
	public static boolean uploadPhotos(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("uploadPhotos", true);
	}
	
	public static boolean uploadLogs(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean("uploadKey", false);
	}
	
	public static String accessKey(Context context){
		String key = "*";
		key += PreferenceManager.getDefaultSharedPreferences(context)
			.getString("accessKey", "123456");
		key += "#";
		
		return key;
	}
	
	public static float getMaxMediaUpload(Context context){
		String tmp = PreferenceManager.getDefaultSharedPreferences(context)
			.getString("maxMediaUpload", "5");
		float size;
		try{
			size = Float.valueOf(tmp);
		}catch(NumberFormatException e){
			size = 5.0F;
		}
		
		return size; // return bytes		
	}
	
	public static String getApiVersion(Context context){
		PackageInfo pi = null;
		try{
			pi = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
		}catch(NameNotFoundException e){
			
		}
		
		String apiVersion = "";
		if(pi != null)
			apiVersion = pi.versionName;
		
		return apiVersion;
	}
}
