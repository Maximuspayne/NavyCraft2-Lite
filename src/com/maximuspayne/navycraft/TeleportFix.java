package com.maximuspayne.navycraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
 
public class TeleportFix implements Listener {
	
	private Server server;
	private Plugin plugin;
	
        // Try increasing this. May be dependent on lag.
	private final int TELEPORT_FIX_DELAY = 100; // ticks
	
	public TeleportFix(Plugin plugin, Server server) {
		this.plugin = plugin;
		this.server = server;
	}
 
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
 
		final Player player = event.getPlayer();
		final int visibleDistance = server.getViewDistance() * 16;
		
		// Fix the visibility issue one tick later
		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				// Refresh nearby clients
				
				String version;

		        version = Bukkit.getServer().getClass().getPackage().getName();
		        if (version.contains("v1_11")) {
		        	TeleportFix_1_11.updateEntities(getPlayersWithin(player, visibleDistance));

		        } else if (version.contains("v1_12")) {
		        	TeleportFix_1_12.updateEntities(getPlayersWithin(player, visibleDistance));
		        }
				
				//System.out.println("Applying fix ... " + visibleDistance);
			}
		}, TELEPORT_FIX_DELAY);
	}
	
	
	private List<Player> getPlayersWithin(Player player, int distance) {
		List<Player> res = new ArrayList<Player>();
		int d2 = distance * distance;
 
		for (Player p : server.getOnlinePlayers()) {
			if (p.getWorld() == player.getWorld()
					&& p.getLocation().distanceSquared(player.getLocation()) <= d2) {
				res.add(p);
			}
		}
 
		return res;
	}
}
