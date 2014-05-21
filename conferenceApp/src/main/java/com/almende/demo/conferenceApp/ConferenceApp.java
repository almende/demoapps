package com.almende.demo.conferenceApp;

import android.app.Application;
import android.content.Intent;

public class ConferenceApp extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.startService(new Intent(this, EveService.class));
	}
}
