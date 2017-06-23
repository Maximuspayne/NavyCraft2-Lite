package com.maximuspayne.navycraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.maximuspayne.aimcannon.AimCannon;
import com.maximuspayne.aimcannon.OneCannon;

public class CraftRotator {
	public Plugin plugin;
	public Craft craft;
	public int newMinX, newMinZ, newOffX, newOffZ;
	
	HashMap<Location,Location> cannonLocs;  //newLocCraftcoord, oldLoc world coord

    //offset between the craft origin and the pivot for rotation

	public CraftRotator(Craft c, Plugin p) {
		craft = c;
		plugin = p;
		cannonLocs = new HashMap<Location,Location>();
		if(craft.offX == 0 || craft.offZ == 0) {
			craft.offX = Math.round(craft.sizeX / 2);
			craft.offZ = Math.round(craft.sizeZ / 2);
		}
	}

	// Gathering up pivot data as a Location
	// This should be how things are stored in Craft.
	public Location getPivot(){
		double x = craft.minX + craft.offX;
		double z = craft.minZ + craft.offZ;
		Location pivot = new Location(craft.world, x, craft.minY, z, craft.rotation, 0);

		return pivot;
	}

	public Vector getCraftSize(){
		Vector craftSize = new Vector(craft.sizeX,craft.sizeY,craft.sizeZ);
		return craftSize;
	}

	public boolean canGoThrough(int blockId){
		/** if the craft can go through this block id */

		//all craft types can move through air
		if(blockId == 0) return true;

		if(!craft.type.canNavigate && !craft.type.canDive)
			return false;

		//ship on water
		if(blockId == 8 || blockId == 9)
			if(craft.waterType == 8) return true;

		//ship on lava
		if(blockId == 10 || blockId == 11)
			if(craft.waterType == 10) return true;

		//iceBreaker can go through ice :)
		if(blockId == 79 && (craft.type.canNavigate || craft.type.canDive) )
			if(craft.waterType == 8) return true;

		return false;
	}

	public Location rotate(Entity ent, int r, int minX, int minZ, int offX, int offZ){
		return rotate(ent.getLocation(),r,true, minX, minZ, offX, offZ);
	}

	public Location rotate(Location point, int r, int minX, int minZ, int offX, int offZ){
		return rotate(point,r,false, minX, minZ, offX, offZ);
	}
	//will replace other rotates
	public Location rotate(Location point, int r, boolean isEntity, int minX, int minZ, int offX, int offZ){
		@SuppressWarnings("unused")
		Location entOffset;
		if (isEntity){
			entOffset = new Location(craft.world, 0.5, 0.0, 0.5);
		}
		else entOffset = new Location(craft.world, 0.0, 0.0, 0.0);
		//Location pivot = this.getPivot().add(entOffset);
		Location newPoint = point.clone();
		//newPoint = point.subtract(pivot);// make point relative to pivot
		NavyCraft.instance.DebugMessage("r " + r, 2);
		NavyCraft.instance.DebugMessage("newPoint1 " + newPoint, 2);

		
		
		newPoint.setX(minX + rotateX(point.getX() - offX, point.getZ() - offZ, r));
		newPoint.setZ(minZ + rotateZ(point.getX() - offX, point.getZ() - offZ, r));

		//return(newPoint.add(pivot));// make newPoint relative to world
		return newPoint;
	}



	public static double rotateX(double x, double z, int r){
		if(r==0)
			return x;
		else if(r==90)
			return -z;
		else if(r==180)
			return -x;
		else if(r==270)
			return z;
		else return x;
	}

	public static double rotateZ(double x, double z, int r){
		/** get the corresponding world z coordinate */
		if(r==0)
			return z;
		else if(r == 90)
			return x;
		else if(r==180)
			return -z;
		else if(r==270)
			return -x;
		else
			return z;
	}

	public int rotateX(int x, int z, int r){
		/** get the corresponding world x coordinate */

		NavyCraft.instance.DebugMessage("r is " + r +
				", x is " + x +
				", z is " + z, 4);

		if(r==0)
			return x;
		else if(r==90)
			return -z;
		else if(r==180)
			return -x;
		else if(r==270)
			return z;
		else return x;
		
	}

	public static int rotateZ(int x, int z, int r){
		/** get the corresponding world z coordinate */
		if(r==0)
			return z;
		else if(r==90)
			return x;
		else if(r==180)
			return -z;
		else if(r==270)
			return -x;
		else
			return z;
	}

