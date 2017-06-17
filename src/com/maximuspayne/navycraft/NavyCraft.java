package com.maximuspayne.navycraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.*;
import java.io.File;

import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.maximuspayne.navycraft.config.ConfigFile;
import com.maximuspayne.navycraft.plugins.PermissionInterface;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;


/**
 * MoveCraft plugin for Hey0 mod (hMod) by Yogoda
 * Ported to Bukkit by SycoPrime
 *
 * You are free to modify it for your own server
 * or use part of the code for your own plugins.
 * You don't need to credit me if you do, but I would appreciate it :)
 *
 * You are not allowed to distribute alternative versions of MoveCraft without my consent.
 * If you do cool modifications, please tell me so I can integrate it :)
 */

public class NavyCraft extends JavaPlugin {

	static final String pluginName = "NavyCraft";
	static String version;
	public static NavyCraft instance;

	public static Logger logger = Logger.getLogger("Minecraft");
	boolean DebugMode = false;

	public ConfigFile configFile;

	public static ArrayList<Player> aaGunnersList = new ArrayList<Player>();
	public static ArrayList<Skeleton> aaSkelesList = new ArrayList<Skeleton>();
	public static ArrayList<Egg> explosiveEggsList = new ArrayList<Egg>();
	public static HashMap<UUID, Player> shotTNTList = new HashMap<UUID, Player>();
	
	
	public final MoveCraft_PlayerListener playerListener = new MoveCraft_PlayerListener(this);
	public final MoveCraft_BlockListener blockListener = new MoveCraft_BlockListener(this);
	public final MoveCraft_EntityListener entityListener = new MoveCraft_EntityListener(this);
	public final MoveCraft_InventoryListener inventoryListener = new MoveCraft_InventoryListener(this);
	
	public static ArrayList<String> playerKits = new ArrayList<String>();
	

	public static Thread updateThread=null;
	public static Thread npcMerchantThread=null;
	public static boolean shutDown = false;
	
	public static WorldGuardPlugin wgp;
	
	public static ArrayList<Periscope> allPeriscopes = new ArrayList<Periscope>();
	
	public static HashMap<Player, Block> playerLastBoughtSign = new HashMap<Player, Block>();
	public static HashMap<Player, Integer> playerLastBoughtCost = new HashMap<Player, Integer>();
	public static HashMap<Player, String> playerLastBoughtSignString0 = new HashMap<Player, String>();
	public static HashMap<Player, String> playerLastBoughtSignString1 = new HashMap<Player, String>();
	public static HashMap<Player, String> playerLastBoughtSignString2 = new HashMap<Player, String>();
	
	public static int spawnTime=10;
	
	public static HashMap<String, Integer> cleanupPlayerTimes = new HashMap<String, Integer>();
	public static ArrayList<String> cleanupPlayers = new ArrayList<String>();
	
	public static HashMap<String, Long> shipTPCooldowns = new HashMap<String, Long>();
	
	public static int schedulerCounter = 0;

	public void loadProperties() {
		configFile = new ConfigFile();

		File dir = getDataFolder();
		if (!dir.exists())
			dir.mkdir();

		CraftType.loadTypes(dir);
		//This setting was removed as of 0.6.9, craft type file creation has been commented out of the whole thing,
			//craft type files are to be distributed with the plugin 
		CraftType.saveTypes(dir);
		
	}
	
	public void onLoad() {
		
	}

	public void onEnable() {
		instance = this;

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(playerListener, this);
		pm.registerEvents(entityListener, this);
		pm.registerEvents(blockListener, this);
		pm.registerEvents(inventoryListener, this);
		

		
		PluginDescriptionFile pdfFile = this.getDescription();
		version = pdfFile.getVersion();

		BlocksInfo.loadBlocksInfo();
		loadProperties();
		PermissionInterface.setupPermissions(this);
		
		PluginManager manager = getServer().getPluginManager();
		 
        manager.registerEvents(new TeleportFix(this, this.getServer()), this);
		
		structureUpdateScheduler();

		System.out.println(pdfFile.getName() + " " + version + " plugin enabled");
	}

