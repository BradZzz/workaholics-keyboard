package com.snaps.workaholics_emojikeyboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class ClassHttpTasks {

	//Make sure that the network is active and working...
	public String network_get_task(String url) throws IOException{
		
		BufferedReader in = null;
    String data = null;

    try{
           HttpClient httpclient = new DefaultHttpClient();
           HttpGet request = new HttpGet();
           URI website = new URI(url);
           request.setURI(website);
           HttpResponse response = httpclient.execute(request);
           in = new BufferedReader(new InputStreamReader(
                   response.getEntity().getContent()));

           StringBuilder sb = new StringBuilder();
 	         String line = null;
 	         while ((line = in.readLine()) != null) {
 	                sb.append(line + "\n");
 	         }
 	         data = sb.toString();
 	         in.close();
 	         return data;
    } catch (Exception e) {
    	in.close();
    	return "error";
    }
	}
}