	//setblock, SAFE !
	@SuppressWarnings("deprecation")
	public void setBlock(double id, int X, int Y, int Z) {
		if(Y < 0 || Y > 255 || id < 0 || id > 255){
			return;
		}

		if((id == 64 || id == 63) && NavyCraft.instance.DebugMode) {
			System.out.println("This stack trace is totally expected.");

			new Throwable().printStackTrace();

		}

		craft.world.getBlockAt(X, Y, Z).setTypeId((int)id);
	}

	public void setBlock(double id, int x, int y, int z, int dx, int dy, int dz, int r) {
		int X = craft.minX + rotateX(x, z, r) + dx;
		int Y = craft.minY + y + dy;
		int Z = craft.minZ + rotateZ(x, z, r) + dz;

		setBlock(id, X, Y, Z);
	}

	@SuppressWarnings("deprecation")
	public void setDataBlock(short id, byte data, int X, int Y, int Z) {
		if(Y < 0 || Y > 255 || id < 0 || id > 255){
			return;
		}

		craft.world.getBlockAt(X, Y, Z).setTypeId(id);
		craft.world.getBlockAt(X, Y, Z).setData(data);
	}

	@SuppressWarnings("deprecation")
	public short getWorldBlockId(int x, int y, int z, int r, int minX, int minY, int minZ, int offX, int offZ){
		/** get world block id with matrix coordinates and rotation */
		short blockId;
		
		blockId = (short) craft.world.getBlockTypeIdAt(minX + x,
				minY + y,
				minZ + z);

		return blockId;
	}

	public short getCraftBlockId(int x, int y, int z, int r){

		
		if(!(x >= craft.minX && x < craft.sizeX+craft.minX &&
				y >= 0 && y < craft.sizeY &&
				z >= craft.minZ && z < craft.sizeZ+craft.minZ))
			return 255;

		return -1;
	}

	public boolean canMoveBlocks(int dx, int dy, int dz, int dr){

		//new rotation of the craft
		int newRotation = (dr + 360) % 360;


		//rotate dimensions
		Vector newSize = this.getCraftSize().clone();

		if(dr == 90 ||dr == 270)
		{
			newSize.setX(this.getCraftSize().getZ());
			newSize.setZ(this.getCraftSize().getX());
		}

		//new matrix
		short newMatrix[][][] = new short[newSize.getBlockX()][newSize.getBlockY()][newSize.getBlockZ()];

		
		//rotate matrix
		for(int x=0; x < newSize.getBlockX(); x++){
			for(int y=0; y < newSize.getBlockY(); y++){
				for(int z=0; z < newSize.getBlockZ(); z++){
					int newX = 0;
					int newZ = 0;
					if(dr == 90) {
						newX = z;
						newZ = newSize.getBlockX() - 1 - x;
					} else if(dr == 270){
						newX = newSize.getBlockZ() - 1 - z;
						newZ = x;
					} else {
						newX = newSize.getBlockX() - 1 - x;
						newZ = newSize.getBlockZ() - 1 - z;
					}

					newMatrix[x][y][z] = craft.matrix[newX][y][newZ];
				}
			}
		}
		

		//craft pivot
		int posX = craft.minX + craft.offX;
		int posZ = craft.minZ + craft.offZ;


		int newoffsetX = rotateX(craft.offX, craft.offZ, dr);
		int newoffsetZ = rotateZ(craft.offX, craft.offZ, dr);


		if(newoffsetX < 0)
			newoffsetX = newSize.getBlockX() - 1 - Math.abs(newoffsetX);
		if(newoffsetZ < 0)
			newoffsetZ = newSize.getBlockZ() - 1 - Math.abs(newoffsetZ);


		//update min/max
		int newminX, newminZ;
		newminX = posX - newoffsetX;
		newminZ = posZ - newoffsetZ;

		for(int x=0;x<newSize.getBlockX();x++){
			for(int z=0;z<newSize.getBlockZ();z++){
				for(int y=0;y<newSize.getBlockY();y++){
					//all blocks new craft.positions needs to have a free space before
					if(newMatrix[x][y][z]!=255){ //before move : craft block

						if(getCraftBlockId(x + dx + newminX, y + dy, z + dz + newminZ, dr) == 255){
							if(!canGoThrough(getWorldBlockId(x + dx, y + dy, z + dz, newRotation, newminX, craft.minY, newminZ, newoffsetX, newoffsetZ))){

								return false;
							}
						}
					}
				}
			}
		}

		return true;
	}

