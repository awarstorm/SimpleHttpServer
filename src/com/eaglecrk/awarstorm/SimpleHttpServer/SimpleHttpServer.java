package com.eaglecrk.awarstorm.SimpleHttpServer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.eaglecrk.awarstorm.modules.AddingModule;

public class SimpleHttpServer {
	public static HttpServer server;

    public static void main(String[] args) throws Exception {
        server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new MyHandler());
        server.createContext("/close", new CloseHandler());
        server.createContext("/add", new AddHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
        	HttpIOHandler.SimpleTextResponse(t, "This only a Test");
        }
    }
    static class AddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
        	String requestMethod = t.getRequestMethod();
        	if(requestMethod.equalsIgnoreCase("GET"))
        	{
	            String root = "/www";
	            URI uri = t.getRequestURI();
	            String filePath = root + uri.getPath()+"/default.html";
	            HttpIOHandler.HtmlPageResponse(t, filePath);
	        }
        	else if (requestMethod.equalsIgnoreCase("POST")) 
        	{
        		Map<String,List<String>> parms = HttpIOHandler.GetPostParameters(t);
        		System.out.println(parms.get("inputNumber1").get(0));
        		System.out.println(parms.get("inputNumber2").get(0));
        		int intInput1 = Integer.parseInt(parms.get("inputNumber1").get(0));
        		int intInput2 = Integer.parseInt(parms.get("inputNumber2").get(0));
        		
        		HttpIOHandler.SimpleTextResponse(t, "Sum: " + AddingModule.sum(intInput1, intInput2));
        		
        	}
        }
    }
   
    static class CloseHandler implements HttpHandler {
    	@Override
        public void handle(HttpExchange t) throws IOException {
            String response = "Closing";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            server.stop(3);
    	}
    }
}