package net.keiaxx.statussigns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.logging.Logger;

public class UpdateCount implements Runnable
	  {
	    
	    Logger log; //gets log from main
	    private boolean debug = StatusSigns.getDebug(); //gets the debug var from main
	    private String cmdpre = StatusSigns.getPrefix();//gets prefix from main
	    private Map<String, String> server = StatusSigns.server; // gets server from main
	    private String hostname1;
	    private int port1;
	    private int timeout = 5000;
	    private String name1;
	    
	    
	    public UpdateCount(String name){
        	String ip = server.get(name.toLowerCase()); 
        	String[] tosplit = ip.split(":");
        	hostname1 = tosplit[0];
        	port1 = Integer.parseInt(tosplit[1]);

	    	name1 = name;
	    }

	    public void run()
	    {
			int currplay = 0;
			try {
	        	   
	               Socket socket = new Socket(); 
	                OutputStream outputStream;
	                DataOutputStream dataOutputStream;
	                InputStream inputStream;
	                InputStreamReader inputStreamReader;

	                socket.setSoTimeout(timeout);

	                socket.connect(new InetSocketAddress(
	                        hostname1,
	                        port1), timeout);

	                outputStream = socket.getOutputStream();
	                dataOutputStream = new DataOutputStream(outputStream);

	                inputStream = socket.getInputStream();
	                inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-16BE"));

	                dataOutputStream.write(new byte[]{
	                            (byte) 0xFE,
	                            (byte) 0x01
	                        });

	                int packetId = inputStream.read();

	                if (packetId == -1) {
	                    throw new IOException("Premature end of stream.");
	                }

	                if (packetId != 0xFF) {
	                    throw new IOException("Invalid packet ID (" + packetId + ").");
	                }

	                int length = inputStreamReader.read();

	                if (length == -1) {
	                    throw new IOException("Premature end of stream.");
	                }

	                if (length == 0) {
	                    throw new IOException("Invalid string length.");
	                }

	                char[] chars = new char[length];

	                if (inputStreamReader.read(chars, 0, length) != length) {
	                    throw new IOException("Premature end of stream.");
	                }

	                String string = new String(chars);

	                if (string.startsWith("§")) {
	                    String[] data1 = string.split("\0");
	      
	                    currplay = Integer.parseInt(data1[4]);
	                } else {
	                    String[] data12 = string.split("§");
	                    currplay = Integer.parseInt(data12[1]);
	                }

	                dataOutputStream.close();
	                outputStream.close();

	                inputStreamReader.close();
	                inputStream.close();
	            
	        } catch (SocketException exception) {
	        } catch (IOException exception) {
	        }
			if (debug){
			log.info(cmdpre + " Updating PlayerCount for "+name1+": "+Integer.toString(currplay));
			}
			StatusSigns.players.put(name1, Integer.toString(currplay));
			this.debug = StatusSigns.getDebug();

		}
	  }