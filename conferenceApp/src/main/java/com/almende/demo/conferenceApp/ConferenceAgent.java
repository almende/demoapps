package com.almende.demo.conferenceApp;

import java.io.IOException;
import java.net.URI;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.eve.transport.ws.WebsocketTransportConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.greenrobot.event.EventBus;

public class ConferenceAgent extends Agent{
	private static final URI serverUri = URI.create("ws://10.10.1.180:8082/ws/switchBoard");
	public ConferenceAgent(){
		EventBus.getDefault().unregister(this);
		EventBus.getDefault().register(this);
	}
	
	public void init(Context ctx){
		final AgentConfig config = new AgentConfig();
		final TelephonyManager tm = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		config.setId(tm.getDeviceId());
		
		final WebsocketTransportConfig clientConfig = new WebsocketTransportConfig();
		clientConfig.setServerUrl(serverUri.toASCIIString());
		
		config.setTransport(clientConfig);
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
		
		if (event.getValue().equals("ReceivedScan")) {
			try {
				ObjectNode params = JOM.createObjectNode();
				InfoBean bean = new InfoBean();
				bean.setDeviceId(getId());
				bean.setLocVector(PositionUtil.getInstance().getCurrent().toString());
				params.put("info", JOM.getInstance().writeValueAsString(bean));
				
				send(serverUri,"receiveInfo",params);
				
			} catch (final JsonProcessingException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void receiveInfo(@Name("info") InfoBean info){
		if (info != null){
			System.err.println("Received:"+info.getLocVector()+" from:"+info.getDeviceId());
		}
	}
}
