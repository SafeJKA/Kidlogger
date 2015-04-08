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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.util.AttributeSet;
//import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class SendTestReport extends Preference {
	private TextView testRes;
	private Button sendTest;
	private Context context;
	private TApplication app;
	
	public SendTestReport(Context context, AttributeSet sett){
		super(context, sett);
		this.context = context;
		
		setLayoutResource(R.layout.test_report);
		app = (TApplication)context.getApplicationContext();
	}
	
	@Override
	protected void onBindView(View view){
		super.onBindView(view);
		
		testRes = (TextView)view.findViewById(R.id.test_result);
		testRes.setText(context.getString(R.string.str_send_message));		
		
		sendTest = (Button)view.findViewById(R.id.send_test_report);
		sendTest.setOnClickListener(new OnClickListener(){
			public void onClick(View view){
				testRes.setText(context.getString(R.string.str_wait_message));
				sendPOST();
				restartService();
			}
		});
		
		String file = "";
		String status = "";
		if(KLService.SIZE_SENT > 0){
			//file = KLService.FILE_SENT + " (" + KLService.SIZE_SENT + " b)";
			file = new String(KLService.FILE_SENT) + " (" + KLService.SIZE_SENT + " b)";
			//status = KLService.pStatus;
			status = new String(KLService.pStatus);
		}
		TextView fileSent = (TextView)view.findViewById(R.id.sent_file_name);
		fileSent.setText(file);
		
		TextView sentStatus = (TextView)view.findViewById(R.id.sent_status);
		sentStatus.setText(status);
		
		TextView sendTime = (TextView)view.findViewById(R.id.send_time);
		sendTime.setText(app.mSendTime);
		
		// Show delivery queue
		TextView deliveryQueue = (TextView)view.findViewById(R.id.delivery_queue);
		deliveryQueue.setText(getQueueList());
		
		// Show uploaded media file size
		TextView uploadedSize = (TextView)view.findViewById(R.id.uploaded_size);
		uploadedSize.setText(String.format("%.2f", (KLService.uploadedSize / 1000000f)));
		//uploadedSize.setText(String.valueOf(KLService.uploadedSize));
	}
	
	private void restartService(){
		if(KLService.serviceStarted){
			setUserControl();
			context.stopService(new Intent(context, KLService.class));
			setUserControl();
			context.startService(new Intent(context, KLService.class));
		}		
	}
	
	private void setUserControl(){
		String prefName = "net.tesline.service.KLService";
		SharedPreferences.Editor pref = context.getApplicationContext()
			.getSharedPreferences(prefName, 0).edit();
		pref.putBoolean("user_control", true);
		pref.commit();
	}
	
	private void sendPOST() {		
		File fs = getFile();
		Date now = new Date();
		String fileDate = String.format("%td/%tm/%tY %tT", now, now, now, now);
		String devField = Settings.getDeviceField(context);
		if(devField.equals("undefined") || devField.equals(""))
			return;				
		try{
			HttpClient client = new DefaultHttpClient();
			String postUrl = context.getString(R.string.upload_link);
			//String postUrl = "http://10.0.2.2/denwer/";
			HttpPost post = new HttpPost(postUrl);
			FileBody bin = new FileBody(fs, "text/html", fs.getName());
			StringBody sb1 = new StringBody(devField);
			StringBody sb2 = new StringBody("HTML");
			StringBody sb3 = new StringBody("Andriod_2_2");
			StringBody sb4 = new StringBody("1.0");
			StringBody sb5 = new StringBody(fileDate);
			
			MultipartEntity reqEntity =
				new MultipartEntity(HttpMultipartMode.STRICT);
			reqEntity.addPart("file", bin);
			reqEntity.addPart("device", sb1);
			reqEntity.addPart("content", sb2);
			reqEntity.addPart("client-ver", sb3);
			reqEntity.addPart("app-ver", sb4);
			reqEntity.addPart("client-date-time", sb5);
			
			post.setEntity(reqEntity);
			HttpResponse response = client.execute(post);
			HttpEntity resEntity = response.getEntity();
			if(resEntity != null){
				String pStatus = EntityUtils.toString(resEntity);
				if(pStatus.equalsIgnoreCase("Ok")){
					fs.delete();
					testRes.setText("Response: " + pStatus);						
				}else{
					//Log.i("sendPOST", pStatus);
				}
				testRes.setText("Response: " + pStatus);
				//Log.i("sendPOST", "Response: " + pStatus);
			}			
		}catch(Exception e){
			//Log.i("sendPOST", e.toString());
		}
	}
	
	private File getFile(){
		String name = "test";
		File path = context.getFilesDir();
		File f = new File(path, name + ".htm");
		if(!f.exists()){			
			String h = "<p class=\"app\" time=\"" + Templates.getCurTime() + "\">";
			h += "Test Report OK " + Build.VERSION.RELEASE + " (";			
			h += Settings.getApiVersion(context) + ")</p>\r\n";
			byte[] buff = h.getBytes();
			
			try{
				FileOutputStream fos = context.openFileOutput(f.getName(), 
						Context.MODE_PRIVATE);
				fos.write(buff);
				fos.close();
			}catch(IOException e){
				//Log.i("STR", e.toString());
			}			
		}
		
		return f;
	}
	
	private String getQueueList(){
		ListFileSync fileList = new ListFileSync(context);
		String[] tmp = fileList.getListOfFiles(false);
		String list = new String();
		
		if(tmp != null){
			for(String s : tmp){
				list += s + "\r\n";
			}
			return list;
		}
		
		return list;
	}
}