	public void turn(int dr) {

		if( craft.waitTorpLoading > 0 )
		{
			if( craft.driverName != null )
			{
				Player p = plugin.getServer().getPlayer(craft.driverName);
				if( p != null )
					p.sendMessage(ChatColor.RED + "Torpedo Reloading Please Wait.");
			}
			return;
		}
		
		CraftMover cm = new CraftMover(craft, plugin);
		cm.structureUpdate(null,false);
		
		if( craft.sinking )
		{
			if( craft.driverName != null )
			{
				Player p = plugin.getServer().getPlayer(craft.driverName);
				if( p != null )
					p.sendMessage(ChatColor.RED + "You are sinking!");
			}
			return;
		}

		
		if( !canMoveBlocks(0,0,0,dr) )
		{
			if( craft.driverName != null )
			{
				Player p = plugin.getServer().getPlayer(craft.driverName);
				if( p != null )
					p.sendMessage("Turn Blocked");
			}
			return;
		}
		
		dr = (dr + 360) % 360;

		ArrayList<Entity> craftEntities = craft.getCraftEntities(false);
		HashMap<Entity, Location> entPreLoc = new HashMap<Entity, Location>();

		// determine where entities will be placed
		for (Entity e : craftEntities) {
			Location oldLoc = e.getLocation();
			int newoffsetX = (int)rotateX( (double)(oldLoc.getBlockX() -(craft.minX+craft.offX)), (double)(oldLoc.getBlockZ() - (craft.minZ+craft.offZ)), dr);
			int newoffsetZ = (int)rotateZ( (double)(oldLoc.getBlockX() -(craft.minX+craft.offX)), (double)(oldLoc.getBlockZ() - (craft.minZ+craft.offZ)), dr);

			NavyCraft.instance.DebugMessage("New off is " + newoffsetX + ", " + newoffsetZ, 2);
			
			entPreLoc.put(e, new Location(craft.world, newoffsetX, e.getLocation().getY(), newoffsetZ));
		}
		
		
		Vector moveBy = new Vector(0, 0, 0);
		moveBlocks(moveBy, dr);
		
		
		//tp all players in the craft area
		for (Entity e : craftEntities) {
			Player p=null;
			if( craft.driverName != null )
				p = plugin.getServer().getPlayer(craft.driverName);
			if(p != null && e != p)
				entPreLoc.get(e).setYaw(entPreLoc.get(e).getYaw() + dr);
			NavyCraft.instance.DebugMessage("teleporting " + entPreLoc.get(e), 2);
				//craft.player.sendMessage("tp coord=" + (double)(entPreLoc.get(e).getX() + craft.minX + craft.offX + 0.5) + "," + (double)(entPreLoc.get(e).getZ() + craft.minZ + craft.offZ + 0.5) );

			e.teleport(new Location(craft.world, (double)(entPreLoc.get(e).getX() + craft.minX + craft.offX + 0.5), entPreLoc.get(e).getY(), (double)(entPreLoc.get(e).getZ() + craft.minZ + craft.offZ + 0.5), e.getLocation().getYaw(), e.getLocation().getPitch() ));
		}

		craft.rotation += dr;
		if(craft.rotation > 360)
			craft.rotation -= 360;
		else if(craft.rotation < 0)
			craft.rotation = 360 - Math.abs(craft.rotation);

	}

