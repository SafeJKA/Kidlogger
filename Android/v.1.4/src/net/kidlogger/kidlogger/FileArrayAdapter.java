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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FileArrayAdapter extends ArrayAdapter<Option>{
	
	private Context c;
	private List<Option> items;
	private int id;
	
	public FileArrayAdapter(Context context, int textViewResourceId, List<Option> objects){
		super(context, textViewResourceId, objects);
		
		c = context;
		items = objects;
		id = textViewResourceId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View v = convertView;
		if(v == null){
			LayoutInflater li = (LayoutInflater)c.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(id, null);
		}
		
		final Option o = items.get(position);		
		if(o != null){
			TextView t1 = (TextView)v.findViewById(R.id.file_name);
			TextView t2 = (TextView)v.findViewById(R.id.file_data);
			if(t1 != null)
				t1.setText(o.getName());
			if(t2 != null)
				t2.setText(o.getData());
		}
		
		return v;
	}
	
	public Option getItem(int i){
		return items.get(i);
	}
}
