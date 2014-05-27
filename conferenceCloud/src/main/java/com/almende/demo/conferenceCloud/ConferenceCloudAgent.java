/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceCloud;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.eve.transport.http.HttpTransportConfig;
import com.almende.eve.transport.http.debug.DebugServlet;
import com.almende.eve.transport.ws.WebsocketTransportConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class ConferenceCloudAgent.
 */
public class ConferenceCloudAgent extends Agent {
	
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
		serverConfig.setAddress("ws://10.10.1.180:8082/ws/" + id);
		
		serverConfig.setServletLauncher("JettyLauncher");
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8082);
		serverConfig.put("jetty", jettyParms);
		transports.add(serverConfig);
		
		final HttpTransportConfig httpConfig = new HttpTransportConfig();
		httpConfig.setServletUrl("http://10.10.1.180:8082/agents/");
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
		
		System.err.println("Mobile has seen:"+id);
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