	@SuppressWarnings("deprecation")
	public void moveBlocks(Vector moveBy, int dr){
		/** move the craft according to a vector d
		wdx : world delta x
		wdy : world delta y
		wdz : world delta z
		dr : delta rotation (90, -90) */


		dr = (dr + 360) % 360;

		CraftMover cm = new CraftMover(craft, plugin);

		//rotate dimensions
		Vector newSize = this.getCraftSize().clone();
		//int newSizeX = craft.sizeX;
		//int newSizeZ = craft.sizeZ;

		if(dr == 90 ||dr == 270){

			newSize.setX(this.getCraftSize().getZ());
			newSize.setZ(this.getCraftSize().getX());

		}

		//new matrix

		short newMatrix[][][] = new short[newSize.getBlockX()][newSize.getBlockY()][newSize.getBlockZ()];

		//store data blocks
		cm.storeDataBlocks();
		cm.storeComplexBlocks();
		


		ArrayList<DataBlock> unMovedDataBlocks = new ArrayList<DataBlock>();
		ArrayList<DataBlock> unMovedComplexBlocks = new ArrayList<DataBlock>();

		while(craft.dataBlocks.size() > 0) {
			unMovedDataBlocks.add(craft.dataBlocks.get(0));
			craft.dataBlocks.remove(0);
		}
		while(craft.complexBlocks.size() > 0) {
			unMovedComplexBlocks.add(craft.complexBlocks.get(0));
			craft.complexBlocks.remove(0);
		}

			
		//rotate matrix
		for(int x=0; x < newSize.getBlockX(); x++){
			for(int y=0; y < newSize.getBlockY(); y++){
				for(int z=0; z < newSize.getBlockZ(); z++){
					int newX = 0;
					int newZ = 0;
					if(dr == 90) {
						newX = z;
						newZ = newSize.getBlockX() - 1 - x;
					} else if(dr == 270){
						newX = newSize.getBlockZ() - 1 - z;
						newZ = x;
					} else {
						newX = newSize.getBlockX() - 1 - x;
						newZ = newSize.getBlockZ() - 1 - z;
					}

					newMatrix[x][y][z] = craft.matrix[newX][y][newZ];

					for(int i = 0; i < unMovedDataBlocks.size(); i ++ ) {
						DataBlock dataBlock = unMovedDataBlocks.get(i);
						if(dataBlock.locationMatches(newX, y, newZ)) {
							
							///check if cannon
							if( dataBlock.id == 23)
							{
								Location cannonLoc = new Location(craft.world, dataBlock.x + craft.minX, dataBlock.y + craft.minY, dataBlock.z + craft.minZ);
								for (OneCannon onec : AimCannon.getCannons()) 
								{
									if (onec.isThisCannon(cannonLoc, false)) 
									{
										cannonLocs.put(new Location(craft.world,x, y, z),cannonLoc);
									}
								}

							}
							
							//check if sponge
							if( dataBlock.id == 19)
							{
								Location spongeLoc = new Location(craft.world, dataBlock.x + craft.minX, dataBlock.y + craft.minY, dataBlock.z + craft.minZ);
								for (Pump p :craft.pumps) 
								{
									if (spongeLoc.equals(p.loc)) 
									{
										p.loc = new Location(craft.world,x, y, z);
									}
								}

							}
							
							dataBlock.x = x;
							dataBlock.z = z;

							craft.dataBlocks.add(dataBlock);
							unMovedDataBlocks.remove(i);
							break;
						}
					}
					for(int i = 0; i < unMovedComplexBlocks.size(); i ++ ) {
						DataBlock dataBlock = unMovedComplexBlocks.get(i);
						if(dataBlock.locationMatches(newX, y, newZ)) {
							dataBlock.x = x;
							dataBlock.z = z;

							craft.complexBlocks.add(dataBlock);
							unMovedComplexBlocks.remove(i);
							break;
						}
					}
				}
			}
		}

		int blockId;
		Block block;

		//remove blocks that need support first
		for(int x=0;x<craft.sizeX;x++){
			for(int z=0;z<craft.sizeZ;z++){
				for(int y=0;y<craft.sizeY;y++){
					if(craft.matrix[x][y][z] != -1){
						blockId = craft.matrix[x][y][z];
						block = craft.world.getBlockAt(craft.minX + x, craft.minY + y, craft.minZ + z);

						if(BlocksInfo.needsSupport(blockId)) {
							if (blockId == 64 || blockId == 71) { // wooden door and steel door
								if (block.getData() >= 8) {
									continue;
								}
							}

							if(blockId == 26 && block.getData() > 4) { //bed
								continue;
							}

							setBlock(0, craft.minX + x, craft.minY + y, craft.minZ + z);
						}
					}
				}
			}
		}

		//remove all the current blocks
		for(int x=0;x<craft.sizeX;x++){
			for(int y=0;y<craft.sizeY;y++){
				for(int z=0;z<craft.sizeZ;z++){

					/**
						Added to attempt to resolve water issues...
					 */

						if (( craft.minY + y >= 63 )
								|| !(craft.type.canNavigate || craft.type.canDive))
							setBlock(0, craft.minX + x, craft.minY + y, craft.minZ + z);
						else
							setBlock(craft.waterType, craft.minX + x, craft.minY + y, craft.minZ + z);
				}
			}
		}
		
		craft.matrix = newMatrix;
		craft.sizeX = newSize.getBlockX();
		craft.sizeZ = newSize.getBlockZ();

		//craft pivot
		int posX = craft.minX + craft.offX;
		int posZ = craft.minZ + craft.offZ;

		NavyCraft.instance.DebugMessage("Min vals start " + craft.minX + ", " + craft.minZ, 2);

		NavyCraft.instance.DebugMessage("Off was " + craft.offX + ", " + craft.offZ, 2);

		//rotate offset
		int newoffsetX = rotateX(craft.offX, craft.offZ, dr);
		int newoffsetZ = rotateZ(craft.offX, craft.offZ, dr);

		NavyCraft.instance.DebugMessage("New off is " + newoffsetX + ", " + newoffsetZ, 2);

		if(newoffsetX < 0)
			newoffsetX = newSize.getBlockX() - 1 - Math.abs(newoffsetX);
		if(newoffsetZ < 0)
			newoffsetZ = newSize.getBlockZ() - 1 - Math.abs(newoffsetZ);

		craft.offX = newoffsetX;
		craft.offZ = newoffsetZ;
		newOffX = newoffsetX;
		newOffZ = newoffsetZ;

		NavyCraft.instance.DebugMessage("Off is " + craft.offX + ", " + craft.offZ, 2);

		//update min/max
		craft.minX = posX - craft.offX;
		craft.minZ = posZ - craft.offZ;
		newMinX = craft.minX;
		newMinZ = craft.minZ;
		craft.maxX = craft.minX + craft.sizeX -1;
		craft.maxZ = craft.minZ + craft.sizeZ -1;

		NavyCraft.instance.DebugMessage("Min vals end " + craft.minX + ", " + craft.minZ, 2);

		rotateCardinals(craft.dataBlocks, dr);
		rotateCardinals(craft.complexBlocks, dr);

		//put craft back
		for(int x = 0; x < getCraftSize().getX(); x++){
			for(int y = 0; y < getCraftSize().getY(); y++){
				for(int z = 0; z < getCraftSize().getZ(); z++){
					blockId = newMatrix[x][y][z];

					if(blockId != -1
							&& !BlocksInfo.needsSupport(blockId) && blockId != 52 && blockId != 34 && blockId != 36 )
						setBlock(blockId, craft.minX + x, craft.minY + y, craft.minZ + z);
					else if(blockId == 34 || blockId == 36)
						setBlock(0, craft.minX + x, craft.minY + y, craft.minZ + z);


				}
			}
		}

		//blocks that need support, but are not data blocks
		for(int x = 0; x < getCraftSize().getX(); x++){
			for(int y = 0; y < getCraftSize().getY(); y++){
				for(int z = 0; z < getCraftSize().getZ(); z++){
					blockId = newMatrix[x][y][z];

					if (BlocksInfo.needsSupport(blockId)
							&& !BlocksInfo.isDataBlock(blockId) && blockId != 63 && blockId != 68 && blockId != 65) {
						setBlock(blockId, craft.minX + x, craft.minY + y, craft.minZ + z);
					}
				}
			}
		}

		restoreDataBlocks(0, 0, 0);
		cm.restoreComplexBlocks(0, 0, 0);
	}

