/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.arum;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import com.almende.util.ApacheHttpClient;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * The Class RESTEndpoint.
 */
@Path("/arum/")
public class RESTEndpoint {
	private static HttpClient	client	= ApacheHttpClient.get();
	private static ArrayNode oldData = null;
	
	/**
	 * Gets the job.
	 */
	public void getJob() {

		String url = "http://95.211.177.143:8080/arum/fnsd/production/jobs/";
		HttpGet httpGet = new HttpGet(URI.create(url));
		try {
			httpGet.addHeader("Accept", "application/json");
			HttpResponse resp = client.execute(httpGet);
			String responseBody = EntityUtils.toString(resp.getEntity());
			ArrayNode tree = (ArrayNode) JOM.getInstance().readTree(responseBody);
			
			if (oldData != null){
					
			}
			oldData = tree;
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (httpGet != null) {
				httpGet.reset();
			}
		}
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	@GET
	@Produces("text/plain")
	public String getMessage() {
		System.out.println("Something changed in the REST API at Arum!");
		//getJob();
		return "Thanks!";
	}
	
	/**
	 * Post message.
	 *
	 * @param json
	 *            the json
	 * @return the string
	 */
	@POST
	@Consumes("application/json")
	@Produces("text/plain")
	public String postMessage(String json) {
		System.out.println("Something changed in the REST API at Arum: "+json);
		//getJob();
		return "Thanks!";
	}
	
}
