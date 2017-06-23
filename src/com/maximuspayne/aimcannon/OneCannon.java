package com.maximuspayne.aimcannon;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.earth2me.essentials.Essentials;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.maximuspayne.navycraft.Craft;
import com.maximuspayne.navycraft.NavyCraft;
import com.maximuspayne.navycraft.Periscope;
import com.maximuspayne.navycraft.plugins.PermissionInterface;

public class OneCannon{
	public WorldGuardPlugin wgp;
	NavyCraft nc;
    BlockFace direction;
    boolean isCannon;
    double timeout;
    int charged;
    boolean ignite;
    int cannonLength;
    public int cannonType; //0 single, 1 double, 2 fireball (crap), 3 torpedo mk2, 4 depth charge, 5 depth charger II, 6 triple cannon, 7 torp mk3, 8 torp mk1, 9 bombs
    int delay;
    int range;
    int torpedoMode;
    int depth;
    boolean leftLoading, rightLoading;
    public Location loc;
    TNTPrimed tntp;
    TNTPrimed tntp2;
    TNTPrimed tntp3;
    int turnCount = 0;
    int ammunition=-1;
    int initAmmo = 0;
    int cannonTurnCounter=0;
    double tnt1X=0;
    double tnt1Z=0;
    double tnt2X=0;
    double tnt2Z=0;
    double tnt3X=0;
    double tnt3Z=0;
    
    
    volatile boolean stopFall0 = false; //center depth charge
    volatile boolean stopFallM1 = false; // -1 direction dc
    volatile boolean stopFall1 = false;// +1 direction dc
    volatile boolean stopFall2 = false; //single depth charge
    
    
    public OneCannon(Location inloc, NavyCraft inplugin) {
	delay = 1000;
    timeout = 0;
	charged = 0;
	range = 10;
	torpedoMode = 0;
	depth = 0;
	leftLoading = false;
	rightLoading = false;
	loc = inloc;
	nc = inplugin;
	wgp = (WorldGuardPlugin) nc.getServer().getPluginManager().getPlugin("WorldGuard");
    }

    public void setLocation(Location inLoc)
    {
    	loc = inLoc;
    }
    
    public boolean isThisCannon(Location inloc, boolean skipValidation) {
	if (inloc.getBlockX() == loc.getBlockX()
		&& inloc.getBlockY() == loc.getBlockY()
		&& inloc.getBlockZ() == loc.getBlockZ()) {
		if( skipValidation || isValidCannon(inloc.getBlock()) )
			return true;
		else
			return false;
	} else {
	    return false;
	}
    }

    public int getCannonLength() {
	return cannonLength;
    }

    public boolean isCharged() {
	return charged > 0;
    }

    public boolean isTimeout() {
	if (timeout + 4000 < new Date().getTime()) {
	    return true;
	} else {
	    return false;
	}

    }

    public boolean isIgnite() {
	return ignite;
    }

    public void Ignite(final Player p) {

	    Block b;

	    if( cannonType == 6 )
	    	b = loc.getBlock().getRelative(BlockFace.UP).getRelative(direction);
	    else
	    	b = loc.getBlock().getRelative(BlockFace.UP);
	    
	    if( cannonType == 0 )
	    {
		    
		    if (b.getType() == Material.AIR) 
		    {

		    	tntp = (TNTPrimed)b.getWorld().spawnEntity(new Location(b.getWorld(), b.getX()+0.5, b.getY()+0.5, b.getZ()+0.5), EntityType.PRIMED_TNT);
				NavyCraft.shotTNTList.put(tntp.getUniqueId(), p);
				ignite = true;
		    }
	    }else if( cannonType == 1 )
	    {
	    	if (b.getType() == Material.AIR) 
		    {
	    		final Block c;
	    		final Block d;
		    	if( direction == BlockFace.NORTH )
		    	{
				    c = b.getRelative(BlockFace.WEST);
				    d = b.getRelative(BlockFace.EAST);
		    	}else if( direction == BlockFace.SOUTH )
		    	{
				    c = b.getRelative(BlockFace.WEST);
				    d = b.getRelative(BlockFace.EAST);
		    	}else if( direction == BlockFace.WEST )
		    	{
				    c = b.getRelative(BlockFace.SOUTH);
				    d = b.getRelative(BlockFace.NORTH);
		    	}else //east
		    	{
				    c = b.getRelative(BlockFace.NORTH);
				    d = b.getRelative(BlockFace.SOUTH);
		    	}

		    		tntp = (TNTPrimed)c.getWorld().spawnEntity(new Location(c.getWorld(), c.getX()+0.5, c.getY()+0.5, c.getZ()+0.5), EntityType.PRIMED_TNT);

		    		tntp2 = (TNTPrimed)d.getWorld().spawnEntity(new Location(d.getWorld(), d.getX()+0.5, d.getY()+0.5, d.getZ()+0.5), EntityType.PRIMED_TNT);
		    		NavyCraft.shotTNTList.put(tntp.getUniqueId(), p);
					NavyCraft.shotTNTList.put(tntp2.getUniqueId(), p);
					ignite = true;
			   }
	    
	    }else if( cannonType == 6 )
	    {

	    	if (b.getType() == Material.AIR) 
		    {
	    		final Block c;
	    		final Block d;
		    	if( direction == BlockFace.NORTH )
		    	{
				    c = b.getRelative(BlockFace.WEST,2);
				    d = b.getRelative(BlockFace.EAST,2);
		    	}else if( direction == BlockFace.SOUTH )
		    	{
				    c = b.getRelative(BlockFace.WEST,2);
				    d = b.getRelative(BlockFace.EAST,2);
		    	}else if( direction == BlockFace.WEST )
		    	{
				    c = b.getRelative(BlockFace.SOUTH,2);
				    d = b.getRelative(BlockFace.NORTH,2);
		    	}else //east
		    	{
				    c = b.getRelative(BlockFace.NORTH,2);
				    d = b.getRelative(BlockFace.SOUTH,2);
		    	}
			    
		    		tntp = (TNTPrimed)c.getWorld().spawnEntity(new Location(c.getWorld(), c.getX()+0.5, c.getY()+0.5, c.getZ()+0.5), EntityType.PRIMED_TNT);
		    		tntp2 = (TNTPrimed)d.getWorld().spawnEntity(new Location(d.getWorld(), d.getX()+0.5, d.getY()+0.5, d.getZ()+0.5), EntityType.PRIMED_TNT);

		    		tntp3 = (TNTPrimed)b.getWorld().spawnEntity(new Location(b.getWorld(), b.getX()+0.5, b.getY()+0.5, b.getZ()+0.5), EntityType.PRIMED_TNT);
		    		NavyCraft.shotTNTList.put(tntp.getUniqueId(), p);
		    		NavyCraft.shotTNTList.put(tntp2.getUniqueId(), p);
		    		NavyCraft.shotTNTList.put(tntp3.getUniqueId(), p);
					ignite = true;
			   }
	    
	    }
	    if( charged == 1 && delay == 2000 )
	    	delay = 1500;

	    if( ignite )
	    {
	    	fireThreadNew(delay, p);
	    	ignite = false;
	    	p.sendMessage("3 - Ready...");
	    }
    }
    