	public void onDisable() {
		shutDown = true;
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " " + version + " plugin disabled");
	}


	public void ToggleDebug() {
		this.DebugMode = !this.DebugMode;
		System.out.println("Debug mode set to " + this.DebugMode);
	}

	public boolean DebugMessage(String message, int messageLevel) {
		/* Message Levels:
		 * 0: Error
		 * 1: Something I'm currently testing
		 * 2: Something I think I just fixed
		 * 3: Something I'm pretty sure is fixed
		 * 4: Supporting information
		 * 5: Nearly frivolous information
		 */
		
		//if(this.DebugMode == true)
		if(Integer.parseInt(this.ConfigSetting("LogLevel")) >= messageLevel)
			System.out.println(message);
		return this.DebugMode;
	}

	public Craft createCraft(Player player, CraftType craftType, int x, int y, int z, String name, float dr, Block signBlock, boolean autoShip) {
		//if( npcMerchantThread == null )
			//npcMerchantThread();
		
		if (DebugMode == true)
			player.sendMessage("Attempting to create " + craftType.name
					+ "at coordinates " + Integer.toString(x) + ", "
					+ Integer.toString(y) + ", " + Integer.toString(z));

	
		Craft craft = new Craft(craftType, player, name, dr, signBlock.getLocation(), this);

		
		// auto-detect and create the craft
		if (!CraftBuilder.detect(craft, x, y, z, autoShip)) {
			return null;
		}
		
		if( autoShip )
			craft.captainName = null;
		
		
		CraftMover cm = new CraftMover(craft, this);
		cm.structureUpdate(null,false);


		Craft.addCraftList.add(craft);
		//craft.cloneCraft();
		
		
		if( craft.type.canFly )
		{
			craft.type.maxEngineSpeed = 10;
		}else if( craft.type.isTerrestrial )
		{
			craft.type.maxEngineSpeed = 4;
		}else
		{
			craft.type.maxEngineSpeed = 6;
		}
		
		
		if( checkSpawnRegion(new Location(craft.world, craft.minX, craft.minY, craft.minZ)) || checkSpawnRegion(new Location(craft.world, craft.maxX, craft.maxY, craft.maxZ)) )
		{
			craft.speedChange(player, true);
		}
		
		if( !autoShip )
		{
			craft.driverName = craft.captainName;
			if(craft.type.listenItem == true)
				player.sendMessage(ChatColor.GRAY + "With a gold sword in your hand, right-click in the direction you want to go.");
			if(craft.type.listenAnimation == true)
				player.sendMessage(ChatColor.GRAY + "Swing your arm in the direction you want to go.");
			if(craft.type.listenMovement == true)
				player.sendMessage(ChatColor.GRAY + "Move in the direction you want to go.");
		}
		return craft;
	}
    
    public static boolean checkStorageRegion(Location loc)
    {
    	
    	wgp = (WorldGuardPlugin) instance.getServer().getPluginManager().getPlugin("WorldGuard");
    	if( wgp != null && loc != null)
    	{
    		if( !PermissionInterface.CheckEnabledWorld(loc) )
    		{
    			return false;
    		}
	    	RegionManager regionManager = wgp.getRegionManager(loc.getWorld());
		
			ApplicableRegionSet set = regionManager.getApplicableRegions(loc);
			
			Iterator<ProtectedRegion> it = set.iterator();
			while( it.hasNext() )
			{
				String id = it.next().getId();
				String[] splits = id.split("_");
				if( splits.length == 2 )
				{
					if( splits[1].equalsIgnoreCase("storage") )
						return true;					
				}
		    }
			return false;
		}
    	return false;
	}
    
    public static boolean checkRepairRegion(Location loc)
    {
    	
    	wgp = (WorldGuardPlugin) instance.getServer().getPluginManager().getPlugin("WorldGuard");
    	if( wgp != null && loc != null)
    	{
    		if( !!PermissionInterface.CheckEnabledWorld(loc) )
    		{
    			return false;
    		}
	    	RegionManager regionManager = wgp.getRegionManager(loc.getWorld());
		
			ApplicableRegionSet set = regionManager.getApplicableRegions(loc);
			
			Iterator<ProtectedRegion> it = set.iterator();
			while( it.hasNext() )
			{
				String id = it.next().getId();
				String[] splits = id.split("_");
				if( splits.length == 2 )
				{
					if( splits[1].equalsIgnoreCase("repair") )
						return true;					
				}
		    }
			return false;
		}
    	return false;
	}
    
    public static boolean checkSafeDockRegion(Location loc)
    {
    	
    	wgp = (WorldGuardPlugin) instance.getServer().getPluginManager().getPlugin("WorldGuard");
    	if( wgp != null && loc != null)
    	{
    		if( !!PermissionInterface.CheckEnabledWorld(loc) )
    		{
    			return false;
    		}
	    	RegionManager regionManager = wgp.getRegionManager(loc.getWorld());
		
			ApplicableRegionSet set = regionManager.getApplicableRegions(loc);
			
			Iterator<ProtectedRegion> it = set.iterator();
			while( it.hasNext() )
			{
				String id = it.next().getId();
				String[] splits = id.split("_");
				if( splits.length == 2 )
				{
					if( splits[1].equalsIgnoreCase("safedock") )
						return true;					
				}
		    }
			return false;
		}
    	return false;
	}
    
    public static boolean checkRecallRegion(Location loc)
    {
    	
    	wgp = (WorldGuardPlugin) instance.getServer().getPluginManager().getPlugin("WorldGuard");
    	if( wgp != null && loc != null)
    	{
    		if( !PermissionInterface.CheckEnabledWorld(loc) )
    		{
    			return false;
    		}
	    	RegionManager regionManager = wgp.getRegionManager(loc.getWorld());
		
			ApplicableRegionSet set = regionManager.getApplicableRegions(loc);
			
			Iterator<ProtectedRegion> it = set.iterator();
			while( it.hasNext() )
			{
				String id = it.next().getId();
				String[] splits = id.split("_");
				if( splits.length == 2 )
				{
					if( splits[1].equalsIgnoreCase("recall") )
						return true;					
				}
		    }
			return false;
		}
    	return false;
	}
    
    public static boolean checkSpawnRegion(Location loc)
    {
    	
    	wgp = (WorldGuardPlugin) instance.getServer().getPluginManager().getPlugin("WorldGuard");
    	if( wgp != null && loc != null)
    	{
    		if( !PermissionInterface.CheckEnabledWorld(loc) )
    		{
    			return false;
    		}
	    	RegionManager regionManager = wgp.getRegionManager(loc.getWorld());
		
			ApplicableRegionSet set = regionManager.getApplicableRegions(loc);
			
			Iterator<ProtectedRegion> it = set.iterator();
			while( it.hasNext() )
			{
				String id = it.next().getId();
				String[] splits = id.split("_");
				if( splits.length == 2 )
				{
					if( splits[1].equalsIgnoreCase("spawn") )
						return true;					
				}
		    }
			return false;
		}
    	return false;
	}
    
    public static boolean checkNoDriveRegion(Location loc)
    {
    	
    	wgp = (WorldGuardPlugin) instance.getServer().getPluginManager().getPlugin("WorldGuard");
    	if( wgp != null && loc != null)
    	{
    		if( !PermissionInterface.CheckEnabledWorld(loc) )
    		{
    			return false;
    		}
	    	RegionManager regionManager = wgp.getRegionManager(loc.getWorld());
		
			ApplicableRegionSet set = regionManager.getApplicableRegions(loc);
			
			Iterator<ProtectedRegion> it = set.iterator();
			while( it.hasNext() )
			{
				String id = it.next().getId();
				String[] splits = id.split("_");
				if( splits.length == 2 )
				{
					if( splits[1].equalsIgnoreCase("nodrive") )
						return true;					
				}
		    }
			return false;
		}
    	return false;
	}
	
	public String ConfigSetting(String setting) {
		if(configFile.ConfigSettings.containsKey(setting))
			return configFile.ConfigSettings.get(setting);
		else {
			System.out.println("Sycoprime needs to be notified that a non-existing config setting '" + setting + 
					"' was attempted to be accessed.");
			return "";
		}
	}

	@SuppressWarnings("deprecation")
	public void dropItem(Block block) {		
		if(NavyCraft.instance.ConfigSetting("HungryHungryDrill").equalsIgnoreCase("true"))
			return;

		int itemToDrop = BlocksInfo.getDropItem(block.getTypeId());
		int quantity = BlocksInfo.getDropQuantity(block.getTypeId());

		if(itemToDrop != -1 && quantity != 0){

			for(int i=0; i<quantity; i++){
				block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(itemToDrop, 1));
			}
		}
	}
	
   
   public void structureUpdateScheduler()
   {
   	this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
   	//new Thread() {
	  //  @Override
		    public void run()
		    {
		    	if( Craft.craftList == null || Craft.craftList.isEmpty() )
		    	{
		    		if( !Craft.addCraftList.isEmpty() )
	    			{
	    				for( Craft c: Craft.addCraftList )
	    				{
	    					Craft.addCraft(c);
	    				}
	    				Craft.addCraftList.clear();
	    			}
		    		return;
		    	}
		    	int vehicleCount = Craft.craftList.size();
	    		int vehicleNum = (schedulerCounter) % vehicleCount;
	    		int updateNum = (schedulerCounter / vehicleCount)%4;
	    
	    		try{
	    		if( vehicleCount < 10 )
	    		{
	    			updateCraft(vehicleNum,updateNum);
	    			schedulerCounter++;
	    		}else if( vehicleCount >= 10 && vehicleCount < 20)
	    		{
	    			//vehicleNum = (vehicleNum + vehicleCount/2)%vehicleCount;
	    			updateCraft(vehicleNum,updateNum);
	    			vehicleNum = (vehicleNum + 1)%vehicleCount;
	    			schedulerCounter = schedulerCounter + 4;
	    			updateCraft(vehicleNum,updateNum);
	    			schedulerCounter = schedulerCounter - 4;
	    			
	    			if( updateNum == 3 )
		    			schedulerCounter+=5;
		    		else
		    			schedulerCounter++;
	    		}else if( vehicleCount >= 20 && vehicleCount < 30 )
	    		{
	    			//vehicleNum = (vehicleNum + vehicleCount/3)%vehicleCount;
	    			vehicleNum = (vehicleNum + 1)%vehicleCount;
	    			schedulerCounter = schedulerCounter + 4;
	    			updateCraft(vehicleNum,updateNum);
	    			vehicleNum = (vehicleNum + 1)%vehicleCount;
	    			schedulerCounter = schedulerCounter + 4;
	    			//vehicleNum = (vehicleNum + vehicleCount/3)%vehicleCount;
	    			updateCraft(vehicleNum,updateNum);
	    			schedulerCounter = schedulerCounter - 4;
	    			schedulerCounter = schedulerCounter - 4;
	    			
	    			if( updateNum == 3 )
		    			schedulerCounter+=9;
		    		else
		    			schedulerCounter++;
	    		}else
	    		{
	    			//vehicleNum = (vehicleNum + vehicleCount/4)%vehicleCount;
	    			vehicleNum = (vehicleNum + 1)%vehicleCount;
	    			schedulerCounter = schedulerCounter + 4;
	    			updateCraft(vehicleNum,updateNum);
	    			//vehicleNum = (vehicleNum + vehicleCount/4)%vehicleCount;
	    			vehicleNum = (vehicleNum + 1)%vehicleCount;
	    			schedulerCounter = schedulerCounter + 4;
	    			updateCraft(vehicleNum,updateNum);
	    			//vehicleNum = (vehicleNum + vehicleCount/4)%vehicleCount;
	    			vehicleNum = (vehicleNum + 1)%vehicleCount;
	    			schedulerCounter = schedulerCounter + 4;
	    			updateCraft(vehicleNum,updateNum);
	    			schedulerCounter = schedulerCounter - 4;
	    			schedulerCounter = schedulerCounter - 4;
	    			schedulerCounter = schedulerCounter - 4;
	    			
	    			if( updateNum == 3 )
		    			schedulerCounter+=13;
		    		else
		    			schedulerCounter++;
	    		}
	    		
	    		
	    		if( updateNum == 3 )
	    		{
	    			if( !Craft.addCraftList.isEmpty() )
	    			{
	    				for( Craft c: Craft.addCraftList )
	    				{
	    					Craft.addCraft(c);
	    				}
	    				Craft.addCraftList.clear();
	    			}
	    		}
	    		}catch(Exception e)
	    		{
	    			schedulerCounter++;
	    		}
		    }
   	}
   	, 4, 1);
	 }
   
   public void updateCraft(int vehicleNum, int updateNum)
   {
	   int vehicleCount = Craft.craftList.size();
	   if( vehicleNum < Craft.craftList.size() && Craft.craftList.get(vehicleNum) != null )
	   {
			Craft checkCraft = Craft.craftList.get(vehicleNum);
			
			if( checkCraft != null && !checkCraft.isDestroying )
			{

				if( checkCraft.isMoving )
				{
					
					if( updateNum == 0 )
					{
						if( checkCraft.doRemove )
						{
							checkCraft.remove();
						}else if( checkCraft.doDestroy )
						{
							checkCraft.destroy();
						}else if( (Math.abs(checkCraft.gear) == 1 && ((schedulerCounter/4) / vehicleCount)%3 == 0)
								|| (Math.abs(checkCraft.gear) == 2 && ((schedulerCounter/4) / vehicleCount)%2 == 0) 
								|| (Math.abs(checkCraft.gear) == 3) )
						{
							if( (Math.abs(checkCraft.gear) == 1 && (System.currentTimeMillis() - checkCraft.lastMove)/1000 > 8)
									|| (Math.abs(checkCraft.gear) == 2 && (System.currentTimeMillis() - checkCraft.lastMove)/1000 >= 5)
									|| (Math.abs(checkCraft.gear) == 3 && (System.currentTimeMillis() - checkCraft.lastMove)/1000.0f >= 2.5) )
							{
								CraftMover cm = new CraftMover(checkCraft, instance);
								cm.moveUpdate();
								//System.out.println("Ship moveupdate="+ checkCraft.craftID);
							}
						}else
						{
							if( !checkCraft.recentlyUpdated )
							{
								if( (System.currentTimeMillis() - checkCraft.lastUpdate)/1000 > 1 )
								{
									CraftMover cm = new CraftMover(checkCraft, instance);
				    				cm.structureUpdate(null,true);
				    				//System.out.println("Ship structureupdate="+ checkCraft.craftID);
								}
							}else
							{
								checkCraft.recentlyUpdated = false;
							}
						}
					}else if( updateNum == 2 )
					{
						if( checkCraft.type.canFly && Math.abs(checkCraft.gear) == 3 )
						{
							if( (System.currentTimeMillis() - checkCraft.lastMove)/1000.0f >= 2.0 )
							{
								CraftMover cm = new CraftMover(checkCraft, instance);
								cm.moveUpdate();
								//System.out.println("Ship moveupdate="+ checkCraft.craftID);
							}
						}
					}
   				}else
   				{
	   				if( checkCraft.enginesOn )
	   				{		
						if( checkCraft.engineIDLocs.isEmpty() )
						{
							if( checkCraft.driverName != null )
							{
								Player p = instance.getServer().getPlayer(checkCraft.driverName);
								if( p != null )
								{
									p.sendMessage("Error: No engines detected! Check engine signs.");
								}
							}
							checkCraft.enginesOn = false;
							checkCraft.speed = 0;
						}else
						{
							for(int id: checkCraft.engineIDLocs.keySet())
							{
								checkCraft.engineIDIsOn.put(id, true);
								checkCraft.engineIDSetOn.put(id, true);
								CraftMover cm = new CraftMover(checkCraft, instance);
								cm.soundThread(checkCraft, id);
							}
						}
   			    
						checkCraft.isMoving = true;
	   				}
   				
	   				if( !checkCraft.recentlyUpdated && updateNum == 0 )
	    			{
	   					if( (System.currentTimeMillis() - checkCraft.lastUpdate)/1000 > 1 )
						{
		    				CraftMover cm = new CraftMover(checkCraft, instance);
		    				cm.structureUpdate(null,true);
		    				checkCraft.lastUpdate = System.currentTimeMillis();
						}
	    				//System.out.println("Ship structureupdate="+ checkCraft.craftID);
	    			}else if( updateNum == 0 )
	    			{
	    				checkCraft.recentlyUpdated = false;
	    			}
	   				
	   				if( checkCraft.doRemove )
					{
						checkCraft.remove();
					}else if( checkCraft.doDestroy )
					{
						checkCraft.destroy();
					}
   				}
				
				
			}
		}
   }
   
	public void npcMerchantThread()
	{
		npcMerchantThread = new Thread(){
			
    	@Override
			public void run() {
    		
    		setPriority(Thread.MIN_PRIORITY);
    		
			try{
				int i=0;
				sleep(30000);
				while(!shutDown)
				{
					npcMerchantUpdate(i);
					i++;
					
					sleep(spawnTime*60000);
				}
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			}
    	}; //, 20L);
    	npcMerchantThread.start();
    }
	
   public void npcMerchantUpdate(final int i)
   {
    	this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
    	//new Thread() {
	  //  @Override
		    public void run()
		    {
		    	if( NavyCraft.shutDown )
					return;
		    	
		    	MoveCraft_BlockListener.autoSpawnSign(null, "");
		    }
    	}
    	);
	}
   
	@SuppressWarnings("deprecation")
	public static void explosion(int explosionRadius, Block warhead)
	{
		short powerMatrix[][][];
		powerMatrix = new short[explosionRadius*2+1][explosionRadius*2+1][explosionRadius*2+1];
		
		powerMatrix[explosionRadius][explosionRadius][explosionRadius] = (short)(explosionRadius*50);
		
		int refI=0;
		int refJ=0;
		int refK=0;
		int curX=0;		
		int curY=0;
		int curZ=0;
		int refX=0;		
		int refY=0;
		int refZ=0;
		boolean doPower=false;
		int fuseDelay = 5;
		
		
		for( int r=1; r<explosionRadius; r++ )
		{
			for( int j=-r; j<=r; j++ )
			{
				for( int i=-r; i<=r; i++ )
				{
					for( int k=-r; k<=r; k++ )
					{
						float refPowerMult = 1.0f;
						if( Math.abs(i) == r )
						{
							refI = (int)((Math.abs(i) - 1)*Math.signum(i));
							if( Math.abs(j) == r )
							{
								refJ = (int)((Math.abs(j) - 1)*Math.signum(j));
								if( Math.abs(k) == r )
								{
									refK = (int)((Math.abs(k) - 1)*Math.signum(k));
									refPowerMult = 0.14f;
								}else if( Math.abs(k) == r - 1 )
								{
									refPowerMult = 0.14f;
								}else	
								{
									refPowerMult = 0.33f;
								}
							}else if( Math.abs(k) == r )
							{
								refK = (int)((Math.abs(k) - 1)*Math.signum(k));
								refPowerMult = 0.33f;
								if( Math.abs(j) == r - 1 )
									refPowerMult = 0.14f;
							}else if( Math.abs(j) == r - 1 )
							{
								refPowerMult = 0.33f;
								if( Math.abs(k) == r - 1 )
									refPowerMult = 0.14f;
							}else if( Math.abs(k) == r - 1 )
							{
								refPowerMult = 0.33f;
								if( Math.abs(j) == r - 1 )
									refPowerMult = 0.14f;
							}
							doPower=true;
						}else if( Math.abs(j) == r )
						{
							refJ = (int)((Math.abs(j) - 1)*Math.signum(j));
							if( Math.abs(k) == r )
							{
								refK = (int)((Math.abs(k) - 1)*Math.signum(k));
								refPowerMult = 0.33f;
								if( Math.abs(i) == r - 1 )
									refPowerMult = 0.14f;
							}else if( Math.abs(i) == r - 1 )
							{
								refPowerMult = 0.33f;
								if( Math.abs(k) == r - 1 )
									refPowerMult = 0.14f;
							}else if( Math.abs(k) == r - 1 )
							{
								refPowerMult = 0.33f;
								if( Math.abs(i) == r - 1 )
									refPowerMult = 0.14f;
							}
							doPower=true;
						}else if( Math.abs(k) == r )
						{
							refK = (int)((Math.abs(k) - 1)*Math.signum(k));
							if( Math.abs(i) == r - 1 )
							{
								refPowerMult = 0.33f;
								if( Math.abs(j) == r - 1 )
									refPowerMult = 0.14f;
							}else if( Math.abs(j) == r - 1 )
							{
								refPowerMult = 0.33f;
								if( Math.abs(i) == r - 1 )
									refPowerMult = 0.14f;
							}
							doPower=true;
						}
						
						if( doPower )
						{
							curX = i + explosionRadius;
							curY = j + explosionRadius;
							curZ = k + explosionRadius;
							refX = refI + explosionRadius;
							refY = refJ + explosionRadius;
							refZ = refK + explosionRadius;
							
							Block theBlock = warhead.getRelative(i,j,k);
					    	int blockType = theBlock.getTypeId();
							
							short refPower = (short)(powerMatrix[refX][refY][refZ] * refPowerMult) ;
							if( refPower > 0 )
							{
								
								short blockResist;
								if( Craft.blockHardness(blockType) == 4 )
								{
									blockResist = -1;
								}else if( Craft.blockHardness(blockType) == 3 )
						    	{
									blockResist = (short)(40 + 40*Math.random());
						    	}else if( Craft.blockHardness(blockType) == 2 )
						    	{
						    		blockResist = (short)(20 + 20*Math.random());
						    	}else if( Craft.blockHardness(blockType) == 1 )
						    	{
						    		blockResist = (short)(10 + 15*Math.random());
						    	}else if( Craft.blockHardness(blockType) == -3 )
						    	{
						    		blockResist=(short)(10+10*Math.random());
						    	}else
						    	{
						    		blockResist = (short)(5+5*Math.random());
						    	}
								
								if( Craft.blockHardness(blockType) == -1 )
								{
									theBlock.setType(Material.AIR);
									TNTPrimed tnt = (TNTPrimed)theBlock.getWorld().spawnEntity(new Location(theBlock.getWorld(), theBlock.getX(), theBlock.getY(), theBlock.getZ()), EntityType.PRIMED_TNT);
						    		tnt.setFuseTicks(fuseDelay);
									fuseDelay = fuseDelay + 2;
								}else if( Craft.blockHardness(blockType) == -2 )
								{
									theBlock.setType(Material.AIR);
									TNTPrimed tnt = (TNTPrimed)theBlock.getWorld().spawnEntity(new Location(theBlock.getWorld(), theBlock.getX(), theBlock.getY(), theBlock.getZ()), EntityType.PRIMED_TNT);
						    		tnt.setFuseTicks(fuseDelay);
						    		tnt.setYield(tnt.getYield()*0.5f);
									fuseDelay = fuseDelay + 2;
								}else
								{
									if( refPower > blockResist && blockResist >= 0 )
									{
										if( theBlock.getY() > 62 )
								    		theBlock.setType(Material.AIR);
								    	else
								    		theBlock.setType(Material.WATER);
									}else
									{
										refPower = 0;
									}
								}
								short newPower = (short)(refPower - blockResist);
								
								if( newPower < 5 )
								{
									powerMatrix[curX][curY][curZ] = 0;
								}else
								{
									powerMatrix[curX][curY][curZ] = newPower;
								}
							}
							
						}
					}
				}
			}
		}
		warhead.getWorld().createExplosion(warhead.getLocation(), explosionRadius);
	}
}