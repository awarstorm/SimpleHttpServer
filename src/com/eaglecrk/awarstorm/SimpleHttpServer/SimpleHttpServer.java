package com.eaglecrk.awarstorm.SimpleHttpServer;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

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
        server.createContext("/create", new DbCreateHandler());
        server.createContext("/list", new ListHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
    static class ListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
    		Connection c = DatabaseHandler.connect("jdbc:sqlite:/sqlite/db/sum.db");
    		DatabaseHandler.createTableIfNotExists(c);
        	ResultSet rs = DatabaseHandler.getList(c, 0);
        	String responseJson;
			try {
				responseJson = SqlExporter.SqlRstoJsonArray(rs).toString();
				HttpIOHandler.JsonResponse(t, responseJson);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        }
    }
    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
        	HttpIOHandler.SimpleTextResponse(t, "This only a Test");
        }
    }
    
    static class DbCreateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
        	DatabaseHandler.createNewDatabase("/sqlite/db/sum.db");
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
        		int intSum = AddingModule.sum(intInput1, intInput2);
        		HttpIOHandler.SimpleTextResponse(t, "Sum: " + intSum );
        		Connection c = DatabaseHandler.connect("jdbc:sqlite:/sqlite/db/sum.db");
        		DatabaseHandler.createTableIfNotExists(c);
        		DatabaseHandler.insertSum(c, intInput1, intInput2, intSum);
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