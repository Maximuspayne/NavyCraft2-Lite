package com.maximuspayne.aimcannon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;


public class Torpedo {
	
	
	Block warhead;
	BlockFace hdg;
	int torpDepth;
	int rudder=0;
	int rudderSetting=0;
	int turnProgress = -1;
	int torpSetHeading=-1;
	boolean doubleTurn=false;
	int tubeNum=0;
	boolean active=false;
	boolean auto=true;
	boolean dead = false;
	public Torpedo(Block b, BlockFace bf, int depth)
	{
		warhead = b;
		hdg = bf;
		torpDepth = depth;
	}

}
