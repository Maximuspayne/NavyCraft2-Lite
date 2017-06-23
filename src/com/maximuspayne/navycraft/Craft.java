package com.maximuspayne.navycraft;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.entity.Item;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;


/**
 * MoveCraft for Bukkit by Yogoda and SycoPrime
 *
 * You are free to modify it for your own server
 * or use part of the code for your own MoveCraft.
 * You don't need to credit me if you do, but I would appreciate it :)
 *
 * You are not allowed to distribute alternative versions of MoveCraft without my consent.
 * If you do cool modifications, please tell me so I can integrate it :)
 */
@SuppressWarnings("deprecation")
public class Craft {

	// list of craft
	public static Plugin plugin;
	public WorldGuardPlugin wgp;
	public WorldEditPlugin wep;
	public static ArrayList<Craft> craftList = new ArrayList<Craft>();
	public static ArrayList<Craft> addCraftList = new ArrayList<Craft>();
	public static HashMap<Player, Thread> playerAbandonTimers = new HashMap<Player, Thread>();
	public static HashMap<Player, Craft> playerShipList = new HashMap<Player, Craft>();
	public static HashMap<Player, ClipboardHolder> playerClipboards = new HashMap<Player, ClipboardHolder>();
	//public static HashMap<Player, Integer> playerClipboardsCost = new HashMap<Player, Integer>();
	public static HashMap<Player, Integer> playerClipboardsRank = new HashMap<Player, Integer>();
	public static HashMap<Player, String> playerClipboardsType = new HashMap<Player, String>();
	public static HashMap<Player, String> playerClipboardsLot = new HashMap<Player, String>();
	
	public static HashMap<String, CuboidClipboard> playerStoredClipboard = new HashMap<String, CuboidClipboard>();

	
	public static int craftIDTicker=0;
	public int craftID;
	
	public CraftType type;
	public String name; // name, a different name can be set
	
	public boolean doDestroy = false;
	public boolean doRemove = false;

	short matrix[][][];
	ArrayList<DataBlock> dataBlocks;
	//convert to LinkedList for preformance boost?
	ArrayList<DataBlock> complexBlocks = new ArrayList<DataBlock>();
	//ArrayList<ArrayList<String>> signLines = new ArrayList<ArrayList<String>>();
	
	short displacedBlocks[][][];
	
	public ArrayList<Entity> checkEntities;
	
	public int dx,dy,dz = 0 ;

	// size of the craft
	int sizeX, sizeZ, sizeY = 0;

	// position of the craft on the map
	public World world;
	//int posX, posY, posZ;
	int centerX, centerZ = -1;
	
	int blockCount = 0;
	int flyBlockCount, digBlockCount = 0;

	int maxBlocks;

	int waterLevel = -1;
	int newWaterLevel = -1; // new detected waterlevel when moving

	short waterType = 0; // water or lava

	int minX, maxX, minY, maxY, minZ, maxZ = 0;

	public String captainName;
	public String driverName;
	public ArrayList<String> crewNames = new ArrayList<String>();
	public ArrayList<String> crewHistory = new ArrayList<String>();
	public HashMap<String, Boolean> isNameOnBoard = new HashMap<String, Boolean>();

	int speed = 0;

	long lastMove = System.currentTimeMillis(); // record time of the last arm
	public long lastUpdate = System.currentTimeMillis(); // record time of the last arm
	// swing
	boolean haveControl = true; // if the player have the control of the craft

	String customName = null;

	boolean blockPlaced = false;
	
	/* Rotation */
	public int rotation = 0;
	int offX, offZ = 0;
	/* End Rotation */

	public MoveCraft_Timer timer = null;
	boolean isPublic = false;
	public boolean inHyperSpace = false;
	public int HyperSpaceMoves[] = new int[3];
	public ArrayList<Location> WayPoints = new ArrayList<Location>();
	public int currentWayPoint = 0;
	public boolean StopRequested = false;
	public Block railBlock;
	int remainingFuel = 0;
	int asyncTaskId = 0;
	
	boolean autoTurn = true;
	Location collisionLoc;
	boolean possibleCollision = false;
	boolean doCollide = false;
	boolean helmDestroyed = false;
	volatile boolean sinking = false;
	int lastDX = 0;
	int lastDZ = 0;
	
	Location signLoc;
	
	int blockCountStart;
	int lastBlockCount;
	
	public HashMap<Player, Integer> damagers = new HashMap<Player, Integer>(); 
	public int uncreditedDamage = 0;
	
	public ArrayList<Periscope> periscopes = new ArrayList<Periscope>();
	
	public ArrayList<Pump> pumps = new ArrayList<Pump>();
	
	public ArrayList<Chunk> chunkList = new ArrayList<Chunk>();
	
	public int waitTorpLoading = 0;
	
	public Player lastCauser;

	int rudder=0; //-1 left, 1 right
	int turnProgress=0; //0 not turning, countdown to zero
	int setSpeed=0;
	int vertPlanes=0; //0 straight, 1 up, -1 down
	int gear=1;
	//volatile boolean enginesRunning=false;
	boolean enginesOn = false;
	boolean isMoving = false;
	int keelDepth=0;
	boolean onGround=true;
	boolean isRepairing=false;
	boolean doSink= false;
	boolean checkLanding = false;
	boolean submergedMode = false;
	boolean speedReducedCol = false;
	int reductionSpeed = -1;
	int collisionSpeed = -1;
	
	boolean isAutoCraft = false;
	
	long abandonTime=0;
	//long lastUpdate=0;
	boolean recentlyUpdated = false;
	
	boolean radarOn = false;
	long lastRadarPulse=0;
	boolean sonarOn = false;
	long lastSonarPulse=0;
	
	boolean leftSafeDock = false;
	boolean isDestroying = false;
	
	boolean isMerchantCraft = false;
	
	public HashMap<Integer, Integer> tubeFiringMode = new HashMap<Integer, Integer>();  /////tubeNumber, tubeStatus...status = -2 straight, -1 periscope, 0+ for target index
	public HashMap<Integer, Integer> tubeFiringDepth = new HashMap<Integer, Integer>();
	public HashMap<Integer, Integer> tubeFiringHeading = new HashMap<Integer, Integer>();
	public HashMap<Integer, Integer> tubeFiringArm = new HashMap<Integer, Integer>();
	public HashMap<Integer, Boolean> tubeFiringArmed = new HashMap<Integer, Boolean>();
	public HashMap<Integer, Boolean> tubeFiringAuto = new HashMap<Integer, Boolean>();
	public HashMap<Integer, Integer> tubeFiringRudder = new HashMap<Integer, Integer>();
	public HashMap<Integer, Integer> tubeFiringDisplay = new HashMap<Integer, Integer>();
	
	public int tubeMk1FiringMode=-2;//-2 straight, -1 periscope
	public int tubeMk1FiringDepth = 1;
	public int tubeMk1FiringSpread = 0;
	public int tubeMk1FiringDisplay = -1; //0 depth, 1 spread
	
	public boolean hfSonarOn = false;
	public HashMap<Craft, Integer> sonarTargetIDs = new HashMap<Craft, Integer>();
	public HashMap<Integer, Craft> sonarTargetIDs2 = new HashMap<Integer, Craft>();
	public HashMap<Craft, Float> sonarTargetStrength = new HashMap<Craft, Float>();
	public Craft sonarTarget;
	public float sonarTargetRng=-1;
	public int sonarTargetIndex=-1;
	public boolean doPing=false;
	public boolean hfOn=false;
	
	public boolean launcherOn=false;
	
	
	
	public float lastPeriscopeYaw=-9999;
	
	public HashMap<Integer,Integer> engineIDTypes = new HashMap<Integer, Integer>();
	public HashMap<Integer, Location> engineIDLocs = new HashMap<Integer, Location>();
	public HashMap<Integer, Boolean> engineIDSetOn = new HashMap<Integer, Boolean>();
	public HashMap<Integer, Boolean> engineIDIsOn = new HashMap<Integer, Boolean>();
	
