package com.maximuspayne.aimcannon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;


public class Weapon {
	
	
	public Block warhead;
	BlockFace hdg;
	int setDepth;
	int rudder=0;
	int rudderSetting=0;
	int turnProgress = -1;
	int torpSetHeading=-1;
	boolean doubleTurn=false;
	int tubeNum=0;
	boolean active=false;
	boolean auto=true;
	boolean dead = false;
	int dcDirection=0;
	public int weaponType = 0;  //0=torpedo, 1=depth charge
	
	public Weapon(Block b, BlockFace bf, int depth)
	{
		weaponType = 0;
		warhead = b;
		hdg = bf;
		setDepth = depth;
	}
	
	public Weapon(Block b, int dcdirectionIn, int depth)
	{
		weaponType = 1;
		warhead = b;
		dcDirection = dcdirectionIn;
		setDepth = depth;
	}

}
