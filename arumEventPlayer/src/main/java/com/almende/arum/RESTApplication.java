/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.arum;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Servlet;
import javax.ws.rs.core.Application;

import com.almende.eve.transport.http.embed.JettyLauncher;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The Class RESTApplication.
 */
public class RESTApplication extends Application {

	/*
	 * (non-Javadoc)
	 * @see javax.ws.rs.core.Application#getClasses()
	 */
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(RESTEndpoint.class);
		return classes;
	}

	/**
	 * Inits the.
	 */
	public static void init() {
		Servlet servlet = new org.apache.wink.server.internal.servlet.RestServlet();
		ObjectNode params = JOM.createObjectNode();
		ArrayNode initParams = JOM.createArrayNode();
		ObjectNode param = JOM.createObjectNode();
		param.put("key", "javax.ws.rs.Application");
		param.put("value", RESTApplication.class.getName());
		initParams.add(param);
		params.set("initParams", initParams);
		
		JettyLauncher launcher = new JettyLauncher();
		try {
			
			launcher.add(servlet,new URI("/rs/"),params);
			launcher.addFilter("com.thetransactioncompany.cors.CORSFilter", "/*");
			
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