	public ArrayList<Chunk> checkedChunks = new ArrayList<Chunk>();
	

	ArrayList<DataBlock> engineBlocks = new ArrayList<DataBlock>();
	
	public boolean doCost = true;
	public int vehicleCost = 0;
	
	public CuboidClipboard repairClipboard;
	
	public Location radioSignLoc=null;
	public int radio1=0;
	public int radio2=0;
	public int radio3=0;
	public int radio4=0;
	public int radioSelector=1;
	public boolean radioSetOn = true;
	public long lastRadioPulse=0;
	
	public boolean abandoned = false;
	public boolean captainAbandoned = false;
	public boolean takingOver = false;
	public boolean cancelTakeoverTimer = false;
	
	public int sinkValue = 0;
	public String routeID = "";
	public int routeStage = 0;
	public boolean freeSpawn = false;
	public boolean discountSpawn = false;
	
	public boolean isMovingPlayers = false;
	
	public int noCaptain=0;
	public int stuckAutoTimer=0;
	
	
	public static HashMap<String, Craft> reboardNames = new HashMap<String, Craft>();
	
	public float weightStart = 0;
	public float weightCurrent = 0;
	public float displacement = 0.0f;
	public float blockDisplacement = 0.0f;
	public float airDisplacement = 0.0f;
	public int currentEngineCount=0;
	public int buoyancy=0;
	public int buoyFloodTicker=0;
	
	public float blockDispValue=1.0f;
	public float airDispValue=15.0f;
	public float weightMult = 1.0f;
	public float minDispValue = 0.33f;
	
	public float ballastDisplacement = 0.0f;
	public int ballastAirPercent=100;
	public int ballastMode=0; //0 closed, 1 flood, 2 blow, 3 auto equalize
	
	
	
	//public int moveTicker = 0;

	Craft(CraftType type, Player player, String customName, float Rotation, Location signBlockLoc, Plugin p) {
		if(Rotation > 45 && Rotation < 135)
			Rotation = 90;
		else if(Rotation > 135 && Rotation < 225)
			Rotation = 180;
		else if (Rotation > 225 && Rotation < 315)
			Rotation = 270;
		else
			Rotation = 0;
		
		this.type = type;
		this.name = type.name;
		this.customName = customName;
		this.captainName = player.getName();
		this.world = player.getWorld();
		this.rotation = (int) Rotation;
		this.signLoc = signBlockLoc;
		plugin = p;
		
		this.craftID = craftIDTicker;
		craftIDTicker++;
	}
	


	public static Craft getPlayerCraft(Player player) {

		if (craftList.isEmpty())
			return null;

		if( player == null )
			return null;
		
		for (Craft craft : craftList) {
			if( craft.captainName != null )
			{
				if( craft.crewNames.isEmpty() )
					continue;
				
				for(String s : craft.crewNames)
				{
					if (s.equalsIgnoreCase(player.getName())) {
						//if( craft.isOnBoard.get(p) )
						return craft;
					}
				}
			}
		}

		return null;
	}

	// return the craft the block is belonging to
	public static Craft getCraft(int x, int y, int z) {

		if (craftList.isEmpty())
			return null;

		for (Craft craft : craftList) {
			if (craft.isIn(x, y, z)) {
				return craft;
			}
		}
		return null;
	}
	
	// return the other craft that does not belong to the player
		public static Craft getOtherCraft(Craft c, Player p, int x, int y, int z) {

			if (craftList.isEmpty())
				return null;

			for (Craft craft : craftList) {
				if ( craft.isIn(x, y, z) && getPlayerCraft(p) != craft ) {
					return craft;
				}
			}
			return null;
		}

	// add a block to the craft, if it is connected to a craft block
	public void addBlock(Block block, boolean overrideAdd) {
		NavyCraft.instance.DebugMessage("Adding a block...", 4);

		// to craft coordinates
		int x = block.getX() - minX;
		int y = block.getY() - minY;
		int z = block.getZ() - minZ;

		// the block can be attached to a bloc of the craft
		if( x < sizeX && x>=0 && y<sizeY && y>=0 && z<sizeZ && z>=0)
		{
			if ( overrideAdd || ( (x < sizeX-1 && !isFree(matrix[x + 1][y][z]) ) || (x > 0 && !isFree(matrix[x - 1][y][z]) ) 
					|| (y < sizeY-1 && !isFree(matrix[x][y + 1][z]) ) || (y > 0 && !isFree(matrix[x][y - 1][z]) )
					|| (z < sizeZ-1 && !isFree(matrix[x][y][z + 1]) ) || (z > 0 &&!isFree(matrix[x][y][z - 1])) ) ) 
			{
			
	
				short blockId = (short) block.getTypeId();
	
				////Check if block is a structureBlock
				boolean found = false;
				for(short checkblockId : type.structureBlocks){
					if(blockId == checkblockId) {
						found = true;
						break;
					}
				}
				if(!found && blockId != 0){
					return;
				}
				
				
				// some items need to be converted into blocks
				if (blockId == 331) // redstone wire
					blockId = 55;
				else if (blockId == 323) // sign
					blockId = 68;
				else if (blockId == 324) { // door
					blockId = 64;
					matrix[x][y + 1][z] = blockId;
					dataBlocks.add(new DataBlock(blockId, x, y + 1, z, block.getData() + 8));
					blockCount++;
				} else if (blockId == 330) { // door
					blockId = 71;
					matrix[x][y + 1][z] = blockId;
					dataBlocks.add(new DataBlock(blockId, x, y + 1, z, block.getData() + 8));
					blockCount++;
				} else if (blockId == 338) { // reed
					blockId = 83;
				} else if (blockId >= 256) {
					return;
				}
	
				matrix[x][y][z] = blockId;
	
				// add block data
				if (BlocksInfo.isDataBlock(blockId)) {
					dataBlocks.add(new DataBlock(blockId, x, y, z, block.getData()));
				}
				if (BlocksInfo.isComplexBlock(blockId)) {
					complexBlocks.add(new DataBlock(blockId, x, y, z, block.getData()));
				}
	
				if( !overrideAdd )
				{
					weightCurrent += Craft.blockWeight(blockId);
					blockCount++;
				}
			}
		}
	}

	// return if the point is in the craft box
	public boolean isIn(int x, int y, int z) {
		return x >= minX && x <= maxX && y >= minY && y <= maxY
		&& z >= minZ && z <= maxZ;
	}

	static void addCraft(Craft craft) {
		craftList.add(craft);
	}

	void releaseHelm() {
		if(this.timer != null)
			this.timer.Destroy();
		
		this.driverName = null;
	}
	
	void remove() {
		isDestroying = true;
		releaseCraft();
		craftList.remove(this);
		matrix = null;
		dataBlocks.clear();
		complexBlocks.clear();
		
		repairClipboard = null;
	}

	// if the craft can go through this block id
	private boolean canGoThrough(int craftBlockId, int blockId, int data) {

		if( this.type.canZamboni )
			return true;
		
		// all craft types can move through air and flowing water/lava
		if ( blockId == 0 ||
				(blockId >= 8 && blockId <= 11) ||
				//blockId == 78 || 
				BlocksInfo.coversGrass(blockId)) //snow cover
			return true;

		// we can't go through adminium
		if(blockId == 7)
			return false;


		
		if (!type.canNavigate && !type.canDive){
			return false;
		}

		if(craftBlockId == 0) {
			if( (blockId >= 8 && blockId <= 11) // air can go through liquid,
					|| blockId == 0) //or other air
				return true;
			else								//but nothing else
				return false;
		}

		// drill can move through all block types...for now.
		if (type.canDig && craftBlockId == type.digBlockId && blockId != 0)
			return true;

		// ship on water
		if (blockId == 8 || blockId == 9)
			if (waterType == 8)
				return true;

		// ship on lava
		if (blockId == 10 || blockId == 11)
			if (waterType == 10)
				return true;

		if(blockId == waterType)
			return true;

		// iceBreaker can go through ice :)
		if (blockId == 79 && (type.canNavigate || type.canDive) )
			if (waterType == 8)
				return true;
		return false;
	}

