package com.maximuspayne.navycraft;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NavyCraft_Timer {
	//needs to be migrated to bukkitscheduler. meh.
	
	Plugin plugin;
	Timer timer;
	Craft craft;
	Player player;
	//public String state = "";
	public static HashMap<Player, NavyCraft_Timer> playerTimers = new HashMap<Player, NavyCraft_Timer>();
	public static HashMap<Craft, NavyCraft_Timer> takeoverTimers = new HashMap<Craft, NavyCraft_Timer>();
	public static HashMap<Craft, NavyCraft_Timer> abandonTimers = new HashMap<Craft, NavyCraft_Timer>();

	public NavyCraft_Timer(Plugin plug, int seconds, Craft vehicle, Player p, String state, boolean forward) {
		//toolkit = Toolkit.getDefaultToolkit();
		plugin = plug;
		this.craft = vehicle;
		this.player = p;
		timer = new Timer();
		if(state.equals("engineCheck"))
			timer.scheduleAtFixedRate(new EngineTask(), 1000, 1000);
		else if(state.equals("engineCheck"))
			timer.schedule(new AutoMoveTask(forward), 1000);
		else if(state.equals("abandonCheck"))
			timer.scheduleAtFixedRate(new ReleaseTask(), seconds * 1000, 60000);
		else if(state.equals("takeoverCheck"))
			timer.scheduleAtFixedRate(new TakeoverTask(), seconds * 1000, 60000);
		else if(state.equals("takeoverCaptainCheck"))
			timer.scheduleAtFixedRate(new TakeoverCaptainTask(), seconds * 1000, 60000);
	}
	
	public void SetState(String newState) {
		//state = newState;
	}
	
	public void Destroy() {
		timer.cancel();
		craft = null;
	}
	
	class EngineTask extends TimerTask {
		public void run() {
			if(craft == null)
				timer.cancel();
			else
				craft.engineTick();
			return;
		}
	}
	
	class AutoMoveTask extends TimerTask {
		boolean MovingForward = false;
		
		public void run() {
			craft.WayPointTravel(MovingForward);
			timer.schedule(new AutoMoveTask(MovingForward), 1000);
		}
		
		public AutoMoveTask(boolean Forward) {
			MovingForward = Forward;
		}
	}

	class ReleaseTask extends TimerTask {
		public void run() {
			/*
			if(state.equals("engineCheck")) {
				craft.engineTick();
			}else
			if(state.equals("abandonCheck")) {
			*/				
				if(craft != null && craft.isNameOnBoard.containsKey(player.getName()) ) {
					if( !craft.isNameOnBoard.get(player.getName()) )
						releaseCraftSync();
					
				}
				timer.cancel();
				return;
				
			//}
		}
	}
	
   public void releaseCraftSync()
    {
    	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
    	//new Thread() {
	  //  @Override
		    public void run()
		    {
		    	//MoveCraft.instance.unboardCraft(player, craft);
		    }
    	}
    	);
	 }
   
	class TakeoverTask extends TimerTask {
		public void run() {
			/*
			if(state.equals("engineCheck")) {
				craft.engineTick();
			}else
			if(state.equals("abandonCheck")) {
			*/				

				takeoverCraftSync();
					

				timer.cancel();
				return;
				
			//}
		}
	}
	
   public void takeoverCraftSync()
    {
    	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
    	//new Thread() {
	  //  @Override
		    public void run()
		    {
		    	if( craft.abandoned && player != null && player.isOnline() && craft.isOnCraft(player, false) )
		    	{
		    		craft.releaseCraft();
		    		player.sendMessage(ChatColor.YELLOW + "Vehicle released! Take command.");
		    	}else
		    	{
		    		player.sendMessage(ChatColor.YELLOW + "Takeover failed.");
		    	}
		    	
		    	
		    }
    	}
    	);
	 }
   
	class TakeoverCaptainTask extends TimerTask {
		public void run() {
			/*
			if(state.equals("engineCheck")) {
				craft.engineTick();
			}else
			if(state.equals("abandonCheck")) {
			*/				

				takeoverCaptainCraftSync();
					

				timer.cancel();
				return;
				
			//}
		}
	}
	
   public void takeoverCaptainCraftSync()
    {
    	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

    	//new Thread() {
	  //  @Override
		    public void run()
		    {
		    	if( craft.captainAbandoned && player != null && player.isOnline() && craft.isOnCraft(player, false) )
		    	{
		    		craft.releaseCraft();
		    		player.sendMessage(ChatColor.YELLOW + "Vehicle released! Take command.");
		    	}else
		    	{
		    		player.sendMessage(ChatColor.YELLOW + "Takeover failed.");
		    	}
		    	
		    	
		    }
    	}
    	);
	 }
   

}

