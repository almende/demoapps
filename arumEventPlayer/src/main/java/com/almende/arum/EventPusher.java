/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.arum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
	private static final Logger			LOG				= Logger.getLogger(EventPusher.class
																.getName());
	private static final EventPusher	SINGLETON		= new EventPusher();
	private static final URI			agentGenerator	= URI.create("wsclient:agentGenerator");

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

		/*
		 * var event = {
		 * time: new Date(), // timestamp of time of event
		 * performedBy: this.workers[0], // who is the agent who should handle
		 * the job
		 * type: 'worker', // type of the agent above, could be clear from name,
		 * then not needed
		 * assignment: 'makeMachine', // title of job
		 * operation: 'start', // start / finish / pause / resume/ endOfDay /
		 * startOfDay
		 * id: jobId, // unique job ID, not unique event id. Used to match
		 * operations on jobs
		 * prerequisites: [] // array of jobIds to watch (strings), or array of
		 * JSON objects containing agentId and type (corresponding to
		 * performedBy and assignments fields) or array of JSON objects only
		 * containing type.
		 */

	}

	/**
	 * Send event.
	 *
	 * @param event
	 *            the event
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void sendEvent(ObjectNode event) throws IOException {
		caller.call(agentGenerator, "receiveEvent", event);
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
		final String id = "events";
		final AgentConfig config = new AgentConfig(id);

		final ArrayNode transports = JOM.createArrayNode();
		final WebsocketTransportConfig serverConfig = new WebsocketTransportConfig();
		serverConfig.setId(id);
		serverConfig.setServer(true);
		serverConfig.setAddress("ws://" + host + "/ws/" + id);
		serverConfig.setServletLauncher("JettyLauncher");
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8082);
		serverConfig.set("jetty", jettyParms);
		transports.add(serverConfig);

		final HttpTransportConfig debugConfig = new HttpTransportConfig();
		debugConfig.setId(id);
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

	/**
	 * Load events.
	 *
	 * @param filename
	 *            the filename
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Access(AccessType.PUBLIC)
	public void loadEvents(@Name("filename") String filename, @Optional @Name("actuallySend") Boolean actuallySend)
			throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(
				filename));
		final DateTimeFormatter formatter = DateTimeFormat
				.forPattern("MM/dd/yyyy HH:mm:ss");

		String line = reader.readLine();
		LOG.warning("Reading line:" + line);
		while (line != null) {

			if (line.isEmpty()) {
				continue;
			}
			final ObjectNode event = JOM.createObjectNode();
			final String[] elements = line.split(",");
			if (!elements[0].equals("jobId")) {
				event.put("jobId", elements[0]);
				event.put("time", DateTime.parse(elements[1], formatter)
						.toString());
				event.put("performedBy", elements[2]);
				event.put("type", elements[3]);
				event.put("assignment", elements[4]);
				event.put("productId", elements[5]);
				event.put("operation", elements[6]);

				if (elements.length > 7) {
					final String[] requirements = elements[7].split(";");
					final ArrayNode reqs = JOM.createArrayNode();
					for (String req : requirements) {
						req = req.replaceAll("\"", "");
						if (req.contains("(")) {
							final ObjectNode r = JOM.createObjectNode();
							r.put("type",
									req.substring(0, req.indexOf("(") - 1));
							r.put("agentId",
									req.substring(req.indexOf("(") + 1,
											req.indexOf(")")));
							reqs.add(r);
						} else {
							final ObjectNode r = JOM.createObjectNode();
							r.put("type", req);
							reqs.add(r);
						}
					}
					event.set("prerequisites", reqs);
				}
				LOG.warning("Sending event:" + event.toString());
				if (actuallySend != null && actuallySend) {
					sendEvent(event);
				}
			}
			line = reader.readLine();

		}
		reader.close();
	}

}
