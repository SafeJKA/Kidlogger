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

import android.os.Build;

import java.util.Date;

public class Templates {

	public static byte[] getHeader(String devId){
		long now = System.currentTimeMillis();		
		String h = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />";
		h += "<title>Log file for <b>Smartphone</b></title>";
		h += "<link rel=\"StyleSheet\" href=\"style.css\" type=\"text/css\" /><body>";
		h += "\r\n<p class=\"app\" time=\"" + String.format("%tR", now) + "\">";
		h += "New session for " + Build.MANUFACTURER + " " + Build.DEVICE;
		h += ", s/n " + devId + " [" + String.format("%tR ", now) + "]</p>\r\n\r\n";		
		byte[] buff = h.getBytes();
		
		return buff;
	}
	
	public static byte[] getLocations(String prov, double longitude, double latitude){
		String b = "<p class=\"gps-point\" name=\"\" longitude=\"" + longitude + "\" ";
		b += "latitude=\"" + latitude + "\" time=\"" + getCurTime() + "\" move=\"\">";
		b += "GPS POINT</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getCellLoc(String cellId, String lac){
		String b = "<p class=\"gps-point\" name=\"\" >Cell ID: " + cellId + " ";
		b += "LAC: " + lac + "</p>\r\n\r\n";
		
		return b.getBytes();
	}
	
	public static byte[] getLocErr(){
		String b = "\r\r\n<br><br><b><font color=\"green\">Location</font></b><br>\r\r\n";
		b += "<br><br><b><font color=\"green\">Couldn't get location</font></b><br>\r\n";
		b += "<br><br><b><font color=\"green\">Time: ";
		b += getCurTime() + "</font></b><br>";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getCall(String number, boolean incoming){
		String b = "<p class=\"" + (incoming ? "in_call" : "out_call") + "\" ";
		b += "time=\"" + getCurTime() + "\">" + number + "</p>\r\n\r\n";
		
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getRecord(String fileName){
		String b = "<p class=\"3gp\" time=\"" + getCurTime() + "\">";
		b += fileName + "</p>\r\n\r\n";
		
		return b.getBytes();
	}
	
	public static byte[] getSmsTag(String address, String body, boolean incoming){
		String b = "<p class=\"" + (incoming ? "in_sms" : "out_sms") + "\" ";
		b += "time=\"" + getCurTime() + "\">SMS " + (incoming ? "from " : "to ");
		b += address + " : " + body + "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getIdle(long idle, long beginIdle, long currTime){
		String idleStr = getTimeString(idle);
		String b = "<p class=\"idle\" time=\"" + String.format("%tR", beginIdle) + "\"";
		b += " duration=\"" + idleStr + "\">[" + String.format("%tR", currTime) + "] ";
		b += "Idle: " + idleStr + "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getUsbLog(boolean connected){
		String b = "<p class=\"system\" time=\"" + getCurTime() + "\">USB ";
		b += (connected ? "connected" : "disconnected") + "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getWifiLog(String state){
		String b = "<p class=\"system\" time=\"" + getCurTime() + "\">WiFi ";
		b += state + "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getWifiPoint(String state, String point){
		String b = "<p class=\"system\" time=\"" + getCurTime() + "\" ";
		b += "info=\"WiFi " + state + "\">WiFi " + state + ", SSID: " + point + "</p>\r\n\r\n";		
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getUrlLog(String url){
		String uRl = url.length() > 150 ? url.substring(0, 150) : url;
		String b = "<p class=\"url\" time=\"" + getCurTime() + "\" ";
		b += "url=\"" + uRl + "\">" + uRl + "</p>\r\n\r\n";

		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getApiLog(String task){
		String b = "<p class=\"app\" name=\"" + task + "\" time=\"" + getCurTime() + "\">" + task;
		b += "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getServiceState(boolean start, boolean byUser){
		String b = "<p class=\"system\" time=\"" + getCurTime() + "\">Logging ";
		b += (start ? "started " : "stopped ") + (byUser ? "by user" : "by system");
		b += "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getClipboardLog(String content){
		String b = "<p class=\"clipboard\" time=\"" + getCurTime() + "\">Clipboard: ";
		b += content + "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getPowerLog(boolean turnOff){
		String b = "<p class=\"system\" time=\"" + getCurTime() + "\">Power ";
		b += (turnOff ? "Off " : "On") + "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getMediaLog(String state){
		String b = "<p class=\"system\" time=\"" + getCurTime() + "\">SD card ";
		b += state + "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getGsmLog(String state){
		String b = "<p class=\"system\" time=\"" + getCurTime() + "\">GSM state ";
		b += state + "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getGsmLog(String state, String operator){
		String b = "<p class=\"system\" time=\"" + getCurTime() + "\">GSM state ";
		b += state + ", " + operator + "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getAirMode(boolean state){
		String b = "<p class=\"system\" time=\"" + getCurTime() + "\">";
		b += "Airplane mode is " + (state ? "On" : "Off") + "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getKeystroke(String string){
		String b = "<p class=\"keystrokes\" time=\"" + getCurTime() + "\">";
		b += string + "</p>\r\n\r\n";
		byte[] buff = b.getBytes();
		
		return buff;
	}
	
	public static byte[] getPhotos(String name){
		String b = "<p class=\"jpg\" time=\"" + getCurTime() + "\" title=\"FOTO\">";
		b += "FOTO <img src=\"" + name + "\"></p>\r\n\r\n";
		
		return b.getBytes();
	}
	
	public static byte[] getRoamingInfo(String country, String operator){
		String b = "<p class=\"system\" time\"" + getCurTime() + "\">Rouming ON ";
		b += country + " \\ " + operator + "</p>\r\n\r\n";
		
		return b.getBytes();
	}
	
	protected static String getCurTime(){
		return String.format("%tR", new Date().getTime());
	}
	
	private static String getTimeString(long t){
		String format = String.format("%%0%dd", 2);
		long elapsedTime = t / 1000L;
		
		//String seconds = String.format(format, elapsedTime % 60);
		String minutes = String.format(format, (elapsedTime % 3600) / 60);
		String hours = String.format(format, elapsedTime / 3600);
		
		return hours + ":" + minutes;
		
		/*int hc = 3600000;
		int mc = 60000; 
		int h = (int)(t / hc);
		int rest = (int)(t % hc);
		float m = (float)(rest / mc);
		rest = rest % mc;
		int s = rest / 1000; // sec	
		
		return String.format("%02d:%2.0f", h, m);*/
	}
}
