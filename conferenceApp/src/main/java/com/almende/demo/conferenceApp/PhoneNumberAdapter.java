package com.almende.demo.conferenceApp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PhoneNumberAdapter extends ArrayAdapter<String> {
	
	Context	context;
	int		layoutResourceId;
	String	data[]	= null;
	
	public PhoneNumberAdapter(Context context, int layoutResourceId,
			String[] data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			TextView phonenumber = (TextView) row
					.findViewById(R.id.phonenumber);
			phonenumber.setText(data[position]);
		}
		return row;
	}
	
}