	private static boolean isFree(int blockId) {
		if (blockId == 0 || blockId == -1)
			return true;
		return false;
	}

	@SuppressWarnings("unused")
	private static boolean isAirOrWater(int blockId) {
		if (blockId == 0 || (blockId >= 8 && blockId <= 11))
			return true;
		return false;
	}

	
	public boolean isOnCraft(String playerName, boolean precise) {
		Player p = plugin.getServer().getPlayer(playerName);
		if( p != null )
			return isOnCraft(p, precise);
		else
			return false;
	}
	
	
	public boolean isOnCraft(Player player, boolean precise) {

		int x = (int) Math.floor(player.getLocation().getX());
		int y = (int) Math.floor(player.getLocation().getY());
		int z = (int) Math.floor(player.getLocation().getZ());

		if (isIn(x, y - 1, z)) {

			if (!precise)
				return true;

			// the block the player is standing on is part of the craft
			if (matrix[x - minX][y - minY - 1][z - minZ] != -1) {
				return true;
			}
		}

		return false;
	}

	public boolean isOnCraft(Entity player, boolean precise) {

		int x = (int) Math.floor(player.getLocation().getX());
		int y = (int) Math.floor(player.getLocation().getY());
		int z = (int) Math.floor(player.getLocation().getZ());

		if (isIn(x, y - 1, z)) {

			if (!precise)
				return true;

			// the block the player is standing on is part of the craft
			if (matrix[x - minZ][y - minY - 1][z - minZ] != -1) {
				return true;
			}
		}

		return false;
	}

	public boolean isCraftBlock(int x, int y, int z) {

		if (x >= 0 && y >= 0 && z >= 0 && x < sizeX && y < sizeY && z < sizeZ) {

			return !(matrix[x][y][z] == -1);
		} else {
			return false;
		}
	}

