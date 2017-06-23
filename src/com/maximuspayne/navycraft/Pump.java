package com.maximuspayne.navycraft;
import org.bukkit.Location;


public class Pump {
	public Location loc;
	public int counter=0;
	public int limit=5;

	public Pump(Location pumpLocIn)
	{
		loc = pumpLocIn;
	}
}
