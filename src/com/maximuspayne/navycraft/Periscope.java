package com.maximuspayne.navycraft;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class Periscope {
	public Location signLoc;
	public Location scopeLoc;
	public int periscopeID;
	public boolean raised=true;
	public boolean destroyed=false;
	public Player user;
	public Periscope(Location signLocIn, int idIn)
	{
		signLoc = signLocIn;
		periscopeID = idIn;
	}
}
