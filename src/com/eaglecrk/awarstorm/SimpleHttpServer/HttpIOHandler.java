package com.eaglecrk.awarstorm.SimpleHttpServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class HttpIOHandler {

	public static void JsonResponse(HttpExchange t, String responseJson) throws IOException
	{
		t.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
        t.sendResponseHeaders(200, responseJson.length());
        OutputStream os = t.getResponseBody();
        os.write(responseJson.getBytes());
        os.close();		
	}
	public static void SimpleTextResponse(HttpExchange t, String responseText) throws IOException
	{
        t.sendResponseHeaders(200, responseText.length());
        OutputStream os = t.getResponseBody();
        os.write(responseText.getBytes());
        os.close();
	}
	public static void HtmlPageResponse(HttpExchange t, String filePath) throws IOException 
	{
		//get File from provided path
        File file = new File(filePath).getCanonicalFile();
        System.out.println(file.toString());
        
        if (!file.isFile()) {
          // Object does not exist or is not a file: reject with 404 error.
          String response = "404 (Not Found)\n";
          t.sendResponseHeaders(404, response.length());
          OutputStream os = t.getResponseBody();
          os.write(response.getBytes());
          os.close();
        } else {
          // Object exists and is a file: accept with response code 200.
          t.sendResponseHeaders(200, 0);
          OutputStream os = t.getResponseBody();
          FileInputStream fs = new FileInputStream(file);
          final byte[] buffer = new byte[0x10000];
          int count = 0;
          //stream buffer to output stream for response body
          while ((count = fs.read(buffer)) >= 0) {
            os.write(buffer,0,count);
          }
          //close file and output streams
          fs.close();
          os.close();
        }
	}
	
	
	public static Map<String,List<String>> GetPostParameters( HttpExchange t) throws IOException {
		// determine encoding
		Headers reqHeaders = t.getRequestHeaders();
		String contentType = reqHeaders.getFirst("Content-Type");
		String encoding = "ISO-8859-1";
		// read the query string from the request body
		String qry;
		InputStream in = t.getRequestBody();
		try {
			//read input Request body into byte array
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    byte buf[] = new byte[4096];
		    for (int n = in.read(buf); n > 0; n = in.read(buf)) {
		        out.write(buf, 0, n);
		    }
		    //output byte array into string with encoding
		    qry = new String(out.toByteArray(), encoding);
		} finally {
			//close input stream
		    in.close();
		}
		System.out.println(qry);
		// parse the query
		Map<String,List<String>> parms = new HashMap<String,List<String>>();
		//split query string on & to get individual keys
		String defs[] = qry.split("[&]");
		
		//Iterate over keys to decode key names and values
		for (String def: defs) {
			//get location of equals sign to split name and value apart
		    int ix = def.indexOf('=');
		    String name;
		    String value;
		    //equals not found, give value of empty string
		    if (ix < 0) {
		        name = URLDecoder.decode(def, encoding);
		        value = "";
		        //Url Decode string of name and value pair
		    } else {
		        name = URLDecoder.decode(def.substring(0, ix), encoding);
		        value = URLDecoder.decode(def.substring(ix+1), encoding);
		    }
		    //add name and values to list
		    List<String> list = parms.get(name);
		    if (list == null) {
		        list = new ArrayList<String>();
		        parms.put(name, list);
		    }
		    list.add(value);
		}
		//return parameter list
		return parms;
	}

}
