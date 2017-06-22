package com.maximuspayne.aimcannon;

import java.util.ArrayList;
import java.util.List;




public class AimCannon{
	public static List<OneCannon> cannons = new ArrayList<OneCannon>();
	public static List<Weapon> weapons = new ArrayList<Weapon>();
	
	public static List<OneCannon> getCannons() {
		return cannons;
	}

	public static List<Weapon> getWeapons() {
		return weapons;
	}

}
