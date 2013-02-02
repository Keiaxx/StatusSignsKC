package net.keiaxx.statussigns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
public class StatusSigns extends JavaPlugin implements Listener{
	static Logger log;
	private static boolean debug = false; // debug messages

    private static String cmdpre = "[StatusSigns]"; //Chat prefix for the plugin
    static Map<String, String> server = new HashMap<String, String>(); //the servername and ip:port
    static Map<String, String> players = new HashMap<String, String>(); //hashmap for players per server
    Map<String, String> pingtask = new HashMap<String, String>(); //taskid for ping runnable
    List<Integer> alltasks = new ArrayList<Integer>(); //list of all task ID's so they can all be killed on plugin disable
    static List<String> signloc = new ArrayList<String>(); //list of all sign locations
    
    public void putString(String name, Integer currplay){
    	StatusSigns.players.put(name, Integer.toString(currplay));
    }
    
    public static String getPlayers(String server){
    	String players1 = StatusSigns.players.get(server.toLowerCase());
    	return players1;
    }  
    
    public static boolean getDebug(){
    	return debug;
    }
    
    public static String getPrefix(){
    	return cmdpre;
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
		StatusSigns.log = getLogger();
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
			  StatusSigns.signloc.add(id1);
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
    	  if(StatusSigns.debug){
    	  StatusSigns.debug=false;
    	  sender.sendMessage(ChatColor.RED + cmdpre + " Debug mode disabled");
    	  } else {
          StatusSigns.debug = true;
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
      if (cmd.getName().equalsIgnoreCase("statussigns") && (args[0].equalsIgnoreCase("players")) && sender.isOp()){
    	  for(Entry<String, String> plyrs : players.entrySet()){
  			String name = plyrs.getKey();
  			String ip = plyrs.getValue();
  			sender.sendMessage(ChatColor.GOLD+"Server: "+ChatColor.BLUE+name+ChatColor.GOLD+" Players: "+ChatColor.BLUE+ip);
  			return true;
  		}
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
        		StatusSigns.signloc.add(signlocation);
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
        	}else if (e.getLine(0).equalsIgnoreCase("[ServerInfo]")){
        		p.sendMessage(ChatColor.GREEN+cmdpre+" You must be an OP to create a Status sign!");
                e.getBlock().breakNaturally();
        }
       
   }



	


	    


}


