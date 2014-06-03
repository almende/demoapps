/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceApp;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.almende.demo.conferenceCloud.Info;
import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.scheduling.SimpleSchedulerConfig;
import com.almende.eve.state.TypedKey;
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
	private static final String								BASEURL		= "ws://10.10.1.180:8082/ws/";
	private URI												cloud		= null;
	private static final TypedKey<HashMap<String, Info>>	CONTACTKEY	= new TypedKey<HashMap<String, Info>>(
																				"contacts") {
																		};
	private static final TypedKey<HashMap<String, String>>	KNOWNNAMES	= new TypedKey<HashMap<String, String>>(
																				"knownNames") {
																		};
	
	private static Context									ctx			= null;
	
	/**
	 * Instantiates a new conference agent.
	 */
	public ConferenceAgent() {
		EventBus.getDefault().unregister(this);
		EventBus.getDefault().register(this);
	}
	
	private void registerAgent(String id, String baseUrl) {
		try {
			System.err.println("Registering agent:" + id + " at:" + baseUrl
					+ "management");
			final WebsocketTransportConfig clientConfig = new WebsocketTransportConfig();
			clientConfig.setServerUrl(baseUrl + "management");
			clientConfig.setId("management_" + id);
			
			final SyncCallback<Boolean> callback = new SyncCallback<Boolean>() {
			};
			final ObjectNode params = JOM.createObjectNode();
			params.put("id", id);
			WsClientTransport client = null;
			try {
				client = WsClientTransportFactory.get(clientConfig,
						new SimpleHandler<Receiver>(new Receiver() {
							@Override
							public void receive(Object msg, URI senderUrl,
									String tag) {
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
				if (client != null && client.isConnected()) {
					// Wait for reply!
					final boolean res = callback.get();
					System.err.println("Got reply on callback:" + res);
					client.disconnect();
				}
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
		ConferenceAgent.ctx = ctx;
		final TelephonyManager tm = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		final AgentConfig config = new AgentConfig();
		config.setId(tm.getDeviceId());
		
		final FileStateConfig stateConfig = new FileStateConfig();
		stateConfig.setJson(true);
		stateConfig.setPath(ctx.getFilesDir().getAbsolutePath()
				+ "/agentStates/");
		stateConfig.setId("conferenceAgent");
		
		config.setState(stateConfig);
		
		SimpleSchedulerConfig schedulerConfig = new SimpleSchedulerConfig();
		config.setScheduler(schedulerConfig);
		
		setConfig(config, true);
		if (!getState().containsKey(CONTACTKEY.getKey())) {
			getState().put(CONTACTKEY.getKey(), new HashMap<String, Info>());
		}
		DetectionUtil.getInstance().startScan();
		getScheduler().schedule(this.getRpc().buildMsg("refresh", null, null),
				DateTime.now().plus(60000));
		
		reconnect();
	}
	
	public void reconnect() {
		
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		final String baseUrl = prefs.getString(
				ctx.getString(R.string.wsServer_key), BASEURL);
		
		registerAgent(getId(), baseUrl);
		
		System.err.println("Reconnecting to server:" + baseUrl + getId());
		final WebsocketTransportConfig clientConfig = new WebsocketTransportConfig();
		clientConfig.setServerUrl(baseUrl + getId());
		clientConfig.setId(getId());
		this.loadTransports(clientConfig, true);
		
		cloud = URI.create(baseUrl + getId());
	}
	
	public void refresh() {
		DetectionUtil.getInstance().startScan();
		getScheduler().schedule(this.getRpc().buildMsg("refresh", null, null),
				DateTime.now().plus(60000));
	}
	
	/**
	 * On event async.
	 * 
	 * @param event
	 *            the event
	 */
	public void onEventAsync(final StateEvent event) {
		if (event.getValue().equals("scanRes")) {
			System.err.println("Checking:" + event.getId());
			String id = event.getId();
			
			if (getState() != null) {
				if (!getState().containsKey(CONTACTKEY.getKey())) {
					getState().put(CONTACTKEY.getKey(), new HashMap<String, Info>());
				}
				HashMap<String, Info> contacts = getState().get(CONTACTKEY);
				Info info = contacts.get(id);
				if (info == null) {
					info = new Info(id);
				}
				info.setLastSeen(DateTime.now());
				contacts.put(id, info);
				getState().put(CONTACTKEY.getKey(), contacts);
				EventBus.getDefault().post(
						new StateEvent(getId(), "listUpdated"));
				
				check(id, info);
			}
		} else if (event.getValue().equals("settingsUpdated")) {
			reconnect();
			sendMyInfo();
		}
	}
	
	private void check(final String id, final Info info) {
		if (cloud != null) {
			final ObjectNode params = JOM.createObjectNode();
			params.put("id", id);
			params.put("info", JOM.getInstance().valueToTree(info));
			try {
				send(cloud, "seen", params);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Not connected?!?");
		}
	}
	
	/**
	 * Send my info.
	 */
	private void sendMyInfo() {
		if (cloud != null) {
			final ObjectNode params = JOM.createObjectNode();
			params.put("info", JOM.getInstance().valueToTree(getMyInfo()));
			try {
				send(cloud, "setMyInfo", params);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Not connected?!?");
		}
	}
	
	@Access(AccessType.PUBLIC)
	public void know(final @Name("id") String id, @Name("info") Info info) {
		if (!getState().containsKey(CONTACTKEY.getKey())) {
			getState().put(CONTACTKEY.getKey(), new HashMap<String, Info>());
		}

		HashMap<String, Info> contacts = getState().get(CONTACTKEY);
		Info oldinfo = contacts.get(id);
		if (oldinfo != null) {
			info = oldinfo.merge(info);
		}
		contacts.put(id, info);
		getState().put(CONTACTKEY.getKey(), contacts);
		EventBus.getDefault().post(new StateEvent(getId(), "listUpdated"));
	}
	
	public List<Info> getList(final boolean filterIgnored) {
		List<Info> result = new ArrayList<Info>();
		if (getState() != null && getState().containsKey(CONTACTKEY.getKey())) {
			HashMap<String, Info> contacts = getState().get(CONTACTKEY);
			
			for (Info info : contacts.values()) {
				if (info.isKnown() && !info.isIgnored()) {
					result.add(info);
				}
			}
		}
		Collections.sort(result, Collections.reverseOrder());
		return result;
	}
	
	public Info get(final String id) {
		if (getState() != null && getState().containsKey("contacts")) {
			return getState().get(CONTACTKEY).get(id);
		}
		return null;
	}
	
	public Info getMyInfo() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		final String myName = prefs.getString(
				ctx.getString(R.string.myName_key), "person:" + getId());
		
		final Info myInfo = new Info(getId());
		myInfo.setName(myName);
		
		final String paperTitle = prefs.getString(
				ctx.getString(R.string.paperTitle_key), "---");
		myInfo.setTitle(paperTitle);
		myInfo.setKnownNames(getKnownNames());
		
		final String phoneNumberPref = prefs.getString(
				ctx.getString(R.string.phoneNumbers_key), "");
		final Set<String> phoneNumberSet = new HashSet<String>();
		final String[] phoneNumbers = phoneNumberPref.split(",");
		
		phoneNumberSet.addAll(Arrays.asList(phoneNumbers));
		phoneNumberSet.remove(null);
		myInfo.setPhonenumbers(phoneNumberSet);
		
		System.err.println("Sending myInfo:"+JOM.getInstance().valueToTree(myInfo));
		return myInfo;
	}
	
	public HashMap<String, String> getKnownNames() {
		if (getState() != null && getState().containsKey(KNOWNNAMES.getKey())) {
			return getState().get(KNOWNNAMES);
		}
		return new HashMap<String, String>();
	}
	
	public void addKnownName(String name, String reason) {
		if (name != null) {
			Map<String, String> names = new HashMap<String, String>();
			if (getState() != null
					&& getState().containsKey(KNOWNNAMES.getKey())) {
				names = getState().get(KNOWNNAMES);
			}
			names.put(name, reason);
			if (getState() != null) {
				getState().put(KNOWNNAMES.getKey(), names);
				EventBus.getDefault().post(
						new StateEvent(null, "addedKnownName"));
			}
		}
	}
	
	public void cleanKnownNames(){
		if (getState() != null) {
			getState().put(KNOWNNAMES.getKey(), new HashMap<String, String>());
			EventBus.getDefault().post(new StateEvent(null, "addedKnownName"));
		}
	}
	public void cleanContacts(){
		if (getState() != null) {
			getState().put(CONTACTKEY.getKey(), new HashMap<String, Info>());
			EventBus.getDefault().post(new StateEvent(getId(), "listUpdated"));
		}
	}
}