	// check there is no blocks in the way
	public boolean canMove(int dx, int dy, int dz) {

		/*
		 * MoveCraft.instance.DebugMessage("move dx : " + dx + " dy : " + dy + " dZ : " + dz);
		 * MoveCraft.instance.DebugMessage("move speed : " + speed);
		 * MoveCraft.instance.DebugMessage("move sizeX : " + sizeX + " sizeY : " + sizeY + " sizeZ : " + sizeZ);
		 */
		ArrayList<Chunk> checkChunks = new ArrayList<Chunk>();
		if( dx > 0 && dz > 0 )
		{
			dx = speed/2 * dx;
			dz = speed/2 * dz;
		}else
		{
			dx = speed * dx;
			dz = speed * dz;
		}

		if (Math.abs(speed * dy) > 1) {
			dy = speed * dy / 2;
			if (Math.abs(dy) == 0  || type.canDive || type.isTerrestrial )
				dy = (int) Math.signum(dy);
		}
		
		if( this.type.canFly && this.type.doesCruise )
		{
			if( !this.onGround )
			{
				if( this.speed >= 10 && dy != 0 )
				{
					dy = ((this.speed - 6)/2)*(int)Math.signum(dy);
				}else if( this.speed >= 8 && dy != 0 )
				{
					dy = (int) Math.signum(dy);
				}else if( this.speed >= 8 && dy == 0 )
				{
					dy = 0;
				}else if( this.speed > 5 )
				{
					dy = -1;
				}else if( this.speed > 3 )
				{
					dy = -3;
				}else if( this.speed <= 3 )
				{
					dy = -5;
				}
			}else
			{
				if( this.speed >= 8 && dy > 0 )
				{
					dy=1;
				}else///stay on ground
				{
					dy=0;
				}
			}
		}
		
		if( this.checkLanding )
		{
			dy = 0;
		}
		
		// vertical limit
		if (minY + dy < 0 || maxY + dy > 255) {
			NavyCraft.instance.DebugMessage("At Max Altitude!", 4);
			dy=0;
		}

		// watch out of the head !
		if (driverName != null && isOnCraft(driverName, true)) {
			Player driver = plugin.getServer().getPlayer(driverName);
			int X = (int) Math.floor(driver.getLocation().getX()) + dx;
			int Y = (int) Math.floor(driver.getLocation().getY()) + dy;
			int Z = (int) Math.floor(driver.getLocation().getZ()) + dz;

			Block targetBlock1 = world.getBlockAt(X, Y, Z);
			Block targetBlock2 = world.getBlockAt(X, Y + 1, Z);
			if (!isCraftBlock(X - minX, Y - minY, Z - minZ)
					&& !canGoThrough(0, targetBlock1.getTypeId(), 0)
					|| !isCraftBlock(X - minX, Y + 1 - minY, Z - minZ)
					&& !canGoThrough(0, targetBlock2.getTypeId(), 0)) {
				NavyCraft.instance.DebugMessage("Craft prevented from because...can't go through?", 4);
				return false;
			}
		}

		// check all blocks can move
		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				for (int y = 0; y < sizeY; y++) {

					// all blocks new positions needs to have a free space
					// before
					if (!isFree(matrix[x][y][z]) && // before move : craft block
							!isCraftBlock(x + dx, y + dy, z + dz)) { // after

						Block theBlock = world.getBlockAt(minX + x + dx, minY
								+ y + dy, minZ + z + dz);
						int blockId = theBlock.getTypeId();
						// int blockId = world.getBlockAt(posX + x + dx, posY +
						// y + dy, posZ + z + dz);
						int blockData = theBlock.getData();
						
						if(!checkChunks.contains(theBlock.getChunk()))
							checkChunks.add(theBlock.getChunk());

						// go into water
						//if (dy < 0 && blockId >= 8 && blockId <= 11) {
						if (blockId >= 8 && blockId <= 11) {

							// MoveCraft.instance.DebugMessage("found water at " + y);
							if (y > newWaterLevel)
								newWaterLevel = y;
						} else if (dy > 0 && blockId == 0) { // get out of water, into air

								// MoveCraft.instance.DebugMessage("found air at " + y);
								if (y - 1 < newWaterLevel)
									newWaterLevel = y - 1;
							}
						
						if (!canGoThrough(matrix[x][y][z], blockId, blockData) ) {
							NavyCraft.instance.DebugMessage("Craft prevented from moving because can't go through.", 4);
							collisionLoc = new Location(world, minX+x+dx,minY+y+dy,minZ+z+dz);
							return false;
							
						}
						

					}
				}
			}
		}
		
		for (Chunk checkChunk : checkChunks) {
			if(!world.isChunkLoaded(checkChunk)) {
				try {
					world.loadChunk(checkChunk);
				}
				catch (Exception ex) {
					NavyCraft.instance.DebugMessage("Craft prevented from moving because destination chunk is not loaded.", 3);
					return false;
				}
			}
		}
		checkedChunks = checkChunks;
		return true;
	}
	

	public void buildCrew(Player newCaptain, boolean addOnly)
	{
		//////detect crew
		if( !addOnly )
		{
			crewNames.clear();
			isNameOnBoard.clear();
		}
		ArrayList<Entity> ents = getCraftEntities(false);
		
		for( Entity e : ents )
		{
			if( e instanceof Player )
			{
				Player p = (Player)e;
				
				Craft c = getPlayerCraft(p);
				if( c != null && c.crewNames.contains(p.getName()) && c != this )
				{
					c.leaveCrew(p);
				}
				
				if( !crewNames.contains(p.getName()) )
					crewNames.add(p.getName());
				
				if( !crewHistory.contains(p.getName()) )
					crewHistory.add(p.getName());

				isNameOnBoard.put(p.getName(), true);
				
				if( p != newCaptain || this.type.canFly || this.type.isTerrestrial )
				{
					if( this.type.canFly )
					{
						if( p.getInventory().getHelmet() == null )
						{
							p.getInventory().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET,1,(short)0));
						}else if( !p.getInventory().contains(Material.CHAINMAIL_HELMET) && p.getInventory().getHelmet().getType() != Material.CHAINMAIL_HELMET )
						{
							p.getInventory().addItem(new ItemStack(Material.CHAINMAIL_HELMET,1,(short)0));
						}
						if( p.getInventory().getChestplate() == null )
						{
							p.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE,1,(short)0));
						}else if( !p.getInventory().contains(Material.CHAINMAIL_CHESTPLATE) && p.getInventory().getChestplate().getType() != Material.CHAINMAIL_CHESTPLATE )
						{
							p.getInventory().addItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE,1,(short)0));
						}
						if( p.getInventory().getLeggings() == null )
						{
							p.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS,1,(short)0));
						}else if( !p.getInventory().contains(Material.CHAINMAIL_LEGGINGS) && p.getInventory().getLeggings().getType() != Material.CHAINMAIL_LEGGINGS )
						{
							p.getInventory().addItem(new ItemStack(Material.CHAINMAIL_LEGGINGS,1,(short)0));
						}
						if( p.getInventory().getBoots() == null )
						{
							p.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS,1,(short)0));
						}else if( !p.getInventory().contains(Material.CHAINMAIL_BOOTS) && p.getInventory().getBoots().getType() != Material.CHAINMAIL_BOOTS )
						{
							p.getInventory().addItem(new ItemStack(Material.CHAINMAIL_BOOTS,1,(short)0));
						}
					}else if( this.type.isTerrestrial )
					{
						if( p.getInventory().getHelmet() == null )
						{
							p.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET,1,(short)0));
						}else if( !p.getInventory().contains(Material.IRON_HELMET) && p.getInventory().getHelmet().getType() != Material.IRON_HELMET )
						{
							p.getInventory().addItem(new ItemStack(Material.IRON_HELMET,1,(short)0));
						}
						if( p.getInventory().getChestplate() == null )
						{
							p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE,1,(short)0));
						}else if( !p.getInventory().contains(Material.IRON_CHESTPLATE) && p.getInventory().getChestplate().getType() != Material.IRON_CHESTPLATE )
						{
							p.getInventory().addItem(new ItemStack(Material.IRON_CHESTPLATE,1,(short)0));
						}
						if( p.getInventory().getLeggings() == null )
						{
							p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS,1,(short)0));
						}else if( !p.getInventory().contains(Material.IRON_LEGGINGS) && p.getInventory().getLeggings().getType() != Material.IRON_LEGGINGS )
						{
							p.getInventory().addItem(new ItemStack(Material.IRON_LEGGINGS,1,(short)0));
						}
						if( p.getInventory().getBoots() == null )
						{
							p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS,1,(short)0));
						}else if( !p.getInventory().contains(Material.IRON_BOOTS) && p.getInventory().getBoots().getType() != Material.IRON_BOOTS )
						{
							p.getInventory().addItem(new ItemStack(Material.IRON_BOOTS,1,(short)0));
						}
					}else
					{
						if( p.getInventory().getHelmet() == null )
						{
							p.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET,1,(short)0));
						}else if( !p.getInventory().contains(Material.LEATHER_HELMET) && p.getInventory().getHelmet().getType() != Material.LEATHER_HELMET )
						{
							p.getInventory().addItem(new ItemStack(Material.LEATHER_HELMET,1,(short)0));
						}
						if( p.getInventory().getChestplate() == null )
						{
							p.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE,1,(short)0));
						}else if( !p.getInventory().contains(Material.LEATHER_CHESTPLATE) && p.getInventory().getChestplate().getType() != Material.LEATHER_CHESTPLATE )
						{
							p.getInventory().addItem(new ItemStack(Material.LEATHER_CHESTPLATE,1,(short)0));
						}
						if( p.getInventory().getLeggings() == null )
						{
							p.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS,1,(short)0));
						}else if( !p.getInventory().contains(Material.LEATHER_LEGGINGS) && p.getInventory().getLeggings().getType() != Material.LEATHER_LEGGINGS )
						{
							p.getInventory().addItem(new ItemStack(Material.LEATHER_LEGGINGS,1,(short)0));
						}
						if( p.getInventory().getBoots() == null )
						{
							p.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS,1,(short)0));
						}else if( !p.getInventory().contains(Material.LEATHER_BOOTS) && p.getInventory().getBoots().getType() != Material.LEATHER_BOOTS )
						{
							p.getInventory().addItem(new ItemStack(Material.LEATHER_BOOTS,1,(short)0));
						}
					}
				}
			}
		}
		
		this.captainName = newCaptain.getName();
		if( !crewNames.contains(this.captainName) )
		{
			crewNames.add(this.captainName);
			isNameOnBoard.put(this.captainName, true);
		}
		//captain inventory
		if( !this.type.canFly && !this.type.isTerrestrial )
		{
			Player captain = plugin.getServer().getPlayer(this.captainName);
			if( captain.getInventory().getHelmet() == null )
			{
				captain.getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET,1,(short)0));
			}else if( !captain.getInventory().contains(Material.GOLD_HELMET) && captain.getInventory().getHelmet().getType() != Material.GOLD_HELMET )
			{
				captain.getInventory().addItem(new ItemStack(Material.GOLD_HELMET,1,(short)0));
			}
			if( captain.getInventory().getChestplate() == null )
			{
				captain.getInventory().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE,1,(short)0));
			}else if( !captain.getInventory().contains(Material.GOLD_CHESTPLATE) && captain.getInventory().getChestplate().getType() != Material.GOLD_CHESTPLATE )
			{
				captain.getInventory().addItem(new ItemStack(Material.GOLD_CHESTPLATE,1,(short)0));
			}
			if( captain.getInventory().getLeggings() == null )
			{
				captain.getInventory().setLeggings(new ItemStack(Material.GOLD_LEGGINGS,1,(short)0));
			}else if( !captain.getInventory().contains(Material.GOLD_LEGGINGS) && captain.getInventory().getLeggings().getType() != Material.GOLD_LEGGINGS )
			{
				captain.getInventory().addItem(new ItemStack(Material.GOLD_LEGGINGS,1,(short)0));
			}
			if( captain.getInventory().getBoots() == null )
			{
				captain.getInventory().setBoots(new ItemStack(Material.GOLD_BOOTS,1,(short)0));
			}else if( !captain.getInventory().contains(Material.GOLD_BOOTS) && captain.getInventory().getBoots().getType() != Material.GOLD_BOOTS )
			{
				captain.getInventory().addItem(new ItemStack(Material.GOLD_BOOTS,1,(short)0));
			}
		}
		
		
		driverName = captainName;
		haveControl = true;
		
		if(this.customName == null)
		{
			for( String s : this.crewNames )
			{
				Player p = NavyCraft.instance.getServer().getPlayer(s);
				if( p != null && s != this.captainName )
				{
					p.sendMessage(ChatColor.YELLOW +  "You join the crew of the " + ChatColor.WHITE + this.name.toUpperCase() + ChatColor.YELLOW + " class under the command of " + newCaptain.getDisplayName() + "!");
					newCaptain.sendMessage(p.getDisplayName() + ChatColor.YELLOW + " joins your crew!");
				}
			}
		}else
		{
			for( String s : this.crewNames )
			{
				Player p = NavyCraft.instance.getServer().getPlayer(s);
				if( p != null && s != this.captainName )
				{
					p.sendMessage(ChatColor.YELLOW +  "You join the " + ChatColor.WHITE + this.name.toUpperCase() + ChatColor.YELLOW + " under the command of " + newCaptain.getDisplayName() + "!");
					newCaptain.sendMessage(p.getDisplayName() + ChatColor.YELLOW + " joins your crew!");
				}
			}
		}

	}
	
	public void releaseCraft()
	{
		captainName = null;
		driverName = null;
		crewNames.clear();
		isNameOnBoard.clear();
		reboardNames.clear();
		abandonTime = System.currentTimeMillis();
		abandoned = false;
		captainAbandoned = false;

		if( !isDestroying )
		{
			CraftMover cm = new CraftMover(this, plugin);
			cm.structureUpdate(null,false);
		}
	}
	
	public ArrayList<Entity> getCraftEntities(boolean removeItems) {
		ArrayList<Entity> checkEntities = new ArrayList<Entity>();

		Chunk firstChunk = world.getChunkAt(new Location(world, minX, minY, minZ));
		Chunk lastChunk = world.getChunkAt(new Location(world, minX + sizeX, minY + sizeY, minZ + sizeZ));
		
		int targetX = 0;
		int targetZ = 0;
		Chunk addChunk;
		Entity[] ents;

		for(int x = 0; Math.abs(firstChunk.getX() - lastChunk.getX()) >= x; x++) {
			targetX = 0;
			if(firstChunk.getX() < lastChunk.getX()) {
				targetX = firstChunk.getX() + x;
			} else {
				targetX = firstChunk.getX() - x;
			}
			for(int z = 0; Math.abs(firstChunk.getZ() - lastChunk.getZ()) >= z; z++) {
				targetZ = 0;
				if(firstChunk.getZ() < lastChunk.getZ()) {
					targetZ = firstChunk.getZ() + z;
				} else {
					targetZ = firstChunk.getZ() - z;
				}

				addChunk = world.getChunkAt(targetX, targetZ);

				try {
					ents = addChunk.getEntities();
					for(Entity e : ents) {
						if(!(e instanceof Item) && this.isOnCraft(e, false)) {
							checkEntities.add(e);
						}else if( e instanceof Item && (this.sinking||removeItems) && this.isOnCraft(e, false) )
						{
							e.remove();
						}
					}
				}
				catch (Exception ex) {

				}
			}
		}
		return checkEntities;
	}

	public void setSpeed(int speed) {
		if (speed < 1)
			this.speed = speed;
		else if ( type.doesCruise )
		{
			if( speed > type.maxEngineSpeed )
			{
				this.speed = type.maxEngineSpeed;
			}else
			{
				this.speed = speed;
			}
		}else
		{
			if( speed > type.maxSpeed )
			{
				this.speed = type.maxSpeed;
			}else
			{
				this.speed = speed;
			}
		}
		
	}

	public int getSpeed() {
		return speed;
	}
	
	public void turn(int dr) {
		CraftRotator cr = new CraftRotator(this, plugin);
		cr.turn(dr);
	}

	public void engineTick() {
		//CraftMover cm = new CraftMover(this);
		int dx = 0;
		int dy = 0;
		int dz = 0;
		int[] returnVals = new int[3]; 
		
		if (type.obeysGravity)
			dy -= 1;
		
		//later these will be config options
		//returnVals = enginesByEngineFace(cm);
		if( driverName == null || engineBlocks == null )
			return;
		returnVals = enginesByPlayerFacing(plugin.getServer().getPlayer(driverName), engineBlocks.size());
		dx = returnVals[0];
		dy = returnVals[1];
		dz = returnVals[2];
		
		if(dx != 0 || dy != 0 || dz != 0) {
			//cm.calculatedMove(dx, dy, dz);
		}
	}
	
	public int[] enginesByEngineFace(CraftMover cm) {
		int dx = 0, dy = 0, dz = 0;
		
		for (DataBlock edb : engineBlocks) {
			//Block engineBlock = world.getBlockAt(this.minX + edb.x, this.minY + edb.y, this.minZ + edb.z);
			Block engineBlock = cm.getWorldBlock(edb.x, edb.y, edb.z);			
			//Sign sign = (Sign) engineBlock.getState();
			
			if(engineBlock.getBlockPower() != 0) {
				//System.out.println("Powered engine.");
			} else {
				//System.out.println("Unpowered engine.");
			}
			
			//0,1,2,3
			//north, east, west, south
			
			//north is dx - 1
			//south is dx + 1
			//east is dz - 1
			//west is dz + 1
			
			int engineDirection = BlocksInfo.getCardinalDirectionFromData(engineBlock.getTypeId(), engineBlock.getData());
			switch(engineDirection) {
			case 0:
				dx -= 1;
				break;
			case 1:
				dz -= 1;
				break;
			case 2:
				dz += 1;
				break;
			case 3:
				dx += 1;
				break;
			}
			
		}
		
		return new int[] {dx, dy, dz};
	}
	
	public int[] enginesByPlayerFacing(Player player, int engineCount) {
		float rotation = (float) Math.PI * player.getLocation().getYaw() / 180f;

		float nx = -(float) Math.sin(rotation);
		float nz = (float) Math.cos(rotation);
		
		int[] returnVals = new int[3];
		
		returnVals[0] = engineCount * (Math.abs(nx) >= 0.5 ? 1 : 0) * (int) Math.signum(nx);
		returnVals[1] = engineCount * (Math.abs(nz) > 0.5 ? 1 : 0) * (int) Math.signum(nz);
		returnVals[2] = 0;
		
		return returnVals;
	}
	
	public boolean addWayPoint(Location loc) {

			WayPoints.add(loc);
		return true;
	}
	
	public void removeWayPoint(Location loc) {
		WayPoints.remove(loc);		
	}
	
	public void WayPointTravel(boolean forward) {
		Location nextWaypoint;
		if(forward == true)
			nextWaypoint = WayPoints.get(currentWayPoint + 1);
		else
			nextWaypoint = WayPoints.get(currentWayPoint - 1);
		
		currentWayPoint++;
		if (forward == true && WayPoints.size() >= currentWayPoint)
			forward = false;
		if (forward == false && currentWayPoint == 0)
			forward = true;
		
		Vector deviation = new Vector();
		deviation.add(getLocation().toVector());
		deviation.add(nextWaypoint.toVector());
		
		plugin.getServer().getPlayer(driverName).sendMessage(deviation.toString());
	}
	
	public void WarpToWorld(World targetWorld) {
		World oldWorld = this.world;
		CraftMover cm = new CraftMover(this, plugin);
		
		//assemble the craft in the new world
		this.world = targetWorld;
		
		//cm.move(0, 0, 0, false, false);
		
		this.world = oldWorld;

		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				for (int y = 0; y < sizeY; y++) {
					Block theBlock = cm.getWorldBlock(x, y, z);
					theBlock.setTypeId(0);
				}
			}
		}

		this.world = targetWorld;
	}
	
	public void SelfDestruct(boolean justTheTip) {
		//figure out what part of the craft is touching the world, or its direction...

		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				for (int y = 0; y < sizeY; y++) {
					Block theBlock = world.getBlockAt(minX + x, minY + y, minZ + z);
					theBlock.setType(Material.TNT);
					//TNT tnt = (TNT) theBlock.getState();
				}
			}
		}
	}
	
	public Location getLocation() {
		return new Location(this.world, this.minX+this.sizeX/2, this.minY+this.sizeY/2, this.minZ+this.sizeZ/2);
	}
	
	public Location getMinLocation() {
		return new Location(this.world, this.minX, this.minY, this.minZ);
	}
	
	public Location getMaxLocation() {
		return new Location(this.world, this.maxX, this.maxY, this.maxZ);
	}
	
	public void destroy() {
		isDestroying = true;
		
		
		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				for (int y = 0; y < sizeY; y++) {
					if( isCraftBlock(x,y,z) )
					{
						Block theBlock = world.getBlockAt(minX + x, minY + y, minZ + z);
						if( theBlock.getY() < 63 )
						{
							theBlock.setType(Material.WATER);
						}else{
							theBlock.setType(Material.AIR);
						}
					}
					//TNT tnt = (TNT) theBlock.getState();
				}
			}
		}
		remove();
		this.getCraftEntities(true);
	}
	
	
	//block resistance to breaking/damage....0=default(glass), 1=wood, 2=iron, 3= obsidian bedrock
	public static int blockHardness(int blockID)
	{
		if(blockID == 7 || blockID == 120 || blockID == 130 || blockID == 137)
		{
			return 4;
		}else if(blockID == 49 || blockID == 116 || blockID == 145)
		{
			return 3;
		}else if( blockID == 8 || blockID == 9 || blockID == 10 || blockID == 11 )
		{
			return -3;
		}else if( blockID == 1 || blockID == 4 || blockID == 23 || blockID == 41 || blockID == 42 || blockID == 48 || blockID == 57
				|| blockID == 67 || blockID == 71 || blockID == 86 || blockID == 98 || blockID == 109 || blockID == 112 
				|| blockID == 114 || blockID == 121 || blockID == 122 || blockID == 133 || blockID == 139 || blockID == 155 
				|| blockID == 156 || blockID == 159 || blockID == 172 )
		{
			return 2;
		}else if( blockID == 2 || blockID == 3 || blockID == 5 || blockID == 12 || blockID == 13 || blockID == 14 || blockID == 15 || blockID == 16 
				|| blockID == 17 || blockID == 19  || blockID == 21 || blockID == 22 || blockID == 24 || blockID == 25 || blockID == 29 || blockID == 33 || blockID == 35 
				|| blockID == 43 || blockID == 44 || blockID == 45 || blockID == 47 || blockID == 53 || blockID == 54 || blockID == 56 || blockID == 58 || blockID == 60 || blockID == 61 
				|| blockID == 62 || blockID == 63 || blockID == 64 || blockID == 65 || blockID == 68 || blockID == 69 || blockID == 70 || blockID == 72 || blockID == 73 || blockID == 74 
				|| blockID == 77 || blockID == 78 || blockID == 79 || blockID == 80 || blockID == 82 || blockID == 84 || blockID == 85 || blockID == 91 || blockID == 96 || blockID == 101 || blockID == 107
				|| blockID == 108 || blockID == 110 || blockID == 113 || blockID == 117 || blockID == 118 || blockID == 125 || blockID == 126 
				|| blockID == 128 || blockID == 134 || blockID == 135 || blockID == 136 || blockID == 138 || blockID == 143 || blockID == 146 || blockID == 147 || blockID == 148 || blockID == 154 || blockID == 158)
		{
			return 1;
		}else if( blockID == 46 || blockID == 129 || blockID == 152 )
		{
			return -1;
		}else if( blockID == 153 || blockID == 170 || blockID == 173 )
		{
			return -2;
		}else
		{
			return 0;
		}
	}
	
	public static float blockWeight(int blockID)
	{
		if(blockID == 7 || blockID == 49 || blockID == 130)
		{
			return 20.0f;
		}else if(  blockID == 1 || blockID == 2 || blockID == 3 || blockID == 4 || blockID == 12 || blockID == 13 || blockID == 22 || blockID == 23 
				|| blockID == 24 || blockID == 29 || blockID == 33 || blockID == 41  || blockID == 42 || blockID == 45 || blockID == 46 || blockID == 48 || blockID == 57 || blockID == 60 || blockID == 61 || blockID == 62   
				|| blockID == 67 || blockID == 71 || blockID == 78 || blockID == 79 || blockID == 80 || blockID == 82 || blockID == 86 || blockID == 98
				|| blockID == 108 || blockID == 109 || blockID == 110 || blockID == 112 || blockID == 114 || blockID == 116 || blockID == 120 || blockID == 121 || blockID == 128 || blockID == 129  || blockID == 133
				|| blockID == 138 || blockID == 139 || blockID == 145 || blockID == 152 || blockID == 154 || blockID == 155 || blockID == 156 || blockID == 158 || blockID == 159 || blockID == 172 )
		{
			return 1.0f;
		}else if(  blockID == 19 || blockID == 43 || blockID == 54 || blockID == 58 || blockID == 64 || blockID == 91 || blockID == 96 || blockID == 101 || blockID == 113 
				|| blockID == 117 || blockID == 118 || blockID == 137 || blockID == 153 || blockID == 170 || blockID == 173 )
		{
			return 0.5f;
		}else if( blockID == 5 || blockID == 14 || blockID == 15 || blockID == 16 || blockID == 17 || blockID == 21 || blockID == 25 || blockID == 44 || blockID == 47 
				|| blockID == 53 || blockID == 56 || blockID == 63 || blockID == 65 || blockID == 68 || blockID == 73 
				|| blockID == 74 || blockID == 84 || blockID == 125 || blockID == 126 || blockID == 134 || blockID == 135 || blockID == 136 )
		{
			return 0.25f;
		}else if( blockID == 35 || blockID == 85 || blockID == 107 )
		{
			return 0.17f;
		}else if( blockID != 0 && (blockID < 8 || blockID > 11) ) 
		{
			return 0.1f;
		}else
		{
			return 0.0f;
		}
	}
	
	public static boolean getAttachedBlockExists(Block inBlock, int blockID, int data)
	{
		if( getAttachedBlock(inBlock, blockID, data).getTypeId() == 0 || (getAttachedBlock(inBlock, blockID, data).getTypeId() >= 8 && getAttachedBlock(inBlock, blockID, data).getTypeId() <= 11))
			return false;
		else
			return true;
	}
	
	public static Block getAttachedBlock(Block inBlock, int blockID, int data)
	{
		if( blockID == 75 || blockID == 76 || blockID == 50) //torche and redstone torch
		{
			if( data == 1 )
				return inBlock.getRelative(BlockFace.WEST);
			else if( data == 2 )
				return inBlock.getRelative(BlockFace.EAST);
			else if( data == 3 )
				return inBlock.getRelative(BlockFace.NORTH);
			else if( data == 4 )
				return inBlock.getRelative(BlockFace.SOUTH);
			else if( data == 5 )
				return inBlock.getRelative(BlockFace.DOWN);
		}else if( blockID == 65 || blockID == 68 ) //ladder and wallsign
		{
			if( data == 2 )
				return inBlock.getRelative(BlockFace.SOUTH);
			else if( data == 3 )
				return inBlock.getRelative(BlockFace.NORTH);
			else if( data == 4 )
				return inBlock.getRelative(BlockFace.EAST);
			else if( data == 5 )
				return inBlock.getRelative(BlockFace.WEST);
		}else if( blockID == 63 ) //sign post
		{
				return inBlock.getRelative(BlockFace.DOWN);
		}else if( blockID == 69 || blockID == 77 || blockID == 143 ) //lever and buttons
		{
			if( data%8 == 0 )
				return inBlock.getRelative(BlockFace.UP);
			else if( data%8 == 1 )
				return inBlock.getRelative(BlockFace.WEST);
			else if( data%8 == 2 )
				return inBlock.getRelative(BlockFace.EAST);
			else if( data%8 == 3 )
				return inBlock.getRelative(BlockFace.NORTH);
			else if( data%8 == 4 )
				return inBlock.getRelative(BlockFace.SOUTH);
			else if( data%8 == 5 )
				return inBlock.getRelative(BlockFace.DOWN);
			else if( data%8 == 6 )
				return inBlock.getRelative(BlockFace.DOWN);
			else if( data%8 == 7 )
				return inBlock.getRelative(BlockFace.UP);
		}else if( blockID == 70 || blockID == 72 || blockID == 55 || blockID == 64 || blockID == 71 ) //pressure plates and wire and doors
		{
			return inBlock.getRelative(BlockFace.DOWN);
		}
		return null;
	}
	
	public void speedChange(Player player, boolean increase)
	{
		if( helmDestroyed )
		{
			if( player != null )
				player.sendMessage("Helm Control or Engines Destroyed!");
			return;
		}
		if( increase )
		{
			if( !this.type.canFly )
			{
				this.setSpeed = this.setSpeed + 1;
				if( this.setSpeed > this.type.maxEngineSpeed )
				{
					this.setSpeed = this.type.maxEngineSpeed;
				}
				if( this.setSpeed == 1 )
				{
					if( player != null)
						player.sendMessage("Starting Engines.");
					if( !enginesOn )
					{
						this.speed = 1;
						this.enginesOn=true;

					}
				}
			}else
			{
				this.setSpeed = this.setSpeed + 1;
				if( this.setSpeed > this.type.maxEngineSpeed )
				{
					this.setSpeed = this.type.maxEngineSpeed;
				}
				if( this.setSpeed == 1 )
				{
					if( player != null)
						player.sendMessage("Starting Engines.");
					if( !enginesOn )
					{
						this.speed = 1;
						this.enginesOn=true;

					}
				}
				if( this.setSpeed > 1 && this.gear == 1 )
				{
					this.setSpeed = 1;
				}
			}
		}else
		{
			if( !type.canFly )
			{
				this.setSpeed = this.setSpeed - 1;
				if( this.setSpeed <= 0 )
				{
					if( NavyCraft.checkSpawnRegion(new Location(this.world, this.minX, this.minY, this.minZ)) || NavyCraft.checkSpawnRegion(new Location(this.world, this.maxX, this.maxY, this.maxZ)) )
					{
						if( player != null )
							player.sendMessage("Cannot stop engines until clear of safe dock area.");
						this.setSpeed = this.setSpeed + 1;
						return;
					}
					
					this.setSpeed = 0;
					this.turnProgress = 0;
					this.rudder = 0;
					player.sendMessage("Stopping Engines...");
					this.enginesOn = false;
					return;
				}
			}else
			{
				this.setSpeed = this.setSpeed - 1;
				if( this.setSpeed == 0 && (this.gear > 1 || !this.onGround ) )
				{
					this.setSpeed = this.setSpeed + 1;
					if( player != null )
						player.sendMessage("Can't reduce speed to zero in this gear");
				}
				if( this.setSpeed <= 0 )
				{
					if( NavyCraft.checkSpawnRegion(new Location(this.world, this.minX, this.minY, this.minZ)) || NavyCraft.checkSpawnRegion(new Location(this.world, this.maxX, this.maxY, this.maxZ)) )
					{
						if( player != null )
							player.sendMessage("Cannot stop engines until clear of safe dock area.");
						this.setSpeed = this.setSpeed + 1;
						return;
					}
					this.setSpeed = 0;
					this.turnProgress = 0;
					this.rudder = 0;
					if( player != null )
						player.sendMessage("Stopping Engines...");
					this.enginesOn = false;
					return;
				}
			}
		}
		if( player != null )
		{
			if( type.canFly )
			{
				player.sendMessage("Throttle-" + this.setSpeed*10 + "%");
			}else if( type.isTerrestrial )
			{
				player.sendMessage("Throttle-" + this.setSpeed*25 + "%");
			}else
			{
				if( this.setSpeed == 0 )
				{
					player.sendMessage("All Stop");
				}else if( this.setSpeed == 1 )
				{
					player.sendMessage("Engines Slow");
				}else if( this.setSpeed == 2 )
				{
					player.sendMessage("Engines 1/3");
				}else if( this.setSpeed == 3 )
				{
					player.sendMessage("Engines 2/3");
				}else if( this.setSpeed == 4 )
				{
					player.sendMessage("Engines Standard");
				}else if( this.setSpeed == 5 )
				{
					player.sendMessage("Engines Full");
				}else if( this.setSpeed == 6 )
				{
					player.sendMessage("Engines Flank!");
				}
				telegraphDingThread(player);
			}
		}
	}
	
	public void gearChange(Player player, boolean increase)
	{
		if( helmDestroyed )
		{
			if( player != null )
				player.sendMessage("Helm Control or Engines Destroyed!");
			return;
		}
		if( increase )
		{
			if( !this.type.canFly )
			{
				this.gear = this.gear + 1;
				if( this.gear == 0 )
				{
					this.gear++;
				}
				if( this.gear > this.type.maxForwardGear )
				{
					this.gear = this.type.maxForwardGear;
				}
				if( this.gear == 1 )
				{
	
					if( this.isMoving )
					{
						this.gear = -1;
						if( player != null )
							player.sendMessage("Stop moving before changing to forward gears.");
						return;
					}
				}
				
			}else////airplanes
			{
				this.gear = this.gear + 1;
				if( this.gear == 0 )
				{
					this.gear++;
				}
				if( this.gear > this.type.maxForwardGear )
				{
					this.gear = this.type.maxForwardGear;
				}
				if( this.gear == 1 )
				{

					if( this.isMoving )
					{
						this.gear = -1;
						if( player != null )
							player.sendMessage("Stop moving before changing to forward gears.");
						return;
					}
				}
				if( this.gear == 2 && this.onGround )
				{
					this.turnProgress = 0;
					this.rudder = 0;
					if( player != null )
						player.sendMessage("Ready for takeoff!");
				}
			}
		}else ///decrease
		{
			if( !this.type.canFly )
			{
				this.gear = this.gear - 1;
				if( this.gear == 0 )
				{
					this.gear = this.gear - 1;
				}
				
				if( this.gear == -1 )
				{

					if( this.isMoving )
					{
						this.gear = 1;
						if( player != null )
							player.sendMessage("Stop moving before changing to reverse gears.");
						return;
					}
				}else if( this.gear <= this.type.maxReverseGear )
				{
					this.gear = this.type.maxReverseGear;
				}
			}else
			{
				this.gear = this.gear - 1;
				if( this.gear == 0 )
				{
					this.gear = this.gear - 1;
				}
				if( this.gear == -1 )
				{

					if( this.isMoving )
					{
						this.gear = 1;
						if( player != null )
							player.sendMessage("Stop moving before changing to reverse gears.");
						return;
					}
				}
				if( this.gear == 1 && (!this.onGround || this.setSpeed != 1) )
				{
					if( player != null )
						player.sendMessage("Must be on ground and engine at idle to shift into 1...");
					this.gear = this.gear + 1;
					return;
				}
				
			}
		}
		if( player != null )
			player.sendMessage("Set engines to Gear-(" + this.gear + ")");
	}
	
	public void rudderChange(Player player, int order, boolean turn)
	{
		if( helmDestroyed )
		{
			if( player != null )
				player.sendMessage("Helm Control or Engines Destroyed!");
			return;
		}
		
		if( this.setSpeed == 0 || this.gear <= 0 )
		{
			if( player != null )
				player.sendMessage("You have to be moving forward to turn.");
			return;
		}
		
		if( NavyCraft.checkSpawnRegion(new Location(this.world, this.minX, this.minY, this.minZ)) || NavyCraft.checkSpawnRegion(new Location(this.world, this.maxX, this.maxY, this.maxZ)) )
		{
			if( player != null )
				player.sendMessage("Rudder disabled in safe dock area.");
			return;
		}
		
		if( this.type.canFly && this.gear > 1 && this.onGround )
		{
			if( player != null )
				player.sendMessage("You can't turn while taking off.");
			return;
		}

		if( order == 1 )
		{
			if( this.rudder == 0 || (this.rudder == 1 && turn && this.turnProgress == 0) )
			{
				this.rudder = 1;
				if( turn )
				{
					this.turnProgress = this.type.turnRadius;
					if( player != null )
						player.sendMessage("Rudder Turning Right");
				}else
				{
					if( player != null )
						player.sendMessage("Rudder Right");
				}
				
			}else if( this.rudder == -1 )
			{
				if( this.turnProgress == 0 || this.turnProgress > this.type.turnRadius/2 )
				{
					this.rudder = 0;
					this.turnProgress = 0;
					if( player != null )
						player.sendMessage("Rudder Centered");
				}else
				{
					if( player != null )
						player.sendMessage("Too late to cancel turn, please wait.");
				}
			}else
			{
				if( player != null )
					player.sendMessage("Rudder already set. Look other way to cancel.");
			}
		}else if( order == -1 )
		{
			if( this.rudder == 0 || (this.rudder == -1 && turn && this.turnProgress == 0) )
			{
				this.rudder = -1;
				if( turn )
				{
					this.turnProgress = this.type.turnRadius;
					if( player != null )
						player.sendMessage("Rudder Turning Left");
				}else
				{
					if( player != null )
						player.sendMessage("Rudder Left");
				}
			}else if( this.rudder == 1 )
			{
				if( this.turnProgress == 0 || this.turnProgress > this.type.turnRadius/2 )
				{
					this.rudder = 0;
					this.turnProgress = 0;
					if( player != null )
						player.sendMessage("Rudder Centered");
				}else
				{
					if( player != null )
						player.sendMessage("Too late to cancel turn, please wait.");
				}
			}else
			{
				if( player != null )
					player.sendMessage("Rudder already set. Look other way to cancel.");
			}
		}
	}
	
	public void leaveCrew(Player player)
	{
		isNameOnBoard.remove(player.getName());
		crewNames.remove(player.getName());
		
		for( String s : crewNames )
		{
			Player p = plugin.getServer().getPlayer(s);
			if( p != null )
			{
				p.sendMessage(ChatColor.LIGHT_PURPLE  + "" + ChatColor.ITALIC + player.getName() + " has left your crew.");
			}
		}
		
		if( player.getName() == captainName )
		{
			for( String s : crewNames )
			{
				Player p = plugin.getServer().getPlayer(s);
				if( p != null )
				{
					p.sendMessage(ChatColor.LIGHT_PURPLE  + "" + ChatColor.BOLD + "Your crew has been disbanded.");
				}
			}
			releaseCraft();
		}
	}
	
	public static void takeoverTimerThread(final Player player, final Craft craft){
		craft.takingOver = true;
		playerAbandonTimers.put(player, new Thread(){
			
    	@Override
			public void run() {
    		
    		setPriority(Thread.MIN_PRIORITY);
    		
			try{
				for( int i=0; i<24; i++)
				{
					sleep(5000);
					if( !craft.cancelTakeoverTimer )
						takeoverTimerUpdate(player, craft, i);
					else
						i=24;
				}
				craft.cancelTakeoverTimer = false;
				craft.takingOver = false;
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			}
    	}); //, 20L);
		playerAbandonTimers.get(player).start();
    }
	
   public static void takeoverTimerUpdate(final Player player, final Craft craft, final int i)
    {
    	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
    	//new Thread() {
	  //  @Override
		    public void run()
		    {
	    		if( i < 23 )
				{
	    			if( !craft.abandoned )
	    			{
	    				craft.cancelTakeoverTimer = true;
	    				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Takeover failed!");
	    			}
				}else
				{
					if( craft.abandoned && craft.isOnCraft(player, false) && !craft.crewNames.contains(player.getName()) )
					{
						if( craft.customName != null )
						{
							if( craft.captainName != null )
								plugin.getServer().broadcastMessage(ChatColor.WHITE + craft.captainName + ChatColor.YELLOW + "'s " + ChatColor.WHITE + craft.customName + ChatColor.YELLOW + " was abandoned.");
							else
								plugin.getServer().broadcastMessage(ChatColor.YELLOW + "The " + ChatColor.WHITE + craft.customName + ChatColor.YELLOW + " was abandoned.");
						}else
						{
							if( craft.captainName != null )
								plugin.getServer().broadcastMessage(ChatColor.WHITE + craft.captainName + ChatColor.YELLOW + "'s " + ChatColor.WHITE + craft.name + ChatColor.YELLOW + " was abandoned.");
							else
								plugin.getServer().broadcastMessage(ChatColor.YELLOW + "The " + ChatColor.WHITE + craft.name + ChatColor.YELLOW + " was abandoned.");
						}
						player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Vehicle abandoned! You may now take command.");
						craft.releaseCraft();
					}else
					{
						player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Takeover failed!");
					}
				}

				
			    	
		    }
    	}
    	);
	 }
   
   public boolean isDressed(Player p)
   {
	   if( this.type.canFly )
	   {
		    if( p.getInventory().getHelmet() != null && p.getInventory().getHelmet().getType() == Material.CHAINMAIL_HELMET )
			{
		    	if( p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getType() == Material.CHAINMAIL_CHESTPLATE )
				{
		    		if( p.getInventory().getLeggings() != null && p.getInventory().getLeggings().getType() == Material.CHAINMAIL_LEGGINGS )
					{
		    			if( p.getInventory().getBoots() != null && p.getInventory().getBoots().getType() == Material.CHAINMAIL_BOOTS )
						{
		    				return true;
						}	
					}	
				}	
			}
		    p.sendMessage(ChatColor.RED + "You need to wear pilot (chainmail) uniform to use this.");
		    return false;
	   }else if( this.type.isTerrestrial)
	   {
		   if( p.getInventory().getHelmet() != null && p.getInventory().getHelmet().getType() == Material.IRON_HELMET )
			{
		    	if( p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getType() == Material.IRON_CHESTPLATE )
				{
		    		if( p.getInventory().getLeggings() != null && p.getInventory().getLeggings().getType() == Material.IRON_LEGGINGS )
					{
		    			if( p.getInventory().getBoots() != null && p.getInventory().getBoots().getType() == Material.IRON_BOOTS )
						{
		    				return true;
						}	
					}	
				}	
			}
		    p.sendMessage(ChatColor.RED + "You need to wear soldier (iron) uniform to use this.");
		    return false;  
	   }else
	   {
		   if( p.getInventory().getHelmet() != null && (p.getInventory().getHelmet().getType() == Material.LEATHER_HELMET || p.getInventory().getHelmet().getType() == Material.GOLD_HELMET || p.getInventory().getHelmet().getType() == Material.DIAMOND_HELMET) )
			{
		    	if( p.getInventory().getChestplate() != null && (p.getInventory().getChestplate().getType() == Material.LEATHER_CHESTPLATE || p.getInventory().getChestplate().getType() == Material.GOLD_CHESTPLATE || p.getInventory().getChestplate().getType() == Material.DIAMOND_CHESTPLATE) )
				{
		    		if( p.getInventory().getLeggings() != null && (p.getInventory().getLeggings().getType() == Material.LEATHER_LEGGINGS || p.getInventory().getLeggings().getType() == Material.GOLD_LEGGINGS || p.getInventory().getLeggings().getType() == Material.DIAMOND_LEGGINGS) )
					{
		    			if( p.getInventory().getBoots() != null && (p.getInventory().getBoots().getType() == Material.LEATHER_BOOTS || p.getInventory().getBoots().getType() == Material.GOLD_BOOTS || p.getInventory().getBoots().getType() == Material.DIAMOND_BOOTS) )
						{
		    				return true;
						}	
					}	
				}	
			}
		    p.sendMessage(ChatColor.RED + "You need to wear sailor (leather) or captain (gold) uniform to use this.");
		    return false;  
	   }
   }
	public static void telegraphDingThread(final Player player) {
		new Thread() {

			@Override
			public void run() {

				setPriority(Thread.MIN_PRIORITY);

				// taskNum = -1;
				try {
					World cw = player.getWorld();
					Location loc = player.getLocation();
					for (int i = 0; i < 2; i++) {
						sleep(200);
						cw.playSound(loc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.9f);

					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start(); // , 20L);
	}
}