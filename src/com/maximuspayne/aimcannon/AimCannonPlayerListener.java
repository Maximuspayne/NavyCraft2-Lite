package com.maximuspayne.aimcannon;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.maximuspayne.navycraft.NavyCraft;

public class AimCannonPlayerListener implements Listener {
    public static AimCannon plugin;

    public static void onPlayerInteract(PlayerInteractEvent event) {
    	
    ///////stone button
	if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK ) {
	    if (event.getClickedBlock().getType() == Material.STONE_BUTTON) {
		// System.out.print("Button clicked");
		Block b = null;
		if (event.getClickedBlock().getRelative(BlockFace.NORTH_EAST).getType() == Material.DISPENSER) {
		    b = event.getClickedBlock().getRelative(BlockFace.NORTH_EAST);
		}
		if (event.getClickedBlock().getRelative(BlockFace.NORTH_WEST).getType() == Material.DISPENSER) {
		    b = event.getClickedBlock().getRelative(BlockFace.NORTH_WEST);
		}
		if (event.getClickedBlock().getRelative(BlockFace.SOUTH_EAST).getType() == Material.DISPENSER) {
		    b = event.getClickedBlock().getRelative(BlockFace.SOUTH_EAST);
		}
		if (event.getClickedBlock().getRelative(BlockFace.SOUTH_WEST).getType() == Material.DISPENSER) {
		    b = event.getClickedBlock().getRelative(BlockFace.SOUTH_WEST);
		}
		
		if (b != null)
		{
		    for (OneCannon onec : AimCannon.getCannons()) 
		    {
				if (onec.isThisCannon(b.getLocation(), false)) 
				{
					if( onec.cannonType == 2 )
					{
						if( event.getAction() == Action.LEFT_CLICK_BLOCK )
							onec.fireCannonButton(event.getPlayer(), true);
						else
							onec.fireCannonButton(event.getPlayer(), false);
					}else if( onec.cannonType == 4 || onec.cannonType == 5 || onec.cannonType == 9)
					{
						if( event.getAction() == Action.LEFT_CLICK_BLOCK )
							onec.fireDCButton(event.getPlayer(), true);
						else
							onec.fireDCButton(event.getPlayer(), false);
					}
					else if(event.getAction() == Action.LEFT_CLICK_BLOCK)
					{
					    if (onec.isCharged() ) {
						onec.Action(event.getPlayer());
					    } else {
						// System.out.print("Load Cannon first");
						event.getPlayer().sendMessage("Load Cannon first.. (left click Dispenser)");
					    }
					}else
					{
						onec.setDelay(event.getPlayer());
					}
				}
			}
		   ////else not gun, maybe torpedo?
		}else
		{
			if( event.getClickedBlock().getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN).getType() == Material.DISPENSER ) 
			{
				b = event.getClickedBlock().getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN);
			}else if( event.getClickedBlock().getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN).getType() == Material.DISPENSER ) 
			{
				b = event.getClickedBlock().getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN);
			}else if( event.getClickedBlock().getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN).getType() == Material.DISPENSER ) 
			{
				b = event.getClickedBlock().getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN);
			}else if( event.getClickedBlock().getRelative(BlockFace.WEST).getRelative(BlockFace.DOWN).getType() == Material.DISPENSER ) 
			{
				b = event.getClickedBlock().getRelative(BlockFace.WEST).getRelative(BlockFace.DOWN);
			}
			
			if( b != null )
			{
				for (OneCannon onec : AimCannon.getCannons()) 
				{
					if (onec.isThisCannon(b.getLocation(), false))
					{
						if( event.getAction() == Action.LEFT_CLICK_BLOCK )
							onec.fireTorpedoButton(event.getPlayer());
						else
							onec.setTorpedoMode(event.getPlayer());
					}
				}
			}
		}
		
	    }

	    //////Levers
	    if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK ) {
		    if (event.getClickedBlock().getType() == Material.LEVER) {
			// System.out.print("Lever used");
			Block b = null;
			
			////cannon levers
			if (event.getClickedBlock().getRelative(BlockFace.NORTH_EAST).getType() == Material.DISPENSER) {
			    b = event.getClickedBlock().getRelative(BlockFace.NORTH_EAST);
			}
			if (event.getClickedBlock().getRelative(BlockFace.NORTH_WEST).getType() == Material.DISPENSER) {
			    b = event.getClickedBlock().getRelative(BlockFace.NORTH_WEST);
			}
			if (event.getClickedBlock().getRelative(BlockFace.SOUTH_EAST).getType() == Material.DISPENSER) {
			    b = event.getClickedBlock().getRelative(BlockFace.SOUTH_EAST);
			}
			if (event.getClickedBlock().getRelative(BlockFace.SOUTH_WEST).getType() == Material.DISPENSER) {
			    b = event.getClickedBlock().getRelative(BlockFace.SOUTH_WEST);
			}
			
			//if no gun lever, then check torpedo levers
			if( b == null )
			{
				////torpedo levers
				//left load lever
				//north
				int torpedoAction = 0;  //1 left load lever, 2 right load lever, 3 outer door lever, 4 right click button,5 left click button, 6 left inner door lever, 7 right inner door lever
				if (event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST).getRelative(BlockFace.NORTH,2).getType() == Material.DISPENSER) 
				{
					b = event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST).getRelative(BlockFace.NORTH,2);
					torpedoAction = 1;
				}//south
				if (event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST).getRelative(BlockFace.SOUTH,2).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST).getRelative(BlockFace.SOUTH,2);
					torpedoAction = 1;
				}//east
				if (event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST,2).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST,2);
					torpedoAction = 1;
				}//west
				if (event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST,2).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST,2);
					torpedoAction = 1;
				}
				//right load lever
				//north
				if (event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST).getRelative(BlockFace.NORTH,2).getType() == Material.DISPENSER) 
				{
					b = event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST).getRelative(BlockFace.NORTH,2);
					torpedoAction = 2;
				}//south
				if (event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST).getRelative(BlockFace.SOUTH,2).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST).getRelative(BlockFace.SOUTH,2);
					torpedoAction = 2;
				}//east
				if (event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST,2).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST,2);
					torpedoAction = 2;
				}//west
				if (event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST,2).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST,2);
					torpedoAction = 2;
				}
				
				
				//left inner door lever
				//north
				if (event.getClickedBlock().getRelative(BlockFace.EAST,2).getRelative(BlockFace.NORTH,3).getType() == Material.DISPENSER) 
				{
					b = event.getClickedBlock().getRelative(BlockFace.EAST,2).getRelative(BlockFace.NORTH,3);
					torpedoAction = 6;
				}//south
				if (event.getClickedBlock().getRelative(BlockFace.WEST,2).getRelative(BlockFace.SOUTH,3).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.WEST,2).getRelative(BlockFace.SOUTH,3);
					torpedoAction = 6;
				}//east
				if (event.getClickedBlock().getRelative(BlockFace.SOUTH,2).getRelative(BlockFace.EAST,3).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.SOUTH,2).getRelative(BlockFace.EAST,3);
					torpedoAction = 6;
				}//west
				if (event.getClickedBlock().getRelative(BlockFace.NORTH,2).getRelative(BlockFace.WEST,3).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.NORTH,2).getRelative(BlockFace.WEST,3);
					torpedoAction = 6;
				}
				//right inner door lever
				//north
				if (event.getClickedBlock().getRelative(BlockFace.WEST,2).getRelative(BlockFace.NORTH,3).getType() == Material.DISPENSER) 
				{
					b = event.getClickedBlock().getRelative(BlockFace.WEST,2).getRelative(BlockFace.NORTH,3);
					torpedoAction = 7;
				}//south
				if (event.getClickedBlock().getRelative(BlockFace.EAST,2).getRelative(BlockFace.SOUTH,3).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.EAST,2).getRelative(BlockFace.SOUTH,3);
					torpedoAction = 7;
				}//east
				if (event.getClickedBlock().getRelative(BlockFace.NORTH,2).getRelative(BlockFace.EAST,3).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.NORTH,2).getRelative(BlockFace.EAST,3);
					torpedoAction = 7;
				}//west
				if (event.getClickedBlock().getRelative(BlockFace.SOUTH,2).getRelative(BlockFace.WEST,3).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.SOUTH,2).getRelative(BlockFace.WEST,3);
					torpedoAction = 7;
				}
				
				
				//outer door lever
				//north
				if (event.getClickedBlock().getRelative(BlockFace.NORTH,1).getType() == Material.DISPENSER) 
				{
					b = event.getClickedBlock().getRelative(BlockFace.NORTH,1);
					torpedoAction = 3;
				}//south
				if (event.getClickedBlock().getRelative(BlockFace.SOUTH,1).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.SOUTH,1);
					torpedoAction = 3;
				}//east
				if (event.getClickedBlock().getRelative(BlockFace.EAST,1).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.EAST,1);
					torpedoAction = 3;
				}//west
				if (event.getClickedBlock().getRelative(BlockFace.WEST,1).getType() == Material.DISPENSER) {
					b = event.getClickedBlock().getRelative(BlockFace.WEST,1);
					torpedoAction = 3;
				}
				
				
				if( b != null )
				{
				for (OneCannon onec : AimCannon.getCannons()) {
					if (onec.isThisCannon(b.getLocation(), false)) {
						//Do torpedo action
						if( torpedoAction > 0 )
						{
							if( torpedoAction == 1 )
							{
								onec.loadTorpedoLever(true, event.getPlayer());
							}else if( torpedoAction == 2 )
							{
								onec.loadTorpedoLever(false, event.getPlayer());
							}else if( torpedoAction == 3 )
							{
								onec.openTorpedoDoors(event.getPlayer(), false, false);
							}else if( torpedoAction == 6 )
							{
								onec.openTorpedoDoors(event.getPlayer(), true, true);
							}else if( torpedoAction == 7 )
							{
								onec.openTorpedoDoors(event.getPlayer(), true, false);
							}
						}
					}
				}
				}
				
			}else { //b != null
			    for (OneCannon onec : AimCannon.getCannons()) {
				if (onec.isThisCannon(b.getLocation(), false)) {
				    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
					    onec.turnCannon(true,event.getPlayer()); 
					}  else  {
					    onec.turnCannon(false,event.getPlayer());
					}
				    event.getPlayer().sendMessage("Cannon turned..");
				    event.setCancelled(true);
				    return;
				}
			    }
			}
		    }
		
	    }

	    ///Dispenser
	    if (event.getClickedBlock().getType() == Material.DISPENSER) {
		for (OneCannon onec : AimCannon.getCannons()) 
		{
		    if (onec.isThisCannon(event.getClickedBlock().getLocation(), false)) 
		    {
		    	if( event.getAction() == Action.LEFT_CLICK_BLOCK )
		    		onec.Charge(event.getPlayer(), true);
				else
					onec.Charge(event.getPlayer(), false);
			    
			    return;
		    }
		}
		
		// new Cannon
		OneCannon oc = new OneCannon(event.getClickedBlock().getLocation(), NavyCraft.instance);
		if (oc.isValidCannon(event.getClickedBlock())) {
		    // System.out.print("New Cannon");
			if( event.getAction() == Action.LEFT_CLICK_BLOCK )
				oc.Charge(event.getPlayer(), true);
			else
				oc.Charge(event.getPlayer(), false);
		    AimCannon.cannons.add(oc);
		    // event.getPlayer().sendMessage("Cannon charged..");
		}
	    }
	    
	    
	}
    }
    

    @SuppressWarnings("deprecation")
	public static void onBlockDispense(BlockDispenseEvent event) {
    	if( event.getBlock() != null && event.getBlock().getTypeId() == 23 )
    	{
    		//System.out.println("test1");
	    	for (OneCannon onec : AimCannon.getCannons()) 
			{
	    		//System.out.println("test2");
			    if (onec.isThisCannon(event.getBlock().getLocation(), true)) 
			    {
			    	//System.out.println("test3");
			    	event.setCancelled(true);
			    	return;
			    }
			}
    	}
    }
}
