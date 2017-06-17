package com.maximuspayne.navycraft.database;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.avaje.ebean.annotation.CacheStrategy;
import com.avaje.ebean.validation.NotNull;

@Entity()
@CacheStrategy
@Table(name = "minecartowners")
public class EBeansHandler {

	/*
	import com.afforess.minecartmaniacore.utils.StringUtils;
	import com.afforess.minecartmaniacore.world.MinecartManiaWorld;
	*/
	public class MinecartOwner {
		@Id
		private int id;
		@NotNull
		private String owner;
		@NotNull
		private String world;

		public MinecartOwner() {
			this.owner = "none";
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public MinecartOwner(String owner) {
			this.owner = owner;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public void setWorld(String world) {
			this.world = world;
		}

		public String getWorld() {
			return world;
		}
		
		public World getBukkitWorld() {
			return Bukkit.getServer().getWorld(world);
		}

	}

}
