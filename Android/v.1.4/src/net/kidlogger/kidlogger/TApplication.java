package net.kidlogger.kidlogger;

import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;

import java.io.File;
import java.util.List;

public class TApplication extends Application {
	
	protected String mSendTime;
	protected String mAccessKey;
	protected KLService mService;
	protected boolean mServiceOnCreate = false;
	
	private SysLog sLog;
	private Handler mHandler;
	private Handler mHandleService;
	private Runnable mTrackService;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		mSendTime = new String();
		sLog = new SysLog();
		mAccessKey = Settings.accessKey(this);
		mHandler = new Handler();
		mHandleService = new Handler(){
			public void handleMessage(Message msg){
				String data = (String)msg.obj;
				if(data == null)
					return;
				
				if(data.equals("start")){
					startService(new Intent(TApplication.this, KLService.class));
				}else if(data.equals("restart")){
					try{
						stopService(new Intent(TApplication.this, KLService.class));
						startService(new Intent(TApplication.this, KLService.class));
					}catch(Exception e){
						logError("TApplication.startCheckService", e.toString());
					}
				}
			}
		};
		startCheckService();
	}
	
	@Override
	public void onLowMemory(){
		super.onLowMemory();
		
		try{
			stopService(new Intent(this, KLService.class));
			startService(new Intent(this, KLService.class));
		}catch(Exception e){
			logError("TApplication.onLowMemory", e.toString());
		}		
		logError("TApplication.onLowMemory", "Service restarted because of low memory event");
	}
	
	protected void startCheckService(){
		mTrackService = new Runnable(){
			public void run() {
				new Thread(new Runnable(){
					public void run() {
						if(!checkService()){
							Message msg = mHandleService.obtainMessage(1, (String)"start");
							mHandleService.sendMessage(msg);
						}else{
							if(mServiceOnCreate){
								Message msg = mHandleService.obtainMessage(2, (String)"restart");
								mHandleService.sendMessage(msg);
							}
						}
					}
					
				}).start();
				mHandler.postDelayed(mTrackService, 300000L);
			}			
		};
		mHandler.postDelayed(mTrackService, 300000L);
	}
	
	protected void stopCheckService(){
		mHandler.removeCallbacks(mTrackService);
	}
	
	protected synchronized boolean checkService(){
		try{
			ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningServiceInfo> services = am.getRunningServices(Integer.MAX_VALUE);
			for(RunningServiceInfo si : services){
				if(si.service.getShortClassName().equals(".KLService"))
					return true;
			}
		}catch(Exception e){
			logError("TApplication.checkService", e.toString());			
		}
		return false;
	}
	
	protected void logError(final String method, final String message){
		final File path = getFilesDir();
		new Thread(new Runnable(){
			public void run(){
				sLog.writeLog(path, new String(method), new String(message));
			}
		}).start();
	}
	
	protected String readLog(){
		File file = new File(getFilesDir(), "errors.txt");
		StringBuilder rsb = sLog.readLog(file);
		if(rsb == null)
			return null;
		
		return rsb.toString();
	}
	
	protected void launchMainActivity(){
		Intent i = new Intent(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);				
		startActivity(i);
	}
}
