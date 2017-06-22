package com.maximuspayne.navycraft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.earth2me.essentials.Essentials;
import com.maximuspayne.aimcannon.AimCannon;
import com.maximuspayne.aimcannon.OneCannon;
import com.maximuspayne.aimcannon.Weapon;
import com.maximuspayne.navycraft.plugins.PermissionInterface;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.ess3.api.MaxMoneyException;

public class CraftMover {
	private Craft craft;
	public static Plugin plugin;
	public WorldGuardPlugin wgp;

	volatile boolean sinkUpdate = false;
	volatile boolean stopSink = false;
	volatile boolean cruiseUpdate = false;
	volatile boolean stopSound = false;

	public HashMap<Player, Location> playerTeleports = new HashMap<>();

	public ArrayList<Block> initWeaponDispensers = new ArrayList<>();

	public CraftMover(Craft c, Plugin p) {
		craft = c;
		plugin = p;
	}

	@SuppressWarnings("deprecation")
	public void setBlock(int id, Block block) {
		// if(y < 0 || y > 127 || id < 0 || id > 255){
		if ((id < 0) || (id > 255)) {
			// + " x=" + x + " y=" + y + " z=" + z);
			System.out.println("Invalid block type ID. Begin panic.");
			return;
		}



		if (block.getTypeId() == id) {
			NavyCraft.instance.DebugMessage("Tried to change a " + id + " to itself.", 5);
			return;
		}

		NavyCraft.instance.DebugMessage("Attempting to set block at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " to " + id, 5);


		try {
			if (block.setTypeId(id) == false) {
				if (craft.world.getBlockAt(block.getLocation()).setTypeId(id) == false) {
					System.out.println("Could not set block of type " + block.getTypeId() + " to type " + id + ". I tried to fix it, but I couldn't.");
				} else {
					System.out.println("I hope to whatever God you believe in that this fix worked.");
				}
			}
		} catch (ClassCastException cce) {
			System.out.println("Routine cast exception.");
		}


	}

	public Block getWorldBlock(int x, int y, int z) {
		// return world.getBlockAt(posX + x, posY + y, posZ + z);
		return craft.world.getBlockAt(craft.minX + x, craft.minY + y, craft.minZ + z);
	}

	@SuppressWarnings("deprecation")
	public void storeDataBlocks() {
		for (DataBlock dataBlock : craft.dataBlocks) {
			if (dataBlock.id == getWorldBlock(dataBlock.x, dataBlock.y, dataBlock.z).getTypeId()) {
				dataBlock.data = getWorldBlock(dataBlock.x, dataBlock.y, dataBlock.z).getData();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void storeComplexBlocks() { // store the data of all complex blocks, or die trying
		Block currentBlock;

		for (DataBlock complexBlock : craft.complexBlocks) {
			currentBlock = getWorldBlock(complexBlock.x, complexBlock.y, complexBlock.z);
			if (complexBlock.id == currentBlock.getTypeId()) {

				complexBlock.id = currentBlock.getTypeId();
				complexBlock.data = currentBlock.getData();

				Inventory inventory = null;

				if (currentBlock.getState() instanceof Sign) {
					Sign sign = (Sign) currentBlock.getState();

					complexBlock.signLines = sign.getLines();

				} else if ((currentBlock.getTypeId() == 54) || (currentBlock.getTypeId() == 146)) {
					Chest chest = ((Chest) currentBlock.getState());
					inventory = chest.getInventory();
				} else if (currentBlock.getTypeId() == 23) {
					Dispenser dispenser = (Dispenser) currentBlock.getState();
					inventory = dispenser.getInventory();
				} else if (currentBlock.getTypeId() == 61) {
					Furnace furnace = (Furnace) currentBlock.getState();
					inventory = furnace.getInventory();
				} else if (currentBlock.getTypeId() == 117) {
					BrewingStand dispenser = (BrewingStand) currentBlock.getState();
					inventory = dispenser.getInventory();
				} else if (currentBlock.getTypeId() == 154) {
					Hopper dispenser = (Hopper) currentBlock.getState();
					inventory = dispenser.getInventory();
				} else if (currentBlock.getTypeId() == 158) {
					Dropper dispenser = (Dropper) currentBlock.getState();
					inventory = dispenser.getInventory();
				}

				if (inventory != null) {
					complexBlock.items = new ItemStack[27];
					NavyCraft.instance.DebugMessage("Inventory is " + inventory.getSize(), 4);
					for (int slot = 0; (slot < inventory.getSize()) && (slot < 27); slot++) {
						if ((inventory.getItem(slot) != null) && (inventory.getItem(slot).getTypeId() != 0)) {

							complexBlock.setItem(slot, inventory.getItem(slot));


							NavyCraft.instance.DebugMessage("Inventory has " + inventory.getItem(slot).getAmount() + " inventory item of type " + inventory.getItem(slot).getTypeId() + " in slot " + slot, 4);

							inventory.setItem(slot, null);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void restoreDataBlocks(int dx, int dy, int dz) {
		Block block;

		for (DataBlock dataBlock : craft.dataBlocks) {

			// this is a pop item, the block needs to be created
			if (BlocksInfo.needsSupport(craft.matrix[dataBlock.x][dataBlock.y][dataBlock.z])) {
				block = getWorldBlock(dx + dataBlock.x, dy + dataBlock.y, dz + dataBlock.z);

				setBlock(craft.matrix[dataBlock.x][dataBlock.y][dataBlock.z], block);

				block.setData((byte) dataBlock.data);
			} else { // the block is already there, just set the data
				Block theBlock = getWorldBlock(dx + dataBlock.x, dy + dataBlock.y, dz + dataBlock.z);
				if (theBlock.getTypeId() == 23) {

					boolean stopSearch = false;
					for (OneCannon onec : AimCannon.getCannons()) {
						if (onec.isThisCannon(theBlock.getLocation(), false)) {
							stopSearch = true;
						}
					}

					if (!stopSearch) {
						OneCannon oc = new OneCannon(theBlock.getLocation(), NavyCraft.instance);
						if (oc.isValidCannon(theBlock)) {

							for (OneCannon onec : AimCannon.getCannons()) {
								boolean oldCannonFound = onec.isThisCannon(new Location(craft.world, dataBlock.x + craft.minX, dataBlock.y + craft.minY, dataBlock.z + craft.minZ), true);
								if (oldCannonFound) {
									Location newLoc = theBlock.getLocation();
									onec.setLocation(newLoc);

								}
							}
						}
					}
				}
				theBlock.setData((byte) dataBlock.data);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void reloadWeapons(Player p) {

		for (DataBlock dataBlock : craft.dataBlocks) {

			Block theBlock = getWorldBlock(dataBlock.x, dataBlock.y, dataBlock.z);
			if (theBlock.getTypeId() == 23) {

				for (OneCannon onec : AimCannon.getCannons()) {
					if (onec.isThisCannon(theBlock.getLocation(), false)) {
						onec.reload(p);
					}
				}

			}
		}
	}

	@SuppressWarnings("deprecation")
	public void delayedRestoreSigns(int dx, int dy, int dz) {

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			if (NavyCraft.shutDown || craft.sinking) { return; }

			Block theBlock;

			for (DataBlock complexBlock : craft.complexBlocks) {
				theBlock = getWorldBlock(dx + complexBlock.x, dy + complexBlock.y, dz + complexBlock.z);

				theBlock.setData((byte) complexBlock.data);

				if (((complexBlock.id == 63) || (complexBlock.id == 68))) {
					NavyCraft.instance.DebugMessage("Restoring a sign.", 4);
					setBlock(complexBlock.id, theBlock);
					// theBlock.setcraft.typeId(complexBlock.id);
					theBlock.setData((byte) complexBlock.data);
					// Sign sign = (Sign) theBlock;
					if ((theBlock.getTypeId() == 63) || (theBlock.getTypeId() == 68)) {
						Sign sign = (Sign) theBlock.getState();

						sign.setLine(0, complexBlock.signLines[0]);
						sign.setLine(1, complexBlock.signLines[1]);
						sign.setLine(2, complexBlock.signLines[2]);
						sign.setLine(3, complexBlock.signLines[3]);

						sign.update();
					}
				}
			}
			structureUpdate(null, false);
		});

	}

	@SuppressWarnings("deprecation")
	public void restoreComplexBlocks(int dx, int dy, int dz) {
		Block theBlock;
		Inventory inventory;

		for (DataBlock complexBlock : craft.complexBlocks) {
			theBlock = getWorldBlock(dx + complexBlock.x, dy + complexBlock.y, dz + complexBlock.z);

			theBlock.setData((byte) complexBlock.data);

			inventory = null;

			if (((complexBlock.id == 63) || (complexBlock.id == 68))) {


			} else if ((theBlock.getTypeId() == 54) || (theBlock.getTypeId() == 146)) {
				Chest chest = ((Chest) theBlock.getState());
				inventory = chest.getInventory();
			} else if (theBlock.getTypeId() == 23) {
				Dispenser dispenser = (Dispenser) theBlock.getState();
				inventory = dispenser.getInventory();
			} else if (theBlock.getTypeId() == 61) {
				Furnace furnace = (Furnace) theBlock.getState();
				inventory = furnace.getInventory();
			} else if (theBlock.getTypeId() == 117) {
				BrewingStand dispenser = (BrewingStand) theBlock.getState();
				inventory = dispenser.getInventory();
			} else if (theBlock.getTypeId() == 154) {
				Hopper dispenser = (Hopper) theBlock.getState();
				inventory = dispenser.getInventory();
			} else if (theBlock.getTypeId() == 158) {
				Dropper dispenser = (Dropper) theBlock.getState();
				inventory = dispenser.getInventory();
			}

			// restore the block's inventory
			if (inventory != null) {
				for (int slot = 0; (slot < inventory.getSize()) && (slot < 27); slot++) {
					if ((complexBlock.items[slot] != null) && (complexBlock.items[slot].getTypeId() != 0)) {
						inventory.setItem(slot, complexBlock.items[slot]);
						NavyCraft.instance.DebugMessage("Moving " + complexBlock.items[slot].getAmount() + " inventory item of type " + complexBlock.items[slot].getTypeId() + " in slot " + slot, 4);
					}
				}
			}

		}
		delayedRestoreSigns(0, 0, 0);
	}

	@SuppressWarnings("deprecation")
	public void calculateMove(int dx, int dy, int dz) {
		//Try to detect damage and changes before moving
		structureUpdate(null, false);
		
		NavyCraft.instance.DebugMessage("DXYZ is (" + dx + ", " + dy + ", " + dz + ")", 4);
		// instead of forcing the craft to move, check some things beforehand
		if (craft.waitTorpLoading > 0) {
			if (craft.driverName != null) {
				Player p = plugin.getServer().getPlayer(craft.driverName);
				if (p != null) {
					p.sendMessage(ChatColor.RED + "Torpedo Reloading Please Wait.");
				}
			}

			return;
		}
		
		//Return if active weapon within vehicle grid
		if( checkForWeapons() )
			return;

		if (craft.sinking) {
			if (craft.driverName != null) {
				Player p = plugin.getServer().getPlayer(craft.driverName);
				if (p != null) {
					p.sendMessage(ChatColor.RED + "You are sinking!");
				}
			}

			return;
		}
	

		if (craft.inHyperSpace) {
			if (dx > 0) {
				dx = 1;
			} else if (dx < 0) {
				dx = -1;
			}
			if (dy > 0) {
				dy = 1;
			} else if (dy < 0) {
				dy = -1;
			}
			if (dz > 0) {
				dz = 1;
			} else if (dz < 0) {
				dz = -1;
			}

			Craft_Hyperspace.hyperSpaceMove(craft, dx, dy, dz);
			return;
		}

		if (craft.type.obeysGravity && craft.onGround && craft.canMove(dx, dy - 1, dz) && (craft.engineBlocks.size() == 0)) {
			dy -= 1;
		}

		// craft.speed decrease with time
		if (!craft.type.doesCruise) {
			craft.setSpeed(craft.speed - (int) ((System.currentTimeMillis() - craft.lastMove) / 1500));
		}
		if (craft.possibleCollision && (((System.currentTimeMillis() - craft.lastMove) / 1000) > 5)) {
			craft.possibleCollision = false;
		}

		if ((craft.speed <= 0) && !craft.type.doesCruise) {
			craft.speed = 1;
		}

		// prevent submarines from getting out of water
		if ((craft.type.canDive || craft.type.canDig) && !craft.type.canFly && (craft.minY >= (63 - craft.keelDepth)) && (dy > 0)) {
			dy = 0;
		}

		craft.doCollide = false;

		boolean collisionSpeed = (craft.collisionSpeed >= 4);

		int lastSpeed = craft.speed;

		// check the craft can move there. If not, reduce the craft.speed and try
		// again until craft.speed = 1
		if (craft.canMove(dx, dy, dz)) {
			if (craft.speedReducedCol && (craft.speed > craft.reductionSpeed)) {
				craft.speedReducedCol = false;
			}
		} else {
			if (!craft.speedReducedCol) {
				craft.speedReducedCol = true;
				craft.collisionSpeed = craft.speed;
			}
			while (!craft.canMove(dx, dy, dz)) {

				if ((craft.speed == 1) && craft.type.isTerrestrial) { // vehicles which are terrestrial
																		// (ground-dwelling) can go over certain terrain
					if (craft.canMove(dx, dy + 1, dz)) {
						dy += 1;
						break;
					}
				}

				if (craft.speed == 1) {

					if (craft.type.canFly) {
						craft.checkLanding = true;

						// craft.driver.sendMessage("Checking landing");

						if (craft.canMove(dx, 0, dz)) {
							if ((lastSpeed > 5) && (lastSpeed < 10)) {
								craft.speed = lastSpeed;
								if (craft.canMove(dx, 0, dz)) {
									Block block1 = craft.world.getBlockAt(craft.minX + (craft.sizeX / 2), craft.minY - 1, craft.minZ + (craft.sizeZ / 2));
									Block block2 = craft.world.getBlockAt(craft.minX, craft.minY - 1, craft.minZ);
									Block block3 = craft.world.getBlockAt((craft.minX + craft.sizeX) - 1, craft.minY - 1, (craft.minZ + craft.sizeZ) - 1);
									if (!((block1.getTypeId() >= 8) && (block1.getTypeId() <= 11)) && (block1.getTypeId() != 0)) {
										if (!((block2.getTypeId() >= 8) && (block2.getTypeId() <= 11)) && (block2.getTypeId() != 0)) {
											if (!((block3.getTypeId() >= 8) && (block3.getTypeId() <= 11)) && (block3.getTypeId() != 0)) {
												if (craft.driverName != null) {
													Player p = plugin.getServer().getPlayer(craft.driverName);
													if (p != null) {
														p.sendMessage("Touch Down!");
													}
												}

												craft.onGround = true;
												craft.possibleCollision = false;
												dy = 0;
												craft.checkLanding = false;
												break;
											} else {
												craft.possibleCollision = true;
											}
										} else {
											craft.possibleCollision = true;
										}
									} else {
										craft.possibleCollision = true;
									}

								} else {
									// craft.driver.sendMessage("runway collision");
									craft.possibleCollision = true;
								}
							} else {
								// craft.driver.sendMessage("last speed=" + lastSpeed);
								craft.possibleCollision = true;
							}
						} else {
							// craft.driver.sendMessage("cant move down");
							craft.possibleCollision = true;
						}
						craft.checkLanding = false;
					}

					if (craft.type.canFly && (dy > 0) && (craft.maxY >= 127)) {
						if (craft.driverName != null) {
							Player p = plugin.getServer().getPlayer(craft.driverName);
							if (p != null) {
								p.sendMessage("Max Altitude!");
							}
						}

						dy = 0;
						break;
					}

					// craft.player.sendMessage(ChatColor.RED + "the " + craft.name + " won't go any further");
					if (craft.possibleCollision && !checkProtectedRegion(craft.collisionLoc) && !NavyCraft.checkSpawnRegion(craft.collisionLoc) && !craft.type.canFly) {
						if (craft.driverName != null) {
							Player p = plugin.getServer().getPlayer(craft.driverName);
							if (p != null) {
								p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "We collided with something! Engines stopped.");
								// plugin.getServer().broadcastMessage("collide!!");
							}
						}
						if (craft.captainName != null) {
							Player p = plugin.getServer().getPlayer(craft.captainName);
							if (p != null) {
								p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "We collided with something! Engines stopped.");
								// plugin.getServer().broadcastMessage("collide!!");
							}
						}

						craft.doCollide = true;
						craft.possibleCollision = false;
						break;
					} else if (NavyCraft.checkSpawnRegion(craft.collisionLoc)) {
						if (craft.driverName != null) {
							Player p = plugin.getServer().getPlayer(craft.driverName);
							if (p != null) {
								p.sendMessage(ChatColor.YELLOW + "That direction is protected.");
							}
						}
						if (craft.captainName != null) {
							Player p = plugin.getServer().getPlayer(craft.captainName);
							if (p != null) {
								p.sendMessage(ChatColor.YELLOW + "That direction is protected.");
							}
						}
						craft.doDestroy = true;
						// craft.sinking = true;
						// craft.helmDestroyed = true;
						return;
					} else if (checkProtectedRegion(craft.collisionLoc)) {
						if (craft.driverName != null) {
							Player p = plugin.getServer().getPlayer(craft.driverName);
							if (p != null) {
								p.sendMessage(ChatColor.YELLOW + "That direction is protected.");
							}
						}
						if (craft.captainName != null) {
							Player p = plugin.getServer().getPlayer(craft.captainName);
							if (p != null) {
								p.sendMessage(ChatColor.YELLOW + "That direction is protected.");
							}
						}
						return;
					} else if (craft.possibleCollision && craft.type.canFly) {
						if (craft.driverName != null) {
							Player p = plugin.getServer().getPlayer(craft.driverName);
							if (p != null) {
								p.sendMessage(ChatColor.RED + "Crash!!! PANIC!!");
							}
						}
						if (craft.captainName != null) {
							Player p = plugin.getServer().getPlayer(craft.captainName);
							if (p != null) {
								p.sendMessage(ChatColor.RED + "Crash!!! PANIC!!");
							}
						}
						craft.doCollide = true;
						craft.possibleCollision = false;
						craft.doSink = true;
						break;
					} else {
						if (craft.driverName != null) {
							Player p = plugin.getServer().getPlayer(craft.driverName);
							if (p != null) {
								p.sendMessage(ChatColor.YELLOW + "That direction is blocked.");
							}
						}
						return;
					}

				}

				if (collisionSpeed) {
					craft.possibleCollision = true;
				} else {
					craft.possibleCollision = false;
				}

				craft.setSpeed(craft.speed - 1);
				craft.reductionSpeed = craft.speed;
			}
		}

		if ((craft.speed > 1) && !collisionSpeed) {
			craft.possibleCollision = false;
		}

		if ((craft.type.canNavigate || craft.type.canDive) && (craft.buoyancy != 0)) {
			dy += calculateBuoyancyMove();
		}

		if (!((dx == 0) && (dy == 0) && (dz == 0))) {

			delayed_move_collide(dx, dy, dz);
		}
	}

	@SuppressWarnings("deprecation")
	public int calculateBuoyancyMove() {
		if (craft.canMove(0, craft.buoyancy, 0)) {
			return craft.buoyancy;
		} else {
			if (craft.buoyancy < 0) {
				Block newBlock;
				int newBlockId;
				int solidCount = 0;
				for (int x = 0; x < craft.sizeX; x++) {
					for (int z = 0; z < craft.sizeZ; z++) {
						newBlock = craft.world.getBlockAt(craft.minX + x, craft.minY - 1, craft.minZ + z);
						newBlockId = newBlock.getTypeId();
						if ((newBlockId != 0) && !((newBlockId >= 8) && (newBlockId <= 11))) {
							solidCount++;
						}
					}
				}

				if (solidCount > (int) (craft.sizeX * craft.sizeZ * 0.2f)) {
					craft.doSink = true;
					for (String s : craft.crewNames) {
						Player p = plugin.getServer().getPlayer(s);
						if (p != null) {
							p.sendMessage(ChatColor.RED + "***We ran aground!***");
						}

					}
				} else {
					return craft.buoyancy;
				}
			}
		}
		return 0;
	}

	// move the craft according to a vector d
	public void move1() {
		// if(craft.type.canDig) {
		if (craft.isDestroying) { return; }

		NavyCraft.instance.DebugMessage("craft.waterLevel is " + craft.waterLevel, 2);
		NavyCraft.instance.DebugMessage("craft.waterType is " + craft.waterType, 2);
		NavyCraft.instance.DebugMessage("newcraft.waterLevel is " + craft.newWaterLevel, 2);

		// }

		if ((Math.abs(craft.dx) > 0) && (Math.abs(craft.dz) > 0)) {
			craft.dx = (craft.speed / 2) * craft.dx;
			craft.dz = (craft.speed / 2) * craft.dz;
		} else {
			craft.dx = craft.speed * craft.dx;
			craft.dz = craft.speed * craft.dz;
		}

		if (craft.type.canDig) {
			craft.waterLevel = craft.newWaterLevel;
			// craft.waterLevel = 63;
		}


		if (craft.type.canDive && (craft.dy != 0)) {
			if (craft.speed > 10) {
				craft.dy = craft.dy * 3;
			} else if (craft.speed > 5) {
				craft.dy = craft.dy * 2;
			}
		}

		if (craft.type.canFly && craft.type.doesCruise) {
			if (!craft.onGround) {
				if ((craft.speed >= 10) && (craft.dy != 0)) {
					craft.dy = ((craft.speed - 6) / 2) * (int) Math.signum(craft.dy);
				} else if ((craft.speed >= 8) && (craft.dy != 0)) {
					craft.dy = (int) Math.signum(craft.dy);
				} else if ((craft.speed >= 8) && (craft.dy == 0)) {
					craft.dy = 0;
				} else if (craft.speed > 5) {
					craft.dy = -1;
				} else if (craft.speed > 3) {
					craft.dy = -3;
				} else if (craft.speed <= 3) {
					craft.dy = -5;
				}
			} else {
				// lift off
				if ((craft.speed >= 8) && (craft.dy > 0)) {
					craft.onGround = false;
					if (craft.driverName != null) {
						Player p = plugin.getServer().getPlayer(craft.driverName);
						if (p != null) {
							p.sendMessage("Lift off!");
						}
					}
					craft.dy = 1;
				} else/// stay on ground
				{

					craft.dy = 0;
				}
			}
		}

		// vertical limit
		if (((craft.minY + craft.dy) < 0) || ((craft.maxY + craft.dy) > 255)) {
			if (craft.driverName != null) {
				Player p = plugin.getServer().getPlayer(craft.driverName);
				if (p != null) {
					p.sendMessage("Max altitude");
				}
			}
			craft.dy = 0;
		}

		if (craft.sinking) {
			craft.speed = 1;
			craft.dy = -1;
		}

		if (craft.helmDestroyed && !craft.sinking && !craft.type.doesCruise) {
			if (craft.driverName != null) {
				Player p = plugin.getServer().getPlayer(craft.driverName);
				if (p != null) {
					p.sendMessage(ChatColor.RED + "Helm Control Destroyed!");
				}
			}
		}

		storeDataBlocks();
		storeComplexBlocks();
		craft.checkEntities = craft.getCraftEntities(false);

	}

	@SuppressWarnings("deprecation")
	public void move2() {
		// first pass, remove all items that need a support
		removeSupportBlocks();

		short blockId;
		Block block;
		Block innerBlock;
		if( craft.matrix == null )
			return;
		
		// second pass, the regular blocks
		for (int x = 0; x < craft.sizeX; x++) {
			for (int z = 0; z < craft.sizeZ; z++) {
				for (int y = 0; y < craft.sizeY; y++) {

					// handle displaced blocks
					if( craft.matrix == null )
						return;
					blockId = craft.matrix[x][y][z];

					if (blockId == -1) {
						continue;
					}

					block = getWorldBlock(x, y, z);

					// old block position (remove)
					if (((x - craft.dx) >= 0) && ((y - craft.dy) >= 0) && ((z - craft.dz) >= 0) && ((x - craft.dx) < craft.sizeX) && ((y - craft.dy) < craft.sizeY) && ((z - craft.dz) < craft.sizeZ)) {

						// after moving, this location is not a craft block anymore
						if ((craft.matrix[x - craft.dx][y - craft.dy][z - craft.dz] == -1) || BlocksInfo.needsSupport(craft.matrix[x - craft.dx][y - craft.dy][z - craft.dz])) {
							if ((block.getY() >= 63) || (!(craft.type.canNavigate || craft.type.canDive) && !craft.sinking)) {
								setBlock(0, block);
							} else {
								if (craft.waterType == 0) {
									setBlock(8, block);
								} else {
									setBlock(craft.waterType, block);
								}
							}
						}
					} else { // the back of the craft, remove

						if ((block.getY() >= 63) || (!(craft.type.canNavigate || craft.type.canDive) && !craft.sinking)) {
							setBlock(0, block);
						} else {
							if (craft.waterType == 0) {
								setBlock(8, block);
							} else {
								setBlock(craft.waterType, block);
							}
						}
					}

					// new block position (place)
					if (!BlocksInfo.needsSupport(blockId)) {

						// Block innerBlock = world.getBlockAt(posX + dx + x,posY + dy + y, posZ + dz + z);
						innerBlock = getWorldBlock(craft.dx + x, craft.dy + y, craft.dz + z);

						// drop the item corresponding to the block if it is not a craft block


						if (craft.type.digBlockDurability > 0) { // break drill bits
							int blockDurability = block.getType().getMaxDurability();
							int num = ((new Random()).nextInt(Math.abs(blockDurability - 0) + 1)) + 0;

							if (num == 1) {
								NavyCraft.instance.DebugMessage("Random = 1", 1);
								continue;
							} else {
								NavyCraft.instance.DebugMessage("Random number = " + Integer.toString(num), 1);
							}
						}

						// inside the craft, the block is different
						if (((x + craft.dx) >= 0) && ((y + craft.dy) >= 0) && ((z + craft.dz) >= 0) && ((x + craft.dx) < craft.sizeX) && ((y + craft.dy) < craft.sizeY) && ((z + craft.dz) < craft.sizeZ)) {
							if (craft.matrix[x][y][z] != craft.matrix[x + craft.dx][y + craft.dy][z + craft.dz]) {
								// setBlock(world, blockId, posX + dx + x,
								// posY + dy + y, posZ + dz + z);
								if ((blockId != 36) && (blockId != 34)) {
									if ((blockId != 8) && (blockId != 9)) {
										setBlock(blockId, innerBlock);
									} else {

										setBlock(0, innerBlock);

									}
								}
							}
						}
						// outside of the previous bounding box
						else {

							if (((innerBlock.getTypeId() >= 8) && (innerBlock.getTypeId() <= 11)) || (blockId != 0)) {
								setBlock(blockId, innerBlock);
							}


						}
					}
				}
			}
		}

		restoreDataBlocks(craft.dx, craft.dy, craft.dz);
		restoreSupportBlocks(craft.dx, craft.dy, craft.dz);
		restoreComplexBlocks(craft.dx, craft.dy, craft.dz);
	}

	@SuppressWarnings("deprecation")
	public void move2b() {
		// first pass, remove all items that need a support
		removeSupportBlocks();

		short blockId;
		Block block;
		Block innerBlock;
		
		if( craft.matrix == null )
			return;

		// second pass, the regular blocks
		for (int x = 0; x < craft.sizeX; x++) {
			for (int z = 0; z < craft.sizeZ; z++) {
				for (int y = 0; y < craft.sizeY; y++) {

					// handle displaced blocks
					if( craft.matrix == null )
						return;
					
					blockId = craft.matrix[x][y][z];

					if (blockId == -1) {
						continue;
					}

					block = getWorldBlock(x, y, z);

					// old block position (remove)


					// after moving, this location is not a craft block anymore
					if ((craft.matrix[x][y][z] == -1) || BlocksInfo.needsSupport(craft.matrix[x][y][z])) {

						if ((block.getY() >= 63) || (!(craft.type.canNavigate || craft.type.canDive) && !craft.sinking)) {
							setBlock(0, block);
						} else {
							if (craft.waterType == 0) {
								setBlock(8, block);
							} else {
								setBlock(craft.waterType, block);
							}
						}
					}

					// new block position (place)
					if (!BlocksInfo.needsSupport(blockId)) {

						// Block innerBlock = world.getBlockAt(posX + dx + x,posY + dy + y, posZ + dz + z);
						innerBlock = getWorldBlock(craft.dx + x, craft.dy + y, craft.dz + z);

						if (craft.type.digBlockDurability > 0) { // break drill bits
							int blockDurability = block.getType().getMaxDurability();
							int num = ((new Random()).nextInt(Math.abs(blockDurability - 0) + 1)) + 0;

							if (num == 1) {
								NavyCraft.instance.DebugMessage("Random = 1", 1);
								continue;
							} else {
								NavyCraft.instance.DebugMessage("Random number = " + Integer.toString(num), 1);
							}
						}

						// inside the craft, the block is different
						if (((x + craft.dx) >= 0) && ((y + craft.dy) >= 0) && ((z + craft.dz) >= 0) && ((x + craft.dx) < craft.sizeX) && ((y + craft.dy) < craft.sizeY) && ((z + craft.dz) < craft.sizeZ)) {
							if (craft.matrix[x][y][z] != craft.matrix[x + craft.dx][y + craft.dy][z + craft.dz]) {
								if ((blockId != 36) && (blockId != 34)) {
									if ((blockId != 8) && (blockId != 9)) {
										setBlock(blockId, innerBlock);
									} else {

										setBlock(0, innerBlock);
									}
								}
							}
						}
						// outside of the previous bounding box
						else {

							if (((innerBlock.getTypeId() >= 8) && (innerBlock.getTypeId() <= 11)) || (blockId != 0)) {
								setBlock(blockId, innerBlock);
							}

						}
					}
				}
			}
		}

		restoreDataBlocks(craft.dx, craft.dy, craft.dz);
		restoreComplexBlocks(craft.dx, craft.dy, craft.dz);

		restoreSupportBlocks(craft.dx, craft.dy, craft.dz);
	}

	public void move3(boolean scheduledMove) {

		playerTeleports.clear();

		craft.isMovingPlayers = true;

		Player pl = null;
		if (craft.driverName != null) {
			pl = plugin.getServer().getPlayer(craft.driverName);
		}

		for (Entity e : craft.checkEntities) {

			if (NavyCraft.instance.ConfigSetting("TryNudge").equalsIgnoreCase("true") && ((craft.type.listenMovement == false) || ((pl != null) && (e != pl)))) {
				movePlayer(e, craft.dx, craft.dy, craft.dz);
			} else {
				teleportPlayer(e, craft.dx, craft.dy, craft.dz);
			}
		}

		teleportUpdate();

		craft.minX += craft.dx;
		craft.minY += craft.dy;
		craft.minZ += craft.dz;
		craft.maxX = (craft.minX + craft.sizeX) - 1;
		craft.maxY = (craft.minY + craft.sizeY) - 1;
		craft.maxZ = (craft.minZ + craft.sizeZ) - 1;


		if ((craft.waterLevel == (craft.sizeY - 1)) && (craft.newWaterLevel < craft.waterLevel)) {
			craft.waterLevel = craft.newWaterLevel;
		} else if ((craft.waterLevel <= -1) && (craft.newWaterLevel > craft.waterLevel)) {
			craft.waterLevel = craft.newWaterLevel;
		} else if ((craft.waterLevel >= 0) && (craft.waterLevel < (craft.sizeY - 1))) {
			craft.waterLevel -= craft.dy;
		}

		if (scheduledMove) {
			craft.lastMove = System.currentTimeMillis();
		}

		/*if (craft.type.requiresRails) {
			int xMid = craft.matrix.length / 2;
			int zMid = craft.matrix[0][0].length / 2;

			Block belowBlock = getWorldBlock(xMid, -1, zMid);
			craft.railBlock = belowBlock;

			if (belowBlock.getType() == Material.RAILS) {
				railMove();
			}
		}*/

		if (!craft.autoTurn) {
			craft.speed = 1;
		}

		// the craft goes faster every click
		if (!craft.type.doesCruise) {
			craft.setSpeed(craft.speed + 1);
		}
	}

/*	public static void updateEntity(Entity entity, List<Player> observers) {

		World world = entity.getWorld();
		WorldServer worldServer = ((CraftWorld) world).getHandle();

		EntityTracker tracker = worldServer.tracker;
		EntityTrackerEntry entry = tracker.trackedEntities.get(entity.getEntityId());

		List<EntityHuman> nmsPlayers = getNmsPlayers(observers);

		// Force Minecraft to resend packets to the affected clients
		entry.trackedPlayers.removeAll(nmsPlayers);
		entry.scanPlayers(nmsPlayers);
	}

	private static List<EntityHuman> getNmsPlayers(List<Player> players) {
		List<EntityHuman> nsmPlayers = new ArrayList<>();

		for (Player bukkitPlayer : players) {
			CraftPlayer craftPlayer = (CraftPlayer) bukkitPlayer;
			nsmPlayers.add(craftPlayer.getHandle());
		}

		return nsmPlayers;
	}*/

	public void move(int dx, int dy, int dz) {
		if (!((dx == 0) && (dy == 0) && (dz == 0))) {
			craft.dx = dx;
			craft.dy = dy;
			craft.dz = dz;
			move1();
			move2();
			move3(false);
		}
	}

	public void delayed_move(int dx, int dy, int dz) {

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			if (NavyCraft.shutDown) { return; }
			move(dx, dy, dz);
		});
	}

	public void delayed_move_collide(int dx, int dy, int dz) {

		if (craft.type.canNavigate || craft.type.canDive) {
			if ((craft.checkEntities != null) && craft.checkEntities.isEmpty()) {
				for (Entity e : craft.checkEntities) {
					if (e instanceof Player) {
						Player p = (Player) e;
						if (craft.submergedMode) {
							p.playSound(p.getLocation(), Sound.ENTITY_BOBBER_SPLASH, 1.0f, 0.6f);
						} else {
							p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 0.5f + ((0.5f * craft.speed) / 12.0f), 0.6f);
						}
					}
				}
			}

		}
		//plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			if (NavyCraft.shutDown) { return; }

			if (!craft.doCollide){
				craft.dx = dx;
				craft.dy = dy;
				craft.dz = dz;
				move1();
				move2();
				move3(true);
			}
			else {

				Block colBlock = null;
				if (craft.collisionLoc != null) {
					colBlock = craft.collisionLoc.getBlock();
				} else {
					colBlock = new Location(craft.world, craft.minX + (craft.sizeX / 2), craft.minY, craft.minZ + (craft.sizeZ / 2)).getBlock();
				}
				NavyCraft.explosion(6, colBlock,false);

				craft.setSpeed = 0;
				craft.turnProgress = 0;
				craft.rudder = 0;
				craft.enginesOn = false;

				structureUpdate(null, false);

				if (craft.captainName != null) {
					Player p = plugin.getServer().getPlayer(craft.captainName);
					if (p != null) {
						Craft otherCraft = Craft.getOtherCraft(craft, p, colBlock.getLocation().getBlockX(), colBlock.getLocation().getBlockY(), colBlock.getLocation().getBlockZ());
						if (otherCraft != null) {
							CraftMover cm1 = new CraftMover(otherCraft, plugin);

							if (p != null) {
								cm1.structureUpdate(p, false);
							}
						} else {
							if (p != null) {
								otherCraft = Craft.getOtherCraft(craft, p, colBlock.getRelative(2, 1, 2).getX(), colBlock.getRelative(2, 1, 2).getY(), colBlock.getRelative(2, 1, 2).getZ());
							}
							if (otherCraft != null) {
								CraftMover cm2 = new CraftMover(otherCraft, plugin);
								cm2.structureUpdate(p, false);
							} else {
								otherCraft = Craft.getOtherCraft(craft, p, colBlock.getRelative(-2, -1, -2).getX(), colBlock.getRelative(-2, -1, -2).getY(), colBlock.getRelative(-2, -1, -2).getZ());
								if (otherCraft != null) {
									CraftMover cm3 = new CraftMover(otherCraft, plugin);
									cm3.structureUpdate(p, false);
								}
							}
						}
					}
				}
			}
		//});
	}

	@SuppressWarnings("deprecation")
	public void move(int dx, int dy, int dz, boolean checkStructure) {
		if (craft.isDestroying) { return; }

		NavyCraft.instance.DebugMessage("craft.waterLevel is " + craft.waterLevel, 2);
		NavyCraft.instance.DebugMessage("craft.waterType is " + craft.waterType, 2);
		NavyCraft.instance.DebugMessage("newcraft.waterLevel is " + craft.newWaterLevel, 2);
		
		if ((Math.abs(dx) > 0) && (Math.abs(dz) > 0)) {
			dx = (craft.speed / 2) * dx;
			dz = (craft.speed / 2) * dz;
		} else {
			dx = craft.speed * dx;
			dz = craft.speed * dz;
		}

		if (craft.type.canDig) {
			craft.waterLevel = craft.newWaterLevel;
		}

		ArrayList<Entity> checkEntities;

		if (Math.abs(craft.speed * dy) > 1) {
			dy = craft.speed * dy;// / 2;
			if ((Math.abs(dy) == 0) || craft.type.canDive || craft.type.isTerrestrial) {
				dy = (int) Math.signum(dy);
			}
		}

		if (craft.type.canFly && craft.type.doesCruise) {
			if (!craft.onGround) {
				if ((craft.speed >= 10) && (dy != 0)) {
					dy = ((craft.speed - 6) / 2) * (int) Math.signum(dy);
				} else if ((craft.speed >= 8) && (dy != 0)) {
					dy = (int) Math.signum(dy);
				} else if ((craft.speed >= 8) && (dy == 0)) {
					dy = 0;
				} else if (craft.speed > 5) {
					dy = -1;
				} else if (craft.speed > 3) {
					dy = -3;
				} else if (craft.speed <= 3) {
					dy = -5;
				}
			} else {
				// lift off
				if ((craft.speed >= 8) && (dy > 0)) {
					craft.onGround = false;
					if (craft.driverName != null) {
						Player p = plugin.getServer().getPlayer(craft.driverName);
						if (p != null) {
							p.sendMessage("Lift off!");
						}
					}
					dy = 1;
				} else/// stay on ground
				{
					dy = 0;
				}
			}
		}

		// vertical limit
		if (((craft.minY + dy) < 0) || ((craft.maxY + dy) > 255)) {
			if (craft.driverName != null) {
				Player p = plugin.getServer().getPlayer(craft.driverName);
				if (p != null) {
					p.sendMessage("Max altitude");
				}
			}
			dy = 0;
		}

		if (craft.sinking) {
			craft.speed = 1;
			dy = -1;
		}

		if (checkStructure) {
			structureUpdate(null, false);
		}

		if (craft.helmDestroyed && !craft.sinking && !craft.type.doesCruise) {
			if (craft.driverName != null) {
				Player p = plugin.getServer().getPlayer(craft.driverName);
				if (p != null) {
					p.sendMessage(ChatColor.RED + "Helm Control Destroyed!");
				}
			}
			// return;
		}

		storeDataBlocks();
		storeComplexBlocks();
		checkEntities = craft.getCraftEntities(false);

		// first pass, remove all items that need a support
		removeSupportBlocks();

		short blockId;
		Block block;
		Block innerBlock;

		// second pass, the regular blocks
		for (int x = 0; x < craft.sizeX; x++) {
			for (int z = 0; z < craft.sizeZ; z++) {
				for (int y = 0; y < craft.sizeY; y++) {

					// handle displaced blocks

					blockId = craft.matrix[x][y][z];

					if (blockId == -1) {
						continue;
					}

					block = getWorldBlock(x, y, z);

					// old block position (remove)
					if (((x - dx) >= 0) && ((y - dy) >= 0) && ((z - dz) >= 0) && ((x - dx) < craft.sizeX) && ((y - dy) < craft.sizeY) && ((z - dz) < craft.sizeZ)) {

						// after moving, this location is not a craft block anymore
						if ((craft.matrix[x - dx][y - dy][z - dz] == -1) || BlocksInfo.needsSupport(craft.matrix[x - dx][y - dy][z - dz])) {
							if ((block.getY() >= 63) || (!(craft.type.canNavigate || craft.type.canDive) && !craft.sinking)) {
								setBlock(0, block);
							} else {
								if (craft.waterType == 0) {
									setBlock(8, block);
								} else {
									setBlock(craft.waterType, block);
								}
							}
						}
					} else { // the back of the craft, remove

						if ((block.getY() >= 63) || (!(craft.type.canNavigate || craft.type.canDive) && !craft.sinking)) {
							setBlock(0, block);
						} else {
							if (craft.waterType == 0) {
								setBlock(8, block);
							} else {
								setBlock(craft.waterType, block);
							}
						}
					}

					// new block position (place)
					if (!BlocksInfo.needsSupport(blockId)) {

						// Block innerBlock = world.getBlockAt(posX + dx + x,posY + dy + y, posZ + dz + z);
						innerBlock = getWorldBlock(dx + x, dy + y, dz + z);

						if (craft.type.digBlockDurability > 0) { // break drill bits
							int blockDurability = block.getType().getMaxDurability();
							int num = ((new Random()).nextInt(Math.abs(blockDurability - 0) + 1)) + 0;

							if (num == 1) {
								NavyCraft.instance.DebugMessage("Random = 1", 1);
								continue;
							} else {
								NavyCraft.instance.DebugMessage("Random number = " + Integer.toString(num), 1);
							}
						}

						// inside the craft, the block is different
						if (((x + dx) >= 0) && ((y + dy) >= 0) && ((z + dz) >= 0) && ((x + dx) < craft.sizeX) && ((y + dy) < craft.sizeY) && ((z + dz) < craft.sizeZ)) {
							if (craft.matrix[x][y][z] != craft.matrix[x + dx][y + dy][z + dz]) {
								// setBlock(world, blockId, posX + dx + x,
								// posY + dy + y, posZ + dz + z);
								if ((blockId != 36) && (blockId != 34)) {
									if ((blockId != 8) && (blockId != 9)) {
										setBlock(blockId, innerBlock);
									} else {

										setBlock(0, innerBlock);

									}
								}
							}
						}
						// outside of the previous bounding box
						else {

							if (blockId != -1) {
								setBlock(blockId, innerBlock);

							}

						}
					}
				}
			}
		}

		restoreDataBlocks(dx, dy, dz);
		restoreComplexBlocks(dx, dy, dz);

		restoreSupportBlocks(dx, dy, dz);

		Player p = null;
		if (craft.driverName != null) {
			p = plugin.getServer().getPlayer(craft.driverName);
		}

		playerTeleports.clear();

		craft.isMovingPlayers = true;

		for (Entity e : checkEntities) {
			if (NavyCraft.instance.ConfigSetting("TryNudge").equalsIgnoreCase("true") && ((craft.type.listenMovement == false) || ((p != null) && (e != p)))) {
				movePlayer(e, dx, dy, dz);
			} else {
				teleportPlayer(e, dx, dy, dz);
			}
		}

		for (Chunk checkChunk : craft.checkedChunks) {
			craft.world.refreshChunk(checkChunk.getX(), checkChunk.getZ());
		}

		if (!playerTeleports.isEmpty()) {
			teleportUpdate();
		}

		craft.minX += dx;
		craft.minY += dy;
		craft.minZ += dz;
		craft.maxX = (craft.minX + craft.sizeX) - 1;
		craft.maxY = (craft.minY + craft.sizeY) - 1;
		craft.maxZ = (craft.minZ + craft.sizeZ) - 1;

		// adjust water level
		// if(craft.waterLevel <= -1)

		if ((craft.waterLevel == (craft.sizeY - 1)) && (craft.newWaterLevel < craft.waterLevel)) {
			craft.waterLevel = craft.newWaterLevel;
		} else if ((craft.waterLevel <= -1) && (craft.newWaterLevel > craft.waterLevel)) {
			craft.waterLevel = craft.newWaterLevel;
		} else if ((craft.waterLevel >= 0) && (craft.waterLevel < (craft.sizeY - 1))) {
			craft.waterLevel -= dy;
		}
		// craft.waterLevel = 63;

		craft.lastMove = System.currentTimeMillis();

		/*if (craft.type.requiresRails) {
			int xMid = craft.matrix.length / 2;
			int zMid = craft.matrix[0][0].length / 2;

			// Block belowBlock = world.getBlockAt(posX + xMid, posY - 1, posZ + zMid);
			Block belowBlock = getWorldBlock(xMid, -1, zMid);
			craft.railBlock = belowBlock;

			if (belowBlock.getType() == Material.RAILS) {
				railMove();
			}
		}*/

	}

	// scan to know if any of the craft blocks are now missing (blocks removed, TNT damage, creeper ?)
	// and update the structure
	@SuppressWarnings("deprecation")
	public void structureUpdate(Player causer, boolean scheduled) {
		short craftBlockId;
		int blockId;

		if (craft == null) { return; }

		if (craft.matrix == null) { return; }

		craft.signLoc = null;

		craft.radioSignLoc = null;

		for (int i : craft.engineIDLocs.keySet()) {
			craft.engineIDLocs.put(i, null);
		}
		craft.currentEngineCount = 0;

		if (!scheduled) {

			craft.recentlyUpdated = true;
		}

		if (causer != null) {
			craft.lastCauser = causer;
		}
		
		if( craft.weightCurrent > craft.weightStart )
			craft.weightStart = craft.weightCurrent;

		craft.lastUpdate = System.currentTimeMillis();

		if (!craft.leftSafeDock && !NavyCraft.checkSpawnRegion(new Location(craft.world, craft.minX, craft.minY, craft.minZ)) && !NavyCraft.checkSpawnRegion(new Location(craft.world, craft.maxX, craft.maxY, craft.maxZ))) {
			craft.leftSafeDock = true;
			if (PermissionInterface.CheckEnabledWorld(craft.getLocation()) && !craft.doCost) {
				if (craft.captainName != null) {
					Player p = plugin.getServer().getPlayer(craft.captainName);
					if ((p != null) && craft.isNameOnBoard.get(craft.captainName)) {
						Essentials ess;
						ess = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
						if (ess == null) {
							p.sendMessage("Essentials Economy error");
							return;
						}
						if (!PermissionInterface.CheckQuietPerm(p, "navycraft.free") && ess.getUser(p).canAfford(new BigDecimal(craft.vehicleCost))) {
							p.sendMessage("Vehicle purchased.");
							ess.getUser(p).takeMoney(new BigDecimal(craft.vehicleCost));

						} else if (PermissionInterface.CheckQuietPerm(p, "navycraft.free")) {
							p.sendMessage("Vehicle given for free!");
						} else {
							p.sendMessage(ChatColor.RED + "You cannot afford this vehicle, destroying vehicle.");
							craft.doDestroy = true;
							return;
						}
					} else {
						if (!craft.isAutoCraft) {
							craft.doDestroy = true;
							return;
						}
					}
				} else {
					if (!craft.isAutoCraft) {
						craft.doDestroy = true;
						return;
					}
				}
			}

		} else if (!craft.isDestroying && craft.leftSafeDock && (NavyCraft.checkSpawnRegion(new Location(craft.world, craft.minX, craft.minY, craft.minZ)) || NavyCraft.checkSpawnRegion(new Location(craft.world, craft.maxX, craft.maxY, craft.maxZ)))) {
			if (craft.driverName != null) {
				Player p = plugin.getServer().getPlayer(craft.driverName);
				p.sendMessage(ChatColor.RED + "Reentering a safe dock area is not permitted! Vehicle Destroyed!");
			}
			craft.doDestroy = true;
			return;

		}

		float displacement = 0.0f;
		craft.airDisplacement = 0.0f;
		craft.blockDisplacement = 0.0f;
		craft.ballastDisplacement = 0.0f;
		craft.buoyancy = 0;

		for (int y = 0; y < craft.sizeY; y++) {
			for (int x = 0; x < craft.sizeX; x++) {
				for (int z = 0; z < craft.sizeZ; z++) {
					craftBlockId = craft.matrix[x][y][z];
					Block newBlock = craft.world.getBlockAt(craft.minX + x, craft.minY + y, craft.minZ + z);
					blockId = newBlock.getTypeId();

					if (!newBlock.getChunk().isLoaded()) {
						newBlock.getChunk().load();
					}

					if (craft.type.canNavigate || craft.type.canDive) {
						if ((newBlock.getY() < 63) && !((blockId >= 8) && (blockId <= 11))) {
							float yScale = craft.sizeY / 3.0f;
							if ((blockId == 0) && (y < yScale)) {
								float disp = (craft.airDispValue * (yScale - y)) / (yScale);
								if (disp < craft.minDispValue) {
									disp = craft.minDispValue;
								}
								displacement += disp;
								craft.airDisplacement += disp;

							} else if (blockId == 121) {
								// float disp = craft.airDispValue * ( craft.sizeY - y ) / (craft.sizeY);
								float disp = craft.airDispValue;
								if (disp < 1) {
									disp = 1;
								}
								disp = (craft.ballastAirPercent - 100) * .01f * disp;
								displacement += disp;
								craft.ballastDisplacement += disp;
							} else if (y < yScale) {
								float disp = (craft.blockDispValue * (yScale - y)) / (yScale);
								if (disp < craft.minDispValue) {
									disp = craft.minDispValue;
								}
								displacement += disp;
								craft.blockDisplacement += disp;
							} else {
								displacement += craft.minDispValue;
								if (blockId == 0) {
									craft.airDisplacement += craft.minDispValue;
								} else {
									craft.blockDisplacement += craft.minDispValue;
								}
							}
						} else if ((newBlock.getY() >= 63) && (blockId == 121)) {
							float disp = craft.airDispValue;
							if (disp < 1) {
								disp = 1;
							}
							disp = (craft.ballastAirPercent - 100) * .01f * disp;
							displacement += disp;
							craft.ballastDisplacement += disp;
						}

					}
					// remove blocks from the structure if it is not there anymore
					if ((craftBlockId == -1) || (craftBlockId == 0) || ((craftBlockId >= 8) && (craftBlockId <= 11))) {
						if (craft.isRepairing) {
							blockId = craft.matrix[x][y][z];
							if (blockId != -1) {
								newBlock.setTypeId(blockId);
							}
						} else if ((blockId != 0) && !((blockId >= 8) && (blockId <= 11)) && (blockId != 79)) {
							if (craft.driverName != null) {
								if (BlocksInfo.isDataBlock(blockId)) {
									craft.dataBlocks.add(new DataBlock(blockId, x, y, z, newBlock.getData()));
								}
							}

						} else if (blockId == 9) {

							if (newBlock.getY() > 62) {
								newBlock.setTypeId(0);
							} else {
								if (craftBlockId == 0) {
									craft.matrix[x][y][z] = -1;
								}

								if (scheduled && ((craft.buoyFloodTicker == 1) || (craft.buoyFloodTicker == 5))) {
									newBlock.setTypeIdAndData(8, newBlock.getData(), true);
								}
							}
						} else if (blockId == 8) {
							if (newBlock.getY() > 62) {
								newBlock.setTypeId(0);
							} else {
								if (craftBlockId == 0) {
									craft.matrix[x][y][z] = -1;
								}

								if (scheduled && ((craft.buoyFloodTicker == 1) || (craft.buoyFloodTicker == 5))) {
									newBlock.setData((byte) 0);
								}
							}
						} else if (blockId == 0) {
							craft.matrix[x][y][z] = 0;
						}
					} else {
						if ((craftBlockId != blockId) && (blockId != 0) && !((blockId >= 8) && (blockId <= 11))) {
							if (craft.isRepairing || craft.type.canZamboni) {
								blockId = craft.matrix[x][y][z];
								if (blockId != -1) {
									newBlock.setTypeId(blockId);
								}
							} else {
								for (short bkId : craft.type.structureBlocks) {
									if (blockId == bkId) {
										craft.matrix[x][y][z] = (short) blockId;
										break;
									}
								}
							}

							if (blockId == 74) {
								newBlock.setTypeId(73);
							}
						}

						// regenerate TNT on a bomber
						/*if ((craftBlockId == 46) && craft.type.bomber) {
							continue;
						}*/

						if ((craftBlockId == 87) && (blockId == 87)) {					
							craft.matrix[x][y][z] = (short) 35;
							craft.dataBlocks.add(new DataBlock(blockId, x, y, z, 4));
							newBlock.setTypeId(35);
							newBlock.setData((byte) 0x4);
						}

						if (blockId == 68) {
							signUpdates(newBlock);
						} // end if sign

						if (craft.doCost && (blockId == 23)) {
							OneCannon oc = new OneCannon(newBlock.getLocation(), NavyCraft.instance);

							Dispenser dispenser = (Dispenser) newBlock.getState();
							Inventory inventory = dispenser.getInventory();

							if (oc.isValidCannon(newBlock) && ((inventory.getItem(4) == null) || (inventory.getItem(4).getTypeId() != 388))) {
								int cost = 0;
								AimCannon.cannons.add(oc);
								if (oc.cannonType == 0) {
									cost = 100;
								} else if (oc.cannonType == 1) {
									cost = 250;
								} else if (oc.cannonType == 3) {
									cost = 600;
								} else if (oc.cannonType == 4) {
									cost = 250;
								} else if (oc.cannonType == 5) {
									cost = 1250;
								} else if (oc.cannonType == 6) {
									cost = 2000;
								} else if (oc.cannonType == 7) {
									cost = 2000;
								} else if (oc.cannonType == 8) {
									cost = 250;
								} else if (oc.cannonType == 9) {
									cost = 100;
								}
								craft.vehicleCost += cost;
								initWeaponDispensers.add(newBlock);
							}
						}

						// block is not here anymore, remove it
						if ((blockId == 0) || ((blockId >= 8) && (blockId <= 11))) {
							int data = 0;
							if (BlocksInfo.isDataBlock(craftBlockId)) {
								for (DataBlock db : craft.dataBlocks) {
									if (db.locationMatches(x, y, z) && (db.id == craftBlockId)) {
										data = db.data;
									}
								}
							}
							// repair washed out ladders, torches, and doors
							if (((blockId == 0) && ((craftBlockId == 50) || (craftBlockId == 75) || (craftBlockId == 76) || (craftBlockId == 65) || (craftBlockId == 68) || (craftBlockId == 63) || (craftBlockId == 69) || (craftBlockId == 77) || (craftBlockId == 70) || (craftBlockId == 72) || (craftBlockId == 55) || (craftBlockId == 143) || (craftBlockId == 64) || (craftBlockId == 71))) && ((Craft.getAttachedBlock(newBlock, craftBlockId, data) != null) && (Craft.getAttachedBlock(newBlock, craftBlockId, data).getTypeId() != 0) && !((Craft.getAttachedBlock(newBlock, craftBlockId, data).getTypeId() >= 8) && (Craft.getAttachedBlock(newBlock, craftBlockId, data).getTypeId() <= 11)))) {

								if (BlocksInfo.isDataBlock(craftBlockId)) {
									for (DataBlock db : craft.dataBlocks) {
										if (db.locationMatches(x, y, z) && (db.id == craftBlockId)) {
											newBlock.setTypeIdAndData(craftBlockId, (byte) db.data, true);
											break;
										}
									}
								} else if (BlocksInfo.isComplexBlock(craftBlockId)) {
									for (DataBlock complexBlock : craft.complexBlocks) {
										if (complexBlock.locationMatches(x, y, z) && (complexBlock.id == craftBlockId)) {
											newBlock.setTypeIdAndData(craftBlockId, (byte) complexBlock.data, true);

											if ((complexBlock.id == 63) || (complexBlock.id == 68)) {
												NavyCraft.instance.DebugMessage("Restoring a sign.", 4);
												setBlock(complexBlock.id, newBlock);
												// theBlock.setcraft.typeId(complexBlock.id);
												newBlock.setData((byte) complexBlock.data);
												Sign sign = (Sign) newBlock.getState();

												sign.setLine(0, complexBlock.signLines[0]);
												sign.setLine(1, complexBlock.signLines[1]);
												sign.setLine(2, complexBlock.signLines[2]);
												sign.setLine(3, complexBlock.signLines[3]);

												sign.update();

											}
											break;
										}
									}
								} else {
									newBlock.setTypeId(craftBlockId);
								}
							} else // remove block
							{

								craft.weightCurrent -= Craft.blockWeight(craftBlockId);

								// air, water, or lava
								if ((craft.waterType != 0) && ((craft.minY + y) <= 62)) {
									craft.matrix[x][y][z] = 0;
								} else {
									craft.matrix[x][y][z] = 0; // make a hole in the craft
								}

								craft.blockCount--;

								for (DataBlock complexBlock : craft.complexBlocks) {
									if (complexBlock.locationMatches(x, y, z)) {
										craft.complexBlocks.remove(complexBlock);
										break;
									}
								}
								for (DataBlock dataBlock : craft.dataBlocks) {
									if (dataBlock.locationMatches(x, y, z)) {
										craft.dataBlocks.remove(dataBlock);
										break;
									}
								}

								NavyCraft.instance.DebugMessage("Removing a block of type " + craftBlockId + " because of type " + blockId, 4);
								// }
							}

						}
					}
				}
			}
		}

		if (craft.type.canNavigate || craft.type.canDive) {
			updateBuoyancy(displacement);
			if (!craft.isMoving && scheduled && (craft.buoyancy != 0) && (((craft.buoyFloodTicker == 3) && (Math.abs((displacement - (craft.weightStart * craft.weightMult)) / (craft.weightStart * craft.weightMult)) > 0.3f)) || (craft.buoyFloodTicker == 7))) {
				if ((craft.waitTorpLoading == 0) && !craft.launcherOn) {
					delayed_move(0, calculateBuoyancyMove(), 0);
				}
			}
			if (scheduled || (craft.gear == 3)) {
				craft.buoyFloodTicker = (craft.buoyFloodTicker + 1) % 8;
				if ((craft.buoyFloodTicker % 2) == 0) {
					for (Entity e : craft.checkEntities) {
						if (e instanceof Player) {
							Player p = (Player) e;
							p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_SWIM, 0.6f, 0.4f + ((0.2f * craft.speed) / 12.0f));
						}
					}
				}
			}
		}

		if (craft.doCost) {
			if (craft.type.discount == 100) 
				craft.vehicleCost = 0;
			else if(craft.type.discount > 0)
				craft.vehicleCost = (int)((float)craft.vehicleCost * (float)craft.type.discount * .01f);
			
			if (NavyCraft.checkSpawnRegion(craft.getLocation())) {
				if (craft.captainName != null) {
					Player p = plugin.getServer().getPlayer(craft.captainName);
					if (p != null) {
						Essentials ess;
						ess = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
						if (ess == null) {
							p.sendMessage("Essentials Economy error");
							return;
						}
						if (!ess.getUser(p).canAfford(new BigDecimal(craft.vehicleCost))) {
							p.sendMessage(ChatColor.YELLOW + "You cannot afford this vehicle:" + ChatColor.RED + "$" + craft.vehicleCost + ChatColor.YELLOW + ". It will be destroyed if driven outside of the dock region.");
						} else {
							p.sendMessage(ChatColor.YELLOW + "This vehicle will cost:" + ChatColor.GREEN + "$" + craft.vehicleCost + ChatColor.YELLOW + " if driven outside of the dock region.");
						}

					}
				}
				if (!initWeaponDispensers.isEmpty()) {
					for (Block b : initWeaponDispensers) {
						Dispenser dispenser = (Dispenser) b.getState();
						Inventory inventory = dispenser.getInventory();
						inventory.setItem(4, new ItemStack(388, 1));
					}
					initWeaponDispensers.clear();
				}
			}
			craft.doCost = false;
		}

		if (craft.signLoc == null) {
			craft.helmDestroyed = true;
		} // else


		if (craft.blockCount < craft.lastBlockCount) {
			if (causer != null) {
				if (craft.damagers.containsKey(causer)) {
					craft.damagers.put(causer, craft.damagers.get(causer) + (craft.lastBlockCount - craft.blockCount));
				} else {
					craft.damagers.put(causer, craft.lastBlockCount - craft.blockCount);
				}
			} else {
				// round off uncredited damage to last causer
				if (craft.lastCauser != null) {
					if (craft.damagers.containsKey(craft.lastCauser)) {
						craft.damagers.put(craft.lastCauser, craft.damagers.get(craft.lastCauser) + (craft.lastBlockCount - craft.blockCount));
					} else {
						craft.damagers.put(craft.lastCauser, craft.lastBlockCount - craft.blockCount);
					}
				} else {
					craft.uncreditedDamage += craft.lastBlockCount - craft.blockCount;
				}
			}

		}

		craft.lastBlockCount = craft.blockCount;

		if (!craft.sinking && (craft.type.canNavigate || craft.type.canDive) && !craft.type.isTerrestrial) {

			if ((((float) craft.blockCount / (float) craft.blockCountStart) < .8) || craft.doSink || (!craft.type.canDive && (craft.maxY < 63))) {
				if (NavyCraft.checkSafeDockRegion(craft.getLocation())) {
					for (String s : craft.crewNames) {
						Player p = plugin.getServer().getPlayer(s);
						if (p != null) {
							p.sendMessage(ChatColor.RED + "***Vehicle destroyed in safedock***");
						}

					}
					craft.doDestroy = true;
					return;
				}

				craft.doSink = false;
				craft.sinking = true;
				sinkingThread();

				for (String s : craft.crewNames) {
					Player p = plugin.getServer().getPlayer(s);
					if (p != null) {
						p.sendMessage(ChatColor.RED + "***We're sinking!***");
						p.sendMessage(ChatColor.RED + "***All Hands Abandon Ship!***");
					}

				}

				sinkBroadcast();

			}
		} else if (!craft.sinking && (craft.type.canFly)) {
			if ((((float) craft.blockCount / (float) craft.blockCountStart) < .8) || craft.doSink) {
				if (NavyCraft.checkSafeDockRegion(craft.getLocation())) {
					for (String s : craft.crewNames) {
						Player p = plugin.getServer().getPlayer(s);
						if (p != null) {
							p.sendMessage(ChatColor.RED + "***Vehicle destroyed in safedock***");
						}

					}
					craft.doDestroy = true;
					return;
				}
				craft.doSink = false;
				craft.sinking = true;
				sinkingThread();

				for (String s : craft.crewNames) {
					Player p = plugin.getServer().getPlayer(s);
					if (p != null) {
						p.sendMessage(ChatColor.RED + "***We're Crashing!***");
						p.sendMessage(ChatColor.RED + "***Bail Out!***");
					}
				}

				sinkBroadcast();
			}
		} else if (!craft.sinking && (craft.type.isTerrestrial)) {
			if ((((float) craft.blockCount / (float) craft.blockCountStart) < .8) || craft.doSink) {
				if (NavyCraft.checkSafeDockRegion(craft.getLocation())) {
					for (String s : craft.crewNames) {
						Player p = plugin.getServer().getPlayer(s);
						if (p != null) {
							p.sendMessage(ChatColor.RED + "***Vehicle destroyed in safedock***");
						}

					}
					craft.doDestroy = true;
					return;
				}
				craft.doSink = false;
				craft.sinking = true;
				sinkingThread();

				for (String s : craft.crewNames) {
					Player p = plugin.getServer().getPlayer(s);
					if (p != null) {
						p.sendMessage(ChatColor.RED + "***Vehicle Destroyed!***");
						p.sendMessage(ChatColor.RED + "***Bail Out!***");
					}
				}

				sinkBroadcast();

			}
		}
	}

	public void updateBuoyancy(float displacement) {
		if (displacement < (craft.weightStart * craft.weightMult)) {
			if (craft.type.canDive && (displacement < (craft.weightStart * craft.weightMult * 0.9f))) {

				craft.buoyancy = -1;
			} else if (!craft.type.canDive) {

				craft.buoyancy = -1;
			} else {
				craft.buoyancy = 0;
			}
		} else {

			if (craft.type.canDive && (displacement > (craft.weightStart * craft.weightMult * 1.1f))) {

				craft.buoyancy = 1;
			} else if (!craft.type.canDive) {

				craft.buoyancy = 1;
			} else {
				craft.buoyancy = 0;
			}
		}

		craft.displacement = displacement;

		if (craft.ballastMode == 1) {
			craft.ballastAirPercent -= 2;
			if (craft.ballastAirPercent < 0) {
				craft.ballastAirPercent = 0;
			}
		} else if ((craft.ballastMode == 2) && (craft.ballastAirPercent < 100)) {
			craft.ballastAirPercent += 2;
			if (craft.ballastAirPercent > 100) {
				craft.ballastAirPercent = 100;
			}
		} else if (craft.ballastMode == 3) {
			if (craft.displacement > (craft.weightStart * 1.05f)) {
				craft.ballastAirPercent -= 2;
				if (craft.ballastAirPercent < 0) {
					craft.ballastAirPercent = 0;
				}
			} else if (craft.displacement < (craft.weightStart * 0.95f)) {
				craft.ballastAirPercent += 2;
				if (craft.ballastAirPercent > 100) {
					craft.ballastAirPercent = 100;
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void signUpdates(Block newBlock) {
		Sign sign = (Sign) newBlock.getState();

		String craftTypeName = sign.getLine(0).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");

		// remove brackets
		if (craftTypeName.startsWith("[")) {
			craftTypeName = craftTypeName.substring(1, craftTypeName.length() - 1);
		}

		if (craftTypeName.equalsIgnoreCase("periscope")) {

			int periscopeId = -1;
			String periscopeIdStr = sign.getLine(1).trim().toLowerCase();
			periscopeIdStr = periscopeIdStr.replaceAll(ChatColor.BLUE.toString(), "");
			if (periscopeIdStr.length() >= 3) {
				periscopeIdStr = periscopeIdStr.substring(2, 3);
			}
			String upDownStr = sign.getLine(3).trim().toLowerCase();
			upDownStr = upDownStr.replaceAll(ChatColor.BLUE.toString(), "");
			boolean signUp = false;
			boolean destroyed = false;
			if (upDownStr.equalsIgnoreCase("up")) {
				signUp = true;
			} else if (upDownStr.equalsIgnoreCase("destroyed")) {
				destroyed = true;
			}
			if (!periscopeIdStr.isEmpty()) {
				try {
					periscopeId = Integer.parseInt(periscopeIdStr);
				} catch (NumberFormatException nfe) {
					periscopeId = -1;
				}
			}

			if (periscopeId != -1) {
				for (Periscope p : craft.periscopes) {
					if (p.periscopeID == periscopeId) {

						p.signLoc = sign.getBlock().getLocation();
						if (!p.destroyed) {
							for (int k = 1; k <= 20; k++) {
								boolean found = false;
								for (int i = -3; i <= 3; i++) {
									if ((newBlock.getRelative(BlockFace.UP, k).getRelative(BlockFace.EAST, i).getTypeId() == 41) && (newBlock.getRelative(BlockFace.UP, k + 1).getRelative(BlockFace.EAST, i).getTypeId() == 113)) {

										found = true;
										p.scopeLoc = newBlock.getRelative(BlockFace.UP, k + 6).getRelative(BlockFace.EAST, i).getLocation();
										if (p.raised) {
											if ((newBlock.getRelative(BlockFace.UP, k + 2).getRelative(BlockFace.EAST, i).getTypeId() == 113) && (newBlock.getRelative(BlockFace.UP, k + 3).getRelative(BlockFace.EAST, i).getTypeId() == 113) && (newBlock.getRelative(BlockFace.UP, k + 4).getRelative(BlockFace.EAST, i).getTypeId() == 113) && (newBlock.getRelative(BlockFace.UP, k + 5).getRelative(BlockFace.EAST, i).getTypeId() == 113)) {
												if (!signUp) {
													sign.setLine(1, "||" + periscopeId + "||");
													sign.setLine(2, "--|(o)|--");
													sign.setLine(3, "UP");
													sign.update();
												}
											} else {

												p.destroyed = true;
											}
										} else if (signUp) {
											sign.setLine(1, "||" + periscopeId + "||");
											sign.setLine(2, "|| ||");
											sign.setLine(3, "DOWN");
											sign.update();
										}
										break;
									}
								}
								if (!found) {
									for (int j = -3; j <= 3; j++) {
										if ((newBlock.getRelative(BlockFace.UP, k).getRelative(BlockFace.NORTH, j).getTypeId() == 41) && (newBlock.getRelative(BlockFace.UP, k + 1).getRelative(BlockFace.NORTH, j).getTypeId() == 113)) {

											found = true;
											p.scopeLoc = newBlock.getRelative(BlockFace.UP, k + 6).getRelative(BlockFace.NORTH, j).getLocation();
											if (p.raised) {
												if ((newBlock.getRelative(BlockFace.UP, k + 2).getRelative(BlockFace.NORTH, j).getTypeId() == 113) && (newBlock.getRelative(BlockFace.UP, k + 3).getRelative(BlockFace.NORTH, j).getTypeId() == 113) && (newBlock.getRelative(BlockFace.UP, k + 4).getRelative(BlockFace.NORTH, j).getTypeId() == 113) && (newBlock.getRelative(BlockFace.UP, k + 5).getRelative(BlockFace.NORTH, j).getTypeId() == 113)) {
													if (!signUp) {
														sign.setLine(1, "||" + periscopeId + "||");
														sign.setLine(2, "--|(o)|--");
														sign.setLine(3, "UP");
														sign.update();
													}
												} else {

													p.destroyed = true;
												}
											} else if (signUp) {
												sign.setLine(1, "||" + periscopeId + "||");
												sign.setLine(2, "|| ||");
												sign.setLine(3, "DOWN");
												sign.update();
											}
										}
									}
								}
								if (found) {
									break;
								}
							}
						}

						if (p.destroyed && !destroyed) {
							sign.setLine(1, "||" + periscopeId + "||");
							sign.setLine(2, "--|(X)|--");
							sign.setLine(3, "DESTROYED");
							sign.update();
						}
					}
				}
			}
		} else if (craftTypeName.equalsIgnoreCase("nav")) {

			String line2;
			if ((craft.rotation == 0) || (craft.rotation == 360)) {
				line2 = "HDG=000";
			} else if (craft.rotation == 90) {
				line2 = "HDG=090";
			} else if (craft.rotation == 180) {
				line2 = "HDG=180";
			} else {
				line2 = "HDG=270";
			}

			int jj = craft.minY - 1;
			int loop = 0;
			int duk = -1;
			while ((jj > 0) && (duk == -1)) {
				for (int i = craft.minX; i <= craft.maxX; i++) {
					for (int k = craft.minZ; k <= craft.maxZ; k++) {
						if (detectableMat(craft.world.getBlockAt(i, jj, k).getTypeId())) {
							duk = loop;
						}
					}
				}
				jj = jj - 1;
				loop++;
			}
			String dukString;
			if (duk == -1) {
				dukString = loop + "+";
			} else {
				dukString = "" + duk;
			}
			String line3 = "Fathometer:" + dukString;

			String line1 = "Grid: ";
			if (PermissionInterface.CheckEnabledWorld(craft.getLocation())) {
				for (int i = 0; i < 2; i++) {
					int cord = 0;
					if (i == 0) {
						cord = craft.getLocation().getBlockZ();
					} else {
						cord = craft.getLocation().getBlockX();
					}
					if ((cord >= -3000) && (cord < -2400)) {
						line1 += "A";
					} else if ((cord >= -2400) && (cord < -1800)) {
						line1 += "B";
					} else if ((cord >= -1800) && (cord < -1200)) {
						line1 += "C";
					} else if ((cord >= -1200) && (cord < -600)) {
						line1 += "D";
					} else if ((cord >= -600) && (cord < 0)) {
						line1 += "E";
					} else if ((cord >= 0) && (cord < 600)) {
						line1 += "F";
					} else if ((cord >= 600) && (cord < 1200)) {
						line1 += "G";
					} else if ((cord >= 1200) && (cord < 1800)) {
						line1 += "H";
					} else if ((cord >= 1800) && (cord < 2400)) {
						line1 += "I";
					} else if ((cord >= 2400) && (cord <= 3000)) {
						line1 += "J";
					} else {
						line1 += "Unknown";
					}
				}

				int xShift = 0;
				int cord = craft.getLocation().getBlockX();
				if (cord < 0) {
					if ((Math.abs(cord) % 600) > 400) {
						xShift = 1;
					} else if ((Math.abs(cord) % 600) > 200) {
						xShift = 2;
					} else {
						xShift = 3;
					}
				} else {
					if ((Math.abs(cord) % 600) > 400) {
						xShift = 3;
					} else if ((Math.abs(cord) % 600) > 200) {
						xShift = 2;
					} else {
						xShift = 1;
					}
				}

				int zShift = 0;
				cord = craft.getLocation().getBlockZ();
				if (cord < 0) {
					if ((Math.abs(cord) % 600) > 400) {
						zShift = 3;
					} else if ((Math.abs(cord) % 600) > 200) {
						zShift = 2;
					} else {
						zShift = 1;
					}
				} else {
					if ((Math.abs(cord) % 600) > 400) {
						zShift = 1;
					} else if ((Math.abs(cord) % 600) > 200) {
						zShift = 2;
					} else {
						zShift = 3;
					}
				}

				line1 += "-";
				if ((xShift == 1) && (zShift == 1)) {
					line1 += "1";
				} else if ((xShift == 1) && (zShift == 2)) {
					line1 += "4";
				} else if ((xShift == 1) && (zShift == 3)) {
					line1 += "7";
				} else if ((xShift == 2) && (zShift == 1)) {
					line1 += "2";
				} else if ((xShift == 2) && (zShift == 2)) {
					line1 += "5";
				} else if ((xShift == 2) && (zShift == 3)) {
					line1 += "8";
				} else if ((xShift == 3) && (zShift == 1)) {
					line1 += "3";
				} else if ((xShift == 3) && (zShift == 2)) {
					line1 += "6";
				} else if ((xShift == 3) && (zShift == 3)) {
					line1 += "9";
				}
			}

			sign.setLine(1, line1);
			sign.setLine(2, line2);
			sign.setLine(3, line3);
			sign.update();
		} else if (craftTypeName.equalsIgnoreCase("helm")) {
			if (craft.signLoc == null) {
				craft.signLoc = sign.getBlock().getLocation();
			}
			if (craft.driverName != null) {
				if (craft.driverName.length() > 15) {
					sign.setLine(3, craft.driverName.substring(0, 14));
				} else {
					sign.setLine(3, craft.driverName);
				}

			} else {
				sign.setLine(3, "No Driver");
			}

			String line1;
			if ((craft.rotation == 0) || (craft.rotation == 360)) {
				line1 = "HDG=000";
			} else if (craft.rotation == 90) {
				line1 = "HDG=090";
			} else if (craft.rotation == 180) {
				line1 = "HDG=180";
			} else {
				line1 = "HDG=270";
			}

			if (craft.rudder == -1) {
				if (craft.turnProgress > 0) {
					line1 = line1 + " <<|";
				} else {
					line1 = line1 + " <|";
				}
			} else if (craft.rudder == 1) {
				if (craft.turnProgress > 0) {
					line1 = line1 + " |>>";
				} else {
					line1 = line1 + " |>";
				}
			} else {
				line1 = line1 + " ||";
			}
			line1 = line1 + " G=" + craft.gear;
			sign.setLine(1, line1);

			String line2;
			line2 = "SP=" + craft.speed + "(" + craft.setSpeed + ")";
			if (craft.type.canDive) {
				line2 = line2 + "DK=" + (63 - craft.minY);
			} else if (craft.type.canFly) {
				line2 = line2 + "AL=" + (craft.minY - 63);
			}
			if (craft.vertPlanes == 1) {
				line2 = line2 + " ^";
			} else if (craft.vertPlanes == -1) {
				line2 = line2 + " v";
			}
			sign.setLine(2, line2);
			sign.update();

		} else if (craftTypeName.equalsIgnoreCase("subdrive")) {
			String tubeString = sign.getLine(1).trim().toLowerCase();
			tubeString = tubeString.replaceAll(ChatColor.BLUE.toString(), "");

			boolean dieselShown = false;
			boolean electricShown = false;
			if (tubeString.equalsIgnoreCase("diesel")) {
				dieselShown = true;
			}
			if (tubeString.equalsIgnoreCase("electric")) {
				electricShown = true;
			}

			if (craft.submergedMode && !electricShown) {
				sign.setLine(1, "ELECTRIC");
				sign.setLine(2, "(DIVE)");
				sign.update();
			} else if (!craft.submergedMode && !dieselShown) {
				sign.setLine(1, "DIESEL");
				sign.setLine(2, "(SURFACE)");
				sign.update();
			}
		} else if (craftTypeName.equalsIgnoreCase("ballasttanks")) {
			boolean doUpdate = false;

			String scaleString = sign.getLine(1).trim().toLowerCase();
			scaleString = scaleString.replaceAll(ChatColor.BLUE.toString(), "");

			if (!scaleString.equalsIgnoreCase("E---------F")) {

				sign.setLine(1, "E---------F");
				doUpdate = true;
			}

			int percentInd = (int) (14.0f * craft.ballastAirPercent * .01f);
			String indicatorString = "";
			int index = 14 - percentInd;

			for (int i = 0; i < 15; i++) {
				if (i == index) {
					indicatorString += "|";
				} else {
					indicatorString += " ";
				}
			}

			String currentIndString = sign.getLine(2).trim().toLowerCase();
			currentIndString = currentIndString.replaceAll(ChatColor.BLUE.toString(), "");
			if (!currentIndString.equalsIgnoreCase(indicatorString)) {
				sign.setLine(2, indicatorString);
				doUpdate = true;
			}

			String modeString = "";
			if (craft.ballastMode == 0) {
				modeString = "Closed";
			} else if (craft.ballastMode == 1) {
				modeString = "Flood";
			} else if (craft.ballastMode == 2) {
				modeString = "Blow";
			} else if (craft.ballastMode == 3) {
				modeString = "Equalize";
			}

			String currentModeString = sign.getLine(3).trim().toLowerCase();
			currentModeString = currentModeString.replaceAll(ChatColor.BLUE.toString(), "");
			if (!currentModeString.equalsIgnoreCase(modeString)) {
				sign.setLine(3, modeString);
				doUpdate = true;
			}

			if (doUpdate) {
				sign.update();
			}

		} else if (craftTypeName.equalsIgnoreCase("buoyancy")) {
			boolean doUpdate = false;

			String weightString = "Wt:" + craft.weightStart;
			if (weightString.length() > 15) {
				weightString.substring(0, 14);
			}
			String curWeightString = sign.getLine(1).trim().toLowerCase();
			curWeightString = curWeightString.replaceAll(ChatColor.BLUE.toString(), "");
			if (!curWeightString.equalsIgnoreCase(weightString)) {

				sign.setLine(1, weightString);
				doUpdate = true;
			}

			String dispString = "Dis:" + craft.displacement;
			if (dispString.length() > 15) {
				dispString.substring(0, 14);
			}
			String curDispString = sign.getLine(2).trim().toLowerCase();
			curDispString = curDispString.replaceAll(ChatColor.BLUE.toString(), "");
			if (!curDispString.equalsIgnoreCase(dispString)) {

				sign.setLine(2, dispString);
				doUpdate = true;
			}

			String ballastString = "Bal:" + craft.ballastDisplacement;
			if (ballastString.length() > 15) {
				ballastString.substring(0, 14);
			}
			String curBallastString = sign.getLine(3).trim().toLowerCase();
			curBallastString = curBallastString.replaceAll(ChatColor.BLUE.toString(), "");
			if (!curBallastString.equalsIgnoreCase(ballastString)) {

				sign.setLine(3, ballastString);
				doUpdate = true;
			}

			if (doUpdate) {
				sign.update();
			}

		} else if (craftTypeName.equalsIgnoreCase("launcher")) {
			String armedString = sign.getLine(1).trim().toLowerCase();
			armedString = armedString.replaceAll(ChatColor.BLUE.toString(), "");

			boolean armedShown = false;
			boolean disarmedShown = false;
			if (armedString.equalsIgnoreCase("armed")) {
				armedShown = true;
			}
			if (armedString.equalsIgnoreCase("disarmed")) {
				disarmedShown = true;
			}

			if (craft.launcherOn && !armedShown) {
				sign.setLine(1, "ARMED");
				sign.update();
			}
			if (!craft.launcherOn && !disarmedShown) {
				sign.setLine(1, "DISARMED");
				sign.update();
			}

		} else if (craftTypeName.equalsIgnoreCase("radio")) {
			craft.radioSignLoc = sign.getLocation();
			String onString = sign.getLine(1).trim().toLowerCase();
			String freqString = sign.getLine(2).trim().toLowerCase();
			freqString = freqString.replaceAll(ChatColor.BLUE.toString(), "");
			String pointerString = sign.getLine(3).trim().toLowerCase();
			pointerString = pointerString.replaceAll(ChatColor.BLUE.toString(), "");

			String newOnString = "";
			if ((craft.maxY >= 63) && craft.radioSetOn) {
				newOnString = "ON";
			} else if (craft.maxY >= 63) {
				newOnString = "OFF";
			} else {
				newOnString = "DISABLED";
			}
			String newFreqString = "" + craft.radio1 + " " + craft.radio2 + " " + craft.radio3 + " " + craft.radio4;
			String newPointerString = "";
			if (craft.radioSelector == 1) {
				newPointerString = "^        ";
			} else if (craft.radioSelector == 2) {
				newPointerString = "^   ";
			} else if (craft.radioSelector == 3) {
				newPointerString = "   ^";
			} else if (craft.radioSelector == 4) {
				newPointerString = "        ^";
			} else if (craft.radioSelector == 5) {
				newOnString = "> " + newOnString + " <";
			}

			if (!freqString.equalsIgnoreCase(newFreqString) || !pointerString.equalsIgnoreCase(newPointerString) || !onString.equalsIgnoreCase(newOnString)) {
				sign.setLine(1, newOnString);
				sign.setLine(2, newFreqString);
				sign.setLine(3, newPointerString);
				sign.update();
			}

		} else if (craftTypeName.equalsIgnoreCase("engine")) {

			String engineNumStr = sign.getLine(1).trim().toLowerCase();
			engineNumStr = engineNumStr.replaceAll(ChatColor.BLUE.toString(), "");
			int engNum = -1;
			if (!engineNumStr.isEmpty()) {
				try {
					engNum = Integer.parseInt(engineNumStr);
				} catch (NumberFormatException nfe) {
					engNum = -1;
				}
			}
			String engineTypeStr = sign.getLine(2).trim().toLowerCase();
			engineTypeStr = engineTypeStr.replaceAll(ChatColor.BLUE.toString(), "");
			int engType = -1;
			if (!engineTypeStr.isEmpty()) {
				if (engineTypeStr.equalsIgnoreCase("Diesel 1")) {
					engType = 0;
				}
				if (engineTypeStr.equalsIgnoreCase("Motor 1")) {
					engType = 1;
				}
				if (engineTypeStr.equalsIgnoreCase("Diesel 2")) {
					engType = 2;
				}
				if (engineTypeStr.equalsIgnoreCase("Boiler 1")) {
					engType = 3;
				}
				if (engineTypeStr.equalsIgnoreCase("Diesel 3")) {
					engType = 4;
				}
				if (engineTypeStr.equalsIgnoreCase("Gasoline 1")) {
					engType = 5;
				}
				if (engineTypeStr.equalsIgnoreCase("Boiler 2")) {
					engType = 6;
				}
				if (engineTypeStr.equalsIgnoreCase("Boiler 3")) {
					engType = 7;
				}
				if (engineTypeStr.equalsIgnoreCase("Gasoline 2")) {
					engType = 8;
				}
				if (engineTypeStr.equalsIgnoreCase("Nuclear")) {
					engType = 9;
				}
				if (engineTypeStr.equalsIgnoreCase("Airplane 1")) {
					engType = 10;
				}
				if (engineTypeStr.equalsIgnoreCase("Airplane 2")) {
					engType = 11;
				}
				if (engineTypeStr.equalsIgnoreCase("Airplane 3")) {
					engType = 12;
				}
				if (engineTypeStr.equalsIgnoreCase("Airplane 4")) {
					engType = 13;
				}
				if (engineTypeStr.equalsIgnoreCase("Airplane 7")) {
					engType = 14;
				}
				if (engineTypeStr.equalsIgnoreCase("Airplane 5")) {
					engType = 15;
				}
				if (engineTypeStr.equalsIgnoreCase("Airplane 6")) {
					engType = 16;
				}
				if (engineTypeStr.equalsIgnoreCase("Airplane 8")) {
					engType = 17;
				}
				if (engineTypeStr.equalsIgnoreCase("Tank 1")) {
					engType = 18;
				}
				if (engineTypeStr.equalsIgnoreCase("Tank 2")) {
					engType = 19;
				}
			}
			if ((engNum != -1) && (engType != -1)) {
				if (craft.engineIDLocs.containsKey(engNum)) {
					craft.currentEngineCount++;
					craft.engineIDLocs.put(engNum, sign.getLocation());
				} else {
					craft.currentEngineCount++;
					craft.engineIDLocs.put(engNum, sign.getLocation());
					craft.engineIDTypes.put(engNum, engType);
					craft.engineIDIsOn.put(engNum, false);
					craft.engineIDSetOn.put(engNum, false);
				}

				String engineOnStr = sign.getLine(3).trim().toLowerCase();
				engineOnStr = engineOnStr.replaceAll(ChatColor.BLUE.toString(), "");
				if (craft.engineIDIsOn.get(engNum)) {
					if (((engType == 0) || (engType == 2) || (engType == 4)) && craft.submergedMode && !engineOnStr.equalsIgnoreCase("ELECTRIC")) {
						sign.setLine(3, "ELECTRIC");
						sign.update();
					} else if (!engineOnStr.equalsIgnoreCase("ON") && !(((engType == 0) || (engType == 2) || (engType == 4)) && craft.submergedMode)) {
						sign.setLine(3, "ON");
						sign.update();
					}
				} else if (!craft.engineIDIsOn.get(engNum) && !engineOnStr.equalsIgnoreCase("OFF")) {
					sign.setLine(3, "OFF");
					sign.update();
				}

			}

		} else if (craftTypeName.equalsIgnoreCase("firecontrol")) {
			int tubeNum = 0;
			String line2 = "";
			String line3 = "";
			String tubeString = sign.getLine(1).trim().toLowerCase();
			tubeString = tubeString.replaceAll(ChatColor.BLUE.toString(), "");
			if (!tubeString.isEmpty()) {
				try {
					tubeNum = Integer.parseInt(tubeString);
				} catch (NumberFormatException nfe) {
					tubeNum = 0;
				}
			}
			if (tubeNum != 0) {
				if (craft.tubeFiringMode.containsKey(tubeNum)) {
					int mode = craft.tubeFiringMode.get(tubeNum);
					if (mode == -3) {
						line2 = "FIRED!";
						int display = craft.tubeFiringDisplay.get(tubeNum);
						switch (display) {

							case 0:
								line3 = "DPT=";
								if (craft.tubeFiringAuto.get(tubeNum)) {
									line3 += "AUTO";
								} else {
									line3 += "" + craft.tubeFiringDepth.get(tubeNum);
								}

								break;
							case 1:
								line3 = "ARMED=";
								if (craft.tubeFiringArmed.get(tubeNum)) {
									line3 += "ON";
								} else {
									line3 += "OFF";
								}
								break;
							case 2:
								line3 = "AUTO=";
								if (craft.tubeFiringAuto.get(tubeNum)) {
									line3 += "ON";
								} else {
									line3 += "OFF";
								}
								break;

						}
					} else if (mode == -2) {
						line2 = "Straight";
						int display = craft.tubeFiringDisplay.get(tubeNum);
						switch (display) {

							case 0:
								line3 = "DPT=" + craft.tubeFiringDepth.get(tubeNum);
								break;
							case 1:
								line3 = "ARM=" + craft.tubeFiringArm.get(tubeNum);
						}
					} else if (mode == -1) {
						line2 = "Periscope";
						int display = craft.tubeFiringDisplay.get(tubeNum);
						switch (display) {
							case 0:
								line3 = "DPT=" + craft.tubeFiringDepth.get(tubeNum);
								break;
							case 1:
								line3 = "ARM=" + craft.tubeFiringArm.get(tubeNum);
						}
					} else {
						line3 = "Target";
						int display = craft.tubeFiringDisplay.get(tubeNum);
						switch (display) {
							case 0:
								line3 = "DPT=" + craft.tubeFiringDepth.get(tubeNum);
								break;
							case 1:
								line3 = "ARM=10" + craft.tubeFiringArm.get(tubeNum);
						}
					}

				} else {
					line2 = "DISABLED.";
				}
			} else {
				line2 = "ERROR";
			}
			sign.setLine(2, line2);
			sign.setLine(3, line3);
			sign.update();

		} else if (craftTypeName.equalsIgnoreCase("tdc")) {
			String line2 = "";
			String line3 = "";
			if (craft.tubeMk1FiringDisplay > -1) {
				int mode = craft.tubeMk1FiringMode;
				if (mode == -2) {
					line2 = "Straight";
					int display = craft.tubeMk1FiringDisplay;
					switch (display) {

						case 0:
							line3 = "DPTH=" + craft.tubeMk1FiringDepth;
							break;
						case 1:
							line3 = "SPRD=" + craft.tubeMk1FiringSpread;
					}
				} else if (mode == -1) {
					line2 = "Periscope";
					int display = craft.tubeMk1FiringDisplay;
					switch (display) {
						case 0:
							line3 = "DPTH=" + craft.tubeMk1FiringDepth;
							break;
						case 1:
							line3 = "SPRD=" + craft.tubeMk1FiringSpread;
					}
				}

			} else {
				line2 = "DISABLED.";
			}
			sign.setLine(2, line2);
			sign.setLine(3, line3);
			sign.update();

		} else if (craftTypeName.equalsIgnoreCase("radar")) {
			if (craft.radarOn && !craft.sinking && (craft.maxY > 63)) {
				craft.lastRadarPulse = System.currentTimeMillis();
				HashMap<Integer, String> bearings = new HashMap<>();
				for (Craft c : Craft.craftList) {
					if ((c != craft) && (c.world == craft.world) && !c.sinking && (c.maxY > 65)) {
						float xDist = (c.minX + (c.sizeX / 2.0f)) - (craft.minX + (craft.sizeX / 2.0f));
						float zDist = (c.minZ + (c.sizeZ / 2.0f)) - (craft.minZ + (craft.sizeZ / 2.0f));
						float dist = (float) Math.sqrt((xDist * xDist) + (zDist * zDist));

						if (dist < 500) {
							double trueBearing = 0;
							if ((xDist >= 0) && (zDist < 0)) {
								double bear = Math.atan(xDist / (-zDist));
								if (bear < 0.26) {
									trueBearing = 0;
								} else if (bear < .70) {
									trueBearing = 30;
								} else if (bear < .87) {
									trueBearing = 45;
								} else if (bear < 1.31) {
									trueBearing = 60;
								} else if (bear < 1.57) {
									trueBearing = 90;
								}
							} else if ((xDist < 0) && (zDist < 0)) {
								double bear = Math.atan(xDist / zDist);
								if (bear < 0.26) {
									trueBearing = 0;
								} else if (bear < .70) {
									trueBearing = 330;
								} else if (bear < .87) {
									trueBearing = 315;
								} else if (bear < 1.31) {
									trueBearing = 300;
								} else if (bear < 1.57) {
									trueBearing = 270;
								}
							} else if ((xDist >= 0) && (zDist > 0)) {
								double bear = Math.atan(xDist / zDist);
								if (bear < 0.26) {
									trueBearing = 180;
								} else if (bear < .70) {
									trueBearing = 150;
								} else if (bear < .87) {
									trueBearing = 135;
								} else if (bear < 1.31) {
									trueBearing = 120;
								} else if (bear < 1.57) {
									trueBearing = 90;
								}
							} else if ((xDist < 0) && (zDist > 0)) {
								double bear = Math.atan((-xDist) / zDist);
								if (bear < 0.26) {
									trueBearing = 180;
								} else if (bear < .70) {
									trueBearing = 210;
								} else if (bear < .87) {
									trueBearing = 225;
								} else if (bear < 1.31) {
									trueBearing = 240;
								} else if (bear < 1.57) {
									trueBearing = 270;
								}
							} else if ((zDist == 0) && (xDist < 0)) {
								trueBearing = 270;
							} else if ((zDist == 0) && (xDist > 0)) {
								trueBearing = 90;
							} else {
								trueBearing = 0;
							}

							int relBearing = (int) trueBearing - craft.rotation;
							if (relBearing < 0) {
								relBearing = relBearing + 360;
							}

							String distanceStr = "";
							if (dist < 150) {
								distanceStr = "X";
							} else if (dist < 300) {
								distanceStr = "*";
							} else {
								distanceStr = ".";
								if (Math.random() < ((dist - 300) / 200)) {
									continue;
								}
							}

							if (bearings.containsKey(relBearing)) {
								if (bearings.get(relBearing).equalsIgnoreCase(".") && (distanceStr.equalsIgnoreCase("*") || distanceStr.equalsIgnoreCase("X"))) {
									bearings.put(relBearing, distanceStr);
								} else if (bearings.get(relBearing).equalsIgnoreCase("*") && distanceStr.equalsIgnoreCase("X")) {
									bearings.put(relBearing, distanceStr);
								}
							} else {
								bearings.put(relBearing, distanceStr);
							}

						}
					}
				}

				String line1;
				line1 = "";
				if (bearings.containsKey(300)) {
					line1 += bearings.get(300);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(315)) {
					line1 += bearings.get(315);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(330)) {
					line1 += bearings.get(330);
				} else {
					line1 += " ";
				}

				line1 += " ";
				if (bearings.containsKey(0)) {
					line1 += bearings.get(0);
				} else {
					line1 += "0";
				}

				line1 += " ";
				if (bearings.containsKey(30)) {
					line1 += bearings.get(30);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(45)) {
					line1 += bearings.get(45);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(60)) {
					line1 += bearings.get(60);
				} else {
					line1 += " ";
				}

				sign.setLine(1, line1);

				String line2;
				line2 = "";
				if (bearings.containsKey(270)) {
					line2 += " " + bearings.get(270) + "           ";
				} else {
					line2 += "270          ";
				}

				if (bearings.containsKey(90)) {
					line2 += bearings.get(90) + " ";
				} else {
					line2 += "90";
				}

				sign.setLine(2, line2);

				String line3;
				line3 = "";
				if (bearings.containsKey(240)) {
					line3 += bearings.get(240);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(225)) {
					line3 += bearings.get(225);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(210)) {
					line3 += bearings.get(210);
				} else {
					line3 += " ";
				}

				if (bearings.containsKey(180)) {
					line3 += " " + bearings.get(180) + " ";
				} else {
					line3 += "180";
				}

				if (bearings.containsKey(150)) {
					line3 += bearings.get(150);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(135)) {
					line3 += bearings.get(135);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(120)) {
					line3 += bearings.get(120);
				} else {
					line3 += " ";
				}

				sign.setLine(3, line3);

				sign.update();

			} else // radar not on
			{
				sign.setLine(1, "");
				sign.setLine(2, "OFF");
				sign.setLine(3, "");
				sign.update();
			}

		} else if (craftTypeName.equalsIgnoreCase("sonar")) {
			if (craft.sonarOn && !craft.sinking && (craft.type.canNavigate || craft.type.canDive)) {
				if (((System.currentTimeMillis() - craft.lastSonarPulse) / 1000) > 3) {

					craft.lastSonarPulse = System.currentTimeMillis();

					HashMap<Integer, String> bearings = new HashMap<>();
					for (Craft c : Craft.craftList) {
						if ((c != craft) && (c.world == craft.world) && !c.sinking && (c.type.canNavigate || c.type.canDive)) {
							float xDist = (c.minX + (c.sizeX / 2.0f)) - (craft.minX + (craft.sizeX / 2.0f));
							float zDist = (c.minZ + (c.sizeZ / 2.0f)) - (craft.minZ + (craft.sizeZ / 2.0f));
							float dist = (float) Math.sqrt((xDist * xDist) + (zDist * zDist));

							if (dist < 200) {
								double trueBearing = 0;
								if ((xDist >= 0) && (zDist < 0)) {
									double bear = Math.atan(xDist / (-zDist));
									if (bear < 0.26) {
										trueBearing = 0;
									} else if (bear < .70) {
										trueBearing = 30;
									} else if (bear < .87) {
										trueBearing = 45;
									} else if (bear < 1.31) {
										trueBearing = 60;
									} else if (bear < 1.57) {
										trueBearing = 90;
									}
								} else if ((xDist < 0) && (zDist < 0)) {
									double bear = Math.atan(xDist / zDist);
									if (bear < 0.26) {
										trueBearing = 0;
									} else if (bear < .70) {
										trueBearing = 330;
									} else if (bear < .87) {
										trueBearing = 315;
									} else if (bear < 1.31) {
										trueBearing = 300;
									} else if (bear < 1.57) {
										trueBearing = 270;
									}
								} else if ((xDist >= 0) && (zDist > 0)) {
									double bear = Math.atan(xDist / zDist);
									if (bear < 0.26) {
										trueBearing = 180;
									} else if (bear < .70) {
										trueBearing = 150;
									} else if (bear < .87) {
										trueBearing = 135;
									} else if (bear < 1.31) {
										trueBearing = 120;
									} else if (bear < 1.57) {
										trueBearing = 90;
									}
								} else if ((xDist < 0) && (zDist > 0)) {
									double bear = Math.atan((-xDist) / zDist);
									if (bear < 0.26) {
										trueBearing = 180;
									} else if (bear < .70) {
										trueBearing = 210;
									} else if (bear < .87) {
										trueBearing = 225;
									} else if (bear < 1.31) {
										trueBearing = 240;
									} else if (bear < 1.57) {
										trueBearing = 270;
									}
								} else if ((zDist == 0) && (xDist < 0)) {
									trueBearing = 270;
								} else if ((zDist == 0) && (xDist > 0)) {
									trueBearing = 90;
								} else {
									trueBearing = 0;
								}

								int relBearing = (int) trueBearing - craft.rotation;
								if (relBearing < 0) {
									relBearing = relBearing + 360;
								}

								String distanceStr = "";
								if (dist < 25) {
									distanceStr = "X";
								} else if (dist < 50) {
									distanceStr = "X";
									float speedChance = 1 - (((float) craft.setSpeed / (float) craft.type.maxEngineSpeed) * .8f);
									float depthChance;
									if ((craft.minY > 15) && (c.minY <= 15)) {
										depthChance = 0;
									} else if ((craft.minY <= 15) && (c.minY > 15)) {
										depthChance = 0;
									} else {
										depthChance = 1 - (((Math.abs(craft.minY - c.minY)) / 50.0f) * .4f);
									}

									float totalChance = depthChance * speedChance;
									if (Math.random() > totalChance) {
										continue;
									}
								} else if (dist < 75) {
									distanceStr = "*";
									float speedChance = 1 - ((float) craft.setSpeed / (float) craft.type.maxEngineSpeed);
									float depthChance;
									if ((craft.minY > 15) && (c.minY <= 15)) {
										depthChance = 0;
									} else if ((craft.minY <= 15) && (c.minY > 15)) {
										depthChance = 0;
									} else {
										depthChance = 1 - (((Math.abs(craft.minY - c.minY)) / 50.0f) * .6f);
									}

									float totalChance = depthChance * speedChance;
									if (Math.random() > totalChance) {
										continue;
									}
								} else {
									distanceStr = ".";
									float distChance = 1 - ((dist - 75) / 125.0f);
									float speedChance = 1 - ((float) craft.setSpeed / (float) craft.type.maxEngineSpeed);
									float depthChance;
									if ((craft.minY > 15) && (c.minY <= 15)) {
										depthChance = 0;
									} else if ((craft.minY <= 15) && (c.minY > 15)) {
										depthChance = 0;
									} else {
										depthChance = 1 - (((Math.abs(craft.minY - c.minY)) / 50.0f) * .6f);
									}

									float totalChance = distChance * depthChance * speedChance;
									if (Math.random() > totalChance) {
										continue;
									}
								}

								if (bearings.containsKey(relBearing)) {
									if (bearings.get(relBearing).equalsIgnoreCase(".") && (distanceStr.equalsIgnoreCase("*") || distanceStr.equalsIgnoreCase("X"))) {
										bearings.put(relBearing, distanceStr);
									} else if (bearings.get(relBearing).equalsIgnoreCase("*") && distanceStr.equalsIgnoreCase("X")) {
										bearings.put(relBearing, distanceStr);
									}
								} else {
									bearings.put(relBearing, distanceStr);
								}
							}
						}
					}

					String line1;
					line1 = "";
					if (bearings.containsKey(300)) {
						line1 += bearings.get(300);
					} else {
						line1 += " ";
					}
					line1 += " ";
					if (bearings.containsKey(315)) {
						line1 += bearings.get(315);
					} else {
						line1 += " ";
					}
					line1 += " ";
					if (bearings.containsKey(330)) {
						line1 += bearings.get(330);
					} else {
						line1 += " ";
					}

					line1 += " ";
					if (bearings.containsKey(0)) {
						line1 += bearings.get(0);
					} else {
						line1 += "0";
					}

					line1 += " ";
					if (bearings.containsKey(30)) {
						line1 += bearings.get(30);
					} else {
						line1 += " ";
					}
					line1 += " ";
					if (bearings.containsKey(45)) {
						line1 += bearings.get(45);
					} else {
						line1 += " ";
					}
					line1 += " ";
					if (bearings.containsKey(60)) {
						line1 += bearings.get(60);
					} else {
						line1 += " ";
					}

					sign.setLine(1, line1);

					String line2;
					line2 = "";
					if (bearings.containsKey(270)) {
						line2 += " " + bearings.get(270) + "           ";
					} else {
						line2 += "270          ";
					}

					if (bearings.containsKey(90)) {
						line2 += bearings.get(90) + " ";
					} else {
						line2 += "90";
					}

					sign.setLine(2, line2);

					String line3;
					line3 = "";
					if (bearings.containsKey(240)) {
						line3 += bearings.get(240);
					} else {
						line3 += " ";
					}
					line3 += " ";
					if (bearings.containsKey(225)) {
						line3 += bearings.get(225);
					} else {
						line3 += " ";
					}
					line3 += " ";
					if (bearings.containsKey(210)) {
						line3 += bearings.get(210);
					} else {
						line3 += " ";
					}

					if (bearings.containsKey(180)) {
						line3 += " " + bearings.get(180) + " ";
					} else {
						line3 += "180";
					}

					if (bearings.containsKey(150)) {
						line3 += bearings.get(150);
					} else {
						line3 += " ";
					}
					line3 += " ";
					if (bearings.containsKey(135)) {
						line3 += bearings.get(135);
					} else {
						line3 += " ";
					}
					line3 += " ";
					if (bearings.containsKey(120)) {
						line3 += bearings.get(120);
					} else {
						line3 += " ";
					}

					sign.setLine(3, line3);

					sign.update();
				}

			} else // radar not on
			{
				sign.setLine(1, "");
				sign.setLine(2, "OFF");
				sign.setLine(3, "");
				sign.update();
			}
		} else if (craftTypeName.equalsIgnoreCase("activesonar")) {
			if (craft.doPing && !craft.sinking && (craft.type.canNavigate || craft.type.canDive)) {

				craft.lastSonarPulse = System.currentTimeMillis();
				craft.doPing = false;

				boolean showRange = false;
				HashMap<Integer, String> bearings = new HashMap<>();
				for (Craft c : Craft.craftList) {
					if ((c != craft) && (c.world == craft.world) && !c.sinking && (c.type.canNavigate || c.type.canDive)) {
						float xDist = (c.minX + (c.sizeX / 2.0f)) - (craft.minX + (craft.sizeX / 2.0f));
						float zDist = (c.minZ + (c.sizeZ / 2.0f)) - (craft.minZ + (craft.sizeZ / 2.0f));
						float dist = (float) Math.sqrt((xDist * xDist) + (zDist * zDist));

						if (dist < 500) {
							double trueBearing = 0;
							if ((xDist >= 0) && (zDist < 0)) {
								double bear = Math.atan(xDist / (-zDist));
								if (bear < 0.26) {
									trueBearing = 0;
								} else if (bear < .70) {
									trueBearing = 30;
								} else if (bear < .87) {
									trueBearing = 45;
								} else if (bear < 1.31) {
									trueBearing = 60;
								} else if (bear < 1.57) {
									trueBearing = 90;
								}
							} else if ((xDist < 0) && (zDist < 0)) {
								double bear = Math.atan(xDist / zDist);
								if (bear < 0.26) {
									trueBearing = 0;
								} else if (bear < .70) {
									trueBearing = 330;
								} else if (bear < .87) {
									trueBearing = 315;
								} else if (bear < 1.31) {
									trueBearing = 300;
								} else if (bear < 1.57) {
									trueBearing = 270;
								}
							} else if ((xDist >= 0) && (zDist > 0)) {
								double bear = Math.atan(xDist / zDist);
								if (bear < 0.26) {
									trueBearing = 180;
								} else if (bear < .70) {
									trueBearing = 150;
								} else if (bear < .87) {
									trueBearing = 135;
								} else if (bear < 1.31) {
									trueBearing = 120;
								} else if (bear < 1.57) {
									trueBearing = 90;
								}
							} else if ((xDist < 0) && (zDist > 0)) {
								double bear = Math.atan((-xDist) / zDist);
								if (bear < 0.26) {
									trueBearing = 180;
								} else if (bear < .70) {
									trueBearing = 210;
								} else if (bear < .87) {
									trueBearing = 225;
								} else if (bear < 1.31) {
									trueBearing = 240;
								} else if (bear < 1.57) {
									trueBearing = 270;
								}
							} else if ((zDist == 0) && (xDist < 0)) {
								trueBearing = 270;
							} else if ((zDist == 0) && (xDist > 0)) {
								trueBearing = 90;
							} else {
								trueBearing = 0;
							}

							int relBearing = (int) trueBearing - craft.rotation;
							if (relBearing < 0) {
								relBearing = relBearing + 360;
							}

							String distanceStr = "";
							if (dist < 25) {
								distanceStr = "X";
							} else if (dist < 100) {
								distanceStr = "X";
								float speedChance = 1 - (((float) craft.setSpeed / (float) craft.type.maxEngineSpeed) * .8f);
								float depthChance;
								if ((craft.minY > 15) && (c.minY <= 15)) {
									depthChance = 0;
								} else if ((craft.minY <= 15) && (c.minY > 15)) {
									depthChance = 0;
								} else {
									depthChance = 1 - (((Math.abs(craft.minY - c.minY)) / 50.0f) * .4f);
								}

								float totalChance = depthChance * speedChance;
								if (Math.random() > totalChance) {
									continue;
								}
							} else if (dist < 200) {
								distanceStr = "*";
								float speedChance = 1 - ((float) craft.setSpeed / (float) craft.type.maxEngineSpeed);
								float depthChance;
								if ((craft.minY > 15) && (c.minY <= 15)) {
									depthChance = 0;
								} else if ((craft.minY <= 15) && (c.minY > 15)) {
									depthChance = 0;
								} else {
									depthChance = 1 - (((Math.abs(craft.minY - c.minY)) / 50.0f) * .6f);
								}

								float totalChance = depthChance * speedChance;
								if (Math.random() > totalChance) {
									continue;
								}
							} else {
								distanceStr = ".";
								float distChance = 1 - ((dist - 200) / 300.0f);
								float speedChance = 1 - ((float) craft.setSpeed / (float) craft.type.maxEngineSpeed);
								float depthChance;
								if ((craft.minY > 15) && (c.minY <= 15)) {
									depthChance = 0;
								} else if ((craft.minY <= 15) && (c.minY > 15)) {
									depthChance = 0;
								} else {
									depthChance = 1 - (((Math.abs(craft.minY - c.minY)) / 50.0f) * .6f);
								}

								float totalChance = distChance * depthChance * speedChance;
								if (Math.random() > totalChance) {
									continue;
								}
							}

							if (bearings.containsKey(relBearing)) {
								if (bearings.get(relBearing).equalsIgnoreCase(".") && (distanceStr.equalsIgnoreCase("*") || distanceStr.equalsIgnoreCase("X"))) {
									bearings.put(relBearing, distanceStr);
								} else if (bearings.get(relBearing).equalsIgnoreCase("*") && distanceStr.equalsIgnoreCase("X")) {
									bearings.put(relBearing, distanceStr);
								}
							} else {
								bearings.put(relBearing, distanceStr);
							}

							if (c == craft.sonarTarget) {
								showRange = true;
								craft.sonarTargetRng = dist;
							}

						}
					}
				}

				String line1;
				line1 = "";
				if (bearings.containsKey(300)) {
					line1 += bearings.get(300);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(315)) {
					line1 += bearings.get(315);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(330)) {
					line1 += bearings.get(330);
				} else {
					line1 += " ";
				}

				line1 += " ";
				if (bearings.containsKey(0)) {
					line1 += bearings.get(0);
				} else {
					line1 += "0";
				}

				line1 += " ";
				if (bearings.containsKey(30)) {
					line1 += bearings.get(30);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(45)) {
					line1 += bearings.get(45);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(60)) {
					line1 += bearings.get(60);
				} else {
					line1 += " ";
				}

				sign.setLine(1, line1);

				String line2;
				line2 = "";
				if (bearings.containsKey(270)) {
					line2 += " " + bearings.get(270) + "   (";
				} else {
					line2 += "270  (";
				}

				if (showRange && (craft.sonarTargetRng < 100)) {
					line2 += "0" + (int) craft.sonarTargetRng;
				} else if (showRange) {
					line2 += (int) craft.sonarTargetRng;
				} else {
					line2 += "XXX";
				}
				line2 += ")  ";

				if (bearings.containsKey(90)) {
					line2 += " " + bearings.get(90) + " ";
				} else {
					line2 += "090";
				}

				sign.setLine(2, line2);

				String line3;
				line3 = "";
				if (bearings.containsKey(240)) {
					line3 += bearings.get(240);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(225)) {
					line3 += bearings.get(225);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(210)) {
					line3 += bearings.get(210);
				} else {
					line3 += " ";
				}

				if (bearings.containsKey(180)) {
					line3 += " " + bearings.get(180) + " ";
				} else {
					line3 += "180";
				}

				if (bearings.containsKey(150)) {
					line3 += bearings.get(150);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(135)) {
					line3 += bearings.get(135);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(120)) {
					line3 += bearings.get(120);
				} else {
					line3 += " ";
				}

				sign.setLine(3, line3);

				sign.update();

			} else // no ping
			{
				if (((System.currentTimeMillis() - craft.lastSonarPulse) / 1000.0f) > 10) {
					sign.setLine(1, "");
					sign.setLine(2, "OFF");
					sign.setLine(3, "");
					sign.update();
				}
			}
		} else if (craftTypeName.equalsIgnoreCase("detector")) {
			if (!craft.sinking && (craft.maxY > 63)) {
				HashMap<Integer, String> bearings = new HashMap<>();
				for (Craft c : Craft.craftList) {
					boolean radarEcho = (((System.currentTimeMillis() - c.lastRadarPulse) / 1000) < 20);
					boolean radioEcho = (((System.currentTimeMillis() - c.lastRadioPulse) / 1000) < 60);
					if ((c != craft) && (c.world == craft.world) && (radarEcho || radioEcho)) {
						float xDist = (c.minX + (c.sizeX / 2.0f)) - (craft.minX + (craft.sizeX / 2.0f));
						float zDist = (c.minZ + (c.sizeZ / 2.0f)) - (craft.minZ + (craft.sizeZ / 2.0f));
						float dist = (float) Math.sqrt((xDist * xDist) + (zDist * zDist));

						radarEcho = (radarEcho && (dist < 1000));
						radioEcho = (radioEcho && (dist < 600));
						if (radarEcho || radioEcho) {
							double trueBearing = 0;
							if ((xDist >= 0) && (zDist < 0)) {
								double bear = Math.atan(xDist / (-zDist));
								if (bear < 0.26) {
									trueBearing = 0;
								} else if (bear < .70) {
									trueBearing = 30;
								} else if (bear < .87) {
									trueBearing = 45;
								} else if (bear < 1.31) {
									trueBearing = 60;
								} else if (bear < 1.57) {
									trueBearing = 90;
								}
							} else if ((xDist < 0) && (zDist < 0)) {
								double bear = Math.atan(xDist / zDist);
								if (bear < 0.26) {
									trueBearing = 0;
								} else if (bear < .70) {
									trueBearing = 330;
								} else if (bear < .87) {
									trueBearing = 315;
								} else if (bear < 1.31) {
									trueBearing = 300;
								} else if (bear < 1.57) {
									trueBearing = 270;
								}
							} else if ((xDist >= 0) && (zDist > 0)) {
								double bear = Math.atan(xDist / zDist);
								if (bear < 0.26) {
									trueBearing = 180;
								} else if (bear < .70) {
									trueBearing = 150;
								} else if (bear < .87) {
									trueBearing = 135;
								} else if (bear < 1.31) {
									trueBearing = 120;
								} else if (bear < 1.57) {
									trueBearing = 90;
								}
							} else if ((xDist < 0) && (zDist > 0)) {
								double bear = Math.atan((-xDist) / zDist);
								if (bear < 0.26) {
									trueBearing = 180;
								} else if (bear < .70) {
									trueBearing = 210;
								} else if (bear < .87) {
									trueBearing = 225;
								} else if (bear < 1.31) {
									trueBearing = 240;
								} else if (bear < 1.57) {
									trueBearing = 270;
								}
							} else if ((zDist == 0) && (xDist < 0)) {
								trueBearing = 270;
							} else if ((zDist == 0) && (xDist > 0)) {
								trueBearing = 90;
							} else {
								trueBearing = 0;
							}

							int relBearing = (int) trueBearing - craft.rotation;
							if (relBearing < 0) {
								relBearing = relBearing + 360;
							}

							String distanceStr = "";
							if (radarEcho) {
								if (dist < 200) {
									distanceStr = "X";
								} else if (dist < 500) {
									distanceStr = "*";
								} else {
									distanceStr = ".";
									if (Math.random() > (1 - ((dist - 500) / 500.0f))) {
										continue;
									}
								}
							} else if (radioEcho) {
								if (dist < 200) {
									distanceStr = "O";
								} else if (dist < 400) {
									distanceStr = "o";
								} else {
									distanceStr = ",";
									if (Math.random() > (1 - ((dist - 400) / 200.0f))) {
										continue;
									}
								}
							}

							if (bearings.containsKey(relBearing)) {
								if (radarEcho) {
									if (bearings.get(relBearing).equalsIgnoreCase(".") && (distanceStr.equalsIgnoreCase("*") || distanceStr.equalsIgnoreCase("X"))) {
										bearings.put(relBearing, distanceStr);
									} else if (bearings.get(relBearing).equalsIgnoreCase("*") && distanceStr.equalsIgnoreCase("X")) {
										bearings.put(relBearing, distanceStr);
									}
								} else if (radioEcho) {
									if (bearings.get(relBearing).equalsIgnoreCase(",") && (distanceStr.equalsIgnoreCase("o") || distanceStr.equalsIgnoreCase("O"))) {
										bearings.put(relBearing, distanceStr);
									} else if (bearings.get(relBearing).equalsIgnoreCase("o") && distanceStr.equalsIgnoreCase("O")) {
										bearings.put(relBearing, distanceStr);
									}
								}
							} else {
								bearings.put(relBearing, distanceStr);
							}

						}
					}
				}

				String line1;
				line1 = "";
				if (bearings.containsKey(300)) {
					line1 += bearings.get(300);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(315)) {
					line1 += bearings.get(315);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(330)) {
					line1 += bearings.get(330);
				} else {
					line1 += " ";
				}

				line1 += " ";
				if (bearings.containsKey(0)) {
					line1 += bearings.get(0);
				} else {
					line1 += "0";
				}

				line1 += " ";
				if (bearings.containsKey(30)) {
					line1 += bearings.get(30);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(45)) {
					line1 += bearings.get(45);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(60)) {
					line1 += bearings.get(60);
				} else {
					line1 += " ";
				}

				sign.setLine(1, line1);

				String line2;
				line2 = "";
				if (bearings.containsKey(270)) {
					line2 += " " + bearings.get(270) + "           ";
				} else {
					line2 += "270          ";
				}

				if (bearings.containsKey(90)) {
					line2 += bearings.get(90) + " ";
				} else {
					line2 += "90";
				}

				sign.setLine(2, line2);

				String line3;
				line3 = "";
				if (bearings.containsKey(240)) {
					line3 += bearings.get(240);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(225)) {
					line3 += bearings.get(225);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(210)) {
					line3 += bearings.get(210);
				} else {
					line3 += " ";
				}

				if (bearings.containsKey(180)) {
					line3 += " " + bearings.get(180) + " ";
				} else {
					line3 += "180";
				}

				if (bearings.containsKey(150)) {
					line3 += bearings.get(150);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(135)) {
					line3 += bearings.get(135);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(120)) {
					line3 += bearings.get(120);
				} else {
					line3 += " ";
				}

				sign.setLine(3, line3);

				sign.update();

			} else // radar not on
			{
				sign.setLine(1, "");
				sign.setLine(2, "OFF");
				sign.setLine(3, "");
				sign.update();
			}

		} else if (craftTypeName.equalsIgnoreCase("hydrophone")) {
			if (!craft.sinking && (craft.type.canNavigate || craft.type.canDive)) {
				HashMap<Integer, String> bearings = new HashMap<>();
				for (Craft c : Craft.craftList) {
					if ((c != craft) && (c.world == craft.world) && ((((System.currentTimeMillis() - c.lastSonarPulse) / 1000) < 10) || ((c.type.canNavigate || c.type.canDive) && c.enginesOn))) {
						float xDist = (c.minX + (c.sizeX / 2.0f)) - (craft.minX + (craft.sizeX / 2.0f));
						float zDist = (c.minZ + (c.sizeZ / 2.0f)) - (craft.minZ + (craft.sizeZ / 2.0f));
						float dist = (float) Math.sqrt((xDist * xDist) + (zDist * zDist));

						if (dist < 1000) {
							double trueBearing = 0;
							if ((xDist >= 0) && (zDist < 0)) {
								double bear = Math.atan(xDist / (-zDist));
								if (bear < 0.26) {
									trueBearing = 0;
								} else if (bear < .70) {
									trueBearing = 30;
								} else if (bear < .87) {
									trueBearing = 45;
								} else if (bear < 1.31) {
									trueBearing = 60;
								} else if (bear < 1.57) {
									trueBearing = 90;
								}
							} else if ((xDist < 0) && (zDist < 0)) {
								double bear = Math.atan(xDist / zDist);
								if (bear < 0.26) {
									trueBearing = 0;
								} else if (bear < .70) {
									trueBearing = 330;
								} else if (bear < .87) {
									trueBearing = 315;
								} else if (bear < 1.31) {
									trueBearing = 300;
								} else if (bear < 1.57) {
									trueBearing = 270;
								}
							} else if ((xDist >= 0) && (zDist > 0)) {
								double bear = Math.atan(xDist / zDist);
								if (bear < 0.26) {
									trueBearing = 180;
								} else if (bear < .70) {
									trueBearing = 150;
								} else if (bear < .87) {
									trueBearing = 135;
								} else if (bear < 1.31) {
									trueBearing = 120;
								} else if (bear < 1.57) {
									trueBearing = 90;
								}
							} else if ((xDist < 0) && (zDist > 0)) {
								double bear = Math.atan((-xDist) / zDist);
								if (bear < 0.26) {
									trueBearing = 180;
								} else if (bear < .70) {
									trueBearing = 210;
								} else if (bear < .87) {
									trueBearing = 225;
								} else if (bear < 1.31) {
									trueBearing = 240;
								} else if (bear < 1.57) {
									trueBearing = 270;
								}
							} else if ((zDist == 0) && (xDist < 0)) {
								trueBearing = 270;
							} else if ((zDist == 0) && (xDist > 0)) {
								trueBearing = 90;
							} else {
								trueBearing = 0;
							}

							int relBearing = (int) trueBearing - craft.rotation;
							if (relBearing < 0) {
								relBearing = relBearing + 360;
							}

							String distanceStr = "";
							if (((System.currentTimeMillis() - c.lastSonarPulse) / 1000) < 10) {
								if (dist < 50) {
									distanceStr = "O";
								} else if (dist < 150) {
									distanceStr = "o";
								} else {
									distanceStr = ".";
								}
							} else if (dist < 50) {
								distanceStr = "X";
								float speed1Chance = 1 - ((float) craft.setSpeed / (float) craft.type.maxEngineSpeed);
								float speed2Chance = (float) c.setSpeed / (float) c.type.maxEngineSpeed;
								float depthChance;
								if ((craft.minY > 15) && (c.minY <= 15)) {
									depthChance = .2f;
								} else if ((craft.minY <= 15) && (c.minY > 15)) {
									depthChance = .2f;
								} else {
									depthChance = 1 - (((Math.abs(craft.minY - c.minY)) / 60.0f) * .5f);
								}

								float totalChance = speed1Chance * speed2Chance * depthChance;
								if (Math.random() > totalChance) {
									continue;
								}
							} else if (dist < 150) {
								distanceStr = "*";
								float speed1Chance = 1 - ((float) craft.setSpeed / (float) craft.type.maxEngineSpeed);
								float speed2Chance = (float) c.setSpeed / (float) c.type.maxEngineSpeed;
								float depthChance;
								if ((craft.minY > 15) && (c.minY <= 15)) {
									depthChance = .2f;
								} else if ((craft.minY <= 15) && (c.minY > 15)) {
									depthChance = .2f;
								} else {
									depthChance = 1 - (((Math.abs(craft.minY - c.minY)) / 60.0f) * .5f);
								}

								float totalChance = speed1Chance * speed2Chance * depthChance;
								if (Math.random() > totalChance) {
									continue;
								}
							} else {
								distanceStr = ".";
								float distChance = 1 - ((dist - 150) / 850.0f);
								float speed1Chance = 1 - ((float) craft.setSpeed / (float) craft.type.maxEngineSpeed);
								float speed2Chance = (float) c.setSpeed / (float) c.type.maxEngineSpeed;
								float depthChance;
								if ((craft.minY > 15) && (c.minY <= 15)) {
									depthChance = .1f;
								} else if ((craft.minY <= 15) && (c.minY > 15)) {
									depthChance = .1f;
								} else {
									depthChance = 1 - (((Math.abs(craft.minY - c.minY)) / 45.0f) * .7f);
								}

								float totalChance = distChance * speed1Chance * speed2Chance * depthChance;

								if (Math.random() > totalChance) {
									continue;
								}
							}

							if (bearings.containsKey(relBearing)) {
								if (bearings.get(relBearing).equalsIgnoreCase(".") && (distanceStr.equalsIgnoreCase("*") || distanceStr.equalsIgnoreCase("X"))) {
									bearings.put(relBearing, distanceStr);
								} else if (bearings.get(relBearing).equalsIgnoreCase("*") && distanceStr.equalsIgnoreCase("X")) {
									bearings.put(relBearing, distanceStr);
								}
							} else {
								bearings.put(relBearing, distanceStr);
							}

						}
					}
				}

				String line1;
				line1 = "";
				if (bearings.containsKey(300)) {
					line1 += bearings.get(300);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(315)) {
					line1 += bearings.get(315);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(330)) {
					line1 += bearings.get(330);
				} else {
					line1 += " ";
				}

				line1 += " ";
				if (bearings.containsKey(0)) {
					line1 += bearings.get(0);
				} else {
					line1 += "0";
				}

				line1 += " ";
				if (bearings.containsKey(30)) {
					line1 += bearings.get(30);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(45)) {
					line1 += bearings.get(45);
				} else {
					line1 += " ";
				}
				line1 += " ";
				if (bearings.containsKey(60)) {
					line1 += bearings.get(60);
				} else {
					line1 += " ";
				}

				sign.setLine(1, line1);

				String line2;
				line2 = "";
				if (bearings.containsKey(270)) {
					line2 += " " + bearings.get(270) + "           ";
				} else {
					line2 += "270          ";
				}

				if (bearings.containsKey(90)) {
					line2 += bearings.get(90) + " ";
				} else {
					line2 += "90";
				}

				sign.setLine(2, line2);

				String line3;
				line3 = "";
				if (bearings.containsKey(240)) {
					line3 += bearings.get(240);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(225)) {
					line3 += bearings.get(225);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(210)) {
					line3 += bearings.get(210);
				} else {
					line3 += " ";
				}

				if (bearings.containsKey(180)) {
					line3 += " " + bearings.get(180) + " ";
				} else {
					line3 += "180";
				}

				if (bearings.containsKey(150)) {
					line3 += bearings.get(150);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(135)) {
					line3 += bearings.get(135);
				} else {
					line3 += " ";
				}
				line3 += " ";
				if (bearings.containsKey(120)) {
					line3 += bearings.get(120);
				} else {
					line3 += " ";
				}

				sign.setLine(3, line3);

				sign.update();

			} else // radar not on
			{
				sign.setLine(1, "");
				sign.setLine(2, "OFF");
				sign.setLine(3, "");
				sign.update();
			}

		} else if (craftTypeName.equalsIgnoreCase("passivesonar")) {
			if (!craft.sinking && (craft.type.canNavigate || craft.type.canDive)) {
				HashMap<Craft, Integer> bearings = new HashMap<>();
				HashMap<Craft, Float> strengths = new HashMap<>();
				HashMap<Integer, Craft> displayBearingsStrong = new HashMap<>();
				HashMap<Integer, Craft> displayBearingsMedium = new HashMap<>();
				HashMap<Integer, Craft> displayBearingsWeak = new HashMap<>();
				for (Craft c : Craft.craftList) {
					if ((c != craft) && (c.world == craft.world) && (((c.type.canNavigate || c.type.canDive) && c.enginesOn))) {
						float xDist = (c.minX + (c.sizeX / 2.0f)) - (craft.minX + (craft.sizeX / 2.0f));
						float zDist = (c.minZ + (c.sizeZ / 2.0f)) - (craft.minZ + (craft.sizeZ / 2.0f));
						float dist = (float) Math.sqrt((xDist * xDist) + (zDist * zDist));

						if (dist < 1500) {
							double trueBearing = 0;
							if ((xDist >= 0) && (zDist < 0)) {
								double bear = Math.atan(xDist / (-zDist));
								if (bear < 0.12) {
									trueBearing = 0;
								} else if (bear < .38) {
									trueBearing = 15;
								} else if (bear < .65) {
									trueBearing = 30;
								} else if (bear < .91) {
									trueBearing = 45;
								} else if (bear < 1.17) {
									trueBearing = 60;
								} else if (bear < 1.43) {
									trueBearing = 75;
								} else if (bear < 1.57) {
									trueBearing = 90;
								}
							} else if ((xDist < 0) && (zDist < 0)) {
								double bear = Math.atan(xDist / zDist);
								if (bear < 0.12) {
									trueBearing = 0;
								} else if (bear < .38) {
									trueBearing = 345;
								} else if (bear < .65) {
									trueBearing = 330;
								} else if (bear < .91) {
									trueBearing = 315;
								} else if (bear < 1.17) {
									trueBearing = 300;
								} else if (bear < 1.43) {
									trueBearing = 285;
								} else if (bear < 1.57) {
									trueBearing = 270;
								}
							} else if ((xDist >= 0) && (zDist > 0)) {
								double bear = Math.atan(xDist / zDist);
								if (bear < 0.12) {
									trueBearing = 180;
								} else if (bear < .38) {
									trueBearing = 165;
								} else if (bear < .65) {
									trueBearing = 150;
								} else if (bear < .91) {
									trueBearing = 135;
								} else if (bear < 1.17) {
									trueBearing = 120;
								} else if (bear < 1.43) {
									trueBearing = 105;
								} else if (bear < 1.57) {
									trueBearing = 90;
								}
							} else if ((xDist < 0) && (zDist > 0)) {
								double bear = Math.atan((-xDist) / zDist);
								if (bear < 0.12) {
									trueBearing = 180;
								} else if (bear < .38) {
									trueBearing = 195;
								} else if (bear < .65) {
									trueBearing = 210;
								} else if (bear < .91) {
									trueBearing = 225;
								} else if (bear < 1.17) {
									trueBearing = 240;
								} else if (bear < 1.43) {
									trueBearing = 255;
								} else if (bear < 1.57) {
									trueBearing = 270;
								}
							} else if ((zDist == 0) && (xDist < 0)) {
								trueBearing = 270;
							} else if ((zDist == 0) && (xDist > 0)) {
								trueBearing = 90;
							} else {
								trueBearing = 0;
							}

							int relBearing = (int) trueBearing - craft.rotation;
							if (relBearing < 0) {
								relBearing = relBearing + 360;
							}

							float distChance;
							if (dist < 150) {
								distChance = 1;
							} else {
								distChance = 1 - ((dist - 150) / 1350.0f);
							}
							float speed1Chance = 1 - ((float) craft.setSpeed / (float) craft.type.maxEngineSpeed);
							float speed2Chance = (((float) c.setSpeed / (float) c.type.maxEngineSpeed) * .8f) + .2f;
							if ((speed2Chance < 1) && c.hfSonarOn) {
								speed2Chance = speed2Chance + 0.3f;
								if (speed2Chance > 1) {
									speed2Chance = 1;
								}
							}
							float depthChance;
							if ((craft.minY > 15) && (c.minY <= 15)) {
								depthChance = .3f;
							} else if ((craft.minY <= 15) && (c.minY > 15)) {
								depthChance = .3f;
							} else {
								depthChance = 1 - (((Math.abs(craft.minY - c.minY)) / 50.0f) * .5f);
							}

							float totalChance = distChance * speed1Chance * speed2Chance * depthChance;

							if ((totalChance < .2) && (Math.random() > totalChance)) {
								continue;
							}

							bearings.put(c, relBearing);
							strengths.put(c, totalChance);
							if (totalChance < .2) {
								if (displayBearingsWeak.containsKey(relBearing)) {
									if (totalChance > strengths.get(displayBearingsWeak.get(relBearing))) {
										displayBearingsWeak.put(relBearing, c);
									}
								} else {
									displayBearingsWeak.put(relBearing, c);
								}
							} else if (totalChance < .5) {
								if (displayBearingsMedium.containsKey(relBearing)) {
									if (totalChance > strengths.get(displayBearingsMedium.get(relBearing))) {
										if (displayBearingsWeak.containsKey(relBearing)) {
											if (strengths.get(displayBearingsMedium.get(relBearing)) > strengths.get(displayBearingsWeak.get(relBearing))) {
												displayBearingsWeak.put(relBearing, displayBearingsMedium.get(relBearing));
											}
										}
										displayBearingsMedium.put(relBearing, c);
									} else {
										if (displayBearingsWeak.containsKey(relBearing)) {
											if (strengths.get(c) > strengths.get(displayBearingsWeak.get(relBearing))) {
												displayBearingsWeak.put(relBearing, c);
											}
										}
									}
								} else {
									displayBearingsMedium.put(relBearing, c);
								}
							} else {
								if (displayBearingsStrong.containsKey(relBearing)) {
									if (totalChance > strengths.get(displayBearingsStrong.get(relBearing))) {
										if (displayBearingsMedium.containsKey(relBearing)) {
											if (strengths.get(displayBearingsStrong.get(relBearing)) > strengths.get(displayBearingsMedium.get(relBearing))) {
												if (displayBearingsWeak.containsKey(relBearing)) {
													if (strengths.get(displayBearingsMedium.get(relBearing)) > strengths.get(displayBearingsWeak.get(relBearing))) {
														displayBearingsWeak.put(relBearing, displayBearingsMedium.get(relBearing));
													}
												}
												displayBearingsMedium.put(relBearing, displayBearingsStrong.get(relBearing));
											}
										} else {
											displayBearingsMedium.put(relBearing, displayBearingsStrong.get(relBearing));
										}
										displayBearingsStrong.put(relBearing, c);
									} else {
										if (displayBearingsMedium.containsKey(relBearing)) {
											if (strengths.get(c) > strengths.get(displayBearingsMedium.get(relBearing))) {
												if (strengths.get(displayBearingsMedium.get(relBearing)) > strengths.get(displayBearingsWeak.get(relBearing))) {
													displayBearingsWeak.put(relBearing, displayBearingsMedium.get(relBearing));
												}
												displayBearingsMedium.put(relBearing, c);
											}
										} else {
											displayBearingsMedium.put(relBearing, c);
										}
									}
								} else {
									displayBearingsStrong.put(relBearing, c);
								}
							}

							if (craft.sonarTargetIDs.containsKey(c)) {
								craft.sonarTargetStrength.put(c, craft.sonarTargetStrength.get(c) + totalChance);
							} else {
								craft.sonarTargetIDs2.put(craft.sonarTargetIDs.size(), c);
								craft.sonarTargetIDs.put(c, craft.sonarTargetIDs.size());
								craft.sonarTargetStrength.put(c, totalChance);
							}

							if ((c == craft.sonarTarget) && (craft.sonarTargetStrength.get(c) >= 40)) {
								craft.sonarTargetRng = dist;
							}
						}
					}
				}

				String line1;
				line1 = "Target:";
				if ((craft.sonarTarget != null) && craft.sonarTargetIDs.containsKey(craft.sonarTarget)) {
					int id = craft.sonarTargetIDs.get(craft.sonarTarget);
					line1 += idIntToString(id);

				} else {
					line1 += "NONE";
				}
				sign.setLine(1, line1);

				String line2;
				line2 = "CLS:";
				if ((craft.sonarTarget != null) && craft.sonarTargetIDs.containsKey(craft.sonarTarget)) {
					if (craft.sonarTargetStrength.get(craft.sonarTarget) >= 20) {
						line2 += craft.sonarTarget.name.toUpperCase();
						if (line2.length() > 15) {
							line2 = line2.substring(0, 14);
						}
					} else {
						line2 += "UNKNOWN";
					}

				} else {
					line2 += "-";
				}

				sign.setLine(2, line2);

				String line3;
				if ((craft.sonarTarget != null) && craft.sonarTargetIDs.containsKey(craft.sonarTarget)) {
					if (craft.sonarTargetStrength.get(craft.sonarTarget) >= 30) {
						line3 = "SPD:" + craft.sonarTarget.setSpeed + " RNG:";
					} else {
						line3 = "SPD:?? RNG:";
					}

					if (craft.sonarTargetRng > -1) {
						line3 += (int) craft.sonarTargetRng;
					} else {
						line3 += "???";
					}

				} else {
					line3 = "SPD:--  RNG:---";
				}

				sign.setLine(3, line3);
				sign.update();

				if (sign.getBlock().getRelative(BlockFace.UP).getTypeId() == 68) {
					Sign sign2 = (Sign) sign.getBlock().getRelative(BlockFace.UP).getState();

					sign2.setLine(0, "_000___090__");

					String line21 = "";
					if (displayBearingsStrong.containsKey(330)) {
						line21 += idIntToString(craft.sonarTargetIDs.get(displayBearingsStrong.get(330)));
					} else {
						line21 += "_";
					}
					if (displayBearingsStrong.containsKey(345)) {
						line21 += idIntToString(craft.sonarTargetIDs.get(displayBearingsStrong.get(345)));
					} else {
						line21 += "_";
					}

					for (int i = 0; i < 10; i++) {
						if (displayBearingsStrong.containsKey(i * 15)) {
							line21 += idIntToString(craft.sonarTargetIDs.get(displayBearingsStrong.get(i * 15)));
						} else {
							line21 += "_";
						}
					}
					sign2.setLine(1, line21);

					String line22 = "";
					if (displayBearingsMedium.containsKey(330)) {
						line22 += idIntToString(craft.sonarTargetIDs.get(displayBearingsMedium.get(330)));
					} else {
						line22 += "_";
					}
					if (displayBearingsMedium.containsKey(345)) {
						line22 += idIntToString(craft.sonarTargetIDs.get(displayBearingsMedium.get(345)));
					} else {
						line22 += "_";
					}

					for (int i = 0; i < 10; i++) {
						if (displayBearingsMedium.containsKey(i * 15)) {
							line22 += idIntToString(craft.sonarTargetIDs.get(displayBearingsMedium.get(i * 15)));
						} else {
							line22 += "_";
						}
					}
					sign2.setLine(2, line22);

					String line23 = "";
					if (displayBearingsWeak.containsKey(330)) {
						line23 += idIntToString(craft.sonarTargetIDs.get(displayBearingsWeak.get(330)));
					} else {
						line23 += "_";
					}
					if (displayBearingsWeak.containsKey(345)) {
						line23 += idIntToString(craft.sonarTargetIDs.get(displayBearingsWeak.get(345)));
					} else {
						line23 += "_";
					}

					for (int i = 0; i < 10; i++) {
						if (displayBearingsWeak.containsKey(i * 15)) {
							line23 += idIntToString(craft.sonarTargetIDs.get(displayBearingsWeak.get(i * 15)));
						} else {
							line23 += "_";
						}
					}
					sign2.setLine(3, line23);

					sign2.update();

					BlockFace bf;
					switch (sign.getBlock().getData()) {
						case (byte) 0x2:// n
							bf = BlockFace.WEST;
							break;
						case (byte) 0x3:// s
							bf = BlockFace.EAST;
							break;
						case (byte) 0x4:// w
							bf = BlockFace.SOUTH;
							break;
						case (byte) 0x5:// e
							bf = BlockFace.NORTH;
							break;
						default:
							bf = BlockFace.NORTH;
							break;
					}

					if (sign2.getBlock().getRelative(bf).getTypeId() == 68) {
						Sign sign3 = (Sign) sign2.getBlock().getRelative(bf).getState();

						sign3.setLine(0, "_180___270__");

						String line31 = "";
						for (int i = 0; i < 12; i++) {
							if (displayBearingsStrong.containsKey((i * 15) + 150)) {
								line31 += idIntToString(craft.sonarTargetIDs.get(displayBearingsStrong.get((i * 15) + 150)));
							} else {
								line31 += "_";
							}
						}
						sign3.setLine(1, line31);

						String line32 = "";
						for (int i = 0; i < 12; i++) {
							if (displayBearingsMedium.containsKey((i * 15) + 150)) {
								line32 += idIntToString(craft.sonarTargetIDs.get(displayBearingsMedium.get((i * 15) + 150)));
							} else {
								line32 += "_";
							}
						}
						sign3.setLine(2, line32);

						String line33 = "";
						for (int i = 0; i < 12; i++) {
							if (displayBearingsWeak.containsKey((i * 15) + 150)) {
								line33 += idIntToString(craft.sonarTargetIDs.get(displayBearingsWeak.get((i * 15) + 150)));
							} else {
								line33 += "_";
							}
						}
						sign3.setLine(3, line33);

						sign3.update();
					}
				}

			} else // radar not on
			{
				sign.setLine(1, "");
				sign.setLine(2, "OFF");
				sign.setLine(3, "");
				sign.update();
			}

		} else if (craftTypeName.equalsIgnoreCase("hfsonar")) {
			if (!craft.sinking && (craft.type.canNavigate || craft.type.canDive) && craft.hfOn) {
				int lrDir, start, forDir, horizCenter, vertCenter;
				boolean lrX = false;
				switch (sign.getBlock().getData()) {
					case (byte) 0x2:// n
						horizCenter = (int) (craft.minX + (craft.sizeX / 2.0f));
						vertCenter = (int) (craft.minY + (craft.sizeY / 2.0f));
						lrDir = -1;
						lrX = true;
						start = craft.maxZ;
						forDir = 1;
						break;
					case (byte) 0x3:// s
						horizCenter = (int) (craft.minX + (craft.sizeX / 2.0f));
						vertCenter = (int) (craft.minY + (craft.sizeY / 2.0f));
						lrDir = 1;
						lrX = true;
						start = craft.minZ;
						forDir = -1;
						break;
					case (byte) 0x4:// w
						horizCenter = (int) (craft.minZ + (craft.sizeZ / 2.0f));
						vertCenter = (int) (craft.minY + (craft.sizeY / 2.0f));
						lrDir = 1;
						start = craft.maxX;
						forDir = 1;
						break;
					default: // case (byte) 0x5://e
						horizCenter = (int) (craft.minZ + (craft.sizeZ / 2.0f));
						vertCenter = (int) (craft.minY + (craft.sizeY / 2.0f));
						lrDir = -1;
						start = craft.minX;
						forDir = -1;
						break;
				}

				int[][] displayArray = new int[26][8];

				for (int i = 0; i < 26; i++) {
					for (int j = 0; j < 8; j++) {
						if (lrX) {
							int dist = 0;
							int k = 0;
							int refX;
							if (lrDir < 0) {
								refX = horizCenter + ((13 - i) * 2);
							} else {
								refX = (horizCenter + ((i) * 2)) - 26;
							}
							int refY = (vertCenter + (j * 5)) - 17;
							int refZ = start + forDir;
							while ((k < 50) && (dist == 0)) {
								int l = 0;
								while ((l < 5) && !detectableMat(craft.world.getBlockAt(refX, refY + l, refZ + (k * forDir)).getTypeId()) && !detectableMat(craft.world.getBlockAt(refX + lrDir, refY + l, refZ + (k * forDir)).getTypeId())) {
									l++;
								}
								if (l < 5) // mat detected
								{
									dist = k / 5;
								}
								k++;
							}
							displayArray[i][j] = dist;
						} else {
							int dist = 0;
							int k = 0;
							int refZ;
							if (lrDir < 0) {
								refZ = horizCenter + ((13 - i) * 2);
							} else {
								refZ = (horizCenter + ((i) * 2)) - 26;
							}
							int refY = (vertCenter + (j * 5)) - 17;
							int refX = start + forDir;
							while ((k < 50) && (dist == 0)) {
								int l = 0;
								while ((l < 5) && !detectableMat(craft.world.getBlockAt(refX + (k * forDir), refY + l, refZ).getTypeId()) && !detectableMat(craft.world.getBlockAt(refX + (k * forDir), refY + l, refZ + lrDir).getTypeId())) {
									l++;
								}
								if (l < 5) // mat detected
								{
									dist = k / 5;
								}
								k++;
							}
							displayArray[i][j] = dist;
						}
					}
				}

				BlockFace bf2;
				switch (sign.getBlock().getData()) {
					case (byte) 0x2:// n
						bf2 = BlockFace.WEST;
						break;
					case (byte) 0x3:// s
						bf2 = BlockFace.EAST;
						break;
					case (byte) 0x4:// w
						bf2 = BlockFace.SOUTH;
						break;
					case (byte) 0x5:// e
						bf2 = BlockFace.NORTH;
						break;
					default:
						bf2 = BlockFace.NORTH;
						break;
				}

				String line11 = "Status:";
				if (craft.hfOn) {
					line11 += "ON";
				} else {
					line11 += "OFF";
				}
				sign.setLine(1, line11);

				String line12 = "Depth:" + (63 - craft.minY);
				sign.setLine(2, line12);

				int jj = craft.minY - 1;
				int loop = 0;
				int duk = -1;
				while ((jj > 0) && (duk == -1)) {
					for (int i = craft.minX; i <= craft.maxX; i++) {
						for (int k = craft.minZ; k <= craft.maxZ; k++) {
							if (detectableMat(craft.world.getBlockAt(i, jj, k).getTypeId())) {
								duk = loop;
							}
						}
					}
					jj = jj - 1;
					loop++;
				}
				String dukString;
				if (duk == -1) {
					dukString = loop + "+";
				} else {
					dukString = "" + duk;
				}
				String line13 = "Fathometer:" + dukString;
				sign.setLine(3, line13);

				sign.update();

				if (sign.getBlock().getRelative(bf2).getTypeId() == 68) {
					Sign sign2 = (Sign) sign.getBlock().getRelative(bf2).getState();
					String line;
					for (int j = 7; j > 3; j--) {
						line = "";
						for (int i = 0; i < 13; i++) {
							if (displayArray[i][j] == 0) {
								line += "~";
							} else {
								line += 10 - displayArray[i][j];
							}
						}
						sign2.setLine(7 - j, line);
					}
					sign2.update();

					if (sign2.getBlock().getRelative(bf2).getTypeId() == 68) {
						Sign sign3 = (Sign) sign2.getBlock().getRelative(bf2).getState();
						String line3;
						for (int j = 7; j > 3; j--) {
							line3 = "";
							for (int i = 13; i < 26; i++) {
								if (displayArray[i][j] == 0) {
									line3 += "~";
								} else {
									line3 += 10 - displayArray[i][j];
								}
							}
							sign3.setLine(7 - j, line3);
						}
						sign3.update();

						if (sign2.getBlock().getRelative(BlockFace.DOWN).getTypeId() == 68) {
							Sign sign4 = (Sign) sign2.getBlock().getRelative(BlockFace.DOWN).getState();
							String line4;
							for (int j = 3; j >= 0; j--) {
								line4 = "";
								for (int i = 0; i < 13; i++) {
									if (displayArray[i][j] == 0) {
										line4 += "~";
									} else {
										line4 += 10 - displayArray[i][j];
									}
								}
								sign4.setLine(3 - j, line4);
							}
							sign4.update();

							if (sign4.getBlock().getRelative(bf2).getTypeId() == 68) {
								Sign sign5 = (Sign) sign4.getBlock().getRelative(bf2).getState();
								String line5;
								for (int j = 3; j >= 0; j--) {
									line5 = "";
									for (int i = 13; i < 26; i++) {
										if (displayArray[i][j] == 0) {
											line5 += "~";
										} else {
											line5 += 10 - displayArray[i][j];
										}
									}
									sign5.setLine(3 - j, line5);
								}
								sign5.update();
							}
						}
					}
				}

			} else // radar not on
			{
				sign.setLine(1, "");
				sign.setLine(2, "OFF");
				sign.setLine(3, "");
				sign.update();
			}

		} else {

			// if the first line of the sign is a craft type, get the matching craft type.
			CraftType craftType = CraftType.getCraftType(craftTypeName);

			// it is a registred craft type !
			if (craftType != null) {
				craft.signLoc = sign.getBlock().getLocation();
				if (craft.captainName != null) {
					if (craft.captainName.length() > 15) {
						sign.setLine(2, craft.captainName.substring(0, 14));
					} else {
						sign.setLine(2, craft.captainName);
					}
					sign.update();
				} else {
					sign.setLine(2, "No Captain");
					sign.update();
				}
				if (craft.driverName != null) {
					if (craft.driverName.length() > 15) {
						sign.setLine(3, craft.driverName.substring(0, 14));
					} else {
						sign.setLine(3, craft.driverName);
					}
					sign.update();
				} else {
					sign.setLine(3, "No Driver");
					sign.update();
				}
			}
		}

		if (craft.doCost) {
			int cost = 0;
			if (craftTypeName.equalsIgnoreCase("helm")) {
				cost = 50;
			} else if (craftTypeName.equalsIgnoreCase("nav")) {
				cost = 50;
			} else if (craftTypeName.equalsIgnoreCase("periscope")) {
				cost = 100;
			} else if (craftTypeName.equalsIgnoreCase("aa-gun")) {
				cost = 100;
			} else if (craftTypeName.equalsIgnoreCase("radar")) {
				cost = 200;
			} else if (craftTypeName.equalsIgnoreCase("radio")) {
				cost = 50;
			} else if (craftTypeName.equalsIgnoreCase("detector")) {
				cost = 50;
			} else if (craftTypeName.equalsIgnoreCase("sonar")) {
				cost = 250;
			} else if (craftTypeName.equalsIgnoreCase("hydrophone")) {
				cost = 100;
			} else if (craftTypeName.equalsIgnoreCase("subdrive")) {
				cost = 50;
			} else if (craftTypeName.equalsIgnoreCase("tdc")) {
				cost = 400;
			} else if (craftTypeName.equalsIgnoreCase("firecontrol")) {
				cost = 1000;
			} else if (craftTypeName.equalsIgnoreCase("passivesonar")) {
				cost = 2000;
			} else if (craftTypeName.equalsIgnoreCase("activesonar")) {
				cost = 2000;
			} else if (craftTypeName.equalsIgnoreCase("hfsonar")) {
				cost = 2000;
			} else if (craftTypeName.equalsIgnoreCase("engine")) {
				String engineTypeStr = sign.getLine(2).trim().toLowerCase().replaceAll(ChatColor.BLUE.toString(), "");
				if (engineTypeStr != null) {
					if (engineTypeStr.equalsIgnoreCase("Diesel 1")) {
						cost = 100;
					}
					if (engineTypeStr.equalsIgnoreCase("Motor 1")) {
						cost = 150;
					}
					if (engineTypeStr.equalsIgnoreCase("Diesel 2")) {
						cost = 250;
					}
					if (engineTypeStr.equalsIgnoreCase("Boiler 1")) {
						cost = 250;
					}
					if (engineTypeStr.equalsIgnoreCase("Diesel 3")) {
						cost = 1000;
					}
					if (engineTypeStr.equalsIgnoreCase("Gasoline 1")) {
						cost = 50;
					}
					if (engineTypeStr.equalsIgnoreCase("Boiler 2")) {
						cost = 600;
					}
					if (engineTypeStr.equalsIgnoreCase("Boiler 3")) {
						cost = 1250;
					}
					if (engineTypeStr.equalsIgnoreCase("Gasoline 2")) {
						cost = 100;
					}
					if (engineTypeStr.equalsIgnoreCase("Nuclear")) {
						cost = 10000;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 1")) {
						cost = 50;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 2")) {
						cost = 80;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 3")) {
						cost = 120;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 4")) {
						cost = 160;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 7")) {
						cost = 500;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 5")) {
						cost = 400;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 6")) {
						cost = 500;
					}
					if (engineTypeStr.equalsIgnoreCase("Airplane 8")) {
						cost = 5000;
					}
					if (engineTypeStr.equalsIgnoreCase("Tank 1")) {
						cost = 50;
					}
					if (engineTypeStr.equalsIgnoreCase("Tank 2")) {
						cost = 5000;
					}
				}
			}
			craft.vehicleCost += cost;
		}
	}

	@SuppressWarnings("deprecation")
	public void removeSupportBlocks() {
		short blockId;
		Block block;
		if( craft.matrix == null )
			return;
		
		for (int x = 0; x < craft.sizeX; x++) {
			for (int z = 0; z < craft.sizeZ; z++) {
				for (int y = craft.sizeY - 1; y > -1; y--) {
					// for (int y = 0; y < craft.sizeY; y++) {
					
					if( craft.matrix == null )
						return;
					blockId = craft.matrix[x][y][z];

					// craft block, replace by air
					if (BlocksInfo.needsSupport(blockId)) {

						// Block block = world.getBlockAt(posX + x, posY + y, posZ + z);
						block = getWorldBlock(x, y, z);

						if (blockId == 26) { // bed
							if (block.getData() >= 4) {
								continue;
							}
						}

						// special case for doors
						// we need to remove the lower part of the door only, or the door will pop
						// lower part have data 0 - 7, upper part have data 8 - 15

						setBlock(0, block);
					}
				}
			}
		}
	}

	// restore items that need a support but are not data blocks
	public void restoreSupportBlocks(int dx, int dy, int dz) {
		short blockId;

		for (int x = 0; x < craft.sizeX; x++) {
			for (int z = 0; z < craft.sizeZ; z++) {
				for (int y = 0; y < craft.sizeY; y++) {

					blockId = craft.matrix[x][y][z];

					if (BlocksInfo.needsSupport(blockId) && !BlocksInfo.isDataBlock(blockId) && !(BlocksInfo.isComplexBlock(blockId))) {

						setBlock(blockId, getWorldBlock(dx + x, dy + y, dz + z));
					}
				}
			}
		}
	}

	public void teleportPlayer(Entity p, int dx, int dy, int dz) {

		NavyCraft.instance.DebugMessage("Teleporting entity " + p.getEntityId(), 4);
		Location pLoc = p.getLocation();
		pLoc.setWorld(craft.world);
		pLoc.setX(pLoc.getX() + dx);
		pLoc.setY(pLoc.getY() + dy + .05);
		pLoc.setZ(pLoc.getZ() + dz);

		if (p instanceof Player) {
			playerTeleports.put((Player) p, pLoc);
		} else {
			p.teleport(pLoc);
		}

	}

	public void movePlayer(Entity p, int dx, int dy, int dz) {
		NavyCraft.instance.DebugMessage("Moving player", 4);
		int mccraftspeed = craft.speed;
		if (mccraftspeed > 2) {
			mccraftspeed = 2;
		}

		Vector pVel = p.getVelocity();

		if (dx > 0) {
			dx = craft.speed;
		} else {
			dx = craft.speed * -1;
		}
		if (dy > 0) {
			dy = craft.speed;
		} else {
			dy = craft.speed * -1;
		}
		if (dz > 0) {
			dz = craft.speed;
		} else {
			dz = craft.speed * -1;
		}
		pVel = pVel.add(new Vector(dx, dy, dz));


		if ((pVel.getX() > 10) || (pVel.getZ() > 10) || (pVel.getY() > 10)) {

			System.out.println("Velocity is too high, have to teleport " + p.getEntityId());
			Location pLoc = p.getLocation();
			pLoc.setX(pLoc.getX() + pVel.getX());
			pLoc.setY(pLoc.getY() + pVel.getY() + .05);
			pLoc.setZ(pLoc.getZ() + pVel.getZ());
			p.teleport(pLoc);
		} else {
			p.setVelocity(pVel);
		}
	}

	@SuppressWarnings("deprecation")
	public void railMove() {
		Byte deets = craft.railBlock.getData();

		if ((deets == 1) || (deets == 2) || (deets == 3)) {

		} else if ((deets == 0) || (deets == 4) || (deets == 5)) {
			// calculatedMove(0, 0, 1);
		}
		// 6-9 are turns

		// get the next block
		// check if its material is rails
		// if so, prep another move?
	}

	@SuppressWarnings("deprecation")
	public void sinkingThread() {
		if (craft.signLoc != null) {
			craft.signLoc.getBlock().setTypeId(0);
		}

		new Thread() {

			@Override
			public void run() {

				setPriority(Thread.MIN_PRIORITY);

				int sleepTime;
				if (!craft.type.canFly) {
					sleepTime = (int) (1000 + (3000 * Math.random()));
				} else {
					sleepTime = (int) (500 + (500 * Math.random()));
				}

				// taskNum = -1;
				try {
					int i = 0;
					while ((i < 120) && (craft != null) && (craft.minY > 2) && !stopSink) {
						if (NavyCraft.shutDown) { return; }

						if (!sinkUpdate) {
							sinkUpdate(false);
						}
						sleep(sleepTime);
						i++;
					}
					sinkUpdate(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void sinkUpdate(final boolean lastRun) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			if (NavyCraft.shutDown) { return; }

			if (craft != null) {
				if (sinkUpdate) { return; }
				sinkUpdate = true;
				if ((craft.minY > 2) && checkSink()) {
					if (!craft.type.canFly) {
						move(0, -1, 0);
					} else {
						move(craft.lastDX, -1, craft.lastDZ);
					}
				} else {
					stopSink = true;
					craft.doRemove = true;
				}
				sinkUpdate = false;
				if (lastRun) {
					craft.doRemove = true;
				}
			}
		});
	}

	@SuppressWarnings("deprecation")
	public boolean checkSink() {
		int blockId;
		Block newBlock;

		int solidCount = 0;
		for (int x = 0; x < craft.sizeX; x++) {
			for (int z = 0; z < craft.sizeZ; z++) {
				newBlock = craft.world.getBlockAt(craft.minX + x, craft.minY - 1, craft.minZ + z);
				blockId = newBlock.getTypeId();
				if ((blockId != 0) && !((blockId >= 8) && (blockId <= 11))) {
					solidCount++;
				}

			}
		}

		if ((solidCount / ((float) craft.sizeX * (float) craft.sizeZ)) >= 0.6f) {
			Block contactBlock = craft.world.getBlockAt(craft.minX + (craft.sizeX / 2), craft.minY, craft.minZ + (craft.sizeZ / 2));
			contactBlock.setTypeId(5);
			////////////////////////// ******************?////////////////
			contactBlock.getWorld().createExplosion(contactBlock.getLocation(), 8);

			return false;
		} else {
			return true;
		}
	}

	public boolean checkProtectedRegion(Location loc) {

		wgp = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		if ((wgp != null) && (loc != null)) {
			if ( !PermissionInterface.CheckEnabledWorld(loc) ) { return true; }
			RegionManager regionManager = wgp.getRegionManager(craft.world);

			ApplicableRegionSet set = regionManager.getApplicableRegions(loc);

			Iterator<ProtectedRegion> it = set.iterator();
			while (it.hasNext()) {
				String id = it.next().getId();

				String[] splits = id.split("_");
				if (splits.length == 2) {
					if (splits[1].equalsIgnoreCase("safedock") || splits[1].equalsIgnoreCase("red") || splits[1].equalsIgnoreCase("blue")) { return true; }
				}
			}
			return false;
		}
		return false;
	}

	public boolean checkSafeDockRegion(Location loc) {

		wgp = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		if ((wgp != null) && (loc != null)) {
			RegionManager regionManager = wgp.getRegionManager(craft.world);

			ApplicableRegionSet set = regionManager.getApplicableRegions(loc);

			Iterator<ProtectedRegion> it = set.iterator();
			while (it.hasNext()) {
				String id = it.next().getId();

				String[] splits = id.split("_");
				if (splits.length == 2) {
					if (splits[1].equalsIgnoreCase("safedock")) { return true; }
				}
			}
			return false;
		}
		return false;
	}

	public void moveUpdate() {

		if ((craft.noCaptain < 200) && (craft.stuckAutoTimer < 20)) {
			if ((!craft.enginesOn && (craft.speed == 0)) || craft.sinking) {
				if (craft.driverName != null) {
					Player p = plugin.getServer().getPlayer(craft.driverName);
					if (p != null) {
						p.sendMessage("Vehicle Stopped.");
					}
				}

				if ((craft.captainName != null) && (craft.driverName != craft.captainName)) {
					Player p = plugin.getServer().getPlayer(craft.captainName);
					if (p != null) {
						p.sendMessage("Vehicle Stopped.");
					}
				}
				craft.enginesOn = false;
				craft.setSpeed = 0;
				craft.speed = 0;
				craft.isMoving = false;
				stopSound = true;
			} else {
				cruiseUpdate();

			}
			if ((craft.captainName == null) && !craft.type.canZamboni && craft.isAutoCraft) {
				craft.noCaptain++;
			} else if (craft.noCaptain > 0) {
				craft.noCaptain = 0;
			}

			if (craft.isAutoCraft && (craft.speed == 0)) {
				craft.stuckAutoTimer++;
			} else if (craft.isAutoCraft && (craft.stuckAutoTimer > 0)) {
				craft.stuckAutoTimer = 0;
			}
		} else {
			craft.enginesOn = false;
			craft.isMoving = false;
		}

	}



	@SuppressWarnings("deprecation")
	public void cruiseUpdate() {

		if (craft.isDestroying || cruiseUpdate || ((craft.speed == 0) && (craft.setSpeed == 0))) { return; }
		cruiseUpdate = true;

		int dx = 0;
		int dy = craft.vertPlanes;
		int dz = 0;

		if ((dy != 0) && !craft.type.canDive && !craft.type.canFly && !craft.type.canDig) {
			dy = 0;
			craft.vertPlanes = 0;
		}

		if ((craft.rotation % 360) == 0) {
			dz = (int) (-1 * Math.signum(craft.gear));
			if (craft.rudder == -1) {
				dx = -1;
			} else if (craft.rudder == 1) {
				dx = 1;
			}
		} else if (craft.rotation == 180) {
			dz = (int) (1 * Math.signum(craft.gear));
			if (craft.rudder == -1) {
				dx = 1;
			} else if (craft.rudder == 1) {
				dx = -1;
			}
		} else if (craft.rotation == 90) {
			dx = (int) (1 * Math.signum(craft.gear));
			if (craft.rudder == -1) {
				dz = -1;
			} else if (craft.rudder == 1) {
				dz = 1;
			}
		} else// if( craft.rotation == 270 )
		{
			dx = (int) (-1 * Math.signum(craft.gear));
			if (craft.rudder == -1) {
				dz = 1;
			} else if (craft.rudder == 1) {
				dz = -1;
			}
		}

		if (craft.turnProgress > 0) {
			if (craft.turnProgress == (craft.type.turnRadius / 2)) {
				if (craft.rudder == 1) {
					craft.turn(90);
					craft.rudder = -craft.rudder;
					craft.turnProgress = craft.turnProgress - 1;
					cruiseUpdate = false;
					return;
				} else if (craft.rudder == -1) {
					craft.turn(270);
					craft.rudder = -craft.rudder;
					cruiseUpdate = false;
					craft.turnProgress = craft.turnProgress - 1;
					return;
				}
			}
			craft.turnProgress = craft.turnProgress - 1;
			if (craft.turnProgress == 0) {
				craft.rudder = 0;
			}
		}

		float actualSetSpeed = 0;

		float maxTypeSpd = 0;
		float powerRating = 0;
		int engVehicleType = 0; // 0=ship, 1=sub, 2= airplane, 3=tank
		float lastActualSpeed = 0;
		for (int engType = 0; engType < 20; engType++) {

			if (engType == 0) // Diesel 1
			{
				maxTypeSpd = 6;
				powerRating = 800;
				engVehicleType = 1;
			}
			if (engType == 1) // Motor 1
			{
				maxTypeSpd = 6;
				powerRating = 500;
				engVehicleType = 1;
			}
			if (engType == 2) // Diesel 2
			{
				maxTypeSpd = 8;
				powerRating = 1400;
				engVehicleType = 1;
			}
			if (engType == 3) // Boiler 1
			{
				maxTypeSpd = 8;
				powerRating = 1800;
				engVehicleType = 0;
			}
			if (engType == 4) // Diesel 3
			{
				maxTypeSpd = 10;
				powerRating = 2500;
				engVehicleType = 1;
			}
			if (engType == 5) // Gasoline 1
			{
				maxTypeSpd = 10;
				powerRating = 500;
				engVehicleType = 0;
			}
			if (engType == 6) // Boiler 2
			{
				maxTypeSpd = 10;
				powerRating = 2000;
				engVehicleType = 0;
			}
			if (engType == 7) // Boiler 3
			{
				maxTypeSpd = 10;
				powerRating = 4000;
				engVehicleType = 0;
			}
			if (engType == 8) // Gasoline 2
			{
				maxTypeSpd = 12;
				powerRating = 700;
				engVehicleType = 0;
			}
			if (engType == 9) // Nuclear
			{
				maxTypeSpd = 14;
				powerRating = 6000;
				engVehicleType = 0;
			}
			if (engType == 10) // Airplane 1
			{
				maxTypeSpd = 14;
				powerRating = 140;
				engVehicleType = 2;
			}
			if (engType == 11) // Airplane 2
			{
				maxTypeSpd = 16;
				powerRating = 140;
				engVehicleType = 2;
			}
			if (engType == 12) // Airplane 3
			{
				maxTypeSpd = 16;
				powerRating = 160;
				engVehicleType = 2;
			}
			if (engType == 13) // Airplane 4
			{
				maxTypeSpd = 18;
				powerRating = 160;
				engVehicleType = 2;
			}
			if (engType == 14) // Airplane 7
			{
				maxTypeSpd = 18;
				powerRating = 300;
				engVehicleType = 2;
			}
			if (engType == 15) // Airplane 5
			{
				maxTypeSpd = 24;
				powerRating = 180;
				engVehicleType = 2;
			}
			if (engType == 16) // Airplane 6
			{
				maxTypeSpd = 24;
				powerRating = 240;
				engVehicleType = 2;
			}
			if (engType == 17) // Airplane 8
			{
				maxTypeSpd = 28;
				powerRating = 500;
				engVehicleType = 2;
			}
			if (engType == 18) // Tank 1
			{
				maxTypeSpd = 4;
				powerRating = 200;
				engVehicleType = 3;
			}
			if (engType == 19) // Tank 2
			{
				maxTypeSpd = 8;
				powerRating = 400;
				engVehicleType = 3;
			}

			float totalActualSetSpeed = 0;
			for (int i : craft.engineIDLocs.keySet()) {
				if (craft.engineIDSetOn.get(i) && !craft.engineIDIsOn.get(i) && (craft.engineIDLocs.get(i) != null)) {
					if ( (craft.maxY >= 64)) {
						int engineType = craft.engineIDTypes.get(i);
						if ((engineType != 1) || (((engineType == 0) || (engineType == 2) || (engineType == 4)) && craft.submergedMode)) {
							craft.engineIDIsOn.put(i, true);
						}
					}
				} else if (craft.engineIDIsOn.get(i) && (craft.engineIDLocs.get(i) != null)) {
					//if ((craft.maxY < 70 && craft.sizeY > 10) ||  (craft.maxY < 63) || craft.submergedMode) {
					if ((craft.maxY < 64) || craft.submergedMode) {
						int engineType = craft.engineIDTypes.get(i);
						if (((engineType != 0) || !craft.submergedMode) && (engineType != 1) && ((engineType != 2) || !craft.submergedMode) && ((engineType != 4) || !craft.submergedMode) && (engineType != 9)) {
							craft.engineIDIsOn.put(i, false);
						}
					}

					if (((craft.type.canNavigate && !craft.type.isTerrestrial) && ((engVehicleType == 0) || (engVehicleType == 1))) || (craft.type.canDive && ((engVehicleType == 0) || (engVehicleType == 1) || (engType == 9))) || (craft.type.canFly && (engVehicleType == 2) && (craft.driverName != null)) || (craft.type.isTerrestrial && (engVehicleType == 3))) {
						if (craft.engineIDTypes.get(i) == engType) {
							float craftWeight;
							if (craft.weightStart < powerRating) {
								craftWeight = powerRating;
							} else {
								craftWeight = craft.weightStart;
							}
							//System.out.println("Engine " + i + " status=" + craft.engineIDOn.get(i) + " powerRating=" +
							//powerRating );
							if (craft.submergedMode && (engType != 9) && (engType != 1)) {
								totalActualSetSpeed += ((maxTypeSpd * powerRating * powerRating) / craftWeight / craftWeight) / 2;
							} else {
								totalActualSetSpeed += (((maxTypeSpd * powerRating * powerRating) / craftWeight / craftWeight));
							}
						}
					}
				}
			}

			float speedChange = totalActualSetSpeed - lastActualSpeed;
			if (speedChange > 0) {
				actualSetSpeed += speedChange;
				if (craft.submergedMode && (engType != 9) && (engType != 1) && (totalActualSetSpeed > (maxTypeSpd / 2))) {
					actualSetSpeed = maxTypeSpd / 2;
				} else if (actualSetSpeed > maxTypeSpd) {
					actualSetSpeed = maxTypeSpd;
				}
				lastActualSpeed = actualSetSpeed;
			}

		}
		actualSetSpeed = ((float) craft.setSpeed / (float) craft.type.maxEngineSpeed) * actualSetSpeed;

		int intActSetSpeed;
		if ((actualSetSpeed - (int) actualSetSpeed) >= 0.5) {
			intActSetSpeed = (int) actualSetSpeed + 1;
		} else {
			intActSetSpeed = (int) actualSetSpeed;
		}

		if (craft.speed < intActSetSpeed) {
			if (craft.speed < (intActSetSpeed / 2)) {
				craft.speed += 2;
			} else {
				craft.speed++;
			}
		} else if (craft.speed > intActSetSpeed) {
			if ((craft.speed / 2) > intActSetSpeed) {
				craft.speed -= 2;
			} else {
				craft.speed--;
			}
		}

		if (craft.type.canFly && !craft.onGround) {
			if (craft.speed == 0) {
				craft.speed = 1;
			}
		}

		if (craft.type.canZamboni) {

			if (craft.minY == 42) {
				if ((craft.minX % 100) == 50) {
					if (craft.minZ <= -800) {
						if (craft.minX >= 1250) {
							dz = 0;
							dx = 0;
							dy = -39;
							craft.speed = 1;
						} else {
							dz = 0;
							dx = 50;
							craft.speed = 1;
						}
					} else {
						dx = 0;
						dz = -1;
					}
				} else if ((craft.minX % 100) == 0) {
					if (craft.maxZ >= 450) {
						dz = 0;
						dx = 50;
						craft.speed = 1;
					} else {
						dx = 0;
						dz = 1;
					}

				}
			} else if (craft.minY == 3) {
				if ((craft.minX % 100) == 50) {
					if (craft.minZ >= 450) {
						if (craft.minX <= 50) {
							dz = 0;
							dx = 0;
							dy = 39;
							craft.speed = 1;
						} else {
							dz = 0;
							dx = -50;
							craft.speed = 1;
						}
					} else {
						dx = 0;
						dz = 1;
					}
				} else if ((craft.minX % 100) == 0) {
					if (craft.maxZ <= -800) {
						dz = 0;
						dx = -50;
						craft.speed = 1;
					} else {
						dx = 0;
						dz = -1;
					}

				}
			}
		}

		if (craft.isAutoCraft) {
			if (!NavyCraft.checkSafeDockRegion(craft.getLocation()) && (craft.turnProgress == 0)) {
				int lrDir, start, forDir, horizCenter, vertCenter;
				boolean lrX = false;
				switch (craft.rotation) {
					case 180:
						horizCenter = (int) (craft.minX + (craft.sizeX / 2.0f));
						vertCenter = (int) (craft.minY + (craft.sizeY / 2.0f));
						lrDir = -1;
						lrX = true;
						start = craft.maxZ;
						forDir = 1;
						break;
					case 90:
						horizCenter = (int) (craft.minZ + (craft.sizeZ / 2.0f));
						vertCenter = (int) (craft.minY + (craft.sizeY / 2.0f));
						lrDir = 1;
						start = craft.maxX;
						forDir = 1;
						break;
					case 270:
						horizCenter = (int) (craft.minZ + (craft.sizeZ / 2.0f));
						vertCenter = (int) (craft.minY + (craft.sizeY / 2.0f));
						lrDir = -1;
						start = craft.minX;
						forDir = -1;
						break;
					default:
						horizCenter = (int) (craft.minX + (craft.sizeX / 2.0f));
						vertCenter = (int) (craft.minY + (craft.sizeY / 2.0f));
						lrDir = 1;
						lrX = true;
						start = craft.minZ;
						forDir = -1;
						break;
				}

				int k = 0;
				boolean obstacleFound = false;
				boolean applyLeft = false;
				while ((k < 40) && !obstacleFound) {
					int i = 0;
					while ((i < 16) && !obstacleFound) {
						int j = 0;
						while ((j < 24) && !obstacleFound) {
							if (lrX) {
								int refX;
								if (lrDir < 0) {
									refX = horizCenter + (8 - i);
								} else {
									refX = (horizCenter + (i)) - 8;
								}
								int refY = (vertCenter + j) - 12;
								int refZ = start + forDir + (k * forDir);


								if (detectableMat(craft.world.getBlockAt(refX, refY, refZ).getTypeId())) {
									obstacleFound = true;
									if (i > 8) {
										applyLeft = true;
									}
								}
							} else {
								int refZ;
								if (lrDir < 0) {
									refZ = horizCenter + (8 - i);
								} else {
									refZ = (horizCenter + (i)) - 8;
								}
								int refY = (vertCenter + j) - 12;
								int refX = start + forDir + (k * forDir);

								if (detectableMat(craft.world.getBlockAt(refX, refY, refZ).getTypeId())) {
									obstacleFound = true;
									if (i > 8) {
										applyLeft = true;
									}
								}
							}
							j++;
						}
						i++;
					}
					k++;
				}

				if (obstacleFound && (craft.turnProgress == 0)) {

					if (applyLeft) {

						if (craft.rudder != -1) {
							craft.rudder = -1;
						}
					} else {

						if (craft.rudder != 1) {
							craft.rudder = 1;
						}
					}
				} else if (craft.turnProgress == 0) {


					if (craft.routeID.equalsIgnoreCase("AB1")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockZ() < (-900 - (Math.random() * 100))) {
								if (craft.rotation == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockX() > (-500 + (Math.random() * 100))) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 2) {
							if (craft.getLocation().getBlockZ() < -1550) {
								if (craft.rotation == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockX() > -60) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("AB2")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockZ() < (-600 - (Math.random() * 200))) {
								if (craft.rotation == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockX() > -60) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockZ() < -1550) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("AB3")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockZ() < 450) {
								if (craft.rotation == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockX() > (-900 + (Math.random() * 200))) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 2) {
							if (craft.getLocation().getBlockZ() < (-600 - (Math.random() * 200))) {
								if (craft.rotation == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 3) {
							if (craft.getLocation().getBlockX() > -60) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockZ() < -1550) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("AC1")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockZ() < 1050) {
								if (craft.rotation == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockX() > (-900 + (Math.random() * 200))) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 2) {
							if (craft.getLocation().getBlockZ() < -750) {
								if (craft.rotation == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockX() > 1750) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("AC2")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockZ() < -300) {
								if (craft.rotation == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockX() > (1100 + (Math.random() * 200))) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 2) {
							if (craft.getLocation().getBlockZ() < -750) {
								if (craft.rotation == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockX() > 1750) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("AC3")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockZ() < 1450) {
								if (craft.rotation == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockX() > (800 + (Math.random() * 200))) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 2) {
							if (craft.getLocation().getBlockZ() < (900 - (Math.random() * 200))) {
								if (craft.rotation == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 3) {
							if (craft.getLocation().getBlockX() > (1400 + (Math.random() * 200))) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 4) {
							if (craft.getLocation().getBlockZ() < -750) {
								if (craft.rotation == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockX() > 1750) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("CB1")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockX() < 50) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockZ() < -1550) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("CB2")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockX() < (1000 - (Math.random() * 300))) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockZ() < (-1300 - (Math.random() * 200))) {
								if (craft.rotation == 270) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 2) {
							if (craft.getLocation().getBlockX() < 50) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockZ() < -1550) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("CA1")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockX() < -1500) {
								if (craft.rotation == 180) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockZ() > 1400) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("CA2")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockX() < (1600 - (Math.random() * 200))) {
								if (craft.rotation == 180) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockZ() > (200 + (Math.random() * 200))) {
								if (craft.rotation == 270) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 2) {
							if (craft.getLocation().getBlockX() < (1000 - (Math.random() * 200))) {
								if (craft.rotation == 180) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 3) {
							if (craft.getLocation().getBlockZ() > 1490) {
								if (craft.rotation == 270) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockX() < -1450) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("BC1")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockZ() > -1500) {
								if ((craft.rotation % 360) == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockX() > (400 + (Math.random() * 300))) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 2) {
							if (craft.getLocation().getBlockZ() < -1800) {
								if ((craft.rotation % 360) == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 3) {
							if (craft.getLocation().getBlockX() > 1500) {
								if ((craft.rotation % 360) == 180) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 4) {
							if (craft.getLocation().getBlockZ() > -750) {
								if ((craft.rotation % 360) == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockX() > 1750) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("BC2")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockZ() > (-1200 + (Math.random() * 200))) {
								if ((craft.rotation % 360) == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockX() > 1500) {
								if ((craft.rotation % 360) == 180) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 2) {
							if (craft.getLocation().getBlockZ() > -750) {
								if ((craft.rotation % 360) == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockX() > 1750) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("BA1")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockZ() > -600) {
								if ((craft.rotation % 360) == 270) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockX() < -1450) {
								if ((craft.rotation % 360) == 180) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockZ() > 1400) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("BA2")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockZ() > -600) {
								if ((craft.rotation % 360) == 270) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockX() < -800) {
								if ((craft.rotation % 360) == 180) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 2) {
							if (craft.getLocation().getBlockZ() > 1000) {
								if ((craft.rotation % 360) == 270) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 3) {
							if (craft.getLocation().getBlockX() < -1450) {
								if ((craft.rotation % 360) == 180) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockZ() > 1400) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("D1")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockX() < 1100) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockZ() < 1400) {
								if ((craft.rotation % 360) == 270) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockX() < -1400) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("D2")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockX() < 1100) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockZ() < 0) {
								if ((craft.rotation % 360) == 270) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 2) {
							if (craft.getLocation().getBlockX() < 500) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 3) {
							if (craft.getLocation().getBlockZ() < -500) {
								if ((craft.rotation % 360) == 270) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 4) {
							if (craft.getLocation().getBlockX() < 50) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockZ() < -1550) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					} else if (craft.routeID.equalsIgnoreCase("D3")) {
						if (craft.routeStage == 0) {
							if (craft.getLocation().getBlockX() < 1100) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 1) {
							if (craft.getLocation().getBlockZ() < 700) {
								if ((craft.rotation % 360) == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 2) {
							if (craft.getLocation().getBlockX() > 1400) {
								if ((craft.rotation % 360) == 0) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, -1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else if (craft.routeStage == 3) {
							if (craft.getLocation().getBlockZ() < -750) {
								if ((craft.rotation % 360) == 90) {
									craft.routeStage++;
								} else {
									craft.rudderChange(null, 1, true);
								}
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						} else {
							if (craft.getLocation().getBlockX() > 1750) {
								craft.doDestroy = true;
								return;
							} else {
								if (craft.rudder != 0) {
									craft.rudder = 0;
								}
							}
						}
					}
				}
			}
		}

		craft.lastDX = dx;
		craft.lastDZ = dz;
		calculateMove(dx, dy, dz);
		cruiseUpdate = false;
	}

	public static void battleLogger(String str) {

		String path = File.separator + "BattleLog.txt";
		File file = new File(path);
		try {
			FileWriter fw = new FileWriter(file.getName(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			Date now = new Date();
			bw.write(DateFormat.getDateInstance(DateFormat.SHORT).format(now) + " " + DateFormat.getTimeInstance().format(now) + " - " + str);
			bw.newLine();

			bw.flush();
			bw.close();
		} catch (Exception e) {
			System.out.println("Battle log failure");
		}
	}

	public void sinkBroadcast() {
		String broadcastMsg = "";

		broadcastMsg += ChatColor.YELLOW;

		if (craft.captainName != null) {
			broadcastMsg += craft.captainName + "'s ";
		}

		if (craft.customName != null) {
			broadcastMsg += craft.customName.toUpperCase() + " (" + craft.name.toUpperCase() + " class)";
		} else {
			broadcastMsg += craft.name.toUpperCase() + " class";
		}

		broadcastMsg += ChatColor.WHITE + " - " + craft.weightStart + " tons -";

		if (craft.type.canFly) {
			broadcastMsg += ChatColor.YELLOW + " was shot down.";
		} else if (craft.type.canNavigate || craft.type.canDive) {
			broadcastMsg += ChatColor.YELLOW + " was sunk.";
		} else {
			broadcastMsg += ChatColor.YELLOW + " was destroyed.";
		}

		plugin.getServer().broadcastMessage(broadcastMsg);

		if (craft.damagers.isEmpty()) {

		} else {
			HashMap<Craft, Integer> craftDamagers = new HashMap<>();
			HashMap<Player, Integer> uncrewedPlayers = new HashMap<>();
			int totalDamage = 0;
			for (Player p : craft.damagers.keySet()) {
				totalDamage += craft.damagers.get(p);

				Craft c = Craft.getPlayerCraft(p);
				if (c != null) {
					if (craftDamagers.containsKey(c)) {
						craftDamagers.put(c, craft.damagers.get(p) + craftDamagers.get(c));
					} else {
						craftDamagers.put(c, craft.damagers.get(p));
					}
				} else {
					if (uncrewedPlayers.containsKey(p)) {
						uncrewedPlayers.put(p, craft.damagers.get(p) + uncrewedPlayers.get(p));
					} else {
						uncrewedPlayers.put(p, craft.damagers.get(p));
					}
				}
			}

			totalDamage += craft.uncreditedDamage;

			Craft topCraft;
			topCraft = null;
			int topScore = 0;

			if (craftDamagers != null) {

				for (Craft c : craftDamagers.keySet()) {
					broadcastMsg = "";
					int score = (int) (((float) craftDamagers.get(c) / (float) totalDamage) * 100.0f);
					if (score > topScore) {
						topScore = score;
						topCraft = c;
					}
					broadcastMsg = " " + ChatColor.WHITE + score + "% - ";


					broadcastMsg += ChatColor.YELLOW;


					if (c.captainName != null) {
						broadcastMsg += c.captainName.toUpperCase() + "'s ";
					}

					if (c.customName != null) {
						broadcastMsg += c.customName.toUpperCase() + " (" + c.name + " class)";
					} else {
						broadcastMsg += c.name.toUpperCase() + " class";
					}

					plugin.getServer().broadcastMessage(broadcastMsg);
				}
			}

			Player topPlayer;
			topPlayer = null;

			if (!uncrewedPlayers.isEmpty()) {
				for (Player p : uncrewedPlayers.keySet()) {
					broadcastMsg = "";
					int score = (int) (((float) uncrewedPlayers.get(p) / (float) totalDamage) * 100.0f);
					if (score > topScore) {
						topScore = score;
						topPlayer = p;
					}

					broadcastMsg = " " + ChatColor.WHITE + score + "% - ";

					broadcastMsg += ChatColor.YELLOW;

					broadcastMsg += p.getName();

					plugin.getServer().broadcastMessage(broadcastMsg);
				}
			}

			if (craft.uncreditedDamage > 0)// && craft.world.getName().equalsIgnoreCase("navalbattlezone")) )
			{
				broadcastMsg = "";
				int score = (int) (((float) craft.uncreditedDamage / (float) totalDamage) * 100.0f);
				broadcastMsg = " " + ChatColor.WHITE + score + "% - ";

				broadcastMsg += ChatColor.YELLOW;

				broadcastMsg += "Unknown damage";
				plugin.getServer().broadcastMessage(broadcastMsg);
			}

			if (craft.isAutoCraft) {
				Essentials ess;
				ess = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
				if (ess == null) { return; }

				if (topPlayer != null) {
					if (craft.crewHistory.contains(topPlayer.getName())) { return; }
					plugin.getServer().broadcastMessage(ChatColor.GREEN + topPlayer.getName() + " receives a $" + craft.sinkValue + " bonus!");
					try {
						ess.getUser(topPlayer).giveMoney(new BigDecimal(craft.sinkValue));
					} catch (MaxMoneyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if ((topCraft != null) && (topCraft != craft)) {
					for (String s : topCraft.crewNames) {
						if (craft.crewHistory.contains(s)) { return; }
					}

					String dispName;
					if (topCraft.customName != null) {
						dispName = topCraft.customName;
					} else {
						dispName = topCraft.name;
					}
					plugin.getServer().broadcastMessage(ChatColor.GREEN + "The crew of the " + ChatColor.WHITE + dispName.toUpperCase() + ChatColor.YELLOW + " receives a $" + craft.sinkValue + " bonus!");
					for (String s : topCraft.crewNames) {
						Player p = plugin.getServer().getPlayer(s);
						if (p != null) {
							try {
								ess.getUser(p).giveMoney(new BigDecimal(craft.sinkValue));
							} catch (MaxMoneyException e) {
							}
						}
					}
				}
			}

			if (!PermissionInterface.CheckEnabledWorld(craft.getLocation()) && (!craft.crewNames.isEmpty() || (((System.currentTimeMillis() - craft.abandonTime) / 1000) < 180) || craft.isAutoCraft)) {
				if (topPlayer != null) {
					if (craft.crewHistory.contains(topPlayer.getName())) { return; }
					int newExp = craft.blockCountStart;
					plugin.getServer().broadcastMessage(ChatColor.GREEN + topPlayer.getName() + " receives " + ChatColor.YELLOW + newExp + ChatColor.GREEN + " rank points!");

					topPlayer.sendMessage(ChatColor.GRAY + "You now have " + ChatColor.WHITE + newExp + ChatColor.GRAY + " rank points.");
				} else if ((topCraft != null) && (topCraft != craft)) {
					for (String s : topCraft.crewNames) {
						if (craft.crewHistory.contains(s)) { return; }
					}

					int newExp = craft.blockCountStart;
					String dispName;
					if (topCraft.customName != null) {
						dispName = topCraft.customName.toUpperCase();
					} else {
						dispName = topCraft.name.toUpperCase();
					}

					plugin.getServer().broadcastMessage(ChatColor.GREEN + "The crew of the " + ChatColor.WHITE + dispName + ChatColor.GREEN + " receives " + ChatColor.YELLOW + newExp + ChatColor.GREEN + " rank points!");

					int playerNewExp = newExp;
					for (String s : topCraft.crewNames) {
						Player p = plugin.getServer().getPlayer(s);
						if (p != null) {
							playerNewExp = newExp;

							p.sendMessage(ChatColor.GRAY + "You now have " + ChatColor.WHITE + playerNewExp + ChatColor.GRAY + " rank points.");
						}
					}
				}
			}
		}
	}

	public String idIntToString(int id) {
		switch (id % 26) {
			case 0:
				return "A";
			case 1:
				return "B";
			case 2:
				return "C";
			case 3:
				return "D";
			case 4:
				return "E";
			case 5:
				return "F";
			case 6:
				return "G";
			case 7:
				return "H";
			case 8:
				return "I";
			case 9:
				return "J";
			case 10:
				return "K";
			case 11:
				return "L";
			case 12:
				return "M";
			case 13:
				return "N";
			case 14:
				return "O";
			case 15:
				return "P";
			case 16:
				return "Q";
			case 17:
				return "R";
			case 18:
				return "S";
			case 19:
				return "T";
			case 20:
				return "U";
			case 21:
				return "V";
			case 22:
				return "W";
			case 23:
				return "X";
			case 24:
				return "Y";
			case 25:
				return "Z";
			default:
				return "&";
		}
	}

	public boolean detectableMat(int itemId) {
		if ((itemId == 8) || (itemId == 9) || (itemId == 0) || (itemId == 79)) {
			return false;
		} else {
			return true;
		}
	}

	public void soundThread(final Craft c, final int engineIndex) {
		new Thread() {

			@Override
			public void run() {

				setPriority(Thread.MIN_PRIORITY);

				// taskNum = -1;
				try {

					if (engineIndex > 0) {
						sleep((int) ((Math.random() * 500) + (1000 * engineIndex)));
					}

					int sleep = 1000;
					int i = 0;
					while (!stopSound && !c.isDestroying && !c.sinking) {
						if (NavyCraft.shutDown) { return; }
						if ((i % 4) == 0) {
							sleep = (2000 - (int) ((1000 * (float) (c.setSpeed * Math.abs(c.gear))) / (c.type.maxEngineSpeed * c.type.maxForwardGear))) / 4;
							if (c.type.canFly) {
								sleep = (1000 - (int) ((500 * (float) (c.setSpeed * Math.abs(c.gear))) / (c.type.maxEngineSpeed * c.type.maxForwardGear))) / 4;
							}
						}
						sleep(sleep);
						if (NavyCraft.shutDown) { return; }
						soundUpdate(c, engineIndex, i);
						i++;

					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void soundUpdate(final Craft c, final int engineIndex, final int i) {
		if (NavyCraft.shutDown) { return; }
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			if (NavyCraft.shutDown) { return; }

			Location noEngineLoc = null;

			// return if engine not on
			if ((engineIndex != -1) && (c.engineIDIsOn.containsKey(engineIndex) && (!c.engineIDIsOn.get(engineIndex) || (c.type.canFly && (c.driverName == null))))) {
				return;
			} else if ((engineIndex == -1) && (c.type.canFly && (c.driverName == null))) { return; }

			if (engineIndex == -1) {
				noEngineLoc = new Location(c.world, c.minX + (c.sizeX / 2), c.minY + (c.sizeY / 2), c.minZ + (c.sizeZ / 2));
			} else {
				if (c.engineIDLocs.containsKey(engineIndex) && (c.engineIDLocs.get(engineIndex) == null)) {
					c.engineIDIsOn.put(engineIndex, false);
				}
			}

			World cw = c.world;
			float volume = ((float) (c.setSpeed * Math.abs(c.gear)) / (float) (c.type.maxEngineSpeed * c.type.maxForwardGear)) + 1.0f;

			if (c.type.canFly) {
				volume = ((2.0f * (c.setSpeed * Math.abs(c.gear))) / (c.type.maxEngineSpeed * c.type.maxForwardGear)) + 1.0f;

			}

			Location cLoc = null;

			if (engineIndex > -1) {
				cLoc = c.engineIDLocs.get(engineIndex);
			}
			if ((cLoc == null) && (noEngineLoc == null)) { return; }

			// System.out.println("Play engine sound-" + engineIndex);
			if (c.setSpeed > 0) {
				if (noEngineLoc != null) {
					float pitch3 = ((float) c.setSpeed / (float) c.type.maxEngineSpeed) + 0.5f;
					if ((i % 5) == 0) {

						if (craft.type.canFly) {
							cw.playSound(noEngineLoc, Sound.ENTITY_ARROW_HIT, volume, pitch3);
						} else if (craft.type.isTerrestrial) {
							cw.playSound(noEngineLoc, Sound.BLOCK_GRAVEL_STEP, volume, pitch3);
						} else {
							cw.playSound(noEngineLoc, Sound.BLOCK_WATER_AMBIENT, volume, pitch3);
						}
					}
				} else if (c.submergedMode) {
					float pitch4 = ((float) c.setSpeed / (float) c.type.maxEngineSpeed) + 0.5f;
					if ((i % 2) == 0) {
						cw.playSound(cLoc, Sound.BLOCK_WATER_AMBIENT, volume * 2, pitch4);
					}
				} else {
					float pitch1 = 1.0f;
					float pitch2 = 2.0f;
					int engType = c.engineIDTypes.get(engineIndex);
					if (engType == 0) // Diesel 1
					{
						pitch1 = 0.8f;
						pitch2 = 0.5f;
						if (((i) % 4) == 0) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch1);
						} else if (((i) % 4) == 1) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch2);
						} else if (((i) % 4) == 2) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_CONTRACT, volume, pitch1);
						} else if (((i) % 4) == 3) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_CONTRACT, volume, pitch2);
						}
					}
					if (engType == 1) // Motor 1
					{
						float pitch5 = ((float) c.setSpeed / (float) c.type.maxEngineSpeed) + 0.5f;
						if ((i % 2) == 0) {
							cw.playSound(cLoc, Sound.BLOCK_WATER_AMBIENT, volume, pitch5);
						}
					}
					if (engType == 2) // Diesel 2
					{
						pitch1 = 0.8f;
						pitch2 = 0.5f;
						if (((i) % 4) == 0) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch1);
						} else if (((i) % 4) == 1) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch2);
						} else if (((i) % 4) == 2) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_CONTRACT, volume, pitch1);
						} else if (((i) % 4) == 3) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_CONTRACT, volume, pitch2);
						}
					}
					if (engType == 3) // Boiler 1
					{
						pitch1 = 0.6f;
						pitch2 = 0.5f;
						if (((i) % 4) == 0) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch1);
						} else if (((i) % 4) == 2) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch2);
						}
					}
					if (engType == 4) // Diesel 3
					{
						pitch1 = 0.8f;
						pitch2 = 0.5f;
						if (((i) % 4) == 0) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch1);
						} else if (((i) % 4) == 1) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch2);
						} else if (((i) % 4) == 2) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_CONTRACT, volume, pitch1);
						} else if (((i) % 4) == 3) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_CONTRACT, volume, pitch2);
						}
					}
					if (engType == 5) // Gasoline 1
					{
						pitch1 = 1.2f;
						pitch2 = 0.8f;
						if (((i) % 4) == 0) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch1);
						} else if (((i) % 4) == 1) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch2);
						} else if (((i) % 4) == 2) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch1);
						} else if (((i) % 4) == 3) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch2);
						}
					}
					if (engType == 6) // Boiler 2
					{
						pitch1 = 0.6f;
						pitch2 = 0.5f;
						if (((i) % 4) == 0) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch1);
						} else if (((i) % 4) == 2) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch2);
						}
					}
					if (engType == 7) // Boiler 3
					{
						pitch1 = 0.6f;
						pitch2 = 0.5f;
						if (((i) % 4) == 0) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch1);
						} else if (((i) % 4) == 2) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch2);
						}
					}
					if (engType == 8) // Gasoline 2
					{
						pitch1 = 1.4f;
						pitch2 = 1.0f;
						if (((i) % 4) == 0) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch1);
						} else if (((i) % 4) == 1) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch2);
						} else if (((i) % 4) == 2) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch1);
						} else if (((i) % 4) == 3) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch2);
						}
					}
					if (engType == 9) // Nuclear
					{
						pitch1 = 0.6f;
						pitch2 = 0.5f;
						if (((i) % 4) == 0) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_CONTRACT, volume, pitch1);
						} else if (((i) % 4) == 2) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_CONTRACT, volume, pitch2);
						}
					}
					if ((engType >= 10) && (engType <= 17)) // Airplanes
					{
						pitch1 = 1.4f;
						pitch2 = 1.1f;
						if (((i) % 4) == 0) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch1);
						} else if (((i) % 4) == 2) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_CONTRACT, volume, pitch1);
						} else if (((i) % 4) == 3) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_CONTRACT, volume, pitch2);
						}
					}
					if ((engType == 18) || (engType == 19)) // Tanks
					{
						pitch1 = 0.8f;
						pitch2 = 0.5f;
						if (((i) % 4) == 0) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch1);
						} else if (((i) % 4) == 1) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_EXTEND, volume, pitch2);
						} else if (((i) % 4) == 3) {
							playEngineSound(cLoc, Sound.BLOCK_PISTON_CONTRACT, volume, pitch2);
						}
					}

				}
			}

		});
	}
	
	public void playEngineSound( Location loc, Sound sound, float volume, float pitch )
	{
		for( Player p : loc.getWorld().getPlayers() )
		{
			if( NavyCraft.playerEngineVolumes.containsKey(p) )
				p.playSound(loc,  sound,  volume*NavyCraft.playerEngineVolumes.get(p)*.01f,  pitch);
			else
				p.playSound(loc,  sound,  volume,  pitch);
		}
	}

	public void teleportUpdate() {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			if ((playerTeleports != null) && !playerTeleports.isEmpty()) {
				for (Player p : playerTeleports.keySet()) {
					Location newLoc = new Location(playerTeleports.get(p).getWorld(), playerTeleports.get(p).getX(), playerTeleports.get(p).getY(), playerTeleports.get(p).getZ());
					newLoc.setPitch(p.getLocation().getPitch());
					newLoc.setYaw(p.getLocation().getYaw());
					p.teleport(newLoc);
				}
			}

			craft.isMovingPlayers = false;

		}, 1);
	}
	
	public boolean checkForWeapons()
	{
		for( Weapon w : AimCannon.weapons )
		{
			if( w.warhead.getLocation().getBlockX() >= (craft.minX-1) && w.warhead.getLocation().getBlockX() < (craft.maxX+1) )
				if( w.warhead.getLocation().getBlockZ() >= (craft.minZ-1) && w.warhead.getLocation().getBlockZ() < (craft.maxZ+1) )
					if( w.warhead.getLocation().getBlockY() >= (craft.minY-1) && w.warhead.getLocation().getBlockY() < (craft.maxY+1) )
						return true;
		}
		return false;
	}
}
