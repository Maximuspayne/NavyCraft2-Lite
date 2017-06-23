package com.maximuspayne.navycraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.EntityTracker;
import net.minecraft.server.v1_11_R1.EntityTrackerEntry;
import net.minecraft.server.v1_11_R1.EnumSkyBlock;
import net.minecraft.server.v1_11_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_11_R1.PlayerConnection;
import net.minecraft.server.v1_11_R1.WorldServer;

import org.bukkit.craftbukkit.v1_11_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
 
public class TeleportFix_1_11 implements Listener {
	
	

	public static void updateEntities(List<Player> observers) {
		
		// Refresh every single player
		for (Player player : observers) {
			updateEntity(player, observers);
		}
	}
	
	public static void updateEntity(Entity entity, List<Player> observers) {
 
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
 
	private static List<EntityHuman> getNmsPlayers(List<Player> players) {
		List<EntityHuman> nsmPlayers = new ArrayList<EntityHuman>();
 
		for (Player bukkitPlayer : players) {
			CraftPlayer craftPlayer = (CraftPlayer) bukkitPlayer;
			nsmPlayers.add(craftPlayer.getHandle());
		}
 
		return nsmPlayers;
	}

	public static void updateNMSChunks(Craft craft) {
		for (Chunk c : craft.chunkList ) {
			for (Player p : c.getWorld().getPlayers() ){
				//EntityPlayer ep = (EntityPlayer)p;
				
				
				//nmsChunk.a(EnumSkyBlock.BLOCK,craft.getLocation().getBlockX() & 0xF,
				//		craft.getLocation().getBlockY(),craft.getLocation().getBlockZ() & 0xF,15);
				
				PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
		        connection.sendPacket(new PacketPlayOutMapChunk(((CraftChunk) c).getHandle(), 1));
		        
	        }
		}
	}
	
	public static void updateNMSLight(Location light, Location oldLight) {
		if( light != null ) {
			Chunk c = light.getChunk();
			for (Player p : c.getWorld().getPlayers() ) {
				net.minecraft.server.v1_11_R1.Chunk nmsChunk = ((CraftChunk)p.getLocation().getChunk()).getHandle();
				Block block = c.getBlock(0, 0, 0);
				nmsChunk.a(EnumSkyBlock.BLOCK, new BlockPosition(block.getX(),block.getY(),block.getZ()),15);
				PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
		        connection.sendPacket(new PacketPlayOutMapChunk(((CraftChunk) c).getHandle(), 1));
			}
		}
		
		if( oldLight != null ) {
			Chunk c = light.getChunk();
			for (Player p : c.getWorld().getPlayers() ) {
				PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
		        connection.sendPacket(new PacketPlayOutMapChunk(((CraftChunk) c).getHandle(), 1));
			}
		}
	}
}
