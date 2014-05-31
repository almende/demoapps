/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceCloud;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.joda.time.DateTime;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.eve.transport.http.DebugServlet;
import com.almende.eve.transport.http.HttpTransportConfig;
import com.almende.eve.transport.ws.WebsocketTransportConfig;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.callback.SyncCallback;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class ConferenceCloudAgent.
 */
public class ConferenceCloudAgent extends Agent {
	
	private static final String	BASEURL		= "http://127.0.0.1:8082/agents/";
	private static final String	WSBASEURL	= "ws://192.168.1.108:8082/ws/";
	private static final String	WSCLIENTURL	= "wsclient:";
	private DateTime			lastUpdate	= null;
	private Info				myInfo		= null;
	
	/**
	 * Inits the Conference Cloud Agent.
	 * 
	 * @param id
	 *            the id
	 */
	public void init(String id) {
		final AgentConfig config = new AgentConfig(id);
		final ArrayNode transports = JOM.createArrayNode();
		
		final WebsocketTransportConfig serverConfig = new WebsocketTransportConfig();
		serverConfig.setServer(true);
		serverConfig.setDoAuthentication(false);
		serverConfig.setAddress(WSBASEURL + id);
		
		serverConfig.setServletLauncher("JettyLauncher");
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8082);
		serverConfig.put("jetty", jettyParms);
		transports.add(serverConfig);
		
		final HttpTransportConfig httpConfig = new HttpTransportConfig();
		httpConfig.setServletUrl(BASEURL);
		httpConfig.setServletClass(DebugServlet.class.getName());
		httpConfig.setDoAuthentication(false);
		
		httpConfig.setServletLauncher("JettyLauncher");
		httpConfig.put("jetty", jettyParms);
		transports.add(httpConfig);
		
		config.setTransport(transports);
		
		setConfig(config);
		myInfo = new Info(getId());
	}
	
	/**
	 * Seen.
	 * 
	 * @param id
	 *            the id
	 * @param info
	 *            the info
	 */
	@Access(AccessType.PUBLIC)
	public void seen(final @Name("id") String id, final @Name("info") Info info) {
		
		// TODO: check if we know url, by contacting the agent and asking.
		final AsyncCallback<Info> callback = new AsyncCallback<Info>() {
			
			@Override
			public void onSuccess(Info result) {
				final ObjectNode params = JOM.createObjectNode();
				params.put("id", id);
				params.put("info", JOM.getInstance().valueToTree(result));
				try {
					send(new URI(WSCLIENTURL + getId()), "know", params);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(Exception exception) {
				System.err.println("onFailure called:");
				exception.printStackTrace();
			}
		};
		try {
			final ObjectNode params = JOM.createObjectNode();
			params.put("mine", JOM.getInstance().valueToTree(getMyInfo()));
			params.put("yours", JOM.getInstance().valueToTree(info));
			send(new URI(BASEURL + id), "getInfo", params, callback);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the info.
	 * 
	 * @param remoteInfo
	 *            the remote info
	 * @param newInfo
	 *            the new info
	 * @return the info
	 */
	@Access(AccessType.PUBLIC)
	public Info getInfo(final @Name("mine") Info remoteInfo,
			final @Name("yours") Info newInfo) {
		// TODO: Based on app specific info: return relevant information and
		// boolean "know you".
		final Info result = getMyInfo();
		result.setKnown(true);
		
		return newInfo.merge(result);
	}
	
	/**
	 * Gets the my info.
	 * 
	 * @return the my info
	 */
	public Info getMyInfo() {
		
		if (myInfo != null && lastUpdate != null && lastUpdate.plus(300000).isAfterNow()){
			return myInfo;
		}
		final ObjectNode params = JOM.createObjectNode();
		final SyncCallback<Info> callback = new SyncCallback<Info>() {
		};
		try {
			send(new URI(WSCLIENTURL + getId()), "getMyInfo", params, callback);
			myInfo = myInfo.merge(callback.get());
			lastUpdate = DateTime.now();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return myInfo;
	}
}
