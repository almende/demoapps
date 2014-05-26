/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceApp;

import java.net.URI;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.transport.ws.WebsocketTransportConfig;

import de.greenrobot.event.EventBus;

/**
 * The Class ConferenceAgent.
 */
public class ConferenceAgent extends Agent {
	private static final URI	serverUri	= URI.create("ws://10.10.1.180:8082/ws/switchBoard");
	
	/**
	 * Instantiates a new conference agent.
	 */
	public ConferenceAgent() {
		EventBus.getDefault().unregister(this);
		EventBus.getDefault().register(this);
	}
	
	/**
	 * Inits the.
	 * 
	 * @param ctx
	 *            the ctx
	 */
	public void init(Context ctx) {
		final AgentConfig config = new AgentConfig();
		final TelephonyManager tm = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		config.setId(tm.getDeviceId());
		
		final WebsocketTransportConfig clientConfig = new WebsocketTransportConfig();
		clientConfig.setServerUrl(serverUri.toASCIIString());
		
		config.setTransport(clientConfig);
		
		setConfig(config);
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

		if (event.getValue().equals("closeBy")){
			System.err.println("I'm close to:"+event.getId());
		}
	}
}
