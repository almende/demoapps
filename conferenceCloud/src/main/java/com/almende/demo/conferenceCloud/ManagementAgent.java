/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceCloud;

import java.util.HashMap;
import java.util.Map;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.eve.transport.ws.WebsocketTransportConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class ConferenceCloudAgent.
 */
public class ManagementAgent extends Agent {
	private static final ManagementAgent					singleton	= new ManagementAgent();
	private static final Map<String, ConferenceCloudAgent>	agents		= new HashMap<String, ConferenceCloudAgent>();
	
	/**
	 * Inits the Conference Cloud Agent.
	 */
	public void init() {
		final String id = "management";
		final AgentConfig config = new AgentConfig(id);
		
		final WebsocketTransportConfig serverConfig = new WebsocketTransportConfig();
		serverConfig.setServer(true);
		serverConfig.setAddress("ws://10.10.1.180:8082/ws/" + id);
		
		serverConfig.setServletLauncher("JettyLauncher");
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8082);
		serverConfig.put("jetty", jettyParms);
		
		config.setTransport(serverConfig);
		
		setConfig(config);
		
	}
	
	/**
	 * Register agent.
	 * 
	 * @param id
	 *            the id
	 * @return true, if successful
	 */
	@Access(AccessType.PUBLIC)
	public boolean registerAgent(final @Name("id") String id) {
		System.err.println("Registering agent:"+id);
		if (!agents.containsKey(id)) {
			System.err.println("new!");
			final ConferenceCloudAgent agent = new ConferenceCloudAgent();
			agent.init(id);
			agents.put(id, agent);
		}
		return true;
	}
	
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(final String[] args) {
		singleton.init();
	}
}
