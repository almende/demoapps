/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceApp;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.ws.WebsocketTransportConfig;
import com.almende.eve.transport.ws.WsClientTransport;
import com.almende.eve.transport.ws.WsClientTransportFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import de.greenrobot.event.EventBus;

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
	private WsClientTransport			client		= null;
	
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
		final PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		// Build notification
		final Notification noti = new Notification.Builder(this)
				.setContentTitle("Conference App running!")
				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent)
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
				System.err.println("Eve Service ThreadId:"
						+ Thread.currentThread().getId());
				
				setupBaseNotification();
				servicesConnected();
				initTransport(ctx);
			}
		});
	}
	
	/**
	 * Inits the transport.
	 * 
	 * @param ctx
	 *            the ctx
	 */
	public void initTransport(final Context ctx) {
		
		final WebsocketTransportConfig clientConfig = new WebsocketTransportConfig();
		final TelephonyManager tm = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		
		clientConfig.setId(tm.getDeviceId());
		clientConfig.setServerUrl("ws://10.10.1.180:8082/ws/switchBoard");
		
		client = WsClientTransportFactory.get(clientConfig, new MyReceiver());
		try {
			client.connect();
			client.send("Good day to you!");
		} catch (final IOException e) {
			System.err.println("IOException during connect/send");
			e.printStackTrace();
		}
		
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
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		if (!myThread.isAlive()) {
			myThread.start();
		}
		EventBus.getDefault().unregister(this);
		EventBus.getDefault().register(this);
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
	
	/**
	 * On event async.
	 * 
	 * @param event
	 *            the event
	 */
	public void onEventAsync(final StateEvent event) {
		System.err.println("Service received StateEvent:" + event.getValue()
				+ " threadId:" + Thread.currentThread().getId());
		
		if (event.getValue().equals("ReceivedScan") && client != null) {
			try {
				client.send(PositionUtil.getInstance().getCurrent().toString());
			} catch (final JsonProcessingException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
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
	
	/**
	 * The Class myReceiver.
	 */
	public class MyReceiver implements Receiver,
			com.almende.eve.capabilities.handler.Handler<Receiver> {
		private final Logger	LOG	= Logger.getLogger(EveService.class
											.getName());
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.almende.eve.transport.Receiver#receive(java.lang.Object,
		 * java.net.URI, java.lang.String)
		 */
		@Override
		public void receive(final Object msg, final URI senderUrl,
				final String tag) {
			
			LOG.warning("Received msg:'" + msg + "' from: "
					+ senderUrl.toASCIIString());
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.almende.eve.capabilities.handler.Handler#get()
		 */
		@Override
		public Receiver get() {
			return this;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.almende.eve.capabilities.handler.Handler#update(com.almende.eve
		 * .capabilities.handler.Handler)
		 */
		@Override
		public void update(
				final com.almende.eve.capabilities.handler.Handler<Receiver> newHandler) {
			// Not used, data should be the same.
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.almende.eve.capabilities.handler.Handler#getKey()
		 */
		@Override
		public String getKey() {
			// Not used, data should be the same.
			return null;
		}
		
	}
	
}
