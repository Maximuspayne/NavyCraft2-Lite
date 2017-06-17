package com.maximuspayne.navycraft;

import java.util.ArrayList;
import java.util.List;
 

import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.EntityTracker;
import net.minecraft.server.v1_11_R1.EntityTrackerEntry;
import net.minecraft.server.v1_11_R1.WorldServer;
 

import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
 
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
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
				updateEntities(getPlayersWithin(player, visibleDistance));
				
				//System.out.println("Applying fix ... " + visibleDistance);
			}
		}, TELEPORT_FIX_DELAY);
	}
	
 
	public void updateEntities(List<Player> observers) {
		
		// Refresh every single player
		for (Player player : observers) {
			updateEntity(player, observers);
		}
	}
	
	public void updateEntity(Entity entity, List<Player> observers) {
 
		World world = entity.getWorld();
		WorldServer worldServer = ((CraftWorld) world).getHandle();
 
		EntityTracker tracker = worldServer.tracker;
		EntityTrackerEntry entry = (EntityTrackerEntry) tracker.trackedEntities
				.get(entity.getEntityId());
 
		List<EntityHuman> nmsPlayers = getNmsPlayers(observers);
 
		// Force Minecraft to resend packets to the affected clients
		entry.trackedPlayers.removeAll(nmsPlayers);
		entry.scanPlayers(nmsPlayers);
	}
 
	private List<EntityHuman> getNmsPlayers(List<Player> players) {
		List<EntityHuman> nsmPlayers = new ArrayList<EntityHuman>();
 
		for (Player bukkitPlayer : players) {
			CraftPlayer craftPlayer = (CraftPlayer) bukkitPlayer;
			nsmPlayers.add(craftPlayer.getHandle());
		}
 
		return nsmPlayers;
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
