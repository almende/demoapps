/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceApp;

import java.io.IOException;
import java.net.URI;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.state.file.FileStateConfig;
import com.almende.eve.transform.rpc.RpcTransformFactory;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.ws.WebsocketTransportConfig;
import com.almende.eve.transport.ws.WsClientTransport;
import com.almende.eve.transport.ws.WsClientTransportFactory;
import com.almende.util.callback.SyncCallback;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.greenrobot.event.EventBus;

/**
 * The Class ConferenceAgent.
 */
public class ConferenceAgent extends Agent {
	private static final String	BASEURL	= "ws://10.10.1.180:8082/ws/";
	private URI					cloud	= null;
	
	/**
	 * Instantiates a new conference agent.
	 */
	public ConferenceAgent() {
		EventBus.getDefault().unregister(this);
		EventBus.getDefault().register(this);
	}
	
	private void registerAgent(String id) {
		try {
			System.err.println("Registering agent:" + id);
			final WebsocketTransportConfig clientConfig = new WebsocketTransportConfig();
			clientConfig.setServerUrl(BASEURL + "management");
			clientConfig.setId("management_"+id);
			
			final SyncCallback<Boolean> callback = new SyncCallback<Boolean>();
			final ObjectNode params = JOM.createObjectNode();
			params.put("id", id);
			try {
				WsClientTransport client = WsClientTransportFactory.get(
						clientConfig, new SimpleHandler<Receiver>(
								new Receiver() {
									@Override
									public void receive(Object msg,
											URI senderUrl, String tag) {
										System.err.println("Received reply:"
												+ msg);
										callback.onSuccess(true);
									}
								}));
				client.connect();
				client.send(RpcTransformFactory.get(null)
						.buildMsg("registerAgent", params, null).toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				// Wait for reply!
				callback.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Inits the.
	 * 
	 * @param ctx
	 *            the ctx
	 */
	public void init(Context ctx) {
		
		final TelephonyManager tm = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		registerAgent(tm.getDeviceId());
		
		final AgentConfig config = new AgentConfig();
		config.setId(tm.getDeviceId());
		final WebsocketTransportConfig clientConfig = new WebsocketTransportConfig();
		clientConfig.setServerUrl(BASEURL + config.getId());
		
		config.setTransport(clientConfig);
		
		final FileStateConfig stateConfig = new FileStateConfig();
		stateConfig.setJson(true);
		stateConfig.setPath(ctx.getFilesDir().getAbsolutePath()
				+ "/agentStates/");
		stateConfig.setId("conferenceAgent");
		
		config.setState(stateConfig);
		
		setConfig(config);
		cloud = URI.create(BASEURL + config.getId());
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
		if (event.getValue().equals("scanRes")) {
			System.err.println("Checking:" + event.getId());
			check(event.getId());
		}
	}
	
	private void check(String id) {
		if (cloud != null) {
			final ObjectNode params = JOM.createObjectNode();
			params.put("id", id);
			try {
				send(cloud, "seen", params);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Not connected?!?");
		}
		
		// TODO:
		/*
		 * 1 check locally if this id has been seen before
		 * 2 contact cloud agent to seek this out
		 */
	}
	
	@Access(AccessType.PUBLIC)
	public void know(final @Name("id") String id) {
		// Todo: interact with user if not done earlier for this url
		// TODO: Store earlier urls in State.
	}
}
