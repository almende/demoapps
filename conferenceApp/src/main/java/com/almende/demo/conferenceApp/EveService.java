/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceApp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * The Class EveService.
 */
public class EveService extends Service {
	
	/**
	 * The Constant myThread.
	 */
	public static final HandlerThread	myThread	= new HandlerThread(
															EveService.class
																	.getCanonicalName());
	
	/**
	 * The Constant NEWTASKID.
	 */
	public static final int				NEWTASKID	= 0;

	public static final ConferenceAgent myAgent = new ConferenceAgent();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}
	
	/**
	 * Setup base notification.
	 */
	public void setupBaseNotification() {
		final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		final Intent intent = new Intent(this, ConferenceBaseActivity.class);
		final PendingIntent pIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		
		// Build notification
		final Notification noti = new Notification.Builder(this)
				.setContentTitle("Conference App running!")
				.setSmallIcon(R.drawable.teamwork).setContentIntent(pIntent)
				.build();
		
		noti.flags |= Notification.FLAG_FOREGROUND_SERVICE;
		
		notificationManager.notify(NEWTASKID, noti);
	}
	
	/**
	 * Inits the eve.
	 * 
	 * @param ctx
	 *            the ctx
	 */
	public void initEve(final Context ctx) {
		final Handler myHandler = new Handler(myThread.getLooper());
		myHandler.post(new Runnable() {
			@Override
			public void run() {
				setupBaseNotification();
				servicesConnected();
				myAgent.init(ctx);
			}
		});
	}
	
	
	/**
	 * Starts the service.
	 * 
	 * @param intent
	 *            the intent
	 * @param flags
	 *            the flags
	 * @param startId
	 *            the start id
	 * @return the int
	 * @see super#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {
		if (!myThread.isAlive()) {
			myThread.start();
		}
		initEve(getApplication());
		
		return START_STICKY;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private boolean servicesConnected() {
		// Check that Google Play services is available
		final int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			Log.d("Location Updates", "Google Play Services are not available?");
		}
		return false;
	}
}
