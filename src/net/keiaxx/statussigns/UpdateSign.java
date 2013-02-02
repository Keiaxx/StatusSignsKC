package net.keiaxx.statussigns;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class UpdateSign implements Runnable
	  {
    private boolean debug = StatusSigns.getDebug(); //gets the debug var from main
    public String cmdpre = StatusSigns.getPrefix();       
    public Logger log = StatusSigns.log;
    public List<String> signloc = StatusSigns.signloc;
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
	    	String playeramt = StatusSigns.getPlayers(server);
        	
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
	    		this.debug = StatusSigns.getDebug();
	    	}
			
		}
	  }