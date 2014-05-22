/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.demo.conferenceCloud;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.logging.Logger;

import com.almende.eve.capabilities.handler.Handler;
import com.almende.eve.transform.rpc.formats.JSONRPCException;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.ws.WebsocketTransportConfig;
import com.almende.eve.transport.ws.WsServerTransport;
import com.almende.eve.transport.ws.WsServerTransportFactory;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class SwitchBoard.
 */
public class SwitchBoard {
	private static final Logger			LOG			= Logger.getLogger(SwitchBoard.class
															.getName());
	private static final SwitchBoard	singleton	= new SwitchBoard();
	private WsServerTransport			server		= null;

	/**
	 * Instantiates a new switch board.
	 */
	public SwitchBoard() {
		final WebsocketTransportConfig serverConfig = new WebsocketTransportConfig();
		serverConfig.setAddress("ws://10.10.1.180:8082/ws/switchBoard");
		
		serverConfig.setServletLauncher("JettyLauncher");
		final ObjectNode jettyParms = JOM.createObjectNode();
		jettyParms.put("port", 8082);
		serverConfig.put("jetty", jettyParms);
		
		server = WsServerTransportFactory.get(serverConfig, new MyReceiver());
	}
	
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws JSONRPCException
	 *             the JSONRPC exception
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws InvocationTargetException
	 *             the invocation target exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public static void main(String[] args) throws JSONRPCException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException,
			ClassNotFoundException, InterruptedException {
		LOG.warning("starting:" + singleton.toString());
	}
	
	/**
	 * The Class myReceiver.
	 */
	public class MyReceiver implements Receiver, Handler<Receiver> {
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.almende.eve.transport.Receiver#receive(java.lang.Object,
		 * java.net.URI, java.lang.String)
		 */
		@Override
		public void receive(final Object msg, final URI senderUrl,
				final String tag) {
			
			for (final URI remote : server.getRemotes()){
				if (remote.equals(senderUrl)){
					continue;
				}
				try {
					LOG.warning("Forwarding message:"+msg+ " from:"+senderUrl+" to:"+remote);
					server.send(remote, (String)msg, null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.almende.eve.capabilities.handler.Handler#get()
		 */
		@Override
		public Receiver get() {
			return this;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.almende.eve.capabilities.handler.Handler#update(com.almende.eve
		 * .capabilities.handler.Handler)
		 */
		@Override
		public void update(final Handler<Receiver> newHandler) {
			// Not used, data should be the same.
		}
		
		/* (non-Javadoc)
		 * @see com.almende.eve.capabilities.handler.Handler#getKey()
		 */
		@Override
		public String getKey() {
			// Not used, data should be the same.
			return null;
		}
		
	}
	
}