    public void fireThreadNew(int delay, final Player p)
    {
    	new Thread() {
    		public void run() {
    			try {
    				setPriority(Thread.MIN_PRIORITY);
					sleep(delay);
					fire1(p);
					sleep(1000);
					fire2(p);
					sleep(500);
					fire3(p);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}.start();
    }
    
    public void fire1(final Player p) {
    	nc.getServer().getScheduler().scheduleSyncDelayedTask(nc, new Runnable(){
	  //  @Override
	    public void run()
	    {
	    	p.sendMessage("2 - Aim...");
	    }
	    });
    }
    
    public void fire2(final Player p) {
    	nc.getServer().getScheduler().scheduleSyncDelayedTask(nc, new Runnable(){
	  //  @Override
	    public void run()
	    {
	    	p.sendMessage("1 - Fire!!!");
	    }
	    });
    }
    
    public void fire3(final Player p) {
    	nc.getServer().getScheduler().scheduleSyncDelayedTask(nc, new Runnable(){
	  //  @Override
	    public void run()
	    {
	    	Vector look;
		    look = p.getLocation().getDirection();
		    if (direction == BlockFace.WEST) {
			if (look.getX() > -0.5)
			    look.setX(-0.5);
				look.setY(0.05);
			if (look.getZ() > 0.5)
			    look.setZ(0.5);
			if (look.getZ() < -0.5)
			    look.setZ(-0.5);
		    }
		    if (direction == BlockFace.NORTH) {
			if (look.getZ() > -0.5)
			    look.setZ(-0.5);
				look.setY(0.05);
			if (look.getX() > 0.5)
			    look.setX(0.5);
			if (look.getX() < -0.5)
			    look.setX(-0.5);
		    }
		    if (direction == BlockFace.EAST) {
			if (look.getX() < 0.5)
			    look.setX(0.5);
				look.setY(0.05);
			if (look.getZ() > 0.5)
			    look.setZ(0.5);
			if (look.getZ() < -0.5)
			    look.setZ(-0.5);
		    }
		    if (direction == BlockFace.SOUTH) {
			if (look.getZ() < 0.5)
			    look.setZ(0.5);
				look.setY(0.05);
			if (look.getX() > 0.5)
			    look.setX(0.5);
			if (look.getX() < -0.5)
			    look.setX(-0.5);
		    }
		    fireShell(look.multiply((float)(2*charged)), p);
	    }
	    });
    }
    	
    public void fireShell(Vector look, Player p)
    {
    	tntp.setVelocity(look);
    	if( cannonType == 1 )
	    {
    		
	    	tntp2.setVelocity(look);
	    }
	    if( cannonType == 6 )
	    {
	    	tntp2.setVelocity(look);
	    	tntp3.setVelocity(look);
	    }
	    charged = 0;
	    
	    Block b;
	    b = loc.getBlock().getRelative(BlockFace.UP);
	    b.getWorld().createExplosion(loc.getBlock().getRelative(direction,4).getLocation(), 0);
	    
	    if( cannonType == 6 )
	    {
	    	if( direction == BlockFace.EAST || direction == BlockFace.WEST )
	    	{
	    		b.getWorld().createExplosion(loc.getBlock().getRelative(direction,4).getRelative(BlockFace.NORTH,2).getLocation(), 0);
	    		b.getWorld().createExplosion(loc.getBlock().getRelative(direction,4).getRelative(BlockFace.SOUTH,2).getLocation(), 0);
	    	}else
	    	{
	    		b.getWorld().createExplosion(loc.getBlock().getRelative(direction,4).getRelative(BlockFace.EAST,2).getLocation(), 0);
	    		b.getWorld().createExplosion(loc.getBlock().getRelative(direction,4).getRelative(BlockFace.WEST,2).getLocation(), 0);
	    	}
	    }
    }
    
    public void Fire(final Player p) {
	ignite = false;
	
	setTimeout();
	// Fire the TNT at player View Direction
	new Thread() {
	    @Override
	    public void run() {
		setPriority(Thread.MIN_PRIORITY);
		try {

			p.sendMessage("2 - Aim...");

		    
		    sleep(1000);
		    p.sendMessage("1 - Fire!!!");
		    sleep(500);

		    Vector look;
		    look = p.getLocation().getDirection();

		    if (direction == BlockFace.WEST) {
			if (look.getX() > -0.5)
			    look.setX(-0.5);
			if (look.getY() < 0.05)
				look.setY(0.05);
			if (look.getZ() > 0.5)
			    look.setZ(0.5);
			if (look.getZ() < -0.5)
			    look.setZ(-0.5);
		    }
		    if (direction == BlockFace.NORTH) {
			if (look.getZ() > -0.5)
			    look.setZ(-0.5);
			if (look.getY() < 0.05)
					look.setY(0.05);
			if (look.getX() > 0.5)
			    look.setX(0.5);
			if (look.getX() < -0.5)
			    look.setX(-0.5);
		    }
		    if (direction == BlockFace.EAST) {
			if (look.getX() < 0.5)
			    look.setX(0.5);
			if (look.getY() < 0.05)
				look.setY(0.05);
			if (look.getZ() > 0.5)
			    look.setZ(0.5);
			if (look.getZ() < -0.5)
			    look.setZ(-0.5);
		    }
		    if (direction == BlockFace.SOUTH) {
			if (look.getZ() < 0.5)
			    look.setZ(0.5);
			if (look.getY() < 0.05)
				look.setY(0.05);
			if (look.getX() > 0.5)
			    look.setX(0.5);
			if (look.getX() < -0.5)
			    look.setX(-0.5);
		    }
		    
		    	
		    
		    fireUpdate(look.multiply((float)(2*charged)), p);

		    
		} catch (InterruptedException e) {
		}
	    }
	}.start();
    }
    
    public void fireUpdate(final Vector look, final Player p) {
    	nc.getServer().getScheduler().scheduleSyncDelayedTask(nc, new Runnable(){

	    public void run()
	    {

	    	tntp.setVelocity(look);
	    	if( cannonType == 1 )
		    {
	    		
		    	tntp2.setVelocity(look);
		    }
		    if( cannonType == 6 )
		    {
		    	tntp2.setVelocity(look);
		    	
		    	tntp3.setVelocity(look);
		    }
		    charged = 0;
		    
		    final Block b;
		    b = loc.getBlock().getRelative(BlockFace.UP);
		    b.getWorld().createExplosion(loc.getBlock().getRelative(direction,4).getLocation(), 0);
		    
		    if( cannonType == 6 )
		    {
		    	if( direction == BlockFace.EAST || direction == BlockFace.WEST )
		    	{
		    		b.getWorld().createExplosion(loc.getBlock().getRelative(direction,4).getRelative(BlockFace.NORTH,2).getLocation(), 0);
		    		b.getWorld().createExplosion(loc.getBlock().getRelative(direction,4).getRelative(BlockFace.SOUTH,2).getLocation(), 0);
		    	}else
		    	{
		    		b.getWorld().createExplosion(loc.getBlock().getRelative(direction,4).getRelative(BlockFace.EAST,2).getLocation(), 0);
		    		b.getWorld().createExplosion(loc.getBlock().getRelative(direction,4).getRelative(BlockFace.WEST,2).getLocation(), 0);
		    	}
		    }
	    	
	    }
    	}
    	
	);
    }
    
    public void setDelay(Player p)
    {
    	if( delay >= 2000 )
    	{
    		delay = 0;
    		p.sendMessage("Long Flight Fuse");
    	}else if( delay == 0 )
    	{
    		delay = 1000;
    		p.sendMessage("Medium Flight Fuse");
    	}else if( delay == 1000 )
    	{
    		delay = 2000;
    		p.sendMessage("Short Flight Fuse");
    	}
    }

    public void Action(Player p) {
	if (isCharged()) {
	    if (isTimeout()) {
		if (isIgnite()) {
		    Fire(p);
		} else {
		    Ignite(p);
		}
	    
	    } else {
		p.sendMessage("You have to wait for the Cannon to cool down!!");
	    }
	}

    }
    
    public void fireCannonButton(Player p, boolean leftClick)
    {
    	if( charged > 0 )
    	{
    		fireCannon(p, loc.getBlock());
    		charged = 0;
    		
    	}else
    	{
    		if( leftClick )
    		{
    			range = range - 10;
    			if( range < 10 )
    				range = 10;
    			p.sendMessage("Range set to " + range + "m.");
    		}else
    		{
    			range = range + 10;
    			if( range > 200 )
    				range = 200;
    			p.sendMessage("Range set to " + range + "m.");
    		}
    	}
    }
    
    public void fireCannon(final Player p, final Block b)
    {
		new Thread(){
			
    	@Override
			public void run() {
    		
    		setPriority(Thread.MIN_PRIORITY);
				//taskNum = -1;
    			try{
    				sleep(500);
    				p.sendMessage("3 - Ready...");
    			    sleep(500);
    			    p.sendMessage("2 - Aim...  ");
    			    sleep(500);
    			    p.sendMessage("1 - Fire!!!");
    			    sleep(500);
    			    Vector look;
    			    look = p.getLocation().getDirection();
    			    look.setY(0);
    			    look.normalize();
    			    if (direction == BlockFace.NORTH) {
    				if (look.getX() > -0.5)
    				    look.setX(-0.5);

    				if (look.getZ() > 0.5)
    				    look.setZ(0.5);
    				if (look.getZ() < -0.5)
    				    look.setZ(-0.5);
    			    }
    			    if (direction == BlockFace.EAST) {
    				if (look.getZ() > -0.5)
    				    look.setZ(-0.5);

    				if (look.getX() > 0.5)
    				    look.setX(0.5);
    				if (look.getX() < -0.5)
    				    look.setX(-0.5);
    			    }
    			    if (direction == BlockFace.SOUTH) {
    				if (look.getX() < 0.5)
    				    look.setX(0.5);

    				if (look.getZ() > 0.5)
    				    look.setZ(0.5);
    				if (look.getZ() < -0.5)
    				    look.setZ(-0.5);
    			    }
    			    if (direction == BlockFace.WEST) {
    				if (look.getZ() < 0.5)
    				    look.setZ(0.5);

    				if (look.getX() > 0.5)
    				    look.setX(0.5);
    				if (look.getX() < -0.5)
    				    look.setX(-0.5);
    			    }
    			    
    			    

    			    
    			    double x2,y2,z2,x1,y1,z1;
    			    double arcHeight;
    			    int gunHeight = b.getY() - 64;
    			    arcHeight = gunHeight+2;
    			    
    			    double termA, termB, termC;
    			    termA = (-4*arcHeight + 2*gunHeight/(range*range));
    			    termB = (-gunHeight/range + 4*arcHeight*range - 2*gunHeight/range);
    			    termC = gunHeight;
    			    //p.sendMessage("termA=")
    			    
    			    for(int i=0; i<=range; i++)
    			    {
    			    	x2 = look.getX() * i;
    			    	z2 = look.getZ() * i;
    			    	y2 = (termA*i*i + termB*i + termC) - gunHeight;
    			    	
        			    if( i > 0 )
        			    {
        			    	x1 = look.getX() * (i-1);
        			    	z1 = look.getZ() * (i-1);
        			    	y1 = (termA*(i-1)*(i-1) + termB*(i-1) + termC) - gunHeight;

        			    }else
        			    {
        			    	x1=0;
        			    	z1=0;
        			    	y1=0;
        			    }
    			    	
        			   
        			    fireCannonUpdate(b.getRelative(direction,cannonLength+1), x2, y2, z2, x1, y1, z1, i);
    			    	sleep(100);
    			    }
    			    
				}catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
    	}.start(); 
    }
    
    public void fireCannonUpdate(final Block b, final double x2, final double y2, final double z2, final double x1, final double y1, final double z1, final int i)
    {
    	nc.getServer().getScheduler().scheduleSyncDelayedTask(nc, new Runnable(){
 
	    @SuppressWarnings("deprecation")
		public void run()
	    {
	    	
	    	Block c = b.getRelative((int)x1, (int)y1, (int)z1);
	    	if( c.getTypeId() == 42 || i == 0 )
	    	{
	    		if( i > 0 )
	    		{
		    		if( c.getY() >= 64 )
			    		c.setType(Material.AIR);
			    	else	
			    		c.setType(Material.WATER);
	    		}
	    		//projectile.
	    		
		    	Block d = b.getRelative((int)x2, (int)y2, (int)z2);
		    	if( d.getY() > 60 )
		    		d.setTypeId(42);
	    	}
	    	
	    }
    	
    	});
    }

    
    @SuppressWarnings("deprecation")
	public void colorTorpedoes()
    {
    	//color wool
		Block a,b,c,d;
		if( direction == BlockFace.NORTH )
		{
    		a = loc.getBlock().getRelative(BlockFace.WEST,2);
    		b = loc.getBlock().getRelative(BlockFace.EAST,2);
    		c = loc.getBlock().getRelative(BlockFace.WEST,1);
    		d = loc.getBlock().getRelative(BlockFace.EAST,1);
		}
    	else if( direction == BlockFace.SOUTH )
    	{
    		a = loc.getBlock().getRelative(BlockFace.EAST,2);
    		b = loc.getBlock().getRelative(BlockFace.WEST,2);
    		c = loc.getBlock().getRelative(BlockFace.EAST,1);
    		d = loc.getBlock().getRelative(BlockFace.WEST,1);
    	}
    	else if( direction == BlockFace.EAST )
    	{
    		a = loc.getBlock().getRelative(BlockFace.NORTH,2);
    		b = loc.getBlock().getRelative(BlockFace.SOUTH,2);
    		c = loc.getBlock().getRelative(BlockFace.NORTH,1);
    		d = loc.getBlock().getRelative(BlockFace.SOUTH,1);
    	}
    	else //if( direction == BlockFace.WEST )
    	{
    		a = loc.getBlock().getRelative(BlockFace.SOUTH,2);
    		b = loc.getBlock().getRelative(BlockFace.NORTH,2);
    		c = loc.getBlock().getRelative(BlockFace.SOUTH,1);
    		d = loc.getBlock().getRelative(BlockFace.NORTH,1);
    	}
    	a = a.getRelative(direction,-5);
    	b = b.getRelative(direction,-5);
    	c = c.getRelative(direction,4);
    	d = d.getRelative(direction,4);
    	
    	byte wool1;
		byte wool2;
		byte wool3;
		byte wool4;
		wool1 = 0xE;
		wool2 = 0x8;
		wool3 = 0x8;
		wool4 = 0x7;
		if( cannonType == 7 )
		{
    		wool1 = 0x3;
			wool2 = 0xD;
			wool3 = 0xD;
			wool4 = 0x7;
		}else if( cannonType == 8 )
		{
    		wool1 = 0x8;
			wool2 = 0x0;
			wool3 = 0x0;
			wool4 = 0x7;
		}
    	
		if( c.getTypeId() == 35 )
    		c.setTypeIdAndData(35, wool1, false);
    	if( c.getRelative(direction,-1).getTypeId() == 35 )
    		c.getRelative(direction,-1).setTypeIdAndData(35, wool2, false);
    	if( c.getRelative(direction,-2).getTypeId() == 35 )
    		c.getRelative(direction,-2).setTypeIdAndData(35, wool3, false);
    	if( c.getRelative(direction,-3).getTypeId() == 35 )
    		c.getRelative(direction,-3).setTypeIdAndData(35, wool4, false);
    	
    	if( d.getTypeId() == 35 )
    		d.setTypeIdAndData(35, wool1, false);
    	if( d.getRelative(direction,-1).getTypeId() == 35 )
    		d.getRelative(direction,-1).setTypeIdAndData(35, wool2, false);
    	if( d.getRelative(direction,-2).getTypeId() == 35 )
    		d.getRelative(direction,-2).setTypeIdAndData(35, wool3, false);
    	if( d.getRelative(direction,-3).getTypeId() == 35 )
    		d.getRelative(direction,-3).setTypeIdAndData(35, wool4, false);
		
    	for( int i=0; i<4; i++ )
    	{
    		
    		
        	if( a.getTypeId() == 35 )
        		a.setTypeIdAndData(35, wool1, false);
        	if( a.getRelative(direction,-1).getTypeId() == 35 )
        		a.getRelative(direction,-1).setTypeIdAndData(35, wool2, false);
        	if( a.getRelative(direction,-2).getTypeId() == 35 )
        		a.getRelative(direction,-2).setTypeIdAndData(35, wool3, false);
        	if( a.getRelative(direction,-3).getTypeId() == 35 )
        		a.getRelative(direction,-3).setTypeIdAndData(35, wool4, false);
        	
        	if( b.getTypeId() == 35 )
        		b.setTypeIdAndData(35, wool1, false);
        	if( b.getRelative(direction,-1).getTypeId() == 35 )
        		b.getRelative(direction,-1).setTypeIdAndData(35, wool2, false);
        	if( b.getRelative(direction,-2).getTypeId() == 35 )
        		b.getRelative(direction,-2).setTypeIdAndData(35, wool3, false);
        	if( b.getRelative(direction,-3).getTypeId() == 35 )
        		b.getRelative(direction,-3).setTypeIdAndData(35, wool4, false);
        	
        	if( i == 0 || i == 2 )
        	{
        		a = a.getRelative(direction,-4);
        		b = b.getRelative(direction,-4);
        	}else if( i == 1 )
        	{
        		a = a.getRelative(direction,4).getRelative(BlockFace.DOWN);
        		b = b.getRelative(direction,4).getRelative(BlockFace.DOWN);
        	}	
    	}
    }

    @SuppressWarnings("deprecation")
	public boolean Charge(Player p, boolean leftClick) 
    {
    	Dispenser dispenser = (Dispenser) loc.getBlock().getState();
    	Inventory inventory = dispenser.getInventory();
    	
    	if( inventory.getItem(4) == null || inventory.getItem(4).getTypeId() != 388 )
    	{
    		Essentials ess;
			ess = (Essentials) nc.getServer().getPluginManager().getPlugin("Essentials");
			if( ess == null )
			{
				p.sendMessage("Essentials Economy error");
				return false;
			}
			
			int cost=0;
		    if( cannonType == 0 ) //single barrel
		    	cost=100;
		    else if( cannonType == 1 )//double barrel
		    	cost=250;
		    else if( cannonType == 3 )//torpedo mk 2
		    	cost=600;
		    else if( cannonType == 4 )//depth charge
		    	cost=250;
		    else if( cannonType == 5 )//depth charge mk2
		    	cost=1250;
		    else if( cannonType == 6 )//triple barrel
		    	cost=2000;
		    else if( cannonType == 7 )//torpedo mk 3
		    	cost=2000;
		    else if( cannonType == 8 )//torpedo mk 1
		    	cost=250;
		    else if( cannonType == 9 )//bombs
		    	cost=100;
		    
		    //p.sendMessage("test1.");
			if( PermissionInterface.CheckEnabledWorld(p.getLocation()) )
			{
				//p.sendMessage("test2.");
				if( ess.getUser(p).canAfford(new BigDecimal(cost)) )
				{
					p.sendMessage("Weapon purchased.");
					
					inventory.setItem(4, new ItemStack( 388, 1));
					ess.getUser(p).takeMoney(new BigDecimal(cost));
				}else
				{
					p.sendMessage("You cannot afford this weapon.");
					AimCannon.cannons.remove(this);
					return false;
				}
			}else
			{
				//p.sendMessage("test2.");
				inventory.setItem(4, new ItemStack( 388, 1));
			}
    	}
    	
    	
    	//color wool for torpedoes
		if( !leftClick && (cannonType == 3 || cannonType == 7 || cannonType == 8) )
		{
    		colorTorpedoes();
    		return false;
		}else if( cannonType == 2 )
		{
			if( leftClick )
			{
				if ((p.getInventory().contains(46) || charged > 0) && p.getInventory().contains(289)) 
			    {
			    	if( charged == 0 )
			    		p.getInventory().removeItem(new ItemStack(46, 1));
			    	p.getInventory().removeItem(new ItemStack(289, 1));
			    	charged = 1;
			    	p.sendMessage("Cannon Loaded!");
			    	return true;
			   
			    } else
			    {
			    	p.sendMessage("You need 1xGunpowder and 1xTNT to Load the cannon!");
			    	return false;
			    }
			}else
			{
				range = 10;
				p.sendMessage("Range Reset to 10m.");
				return false;
			}
		}else if( charged < 4 )
    	{
	    	if( cannonType == 0 )
	    	{
	    		
			    if( charged == 0 )
			    {
			    	if( ammunition > 0 )
			    	{
			    		ammunition = ammunition - 1;
			    	}else
			    	{
			    		p.sendMessage( ChatColor.RED + "Cannon out of ammo!");
			    		return false;
			    	}
			    	charged=1;
			    	p.sendMessage("Cannon Loaded! " + ammunition + " shots remaining. Cannon Power X" + charged);
			    }else{
			    	charged++;
			    	p.sendMessage("Cannon Power X" + charged);
			    }
		    	
			    return true;
			    
	    	} else if( cannonType == 1 )
	    	{
	    		if( charged == 0 )
			    {
			    	if( ammunition > 0 )
			    	{
			    		ammunition = ammunition - 1;
			    	}else
			    	{
			    		p.sendMessage( ChatColor.RED + "Cannon out of ammo!");
			    		return false;
			    	}
			    	charged=1;
			    	p.sendMessage("Cannon Loaded! " + ammunition + " shots remaining. Cannon Power X" + charged);
			    }else{
			    	charged++;
			    	p.sendMessage("Cannon Power X" + charged);
			    }
		    	
			    return true;
	    	}else if( cannonType == 6 )
	    	{
	    		if( charged == 0 )
			    {
			    	if( ammunition > 0 )
			    	{
			    		ammunition = ammunition - 1;
			    	}else
			    	{
			    		p.sendMessage( ChatColor.RED + "Cannon out of ammo!");
			    		return false;
			    	}
			    	charged=1;
			    	p.sendMessage("Cannon Loaded! " + ammunition + " shots remaining. Cannon Power X" + charged);
			    }else{
			    	charged++;
			    	p.sendMessage("Cannon Power X" + charged);
			    }
		    	
			    return true;
			}else if( cannonType == 3 || cannonType == 7 || cannonType == 8 )
	    	{
	    		charged=1;
	    		if( depth < 5 )
	    			depth++;
	    		else if( depth == 5 )
	    			depth = 10;
	    		else if( depth >= 10 && depth < 50 )
	    			depth = depth + 5;
	    		else
	    			depth = 0;
	    		p.sendMessage("Torpedo System Active: Depth set to " + depth + " meters.");
	   		
	    		return true;
	    	}
	    	else if( cannonType == 4 )
	    	{
	    		if( charged == 0 )
			    {
			    	if( ammunition > 0 )
			    	{
			    		ammunition = ammunition - 1;
			    	}else
			    	{
			    		p.sendMessage( ChatColor.RED + "Out of depth charges!");
			    		return false;
			    	}
			    	charged=1;
			    	p.sendMessage("Depth charge dropper loaded! " + ammunition + " depth charges left.");
			    }else{
			    	p.sendMessage("Depth charge dropper already loaded! " + ammunition + " depth charges left.");
			    }
		    	
			    return true;
	    		
	    	}else if( cannonType == 5 )
	    	{
	    		if( charged == 0 )
			    {
			    	if( ammunition > 0 )
			    	{
			    		ammunition = ammunition - 1;
			    	}else
			    	{
			    		p.sendMessage( ChatColor.RED + "Out of depth charges!");
			    		return false;
			    	}
			    	charged=1;
			    	p.sendMessage("Depth charge launcher loaded! " + ammunition + " depth charge launches left.");
			    }else{
			    	p.sendMessage("Depth charge launcher already loaded! " + ammunition + " depth charge launches left.");
			    }
		    	
			    return true;
	    		
	    	}else if( cannonType == 9 )
	    	{
	    		if( charged == 0 )
			    {
			    	if( ammunition > 0 )
			    	{
			    		ammunition = ammunition - 1;
			    	}else
			    	{
			    		p.sendMessage( ChatColor.RED + "Out of bombs!");
			    		return false;
			    	}
			    	charged=1;
			    	p.sendMessage("Bomb dropper loaded! " + ammunition + " bombs left.");
			    }else{
			    	p.sendMessage("Bomb dropper already loaded! " + ammunition + " bombs left.");
			    }
		    	
			    return true;
	    		
	    	}else
	    	{
	    		charged = 1;
	    		return true;
	    	}
    	}else
    	{
    		charged=1;
    		p.sendMessage("Cannon Power X" + charged);
    		return false;
    	}
	
    }

    private void setTimeout() {
	timeout = new Date().getTime();
    }

    
    public void loadTorpedoLever(boolean left, Player p)
    {
    	if( !checkTubeLoaded(left) )
    	{
    		if( checkOuterDoorClosed() )
    		{
    			if( !checkInnerDoorClosed(left) )
    			{
    				if( loadTorpedo(left) )
    				{
    					p.sendMessage("Tube Loading!");
    				}else
    				{
    					p.sendMessage("No torpedoes remaining for this tube!");
    				}
    			}else
    			{
    				p.sendMessage("Open inner door before loading.");
    			}
    		}else
    		{
    			p.sendMessage("Close outer doors first.");	
    		}
    	}else
    	{
    		p.sendMessage("Tube already loaded.");
    	}
    }
    
    @SuppressWarnings("deprecation")
	public boolean loadTorpedo(boolean left)
    {
    	Block b;
    	b = getDirectionFromRelative(loc.getBlock(), direction, left).getRelative(direction, -5);
    	
    	if( direction == BlockFace.NORTH && left )
    		b = b.getRelative(BlockFace.WEST);
    	else if( direction == BlockFace.NORTH && !left )
    		b = b.getRelative(BlockFace.EAST);
    	else if( direction == BlockFace.SOUTH && left )
    		b = b.getRelative(BlockFace.EAST);
    	else if( direction == BlockFace.SOUTH && !left )
    		b = b.getRelative(BlockFace.WEST);
    	else if( direction == BlockFace.EAST && left )
    		b = b.getRelative(BlockFace.NORTH);
    	else if( direction == BlockFace.EAST && !left )
    		b = b.getRelative(BlockFace.SOUTH);
    	else if( direction == BlockFace.WEST && left )
    		b = b.getRelative(BlockFace.SOUTH);
    	else //if( direction == BlockFace.WEST && !left )
    		b = b.getRelative(BlockFace.NORTH);
    	
    	for( int i=0; i<4; i++)
    	{
    		if( b.getTypeId() == 35 )
    			if( b.getRelative(direction,-1).getTypeId() == 35 )
        			if( b.getRelative(direction,-2).getTypeId() == 35 )
        				if( b.getRelative(direction,-3).getTypeId() == 35 )
        				{
        					if( i > 1 )
        						loadingTorp(left, b, direction, true);
        					else
        						loadingTorp(left, b, direction, false);
        					
        					if( left )
        						leftLoading = true;
        					else
        						rightLoading = true;
        					
        					Craft testCraft = Craft.getCraft(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        					if( testCraft != null )
        					{
        						testCraft.waitTorpLoading++;
        					}
        					return true;
        				}
    		//move back four spaces unless going from 2 to 3, then carriage return it
    		if( i == 1 )
    			b = b.getRelative(direction,4).getRelative(BlockFace.DOWN);
    		else
    			b = b.getRelative(direction,-4);
    	}
    	return false;
    	
    }
    
    public void loadingTorp(final boolean left, final Block b, final BlockFace torpHeading, final boolean lift) {
    	new Thread() {
    	    @Override
    	    public void run()
    	    {
    	    	
    		setPriority(Thread.MIN_PRIORITY);
    			try
    			{ 
    				
					sleep(1000);
					for( int i=0; i<16; i++ )
					{
						loadingTorpUpdate(left, b, torpHeading, lift, i);
						sleep(1000);
					}
					
    			} catch (InterruptedException e) 
    			{
    			}
    	    }
    	}.start();
        }
    
    
    public void loadingTorpUpdate(final boolean left, final Block b, final BlockFace torpHeading, final boolean lift, final int i)
    {
    	nc.getServer().getScheduler().scheduleSyncDelayedTask(nc, new Runnable(){
 
	    @SuppressWarnings("deprecation")
		public void run()
	    {
	    	Block warhead = b;
	    	Craft testCraft = Craft.getCraft(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
			
	    	if( (left && !leftLoading) || (!left && !rightLoading) )
	    		return;
	    	
	    	
	    	if( i == 0 && lift )
	    	{
	    		warhead.setTypeId(0);
				warhead.getRelative(torpHeading,-1).setTypeId(0);
				warhead.getRelative(torpHeading,-2).setTypeId(0);
				warhead.getRelative(torpHeading,-3).setTypeId(0);
				
				if( testCraft != null )
			    {
			    	testCraft.addBlock(warhead, true);
			    	testCraft.addBlock(warhead.getRelative(torpHeading,-1), true);
			    	testCraft.addBlock(warhead.getRelative(torpHeading,-2), true);
			    	testCraft.addBlock(warhead.getRelative(torpHeading,-3), true);
			    }
				
				warhead = warhead.getRelative(BlockFace.UP);
				if( cannonType == 7 )
				{
					warhead.setTypeIdAndData(35, (byte) 0x3, false);
					warhead.getRelative(torpHeading,-1).setTypeIdAndData(35, (byte) 0xD, false);
					warhead.getRelative(torpHeading,-2).setTypeIdAndData(35, (byte) 0xD, false);
					warhead.getRelative(torpHeading,-3).setTypeIdAndData(35, (byte) 0x7, false);
				}else if( cannonType == 8 )
				{
					warhead.setTypeIdAndData(35, (byte) 0x8, false);
					warhead.getRelative(torpHeading,-1).setTypeIdAndData(35, (byte) 0x0, false);
					warhead.getRelative(torpHeading,-2).setTypeIdAndData(35, (byte) 0x0, false);
					warhead.getRelative(torpHeading,-3).setTypeIdAndData(35, (byte) 0x7, false);
				}else
				{
					warhead.setTypeIdAndData(35, (byte) 0xE, false);
					warhead.getRelative(torpHeading,-1).setTypeIdAndData(35, (byte) 0x8, false);
					warhead.getRelative(torpHeading,-2).setTypeIdAndData(35, (byte) 0x8, false);
					warhead.getRelative(torpHeading,-3).setTypeIdAndData(35, (byte) 0x7, false);
				}
	    		
	    	}else if( i == 0 )
	    	{
	    		warhead.setTypeId(0);
				warhead.getRelative(torpHeading,-1).setTypeId(0);
				warhead.getRelative(torpHeading,-2).setTypeId(0);
				warhead.getRelative(torpHeading,-3).setTypeId(0);
				
				if( testCraft != null )
			    {
			    	testCraft.addBlock(warhead, true);
			    	testCraft.addBlock(warhead.getRelative(torpHeading,-1), true);
			    	testCraft.addBlock(warhead.getRelative(torpHeading,-2), true);
			    	testCraft.addBlock(warhead.getRelative(torpHeading,-3), true);
			    }
				
	    		if( torpHeading == BlockFace.NORTH )
				{
					if( left )
						warhead = warhead.getRelative(BlockFace.EAST);
					else
						warhead = warhead.getRelative(BlockFace.WEST);
				}else if( torpHeading == BlockFace.SOUTH )
				{
					if( left )
						warhead = warhead.getRelative(BlockFace.WEST);
					else
						warhead = warhead.getRelative(BlockFace.EAST);
				}else if( torpHeading == BlockFace.EAST )
				{
					if( left )
						warhead = warhead.getRelative(BlockFace.SOUTH);
					else
						warhead = warhead.getRelative(BlockFace.NORTH);
				}else
				{
					if( left )
						warhead = warhead.getRelative(BlockFace.NORTH);
					else
						warhead = warhead.getRelative(BlockFace.SOUTH);
				}
	    		
	    		if( cannonType == 7 )
				{
					warhead.setTypeIdAndData(35, (byte) 0x3, false);
					warhead.getRelative(torpHeading,-1).setTypeIdAndData(35, (byte) 0xD, false);
					warhead.getRelative(torpHeading,-2).setTypeIdAndData(35, (byte) 0xD, false);
					warhead.getRelative(torpHeading,-3).setTypeIdAndData(35, (byte) 0x7, false);
				}else if( cannonType == 8 )
				{
		    		warhead.setTypeIdAndData(35, (byte) 0x8, false);
					warhead.getRelative(torpHeading,-1).setTypeIdAndData(35, (byte) 0x0, false);
					warhead.getRelative(torpHeading,-2).setTypeIdAndData(35, (byte) 0x0, false);
					warhead.getRelative(torpHeading,-3).setTypeIdAndData(35, (byte) 0x7, false);
				}else
				{
		    		warhead.setTypeIdAndData(35, (byte) 0xE, false);
					warhead.getRelative(torpHeading,-1).setTypeIdAndData(35, (byte) 0x8, false);
					warhead.getRelative(torpHeading,-2).setTypeIdAndData(35, (byte) 0x8, false);
					warhead.getRelative(torpHeading,-3).setTypeIdAndData(35, (byte) 0x7, false);
				}
	    	}else if( i == 1 && lift )
	    	{
	    		warhead = warhead.getRelative(BlockFace.UP);
	    		warhead.setTypeId(0);
				warhead.getRelative(torpHeading,-1).setTypeId(0);
				warhead.getRelative(torpHeading,-2).setTypeId(0);
				warhead.getRelative(torpHeading,-3).setTypeId(0);
				
				if( testCraft != null )
			    {
			    	testCraft.addBlock(warhead, true);
			    	testCraft.addBlock(warhead.getRelative(torpHeading,-1), true);
			    	testCraft.addBlock(warhead.getRelative(torpHeading,-2), true);
			    	testCraft.addBlock(warhead.getRelative(torpHeading,-3), true);
			    }
				
	    		if( torpHeading == BlockFace.NORTH )
				{
					if( left )
						warhead = warhead.getRelative(BlockFace.EAST);
					else
						warhead = warhead.getRelative(BlockFace.WEST);
				}else if( torpHeading == BlockFace.SOUTH )
				{
					if( left )
						warhead = warhead.getRelative(BlockFace.WEST);
					else
						warhead = warhead.getRelative(BlockFace.EAST);
				}else if( torpHeading == BlockFace.EAST )
				{
					if( left )
						warhead = warhead.getRelative(BlockFace.SOUTH);
					else
						warhead = warhead.getRelative(BlockFace.NORTH);
				}else
				{
					if( left )
						warhead = warhead.getRelative(BlockFace.NORTH);
					else
						warhead = warhead.getRelative(BlockFace.SOUTH);
				}
	    		
	    		if( cannonType == 7 )
				{
					warhead.setTypeIdAndData(35, (byte) 0x3, false);
					warhead.getRelative(torpHeading,-1).setTypeIdAndData(35, (byte) 0xD, false);
					warhead.getRelative(torpHeading,-2).setTypeIdAndData(35, (byte) 0xD, false);
					warhead.getRelative(torpHeading,-3).setTypeIdAndData(35, (byte) 0x7, false);
				}else if( cannonType == 8 )
				{
					warhead.setTypeIdAndData(35, (byte) 0x8, false);
					warhead.getRelative(torpHeading,-1).setTypeIdAndData(35, (byte) 0x0, false);
					warhead.getRelative(torpHeading,-2).setTypeIdAndData(35, (byte) 0x0, false);
					warhead.getRelative(torpHeading,-3).setTypeIdAndData(35, (byte) 0x7, false);
				}else
				{
					warhead.setTypeIdAndData(35, (byte) 0xE, false);
					warhead.getRelative(torpHeading,-1).setTypeIdAndData(35, (byte) 0x8, false);
					warhead.getRelative(torpHeading,-2).setTypeIdAndData(35, (byte) 0x8, false);
					warhead.getRelative(torpHeading,-3).setTypeIdAndData(35, (byte) 0x7, false);
				}
	    	}else
	    	{
	    		int j=i;
	    		if( lift )
	    		{
	    			warhead = warhead.getRelative(BlockFace.UP);
	    			j = j - 1;
	    		}
	    		if( torpHeading == BlockFace.NORTH )
				{
					if( left )
						warhead = warhead.getRelative(BlockFace.EAST);
					else
						warhead = warhead.getRelative(BlockFace.WEST);
				}else if( torpHeading == BlockFace.SOUTH )
				{
					if( left )
						warhead = warhead.getRelative(BlockFace.WEST);
					else
						warhead = warhead.getRelative(BlockFace.EAST);
				}else if( torpHeading == BlockFace.EAST )
				{
					if( left )
						warhead = warhead.getRelative(BlockFace.SOUTH);
					else
						warhead = warhead.getRelative(BlockFace.NORTH);
				}else
				{
					if( left )
						warhead = warhead.getRelative(BlockFace.NORTH);
					else
						warhead = warhead.getRelative(BlockFace.SOUTH);
				}
	    		
	    		
	    		for( int k=1; k<=j; k++ )
	    		{
	    			if( warhead.getRelative(torpHeading, k).getType() == Material.CLAY )
	    			{
	    				if( left )
	    					leftLoading = false;
	    				else
	    					rightLoading = false;
	    				
	    				if( testCraft != null)
    					{
    						testCraft.waitTorpLoading--;
    					}
	    				return;
	    			}
	    		}
	    		
	    		warhead = warhead.getRelative(torpHeading, j);
	    		Block oldWarhead = warhead.getRelative(torpHeading, -1);
    			oldWarhead.setTypeId(0);
    			oldWarhead.getRelative(torpHeading,-1).setTypeId(0);
    			oldWarhead.getRelative(torpHeading,-2).setTypeId(0);
    			oldWarhead.getRelative(torpHeading,-3).setTypeId(0);
    			
    			if( testCraft != null )
			    {
			    	testCraft.addBlock(oldWarhead, true);
			    	testCraft.addBlock(oldWarhead.getRelative(torpHeading,-1), true);
			    	testCraft.addBlock(oldWarhead.getRelative(torpHeading,-2), true);
			    	testCraft.addBlock(oldWarhead.getRelative(torpHeading,-3), true);
			    }
    			
    			
    			if( cannonType == 7 )
				{
					warhead.setTypeIdAndData(35, (byte) 0x3, false);
					warhead.getRelative(torpHeading,-1).setTypeIdAndData(35, (byte) 0xD, false);
					warhead.getRelative(torpHeading,-2).setTypeIdAndData(35, (byte) 0xD, false);
					warhead.getRelative(torpHeading,-3).setTypeIdAndData(35, (byte) 0x7, false);
				}else if( cannonType == 8 )
				{
					warhead.setTypeIdAndData(35, (byte) 0x8, false);
					warhead.getRelative(torpHeading,-1).setTypeIdAndData(35, (byte) 0x0, false);
					warhead.getRelative(torpHeading,-2).setTypeIdAndData(35, (byte) 0x0, false);
					warhead.getRelative(torpHeading,-3).setTypeIdAndData(35, (byte) 0x7, false);
				}else
				{
					warhead.setTypeIdAndData(35, (byte) 0xE, false);
					warhead.getRelative(torpHeading,-1).setTypeIdAndData(35, (byte) 0x8, false);
					warhead.getRelative(torpHeading,-2).setTypeIdAndData(35, (byte) 0x8, false);
					warhead.getRelative(torpHeading,-3).setTypeIdAndData(35, (byte) 0x7, false);
				}
    			
				if( testCraft != null )
			    {
			    	testCraft.addBlock(warhead, true);
			    	testCraft.addBlock(warhead.getRelative(torpHeading,-1), true);
			    	testCraft.addBlock(warhead.getRelative(torpHeading,-2), true);
			    	testCraft.addBlock(warhead.getRelative(torpHeading,-3), true);
			    }
	    	}
	    	if( i== 15 )
	    	{
	    		leftLoading = false;
				rightLoading = false;
				if( testCraft != null )
				{
					testCraft.waitTorpLoading--;
				}
	    	}
	    }
    	
    	});
    }

    @SuppressWarnings("deprecation")
	public void openTorpedoDoors(Player p, boolean inner, boolean leftInner)
    {
    	if( checkProtectedRegion(p, p.getLocation()) )
    	{
    		p.sendMessage("You are in a protected region");
    		return;
    	}
    	
    	Craft testCraft = Craft.getCraft(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
    	if( !inner )
    	{
	    	Block a,b,c;
	    	a = loc.getBlock().getRelative(direction, 5);
	    	if( direction == BlockFace.NORTH || direction == BlockFace.SOUTH )
	    	{
	    		b = a.getRelative(BlockFace.EAST);
	    		c = a.getRelative(BlockFace.WEST);
	    	}else
	    	{
	    		b = a.getRelative(BlockFace.NORTH);
	    		c = a.getRelative(BlockFace.SOUTH);
	    	}
	    	
	    	
	    	if( checkOuterDoorClosed() )
	    	{
	    		if( checkInnerDoorClosed(true) )
	    		{
	    			if( checkInnerDoorClosed(false) )
	    			{
	    				b.setType(Material.AIR);
	    	    		c.setType(Material.AIR);
	    	    		p.sendMessage("Opening Outer Tube Doors!");
	    			}else
	    			{
	    				p.sendMessage("Close BOTH inner doors before opening outer doors.");
	    			}
	    		}else
	    		{
	    			p.sendMessage("Close BOTH inner doors before opening outer doors.");
	    		}
	    	}else
	    	{
	    		b.setType(Material.CLAY);
	    		c.setType(Material.CLAY);
;
			    if( testCraft != null )
			    {
			    	testCraft.addBlock(b, true);
			    	testCraft.addBlock(c, true);
			    }
	    		p.sendMessage("Closing Outer Tube Doors!");
	    	}
    	}else ///inner doors
    	{
	    	Block a,b,c;
	    	a = loc.getBlock();
	    	if( direction == BlockFace.NORTH )
	    	{
	    		c = a.getRelative(BlockFace.EAST);
	    		b = a.getRelative(BlockFace.WEST);
	    	}else if( direction == BlockFace.SOUTH )
	    	{
	    		b = a.getRelative(BlockFace.EAST);
	    		c = a.getRelative(BlockFace.WEST);
	    	}else if( direction == BlockFace.EAST )
	    	{
	    		b = a.getRelative(BlockFace.NORTH);
	    		c = a.getRelative(BlockFace.SOUTH);
	    	}else
	    	{
	    		c = a.getRelative(BlockFace.NORTH);
	    		b = a.getRelative(BlockFace.SOUTH);
	    	}
	    	
	    	
	    	if( checkInnerDoorClosed(leftInner) )
	    	{
	    		if( checkOuterDoorClosed() )
	    		{
	    			
	    	    	
	    	    	if( leftInner )
	    	    	{
	    	    		b.setType(Material.AIR);
	    	    		if( b.getRelative(direction).getTypeId() >= 8 && b.getRelative(direction).getTypeId() <= 11)
	    	    			b.getRelative(direction).setType(Material.AIR);
	    	    		if( b.getRelative(direction,2).getTypeId() >= 8 && b.getRelative(direction,2).getTypeId() <= 11)
	    	    			b.getRelative(direction,2).setType(Material.AIR);
	    	    		if( b.getRelative(direction,3).getTypeId() >= 8 && b.getRelative(direction,3).getTypeId() <= 11)
	    	    			b.getRelative(direction,3).setType(Material.AIR);
	    	    		if( b.getRelative(direction,4).getTypeId() >= 8 && b.getRelative(direction,4).getTypeId() <= 11)
	    	    			b.getRelative(direction,4).setType(Material.AIR);
	    	    		p.sendMessage("Opening Left Inner Tube Door!");
	    	    	}
	    	    	else
	    	    	{
	    	    		c.setType(Material.AIR);
	    	    		if( c.getRelative(direction).getTypeId() >= 8 && c.getRelative(direction).getTypeId() <= 11)
	    	    			c.getRelative(direction).setType(Material.AIR);
	    	    		if( c.getRelative(direction,2).getTypeId() >= 8 && c.getRelative(direction,2).getTypeId() <= 11)
	    	    			c.getRelative(direction,2).setType(Material.AIR);
	    	    		if( c.getRelative(direction,3).getTypeId() >= 8 && c.getRelative(direction,3).getTypeId() <= 11)
	    	    			c.getRelative(direction,3).setType(Material.AIR);
	    	    		if( c.getRelative(direction,4).getTypeId() >= 8 && c.getRelative(direction,4).getTypeId() <= 11)
	    	    			c.getRelative(direction,4).setType(Material.AIR);
	    	    		p.sendMessage("Opening Right Inner Tube Door!");
	    	    	}
	    		}else
	    		{
	    			p.sendMessage("Close the OUTER doors before opening inner doors.");
	    		}
	    	}else
	    	{
	    		if( leftInner )
	    		{
	    			b.setType(Material.CLAY);
	    			if( testCraft != null )
				    {
				    	testCraft.addBlock(b, true);
				    }
	    			p.sendMessage("Closing Left Inner Tube Door!");
	    		}
	    		else
	    		{
	    			c.setType(Material.CLAY);
	    			if( testCraft != null )
				    {
				    	testCraft.addBlock(c, true);
				    }
	    			p.sendMessage("Closing Right Inner Tube Door!");
	    		}
	    	}
    	}
    }
    
    public void setTorpedoMode(Player p)
    {
    	torpedoMode++;
    	torpedoMode = torpedoMode%3;
    	switch( torpedoMode )
    	{
    	case 0:
    		p.sendMessage("Firing Mode : Left Tube");
    		break;
    	case 1:
    		p.sendMessage("Firing Mode : Right Tube");
    		break;
    	case 2:
    		p.sendMessage("Firing Mode : Both");
    		break;
    	}
    }
    
    public void fireTorpedoButton(Player p)
    {
    	if( checkProtectedRegion(p, p.getLocation()) )
    	{
    		p.sendMessage("You are in a protected region");
    		return;
    	}
    	
    	if( torpedoMode == 0 )
    	{
    		if( checkTubeLoaded(true) )
    		{
    			if( checkInnerDoorClosed(true) && !checkOuterDoorClosed() )
    			{
    				fireLeft(p);
    			}else
    			{
    				p.sendMessage("Left Tube: Open Outer Doors and Close Left Inner Door");
    			}
    		}else
    		{
    			p.sendMessage("Left Tube: Tube Not Loaded");
    		}
    		
    	}else if( torpedoMode == 1)
    	{
    		if( checkTubeLoaded(false) )
    		{
    			if( checkInnerDoorClosed(false) && !checkOuterDoorClosed() )
    			{
    				fireRight(p);
    			}else
    			{
    				p.sendMessage("Right Tube: Open Outer Doors and Close Right Inner Door");
    			}
    			
    		}else
    		{
    			p.sendMessage("Right Tube: Tube Not Loaded");
    		}
    		
    	}else
    	{
    		if( checkTubeLoaded(true) && checkTubeLoaded(false) )
    		{
    			if( checkInnerDoorClosed(true) && checkInnerDoorClosed(false) && !checkOuterDoorClosed() )
    			{
    				fireBoth(p);
    			}else
    			{
    				p.sendMessage("Both Tubes: Open Outer Doors and Close Both Inner Doors");
    			}
    			
    		}else
    		{
    			p.sendMessage("Both Tubes: Both Tubes Not Loaded");
    		}
    	}
    }
    
 
    @SuppressWarnings("deprecation")
	public void fireTorpedoMk1(final Player p, final Block b, final BlockFace torpHeading, final int torpDepth, final int delayShoot, final boolean left){

    	final Craft testCraft = Craft.getCraft(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
		final Weapon torp = new Weapon(b, torpHeading, torpDepth);
		AimCannon.weapons.add(torp);
		
		if( torp.warhead.getRelative(torp.hdg, -4).getRelative(BlockFace.UP).getTypeId() == 68 )
		{
			Sign sign = (Sign) torp.warhead.getRelative(torp.hdg, -4).getRelative(BlockFace.UP).getState();
			String signLine0 =  sign.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
			int tubeNum=0;
			if( signLine0.equalsIgnoreCase("Tube") )
			{
				String tubeString = sign.getLine(1).trim().toLowerCase();
				tubeString = tubeString.replaceAll(ChatColor.BLUE.toString(), "");
				if( !tubeString.isEmpty() )
				{
					try{
						tubeNum = Integer.parseInt(tubeString);
					}catch (NumberFormatException nfe)
					{
						tubeNum=0;
					}
				}
			}
			torp.tubeNum=tubeNum;
		}
		
		torp.setDepth = torpDepth;
		
		if( testCraft != null )
		{
			for( String s : testCraft.crewNames )
			{
				Player pl = nc.getServer().getPlayer(s);
				if( pl != null )
				{
					if( torp.tubeNum == 0 )
						pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Tube Fired!");
					else
						pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Tube " + torp.tubeNum + " Fired!");
				}
			}
		}
		
    	
    	
		new Thread(){
			
    	@Override
			public void run() {
    		
    		setPriority(Thread.MIN_PRIORITY);
				//taskNum = -1;
    			try{
    				sleep(delayShoot);
    				
    				
    				
    				
    				for( int i=0; i<150; i++ )
    				{
						fireTorpedoUpdateMk1(p, torp, i, testCraft, left);
						sleep(200);
					} 
				}catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
    	}.start(); //, 20L);
    }
    
    public void fireTorpedoUpdateMk1(final Player p, final Weapon torp, final int i, final Craft firingCraft, final boolean left) {
    	nc.getServer().getScheduler().scheduleSyncDelayedTask(nc, new Runnable(){
    	//new Thread() {
	  //  @Override
	    @SuppressWarnings({ "deprecation", "unused" })
		public void run()
	    {

	    	if( !torp.dead )
	    	{
		    	if( torp.warhead.getTypeId() == 35 && torp.warhead.getRelative(torp.hdg, -1).getTypeId() == 35 && torp.warhead.getRelative(torp.hdg, -2).getTypeId() == 35 && torp.warhead.getRelative(torp.hdg, -3).getTypeId() == 35 )
		    	{
		    		firingCraft.world.playSound(torp.warhead.getLocation(), Sound.ENTITY_PLAYER_BREATH, 2.0f, 0.8f);
		    		
					if( i > 15 )
					{
						if( torp.warhead.getY() > 62 )
			    		{
			    			torp.warhead.setType(Material.AIR);
			    			torp.warhead.getRelative(torp.hdg, -1).setType(Material.AIR);
			    			torp.warhead.getRelative(torp.hdg, -2).setType(Material.AIR);
			    			torp.warhead.getRelative(torp.hdg, -3).setType(Material.AIR);
			    		}else
			    		{
			    			torp.warhead.setType(Material.WATER);
			    			torp.warhead.getRelative(torp.hdg, -1).setType(Material.WATER);
			    			torp.warhead.getRelative(torp.hdg, -2).setType(Material.WATER);
			    			torp.warhead.getRelative(torp.hdg, -3).setType(Material.WATER);
			    		}
						
						
						
						
						//new position
						torp.warhead = torp.warhead.getRelative(torp.hdg);
						int depthDifference = torp.setDepth - (63 - torp.warhead.getY());
						if( depthDifference > 0 )
						{
							torp.warhead = torp.warhead.getRelative(BlockFace.DOWN);
						}else if( depthDifference < 0)
						{
							torp.warhead = torp.warhead.getRelative(BlockFace.UP);
						}
						
						if( torp.turnProgress > -1 )
						{
							
							if( torp.turnProgress == 10 )
							{
								if( torp.hdg == BlockFace.NORTH )
								{
									if( torp.rudder < 0 )
										torp.hdg = BlockFace.WEST;
									else
										torp.hdg = BlockFace.EAST;
								}else if( torp.hdg == BlockFace.SOUTH )
								{
									if( torp.rudder < 0 )
										torp.hdg = BlockFace.EAST;
									else
										torp.hdg = BlockFace.WEST;
								}else if( torp.hdg == BlockFace.EAST )
								{
									if( torp.rudder < 0 )
										torp.hdg = BlockFace.NORTH;
									else
										torp.hdg = BlockFace.SOUTH;
								}else
								{
									if( torp.rudder < 0 )
										torp.hdg = BlockFace.SOUTH;
									else
										torp.hdg = BlockFace.NORTH;
								}
								torp.rudder = -torp.rudder;
							}
							
							if( torp.turnProgress == 20 )
							{
								if( torp.doubleTurn )
								{
									torp.turnProgress = 0;
									torp.rudder = -torp.rudder;
									torp.doubleTurn = false;
								}else
								{
									torp.turnProgress = -1;
									torp.rudder = torp.rudderSetting;
								}
							}else
								torp.turnProgress += 1;
						}
						
						if( torp.rudder != 0 )
						{
							int dirMod  = Math.abs(torp.rudder);
							if( i % dirMod == 0 )
							{
								if( torp.rudder < 0 )
								{
									torp.warhead = getDirectionFromRelative(torp.warhead, torp.hdg, true);
								}else
								{
									torp.warhead = getDirectionFromRelative(torp.warhead, torp.hdg, false);
								}
							}
						}
						
						
						
						//check new position
						if( torp.warhead.getType() == Material.WATER || torp.warhead.getType() == Material.AIR || torp.warhead.getType() == Material.STATIONARY_WATER )
						{
		    				if( i == 149 )
							{
								if( torp.warhead.getY() > 62 )
								{
									torp.warhead.setType(Material.AIR);
									torp.warhead.getRelative(torp.hdg).setType(Material.AIR);
									torp.warhead.getRelative(torp.hdg, -2).setType(Material.AIR);
									torp.warhead.getRelative(torp.hdg, -3).setType(Material.AIR);
								}else
								{
									torp.warhead.setType(Material.WATER);
									torp.warhead.getRelative(torp.hdg, -1).setType(Material.WATER);
									torp.warhead.getRelative(torp.hdg, -2).setType(Material.WATER);
									torp.warhead.getRelative(torp.hdg, -3).setType(Material.WATER);
								}
								p.sendMessage("Torpedo expired.");
								return;
							}
		    				
		    				torp.warhead.setTypeIdAndData(35, (byte) 0x8, false);
		    				torp.warhead.getRelative(torp.hdg, -1).setTypeIdAndData(35, (byte) 0x0, false);
		    				torp.warhead.getRelative(torp.hdg, -2).setTypeIdAndData(35, (byte) 0x0, false);
		    				torp.warhead.getRelative(torp.hdg, -3).setTypeIdAndData(35, (byte) 0x7, false);
						}else if( torp.active ) ///detonate!
						{
							if( checkProtectedRegion(p, torp.warhead.getLocation()) )
							{
								p.sendMessage(ChatColor.RED + "No torpedo explosions in dock area.");
								return;
							}
							
							
							
							
							
							torp.warhead = torp.warhead.getRelative(torp.hdg,-1);
							NavyCraft.explosion(7,  torp.warhead, false);
							torp.dead = true;
	
							Craft checkCraft=null;
							checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(torp.hdg,2).getLocation(), p);
							if( checkCraft == null ) {
								checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(7,7,7).getLocation(), p);
								if( checkCraft == null ) {
									checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(-7,-7,-7).getLocation(), p);
									if( checkCraft == null ) {
										checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(3,-2,-3).getLocation(), p);
										if( checkCraft == null ) {
											checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(-3,2,3).getLocation(), p);
										}
									}
								}
							}
							
							if( checkCraft == null )
								p.sendMessage("Torpedo hit unknown object!");
							else
								p.sendMessage("Torpedo hit " + checkCraft.name + "!");
										
						
						}else
						{
							torp.dead = true;
							p.sendMessage("Torpedo Dud (Too close).");
						}
						
						
					}
					else/// i <= 15
					{
						if( torp.warhead.getY() > 62 || i < 5 )
						{
							torp.warhead.setType(Material.AIR);
							torp.warhead.getRelative(torp.hdg, -1).setType(Material.AIR);
							torp.warhead.getRelative(torp.hdg, -2).setType(Material.AIR);
							torp.warhead.getRelative(torp.hdg, -3).setType(Material.AIR);
							
							if( i == 4  )
							{
								if( firingCraft != null )
								{
									firingCraft.addBlock(torp.warhead, true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -1), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -2), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -3), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -4), true);
	
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -5), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -6), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -7), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -8), true);
								}
							}
						}else
						{
							torp.warhead.setType(Material.WATER);
							torp.warhead.getRelative(torp.hdg, -1).setType(Material.WATER);
							torp.warhead.getRelative(torp.hdg, -2).setType(Material.WATER);
							torp.warhead.getRelative(torp.hdg, -3).setType(Material.WATER);
						}
						
	
						//Move torp
						torp.warhead = torp.warhead.getRelative(torp.hdg);
						
						if( torp.warhead.getType() == Material.WATER || torp.warhead.getType() == Material.AIR || torp.warhead.getType() == Material.STATIONARY_WATER )
						{
							torp.warhead.setTypeIdAndData(35, (byte) 0x8, false);
			    			torp.warhead.getRelative(torp.hdg, -1).setTypeIdAndData(35, (byte) 0x0, false);
			    			torp.warhead.getRelative(torp.hdg, -2).setTypeIdAndData(35, (byte) 0x0, false);
			    			torp.warhead.getRelative(torp.hdg, -3).setTypeIdAndData(35, (byte) 0x7, false);
						}else
						{
							if( firingCraft != null )
							{
								firingCraft.waitTorpLoading--;
								if( left )
									leftLoading = false;
								else
									rightLoading = false;
								
								if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
									openTorpedoDoors(p, false, false);
							}else
							{
								if( left )
									leftLoading = false;
								else
									rightLoading = false;
								
								if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
									openTorpedoDoors(p, false, false);
							}
							p.sendMessage("Dud Torpedo! Too close...");
							torp.dead = true;
						}
						
						
					}
					
					
		    		
		    	}else //else torp blocks missing, detonate
		    	{
		    		if( checkProtectedRegion(p, torp.warhead.getLocation()) )
					{
						p.sendMessage(ChatColor.RED + "No torpedo explosions in dock area.");
						return;
					}
					
		    		if( !torp.active )
		    		{
		    			p.sendMessage("Dud Torpedo! Too close...");
						torp.dead = true;
						if( firingCraft != null )
						{
							firingCraft.waitTorpLoading--;
							if( left )
								leftLoading = false;
							else
								rightLoading = false;
							
							if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
								openTorpedoDoors(p, false, false);
						}else
						{
							if( left )
								leftLoading = false;
							else
								rightLoading = false;
							
							if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
								openTorpedoDoors(p, false, false);
						}
						return;
		    		}
					
					
					torp.warhead = torp.warhead.getRelative(torp.hdg,-1);
					NavyCraft.explosion(7,  torp.warhead, false);
					torp.dead = true;
					
					Craft checkCraft=null;
					checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(torp.hdg,2).getLocation(), p);
					if( checkCraft == null ) {
						checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(7,7,7).getLocation(), p);
						if( checkCraft == null ) {
							checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(-7,-7,-7).getLocation(), p);
							if( checkCraft == null ) {
								checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(3,-2,-3).getLocation(), p);
								if( checkCraft == null ) {
									checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(-3,2,3).getLocation(), p);
								}
							}
						}
					}
					
					if( checkCraft == null )
						p.sendMessage("Torpedo detonated prematurely!");
					else
						p.sendMessage("Torpedo hit " + checkCraft.name + "!");
					
					
					if( firingCraft != null )
					{
						firingCraft.waitTorpLoading--;
						if( left )
							leftLoading = false;
						else
							rightLoading = false;
						
						if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
							openTorpedoDoors(p, false, false);
					}else
					{
						if( left )
							leftLoading = false;
						else
							rightLoading = false;
						
						if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
							openTorpedoDoors(p, false, false);
					}
		    	}
		    	if( i == 15 )
				{
					
					if( firingCraft != null )
					{
						firingCraft.waitTorpLoading--;
						if( left )
							leftLoading = false;
						else
							rightLoading = false;
						
						if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
							openTorpedoDoors(p, false, false);
					}else
					{
						if( left )
							leftLoading = false;
						else
							rightLoading = false;
						
						if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
							openTorpedoDoors(p, false, false);
					}
					torp.active = true;
				}
	    	}

	    }
    	}
    	
	);

    }
    
    

    @SuppressWarnings("deprecation")
	public void fireTorpedoMk2(final Player p, final Block b, final BlockFace torpHeading, final int torpDepth, final int delayShoot, final boolean left){
    	//final int taskNum;
    	//int taskNum = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
    	final Craft testCraft = Craft.getCraft(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
		final Weapon torp = new Weapon(b, torpHeading, torpDepth);
		AimCannon.weapons.add(torp);
		
		if( torp.warhead.getRelative(torp.hdg, -4).getRelative(BlockFace.UP).getTypeId() == 68 )
		{
			Sign sign = (Sign) torp.warhead.getRelative(torp.hdg, -4).getRelative(BlockFace.UP).getState();
			String signLine0 =  sign.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
			int tubeNum=0;
			if( signLine0.equalsIgnoreCase("Tube") )
			{
				String tubeString = sign.getLine(1).trim().toLowerCase();
				tubeString = tubeString.replaceAll(ChatColor.BLUE.toString(), "");
				if( !tubeString.isEmpty() )
				{
					try{
						tubeNum = Integer.parseInt(tubeString);
					}catch (NumberFormatException nfe)
					{
						tubeNum=0;
					}
				}
			}
			torp.tubeNum=tubeNum;
		}
		
		if( testCraft != null && testCraft.tubeMk1FiringDisplay > -1 )
		{
			if( testCraft.tubeMk1FiringDepth > -1)
				torp.setDepth = testCraft.tubeMk1FiringDepth;
			else
				torp.setDepth = torpDepth;
			Player onScopePlayer=null;
			for( Periscope per: testCraft.periscopes )
			{
				if( per.user != null )
				{
					onScopePlayer = per.user;
					break;
				}
			}
			

			float rotation=0;
			int torpRotation=0;
			if( direction == BlockFace.SOUTH )
				torpRotation=180;
			else if( direction == BlockFace.WEST )
				torpRotation=270;
			else if( direction == BlockFace.EAST )
				torpRotation=90;
			else if( direction == BlockFace.NORTH )
				torpRotation=0;
			
			if( onScopePlayer != null && testCraft.tubeMk1FiringMode == -1 )
				rotation = (float) Math.PI * onScopePlayer.getLocation().getYaw() / 180f;
			else if( testCraft.lastPeriscopeYaw != -9999 && testCraft.tubeMk1FiringMode == -1 )
				rotation = (float) Math.PI * testCraft.lastPeriscopeYaw / 180f;
			else
			{
				rotation = (float) Math.PI * (torpRotation+180f) / 180f;
				
			}
			
			if( left )
				rotation -= testCraft.tubeMk1FiringSpread*Math.PI/180f;
			else
				rotation += testCraft.tubeMk1FiringSpread*Math.PI/180f;
			
			float nx = -(float) Math.sin(rotation);
			float nz = (float) Math.cos(rotation);

		////north
			
			//p.sendMessage("torpRotation=" + torpRotation + " rotation=" + rotation);
			
					
			if( torpRotation%360 == 0 )
			{
				if( nx > 0.5 )
				{
					torp.rudder = 1;
					torp.turnProgress = 0;
					if(  Math.abs(nz) > .07 )
					{
						torp.rudderSetting = (int)(1.0f / nz);
						if( torp.rudderSetting > 10 )
							torp.rudderSetting = 10;
						else if( torp.rudderSetting < -10 )
							torp.rudderSetting = -10;
					}
				}
				else if( nx < -0.5 )
				{
					torp.rudder = -1;
					torp.turnProgress = 0;
					if( Math.abs(nz) > .07 )
					{
						torp.rudderSetting = -(int)(1.0f / nz);
						if( torp.rudderSetting > 10 )
							torp.rudderSetting = 10;
						else if( torp.rudderSetting < -10 )
							torp.rudderSetting = -10;
					}
				}else if( nz < 0 )
				{
					if(  Math.abs(nx) > .07 )
					{
						torp.rudder = (int)(1.0f / nx);
						if( torp.rudder > 10 )
							torp.rudder = 10;
						else if( torp.rudder < -10 )
							torp.rudder = -10;
						torp.rudderSetting = torp.rudder;
					}
				}else
				{
					if( nx < 0 )
					{
						torp.doubleTurn = true;
						torp.rudder = -1;
						torp.turnProgress = 0;
					}else if( nx > 0 )
					{
						torp.doubleTurn = true;
						torp.rudder = 1;
						torp.turnProgress = 0;
					}
					if(  Math.abs(nx) > .07 )
					{
						torp.rudderSetting = (int)(1.0f / -nx);
						if( torp.rudderSetting > 10 )
							torp.rudderSetting = 10;
						else if( torp.rudderSetting < -10 )
							torp.rudderSetting = -10;
					}
				}
				
			
			//////south
			}else if( torpRotation%360 == 180 )
			{
				
				if( nx > 0.5 )
				{
					torp.rudder = -1;
					torp.turnProgress = 0;
					if(  Math.abs(nz) > .07 )
					{
						torp.rudderSetting = (int)(1.0f / nz);
						if( torp.rudderSetting > 10 )
							torp.rudderSetting = 10;
						else if( torp.rudderSetting < -10 )
							torp.rudderSetting = -10;
					}
				}
				else if( nx < -0.5 )
				{
					torp.rudder = 1;
					torp.turnProgress = 0;
					if(  Math.abs(nz) > .07 )
					{
						torp.rudderSetting = (int)(1.0f / -nz);
						if( torp.rudderSetting > 10 )
							torp.rudderSetting = 10;
						else if( torp.rudderSetting < -10 )
							torp.rudderSetting = -10;
					}
				}else if( nz > 0 )
				{
					if(  Math.abs(nx) > .07 )
					{
						torp.rudder = (int)(1.0f / -nx);
						if( torp.rudder > 10 )
							torp.rudder = 10;
						else if( torp.rudder < -10 )
							torp.rudder = -10;
						torp.rudderSetting = torp.rudder;
					}
				}else
				{
					if( nx < 0 )
					{
						torp.doubleTurn = true;
						torp.rudder = 1;
						torp.turnProgress = 0;
					}else if( nx > 0 )
					{
						torp.doubleTurn = true;
						torp.rudder = -1;
						torp.turnProgress = 0;
					}
					if(  Math.abs(nx) > .07 )
					{
						torp.rudderSetting = (int)(1.0f / nx);
						if( torp.rudderSetting > 10 )
							torp.rudderSetting = 10;
						else if( torp.rudderSetting < -10 )
							torp.rudderSetting = -10;
					}
				}
			//////east
			}else if( torpRotation%360 == 90 )
			{
				
				if( nz > 0.5 )
				{
					torp.rudder = 1;
					torp.turnProgress = 0;
					if(  Math.abs(nx) > .07 )
					{
						torp.rudderSetting = (int)(1.0f / -nx);
						if( torp.rudderSetting > 10 )
							torp.rudderSetting = 10;
						else if( torp.rudderSetting < -10 )
							torp.rudderSetting = -10;
					}
				}
				else if( nz < -0.5 )
				{
					torp.rudder = -1;
					torp.turnProgress = 0;
					if(  Math.abs(nx) > .07 )
					{
						torp.rudderSetting = (int)(1.0f / nx);
						if( torp.rudderSetting > 10 )
							torp.rudderSetting = 10;
						else if( torp.rudderSetting < -10 )
							torp.rudderSetting = -10;
					}
				}else if( nx > 0 )
				{
					if(  Math.abs(nz) > .07 )
					{
						torp.rudder = (int)(1.0f / nz);
						if( torp.rudder > 10 )
							torp.rudder = 10;
						else if( torp.rudder < -10 )
							torp.rudder = -10;
						torp.rudderSetting = torp.rudder;
					}
				}else
				{
					if( nz < 0 )
					{
						torp.doubleTurn = true;
						torp.rudder = -1;
						torp.turnProgress = 0;
					}else if( nz > 0 )
					{
						torp.doubleTurn = true;
						torp.rudder = 1;
						torp.turnProgress = 0;
					}
					if(  Math.abs(nz) > .07 )
					{
						torp.rudderSetting = (int)(1.0f / -nz);
						if( torp.rudderSetting > 10 )
							torp.rudderSetting = 10;
						else if( torp.rudderSetting < -10 )
							torp.rudderSetting = -10;
					}
				}
			//////////////west
			}else if( torpRotation%360 == 270 )
			{
				if( nz > 0.5 )
				{
					torp.rudder = -1;
					torp.turnProgress = 0;
					if(  Math.abs(nx) > .07 )
					{
						torp.rudderSetting = (int)(1.0f / -nx);
						if( torp.rudderSetting > 10 )
							torp.rudderSetting = 10;
						else if( torp.rudderSetting < -10 )
							torp.rudderSetting = -10;
					}
				}
				else if( nz < -0.5 )
				{
					torp.rudder = 1;
					torp.turnProgress = 0;
					if(  Math.abs(nx) > .07 )
					{
						torp.rudderSetting = (int)(1.0f / nx);
						if( torp.rudderSetting > 10 )
							torp.rudderSetting = 10;
						else if( torp.rudderSetting < -10 )
							torp.rudderSetting = -10;
					}
				}else if( nx < 0 )
				{
					if(  Math.abs(nz) > .07 )
					{
						torp.rudder = (int)(1.0f / -nz);
						if( torp.rudder > 10 )
							torp.rudder = 10;
						else if( torp.rudder < -10 )
							torp.rudder = -10;
						torp.rudderSetting = torp.rudder;
					}
				}else
				{
					if( nz < 0 )
					{
						torp.doubleTurn = true;
						torp.rudder = 1;
						torp.turnProgress = 0;
					}else if( nz > 0 )
					{
						torp.doubleTurn = true;
						torp.rudder = -1;
						torp.turnProgress = 0;
					}
					if(  Math.abs(nz) > .07 )
					{
						torp.rudderSetting = (int)(1.0f / nz);
						if( torp.rudderSetting > 10 )
							torp.rudderSetting = 10;
						else if( torp.rudderSetting < -10 )
							torp.rudderSetting = -10;
					}
				}
			}
			
			
			for( String s : testCraft.crewNames )
			{
				Player pl = nc.getServer().getPlayer(s);
				if( pl != null )
				{
					if( torp.tubeNum == 0 )
						pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Tube Fired!");
					else
						pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Tube " + torp.tubeNum + " Fired!");
				}
			}
		}else
		{
			torp.setDepth = depth;
		}
    	
    	
    	
		new Thread(){
			
    	@Override
			public void run() {
    		
    		setPriority(Thread.MIN_PRIORITY);
				//taskNum = -1;
    			try{
    				sleep(delayShoot);
    				
    				
    				
    				
    				for( int i=0; i<250; i++ )
    				{
						fireTorpedoUpdateMk2(p, torp, i, testCraft, left);
						sleep(160);
					} 
				}catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
    	}.start(); //, 20L);
    }
    
    public void fireTorpedoUpdateMk2(final Player p, final Weapon torp, final int i, final Craft firingCraft, final boolean left) {
    	nc.getServer().getScheduler().scheduleSyncDelayedTask(nc, new Runnable(){
    	//new Thread() {
	  //  @Override
	    @SuppressWarnings({ "deprecation", "unused" })
		public void run()
	    {
	    	//getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
	    //	}
		//setPriority(Thread.MIN_PRIORITY);
			//try
			//{ 
	    	if( !torp.dead )
	    	{
		    	if( torp.warhead.getTypeId() == 35 && torp.warhead.getRelative(torp.hdg, -1).getTypeId() == 35 && torp.warhead.getRelative(torp.hdg, -2).getTypeId() == 35 && torp.warhead.getRelative(torp.hdg, -3).getTypeId() == 35 )
		    	{
		    		firingCraft.world.playSound(torp.warhead.getLocation(), Sound.ENTITY_PLAYER_BREATH, 2.0f, 0.8f);
					if( i > 15 )
					{
						if( torp.warhead.getY() > 62 )
			    		{
			    			torp.warhead.setType(Material.AIR);
			    			torp.warhead.getRelative(torp.hdg, -1).setType(Material.AIR);
			    			torp.warhead.getRelative(torp.hdg, -2).setType(Material.AIR);
			    			torp.warhead.getRelative(torp.hdg, -3).setType(Material.AIR);
			    		}else
			    		{
			    			torp.warhead.setType(Material.WATER);
			    			torp.warhead.getRelative(torp.hdg, -1).setType(Material.WATER);
			    			torp.warhead.getRelative(torp.hdg, -2).setType(Material.WATER);
			    			torp.warhead.getRelative(torp.hdg, -3).setType(Material.WATER);
			    		}
						
						
						
						
						//new position
						torp.warhead = torp.warhead.getRelative(torp.hdg);
						int depthDifference = torp.setDepth - (63 - torp.warhead.getY());
						if( depthDifference > 0 )
						{
							torp.warhead = torp.warhead.getRelative(BlockFace.DOWN);
						}else if( depthDifference < 0)
						{
							torp.warhead = torp.warhead.getRelative(BlockFace.UP);
						}
						
						if( torp.turnProgress > -1 )
						{
							
							if( torp.turnProgress == 10 )
							{
								if( torp.hdg == BlockFace.NORTH )
								{
									if( torp.rudder < 0 )
										torp.hdg = BlockFace.WEST;
									else
										torp.hdg = BlockFace.EAST;
								}else if( torp.hdg == BlockFace.SOUTH )
								{
									if( torp.rudder < 0 )
										torp.hdg = BlockFace.EAST;
									else
										torp.hdg = BlockFace.WEST;
								}else if( torp.hdg == BlockFace.EAST )
								{
									if( torp.rudder < 0 )
										torp.hdg = BlockFace.NORTH;
									else
										torp.hdg = BlockFace.SOUTH;
								}else
								{
									if( torp.rudder < 0 )
										torp.hdg = BlockFace.SOUTH;
									else
										torp.hdg = BlockFace.NORTH;
								}
								torp.rudder = -torp.rudder;
							}
							
							if( torp.turnProgress == 20 )
							{
								if( torp.doubleTurn )
								{
									torp.turnProgress = 0;
									torp.rudder = -torp.rudder;
									torp.doubleTurn = false;
								}else
								{
									torp.turnProgress = -1;
									torp.rudder = torp.rudderSetting;
								}
							}else
								torp.turnProgress += 1;
						}
						
						if( torp.rudder != 0 )
						{
							int dirMod  = Math.abs(torp.rudder);
							if( i % dirMod == 0 )
							{
								if( torp.rudder < 0 )
								{
									torp.warhead = getDirectionFromRelative(torp.warhead, torp.hdg, true);
								}else
								{
									torp.warhead = getDirectionFromRelative(torp.warhead, torp.hdg, false);
								}
							}
						}
						
						
						
						//check new position
						if( torp.warhead.getType() == Material.WATER || torp.warhead.getType() == Material.AIR || torp.warhead.getType() == Material.STATIONARY_WATER )
						{
		    				if( i == 249 )
							{
								if( torp.warhead.getY() > 62 )
								{
									torp.warhead.setType(Material.AIR);
									torp.warhead.getRelative(torp.hdg).setType(Material.AIR);
									torp.warhead.getRelative(torp.hdg, -2).setType(Material.AIR);
									torp.warhead.getRelative(torp.hdg, -3).setType(Material.AIR);
								}else
								{
									torp.warhead.setType(Material.WATER);
									torp.warhead.getRelative(torp.hdg, -1).setType(Material.WATER);
									torp.warhead.getRelative(torp.hdg, -2).setType(Material.WATER);
									torp.warhead.getRelative(torp.hdg, -3).setType(Material.WATER);
								}
								p.sendMessage("Torpedo expired.");
								return;
							}
		    				
		    				torp.warhead.setTypeIdAndData(35, (byte) 0xE, false);
		    				torp.warhead.getRelative(torp.hdg, -1).setTypeIdAndData(35, (byte) 0x8, false);
		    				torp.warhead.getRelative(torp.hdg, -2).setTypeIdAndData(35, (byte) 0x8, false);
		    				torp.warhead.getRelative(torp.hdg, -3).setTypeIdAndData(35, (byte) 0x7, false);
						}else if( torp.active ) ///detonate!
						{
							if( checkProtectedRegion(p, torp.warhead.getLocation()) )
							{
								p.sendMessage(ChatColor.RED + "No torpedo explosions in dock area.");
								torp.dead = true;
								return;
							}
	
							
							torp.warhead = torp.warhead.getRelative(torp.hdg,-1);
							NavyCraft.explosion(10,  torp.warhead, false);
							torp.dead = true;
							
							Craft checkCraft=null;
							checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(torp.hdg,2).getLocation(), p);
							if( checkCraft == null ) {
								checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(7,7,7).getLocation(), p);
								if( checkCraft == null ) {
									checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(-7,-7,-7).getLocation(), p);
									if( checkCraft == null ) {
										checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(3,-2,-3).getLocation(), p);
										if( checkCraft == null ) {
											checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(-3,2,3).getLocation(), p);
										}
									}
								}
							}
							
							if( checkCraft == null )
								p.sendMessage("Torpedo hit unknown object!");
							else
								p.sendMessage("Torpedo hit " + checkCraft.name + "!");
							
						}else
						{
							p.sendMessage("Torpedo Dud (Too close).");
							torp.dead = true;
						}
						
						
					}
					else/// i <= 15
					{
						if( torp.warhead.getY() > 62 || i < 5 )
						{
							torp.warhead.setType(Material.AIR);
							torp.warhead.getRelative(torp.hdg, -1).setType(Material.AIR);
							torp.warhead.getRelative(torp.hdg, -2).setType(Material.AIR);
							torp.warhead.getRelative(torp.hdg, -3).setType(Material.AIR);
							
							if( i == 4  )
							{
								if( firingCraft != null )
								{
									firingCraft.addBlock(torp.warhead, true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -1), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -2), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -3), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -4), true);
	
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -5), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -6), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -7), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -8), true);
								}
							}
						}else
						{
							torp.warhead.setType(Material.WATER);
							torp.warhead.getRelative(torp.hdg, -1).setType(Material.WATER);
							torp.warhead.getRelative(torp.hdg, -2).setType(Material.WATER);
							torp.warhead.getRelative(torp.hdg, -3).setType(Material.WATER);
						}
						
						
						
						
						//Move torp
						torp.warhead = torp.warhead.getRelative(torp.hdg);
						
						if( torp.warhead.getType() == Material.WATER || torp.warhead.getType() == Material.AIR || torp.warhead.getType() == Material.STATIONARY_WATER )
						{
							torp.warhead.setTypeIdAndData(35, (byte) 0xE, false);
			    			torp.warhead.getRelative(torp.hdg, -1).setTypeIdAndData(35, (byte) 0x8, false);
			    			torp.warhead.getRelative(torp.hdg, -2).setTypeIdAndData(35, (byte) 0x8, false);
			    			torp.warhead.getRelative(torp.hdg, -3).setTypeIdAndData(35, (byte) 0x7, false);
						}else
						{
							if( firingCraft != null )
							{
								firingCraft.waitTorpLoading--;
								if( left )
									leftLoading = false;
								else
									rightLoading = false;
								
								if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
									openTorpedoDoors(p, false, false);
							}else
							{
								if( left )
									leftLoading = false;
								else
									rightLoading = false;
								
								if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
									openTorpedoDoors(p, false, false);
							}
							torp.dead = true;
							p.sendMessage("Dud Torpedo! Too close...");
						}
	
					}
					
					
		    		
		    	}else //torp blocks missing, detonate
		    	{
		    		if( checkProtectedRegion(p, torp.warhead.getLocation()) )
					{
						p.sendMessage(ChatColor.RED + "No torpedo explosions in dock area.");
						return;
					}
					
		    		if( !torp.active )
		    		{
		    			p.sendMessage("Dud Torpedo! Too close...");
						torp.dead = true;
						if( firingCraft != null )
						{
							firingCraft.waitTorpLoading--;
							if( left )
								leftLoading = false;
							else
								rightLoading = false;
							
							if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
								openTorpedoDoors(p, false, false);
						}else
						{
							if( left )
								leftLoading = false;
							else
								rightLoading = false;
							
							if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
								openTorpedoDoors(p, false, false);
						}
						return;
		    		}
					
					
					torp.warhead = torp.warhead.getRelative(torp.hdg,-1);
					NavyCraft.explosion(10,  torp.warhead, false);
					torp.dead = true;
					
					Craft checkCraft=null;
					checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(torp.hdg,2).getLocation(), p);
					if( checkCraft == null ) {
						checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(7,7,7).getLocation(), p);
						if( checkCraft == null ) {
							checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(-7,-7,-7).getLocation(), p);
							if( checkCraft == null ) {
								checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(3,-2,-3).getLocation(), p);
								if( checkCraft == null ) {
									checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(-3,2,3).getLocation(), p);
								}
							}
						}
					}
					
					if( checkCraft == null )
						p.sendMessage("Torpedo detonated prematurely!");
					else
						p.sendMessage("Torpedo hit " + checkCraft.name + "!");
					
					
					if( firingCraft != null )
					{
						firingCraft.waitTorpLoading--;
						if( left )
							leftLoading = false;
						else
							rightLoading = false;
						
						if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
							openTorpedoDoors(p, false, false);
					}else
					{
						if( left )
							leftLoading = false;
						else
							rightLoading = false;
						
						if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
							openTorpedoDoors(p, false, false);
					}
		    	}
		    	
		    	if( i == 15 )
				{
					
					if( firingCraft != null )
					{
						firingCraft.waitTorpLoading--;
						if( left )
							leftLoading = false;
						else
							rightLoading = false;
						
						if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
							openTorpedoDoors(p, false, false);
					}else
					{
						if( left )
							leftLoading = false;
						else
							rightLoading = false;
						
						if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
							openTorpedoDoors(p, false, false);
					}
					torp.active = true;
				}
	    	}

	    }
    	}
    	
	);

    }
    
    
    @SuppressWarnings("deprecation")
	public void fireTorpedoMk3(final Player p, final Block b, final BlockFace torpHeading, final int torpDepth, final int delayShoot, final boolean left){
    	//final int taskNum;
    	//int taskNum = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
    	final Craft testCraft = Craft.getCraft(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
		final Weapon torp = new Weapon(b, torpHeading, torpDepth);
		AimCannon.weapons.add(torp);
		
		if( torp.warhead.getRelative(torp.hdg, -4).getRelative(BlockFace.UP).getTypeId() == 68 )
		{
			Sign sign = (Sign) torp.warhead.getRelative(torp.hdg, -4).getRelative(BlockFace.UP).getState();
			String signLine0 =  sign.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
			int tubeNum=0;
			if( signLine0.equalsIgnoreCase("Tube") )
			{
				String tubeString = sign.getLine(1).trim().toLowerCase();
				tubeString = tubeString.replaceAll(ChatColor.BLUE.toString(), "");
				if( !tubeString.isEmpty() )
				{
					try{
						tubeNum = Integer.parseInt(tubeString);
					}catch (NumberFormatException nfe)
					{
						tubeNum=0;
					}
				}
			}
			torp.tubeNum=tubeNum;
		}
		
		if( testCraft != null && torp.tubeNum != 0 && testCraft.tubeFiringMode.containsKey(torp.tubeNum) )
		{
			torp.setDepth = testCraft.tubeFiringDepth.get(torp.tubeNum);
			Player onScopePlayer=null;
			for( Periscope per: testCraft.periscopes )
			{
				if( per.user != null )
				{
					onScopePlayer = per.user;
					break;
				}
			}
			//if( testCraft.tubeFiringMode.get(torp.tubeNum) == -1 && (onScopePlayer != null || testCraft.lastPeriscopeYaw != -9999) )
			//{
				/*float rotation=0;
				if( onScopePlayer != null)
					rotation = (float) Math.PI * onScopePlayer.getLocation().getYaw() / 180f;
				else
					rotation = (float) Math.PI * testCraft.lastPeriscopeYaw / 180f;*/
				float rotation=0;
				int torpRotation=0;
				if( direction == BlockFace.SOUTH )
					torpRotation=180;
				else if( direction == BlockFace.WEST )
					torpRotation=270;
				else if( direction == BlockFace.EAST )
					torpRotation=90;
				else if( direction == BlockFace.NORTH )
					torpRotation=0;
				
				if( onScopePlayer != null && testCraft.tubeFiringMode.get(torp.tubeNum) == -1 )
					rotation = (float) Math.PI * onScopePlayer.getLocation().getYaw() / 180f;
				else if( testCraft.lastPeriscopeYaw != -9999 && testCraft.tubeFiringMode.get(torp.tubeNum) == -1 )
					rotation = (float) Math.PI * testCraft.lastPeriscopeYaw / 180f;
				else
				{
					rotation = (float) Math.PI * (torpRotation+180f) / 180f;
					
				}
				float nx = -(float) Math.sin(rotation);
				float nz = (float) Math.cos(rotation);

			////north
				if( torpRotation%360 == 0 )
				{
					if( nx > 0.5 )
					{
						torp.rudder = 1;
						torp.turnProgress = 0;
						if(  Math.abs(nz) > .07 )
						{
							torp.rudderSetting = (int)(1.0f / nz);
							if( torp.rudderSetting > 10 )
								torp.rudderSetting = 10;
							else if( torp.rudderSetting < -10 )
								torp.rudderSetting = -10;
						}
					}
					else if( nx < -0.5 )
					{
						torp.rudder = -1;
						torp.turnProgress = 0;
						if( Math.abs(nz) > .07 )
						{
							torp.rudderSetting = -(int)(1.0f / nz);
							if( torp.rudderSetting > 10 )
								torp.rudderSetting = 10;
							else if( torp.rudderSetting < -10 )
								torp.rudderSetting = -10;
						}
					}else if( nz < 0 )
					{
						if(  Math.abs(nx) > .07 )
						{
							torp.rudder = (int)(1.0f / nx);
							if( torp.rudder > 10 )
								torp.rudder = 10;
							else if( torp.rudder < -10 )
								torp.rudder = -10;
							torp.rudderSetting = torp.rudder;
						}
					}else
					{
						if( nx < 0 )
						{
							torp.doubleTurn = true;
							torp.rudder = -1;
							torp.turnProgress = 0;
						}else if( nx > 0 )
						{
							torp.doubleTurn = true;
							torp.rudder = 1;
							torp.turnProgress = 0;
						}
						if(  Math.abs(nx) > .07 )
						{
							torp.rudderSetting = (int)(1.0f / -nx);
							if( torp.rudderSetting > 10 )
								torp.rudderSetting = 10;
							else if( torp.rudderSetting < -10 )
								torp.rudderSetting = -10;
						}
					}
					
				
				//////south
				}else if( torpRotation%360 == 180 )
				{
					
					if( nx > 0.5 )
					{
						torp.rudder = -1;
						torp.turnProgress = 0;
						if(  Math.abs(nz) > .07 )
						{
							torp.rudderSetting = (int)(1.0f / nz);
							if( torp.rudderSetting > 10 )
								torp.rudderSetting = 10;
							else if( torp.rudderSetting < -10 )
								torp.rudderSetting = -10;
						}
					}
					else if( nx < -0.5 )
					{
						torp.rudder = 1;
						torp.turnProgress = 0;
						if(  Math.abs(nz) > .07 )
						{
							torp.rudderSetting = (int)(1.0f / -nz);
							if( torp.rudderSetting > 10 )
								torp.rudderSetting = 10;
							else if( torp.rudderSetting < -10 )
								torp.rudderSetting = -10;
						}
					}else if( nz > 0 )
					{
						if(  Math.abs(nx) > .07 )
						{
							torp.rudder = (int)(1.0f / -nx);
							if( torp.rudder > 10 )
								torp.rudder = 10;
							else if( torp.rudder < -10 )
								torp.rudder = -10;
							torp.rudderSetting = torp.rudder;
						}
					}else
					{
						if( nx < 0 )
						{
							torp.doubleTurn = true;
							torp.rudder = 1;
							torp.turnProgress = 0;
						}else if( nx > 0 )
						{
							torp.doubleTurn = true;
							torp.rudder = -1;
							torp.turnProgress = 0;
						}
						if(  Math.abs(nx) > .07 )
						{
							torp.rudderSetting = (int)(1.0f / nx);
							if( torp.rudderSetting > 10 )
								torp.rudderSetting = 10;
							else if( torp.rudderSetting < -10 )
								torp.rudderSetting = -10;
						}
					}
				//////east
				}else if( torpRotation%360 == 90 )
				{
					
					if( nz > 0.5 )
					{
						torp.rudder = 1;
						torp.turnProgress = 0;
						if(  Math.abs(nx) > .07 )
						{
							torp.rudderSetting = (int)(1.0f / -nx);
							if( torp.rudderSetting > 10 )
								torp.rudderSetting = 10;
							else if( torp.rudderSetting < -10 )
								torp.rudderSetting = -10;
						}
					}
					else if( nz < -0.5 )
					{
						torp.rudder = -1;
						torp.turnProgress = 0;
						if(  Math.abs(nx) > .07 )
						{
							torp.rudderSetting = (int)(1.0f / nx);
							if( torp.rudderSetting > 10 )
								torp.rudderSetting = 10;
							else if( torp.rudderSetting < -10 )
								torp.rudderSetting = -10;
						}
					}else if( nx > 0 )
					{
						if(  Math.abs(nz) > .07 )
						{
							torp.rudder = (int)(1.0f / nz);
							if( torp.rudder > 10 )
								torp.rudder = 10;
							else if( torp.rudder < -10 )
								torp.rudder = -10;
							torp.rudderSetting = torp.rudder;
						}
					}else
					{
						if( nz < 0 )
						{
							torp.doubleTurn = true;
							torp.rudder = -1;
							torp.turnProgress = 0;
						}else if( nz > 0 )
						{
							torp.doubleTurn = true;
							torp.rudder = 1;
							torp.turnProgress = 0;
						}
						if(  Math.abs(nz) > .07 )
						{
							torp.rudderSetting = (int)(1.0f / -nz);
							if( torp.rudderSetting > 10 )
								torp.rudderSetting = 10;
							else if( torp.rudderSetting < -10 )
								torp.rudderSetting = -10;
						}
					}
				//////////////west
				}else if( torpRotation%360 == 270 )
				{
					if( nz > 0.5 )
					{
						torp.rudder = -1;
						torp.turnProgress = 0;
						if(  Math.abs(nx) > .07 )
						{
							torp.rudderSetting = (int)(1.0f / -nx);
							if( torp.rudderSetting > 10 )
								torp.rudderSetting = 10;
							else if( torp.rudderSetting < -10 )
								torp.rudderSetting = -10;
						}
					}
					else if( nz < -0.5 )
					{
						torp.rudder = 1;
						torp.turnProgress = 0;
						if(  Math.abs(nx) > .07 )
						{
							torp.rudderSetting = (int)(1.0f / nx);
							if( torp.rudderSetting > 10 )
								torp.rudderSetting = 10;
							else if( torp.rudderSetting < -10 )
								torp.rudderSetting = -10;
						}
					}else if( nx < 0 )
					{
						if(  Math.abs(nz) > .07 )
						{
							torp.rudder = (int)(1.0f / -nz);
							if( torp.rudder > 10 )
								torp.rudder = 10;
							else if( torp.rudder < -10 )
								torp.rudder = -10;
							torp.rudderSetting = torp.rudder;
						}
					}else
					{
						if( nz < 0 )
						{
							torp.doubleTurn = true;
							torp.rudder = 1;
							torp.turnProgress = 0;
						}else if( nz > 0 )
						{
							torp.doubleTurn = true;
							torp.rudder = -1;
							torp.turnProgress = 0;
						}
						if(  Math.abs(nz) > .07 )
						{
							torp.rudderSetting = (int)(1.0f / nz);
							if( torp.rudderSetting > 10 )
								torp.rudderSetting = 10;
							else if( torp.rudderSetting < -10 )
								torp.rudderSetting = -10;
						}
					}
				}
			//}
			testCraft.tubeFiringMode.put(torp.tubeNum, -3);
			testCraft.tubeFiringDisplay.put(torp.tubeNum, 0);
		}
		
		if( testCraft != null )
		{
			for( String s : testCraft.crewNames )
			{
				Player pl = nc.getServer().getPlayer(s);
				if( pl != null )
				{
					if( torp.tubeNum == 0 )
						pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Tube Fired!");
					else
						pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Tube " + torp.tubeNum + " Fired!");
				}
			}
		}
    	
    	
    	
		new Thread(){
			
    	@Override
			public void run() {
    		
    		setPriority(Thread.MIN_PRIORITY);
				//taskNum = -1;
    			try{
    				sleep(delayShoot);
    				
    				
    				
    				
    				for( int i=0; i<300; i++ )
    				{
						fireTorpedoUpdateMk3(p, torp, i, testCraft, left);
						sleep(160);
					} 
				}catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
    	}.start(); //, 20L);
    }
    
    public void fireTorpedoUpdateMk3(final Player p, final Weapon torp, final int i, final Craft firingCraft, final boolean left) {
    	nc.getServer().getScheduler().scheduleSyncDelayedTask(nc, new Runnable(){
    	//new Thread() {
	  //  @Override
	    @SuppressWarnings({ "deprecation", "unused" })
		public void run()
	    {

	    	if( !torp.dead )
	    	{
		    	if( torp.warhead.getTypeId() == 35 && torp.warhead.getRelative(torp.hdg, -1).getTypeId() == 35 && torp.warhead.getRelative(torp.hdg, -2).getTypeId() == 35 && torp.warhead.getRelative(torp.hdg, -3).getTypeId() == 35 )
		    	{
		    		firingCraft.world.playSound(torp.warhead.getLocation(), Sound.ENTITY_PLAYER_BREATH, 2.0f, 0.8f);
					if( i > 15 )
					{
						if( torp.warhead.getY() > 62 )
			    		{
			    			torp.warhead.setType(Material.AIR);
			    			torp.warhead.getRelative(torp.hdg, -1).setType(Material.AIR);
			    			torp.warhead.getRelative(torp.hdg, -2).setType(Material.AIR);
			    			torp.warhead.getRelative(torp.hdg, -3).setType(Material.AIR);
			    		}else
			    		{
			    			torp.warhead.setType(Material.WATER);
			    			torp.warhead.getRelative(torp.hdg, -1).setType(Material.WATER);
			    			torp.warhead.getRelative(torp.hdg, -2).setType(Material.WATER);
			    			torp.warhead.getRelative(torp.hdg, -3).setType(Material.WATER);
			    		}
						
						//check sub update
						if( firingCraft.tubeFiringMode.containsKey(torp.tubeNum) )
						{
							if( firingCraft.tubeFiringDepth.get(torp.tubeNum) != torp.setDepth )
							{
								torp.setDepth = firingCraft.tubeFiringDepth.get(torp.tubeNum);
							}
							if( firingCraft.tubeFiringHeading.get(torp.tubeNum) != torp.torpSetHeading )
							{
								torp.torpSetHeading = firingCraft.tubeFiringHeading.get(torp.tubeNum);
							}
							if( firingCraft.tubeFiringArmed.get(torp.tubeNum) != torp.active )
							{
								torp.active = firingCraft.tubeFiringArmed.get(torp.tubeNum);
							}
							if( firingCraft.tubeFiringAuto.get(torp.tubeNum) != torp.auto )
							{
								torp.auto = firingCraft.tubeFiringAuto.get(torp.tubeNum);
							}
							
							if( !firingCraft.tubeFiringArmed.get(torp.tubeNum) && (i == firingCraft.tubeFiringArm.get(torp.tubeNum) || (i==11 && firingCraft.tubeFiringArm.get(torp.tubeNum)==10)) )
							{
								firingCraft.tubeFiringArmed.put(torp.tubeNum,true);
								torp.active = true;
							}
						}
						
						
						//new position
						torp.warhead = torp.warhead.getRelative(torp.hdg);
						int depthDifference = torp.setDepth - (63 - torp.warhead.getY());
						if( depthDifference > 0 )
						{
							torp.warhead = torp.warhead.getRelative(BlockFace.DOWN);
						}else if( depthDifference < 0)
						{
							torp.warhead = torp.warhead.getRelative(BlockFace.UP);
						}
						
						if( torp.turnProgress > -1 )
						{
							
							if( torp.turnProgress == 10 )
							{
								if( torp.hdg == BlockFace.NORTH )
								{
									if( torp.rudder < 0 )
										torp.hdg = BlockFace.WEST;
									else
										torp.hdg = BlockFace.EAST;
								}else if( torp.hdg == BlockFace.SOUTH )
								{
									if( torp.rudder < 0 )
										torp.hdg = BlockFace.EAST;
									else
										torp.hdg = BlockFace.WEST;
								}else if( torp.hdg == BlockFace.EAST )
								{
									if( torp.rudder < 0 )
										torp.hdg = BlockFace.NORTH;
									else
										torp.hdg = BlockFace.SOUTH;
								}else
								{
									if( torp.rudder < 0 )
										torp.hdg = BlockFace.SOUTH;
									else
										torp.hdg = BlockFace.NORTH;
								}
								torp.rudder = -torp.rudder;
							}
							
							if( torp.turnProgress == 20 )
							{
								if( torp.doubleTurn )
								{
									torp.turnProgress = 0;
									torp.rudder = -torp.rudder;
									torp.doubleTurn = false;
								}else
								{
									torp.turnProgress = -1;
									torp.rudder = torp.rudderSetting;
								}
							}else
								torp.turnProgress += 1;
						}
						
						if( torp.rudder != 0 )
						{
							int dirMod  = Math.abs(torp.rudder);
							if( i % dirMod == 0 )
							{
								if( torp.rudder < 0 )
								{
									torp.warhead = getDirectionFromRelative(torp.warhead, torp.hdg, true);
								}else
								{
									torp.warhead = getDirectionFromRelative(torp.warhead, torp.hdg, false);
								}
							}
						}
						
						
						
						//check new position
						if( torp.warhead.getType() == Material.WATER || torp.warhead.getType() == Material.AIR || torp.warhead.getType() == Material.STATIONARY_WATER )
						{
		    				if( i == 299 )
							{
								if( torp.warhead.getY() > 62 )
								{
									torp.warhead.setType(Material.AIR);
									torp.warhead.getRelative(torp.hdg).setType(Material.AIR);
									torp.warhead.getRelative(torp.hdg, -2).setType(Material.AIR);
									torp.warhead.getRelative(torp.hdg, -3).setType(Material.AIR);
								}else
								{
									torp.warhead.setType(Material.WATER);
									torp.warhead.getRelative(torp.hdg, -1).setType(Material.WATER);
									torp.warhead.getRelative(torp.hdg, -2).setType(Material.WATER);
									torp.warhead.getRelative(torp.hdg, -3).setType(Material.WATER);
								}
								p.sendMessage("Torpedo expired.");
								firingCraft.tubeFiringMode.put(torp.tubeNum, -2);
								firingCraft.tubeFiringDepth.put(torp.tubeNum, 1);
								firingCraft.tubeFiringArm.put(torp.tubeNum, 20);
								firingCraft.tubeFiringArmed.put(torp.tubeNum, false);
								firingCraft.tubeFiringHeading.put(torp.tubeNum, firingCraft.rotation);
								firingCraft.tubeFiringAuto.put(torp.tubeNum, true);
								firingCraft.tubeFiringDisplay.put(torp.tubeNum, 0);
								return;
							}
		    				
		    				torp.warhead.setTypeIdAndData(35, (byte) 0x3, false);
		    				torp.warhead.getRelative(torp.hdg, -1).setTypeIdAndData(35, (byte) 0xD, false);
		    				torp.warhead.getRelative(torp.hdg, -2).setTypeIdAndData(35, (byte) 0xD, false);
		    				torp.warhead.getRelative(torp.hdg, -3).setTypeIdAndData(35, (byte) 0x7, false);
						}else if( torp.active ) ///detonate!
						{
							if( checkProtectedRegion(p, torp.warhead.getLocation()) )
							{
								p.sendMessage(ChatColor.RED + "No torpedo explosions in dock area.");
								torp.dead=true;
								if( firingCraft != null )
								{
									firingCraft.waitTorpLoading--;
									if( left )
										leftLoading = false;
									else
										rightLoading = false;
									
									if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
										openTorpedoDoors(p, false, false);
								}
								return;
							}
							
							torp.warhead = torp.warhead.getRelative(torp.hdg,-1);
							NavyCraft.explosion(14,  torp.warhead, false);
							torp.dead=true;
							
							
							Craft checkCraft=null;
							checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(torp.hdg,2).getLocation(), p);
							if( checkCraft == null ) {
								checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(7,7,7).getLocation(), p);
								if( checkCraft == null ) {
									checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(-7,-7,-7).getLocation(), p);
									if( checkCraft == null ) {
										checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(3,-2,-3).getLocation(), p);
										if( checkCraft == null ) {
											checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(-3,2,3).getLocation(), p);
										}
									}
								}
							}
							
							if( checkCraft == null )
								p.sendMessage("Torpedo hit unknown object!");
							else
								p.sendMessage("Torpedo hit " + checkCraft.name + "!");
							
							firingCraft.tubeFiringMode.put(torp.tubeNum, -2);
							firingCraft.tubeFiringDepth.put(torp.tubeNum, 1);
							firingCraft.tubeFiringArm.put(torp.tubeNum, 20);
							firingCraft.tubeFiringArmed.put(torp.tubeNum, false);
							firingCraft.tubeFiringHeading.put(torp.tubeNum, firingCraft.rotation);
							firingCraft.tubeFiringAuto.put(torp.tubeNum, true);
							firingCraft.tubeFiringDisplay.put(torp.tubeNum, 0);
							//CraftMover cm = new CraftMover( firingCraft, plugin);
							//cm.structureUpdate(null);
						}else
						{
							p.sendMessage("Torpedo Dud (Inactive).");
							firingCraft.tubeFiringMode.put(torp.tubeNum, -2);
							firingCraft.tubeFiringDepth.put(torp.tubeNum, 1);
							firingCraft.tubeFiringArm.put(torp.tubeNum, 20);
							firingCraft.tubeFiringArmed.put(torp.tubeNum, false);
							firingCraft.tubeFiringHeading.put(torp.tubeNum, firingCraft.rotation);
							firingCraft.tubeFiringAuto.put(torp.tubeNum, true);
							firingCraft.tubeFiringDisplay.put(torp.tubeNum, 0);
							//CraftMover cm = new CraftMover( firingCraft, plugin);
							//cm.structureUpdate(null);
						}
						
						
					}
					else/// i <= 15
					{
						if( torp.warhead.getY() > 62 || i < 5 )
						{
							torp.warhead.setType(Material.AIR);
							torp.warhead.getRelative(torp.hdg, -1).setType(Material.AIR);
							torp.warhead.getRelative(torp.hdg, -2).setType(Material.AIR);
							torp.warhead.getRelative(torp.hdg, -3).setType(Material.AIR);
							
							if( i == 4  )
							{
								if( firingCraft != null )
								{
									firingCraft.addBlock(torp.warhead, true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -1), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -2), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -3), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -4), true);
	
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -5), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -6), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -7), true);
									firingCraft.addBlock(torp.warhead.getRelative(torp.hdg, -8), true);
								}
							}
						}else
						{
							torp.warhead.setType(Material.WATER);
							torp.warhead.getRelative(torp.hdg, -1).setType(Material.WATER);
							torp.warhead.getRelative(torp.hdg, -2).setType(Material.WATER);
							torp.warhead.getRelative(torp.hdg, -3).setType(Material.WATER);
						}
						
						//Move torp
						torp.warhead = torp.warhead.getRelative(torp.hdg);
						
						if( torp.warhead.getType() == Material.WATER || torp.warhead.getType() == Material.AIR || torp.warhead.getType() == Material.STATIONARY_WATER )
						{
							torp.warhead.setTypeIdAndData(35, (byte) 0x3, false);
			    			torp.warhead.getRelative(torp.hdg, -1).setTypeIdAndData(35, (byte) 0xD, false);
			    			torp.warhead.getRelative(torp.hdg, -2).setTypeIdAndData(35, (byte) 0xD, false);
			    			torp.warhead.getRelative(torp.hdg, -3).setTypeIdAndData(35, (byte) 0x7, false);
						}else
						{
							p.sendMessage("Dud Torpedo! Too close...");
							torp.dead=true;
							firingCraft.tubeFiringMode.put(torp.tubeNum, -2);
							firingCraft.tubeFiringDepth.put(torp.tubeNum, 1);
							firingCraft.tubeFiringArm.put(torp.tubeNum, 20);
							firingCraft.tubeFiringArmed.put(torp.tubeNum, false);
							firingCraft.tubeFiringHeading.put(torp.tubeNum, firingCraft.rotation);
							firingCraft.tubeFiringAuto.put(torp.tubeNum, true);
							firingCraft.tubeFiringDisplay.put(torp.tubeNum, 0);
							if( firingCraft != null )
							{
								firingCraft.waitTorpLoading--;
								if( left )
									leftLoading = false;
								else
									rightLoading = false;
								
								if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
									openTorpedoDoors(p, false, false);
							}
						}
					}
					
					
		    		
		    	}else
		    	{
		    		if( checkProtectedRegion(p, torp.warhead.getLocation()) )
					{
						p.sendMessage(ChatColor.RED + "No torpedo explosions in dock area.");
						return;
					}
					
		    		if( !torp.active )
		    		{
		    			p.sendMessage("Dud Torpedo! Too close...");
						torp.dead = true;
						if( firingCraft != null )
						{
							firingCraft.waitTorpLoading--;
							if( left )
								leftLoading = false;
							else
								rightLoading = false;
							
							if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
								openTorpedoDoors(p, false, false);
						}else
						{
							if( left )
								leftLoading = false;
							else
								rightLoading = false;
							
							if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
								openTorpedoDoors(p, false, false);
						}
						return;
		    		}
					
					torp.warhead = torp.warhead.getRelative(torp.hdg,-1);
					NavyCraft.explosion(14,  torp.warhead, false);
					torp.dead = true;
					
					Craft checkCraft=null;
					checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(torp.hdg,2).getLocation(), p);
					if( checkCraft == null ) {
						checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(7,7,7).getLocation(), p);
						if( checkCraft == null ) {
							checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(-7,-7,-7).getLocation(), p);
							if( checkCraft == null ) {
								checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(3,-2,-3).getLocation(), p);
								if( checkCraft == null ) {
									checkCraft = NavyCraft.instance.entityListener.structureUpdate(torp.warhead.getRelative(-3,2,3).getLocation(), p);
								}
							}
						}
					}
					
					if( checkCraft == null )
						p.sendMessage("Torpedo detonated prematurely!");
					else
						p.sendMessage("Torpedo hit " + checkCraft.name + "!");
					
					
					if( firingCraft != null )
					{
						firingCraft.waitTorpLoading--;
						if( left )
							leftLoading = false;
						else
							rightLoading = false;
						
						if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
							openTorpedoDoors(p, false, false);
					}else
					{
						if( left )
							leftLoading = false;
						else
							rightLoading = false;
						
						if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
							openTorpedoDoors(p, false, false);
					}
		    		if( firingCraft.tubeFiringMode.get(torp.tubeNum) == -3 )
		    		{
			    		firingCraft.tubeFiringMode.put(torp.tubeNum, -2);
						firingCraft.tubeFiringDepth.put(torp.tubeNum, 1);
						firingCraft.tubeFiringArm.put(torp.tubeNum, 20);
						firingCraft.tubeFiringArmed.put(torp.tubeNum, false);
						firingCraft.tubeFiringHeading.put(torp.tubeNum, firingCraft.rotation);
						firingCraft.tubeFiringAuto.put(torp.tubeNum, true);
						firingCraft.tubeFiringDisplay.put(torp.tubeNum, 0);
						//CraftMover cm = new CraftMover( firingCraft, plugin);
						//cm.structureUpdate(null);
		    		}
		    	}
		    	
		    	if( i == 15 )
				{
					
					if( firingCraft != null )
					{
						firingCraft.waitTorpLoading--;
						if( left )
							leftLoading = false;
						else
							rightLoading = false;
						
						if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
							openTorpedoDoors(p, false, false);
					}else
					{
						if( left )
							leftLoading = false;
						else
							rightLoading = false;
						
						if( !leftLoading && !rightLoading && !checkOuterDoorClosed() )
							openTorpedoDoors(p, false, false);
					}
					torp.active = true;
				}
	
		    }
	    	}
    	}
    	
	);

    }
		
    
    public void fireLeft(Player p)
    {
    	Block b;
    	b = getDirectionFromRelative(loc.getBlock(), direction, true).getRelative(direction,4);
    	Craft testCraft = Craft.getCraft(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    	leftLoading = true;
		if( testCraft != null )
		{
			testCraft.waitTorpLoading++;
		}
		if( cannonType == 7 )
			fireTorpedoMk3(p, b, direction, depth, 0, true);
		else if( cannonType == 8 )
			//fireTorpedo2(p, b, direction, depth, 500, getTubeBlockFace(true), 0);
			fireTorpedoMk1(p, b, direction, depth, 0, true);
		else
			fireTorpedoMk2(p, b, direction, depth, 0, true);
    	//p.sendMessage("Tube 1 Fired!");
    }
    
    public void fireRight(Player p)
    {
    	Block b;
    	b = getDirectionFromRelative(loc.getBlock(), direction, false).getRelative(direction,4);
    	Craft testCraft = Craft.getCraft(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    	rightLoading = true;
		if( testCraft != null )
		{
			testCraft.waitTorpLoading++;
		}
		if( cannonType == 7 )
			fireTorpedoMk3(p, b, direction, depth, 0, false);
		else if( cannonType == 8 )
			fireTorpedoMk1(p, b, direction, depth, 0, false);
		else
			//fireTorpedo(p, b, direction, depth, 500, getTubeBlockFace(false), 0);
			fireTorpedoMk2(p, b, direction, depth, 0, false);
    	//p.sendMessage("Tube 2 Fired!");	
    }
    
    public void fireBoth(Player p)
    {
    	Block b;
    	Craft testCraft = Craft.getCraft(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    	rightLoading = true;
    	leftLoading = true;
		if( testCraft != null )
		{
			testCraft.waitTorpLoading+=2;
		}
    	
    	if( cannonType == 7 )
    	{
    		b = getDirectionFromRelative(loc.getBlock(), direction, true).getRelative(direction,4);
    		fireTorpedoMk3(p, b, direction, depth, 0, true);
    		b = getDirectionFromRelative(loc.getBlock(), direction, false).getRelative(direction,4);
    		fireTorpedoMk3(p, b, direction, depth, 2000, false);
    	}else if( cannonType == 8 )
    	{
    		b = getDirectionFromRelative(loc.getBlock(), direction, true).getRelative(direction,4);
    		fireTorpedoMk1(p, b, direction, depth, 0, true);
    		b = getDirectionFromRelative(loc.getBlock(), direction, false).getRelative(direction,4);
    		fireTorpedoMk1(p, b, direction, depth, 2000, false);
    	}else
    	{
    		b = getDirectionFromRelative(loc.getBlock(), direction, true).getRelative(direction,4);
    		fireTorpedoMk2(p, b, direction, depth, 0,true);
    		b = getDirectionFromRelative(loc.getBlock(), direction, false).getRelative(direction,4);
    		fireTorpedoMk2(p, b, direction, depth, 2000,false);
    	}
    	
    	
    	
    	//p.sendMessage("Tube 1 and 2 Fired!");	
    }
    
    public Block getDirectionFromRelative(Block blockIn, BlockFace dir, boolean left)
    {
    	Block b;
    	if( dir == BlockFace.NORTH )	
    	{
    		if( left )
    			b = blockIn.getRelative(BlockFace.WEST);
    		else
    			b = blockIn.getRelative(BlockFace.EAST);
    	}else if( dir == BlockFace.SOUTH )
    	{
    		if( left )
    			b = blockIn.getRelative(BlockFace.EAST);
    		else
    			b = blockIn.getRelative(BlockFace.WEST);
    	}else if( dir == BlockFace.EAST )
    	{
    		if( left )
    			b = blockIn.getRelative(BlockFace.NORTH);
    		else
    			b = blockIn.getRelative(BlockFace.SOUTH);
    	}else //if( direction == BlockFace.WEST )
    	{
    		if( left )
    			b = blockIn.getRelative(BlockFace.SOUTH);
    		else
    			b = blockIn.getRelative(BlockFace.NORTH);
    	}
    	return b;
    }
    
    public BlockFace getTubeBlockFace(boolean left)
    {
    	BlockFace bf;
    	if( direction == BlockFace.NORTH )	
    	{
    		if( left )
    			bf = BlockFace.WEST;
    		else
    			bf = BlockFace.EAST;
    	}else if( direction == BlockFace.SOUTH )
    	{
    		if( left )
    			bf = BlockFace.EAST;
    		else
    			bf = BlockFace.WEST;
    	}else if( direction == BlockFace.EAST )
    	{
    		if( left )
    			bf = BlockFace.NORTH;
    		else
    			bf = BlockFace.SOUTH;
    	}else //if( direction == BlockFace.WEST )
    	{
    		if( left )
    			bf = BlockFace.SOUTH;
    		else
    			bf = BlockFace.NORTH;
    	}
    	return bf;
    }
    
    public boolean checkInnerDoorClosed(boolean left)
    {
    	Block b;
    	b = getDirectionFromRelative(loc.getBlock(), direction, left);
    	if( b.getType() == Material.CLAY )
    		return true;
    	else
    		return false;
    }
    
    public boolean checkOuterDoorClosed()
    {
    	Block b;
    	b = loc.getBlock().getRelative(direction, 5);
    	if( b.getRelative(BlockFace.EAST).getType() == Material.CLAY && b.getRelative(BlockFace.WEST).getType() == Material.CLAY )
    	{
    		return true;
    	}else if( b.getRelative(BlockFace.NORTH).getType() == Material.CLAY && b.getRelative(BlockFace.SOUTH).getType() == Material.CLAY )
    	{
    		return true;
    	}else
    	{
    		return false;
    	}
    }
    
    @SuppressWarnings("deprecation")
	public boolean checkTubeLoaded(boolean left)
    {
    	Block b;
    	b = getDirectionFromRelative(loc.getBlock(), direction, left);
    	
    	if( b.getRelative(direction).getTypeId() == 35 )
    		if( b.getRelative(direction,2).getTypeId() == 35 )
    			if( b.getRelative(direction,3).getTypeId() == 35 )
    				if( b.getRelative(direction,4).getTypeId() == 35 )
    					return true;
    	
    	if( left )
    	{
    		if( leftLoading )
    			return true;
    		else
    			return false;
    	}else
    	{
    		if( rightLoading )
    			return true;
    		else
    			return false;
    	}
    	
    }
    
    
    public boolean isValidCannon(Block b) {
	direction = null;
	isCannon = false;

	if (b.getRelative(BlockFace.NORTH, 1).getType() == Material.DIAMOND_BLOCK )
	{
		direction = BlockFace.NORTH;
	    cannonType = 3;
	}else if(b.getRelative(BlockFace.SOUTH, 1).getType() == Material.DIAMOND_BLOCK)
	{
		direction = BlockFace.SOUTH;
	    cannonType = 3;
	}else if(b.getRelative(BlockFace.EAST, 1).getType() == Material.DIAMOND_BLOCK)
	{
		direction = BlockFace.EAST;
	    cannonType = 3;
	}else if(b.getRelative(BlockFace.WEST, 1).getType() == Material.DIAMOND_BLOCK)
	{
		direction = BlockFace.WEST;
	    cannonType = 3;
	}else if (b.getRelative(BlockFace.NORTH, 1).getType() == Material.EMERALD_BLOCK )
	{
		direction = BlockFace.NORTH;
	    cannonType = 7;
	}else if(b.getRelative(BlockFace.SOUTH, 1).getType() == Material.EMERALD_BLOCK)
	{
		direction = BlockFace.SOUTH;
	    cannonType = 7;
	}else if(b.getRelative(BlockFace.EAST, 1).getType() == Material.EMERALD_BLOCK)
	{
		direction = BlockFace.EAST;
	    cannonType = 7;
	}else if(b.getRelative(BlockFace.WEST, 1).getType() == Material.EMERALD_BLOCK)
	{
		direction = BlockFace.WEST;
	    cannonType = 7;
	}else if (b.getRelative(BlockFace.NORTH, 1).getType() == Material.LAPIS_BLOCK )
	{
		direction = BlockFace.NORTH;
	    cannonType = 8;
	}else if(b.getRelative(BlockFace.SOUTH, 1).getType() == Material.LAPIS_BLOCK)
	{
		direction = BlockFace.SOUTH;
	    cannonType = 8;
	}else if(b.getRelative(BlockFace.EAST, 1).getType() == Material.LAPIS_BLOCK)
	{
		direction = BlockFace.EAST;
	    cannonType = 8;
	}else if(b.getRelative(BlockFace.WEST, 1).getType() == Material.LAPIS_BLOCK)
	{
		direction = BlockFace.WEST;
	    cannonType = 8;
	}else
	{
	
		if (b.getRelative(BlockFace.NORTH, 1).getType() == Material.PUMPKIN
			&& b.getRelative(BlockFace.NORTH, 2).getType() == Material.PUMPKIN)
		{
		    direction = BlockFace.NORTH;
		    cannonType = 0;
		    if( ammunition == -1 )
		    {
		    	ammunition = 40;
		    	initAmmo = ammunition;
		    }
		    if (b.getRelative(BlockFace.NORTH, 3).getType() == Material.PUMPKIN)
		    	cannonLength = 3;
		    else
		    	cannonLength = 2;
		    
		    if( b.getRelative(BlockFace.DOWN).getType() == Material.GOLD_BLOCK )
		    {
		    	cannonType = 6;
		    	if( ammunition == -1 )
		    	{
		    		ammunition = 20;
		    		initAmmo = ammunition;
		    	}
		    }
		}
		if (b.getRelative(BlockFace.EAST, 1).getType() == Material.PUMPKIN
			&& b.getRelative(BlockFace.EAST, 2).getType() == Material.PUMPKIN)
		{
			direction = BlockFace.EAST;
			cannonType = 0;
			if( ammunition == -1 )
			{
				ammunition = 40;
				initAmmo = ammunition;
	    	}
		    if (b.getRelative(BlockFace.EAST, 3).getType() == Material.PUMPKIN)
		    	cannonLength = 3;
		    else
		    	cannonLength = 2;
		    if( b.getRelative(BlockFace.DOWN).getType() == Material.GOLD_BLOCK )
		    {
		    	cannonType = 6;
		    	if( ammunition == -1 )
		    	{
		    		ammunition = 20;
		    		initAmmo = ammunition;
		    	}
		    }
		}
		if (b.getRelative(BlockFace.SOUTH, 1).getType() == Material.PUMPKIN
			&& b.getRelative(BlockFace.SOUTH, 2).getType() == Material.PUMPKIN)
		{
			direction = BlockFace.SOUTH;
			cannonType = 0;
			if( ammunition == -1 )
			{
				ammunition = 40;
				initAmmo = ammunition;
	    	}
		    if (b.getRelative(BlockFace.SOUTH, 3).getType() == Material.PUMPKIN)
		    	cannonLength = 3;
		    else
		    	cannonLength = 2;
		    
		    if( b.getRelative(BlockFace.DOWN).getType() == Material.GOLD_BLOCK )
		    {
		    	if( ammunition == -1 )
		    	{
		    		ammunition = 20;
		    		initAmmo = ammunition;
		    	}
		    	cannonType = 6;
		    }
		}
		if (b.getRelative(BlockFace.WEST, 1).getType() == Material.PUMPKIN
			&& b.getRelative(BlockFace.WEST, 2).getType() == Material.PUMPKIN)
		{
			direction = BlockFace.WEST;
			cannonType = 0;
			if( ammunition == -1 )
			{
				ammunition = 40;
				initAmmo = ammunition;
	    	}
		    if (b.getRelative(BlockFace.WEST, 3).getType() == Material.PUMPKIN)
		    	cannonLength = 3;
		    else
		    	cannonLength = 2;
		    
		    if( b.getRelative(BlockFace.DOWN).getType() == Material.GOLD_BLOCK )
		    {
		    	cannonType = 6;
		    	if( ammunition == -1 )
		    	{
		    		ammunition = 20;
		    		initAmmo = ammunition;
		    	}
		    }
		}
		//check for cannon type 2 (fireball)
		if( direction != null && b.getRelative(BlockFace.DOWN, 1).getType() == Material.DIAMOND_BLOCK )
			cannonType = 2;
		
		
		
		///cannon type 1 (two barrel)
		if (b.getRelative(BlockFace.NORTH, 1).getType() == Material.GOLD_BLOCK
				&& b.getRelative(BlockFace.SOUTH, 1).getType() == Material.GOLD_BLOCK)
		{
			if (b.getRelative(BlockFace.NORTH_WEST, 1).getType() == Material.PUMPKIN
					&& b.getRelative(BlockFace.SOUTH_WEST, 1).getType() == Material.PUMPKIN)
				direction = BlockFace.WEST;
			else if(b.getRelative(BlockFace.NORTH_EAST, 1).getType() == Material.PUMPKIN
					&& b.getRelative(BlockFace.SOUTH_EAST, 1).getType() == Material.PUMPKIN)
				direction = BlockFace.EAST;
		    cannonType = 1;
		    cannonLength = 2;
		    if( ammunition == -1 )
		    {
		    	ammunition = 30;
		    	initAmmo = ammunition;
	    	}
		}
		if (b.getRelative(BlockFace.EAST, 1).getType() == Material.GOLD_BLOCK
			&& b.getRelative(BlockFace.WEST, 1).getType() == Material.GOLD_BLOCK)
		{
			if (b.getRelative(BlockFace.NORTH_WEST, 1).getType() == Material.PUMPKIN
					&& b.getRelative(BlockFace.NORTH_EAST, 1).getType() == Material.PUMPKIN)
				direction = BlockFace.NORTH;
			else if(b.getRelative(BlockFace.SOUTH_WEST, 1).getType() == Material.PUMPKIN
					&& b.getRelative(BlockFace.SOUTH_EAST, 1).getType() == Material.PUMPKIN)
				direction = BlockFace.SOUTH;
			cannonType = 1;
		    cannonLength = 2;
		    if( ammunition == -1 )
		    {
		    	ammunition = 30;
		    	initAmmo = ammunition;
	    	}
		}
		
		////check if depth charger
		if ( direction == null )
		{
			if (b.getRelative(BlockFace.NORTH, 1).getType() == Material.WOOD && b.getRelative(BlockFace.DOWN, 1).getType() == Material.LAPIS_BLOCK)
			{
				direction = BlockFace.EAST;
			    cannonType = 9;
			    if( ammunition == -1 )
			    {
			    	ammunition = 2;
			    	initAmmo = ammunition;
			    }
			}else if(b.getRelative(BlockFace.SOUTH, 1).getType() == Material.WOOD && b.getRelative(BlockFace.DOWN, 1).getType() == Material.LAPIS_BLOCK)
			{
				direction = BlockFace.WEST;
			    cannonType = 9;
			    if( ammunition == -1 )
			    {
			    	ammunition = 2;
			    	initAmmo = ammunition;
			    }
			}else if(b.getRelative(BlockFace.EAST, 1).getType() == Material.WOOD && b.getRelative(BlockFace.DOWN, 1).getType() == Material.LAPIS_BLOCK)
			{
				direction = BlockFace.SOUTH;
			    cannonType = 9;
			    if( ammunition == -1 )
			    {
			    	ammunition = 2;
			    	initAmmo = ammunition;
			    }
			}else if(b.getRelative(BlockFace.WEST, 1).getType() == Material.WOOD && b.getRelative(BlockFace.DOWN, 1).getType() == Material.LAPIS_BLOCK)
			{
				direction = BlockFace.NORTH;
			    cannonType = 9;
			    if( ammunition == -1 )
			    {
			    	ammunition = 2;
			    	initAmmo = ammunition;
			    }
			}else if (b.getRelative(BlockFace.NORTH, 1).getType() == Material.IRON_BLOCK && b.getRelative(BlockFace.DOWN, 1).getType() == Material.LAPIS_BLOCK)
			{
				direction = BlockFace.EAST;
			    cannonType = 4;
			    if( ammunition == -1 )
			    {
			    	ammunition = 10;
			    	initAmmo = ammunition;
			    }
			}else if(b.getRelative(BlockFace.SOUTH, 1).getType() == Material.IRON_BLOCK && b.getRelative(BlockFace.DOWN, 1).getType() == Material.LAPIS_BLOCK)
			{
				direction = BlockFace.WEST;
			    cannonType = 4;
			    if( ammunition == -1 )
			    {
			    	ammunition = 10;
			    	initAmmo = ammunition;
			    }
			}else if(b.getRelative(BlockFace.EAST, 1).getType() == Material.IRON_BLOCK && b.getRelative(BlockFace.DOWN, 1).getType() == Material.LAPIS_BLOCK)
			{
				direction = BlockFace.SOUTH;
			    cannonType = 4;
			    if( ammunition == -1 )
			    {
			    	ammunition = 10;
			    	initAmmo = ammunition;
			    }
			}else if(b.getRelative(BlockFace.WEST, 1).getType() == Material.IRON_BLOCK && b.getRelative(BlockFace.DOWN, 1).getType() == Material.LAPIS_BLOCK)
			{
				direction = BlockFace.NORTH;
			    cannonType = 4;
			    if( ammunition == -1 )
			    {
			    	ammunition = 10;
			    	initAmmo = ammunition;
			    }
			}else if (b.getRelative(BlockFace.NORTH, 1).getType() == Material.GOLD_BLOCK && b.getRelative(BlockFace.DOWN, 1).getType() == Material.LAPIS_BLOCK)
			{
				direction = BlockFace.EAST;
			    cannonType = 5;
			    if( ammunition == -1 )
			    {
			    	ammunition = 20;
			    	initAmmo = ammunition;
			    }
			}else if(b.getRelative(BlockFace.SOUTH, 1).getType() == Material.GOLD_BLOCK && b.getRelative(BlockFace.DOWN, 1).getType() == Material.LAPIS_BLOCK)
			{
				direction = BlockFace.WEST;
			    cannonType = 5;
			    if( ammunition == -1 )
			    {
			    	ammunition = 20;
			    	initAmmo = ammunition;
			    }
			}else if(b.getRelative(BlockFace.EAST, 1).getType() == Material.GOLD_BLOCK && b.getRelative(BlockFace.DOWN, 1).getType() == Material.LAPIS_BLOCK)
			{
				direction = BlockFace.SOUTH;
			    cannonType = 5;
			    if( ammunition == -1 )
			    {
			    	ammunition = 20;
			    	initAmmo = ammunition;
			    }
			}else if(b.getRelative(BlockFace.WEST, 1).getType() == Material.GOLD_BLOCK && b.getRelative(BlockFace.DOWN, 1).getType() == Material.LAPIS_BLOCK)
			{
				direction = BlockFace.NORTH;
			    cannonType = 5;
			    if( ammunition == -1 )
			    {
			    	ammunition = 20;
			    	initAmmo = ammunition;
			    }
			}
		}
	}

	
	if (direction != null) {
	    isCannon = true;
	    // System.out.print("It is a valid Cannon");
	    return true;
	} else {
	    // System.out.print("No Cannon");
	    return false;
	}
    }

    public int[][] rotateRight(int[][] arr) {
	int[][] result = new int[arr.length][arr.length];
	for (int x = 0; x < arr.length; x++) {
	    for (int y = 0; y < arr.length; y++) {
		result[x][y] = arr[arr.length - 1 - y][x];
	    }
	}
	return result;
    }

    public int[][] rotateLeft(int[][] arr) {
	int[][] result = new int[arr.length][arr.length];
	for (int x = 0; x < arr.length; x++) {
	    for (int y = 0; y < arr.length; y++) {
		result[x][y] = arr[y][arr.length - 1 - x];
	    }
	}
	return result;
    }

    public byte[][] rotateRightB(byte[][] arr, int[][] arro) {
	byte[][] result = new byte[arr.length][arr.length];
	for (int x = 0; x < arr.length; x++) {
	    for (int y = 0; y < arr.length; y++) {
		result[x][y] = arr[arr.length - 1 - y][x];
		if( arro[x][y] == 77 || arro[x][y] == 69 )
		{
			switch (result[x][y]) {
			case (byte) 0x3:
			    result[x][y] = (byte) 0x1;
			    break;
			case (byte) 0x4:
			    result[x][y] = (byte) 0x2;
			    break;
			case (byte) 0x2:
			    result[x][y] = (byte) 0x3;
			    break;
			case (byte) 0x1:
			    result[x][y] = (byte) 0x4;
			    break;
			case (byte) 0x9:
			    result[x][y] = (byte) 0x4;
			    break;
			case (byte) 0xA:
			    result[x][y] = (byte) 0x3;
			    break;
			case (byte) 0xB:
			    result[x][y] = (byte) 0x1;
			    break;
			case (byte) 0xC:
			    result[x][y] = (byte) 0x2;
			    break;
			}
		}else if( arro[x][y] == 86 )
		{
			switch (result[x][y]) {
			case (byte) 0x0:
			    result[x][y] = (byte) 0x3;
			    break;
			case (byte) 0x1:
			    result[x][y] = (byte) 0x0;
			    break;
			case (byte) 0x2:
			    result[x][y] = (byte) 0x1;
			    break;
			case (byte) 0x3:
			    result[x][y] = (byte) 0x2;
			    break;
			}
		}else
		{
			switch (result[x][y]) {
			case (byte) 0x3:
			    result[x][y] = (byte) 0x1;
			    break;
			case (byte) 0x4:
			    result[x][y] = (byte) 0x2;
			    break;
			case (byte) 0x2:
			    result[x][y] = (byte) 0x3;
			    break;
			case (byte) 0x1:
			    result[x][y] = (byte) 0x4;
			    break;
			case (byte) 0x9:
			    result[x][y] = (byte) 0x1;
			    break;
			case (byte) 0xA:
			    result[x][y] = (byte) 0x2;
			    break;
			case (byte) 0xB:
			    result[x][y] = (byte) 0x3;
			    break;
			case (byte) 0xC:
			    result[x][y] = (byte) 0x4;
			    break;
			}
		}
	    }
	}
	return result;
    }

    public byte[][] rotateLeftB(byte[][] arr, int[][] arro) {
	byte[][] result = new byte[arr.length][arr.length];
	for (int x = 0; x < arr.length; x++) {
	    for (int y = 0; y < arr.length; y++) {
		result[x][y] = arr[y][arr.length - 1 - x];
		if( arro[x][y] == 77 || arro[x][y] == 69 )
		{
			switch (result[x][y]) {
			case (byte) 0x3:
			    result[x][y] = (byte) 0x2;
			    break;
			case (byte) 0x4:
			    result[x][y] = (byte) 0x1;
			    break;
			case (byte) 0x2:
			    result[x][y] = (byte) 0x4;
			    break;
			case (byte) 0x1:
			    result[x][y] = (byte) 0x3;
			    break;
			case (byte) 0x9:
			    result[x][y] = (byte) 0x3;
			    break;
			case (byte) 0xA:
			    result[x][y] = (byte) 0x4;
			    break;
			case (byte) 0xB:
			    result[x][y] = (byte) 0x2;
			    break;
			case (byte) 0xC:
			    result[x][y] = (byte) 0x1;
			    break;
			}
		}else if( arro[x][y] == 86 )
		{
			switch (result[x][y]) {
			case (byte) 0x0:
			    result[x][y] = (byte) 0x1;
			    break;
			case (byte) 0x1:
			    result[x][y] = (byte) 0x2;
			    break;
			case (byte) 0x2:
			    result[x][y] = (byte) 0x3;
			    break;
			case (byte) 0x3:
			    result[x][y] = (byte) 0x0;
			    break;
			}
		}else
		{
			switch (result[x][y]) {
			case (byte) 0x1:
			    result[x][y] = (byte) 0x3;
			    break;
			case (byte) 0x2:
			    result[x][y] = (byte) 0x4;
			    break;
			case (byte) 0x3:
			    result[x][y] = (byte) 0x2;
			    break;
			case (byte) 0x4:
			    result[x][y] = (byte) 0x1;
			    break;
			case (byte) 0x9:
			    result[x][y] = (byte) 0x3;
			    break;
			case (byte) 0xA:
			    result[x][y] = (byte) 0x4;
			    break;
			case (byte) 0xB:
			    result[x][y] = (byte) 0x2;
			    break;
			case (byte) 0xC:
			    result[x][y] = (byte) 0x1;
			    break;
			}
		}
	    }
	}
	return result;
    }

    // 0x1: Facing south
    // 0x2: Facing north
    // 0x3: Facing west
    // 0x4: Facing east
    public void turnCannon(Boolean right, Player p)
    {
    	if( cannonType == 6 )
    	{
    		turnCannonLayer(right, p, -1);
    		turnCannonLayer(right, p, 1);
    		turnCannonLayer(right, p, 2);
    	}
    	turnCannonLayer(right, p, 0);
    }
    
    
    @SuppressWarnings("deprecation")
	public void turnCannonLayer(Boolean right, Player p, int offsetY) {
		// Get data
		int[][] arr = new int[7][7];
		byte[][] arrb = new byte[7][7];
		for (int x = 0; x < 7; x++) {
		    for (int z = 0; z < 7; z++) {
			arr[x][z] = loc.getBlock().getRelative(x - 3, 0, z - 3).getRelative(BlockFace.UP, offsetY).getTypeId();
			arrb[x][z] = loc.getBlock().getRelative(x - 3, 0, z - 3).getRelative(BlockFace.UP, offsetY).getData();
		    }
		}
	
		int[][] arro = new int[7][7];
		byte[][] arrbo = new byte[7][7];
		// Rotate the shit
		if (right) {
		    arro = rotateLeft(arr);
		    arrbo = rotateLeftB(arrb,arro);
		} else {
		    arro = rotateRight(arr);
		    arrbo = rotateRightB(arrb,arro);
		}
	
		// Cleanup Cannon (button und lever first)
		for (int x = 0; x < 7; x++) {
		    for (int z = 0; z < 7; z++) {
			if (loc.getBlock().getRelative(x - 3, 0, z - 3).getRelative(BlockFace.UP, offsetY).getTypeId() == 69
				|| loc.getBlock().getRelative(x - 3, 0, z - 3).getRelative(BlockFace.UP, offsetY).getTypeId() == 77) {
			    loc.getBlock().getRelative(x - 3, 0, z - 3).getRelative(BlockFace.UP, offsetY).setTypeIdAndData(0, (byte) 0, false);
			}
		    }
		}
	
		// Cleanup Rest
		for (int x = 0; x < 7; x++) {
		    for (int z = 0; z < 7; z++) {
		    	if( !(x-3==0 && z-3==0 && offsetY==0) )
		    		loc.getBlock().getRelative(x - 3, 0, z - 3).getRelative(BlockFace.UP, offsetY).setTypeIdAndData(0, (byte) 0, false);
		    }
		}
		
		
		Craft testCraft = Craft.getCraft(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
		// Place cannon
		for (int x = 0; x < 7; x++) {
		    for (int z = 0; z < 7; z++) {
			if ((arro[x][z] != 69) && (arro[x][z] != 77) && (arro[x][z] != 23)) 
			{
			    loc.getBlock().getRelative(x - 3, 0, z - 3).getRelative(BlockFace.UP, offsetY).setTypeIdAndData(arro[x][z], arrbo[x][z], false);
			    if( testCraft != null )
			    {
			    	testCraft.addBlock(loc.getBlock().getRelative(x - 3, 0, z - 3).getRelative(BlockFace.UP, offsetY), true);
			    }
				
			}else if( (arro[x][z] == 23) )
			{
				loc.getBlock().getRelative(x - 3, 0, z - 3).getRelative(BlockFace.UP, offsetY).setData(arrbo[x][z]);
		    }
		    }
		}
	
		// Place rest
		for (int x = 0; x < 7; x++) {
		    for (int z = 0; z < 7; z++) {
			if ((arro[x][z] == 69) || (arro[x][z] == 77)) 
			{
			    loc.getBlock().getRelative(x - 3, 0, z - 3).getRelative(BlockFace.UP, offsetY).setTypeIdAndData(arro[x][z], arrbo[x][z], false);
			    if( testCraft != null )
			    {
			    	testCraft.addBlock(loc.getBlock().getRelative(x - 3, 0, z - 3).getRelative(BlockFace.UP, offsetY), true);
			    }
			}
		    }
		}
	
		if( testCraft != null )
		{
			//CraftMover cm = new CraftMover(testCraft, plugin);
			//cm.structureUpdate(null);
		}
		// 0x2: Facing east
		// 0x3: Facing west
		// 0x4: Facing north
		// 0x5: Facing south
		
		if( offsetY == 0 )
		{
			if (right) {
		    	if (direction == BlockFace.NORTH) 
		    	{
		    	    direction = BlockFace.EAST;
		    	    loc.getBlock().setData((byte) 0x3);
		    	}else if (direction == BlockFace.EAST) 
		    	{
		    	    direction = BlockFace.SOUTH;
		    	    loc.getBlock().setData((byte) 0x4);	    	 
		    	}else if (direction == BlockFace.SOUTH) 
		    	{
		    	    direction = BlockFace.WEST;
		    	    loc.getBlock().setData((byte) 0x2);
		    	}else// if (direction == BlockFace.WEST) 
		    	{
		    	    direction = BlockFace.NORTH;
		    	    loc.getBlock().setData((byte) 0x5);
		    	}
			} else
			{
			    if (direction == BlockFace.EAST)
			    {
			    	    direction = BlockFace.NORTH;
			    	    loc.getBlock().setData((byte) 0x5);
			    }else if (direction == BlockFace.SOUTH) 
			    {
			    	    direction = BlockFace.EAST;
			    	    loc.getBlock().setData((byte) 0x3);
			    }else if (direction == BlockFace.WEST) 
			    {
			    	    direction = BlockFace.SOUTH;
			    	    loc.getBlock().setData((byte) 0x4);
			    }else// if (direction == BlockFace.NORTH) 
			    {
			    	    direction = BlockFace.WEST;
			    	    loc.getBlock().setData((byte) 0x2);
			    }
			}
			
			Location teleLoc = new Location(p.getWorld(), loc.getBlock().getRelative(direction, -1).getX() + 0.5, (double)loc.getBlock().getRelative(direction, -1).getY(), loc.getBlock().getRelative(direction, -1).getZ() + 0.5);
			//p.sendMessage("player yaw=" + p.getLocation().getYaw() );
			if( right )
				teleLoc.setYaw(p.getLocation().getYaw() + 90);
			else
				teleLoc.setYaw(p.getLocation().getYaw() - 90);
			teleLoc.setPitch(p.getLocation().getPitch());
			p.teleport(teleLoc);
			
			
			if( cannonTurnCounter < 4 && ((loc.getBlock().getRelative(direction, 1).getRelative(BlockFace.DOWN,1).getTypeId() == 5)
					|| ( cannonType == 6 && loc.getBlock().getRelative(direction, 1).getRelative(BlockFace.DOWN,2).getTypeId() == 5)))
			{
				cannonTurnCounter++;
				turnCannon(right, p);
			}else
			{
				cannonTurnCounter=0;
			}
			
		}
    }
    
    
    public boolean checkProtectedRegion(Player player, Location loc)
    {
    	if( wgp != null )
    	{
    		if( !PermissionInterface.CheckEnabledWorld(loc) )
    		{
    			return true;
    		}
	    	RegionManager regionManager = wgp.getRegionManager(player.getWorld());
		
			ApplicableRegionSet set = regionManager.getApplicableRegions(loc);
			
			Iterator<ProtectedRegion> it = set.iterator();
			while( it.hasNext() )
			{
				String id = it.next().getId();
		
				String[] splits = id.split("_");
				if( splits.length == 2 )
				{
					if( splits[1].equalsIgnoreCase("safedock") || splits[1].equalsIgnoreCase("red") || splits[1].equalsIgnoreCase("blue") )
					{
						return true;
					}
				}
		    }
			return false;
		}
    	return false;
	}
    
    @SuppressWarnings("deprecation")
	public void fireDCButton(Player p, boolean leftClick)
    {
    	if( checkProtectedRegion(p, p.getLocation()) )
    	{
    		p.sendMessage("You are in a protected region");
    		return;
    	}
    	
		if( leftClick )
		{
			if( charged > 0 )
			{
				if( depth == 0 )
					depth = 10;
				if( cannonType == 4 )
				{
					
					loc.getBlock().getRelative(direction,4).setTypeIdAndData(35, (byte) 0x8, false);
					loc.getBlock().getRelative(direction,4).getRelative(BlockFace.DOWN).setTypeIdAndData(35, (byte) 0x8, false);
					fireDC(p, loc.getBlock().getRelative(direction,4), depth, loc.getBlockY(), 0, 2);
					p.sendMessage("Depth Charge Away!");
				}else if( cannonType == 9 )
				{
					
					loc.getBlock().getRelative(BlockFace.DOWN,5).setTypeIdAndData(35, (byte) 0x8, false);
					loc.getBlock().getRelative(BlockFace.DOWN,5).getRelative(BlockFace.DOWN).setTypeIdAndData(35, (byte) 0x7, false);
					fireDC(p, loc.getBlock().getRelative(BlockFace.DOWN,5), 0, loc.getBlock().getRelative(BlockFace.DOWN,5).getY(), 0, 2);
					p.sendMessage("Bomb Away!");
				}else
				{
					
					loc.getBlock().getRelative(direction,4).setTypeIdAndData(35, (byte) 0x8, false);
					loc.getBlock().getRelative(direction,4).getRelative(BlockFace.DOWN).setTypeIdAndData(35, (byte) 0x8, false);
					fireDC(p, loc.getBlock().getRelative(direction,4), depth, loc.getBlockY(), 0, 0);
					
					if( direction == BlockFace.NORTH || direction == BlockFace.SOUTH )
					{
						loc.getBlock().getRelative(direction,6).getRelative(BlockFace.EAST,10).setTypeIdAndData(35, (byte) 0x8, false);
						loc.getBlock().getRelative(direction,6).getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST,10).setTypeIdAndData(35, (byte) 0x8, false);
						fireDC(p, loc.getBlock().getRelative(direction,6).getRelative(BlockFace.EAST,10), depth, loc.getBlockY(), 1000, 1);
						loc.getBlock().getRelative(direction,6).getRelative(BlockFace.WEST,10).setTypeIdAndData(35, (byte) 0x8, false);
						loc.getBlock().getRelative(direction,6).getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST,10).setTypeIdAndData(35, (byte) 0x8, false);
						fireDC(p, loc.getBlock().getRelative(direction,6).getRelative(BlockFace.WEST,10), depth, loc.getBlockY(), 1000, -1);
					}else
					{
						loc.getBlock().getRelative(direction,6).getRelative(BlockFace.NORTH,10).setTypeIdAndData(35, (byte) 0x8, false);
						loc.getBlock().getRelative(direction,6).getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH,10).setTypeIdAndData(35, (byte) 0x8, false);
						fireDC(p, loc.getBlock().getRelative(direction,6).getRelative(BlockFace.NORTH,10), depth, loc.getBlockY(), 1000, 1);
						loc.getBlock().getRelative(direction,6).getRelative(BlockFace.SOUTH,10).setTypeIdAndData(35, (byte) 0x8, false);
						loc.getBlock().getRelative(direction,6).getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH,10).setTypeIdAndData(35, (byte) 0x8, false);
						fireDC(p, loc.getBlock().getRelative(direction,6).getRelative(BlockFace.SOUTH,10), depth, loc.getBlockY(), 1000, -1);
					}
					p.sendMessage("Depth Charges Away!");
				}
				
				charged = 0;
			}else
			{
				if( cannonType == 4 )
					p.sendMessage("Load Depth Charge Dropper first.");
				else if( cannonType == 5 )
					p.sendMessage("Load Depth Charge Launcher first.");
				else
					p.sendMessage("Load Bomb Dropper first.");
			}
		}else
		{
			depth += 10;
			if( depth > 40 )
				depth = 10;
			if( cannonType == 4 )
				p.sendMessage("Depth Charge Dropper set to " + depth + " meters.");
			else if( cannonType == 5 )
				p.sendMessage("Depth Charge Launcher set to " + depth + " meters.");
			else if( cannonType == 9 )
				p.sendMessage("Left click to drop bomb.");
		}
    }
    
    public void fireDC(final Player p, final Block b, final int dcDepth, final int startY, final int delayShoot, final int direction){
    	//final int taskNum;                                                      ////direction= -1 left, 0 center, 1 right, 2 single
    	//int taskNum = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
    	final Weapon dc = new Weapon(b, direction, dcDepth);
    	AimCannon.weapons.add(dc);
    	
    	new Thread(){
			
    	@Override
			public void run() {
    		
    		setPriority(Thread.MIN_PRIORITY);
				//taskNum = -1;
    			try{
    				sleep(delayShoot);
    				
    				int freeFallHt=0;
    				if( startY > 63 )
    					freeFallHt = startY - 63;
    				
    				for( int i=0; i <= dc.setDepth+freeFallHt; i++ )
    				{
						fireDCUpdate(p, dc, freeFallHt, i);
						if( i<freeFallHt )
							sleep(160);
						else
							sleep(250);
					} 
    				stopFall0 = false;
    				stopFall1 = false;
    				stopFallM1 = false;
    				stopFall2 = false;
				}catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
    	}.start(); //, 20L);
    }
    
    
    public void fireDCUpdate(final Player p, final Weapon dc, final int freeFallHt, final int i) {
    	nc.getServer().getScheduler().scheduleSyncDelayedTask(nc, new Runnable(){
    	//new Thread() {
	  //  @Override
	    @SuppressWarnings("deprecation")
		public void run()
	    {
	    		if ( stopFall1 && dc.dcDirection == 1 )
	    			return;
	    		if ( stopFallM1 && dc.dcDirection == -1 )
	    			return;
	    		if ( stopFall0 && dc.dcDirection == 0 )
	    			return;
	    		if ( stopFall2 && dc.dcDirection == 2 )
	    			return;
	    				
	 
				
				//currentBlock = warhead.getRelative(BlockFace.DOWN, i);
				
				if( dc.warhead.getTypeId() != 35 )
				{
					return;
				}

				if( i >= dc.setDepth+freeFallHt || ( dc.warhead.getRelative(BlockFace.DOWN,2).getTypeId() != 8 && dc.warhead.getRelative(BlockFace.DOWN,2).getTypeId() != 9 && dc.warhead.getRelative(BlockFace.DOWN,2).getTypeId() != 0 && dc.warhead.getRelative(BlockFace.DOWN,2).getTypeId() != 79) )
				{
					
					if( checkProtectedRegion(p, dc.warhead.getLocation()) )
					{
						p.sendMessage(ChatColor.RED + "No Depth Charge explosions in dock area.");
						if( dc.warhead.getY() >= 63 )
						{
							dc.warhead.setTypeId(0);
							dc.warhead.getRelative(BlockFace.DOWN).setTypeId(0);
						}
						else
						{
							dc.warhead.setTypeId(8);
							dc.warhead.getRelative(BlockFace.DOWN).setTypeId(8);
						}
			    		if ( dc.dcDirection == 1 )
			    			stopFall1 = true;
			    		if ( dc.dcDirection == -1 )
			    			stopFallM1 = true;
			    		if ( dc.dcDirection == 0 )
			    			stopFall0 = true;
			    		if ( dc.dcDirection == 2 )
			    			stopFall2 = true;
						return;
					}
					
					/*
					int fuseDelay = 5;
					for (int x = 0; x < 9; x++) 
					{
						for( int y = 0; y < 9; y++ )
						{
						    for (int z = 0; z < 9; z++) 
						    {
						    	double ranNum = Math.random();
						    	int xdist = Math.abs(x - 4);
						    	int ydist = Math.abs(y - 4);
						    	int zdist = Math.abs(z - 4);
						    	int dist = xdist+ydist+zdist;
						    	Block theBlock = currentBlock.getRelative(x - 4, y - 4, z - 4);
						    	int blockType = theBlock.getTypeId();
						    	
						    	double breakChance = 1;
						    	if( Craft.blockHardness(blockType) == 3 )
						    	{
						    		breakChance = 0;
						    		
						    	}else if( Craft.blockHardness(blockType) == 46 )
						    	{
						    		breakChance = -1;
						    		
						    	}else if( dist > 9 )
						    	{
						    		if( Craft.blockHardness(blockType) == 2 )
						    		{
						    			breakChance = .1;
						    		}else if( Craft.blockHardness(blockType) == 1 )
						    		{
						    			breakChance = .3;
						    		}
						    	}else if( dist > 6 )
						    	{
						    		if( Craft.blockHardness(blockType) == 2 )
						    		{
						    			breakChance = .3;
						    		}else if( Craft.blockHardness(blockType) == 1 )
						    		{
						    			breakChance = .6;
						    		}
						    	}else if( dist > 3 )
						    	{
						    		if( Craft.blockHardness(blockType) == 2 )
						    		{
						    			breakChance = .6;
						    		}else if( Craft.blockHardness(blockType) == 1 )
						    		{
						    			breakChance = .9;
						    		}
						    	}
						    	
						    	if( breakChance == -1 )
						    	{

						    		TNTPrimed tnt = (TNTPrimed)theBlock.getWorld().spawnEntity(new Location(theBlock.getWorld(), theBlock.getX(), theBlock.getY(), theBlock.getZ()), EntityType.PRIMED_TNT);
									tnt.setFuseTicks(fuseDelay);
									fuseDelay = fuseDelay + 2;
						    	}else if( ranNum < breakChance )
						    	{
						    		if( theBlock.getY() >= 63 )
						    			theBlock.setType(Material.AIR);
						    		else
						    			theBlock.setType(Material.WATER);
						    	}
						    }
						}
					}*/
					

				    
					//currentBlock.getWorld().createExplosion(currentBlock.getLocation(), 10);
					NavyCraft.explosion(10, dc.warhead, false);
					
					if( dc.warhead.getY() >= 63 )
					{
						dc.warhead.setTypeId(0);
						dc.warhead.getRelative(BlockFace.DOWN).setTypeId(0);
					}
					else
					{
						dc.warhead.setTypeId(8);
						dc.warhead.getRelative(BlockFace.DOWN).setTypeId(8);
					}

					Craft checkCraft=null;
					checkCraft = NavyCraft.instance.entityListener.structureUpdate(dc.warhead.getLocation(), p);
					if( checkCraft == null ) {
						checkCraft = NavyCraft.instance.entityListener.structureUpdate(dc.warhead.getRelative(4,4,4).getLocation(), p);
						if( checkCraft == null ) {
							checkCraft = NavyCraft.instance.entityListener.structureUpdate(dc.warhead.getRelative(-4,-4,-4).getLocation(), p);
							if( checkCraft == null ) {
								checkCraft = NavyCraft.instance.entityListener.structureUpdate(dc.warhead.getRelative(2,-2,-2).getLocation(), p);
								if( checkCraft == null ) {
									checkCraft = NavyCraft.instance.entityListener.structureUpdate(dc.warhead.getRelative(-2,2,2).getLocation(), p);
								}
							}
						}
					}
					
					if( checkCraft == null )
						p.sendMessage("Depth Charge hit unknown object!");
					else
						p.sendMessage("Depth Charge hit " + checkCraft.name + "!");
					
							

				}else
				{

					if( dc.warhead.getY() >= 63 )
						dc.warhead.setTypeId(0);
					else
						dc.warhead.setTypeId(8);
					if( dc.setDepth == 0 ) //if bomb?
					{
						dc.warhead.getRelative(BlockFace.DOWN).setTypeIdAndData(35, (byte) 0x7, false);
						dc.warhead.getRelative(BlockFace.DOWN,2).setTypeIdAndData(35, (byte) 0x7, false);
					}else
					{
						dc.warhead.getRelative(BlockFace.DOWN).setTypeIdAndData(35, (byte) 0x8, false);
						dc.warhead.getRelative(BlockFace.DOWN,2).setTypeIdAndData(35, (byte) 0x8, false);
					}
					dc.warhead = dc.warhead.getRelative(BlockFace.DOWN,1);
						
				}


	    }
    	}
    	
	);
    }
    
    public void reload(Player p)
    {
    	ammunition = initAmmo;
    	p.sendMessage(ChatColor.GREEN + "Weapon Systems Reloaded!");
    }
    
    public void shellThread(final int num)
    {
    	//final int taskNum;                                                      ////direction= -1 left, 0 center, 1 right, 2 single
    	//int taskNum = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
		new Thread(){
			
    	@Override
			public void run() {
    		
    		setPriority(Thread.MIN_PRIORITY);
				//taskNum = -1;
    			try{
    				//sleep(200);
    				int i = 0;
    				while((num == 1 && !tntp.isDead())||(num == 2 && !tntp2.isDead())||(num == 3 && !tntp3.isDead()))
    				{
    					sleep(100);
    					shellUpdate(num,i);
    					i++;
    				}

				}catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
    	}.start(); //, 20L);
    }
    
    public void shellUpdate(final int num, final int i) {
    	nc.getServer().getScheduler().scheduleSyncDelayedTask(nc, new Runnable(){
    	//new Thread() {
	  //  @Override
	    public void run()
	    {
	    	if( i == 0 )
	    	{
	    		if(num == 1)
		    	{
		    		tnt1X = tntp.getVelocity().getX();
			    	tnt1Z = tntp.getVelocity().getZ();
		    	}else if(num == 2)
		    	{
		    		tnt2X = tntp2.getVelocity().getX();
		    		tnt2Z = tntp2.getVelocity().getZ();
		    	}else if(num == 3)
		    	{
			    	tnt3X = tntp3.getVelocity().getX();
			    	tnt3Z = tntp3.getVelocity().getZ();
		    	}
	    	}else
	    	{
		    	if(num == 1 && !tntp.isDead())
		    	{
		    		if( Math.signum(tnt1X) != Math.signum(tntp.getVelocity().getX()) || Math.signum(tnt1Z) != Math.signum(tntp.getVelocity().getZ()) || Math.abs(tntp.getVelocity().getX()) < Math.abs(tnt1X/4) || Math.abs(tntp.getVelocity().getZ()) < Math.abs(tnt1Z/4) )
		    		{
		    			System.out.println("tnt1X=" + tnt1X + " tnt1Z=" + tnt1Z);
		    			System.out.println("tntp.getVelocity().getX()=" + tntp.getVelocity().getX() + " tntp.getVelocity().getZ()=" + tntp.getVelocity().getZ());
		    			tntp.setFuseTicks(1);
		    			
		    		}else
		    		{
		    			tntp.setFuseTicks(1000);
		    		}
		    	}else if(num == 2 && !tntp2.isDead())
		    	{
		    		if( tnt2X < tntp2.getVelocity().getX() - 0.5 || tnt2X > tntp2.getVelocity().getX() + 0.5 || tnt2Z < tntp2.getVelocity().getZ() - 0.5 || tnt2Z > tntp2.getVelocity().getZ() + 0.5 )
		    		{
		    			tntp2.setFuseTicks(1);
		    		}else
		    		{
		    			tntp2.setFuseTicks(1000);
		    		}
		    	}else if(num == 3 && !tntp3.isDead())
		    	{
		    		if( tnt3X < tntp3.getVelocity().getX() - 0.5 || tnt3X > tntp3.getVelocity().getX() + 0.5 || tnt3Z < tntp3.getVelocity().getZ() - 0.5 || tnt3Z > tntp3.getVelocity().getZ() + 0.5 )
		    		{
		    			tntp3.setFuseTicks(1);
		    		}else
		    		{
		    			tntp3.setFuseTicks(1000);
		    		}
		    	}
	    	}

	    }
    	}
    	
	);
    }
}
