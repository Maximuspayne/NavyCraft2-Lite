package com.maximuspayne.navycraft;


import org.bukkit.Location;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.Plugin;

import com.maximuspayne.navycraft.plugins.PermissionInterface;

public class MoveCraft_EntityListener implements Listener {
    private static Plugin plugin;


    public MoveCraft_EntityListener(Plugin p) {
    	plugin = p;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) 
    {
    	Entity ent = event.getEntity();
    	if( (ent != null && ent instanceof TNTPrimed) )
    	{
    		if( event.getLocation() != null )
    		{
    			if( NavyCraft.shotTNTList.containsKey(ent.getUniqueId()) )
    			{
    				Craft checkCraft;
    				checkCraft = structureUpdate(event.getLocation(), NavyCraft.shotTNTList.get(ent.getUniqueId()));
    				if( checkCraft == null ) {
    					checkCraft = structureUpdate(event.getLocation().getBlock().getRelative(4,4,4).getLocation(), NavyCraft.shotTNTList.get(ent.getUniqueId()));
    					if( checkCraft == null ) {
    						checkCraft = structureUpdate(event.getLocation().getBlock().getRelative(-4,-4,-4).getLocation(), NavyCraft.shotTNTList.get(ent.getUniqueId()));
    						if( checkCraft == null ) {
    							checkCraft = structureUpdate(event.getLocation().getBlock().getRelative(2,-2,-2).getLocation(), NavyCraft.shotTNTList.get(ent.getUniqueId()));
    							if( checkCraft == null ) {
    								checkCraft = structureUpdate(event.getLocation().getBlock().getRelative(-2,2,2).getLocation(), NavyCraft.shotTNTList.get(ent.getUniqueId()));
    							}
    						}
    					}
    				}
    				NavyCraft.shotTNTList.remove(ent.getUniqueId());
    			}
    			else
    				structureUpdate(event.getLocation(), null);
    		}
    	}
 
    }
    
    
    public Craft structureUpdate(Location loc, Player causer)
    {
    	Craft testcraft = Craft.getCraft(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		if( testcraft != null )
		{
			CraftMover cm = new CraftMover(testcraft, plugin);
			cm.structureUpdate(causer,false);
			return testcraft;
		}
		return null;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {

    	if( PermissionInterface.CheckEnabledWorld(event.getEntity().getLocation()) )
    	{

    	}
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent event)
    {
    	if( event.getEntity() instanceof Skeleton && NavyCraft.aaSkelesList.contains((Skeleton)event.getEntity()) )
    	{
    		if( event.getTarget() instanceof Player )
    		{
    			Player target = (Player)event.getTarget();
    			Craft skeleCraft = Craft.getCraft(target.getLocation().getBlockX(), target.getLocation().getBlockY(), target.getLocation().getBlockZ());
    			if( skeleCraft != null && !skeleCraft.crewNames.isEmpty() && skeleCraft.crewNames.contains(target.getName()) )
    			{
    				event.setCancelled(true);
    			}
    		}
    	}
    }
    
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event)
    {
    	
    	//cancel if on periscope
    	if( event.getEntity() instanceof Player )
    	{
    		Player player = (Player)event.getEntity();


    		for( Periscope p: NavyCraft.allPeriscopes )
    		{
    			if( p.user == player )
    			{
    				event.setCancelled(true);
        			return;
    			}
    		}
    		
    		if( event.getCause() == DamageCause.SUFFOCATION )
    		{
    			Craft c = Craft.getCraft(player.getLocation().getBlockX(),player.getLocation().getBlockY(), player.getLocation().getBlockZ());
    			if( c != null )
    			{
    				event.setCancelled(true);
        			return;
    			}
    				
    		}
    		
    	}
    	
    	if ( event instanceof EntityDamageByEntityEvent ) 
    	{


    		Entity attacker = ((EntityDamageByEntityEvent) event).getDamager();
    		if( attacker instanceof Egg )
    		{
    			if( NavyCraft.explosiveEggsList.contains((Egg)attacker) )
    			{	
    				event.setDamage(5);
    			}
    		}
    	}

    }
    
}