	@SuppressWarnings("deprecation")
	public void rotateCardinals(ArrayList<DataBlock> blocksToRotate, int dr) {
		//http://www.minecraftwiki.net/wiki/Data_values
		//and beds

		byte[] cardinals;
		int blockId;

		for(DataBlock dataBlock: blocksToRotate) {
			//Block theBlock = craft.getWorldBlock(dataBlock.x, dataBlock.y, dataBlock.z);
			blockId = dataBlock.id;

			//logs
			if( blockId == 17 && dataBlock.data > 3 )
			{
				if( dataBlock.data < 8 )
					dataBlock.data += 4;
				else
					dataBlock.data -= 4;
			}
			
			//quartz block
			if( blockId == 155 && dataBlock.data > 2 )
			{
				if( dataBlock.data == 3)
					dataBlock.data = 4;
				else
					dataBlock.data = 3;
			}
			
			//hay bales
			if( blockId == 170 && dataBlock.data > 3 )
			{
				if( dataBlock.data < 8 )
					dataBlock.data += 4;
				else
					dataBlock.data -= 4;
			}
			
			//torches, skip 'em if they're centered on the tile on the ground
			if(blockId == 50 || blockId == 75 || blockId == 76) {
				if(dataBlock.data == 5)
					continue;
			}
			
			if( blockId == 33 || blockId == 29 || blockId == 34 )
			{
				if( dataBlock.data == 0 || dataBlock.data == 1 || dataBlock.data == 8 || dataBlock.data == 9 )
				{
					if( dataBlock.data == 0 )
						dataBlock.data = 1;
					if( dataBlock.data == 8 )
						dataBlock.data = 9;
					continue;
				}
			}

			if(BlocksInfo.getCardinals(blockId) != null)
				cardinals = Arrays.copyOf(BlocksInfo.getCardinals(blockId), 4);
			else
				cardinals = null;

			
			////stairs
			if( blockId == 53 || blockId == 67 || blockId == 108 || blockId == 109 || blockId == 114 || blockId == 128 || blockId == 134 || blockId == 135 || blockId == 136 || blockId == 156 || blockId == 180 ) 
			{	
				if(dataBlock.data > 3) 
					{	//upside down
						for(int c = 0; c < 4; c++) {
							cardinals[c] += 4;
					}
				}
			}
			
			
			if(blockId == 63) {	//sign post
				dataBlock.data = (dataBlock.data + 4) % 16;
				//dataBlock.data = dataBlock.data + 4;
				//if(dataBlock.data > 14) dataBlock.data -= 16;
				continue;
			}
			
			if(blockId == 176) {	//banner post
				dataBlock.data = (dataBlock.data + 4) % 16;
				//dataBlock.data = dataBlock.data + 4;
				//if(dataBlock.data > 14) dataBlock.data -= 16;
				continue;
			}

			if(blockId == 26) {	//bed
				if(dataBlock.data >= 8) {
					for(int c = 0; c < 4; c++)
						cardinals[c] += 8;
				}
			}

			if(blockId == 64 || blockId == 71 || blockId == 193 || blockId == 194 || blockId == 195 ||	blockId == 196 || blockId == 197//wooden or steel door
					|| blockId == 93 || blockId == 94) {	//repeater

				if(dataBlock.data >= 12) {	//if the door is an open top
					for(int c = 0; c < 4; c++)
						cardinals[c] += 12;
				} else if (dataBlock.data >= 8) {		//if the door is a top
					for(int c = 0; c < 4; c++)
						cardinals[c] += 8;
				} else if (dataBlock.data >= 4) {		//not a top, but open
					for(int c = 0; c < 4; c++)
						cardinals[c] += 4;
				}
			}

			if (blockId == 66 ) { // rails
				if(dataBlock.data == 0) {
					dataBlock.data = 1;
					continue;
				}
				if(dataBlock.data == 1) {
					dataBlock.data = 0;
					continue;
				}
			}

			if(blockId == 69) {	//lever

				if(dataBlock.data == 5 || dataBlock.data == 6 ||	//if it's on the floor
						dataBlock.data == 13 || dataBlock.data == 14) {
					cardinals = new byte[]{6, 5, 14, 13};
				}
				else if(dataBlock.data > 4) {	//switched on
					for(int c = 0; c < 4; c++) {
						cardinals[c] += 8;
					}
				}
			}
			
			if(blockId == 77 || blockId == 143) {	//button

				if(dataBlock.data > 4) 
					{	//switched on
						for(int c = 0; c < 4; c++) {
							cardinals[c] += 8;
					}
				}
			}
			
			if(blockId == 96||blockId == 167) {	//hatch

				if(dataBlock.data > 4) 
					{	//switched on
						for(int c = 0; c < 4; c++) {
							cardinals[c] += 4;
					}
				}
			}

			if(blockId == 93 || blockId == 94) {	//repeater
				if(dataBlock.data > 11) {
					for(int c = 0; c < 4; c++)
						cardinals[c] += 12;
				}
				else if(dataBlock.data > 7) {
					for(int c = 0; c < 4; c++)
						cardinals[c] += 8;
				}
				else if(dataBlock.data > 3) {
					for(int c = 0; c < 4; c++)
						cardinals[c] += 4;
				}
			}

			if(cardinals != null) {
				NavyCraft.instance.DebugMessage(Material.getMaterial(blockId) +
						" Cardinals are "
						+ cardinals[0] + ", "
						+ cardinals[1] + ", "
						+ cardinals[2] + ", "
						+ cardinals[3], 2);

				int i = 0;
				for(i = 0; i < 3; i++)
					if(dataBlock.data == cardinals[i])
						break;

				NavyCraft.instance.DebugMessage("i starts as " + i + " which is " + cardinals[i], 2);

				i += (dr / 90);

				if(i > 3)
					i = i - 4;

				NavyCraft.instance.DebugMessage("i ends as " + i + ", which is " + cardinals[i], 2);

				dataBlock.data = cardinals[i];
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void removeSupportBlocks() {
		short blockId;
		Block block;
		
		for (int x = 0; x < craft.sizeX; x++) {
			for (int z = 0; z < craft.sizeZ; z++) {
				for (int y = craft.sizeY - 1; y > -1; y--) {
				//for (int y = 0; y < craft.sizeY; y++) {

					blockId = craft.matrix[x][y][z];

					// craft block, replace by air
					if (BlocksInfo.needsSupport(blockId)) {

						//Block block = world.getBlockAt(posX + x, posY + y, posZ + z);
						block = getWorldBlock(x, y, z);

						// special case for doors
						// we need to remove the lower part of the door only, or the door will pop
						// lower part have data 0 - 7, upper part have data 8 - 15
						if (blockId == 64 || blockId == 71) { // wooden door and steel door
							if (block.getData() >= 8)
								continue;
						}
						
						if(blockId == 26) { //bed
							if(block.getData() >= 4)
								continue;
						}

						setBlock(0, block);
					}
				}
			}
		}
	}
	
	public Block getWorldBlock(int x, int y, int z) {
		//return world.getBlockAt(posX + x, posY + y, posZ + z);
		return craft.world.getBlockAt(craft.minX + x, craft.minY + y, craft.minZ + z);
	}
	
	@SuppressWarnings("deprecation")
	public void setBlock(int id, Block block) {		
		// if(y < 0 || y > 127 || id < 0 || id > 255){
		if (id < 0 || id > 255) {
			// + " x=" + x + " y=" + y + " z=" + z);
			System.out.println("Invalid block type ID. Begin panic.");
			return;
		}
		
		if(block.getTypeId() == id) {
			NavyCraft.instance.DebugMessage("Tried to change a " + id + " to itself.", 5);
			return;
		}
		
		NavyCraft.instance.DebugMessage("Attempting to set block at " + block.getX() + ", "
				 + block.getY() + ", " + block.getZ() + " to " + id, 5);
		
		if (block.setTypeId(id) == false) {
			if(craft.world.getBlockAt(block.getLocation()).setTypeId(id) == false)
				System.out.println("Could not set block of type " + block.getTypeId() + 
						" to type " + id + ". I tried to fix it, but I couldn't.");
			else
				System.out.println("I hope to whatever God you believe in that this fix worked.");
		}
	}
	
	
	@SuppressWarnings("deprecation")
	public void restoreDataBlocks(int dx, int dy, int dz) {
		Block block;
		
		for (DataBlock dataBlock : craft.dataBlocks) {
			
			// this is a pop item, the block needs to be created
			if (BlocksInfo.needsSupport(craft.matrix[dataBlock.x][dataBlock.y][dataBlock.z])) {
				block = getWorldBlock(dx + dataBlock.x, dy + dataBlock.y, dz + dataBlock.z);

				setBlock(craft.matrix[dataBlock.x][dataBlock.y][dataBlock.z], block);
				//block.setcraft.typeId(matrix[dataBlock.x][dataBlock.y][dataBlock.z]);
				block.setData((byte) dataBlock.data);
			} else { //the block is already there, just set the data
				Block theBlock = getWorldBlock(dx + dataBlock.x, dy + dataBlock.y, dz + dataBlock.z);
				if( theBlock.getTypeId() == 23)
				{
					OneCannon oc = new OneCannon(theBlock.getLocation(), NavyCraft.instance);
					if (oc.isValidCannon(theBlock)) 
					{
						for (OneCannon onec : AimCannon.getCannons()) 
						{
							Location testLoc = new Location(craft.world, dataBlock.x,dataBlock.y,dataBlock.z);
							if( cannonLocs.containsKey(testLoc) )
							{
								boolean oldCannonFound = onec.isThisCannon(cannonLocs.get(testLoc), true);
								if(oldCannonFound) 
								{
									Location newLoc = theBlock.getLocation();
									onec.setLocation(newLoc);
									cannonLocs.remove(testLoc);
								}
							}
						}
					}
				}
				theBlock.setData((byte)dataBlock.data);
			}
		}
	}

}
