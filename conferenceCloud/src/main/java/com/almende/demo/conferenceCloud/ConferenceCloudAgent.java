/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceCloud;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.eve.transport.http.HttpTransportConfig;
import com.almende.eve.transport.http.debug.DebugServlet;
import com.almende.eve.transport.ws.WebsocketTransportConfig;
import com.almende.util.callback.AsyncCallback;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class ConferenceCloudAgent.
 */
public class ConferenceCloudAgent extends Agent {
	
	private static final String BASEURL = "http://10.10.1.180:8082/agents/"; 
	private static final String WSBASEURL = "ws://10.10.1.180:8082/ws/";
	private static final String WSCLIENTURL = "wsclient:";
	
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
	}
	
	/**
	 * Seen.
	 * 
	 * @param id
	 *            the id
	 */
	@Access(AccessType.PUBLIC)
	public void seen(final @Name("id") String id) {
		// TODO: check if we know url, by contacting the agent and asking.
		final Info myInfo = new Info();
		final AsyncCallback<Info> callback = new AsyncCallback<Info>(){

			@Override
			public void onSuccess(Info result) {
				final ObjectNode params = JOM.createObjectNode();
				params.put("id", id);
				params.put("info", JOM.getInstance().valueToTree(result));
				try {
					send(new URI(WSCLIENTURL+getId()),"know",params);
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
			params.put("info",JOM.getInstance().valueToTree(myInfo));
			send(new URI(BASEURL+id),"getInfo",params,callback);
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
	 * @return the info
	 */
	@Access(AccessType.PUBLIC)
	public Info getInfo(final @Name("info") Info remoteInfo) {
		// TODO: Based on app specific info: return relevant information and
		// boolean "know you".
		final Info result = new Info();
		result.setKnown(true);
		return result;
	}
	
}
