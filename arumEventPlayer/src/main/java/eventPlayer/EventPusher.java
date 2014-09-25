/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package eventPlayer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.Enumeration;
import java.util.logging.Logger;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.scheduling.SimpleSchedulerConfig;
import com.almende.eve.transform.rpc.annotation.Access;
import com.almende.eve.transform.rpc.annotation.AccessType;
import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.eve.transform.rpc.annotation.Optional;
import com.almende.eve.transport.http.HttpTransportConfig;
import com.almende.eve.transport.ws.WebsocketTransportConfig;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class EventPusher.
 */
public class EventPusher extends Agent {
	private static final Logger LOG = Logger.getLogger(EventPusher.class
			.getName());
	private static final EventPusher	SINGLETON	= new EventPusher();
	private static final URI			monitor		= URI.create("wsclient:monitor");

	/**
	 * Send events.
	 *
	 * @param starttime
	 *            the starttime
	 * @param endtime
	 *            the endtime
	 */
	@Access(AccessType.PUBLIC)
	public void sendEvents(@Optional @Name("from") Long starttime,
			@Optional @Name("to") Long endtime) {

	}
	
	private String getHostAddress() throws SocketException {
		Enumeration<NetworkInterface> e = NetworkInterface
				.getNetworkInterfaces();
		while (e.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) e.nextElement();
			if (!n.isLoopback() && n.isUp() && !n.isVirtual()) {

				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					InetAddress i = (InetAddress) ee.nextElement();
					if (i instanceof Inet4Address && !i.isLinkLocalAddress()
							&& !i.isMulticastAddress()) {
						return i.getHostAddress().trim();
					}
				}
			}
		}
		return null;
	}

	
	/**
	 * Inits the Conference Cloud Agent.
	 */
	public void init() {
		String host;
		try {
			host = getHostAddress() + ":8082";
		} catch (SocketException e) {
			LOG.warning("Couldn't determine ipaddress, defaulting to 10.10.1.105");
			host = "10.10.1.105:8082";
		}
		final String id = "conductor";
		final AgentConfig config = new AgentConfig(id);

		final ArrayNode transports = JOM.createArrayNode();
		final WebsocketTransportConfig serverConfig = new WebsocketTransportConfig();
		serverConfig.setId("conductor");
		serverConfig.setServer(true);
		serverConfig.setAddress("ws://" + host + "/ws/" + id);
		serverConfig.setServletLauncher("JettyLauncher");
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8082);
		serverConfig.set("jetty", jettyParms);
		transports.add(serverConfig);

		final HttpTransportConfig debugConfig = new HttpTransportConfig();
		debugConfig.setId("conductor");
		debugConfig.setDoAuthentication(false);
		debugConfig.setServletUrl("http://" + host + "/www/");
		debugConfig
				.setServletClass("com.almende.eve.transport.http.DebugServlet");
		debugConfig.setServletLauncher("JettyLauncher");
		debugConfig.set("jetty", jettyParms);
		transports.add(debugConfig);

		config.setTransport(transports);

		final SimpleSchedulerConfig schedulerConfig = new SimpleSchedulerConfig();
		config.setScheduler(schedulerConfig);

		setConfig(config);

		LOG.warning("Started Conductor at:" + host);
	}
	
	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(final String[] args) {
		SINGLETON.init();
	}
	
}
