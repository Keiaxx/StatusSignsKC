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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class StatusSigns extends JavaPlugin implements Listener{
	Logger log;
	public boolean debug = false;
    int port = 25565;
    int timeout = 1000;
    int playersOnline = -1;
    int iter = 0;
    String cmdpre = "[StatusSigns]";
    Map<String, String> server = new HashMap<String, String>(); //the servername and ip:port
    Map<String, String> players = new HashMap<String, String>(); //hashmap for players per server
    Map<String, String> pingtask = new HashMap<String, String>(); //taskid for ping runnable
    List<Integer> alltasks = new ArrayList<Integer>();
    List<String> signloc = new ArrayList<String>();
    @SuppressWarnings("deprecation")
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
      if (cmd.getName().equalsIgnoreCase("statussigns") && (args.length < 1)) {
        
        sender.sendMessage(ChatColor.RED + cmdpre + " Running StatusSigns Version: 1.0 by Keiaxx");
        sender.sendMessage(ChatColor.RED + cmdpre + " Syntax: /statussigns set <name> <ip:port>");
        return true;
      }
      if (cmd.getName().equalsIgnoreCase("statussigns") && (args[0].equalsIgnoreCase("set")) && sender.isOp()){
    	  String name = args[1];
    	  String ip = args[2];
    	  
    	  server.put(name, ip);
  		  int pingtaskid = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this,
				new UpdateCount(name), 20L, 200L);
  		  pingtask.put(name, Integer.toString(pingtaskid));
  		  alltasks.add(pingtaskid);
  		sender.sendMessage(ChatColor.RED + cmdpre + " Added Server to List");
          return true;
      } else {
    	  sender.sendMessage(ChatColor.RED + cmdpre + " Syntax: /statussigns set <name> <ip:port>");
      }
      if (cmd.getName().equalsIgnoreCase("statussigns") && (args[0].equalsIgnoreCase("debug")) && sender.isOp()){
    	  if(this.debug){
    	  this.debug=false;
    	  sender.sendMessage(ChatColor.RED + cmdpre + " Debug mode disabled");
    	  } else {
          this.debug = true;
    	  sender.sendMessage(ChatColor.RED + cmdpre + " Debug mode enabled");
    	  }
          return true;
      }
      if (cmd.getName().equalsIgnoreCase("statussigns") && (args[0].equalsIgnoreCase("list")) && sender.isOp()){
    	  sender.sendMessage(ChatColor.RED +cmdpre + " Available Servers " + pingtask.keySet());
          return true;
      }
      if (cmd.getName().equalsIgnoreCase("statussigns") && (args[0].equalsIgnoreCase("delete")) && sender.isOp()){
    	  String whichserver = args[1];  
    	  String taski1d = pingtask.get(whichserver);
  		int taskid = Integer.parseInt(taski1d);
  		Bukkit.getScheduler().cancelTask(taskid);
  		server.remove(whichserver);
  	    players.remove(whichserver);
  	    pingtask.remove(whichserver);
    	  sender.sendMessage(ChatColor.RED + cmdpre + " Removed server "+whichserver);
          return true;
      }
      return false;
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onSignCreate(SignChangeEvent e){
    Player p = e.getPlayer();
        if(e.getLine(0).equalsIgnoreCase("[ServerInfo]") && p.isOp()){
        	String servername = e.getLine(1);
        	String playeramt = players.get(servername.toLowerCase());
        	Player player = e.getPlayer();
        	Block b = e.getBlock();
            String world = b.getWorld().getName();
            double x = b.getX();
            double y = b.getY();
            double z = b.getZ();
        	String signlocation = servername+":"+world+":"+x+":"+y+":"+z;
        	
        	if (playeramt == null){
        		player.sendMessage(ChatColor.GREEN + cmdpre + " Server "+servername+" has not been set! Use /statussigns to set the server.");
        	} else {
        		signloc.add(signlocation);
        		UpdateSign us = new UpdateSign(signlocation);
	    		int signtask1 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this,
	    				us, 0L, 200L);
	    		us.setId(signtask1);
	    		alltasks.add(signtask1);
	            player.sendMessage(ChatColor.GREEN + cmdpre + " Sign Created!");
	            
	            if (debug){
	            	log.info(cmdpre + " Debug: Sign Created at "+signlocation+"with taskID: "+signtask1);
	            }

        	} 
        	}else {
        		p.sendMessage(ChatColor.GREEN+cmdpre+" You must be an OP to create a Status sign!");
        
        }
       
   }


	@Override
	public void onDisable() {
		Iterator<Integer> taskiterate = alltasks.iterator(); 
		while(taskiterate.hasNext()){
	    int taskid = taskiterate.next(); 
		Bukkit.getScheduler().cancelTask(taskid);
		if (debug){
		log.info("[StatusSigns Debug] Cancelling task ID : "+taskid);
		}
		}
		for(Entry<String, String> server2 : server.entrySet()){
			String name = server2.getKey();
			String ip = server2.getValue();
		getConfig().set("servers."+name, ip);
		}
		signloc.add("default");
	    getConfig().set("signs", signloc);
		saveConfig();
		log.info("[StatusSigns by Keiaxx] Plugin Disabled!");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		this.log = getLogger();
		this.saveDefaultConfig();
		
		for (String server1 : getConfig().getConfigurationSection("servers").getKeys(true)) {
			  String servername = server1;
			  String ip = getConfig().getString("servers." + servername);
			  
			  if (!servername.equals("default")){
			  server.put(servername, ip);
			  int pingtaskid = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this,
						new UpdateCount(servername), 20L, 200L);
		  		  pingtask.put(servername, Integer.toString(pingtaskid));
		  		  alltasks.add(pingtaskid);
		  		log.info("[StatusSigns by Keiaxx] Ping task starting for server: "+servername);  
			  }
			}
		
		for (String id : getConfig().getStringList("signs")) {
			  String id1 = id;

	        	String[] tosplit = id1.split(":");
	        	String name = tosplit[0];
	        if(!name.equals("default")){
			  if (id1 != null){
			  signloc.add(id1);
			  UpdateSign us = new UpdateSign(id1);
	    		int signtask1 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this,
	    				us, 0L, 200L);
	    		us.setId(signtask1);
 
		  		  alltasks.add(signtask1);
		  		log.info("[StatusSigns by Keiaxx] Sign Loaded at location: "+id1);  

			  }
	        }
			}
	
		getServer().getPluginManager().registerEvents(this, this);
		log.info("[StatusSigns by Keiaxx] Plugin Enabled!");
		
	}
	

	    class UpdateSign
	    implements Runnable
	  {
	   UpdateSign()
	    {
	    }
	    public String sid;
	    public UpdateSign(String SignID){
	    	sid = SignID;

	    }
	    private int id;
	    
	    public int getId() {
	        return id;
	    }
	   
	    public void cancel() {
	        Bukkit.getScheduler().cancelTask(id);
	    }
	       
	    public void setId(int id) {
	        this.id = id;
	    }

	    public void run()
	    {
	    	String signinfo = sid;
	        
	    	

	    	
        	String[] tosplit = signinfo.split(":");
        	String server = tosplit[0];
        	String bW = tosplit[1];
        	double bX = Double.parseDouble(tosplit[2]);
        	double bY = Double.parseDouble(tosplit[3]);
        	double bZ = Double.parseDouble(tosplit[4]);
        	
        	Location loc = new Location(Bukkit.getServer().getWorld(bW), bX, bY, bZ);
        	Block block = loc.getBlock();
	    	String playeramt = players.get(server.toLowerCase());
        	
	    	if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN){
	           Sign sign = (Sign)block.getState();
	           sign.setLine(0, ChatColor.GOLD+"[ServerInfo]");
	           sign.setLine(1, ChatColor.DARK_BLUE+server);
	           sign.setLine(2, ChatColor.GREEN+"~~~Players~~~");
	           sign.setLine(3, ChatColor.YELLOW+playeramt);
	           sign.update();
	    		if (debug){
	                log.info(cmdpre + " Updating sign at location: "+sign+" With task ID: "+this.getId());
	                
		    		}
	    	} else {
	    		this.cancel();
	    		for(Iterator<String> itr = signloc.iterator();itr.hasNext();)
	    		{
	    			String element = itr.next();
	    			if(sid.equals(element))
	    			{
	    				itr.remove();
	    	    		if (debug){
	    	                log.info(cmdpre + "Current list : "+signloc);
	    	                
	    		    		}
	    			}
	    		}
	    		if (debug){
                log.info(cmdpre + " Task "+getId()+" Sign removed from "+block);
                
	    		}
	    	}
			
		}
	  }
	    
	    class UpdateCount
	    implements Runnable
	  {
	   UpdateCount()
	    {
	    }
	    public String hostname1;
	    public int port1;
	    public int timeout = 1000;
	    public int id1;
	    public String name1;
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
			players.put(name1, Integer.toString(currplay));

		
		}
	  }

}


