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

public class Option implements Comparable<Option> {
	
	private String name;
	private String data;
	private String path;
	
	public Option(String name, String data, String path){
		this.name = name;
		this.data = data;
		this.path = path;
	}
	
	@Override
	public int compareTo(Option o){
		if(name != null)
			return name.toLowerCase().compareTo(o.getName().toLowerCase());
		else
			throw new IllegalArgumentException();
	}
	
	public String getName(){
		return name;
	}
	
	public String getData(){
		return data;
	}
	
	public String getPath(){
		return path;
	}
}
