package com.maximuspayne.navycraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EnumSkyBlock;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityTracker;
import net.minecraft.server.v1_12_R1.EntityTrackerEntry;
import net.minecraft.server.v1_12_R1.WorldServer;

import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
 
public class TeleportFix_1_12 implements Listener {
	
	

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
					//	craft.getLocation().getBlockY(),craft.getLocation().getBlockZ() & 0xF,15);
				
				PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
		        connection.sendPacket(new PacketPlayOutMapChunk(((CraftChunk) c).getHandle(), 20));
		        
	        }
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void updateNMSLight(Location light, Location oldLight) {
		if( oldLight != null ) {
			Chunk oc = oldLight.getChunk();
			ArrayList<net.minecraft.server.v1_12_R1.Chunk> chunks = new ArrayList<net.minecraft.server.v1_12_R1.Chunk>();
			
			for (Player p : oc.getWorld().getPlayers() ) {
				net.minecraft.server.v1_12_R1.Chunk nmsChunk = ((CraftChunk)oc).getHandle();
				Block block = oldLight.getBlock();

				for( int i=-3; i<=3; i++ ) {
					for( int j=-3; j<=3; j++ ) {
						for( int k=-3; k<=3; k++ ) {
							Location loc = new Location(block.getWorld(), block.getX()+i,block.getY()+j,block.getZ()+k);
							nmsChunk = ((CraftChunk)loc.getChunk()).getHandle();
							nmsChunk.a(EnumSkyBlock.BLOCK, 
									new BlockPosition(block.getX()+i,block.getY()+j,block.getZ()+k),0);
							if( !chunks.contains(nmsChunk) )
								chunks.add(nmsChunk);
						}
					}
						
				}
				for( net.minecraft.server.v1_12_R1.Chunk ck : chunks )
				{
					PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
		        	connection.sendPacket(new PacketPlayOutMapChunk(ck, 20));
				}
			}
			oldLight.getWorld().refreshChunk(oc.getX(), oc.getZ());
			oldLight.getWorld().refreshChunk(oc.getX()+1, oc.getZ()+1);
			oldLight.getWorld().refreshChunk(oc.getX()+1, oc.getZ());
			oldLight.getWorld().refreshChunk(oc.getX(), oc.getZ()+1);
			oldLight.getWorld().refreshChunk(oc.getX()-1, oc.getZ()-1);
			oldLight.getWorld().refreshChunk(oc.getX()-1, oc.getZ());
			oldLight.getWorld().refreshChunk(oc.getX(), oc.getZ()-1);
		}
		
		if( light != null ) {
			Chunk c = light.getChunk();
			ArrayList<net.minecraft.server.v1_12_R1.Chunk> chunks = new ArrayList<net.minecraft.server.v1_12_R1.Chunk>();
			
			for (Player p : c.getWorld().getPlayers() ) {
				net.minecraft.server.v1_12_R1.Chunk nmsChunk = ((CraftChunk)c).getHandle();
				Block block = light.getBlock();
				
				
				//nmsChunk.a(EnumSkyBlock.BLOCK, block.getX() & 0xF, block.getY() & 0xF, block.getZ() & 0xF,15);
				for( int i=-3; i<=3; i++ ) {
					for( int j=-3; j<=3; j++ ) {
						for( int k=-3; k<=3; k++ ) {
							int strength = (int) (15 - Math.sqrt((Math.abs(i)*Math.abs(i) + Math.abs(j)*Math.abs(j) + Math.abs(k)*Math.abs(k)))*3);
							Location loc = new Location(block.getWorld(), block.getX()+i,block.getY()+j,block.getZ()+k);
							nmsChunk = ((CraftChunk)loc.getChunk()).getHandle();
							nmsChunk.a(EnumSkyBlock.BLOCK, 
									new BlockPosition(block.getX()+i,block.getY()+j,block.getZ()+k),strength);
							if( !chunks.contains(nmsChunk) )
								chunks.add(nmsChunk);
						}
					}
						
				}
				
				for( net.minecraft.server.v1_12_R1.Chunk ck : chunks )
				{
					PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
		        	connection.sendPacket(new PacketPlayOutMapChunk(ck, 20));
				}
			}
			light.getWorld().refreshChunk(c.getX(), c.getZ());
			light.getWorld().refreshChunk(c.getX()+1, c.getZ()+1);
			light.getWorld().refreshChunk(c.getX()+1, c.getZ());
			light.getWorld().refreshChunk(c.getX(), c.getZ()+1);
			light.getWorld().refreshChunk(c.getX()-1, c.getZ()-1);
			light.getWorld().refreshChunk(c.getX()-1, c.getZ());
			light.getWorld().refreshChunk(c.getX(), c.getZ()-1);
		}
	}
}
