package com.maximuspayne.navycraft;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.earth2me.essentials.Essentials;
import com.maximuspayne.aimcannon.AimCannon;
import com.maximuspayne.aimcannon.AimCannonPlayerListener;
import com.maximuspayne.aimcannon.OneCannon;
import com.maximuspayne.aimcannon.Weapon;
import com.maximuspayne.navycraft.plugins.PermissionInterface;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.ess3.api.MaxMoneyException;

public class MoveCraft_PlayerListener implements Listener {

	public Plugin plugin;
	public WorldGuardPlugin wgp;
	public WorldEditPlugin wep;

	Thread timerThread;

	public MoveCraft_PlayerListener(Plugin p) {
		plugin = p;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		Craft craft = Craft.getPlayerCraft(player);

		if (craft != null) {
			if (craft.isNameOnBoard.get(player.getName())) {
				Craft.reboardNames.put(player.getName(), craft);

				craft.isNameOnBoard.put(player.getName(), false);
				if (craft.driverName == player.getName()) {
					craft.haveControl = false;
					craft.releaseHelm();
				}

				boolean abandonCheck = true;
				for (String s : craft.isNameOnBoard.keySet()) {
					if (craft.isNameOnBoard.get(s)) {
						abandonCheck = false;
					}
				}

				if (abandonCheck) {
					craft.abandoned = true;
					craft.captainAbandoned = true;
				} else if (player.getName() == craft.captainName) {
					craft.captainAbandoned = true;
				}
			}

			for (Periscope p : craft.periscopes) {
				if (p.user == player) {
					p.user = null;
					break;
				}
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (Craft.reboardNames.containsKey(player.getName())) {
			if ((Craft.reboardNames.get(player.getName()) != null)
					&& Craft.reboardNames.get(player.getName()).crewNames.contains(player.getName())) {
				Craft c = Craft.reboardNames.get(player.getName());
				Location loc = new Location(c.world, c.minX + (c.sizeX / 2), c.maxY, c.minZ + (c.sizeZ / 2));
				player.teleport(loc);

			}
			Craft.reboardNames.remove(player.getName());

		}

	}

	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		String deathMsg = event.getDeathMessage();

		String[] msgWords = deathMsg.split("\\s");
		if (msgWords.length == 5) {
			if (msgWords[1].equalsIgnoreCase("was") && msgWords[3].equalsIgnoreCase("by")) {
				Player p = plugin.getServer().getPlayer(msgWords[4]);
				if ((p != null) && PermissionInterface.CheckEnabledWorld(p.getLocation())) {
					int newExp = 100;
					plugin.getServer().broadcastMessage(ChatColor.GREEN + p.getName() + " receives " + ChatColor.YELLOW
							+ newExp + ChatColor.GREEN + " rank points!");
				}
			}
		}

		if (!NavyCraft.playerKits.isEmpty() && NavyCraft.playerKits.contains(event.getEntity().getName())) {
			NavyCraft.playerKits.remove(event.getEntity().getName());
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		Craft craft = Craft.getPlayerCraft(player);

		

		if (NavyCraft.aaGunnersList.contains(player) && ((event.getFrom().getBlockX() != event.getTo().getBlockX())
				|| (event.getFrom().getBlockZ() != event.getTo().getBlockZ()))) {
			NavyCraft.aaGunnersList.remove(player);
			if (player.getInventory().contains(Material.BLAZE_ROD)) {
				player.getInventory().remove(Material.BLAZE_ROD);
			}
			player.sendMessage(ChatColor.YELLOW + "You get off the AA-Gun.");

		} else if (craft != null) {
			// craft.setSpeed(1);

			if (craft.isMovingPlayers) {
				return;
			}

			Periscope playerScope = null;
			for (Periscope p : craft.periscopes) {
				if (p.user == player) {
					playerScope = p;
					break;
				}
			}

			if (!craft.isNameOnBoard.isEmpty() && craft.isNameOnBoard.containsKey(player.getName())
					&& craft.isNameOnBoard.get(player.getName()) && !craft.isOnCraft(player, false)) {
				if (craft.customName != null) {
					player.sendMessage(ChatColor.YELLOW + "You get off the " + craft.customName);
				} else {
					player.sendMessage(ChatColor.YELLOW + "You get off the " + craft.name + " class.");

				}

				craft.isNameOnBoard.put(player.getName(), false);
				if (craft.driverName == player.getName()) {
					player.sendMessage(ChatColor.YELLOW + "You release the helm");
					craft.haveControl = false;
					craft.releaseHelm();
				}

				boolean abandonCheck = true;
				for (String s : craft.isNameOnBoard.keySet()) {
					if (craft.isNameOnBoard.get(s)) {
						abandonCheck = false;
					}
				}

				if (abandonCheck) {
					craft.abandoned = true;
					craft.captainAbandoned = true;
					// aft.abandonTimerThread(player, craft, false);
				} else if (player.getName() == craft.captainName) {
					craft.captainAbandoned = true;
					// craft.abandonTimerThread(player, craft, true);
				}


			} else if (craft.isNameOnBoard.containsKey(player.getName()) && !craft.isNameOnBoard.get(player.getName())
					&& craft.isOnCraft(player, false)) {
				player.sendMessage(ChatColor.YELLOW + "Welcome on board");

				craft.isNameOnBoard.put(player.getName(), true);

				if (craft.abandoned) {
					craft.abandoned = false;
				}
				if (craft.captainAbandoned && player.getName().equalsIgnoreCase(craft.captainName)) {
					craft.captainAbandoned = false;
				}

				if (player.getName() == craft.driverName) {
					craft.haveControl = true;

				}
			} else if (craft.type.listenMovement == true) {
				Location fromLoc = event.getFrom();
				Location toLoc = event.getTo();
				int dx = toLoc.getBlockX() - fromLoc.getBlockX();
				int dy = toLoc.getBlockY() - fromLoc.getBlockY();
				int dz = toLoc.getBlockZ() - fromLoc.getBlockZ();

				CraftMover cm = new CraftMover(craft, plugin);
				cm.calculateMove(dx, dy, dz);

				//// periscope
			} else if ((playerScope != null) && ((event.getFrom().getBlockX() != event.getTo().getBlockX())
					|| (event.getFrom().getBlockZ() != event.getTo().getBlockZ()))) {


				CraftMover cmer = new CraftMover(craft, plugin);
				cmer.structureUpdate(null, false);
				craft.lastPeriscopeYaw = player.getLocation().getYaw();
				Location newLoc = new Location(craft.world, playerScope.signLoc.getBlockX() + .5,
						playerScope.signLoc.getBlockY() - .5, playerScope.signLoc.getBlockZ() + .5);
				newLoc.setYaw(player.getLocation().getYaw());
				player.teleport(newLoc);
				playerScope.user = null;
				///// helm
			} else if ((craft.driverName == player.getName())
					&& ((event.getFrom().getBlockX() != event.getTo().getBlockX())
							|| (event.getFrom().getBlockZ() != event.getTo().getBlockZ()))) {
				craft.releaseHelm();
				craft.haveControl = false;
				if (player.getInventory().contains(Material.GOLD_SWORD)) {
					player.getInventory().remove(Material.GOLD_SWORD);
				}
				player.sendMessage(ChatColor.YELLOW + "You release the helm.");
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		Player player = event.getPlayer();

		Craft playerCraft = Craft.getPlayerCraft(player);

		if (action == Action.RIGHT_CLICK_BLOCK) {

			if (event.hasBlock()) {
				Block block = event.getClickedBlock();
				NavyCraft.instance.DebugMessage("The action has a block " + block + " associated with it.", 4);

				if ((block.getTypeId() == 63) || (block.getTypeId() == 68)) {
					MoveCraft_BlockListener.ClickedASign(player, block, false);
					return;
				}

				if ((block.getTypeId() == 69) && (block.getRelative(BlockFace.DOWN, 1).getTypeId() == 68)) {
					Craft testCraft = Craft.getCraft(block.getX(), block.getY(), block.getZ());
					if (testCraft != null) {
						Sign sign = (Sign) block.getRelative(BlockFace.DOWN, 1).getState();

						if ((sign.getLine(0) == null) || sign.getLine(0).trim().equals("")) {
							return;
						}

						String craftTypeName = sign.getLine(0).trim().toLowerCase();

						// remove colors
						craftTypeName = craftTypeName.replaceAll(ChatColor.BLUE.toString(), "");

						// remove brackets
						if (craftTypeName.startsWith("[")) {
							craftTypeName = craftTypeName.substring(1, craftTypeName.length() - 1);
						}

						if (craftTypeName.equalsIgnoreCase("periscope")) {


							CraftMover cmer = new CraftMover(testCraft, plugin);
							cmer.structureUpdate(null, false);
							for (Periscope p : testCraft.periscopes) {
								if ((p.signLoc.getBlockX() == block.getRelative(BlockFace.DOWN, 1).getLocation()
										.getBlockX())
										&& (p.signLoc.getBlockY() == block.getRelative(BlockFace.DOWN, 1).getLocation()
												.getBlockY())
										&& (p.signLoc.getBlockZ() == block.getRelative(BlockFace.DOWN, 1).getLocation()
												.getBlockZ())) {
									if (p.raised && !p.destroyed && (p.scopeLoc != null)) {
										if ((p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 1).getTypeId() == 113)
												&& (p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 2)
														.getTypeId() == 113)
												&& (p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 3)
														.getTypeId() == 113)
												&& (p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 4)
														.getTypeId() == 113)) {
											p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 1).setTypeId(0);
											p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 2).setTypeId(0);
											p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 3).setTypeId(0);
											p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 4).setTypeId(0);
											p.raised = false;
											CraftMover cm = new CraftMover(testCraft, plugin);
											cm.structureUpdate(null, false);

											player.sendMessage("Down Periscope!");
										} else {
											p.destroyed = true;
										}
									} else if (!p.destroyed && (p.scopeLoc != null)) {
										p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 1).setTypeId(113);
										testCraft.addBlock(p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 1), true);
										p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 2).setTypeId(113);
										testCraft.addBlock(p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 2), true);
										p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 3).setTypeId(113);
										testCraft.addBlock(p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 3), true);
										p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 4).setTypeId(113);
										testCraft.addBlock(p.scopeLoc.getBlock().getRelative(BlockFace.DOWN, 4), true);
										p.raised = true;
										CraftMover cm = new CraftMover(testCraft, plugin);
										cm.structureUpdate(null, false);
										player.sendMessage("Up Periscope!");
									} else {
										player.sendMessage("Periscope Destroyed!");
									}
								}
							}
						}
					}
					return;
				}

				if ((block.getTypeId() == 54) || (block.getTypeId() == 23) || (block.getTypeId() == 61)) {
					// Need to handle workbench as well...

					return;
				}

				if ((NavyCraft.instance.ConfigSetting("RequireRemote") == "true") && (playerCraft != null)) {
					playerCraft.addBlock(block, false);
				}

			}

			if ((playerCraft != null) && (playerCraft.driverName == player.getName())) {
				if ((NavyCraft.instance.ConfigSetting("RequireRemote") == "true")
						&& (event.getItem().getTypeId() != playerCraft.type.remoteControllerItem)) {
					return;
				}
				if (event.getHand() == EquipmentSlot.HAND)
					playerUsedAnItem(player, playerCraft);
			} else {
				Vector pVel = player.getVelocity();
				if ((player.getLocation().getPitch() < 90) || (player.getLocation().getPitch() > 180)) {
					pVel.setX(pVel.getX() + 1);
				} else {
					pVel.setY(pVel.getY() + 1);
				}
			}
		}

		if ((action == Action.LEFT_CLICK_BLOCK) && event.hasBlock()) {
			Block block = event.getClickedBlock();
			if ((block.getTypeId() == 63) || (block.getTypeId() == 68)) {
				MoveCraft_BlockListener.ClickedASign(player, block, true);
				return;
			}
		}

		// fire airplane gun
		if ((action == Action.LEFT_CLICK_AIR) && (player.getItemInHand().getType() == Material.GOLD_SWORD)) {
			Craft testCraft = Craft.getPlayerCraft(event.getPlayer());
			if ((testCraft != null) && (testCraft.driverName == player.getName()) && testCraft.type.canFly
					&& !testCraft.sinking && !testCraft.helmDestroyed) {
				Egg newEgg = player.launchProjectile(Egg.class);

	

				newEgg.setVelocity(newEgg.getVelocity().multiply(2.0f));
				NavyCraft.explosiveEggsList.add(newEgg);
				event.getPlayer().getWorld().playEffect(player.getLocation(), Effect.SMOKE, 0);
				event.getPlayer().getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 5.0f,
						1.70f);

			}

		}

		// AA Gunner...
		if ((action == Action.LEFT_CLICK_AIR) && NavyCraft.aaGunnersList.contains(player)
				&& (player.getItemInHand().getType() == Material.BLAZE_ROD)) {
			Egg newEgg = player.launchProjectile(Egg.class);
			newEgg.setVelocity(newEgg.getVelocity().multiply(1.5f));
			NavyCraft.explosiveEggsList.add(newEgg);
			event.getPlayer().getWorld().playEffect(player.getLocation(), Effect.SMOKE, 0);

			event.getPlayer().getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 5.0f,
					1.70f);

			//// else check for movement clicking
		} else if ((action == Action.RIGHT_CLICK_AIR) && (playerCraft != null)
				&& (playerCraft.driverName == player.getName()) && (playerCraft.type.listenItem == true)) {
			if ((NavyCraft.instance.ConfigSetting("RequireRemote") == "true")
					&& (event.getItem().getTypeId() != playerCraft.type.remoteControllerItem)) {
				return;
			}
			playerUsedAnItem(player, playerCraft);
		}

		
		AimCannonPlayerListener.onPlayerInteract(event);

	}

	@SuppressWarnings("deprecation")
	public void playerUsedAnItem(Player player, Craft craft) {

		// minimum time between 2 swings
		if ((System.currentTimeMillis() - craft.lastMove) < (1.0 * 1000)) {
			return;
		}

		if (craft.blockCount <= 0) {
			craft.releaseCraft();
			return;
		}

		ItemStack pItem = player.getItemInHand();
		int item = pItem.getTypeId();



		// the craft won't budge if you have any tool in the hand
		if (!craft.haveControl) {
			if (((item == craft.type.remoteControllerItem)
					|| (item == Integer.parseInt(NavyCraft.instance.ConfigSetting("UniversalRemoteId"))))
					&& !craft.isOnCraft(player, true) && PermissionInterface.CheckPerm(player, "remote")) {
				if (craft.haveControl) {
					player.sendMessage(ChatColor.YELLOW + "You switch off the remote controller");
				} else {
					MoveCraft_Timer timer = MoveCraft_Timer.playerTimers.get(player);
					if (timer != null) {
						timer.Destroy();
					}
					player.sendMessage(ChatColor.YELLOW + "You switch on the remote controller");
				}
				craft.haveControl = !craft.haveControl;
			} else {
				return;
				/////////////// *******************//////////////////////////////////////////////////
			}
		} else if (item == Material.GOLD_SWORD.getId()) {
			// cruise movmeent
			if (craft.type.doesCruise && craft.autoTurn) {
				float rotation = ((float) Math.PI * player.getLocation().getYaw()) / 180f;

				// Not really sure what the N stands for...
				float nx = -(float) Math.sin(rotation);
				float nz = (float) Math.cos(rotation);

				int dx = (Math.abs(nx) >= 0.5 ? 1 : 0) * (int) Math.signum(nx);
				int dz = (Math.abs(nz) > 0.5 ? 1 : 0) * (int) Math.signum(nz);
				int dy = 0;

				///// Planes
				if (craft.type.canFly || craft.type.canDig) {

					float p = player.getLocation().getPitch();

					dy = -(Math.abs(p) >= 45 ? 1 : 0) * (int) Math.signum(p);

					if (dy == 1) {
						if (craft.vertPlanes == 0) {
							craft.vertPlanes = 1;
							player.sendMessage("Up Elevator");
						} else if (craft.vertPlanes == -1) {
							craft.vertPlanes = 0;
							player.sendMessage("Neutral Elevator");
						} else {
							player.sendMessage("Elevator already up");
						}
						return;
					} else if (dy == -1) {
						if (craft.vertPlanes == 0) {
							craft.vertPlanes = -1;
							player.sendMessage("Down Elevator");
						} else if (craft.vertPlanes == 1) {
							craft.vertPlanes = 0;
							player.sendMessage("Neutral Elevator");
						} else {
							player.sendMessage("Elevator already down");
						}
						return;
					}
				}

				/// subs
				if (craft.type.canDive) {
					float p = player.getLocation().getPitch();

					dy = -(Math.abs(p) >= 45 ? 1 : 0) * (int) Math.signum(p);

					if (dy != 0) {
						if (!craft.submergedMode) {
							player.sendMessage("Set engines to dive first.");
							return;
						}
					}

					if (dy == 1) {
						if (craft.vertPlanes == 0) {
							craft.vertPlanes = 1;
							player.sendMessage("Diving Planes Up Bubble");
						} else if (craft.vertPlanes == -1) {
							craft.vertPlanes = 0;
							player.sendMessage("Diving Planes Neutral");
						} else {
							player.sendMessage("Diving Planes already up");
						}
						return;
					} else if (dy == -1) {
						if (craft.vertPlanes == 0) {
							craft.vertPlanes = -1;
							player.sendMessage("Diving Planes Down Bubble");
						} else if (craft.vertPlanes == 1) {
							craft.vertPlanes = 0;
							player.sendMessage("Diving Planes Neutral");
						} else {
							player.sendMessage("Diving Planes already down");
						}
						return;
					}
				}

				////////////// turning

				//// north
				if ((craft.rotation % 360) == 0) {
					if (nx > 0.866) {
						if (craft.rudder == 0) {
							player.sendMessage("Turning to heading 090");
						}
						craft.rudderChange(player, 1, true);
						return;
					} else if (nx < -0.866) {
						if (craft.rudder == 0) {
							player.sendMessage("Turning to heading 270");
						}
						craft.rudderChange(player, -1, true);
						return;
					} else if (dx == 1) {
						craft.rudderChange(player, 1, false);
						return;
					} else if (dx == -1) {
						craft.rudderChange(player, -1, false);
						return;
					} else if (dz == -1) {
						if (player.isSneaking()) {
							craft.gearChange(player, true);
						} else {
							craft.speedChange(player, true);
						}
						return;
					} else if (dz == 1) {
						if (player.isSneaking()) {
							craft.gearChange(player, false);

						} else {
							craft.speedChange(player, false);
						}
						return;
					}

					////// south
				} else if (craft.rotation == 180) {

					if (nx > 0.866) {
						if (craft.rudder == 0) {
							player.sendMessage("Turning to heading 090");
						}
						craft.rudderChange(player, -1, true);
						return;
					} else if (nx < -0.866) {
						if (craft.rudder == 0) {
							player.sendMessage("Turning to heading 270");
						}
						craft.rudderChange(player, 1, true);
						return;
					} else if (dx == 1) {
						craft.rudderChange(player, -1, false);
						return;
					} else if (dx == -1) {
						craft.rudderChange(player, 1, false);
						return;
					} else if (dz == -1) {
						if (player.isSneaking()) {
							craft.gearChange(player, false);
						} else {
							craft.speedChange(player, false);
						}
						return;
					} else if (dz == 1) {
						if (player.isSneaking()) {
							craft.gearChange(player, true);

						} else {
							craft.speedChange(player, true);
						}
						return;
					}

					////// east
				} else if (craft.rotation == 90) {

					if (nz > 0.866) {
						if (craft.rudder == 0) {
							player.sendMessage("Turning to heading 180");
						}
						craft.rudderChange(player, 1, true);
						return;
					} else if (nz < -0.866) {
						if (craft.rudder == 0) {
							player.sendMessage("Turning to heading 000");
						}
						craft.rudderChange(player, -1, true);
						return;
					} else if (dz == 1) {
						craft.rudderChange(player, 1, false);
						return;
					} else if (dz == -1) {
						craft.rudderChange(player, -1, false);
						return;
					} else if (dx == -1) {
						if (player.isSneaking()) {
							craft.gearChange(player, false);
						} else {
							craft.speedChange(player, false);
						}
						return;
					} else if (dx == 1) {
						if (player.isSneaking()) {
							craft.gearChange(player, true);

						} else {
							craft.speedChange(player, true);
						}
						return;
					}
					////////////// west
				} else if (craft.rotation == 270) {
					if (nz > 0.866) {
						if (craft.rudder == 0) {
							player.sendMessage("Turning to heading 180");
						}
						craft.rudderChange(player, -1, true);
						return;
					} else if (nz < -0.866) {
						if (craft.rudder == 0) {
							player.sendMessage("Turning to heading 000");
						}
						craft.rudderChange(player, 1, true);
						return;
					} else if (dz == 1) {
						craft.rudderChange(player, -1, false);
						return;
					} else if (dz == -1) {
						craft.rudderChange(player, 1, false);
						return;
					} else if (dx == -1) {
						if (player.isSneaking()) {
							craft.gearChange(player, true);
						} else {
							craft.speedChange(player, true);
						}
						return;
					} else if (dx == 1) {
						if (player.isSneaking()) {
							craft.gearChange(player, false);

						} else {
							craft.speedChange(player, false);
						}
						return;
					}
				}

			} else // old style movement
			{
				float rotation = ((float) Math.PI * player.getLocation().getYaw()) / 180f;

				// Not really sure what the N stands for...
				float nx = -(float) Math.sin(rotation);
				float nz = (float) Math.cos(rotation);

				int dx = (Math.abs(nx) >= 0.5 ? 1 : 0) * (int) Math.signum(nx);
				int dz = (Math.abs(nz) > 0.5 ? 1 : 0) * (int) Math.signum(nz);
				int dy = 0;


				// we are on a flying object, handle height change
				if (craft.type.canFly || craft.type.canDive || craft.type.canDig) {

					float p = player.getLocation().getPitch();

					dy = -(Math.abs(p) >= 25 ? 1 : 0) * (int) Math.signum(p);

					// move straight up or straight down
					if (Math.abs(player.getLocation().getPitch()) >= 75) {
						dx = 0;
						dz = 0;
					}
				}



				if (craft.autoTurn) {
					if ((craft.rotation % 360) == 0) {
						if (nx > 0.866) {
							craft.turn(90);
							return;
						} else if (nx < -0.866) {
							craft.turn(270);
							return;
						}
					} else if (craft.rotation == 180) {
						if (nx > 0.866) {
							craft.turn(270);
							return;
						} else if (nx < -0.866) {
							craft.turn(90);
							return;
						}
					} else if (craft.rotation == 90) {
						if (nz > 0.866) {
							craft.turn(90);
							return;
						} else if (nz < -0.866) {
							craft.turn(270);
							return;
						}
					} else if (craft.rotation == 270) {
						if (nz > 0.866) {
							craft.turn(270);
							return;
						} else if (nz < -0.866) {
							craft.turn(90);
							return;
						}
					}
				}
				CraftMover cm = new CraftMover(craft, plugin);
				cm.calculateMove(dx, dy, dz);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
			Player player = event.getPlayer();
			Craft craft = Craft.getPlayerCraft(player);

			if ((player.getItemInHand().getType() == Material.FLINT_AND_STEEL)
					&& NavyCraft.cleanupPlayers.contains(player.getName())
					&& PermissionInterface.CheckEnabledWorld(player.getLocation() )) {
				Set<Material> transp = new HashSet<>();
				transp.add(Material.AIR);
				transp.add(Material.STATIONARY_WATER);
				transp.add(Material.WATER);
				Block block = player.getTargetBlock(transp, 300);
				if (block != null) {
					Craft c = Craft.getCraft(block.getX(), block.getY(), block.getZ());
					if (c != null) {
						if (!((c.captainName != null) && (plugin.getServer().getPlayer(c.captainName) != null)
								&& plugin.getServer().getPlayer(c.captainName).isOnline())) {
							if (craft != null)// && playerCraft.type ==
												// craftType) {
							{
								craft.leaveCrew(player);
							}

							c.buildCrew(player, false);

							// CraftMover cm = new CraftMover(c, plugin);
							// cm.structureUpdate(null);
							System.out.println("Vehicle destroyed by:" + player.getName() + " Name:" + c.name + " X:"
									+ c.getLocation().getBlockX() + " Y:" + c.getLocation().getBlockY() + " Z:"
									+ c.getLocation().getBlockZ());
							c.doDestroy = true;
							player.sendMessage("Vehicle destroyed.");
						} else {
							player.sendMessage("Vehicle's captain is online.");
						}
					} else {
						block.getRelative(BlockFace.UP, 1).setTypeId(63);
						Sign sign = (Sign) block.getRelative(BlockFace.UP, 1).getState();
						sign.setLine(0, "Ship");
						sign.update();

						Craft theCraft = NavyCraft.instance.createCraft(player, CraftType.getCraftType("ship"),
								sign.getX(), sign.getY(), sign.getZ(), "ship", 0, block.getRelative(BlockFace.UP, 1),
								false);
						if (theCraft != null) {
							if (!NavyCraft.checkNoDriveRegion(theCraft.getLocation())) {
								System.out.println("Vehicle destroyed by:" + player.getName() + " Name:" + theCraft.name
										+ " X:" + theCraft.getLocation().getBlockX() + " Y:"
										+ theCraft.getLocation().getBlockY() + " Z:"
										+ theCraft.getLocation().getBlockZ());
								theCraft.doDestroy = true;
								player.sendMessage("Vehicle destroyed.");
							} else {
								player.sendMessage(ChatColor.RED + player.getName()
										+ ", why are you trying to destroy a dock vehicle??");
								System.out
										.println(player.getName() + ", why are you trying to destroy a dock vehicle??");
							}
						} else {
							sign.setLine(0, "Aircraft");
							sign.update();
							theCraft = NavyCraft.instance.createCraft(player, CraftType.getCraftType("aircraft"),
									sign.getX(), sign.getY(), sign.getZ(), "aircraft", 0,
									block.getRelative(BlockFace.UP, 1), false);

							if (theCraft != null) {
								if (!NavyCraft.checkNoDriveRegion(theCraft.getLocation())) {
									System.out.println("Vehicle destroyed by:" + player.getName() + " Name:"
											+ theCraft.name + " X:" + theCraft.getLocation().getBlockX() + " Y:"
											+ theCraft.getLocation().getBlockY() + " Z:"
											+ theCraft.getLocation().getBlockZ());
									theCraft.doDestroy = true;
									player.sendMessage("Vehicle destroyed.");
								} else {
									player.sendMessage(ChatColor.RED + player.getName()
											+ ", why are you trying to destroy a dock vehicle??");
									System.out.println(
											player.getName() + ", why are you trying to destroy a dock vehicle??");
								}
							} else {
								player.sendMessage("No vehicle could be detected.");
								block.getRelative(BlockFace.UP, 1).setTypeId(0);
							}
						}
					}
				} else {
					player.sendMessage("No block detected");
				}
				return;
			}
			if ((player.getItemInHand().getType() == Material.SHEARS)
					&& NavyCraft.cleanupPlayers.contains(player.getName())
					&& PermissionInterface.CheckEnabledWorld(player.getLocation())
					&& !NavyCraft.checkSafeDockRegion(player.getLocation())) {
				HashSet<Byte> hs = new HashSet<>();
				hs.add((byte) 0x0);
				Block block = player.getTargetBlock(hs, 200);
				if (block != null) {
					System.out.println("Shears used:" + player.getName() + " X:" + block.getX() + " Y:" + block.getY()
							+ " Z:" + block.getZ());
					player.sendMessage("Shears used!");
					for (int x = block.getX() - 7; x <= (block.getX() + 7); x++) {
						for (int z = block.getZ() - 7; z <= (block.getZ() + 7); z++) {
							for (int y = block.getY() - 7; y <= (block.getY() + 7); y++) {
								Block theBlock = block.getWorld().getBlockAt(x, y, z);
								if (theBlock.getType() != Material.BEDROCK) {

									if (theBlock.getY() < 63) {
										theBlock.setType(Material.WATER);
									} else {
										theBlock.setType(Material.AIR);
									}
								}
								// TNT tnt = (TNT) theBlock.getState();
							}
						}
					}
				} else {
					player.sendMessage("No block detected");
				}
				return;
			}
			if ((player.getItemInHand().getType() == Material.GOLD_SPADE)
					&& NavyCraft.cleanupPlayers.contains(player.getName())
					&& PermissionInterface.CheckEnabledWorld(player.getLocation())
					&& !NavyCraft.checkSafeDockRegion(player.getLocation())) {
				HashSet<Byte> hs = new HashSet<>();
				hs.add((byte) 0x0);
				hs.add((byte) 0x8);
				hs.add((byte) 0x9);
				Block block = player.getTargetBlock(hs, 200);

				if (block != null) {
					System.out.println("Golden Shovel used:" + player.getName() + " X:" + block.getX() + " Y:"
							+ block.getY() + " Z:" + block.getZ());
					player.sendMessage("Golden Shovel used!");
					for (int x = block.getX() - 7; x <= (block.getX() + 7); x++) {
						for (int z = block.getZ() - 7; z <= (block.getZ() + 7); z++) {
							for (int y = block.getY() - 7; y <= (block.getY() + 7); y++) {
								Block theBlock = block.getWorld().getBlockAt(x, y, z);
								for (Short s : CraftType.getCraftType("ship").structureBlocks) {
									if ((s == theBlock.getTypeId()) && (theBlock.getType() != Material.BEDROCK)) {
										if (theBlock.getY() < 63) {
											theBlock.setType(Material.WATER);
										} else {
											theBlock.setType(Material.AIR);
										}
									}
								}

							}
						}
					}
				} else {
					player.sendMessage("No block detected");
				}
				return;
			}

			if ((craft != null) && (craft.driverName == player.getName()) && (craft.type.listenAnimation == true)) {
				playerUsedAnItem(player, craft);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {


	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		// public void onPlayerCommand(PlayerChatEvent event) {
		Player player = event.getPlayer();
		String[] split = event.getMessage().split(" ");
		split[0] = split[0].substring(1);

		// debug commands
		if (NavyCraft.instance.DebugMode == true) {
			if (split[0].equalsIgnoreCase("isDataBlock")) {
				player.sendMessage(Boolean.toString(BlocksInfo.isDataBlock(Integer.parseInt(split[1]))));
			} else if (split[0].equalsIgnoreCase("isComplexBlock")) {
				player.sendMessage(Boolean.toString(BlocksInfo.isComplexBlock(Integer.parseInt(split[1]))));

			} else if (split[0].equalsIgnoreCase("finddatablocks")) {
				Craft craft = Craft.getPlayerCraft(player);
				for (DataBlock dataBlock : craft.dataBlocks) {
					Block theBlock = player.getWorld().getBlockAt(new Location(player.getWorld(),
							craft.minX + dataBlock.x, craft.minY + dataBlock.y, craft.minZ + dataBlock.z));
					theBlock.setType(Material.GOLD_BLOCK);
				}
			} else if (split[0].equalsIgnoreCase("findcomplexblocks")) {
				Craft craft = Craft.getPlayerCraft(player);
				for (DataBlock dataBlock : craft.complexBlocks) {
					Block theBlock = player.getWorld().getBlockAt(new Location(player.getWorld(),
							craft.minX + dataBlock.x, craft.minY + dataBlock.y, craft.minZ + dataBlock.z));
					theBlock.setType(Material.GOLD_BLOCK);
				}
			} else if (split[0].equalsIgnoreCase("diamondit")) {
				Craft craft = Craft.getPlayerCraft(player);

				for (int x = 0; x < craft.sizeX; x++) {
					for (int y = 0; y < craft.sizeY; y++) {
						for (int z = 0; z < craft.sizeZ; z++) {
							if (craft.matrix[x][y][z] != -1) {
								Block theBlock = player.getWorld().getBlockAt(new Location(player.getWorld(),
										craft.minX + x, craft.minY + y, craft.minZ + z));
								theBlock.setType(Material.DIAMOND_BLOCK);
							}
						}
					}
				}
			} else if (split[0].equalsIgnoreCase("craftvars")) {
				Craft craft = Craft.getPlayerCraft(player);

				NavyCraft.instance.DebugMessage("Craft type: " + craft.type, 4);
				NavyCraft.instance.DebugMessage("Craft name: " + craft.name, 4);

				// may need to make multidimensional
				NavyCraft.instance.DebugMessage("Craft matrix size: " + craft.matrix.length, 4);
				NavyCraft.instance.DebugMessage("Craft block count: " + craft.blockCount, 4);
				NavyCraft.instance.DebugMessage("Craft data block count: " + craft.dataBlocks.size(), 4);
				NavyCraft.instance.DebugMessage("Craft complex block count: " + craft.complexBlocks.size(), 4);

				NavyCraft.instance.DebugMessage("Craft speed: " + craft.speed, 4);
				NavyCraft.instance
						.DebugMessage("Craft size: " + craft.sizeX + " * " + craft.sizeY + " * " + craft.sizeZ, 4);

				NavyCraft.instance.DebugMessage("Craft last move: " + craft.lastMove, 4);
				// world?
				NavyCraft.instance.DebugMessage("Craft center: " + craft.centerX + ", " + craft.centerZ, 4);

				NavyCraft.instance.DebugMessage("Craft water level: " + craft.waterLevel, 4);
				NavyCraft.instance.DebugMessage("Craft new water level: " + craft.newWaterLevel, 4);
				NavyCraft.instance.DebugMessage("Craft water type: " + craft.waterType, 4);

				NavyCraft.instance.DebugMessage("Craft bounds: " + craft.minX + "->" + craft.maxX + ", " + craft.minY
						+ "->" + craft.maxY + ", " + craft.minZ + "->" + craft.maxZ, 4);

			} else if (split[0].equalsIgnoreCase("getRotation")) {
				Set<Material> meh = new HashSet<>();
				Block examineBlock = player.getTargetBlock(meh, 100);

				int blockDirection = BlocksInfo.getCardinalDirectionFromData(examineBlock.getTypeId(),
						examineBlock.getData());
				player.sendMessage("Block data is " + examineBlock.getData() + " direction is " + blockDirection);
			}
		}

		if (split[0].equalsIgnoreCase("movecraft") || split[0].equalsIgnoreCase("navycraft")
				|| split[0].equalsIgnoreCase("nc")) {
			/*if (!PermissionInterface.CheckPermission(player, "navycraft." + event.getMessage().substring(1))) {
				return;
			}*/

			if (split.length >= 2) {
				if (split[1].equalsIgnoreCase("types")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.basic") )
						return;
					for (CraftType craftType : CraftType.craftTypes) {
						if (craftType.canUse(player)) {
							player.sendMessage(ChatColor.GREEN + craftType.name + ChatColor.YELLOW + craftType.minBlocks
									+ "-" + craftType.maxBlocks + " blocks" + " doesCruise : " + craftType.doesCruise);
						}
					}
				} else if (split[1].equalsIgnoreCase("list")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					if (Craft.craftList.isEmpty()) {
						player.sendMessage(ChatColor.YELLOW + "no player controlled craft");
						// return true;
					}

					for (Craft craft : Craft.craftList) {

						player.sendMessage(ChatColor.YELLOW + "" + craft.craftID + "-" + craft.name + " commanded by " + craft.captainName + " : "
								+ craft.blockCount + " blocks");
					}
				} else if (split[1].equalsIgnoreCase("reload")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					NavyCraft.instance.loadProperties();
					player.sendMessage(ChatColor.YELLOW + "MoveCraft configuration reloaded");
					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("debug")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					NavyCraft.instance.ToggleDebug();
					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("loglevel")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					try {
						Integer.parseInt(split[2]);
						NavyCraft.instance.configFile.ConfigSettings.put("LogLevel", split[2]);
					} catch (Exception ex) {
						player.sendMessage("Invalid loglevel.");
					}
					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("config")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					NavyCraft.instance.configFile.ListSettings(player);
					return;
				} else if (split[1].equalsIgnoreCase("spawntimer")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					int timerMin = -1;
					if (split.length == 3) {
						try {
							timerMin = Integer.parseInt(split[2]);
						} catch (NumberFormatException e) {
							player.sendMessage("Invalid timer value");
							e.printStackTrace();
						}
					}
					if ((timerMin >= 1) || (timerMin <= 60)) {

						NavyCraft.spawnTime = timerMin;
						player.sendMessage("Spawn time set to " + timerMin + " minutes.");
					} else {
						player.sendMessage("Invalid timer value..between 1 to 60 minutes");
					}

					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("cleanup")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					if (NavyCraft.cleanupPlayers.contains(player.getName())) {
						NavyCraft.cleanupPlayers.remove(player.getName());
						player.sendMessage("Exiting cleanup mode.");
					} else {


						NavyCraft.cleanupPlayers.add(player.getName());
						player.sendMessage("Entering cleanup mode.");
					}
					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("listShips") || split[1].equalsIgnoreCase("ls")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					for (Craft c : Craft.craftList) {
						if (c.isAutoCraft) {
							if (c.customName != null) {
								player.sendMessage(c.craftID + "-" + c.customName + " Route=" + c.routeID + " Stage="
										+ c.routeStage);
							} else {
								player.sendMessage(
										c.craftID + "-" + c.name + " Route=" + c.routeID + " Stage=" + c.routeStage);
							}
						} else {
							if (c.customName != null) {
								player.sendMessage(c.craftID + "-" + c.customName);
							} else {
								player.sendMessage(c.craftID + "-" + c.name);
							}
						}
					}
					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("weapons")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					for (Weapon w : AimCannon.weapons) {
							player.sendMessage("weapon -" + w.weaponType);
					}
					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("cannons")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					for (OneCannon c : AimCannon.cannons) {
							player.sendMessage("cannon -" + c.cannonType);
					}
					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("destroyShips")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					for (Craft c : Craft.craftList) {
						c.doDestroy = true;
					}
					player.sendMessage("All vehicles destroyed");
					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("removeships")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					for (Craft c : Craft.craftList) {
						c.doRemove = true;
					}
					player.sendMessage("All vehicles removed");
					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("destroyauto")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					ArrayList<Craft> craftCheckList = new ArrayList<>();
					for (Craft c : Craft.craftList) {
						craftCheckList.add(c);
					}
					int count = 0;
					for (Craft c : craftCheckList) {
						if (c.isAutoCraft) {
							c.doDestroy = true;
							count++;
						}
					}
					craftCheckList.clear();

					player.sendMessage("All auto vehicles destroyed-" + count);
					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("destroystuck")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					ArrayList<Craft> craftCheckList = new ArrayList<>();
					for (Craft c : Craft.craftList) {
						craftCheckList.add(c);
					}
					int count = 0;
					for (Craft c : craftCheckList) {
						if (c.isAutoCraft && ((c.speed == 0) || (c.speed == 1))) {
							c.doDestroy = true;
							count++;
						}
					}
					craftCheckList.clear();

					player.sendMessage("All stopped auto vehicles destroyed-" + count);
					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("tpShip") || split[1].equalsIgnoreCase("tp")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					int shipNum = -1;
					if (split.length == 3) {
						try {
							shipNum = Integer.parseInt(split[2]);
						} catch (NumberFormatException e) {
							player.sendMessage("Invalid id number");
							e.printStackTrace();
						}
					}
					if (shipNum != -1) {
						for (Craft c : Craft.craftList) {
							if (shipNum == c.craftID) {
								player.teleport(
										new Location(c.world, c.getLocation().getX(), c.maxY, c.getLocation().getZ()));
								event.setCancelled(true);
								return;
							}
						}
						player.sendMessage("ID Number not found");
					} else {
						player.sendMessage("Invalid id number");
					}

					event.setCancelled(true);
					return;
				} else if (split[1].equalsIgnoreCase("loadShips")) {
					if( !PermissionInterface.CheckPerm(player, "navycraft.admin") )
						return;
					for (int x = -1800; x <= 2000; x++) {
						for (int y = 30; y <= 128; y++) {
							for (int z = -1100; z <= 1700; z++) {
								if (player.getWorld().getBlockAt(x, y, z).getTypeId() == 68) {
									Block shipSignBlock = player.getWorld().getBlockAt(x, y, z);
									Sign shipSign = (Sign) shipSignBlock.getState();
									String signLine0 = shipSign.getLine(0).trim().toLowerCase()
											.replaceAll(ChatColor.BLUE.toString(), "");
									CraftType craftType = CraftType.getCraftType(signLine0);
									if (craftType != null) {
										String name = shipSign.getLine(1);// .replaceAll("§.",
																			// "");

										if (name.trim().equals("")) {
											name = null;
										}

										int shipx = shipSignBlock.getX();
										int shipy = shipSignBlock.getY();
										int shipz = shipSignBlock.getZ();

										int direction = shipSignBlock.getData();

										// get the block the sign is attached to
										shipx = shipx + (direction == 4 ? 1 : (direction == 5 ? -1 : 0));
										shipz = shipz + (direction == 2 ? 1 : (direction == 3 ? -1 : 0));

										float dr = 0;

										switch (shipSignBlock.getData()) {
										case (byte) 0x2:// n
											dr = 180;
											break;
										case (byte) 0x3:// s
											dr = 0;
											break;
										case (byte) 0x4:// w
											dr = 90;
											break;
										case (byte) 0x5:// e
											dr = 270;
											break;
										}
										player.sendMessage("x=" + x + " y=" + y + " z=" + z);
										Craft theCraft = NavyCraft.instance.createCraft(player, craftType, shipx, shipy,
												shipz, name, dr, shipSignBlock, true);
										if (theCraft != null) {
											if (name != null) {
												player.sendMessage(name + " activated!");
											} else {
												player.sendMessage(signLine0 + " activated!");
											}
											CraftMover cm = new CraftMover(theCraft, plugin);
											cm.structureUpdate(null, false);
										} else {
											player.getWorld().getBlockAt(x, y, z).setTypeId(0);
										}
									}
								}
							}
						}
					}
					player.sendMessage("All vehicles loaded in ocean area");
					event.setCancelled(true);
					return;
				}
			} else {
				if( PermissionInterface.CheckPerm(player, "navycraft.basic") ){
					player.sendMessage(ChatColor.WHITE + "NavyCraft v" + NavyCraft.version + " commands :");
					player.sendMessage(ChatColor.YELLOW + "/navycraft types " + " : " + ChatColor.WHITE
							+ "list the types of craft available");
					player.sendMessage(ChatColor.YELLOW + "/[craft type] " + " : " + ChatColor.WHITE
							+ "commands specific to the craft type try /ship help");
					player.sendMessage(ChatColor.YELLOW + "/volume ## " + " : " + ChatColor.WHITE
							+ "set engine piston volume from 0-100");
				}
				
				if( PermissionInterface.CheckQuietPerm(player, "navycraft.admin") )
				{
					player.sendMessage(ChatColor.YELLOW + "/navycraft list : " + ChatColor.WHITE
						+ "list all craft");
					player.sendMessage(ChatColor.YELLOW + "/navycraft reload : " + ChatColor.WHITE + "reload config files");
					player.sendMessage(ChatColor.YELLOW + "/navycraft config : " + ChatColor.WHITE + "display config settings");
					player.sendMessage(ChatColor.YELLOW + "/navycraft cleanup : " + ChatColor.WHITE + "enables cleanup tools, use lighter, gold spade, and shears");
					player.sendMessage(ChatColor.YELLOW + "/navycraft destroyships : " + ChatColor.WHITE + "destroys all active ships");
					player.sendMessage(ChatColor.YELLOW + "/navycraft removeships : " + ChatColor.WHITE + "deactivates all active ships");
					player.sendMessage(ChatColor.YELLOW + "/navycraft destroyauto : " + ChatColor.WHITE + "destroys all auto ships");
					player.sendMessage(ChatColor.YELLOW + "/navycraft destroystuck : " + ChatColor.WHITE + "destroys stuck auto ships");
					player.sendMessage(ChatColor.YELLOW + "/navycraft tpship id# : " + ChatColor.WHITE + "teleport to ship ID #");
				}

			}
			event.setCancelled(true);
		} else if (split[0].equalsIgnoreCase("park")) {
			// MoveCraft.instance.releaseCraft(player,
			// Craft.getPlayerCraft(player));
			Craft c = Craft.getPlayerCraft(player);
			if ((c != null) && (c.captainName == player.getName())) {
				c.releaseHelm();
				c.releaseCraft();
			}
			event.setCancelled(true);
		} else if (split[0].equalsIgnoreCase("remote")) {
			player.sendMessage("0");
			Craft craft = Craft.getPlayerCraft(player);
			if ((craft != null) && (craft.driverName == player.getName())) {
				split[0] = craft.type.name;
				split[1] = "remote";

				if (!PermissionInterface.CheckPerm(player, "navycraft.remote")) {
					event.setCancelled(true);
					return;
				}
				player.sendMessage("1");
				if (processCommand(craft.type, player, split) == true) {
					event.setCancelled(true);
				}
			} else {
				player.sendMessage("You have no craft to remote :( Hurry and get one before they're sold out!");
			}
		} else {

			String craftName = split[0];

			CraftType craftType = CraftType.getCraftType(craftName);
			//// CREW chat
			if (craftName.equalsIgnoreCase("crew")) {
				Craft craft = Craft.getPlayerCraft(player);
				if (craft == null) {
					player.sendMessage("You are not on a crew!");
					event.setCancelled(true);
					return;
				}

				if (split.length == 1) {
					player.sendMessage("Your " + craft.name + " crew...");
					if (craft.captainName != null) {
						player.sendMessage("Captain - " + craft.captainName);
					}
					for (String s : craft.crewNames) {
						if (s != craft.captainName) {
							player.sendMessage(s);
						}
					}
				} else {
					String msgString;
					msgString = "> ";
					for (int i = 1; i < split.length; i++) {
						msgString += split[i] + " ";
					}

					for (String s : craft.crewNames) {
						Player p = plugin.getServer().getPlayer(s);
						if (p != null) {
							if (player.getName() == craft.captainName) {
								p.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Captain] <"
										+ player.getName() + msgString);

							} else {
								p.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Crew] <"
										+ player.getName() + msgString);

							}
						}
					}

					if (player.getName() == craft.captainName) {
						System.out.println("[Captain] <" + player.getName() + msgString);
					} else {
						System.out.println("[Crew] <" + player.getName() + msgString);
					}
				}
				event.setCancelled(true);
				return;
			} else if (craftName.equalsIgnoreCase("radio") || craftName.equalsIgnoreCase("ra")) {
				Craft craft = Craft.getPlayerCraft(player);
				if (craft == null) {
					player.sendMessage("You are not on a crew!");
					event.setCancelled(true);
					return;
				}

				if (split.length == 1) {
					if ((craft.radioSignLoc != null) && (craft.maxY >= 63) && craft.radioSetOn) {
						player.sendMessage("Your radio is Active on frequency-" + craft.radio1 + "" + craft.radio2 + ""
								+ craft.radio3 + "" + craft.radio4);

						int craftCount = 0;
						for (Craft c : Craft.craftList) {
							if (c.captainName != null) {
								if (c.radio1 == craft.radio1) {
									if (c.radio2 == craft.radio2) {
										if (c.radio3 == craft.radio3) {
											if (c.radio4 == craft.radio4) {
												craftCount++;
											}
										}
									}
								}
							}
						}
						player.sendMessage("There are " + craftCount + " vehicles on your frequency.");

					} else if ((craft.radioSignLoc != null) && craft.radioSetOn) {
						player.sendMessage("Your radio is disabled because you are underwater...");
					} else if (craft.radioSignLoc != null) {
						player.sendMessage("Your radio is turned off.");
					} else {
						player.sendMessage("No radio detected...");
					}
				} else {
					if (craft.radioSignLoc == null) {
						player.sendMessage("No radio detected...");
						event.setCancelled(true);
						return;
					}

					if (!craft.radioSetOn) {
						player.sendMessage("Your radio is turned off.");
						event.setCancelled(true);
						return;
					}

					if (craft.maxY < 63) {
						player.sendMessage("Your radio will not work underwater.");
						event.setCancelled(true);
						return;
					}

					if ((craft.radio1 == 0) && (craft.radio2 == 0) && (craft.radio3 == 0) && (craft.radio4 == 0)) {
						player.sendMessage("0000 is invalid frequency, use Radio sign to change.");
						event.setCancelled(true);
						return;
					}

					String msgString;
					msgString = "> ";
					for (int i = 1; i < split.length; i++) {
						msgString += split[i] + " ";
					}

					for (String s : craft.crewNames) {
						Player p = plugin.getServer().getPlayer(s);
						if (p != null) {
							if (craft.customName != null) {
								p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[Radio-" + craft.radio1 + ""
										+ craft.radio2 + "" + craft.radio3 + "" + craft.radio4 + "] <"
										+ craft.customName.toUpperCase() + "><" + player.getName() + msgString);
							} else {
								p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[Radio-" + craft.radio1 + ""
										+ craft.radio2 + "" + craft.radio3 + "" + craft.radio4 + "] <"
										+ craft.name.toUpperCase() + "><" + player.getName() + msgString);
							}
						}
					}

					for (Craft c : Craft.craftList) {
						if ((c != craft) && c.radioSetOn) {
							if (c.radio1 == craft.radio1) {
								if (c.radio2 == craft.radio2) {
									if (c.radio3 == craft.radio3) {
										if (c.radio4 == craft.radio4) {
											if ((c.world == craft.world)
													&& (c.getLocation().distance(craft.getLocation()) < 2000)) {
												for (String s : c.crewNames) {
													Player p = plugin.getServer().getPlayer(s);
													if (p != null) {
														if (craft.customName != null) {
															p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD
																	+ "[Radio-" + craft.radio1 + "" + craft.radio2 + ""
																	+ craft.radio3 + "" + craft.radio4 + "] <"
																	+ craft.customName.toUpperCase() + "><"
																	+ player.getName() + msgString);
														} else {
															p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD
																	+ "[Radio-" + craft.radio1 + "" + craft.radio2 + ""
																	+ craft.radio3 + "" + craft.radio4 + "] <"
																	+ craft.name.toUpperCase() + "><" + player.getName()
																	+ msgString);
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}

					if (craft.customName != null) {
						System.out.println("[Radio-" + craft.radio1 + "" + craft.radio2 + "" + craft.radio3 + ""
								+ craft.radio4 + "]<" + craft.customName + "><" + player.getName() + msgString);
					} else {
						System.out.println("[Radio-" + craft.radio1 + "" + craft.radio2 + "" + craft.radio3 + ""
								+ craft.radio4 + "]<" + craft.name + "><" + player.getName() + msgString);
					}

					craft.lastRadioPulse = System.currentTimeMillis();

				}
				event.setCancelled(true);
				return;
			} else if (craftName.equalsIgnoreCase("sign")) {
				if (split.length == 2) {
					if (split[1].equalsIgnoreCase("undo")) {
						if (NavyCraft.playerLastBoughtSign.containsKey(player)) {
							if ((NavyCraft.playerLastBoughtSign.get(player).getTypeId() == 68)
									|| (NavyCraft.playerLastBoughtSign.get(player).getTypeId() == 63)) {
								Sign sign = (Sign) NavyCraft.playerLastBoughtSign.get(player).getState();
								String signString0 = sign.getLine(0).trim().toLowerCase();
								signString0 = signString0.replaceAll(ChatColor.BLUE.toString(), "");
								String signString1 = sign.getLine(1).trim().toLowerCase();
								signString1 = signString1.replaceAll(ChatColor.BLUE.toString(), "");
								String signString2 = sign.getLine(2).trim().toLowerCase();
								signString2 = signString2.replaceAll(ChatColor.BLUE.toString(), "");
								if (signString0.equalsIgnoreCase(NavyCraft.playerLastBoughtSignString0.get(player))
										&& signString1
												.equalsIgnoreCase(NavyCraft.playerLastBoughtSignString1.get(player))
										&& signString2
												.equalsIgnoreCase(NavyCraft.playerLastBoughtSignString2.get(player))) {
									NavyCraft.playerLastBoughtSign.get(player).setTypeId(0);
									Essentials ess;
									ess = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
									if (ess == null) {
										player.sendMessage("Essentials Economy error");
										event.setCancelled(true);
										return;
									}
									player.sendMessage("Undoing sign and refunding player.");
									try {
										ess.getUser(player)
												.giveMoney(new BigDecimal(NavyCraft.playerLastBoughtCost.get(player)));
									} catch (MaxMoneyException e) {
										
										e.printStackTrace();
									}
									NavyCraft.playerLastBoughtSign.remove(player);
									NavyCraft.playerLastBoughtCost.remove(player);
									NavyCraft.playerLastBoughtSignString0.remove(player);
									NavyCraft.playerLastBoughtSignString1.remove(player);
									NavyCraft.playerLastBoughtSignString2.remove(player);
								} else {
									player.sendMessage("Incorrect sign detected.");
								}

							} else {
								player.sendMessage("No sign detected to undo.");
							}
						} else {
							player.sendMessage("Nothing to undo.");
						}
						event.setCancelled(true);
						return;
					}
				}
			} else if (craftName.equalsIgnoreCase("sailor")) {
				if (PermissionInterface.CheckEnabledWorld(player.getLocation())) {
					if (PermissionInterface.CheckPerm(player, "navycraft.kit")) {
						if (!NavyCraft.playerKits.contains(player.getName())) {
							player.sendMessage("Anchors Aweigh!");
							player.getInventory().addItem(new ItemStack(Material.IRON_SWORD, 1));
							player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE, 1));
							player.getInventory().addItem(new ItemStack(Material.IRON_AXE, 1));
							player.getInventory().addItem(new ItemStack(Material.BOW, 1));
							player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
							player.getInventory().addItem(new ItemStack(Material.LADDER, 10));
							player.getInventory().addItem(new ItemStack(Material.COOKED_FISH, 64));
							player.getInventory().addItem(new ItemStack(Material.WOOD, 20));
							player.getInventory().addItem(new ItemStack(Material.SMOOTH_BRICK, 20));
							player.getInventory().addItem(new ItemStack(Material.GLASS, 10));
							player.getInventory().addItem(new ItemStack(Material.STONE_BUTTON, 5));
							player.getInventory().addItem(new ItemStack(Material.LEVER, 5));
							player.getInventory().addItem(new ItemStack(Material.BOAT, 1));
							player.getInventory().addItem(new ItemStack(Material.TNT, 1));
							player.getInventory().addItem(new ItemStack(Material.REDSTONE_TORCH_ON, 1));
							NavyCraft.playerKits.add(player.getName());
						} else {
							player.sendMessage("You only get one sailor kit per life!");
						}
					}else{
						player.sendMessage("You do not have permission to use that kit.");
					}
				} else {
					player.sendMessage("You can only get this kit in enabled worlds.");
				}
				event.setCancelled(true);
				return;
			} else if (craftName.equalsIgnoreCase("volume")) {
				if (split.length == 2) {
					float inValue = 1.0f;
					try {
						inValue = Float.parseFloat(split[1]);
						if ((inValue >= 0) && (inValue <= 100.0f)) {
							NavyCraft.playerEngineVolumes.put(player, inValue);
							player.sendMessage("Volume set - " + inValue + "%");
						} else {
							player.sendMessage("Invalid volume percent, use a number from 0 to 100");
						}
					} catch (NumberFormatException e) {
						player.sendMessage("Invalid volume percent, use a number from 0 to 100");
					}
				} else {
					player.sendMessage("Change engine volume with /volume <%> with % from 0 to 100");
				}
				
				event.setCancelled(true);
				return;
			} else if (craftName.equalsIgnoreCase("explode")) {
				if (PermissionInterface.CheckPerm(player, "navycraft.admin")) {
					if (split.length == 2) {
						float inValue = 1.0f;
						try {
							inValue = Float.parseFloat(split[1]);
							if ((inValue >= 1) && (inValue <= 100.0f)) {
								NavyCraft.explosion((int)inValue, player.getLocation().getBlock(),false);
								player.sendMessage("Boom Level - " + inValue);
							} else {
								player.sendMessage("Invalid explosion level, use a number from 1 to 100");
							}
						} catch (NumberFormatException e) {
							player.sendMessage("Invalid explosion level, use a number from 1 to 100");
						}
					} else {
						player.sendMessage("command /explode ###  number from 1-100");
					}
				}else {
					player.sendMessage("You do not have permission to use that.");
				}
				
				event.setCancelled(true);
				return;
			}else if (craftName.equalsIgnoreCase("explodesigns")) {
				if (PermissionInterface.CheckPerm(player, "navycraft.admin")) {
					if (split.length == 2) {
						float inValue = 1.0f;
						try {
							inValue = Float.parseFloat(split[1]);
							if ((inValue >= 1) && (inValue <= 100.0f)) {
								NavyCraft.explosion((int)inValue, player.getLocation().getBlock(),true);
								player.sendMessage("Boom Level - " + inValue);
							} else {
								player.sendMessage("Invalid explosion level, use a number from 1 to 100");
							}
						} catch (NumberFormatException e) {
							player.sendMessage("Invalid explosion level, use a number from 1 to 100");
						}
					} else {
						player.sendMessage("command /explode ###  number from 1-100");
					}
				}else{
					player.sendMessage("You do not have permission to use that.");
				}
				
				event.setCancelled(true);
				return;
			} else if (craftType != null) {

				if (processCommand(craftType, player, split) == true) {
					event.setCancelled(true);
				}
			} else {
				Craft craft = Craft.getPlayerCraft(player);

				if (craft == null) {
					return;
				}

				int i = 0;
				while (i < split.length) {
					String tmpName = split[0];
					// build out tmpName with 0 + i
					if (tmpName.equalsIgnoreCase(craft.name)) {
						if (processCommand(craftType, player, split) == true) {
							event.setCancelled(true);
						}
					}
					i++;
				}
			}
		}

		return;
	}

	@SuppressWarnings("deprecation")
	public boolean processCommand(CraftType craftType, Player player, String[] split) {

		Craft craft = Craft.getPlayerCraft(player);

		if (split.length >= 2) {

			if (split[1].equalsIgnoreCase(craftType.driveCommand) && PermissionInterface.CheckPerm(player,  "navycraft.remote")) {

				String name = craftType.name;
				if ((split.length > 2) && (split[2] != null)) {
					name = split[2];
				}

				// try to detect and create the craft
				// use the block the player is standing on
				Craft checkCraft = Craft.getCraft(player.getLocation().getBlockX(), player.getLocation().getBlockY(),
						player.getLocation().getBlockZ());
				if (checkCraft != null) {

				} else {
					NavyCraft.instance.createCraft(player, craftType, (int) Math.floor(player.getLocation().getX()),
							(int) Math.floor(player.getLocation().getY() - 1),
							(int) Math.floor(player.getLocation().getZ()), name, player.getLocation().getYaw(), null,
							false);
				}
				return true;

			} else if (split[1].equalsIgnoreCase("move") && PermissionInterface.CheckPerm(player,  "navycraft.remote")) {
				try {
					int dx = Integer.parseInt(split[2]);
					int dy = Integer.parseInt(split[3]);
					int dz = Integer.parseInt(split[4]);

					CraftMover cm = new CraftMover(craft, plugin);
					cm.calculateMove(dx, dy, dz);
				} catch (Exception ex) {
					player.sendMessage(ChatColor.WHITE + "Invalid movement parameters. Please use " + ChatColor.AQUA
							+ "Move x y z " + ChatColor.WHITE
							+ " Where x, y, and z are whole numbers separated by spaces.");
				}
				return true;
			} else if (split[1].equalsIgnoreCase("setspeed") && PermissionInterface.CheckPerm(player,  "navycraft.remote")) {
				int speed = Math.abs(Integer.parseInt(split[2]));

				if ((speed < 1) || (speed > craftType.maxSpeed)) {
					player.sendMessage(ChatColor.YELLOW + "Allowed speed between 1 and " + craftType.maxSpeed);
					return true;
				}

				craft.setSpeed(speed);
				player.sendMessage(ChatColor.YELLOW + craft.name + "'s speed set to " + craft.speed);

				return true;

			} else if (split[1].equalsIgnoreCase("setname")) {
				if( split.length > 2 )
				{
					craft.name = split[2];
					player.sendMessage(ChatColor.YELLOW + craft.type.name + "'s name set to " + craft.name);
					return true;
				}

			} else if (split[1].equalsIgnoreCase("remote") && PermissionInterface.CheckPerm(player,  "navycraft.remote")) {
				if ((craft == null) || (craft.type != craftType)) {
					Set<Material> meh = new HashSet<>();
					Block targetBlock = player.getTargetBlock(meh, 100);

					if (targetBlock != null) {
						NavyCraft.instance.createCraft(player, craftType, targetBlock.getX(), targetBlock.getY(),
								targetBlock.getZ(), null, player.getLocation().getYaw(), null, false);
						Craft.getPlayerCraft(player).isNameOnBoard.put(player.getName(), false);
					} else {
						player.sendMessage("Couldn't find a target within 100 blocks. "
								+ "If your admin asks reeeaaaaaally nicely, I might add distance as a config setting.");
					}

					return true;
				}

				if (craft.isOnCraft(player, true)) {
					player.sendMessage(
							ChatColor.YELLOW + "You are on the " + craftType.name + ", remote control not possible");
				} else {
					if (craft.haveControl) {
						player.sendMessage(ChatColor.YELLOW + "You switch off the remote controller");
					} else {
						MoveCraft_Timer timer = MoveCraft_Timer.playerTimers.get(player);
						if (timer != null) {
							timer.Destroy();
						}
						player.sendMessage(ChatColor.YELLOW + "You switch on the remote controller");
					}

					craft.haveControl = !craft.haveControl;
				}

				return true;

			} else if (split[1].equalsIgnoreCase("release")) {
				// MoveCraft.instance.releaseCraft(player, craft);
				if (craft != null) {
					if ((craft.captainName == player.getName()) || player.isOp()) {
						player.sendMessage(ChatColor.YELLOW + "You release command of the ship");
						craft.releaseCraft();
						if (player.getInventory().contains(Material.GOLD_SWORD)) {
							player.getInventory().remove(Material.GOLD_SWORD);
						}
					}
				} else {
					player.sendMessage(ChatColor.YELLOW + "No vehicle detected.");
				}
				return true;

			} else if (split[1].equalsIgnoreCase("reload")
					&& (PermissionInterface.CheckPerm(player,  "navycraft.reload"))) {


				if (craft != null) {
					CraftMover cm = new CraftMover(craft, plugin);
					cm.reloadWeapons(player);
				} else {
					player.sendMessage(ChatColor.YELLOW + "No vehicle detected.");
				}

				// }

				return true;

			}else if (split[1].equalsIgnoreCase("drive")
					&& (PermissionInterface.CheckPerm(player,  "navycraft.drive"))) {
				if (player.getItemInHand().getTypeId() > 0) {
					player.sendMessage(ChatColor.RED + "Have nothing in your hand before using this.");
					return true;
				}
				craft.driverName = player.getName();
				craft.haveControl = true;
				player.sendMessage(ChatColor.YELLOW + "You take control of the helm.");
				player.setItemInHand(new ItemStack(283, 1));
				CraftMover cm = new CraftMover(craft, plugin);
				cm.structureUpdate(null, false);

				return true;

			} else if (split[1].equalsIgnoreCase("info")) {

				player.sendMessage(ChatColor.WHITE + craftType.name);
				if (craft != null) {
					player.sendMessage(ChatColor.YELLOW + "Using " + craft.blockCount + " of " + craftType.maxBlocks
							+ " blocks (minimum " + craftType.minBlocks + ").");

				} else {
					player.sendMessage(ChatColor.YELLOW + Integer.toString(craftType.minBlocks) + "-"
							+ craftType.maxBlocks + " blocks.");
				}
				player.sendMessage(ChatColor.YELLOW + "Max speed: " + craftType.maxSpeed);

				if (NavyCraft.instance.DebugMode) {
					player.sendMessage(ChatColor.YELLOW + Integer.toString(craft.dataBlocks.size()) + " data Blocks, "
							+ craft.complexBlocks.size() + " complex Blocks, " + craft.engineBlocks.size()
							+ " engine Blocks," + craft.digBlockCount + " drill bits.");
				}



				String canDo = ChatColor.YELLOW + craftType.name + "s can ";

				if (craftType.canFly) {
					canDo += "fly, ";
				}

				if (craftType.canDive) {
					canDo += "dive, ";
				}

				if (craftType.canDig) {
					canDo += "dig, ";
				}

				if (craftType.canNavigate) {
					canDo += " navigate on both water and lava, ";
				}

				player.sendMessage(canDo);

				/*if (craftType.flyBlockType != 0) {
					int flyBlocksNeeded = (int) Math.floor(
							((craft.blockCount - craft.flyBlockCount) * ((float) craft.type.flyBlockPercent * 0.01))
									/ (1 - ((float) craft.type.flyBlockPercent * 0.01)));

					if (flyBlocksNeeded < 1) {
						flyBlocksNeeded = 1;
					}

					player.sendMessage(ChatColor.YELLOW + "Flight requirement: " + craftType.flyBlockPercent + "%"
							+ " of " + BlocksInfo.getName(craft.type.flyBlockType) + "(" + flyBlocksNeeded + ")");
				}

				if (craft.type.fuelItemId != 0) {
					player.sendMessage(craft.remainingFuel + " units of fuel on board. " + "Movement requires type "
							+ craft.type.fuelItemId);
				}*/

				return true;

			} else if (split[1].equalsIgnoreCase("hyperspace") && (PermissionInterface.CheckPerm(player,  "navycraft.other"))) {
				if (!craft.inHyperSpace) {
					Craft_Hyperspace.enterHyperSpace(craft);
				} else {
					Craft_Hyperspace.exitHyperSpace(craft);
				}
				return true;
			} else if (split[1].equalsIgnoreCase("addwaypoint") && (PermissionInterface.CheckPerm(player,  "navycraft.other"))) {
				// if(split[2].equalsIgnoreCase("absolute"))
				if (split[2].equalsIgnoreCase("relative")) {
					Location newLoc = craft.WayPoints.get(craft.WayPoints.size() - 1);
					if (!split[3].equalsIgnoreCase("0")) {
						newLoc.setX(newLoc.getX() + Integer.parseInt(split[3]));
					} else if (!split[4].equalsIgnoreCase("0")) {
						newLoc.setY(newLoc.getY() + Integer.parseInt(split[4]));
					} else if (!split[5].equalsIgnoreCase("0")) {
						newLoc.setZ(newLoc.getZ() + Integer.parseInt(split[5]));
					}

					craft.addWayPoint(newLoc);
				} else {
					craft.addWayPoint(player.getLocation());
				}

				player.sendMessage("Added waypoint...");

			} else if (split[1].equalsIgnoreCase("autotravel") && (PermissionInterface.CheckPerm(player,  "navycraft.other"))) {
				if (split[2].equalsIgnoreCase("true")) {
					new MoveCraft_Timer(plugin, 0, craft, player, "automove", true);
				} else {
					new MoveCraft_Timer(plugin, 0, craft, player, "automove", false);
				}

			} else if (split[1].equalsIgnoreCase("dock")) {
				if (craft != null) {
					if (craft.driverName == player.getName()) {
						if (craft.autoTurn) {
							player.sendMessage("Docking mode engaged");
						} else {
							player.sendMessage("Docking mode disengaged");
						}
						craft.autoTurn = !craft.autoTurn;
					}
				} else {
					player.sendMessage(ChatColor.YELLOW + "No vehicle detected.");
				}
				return true;
			} else if (split[1].equalsIgnoreCase("command")
					&& (PermissionInterface.CheckPerm(player,  "navycraft.admin"))) {
				Craft testCraft = Craft.getCraft(player.getLocation().getBlockX(), player.getLocation().getBlockY(),
						player.getLocation().getBlockZ());
				if (testCraft != null) {
					if (craft != null)// && playerCraft.type == craftType) {
					{

						craft.leaveCrew(player);
					}

					testCraft.buildCrew(player, false);


					CraftMover cm = new CraftMover(testCraft, plugin);
					cm.structureUpdate(null, false);
					if (testCraft.captainName == player.getName()) {
						player.sendMessage("You admin-hijack this vehicle!");
					}
				}
				return true;
			} else if (split[1].equalsIgnoreCase("remove")) {
				if (craft != null) {
					if ((craft.captainName == player.getName()) || (player.isOnline() && player.isOp())) {
						if (PermissionInterface.CheckPerm(player,  "navycraft.admin")) {
							craft.doRemove = true;
							if (player.getInventory().contains(Material.GOLD_SWORD)) {
								player.getInventory().remove(Material.GOLD_SWORD);
							}
							player.sendMessage("Vehicle Removed");
						} else {
							player.sendMessage(ChatColor.RED
									+ "You do not have permission for this command. Use \"/ship disable\" instead.");
						}

					} else {
						player.sendMessage(ChatColor.RED + "You do not command this ship.");
					}
				} else {
					player.sendMessage(ChatColor.YELLOW + "No vehicle detected.");
				}
				return true;
			} else if (split[1].equalsIgnoreCase("disable")) {
				if (craft != null) {
					if ((craft.captainName == player.getName()) || (player.isOnline() && player.isOp())) {
						if (NavyCraft.checkRepairRegion(craft.getLocation())) {
							craft.doRemove = true;
							if (player.getInventory().contains(Material.GOLD_SWORD)) {
								player.getInventory().remove(Material.GOLD_SWORD);
							}
							player.sendMessage("Vehicle disabled.");
						} else if (!checkProtectedRegion(player, player.getLocation())) {
							craft.helmDestroyed = true;
							craft.setSpeed = 0;
							playerDisableThread(player, craft);
							player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD
									+ "Your vehicle will be fully disabled in 3 minutes.");
						} else {
							player.sendMessage(ChatColor.RED
									+ "You can only use that command in a repair dock within the safe dock area.");
						}
					} else {
						player.sendMessage(ChatColor.RED + "You do not command this ship.");
					}
				} else {
					player.sendMessage(ChatColor.YELLOW + "No vehicle detected.");
				}
				return true;
			} else if (split[1].equalsIgnoreCase("destroy")) {
				if (craft != null) {
					if ((craft.captainName == player.getName()) || player.isOp()) {
						if (checkProtectedRegion(player, craft.getLocation()) || PermissionInterface.CheckPerm(player,  "navycraft.admin")) {
							craft.doDestroy = true;
							if (player.getInventory().contains(Material.GOLD_SWORD)) {
								player.getInventory().remove(Material.GOLD_SWORD);
							}
							player.sendMessage("Vehicle Destroyed");
						} else {
							player.sendMessage(
									ChatColor.RED + "You can only use this command in a safe dock region.");
						}

					} else {
						player.sendMessage(ChatColor.RED + "You do not command this ship.");
					}
				} else {
					player.sendMessage(ChatColor.YELLOW + "No vehicle detected.");
				}
				return true;
			} else if (split[1].equalsIgnoreCase("sink")) {
				if (craft != null) {
					if (!craft.sinking) {
						if (craft.captainName == player.getName()) {
							if (!checkProtectedRegion(player, craft.getLocation())) {

								craft.helmDestroyed = true;
								craft.setSpeed = 0;
								playerSinkThread(craft);
								player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD
										+ "Your vehicle will be scuttled in 3 minutes.");
							} else {
								player.sendMessage(
										ChatColor.RED + "This command cannot be used within a protected region.");
							}
						} else {
							player.sendMessage(ChatColor.RED + "You do not command this ship.");
						}
					} else {
						player.sendMessage(ChatColor.RED + "You are already sinking!");
					}
				} else {
					player.sendMessage(ChatColor.YELLOW + "No vehicle detected.");
				}
				return true;
			} else if (split[1].equalsIgnoreCase("update")) {

				return true;
			} else if (split[1].equalsIgnoreCase("turn") && PermissionInterface.CheckPerm(player,  "navycraft.remote")) {

				if (craft != null) {
					if (craft.autoTurn) {
						if ((split.length > 2) && (split[2] != null)) {
							if (split[2].equalsIgnoreCase("right")) {
								craft.turn(90);
								return true;
							} else if (split[2].equalsIgnoreCase("left")) {
								craft.turn(270);
								return true;
							} else if (split[2].equalsIgnoreCase("around")) {
								craft.turn(180);
								return true;
							}
							return false;
						} else {
							return false;
						}
					} else {
						player.sendMessage(ChatColor.YELLOW + "You cannot use this command on this vehicle.");
					}
				} else {
					player.sendMessage(ChatColor.YELLOW + "No vehicle detected.");
				}
			} else if (split[1].equalsIgnoreCase("warpdrive")&& PermissionInterface.CheckPerm(player,  "navycraft.other") ) {

				if (split.length == 1) {
					List<World> worlds = NavyCraft.instance.getServer().getWorlds();
					player.sendMessage("You can warp to: ");
					for (World world : worlds) {
						player.sendMessage(world.getName());
					}
				} else {
					World targetWorld = NavyCraft.instance.getServer().getWorld(split[2]);
					if (targetWorld != null) {
						craft.WarpToWorld(targetWorld);
					} else if (player.isOp()) { // create the world, if the
												// player is an op
						if ((split.length > 3) && split[3].equalsIgnoreCase("nether")) {

						} else {

						}

						while (targetWorld == null) {
							try {
								wait(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							targetWorld = NavyCraft.instance.getServer().getWorld(split[2]);
						}
						Chunk targetChunk = targetWorld
								.getChunkAt(new Location(targetWorld, craft.minX, craft.minY, craft.minZ));
						targetWorld.loadChunk(targetChunk);

						craft.WarpToWorld(targetWorld);
					}
				}
			} else if (split[1].equalsIgnoreCase("leave")) {
				if (craft != null) {
					craft.leaveCrew(player);
					player.sendMessage(ChatColor.YELLOW + "You leave the crew.");
				} else {
					player.sendMessage(ChatColor.YELLOW + "You are not on a crew.");
				}
				return true;
			} else if (split[1].equalsIgnoreCase("crew")) {
				if (craft != null) {
					if (craft.captainName == player.getName()) {
						craft.buildCrew(player, false);
					} else {
						player.sendMessage(ChatColor.YELLOW + "You are not the captain of this crew.");
					}
				} else {
					player.sendMessage(ChatColor.YELLOW + "You are not on a crew.");
				}
				return true;
			} else if (split[1].equalsIgnoreCase("add")) {
				if (craft != null) {
					if (craft.captainName == player.getName()) {
						craft.buildCrew(player, true);
					} else {
						player.sendMessage(ChatColor.YELLOW + "You are not the captain of this crew.");
					}
				} else {
					player.sendMessage(ChatColor.YELLOW + "You are not on a crew.");
				}
				return true;
			} else if (split[1].equalsIgnoreCase("summon")) {
				if (craft != null) {
					if (craft.captainName == player.getName()) {
						if (craft.signLoc != null) {
							player.sendMessage(ChatColor.YELLOW + "Summoning crew to your vehicle...");
							for (String s : craft.crewNames) {
								Player p = plugin.getServer().getPlayer(s);
								if (p != null) {
									if (!NavyCraft.shipTPCooldowns.containsKey(s) || (System
											.currentTimeMillis() > (NavyCraft.shipTPCooldowns.get(s) + 600000))) {
										NavyCraft.shipTPCooldowns.put(s, System.currentTimeMillis());
										p.teleport(new Location(craft.world, craft.signLoc.getBlockX() + 0.5,
												craft.signLoc.getBlockY() + 0.1, craft.signLoc.getBlockZ() + 0.5));
									} else {
										int timeLeft = (int) (((NavyCraft.shipTPCooldowns.get(s) + 600000)
												- System.currentTimeMillis()) / 60000);
										player.sendMessage(ChatColor.RED + "Player-" + s + " is on cooldown for "
												+ timeLeft + " min");
									}
								} else {
									player.sendMessage(ChatColor.RED + "Player-" + s + " not located.");
								}
							}
						} else {
							player.sendMessage(ChatColor.RED + "Vehicle sign not located.");
						}
					} else {
						player.sendMessage(ChatColor.YELLOW + "You are not the captain of this crew.");
					}
				} else {
					player.sendMessage(ChatColor.YELLOW + "You are not on a crew.");
				}
				return true;
			} else if (split[1].equalsIgnoreCase("tp")) {
				if (craft != null) {
					if (craft.signLoc != null) {
						if (!NavyCraft.shipTPCooldowns.containsKey(player.getName()) || (System
								.currentTimeMillis() > (NavyCraft.shipTPCooldowns.get(player.getName()) + 600000))) {
							NavyCraft.shipTPCooldowns.put(player.getName(), System.currentTimeMillis());
							player.teleport(new Location(craft.world, craft.signLoc.getBlockX() + 0.5,
									craft.signLoc.getBlockY() + 0.1, craft.signLoc.getBlockZ() + 0.5));
						} else {
							int timeLeft = (int) (((NavyCraft.shipTPCooldowns.get(player.getName()) + 600000)
									- System.currentTimeMillis()) / 60000);
							player.sendMessage(ChatColor.RED + "You are on cooldown for " + timeLeft + " min");
						}
					} else {
						player.sendMessage(ChatColor.RED + "Vehicle sign not located.");
					}
				} else {
					player.sendMessage(ChatColor.YELLOW + "You are not on a crew.");
				}
				return true;
			} else if (split[1].equalsIgnoreCase("buoy") && PermissionInterface.CheckPerm(player,  "navycraft.admin")) {
				if (craft != null) {
					if ((split.length > 3) && split[2].equalsIgnoreCase("block")) {
						float blockValue = 0.33f;
						try {
							blockValue = Float.parseFloat(split[3]);
							if ((blockValue >= 0.01f) && (blockValue <= 100.0f)) {
								craft.blockDispValue = blockValue;
							} else {
								player.sendMessage("Invalid block displacement value, use 0.01 to 100.0");
							}
						} catch (NumberFormatException e) {
							player.sendMessage("Invalid block displacement value, use 0.01 to 100.0");
						}
					} else if ((split.length > 3) && split[2].equalsIgnoreCase("air")) {
						float airValue = 5.00f;
						try {
							airValue = Float.parseFloat(split[3]);
							if ((airValue >= 0.01f) && (airValue <= 100.0f)) {
								craft.airDispValue = airValue;
							} else {
								player.sendMessage("Invalid air displacement value, use 0.01 to 100.0");
							}
						} catch (NumberFormatException e) {
							player.sendMessage("Invalid air displacement value, use 0.01 to 100.0");
						}
					} else if ((split.length > 3) && split[2].equalsIgnoreCase("min")) {
						float minValue = 5.00f;
						try {
							minValue = Float.parseFloat(split[3]);
							if ((minValue >= 0.01f) && (minValue <= 100.0f)) {
								craft.minDispValue = minValue;
							} else {
								player.sendMessage("Invalid min displacement value, use 0.01 to 100.0");
							}
						} catch (NumberFormatException e) {
							player.sendMessage("Invalid min displacement value, use 0.01 to 100.0");
						}
					} else if ((split.length > 3) && split[2].equalsIgnoreCase("weight")) {
						float weightValue = 1.00f;
						try {
							weightValue = Float.parseFloat(split[3]);
							if ((weightValue >= 0.01f) && (weightValue <= 100.0f)) {
								craft.weightMult = weightValue;
							} else {
								player.sendMessage("Invalid weight multiplier value, use 0.01 to 100.0");
							}
						} catch (NumberFormatException e) {
							player.sendMessage("Invalid weight multiplier value, use 0.01 to 100.0");
						}
					} else {

						player.sendMessage("Block Displacement = " + craft.blockDispValue
								+ ", use \"/ship buoy block <value>\" to set");
						player.sendMessage(
								"Air Displacement = " + craft.airDispValue + ", use \"/ship buoy air <value>\" to set");
						player.sendMessage("Minimum Displacement = " + craft.minDispValue
								+ ", use \"/ship buoy min <value>\" to set");
						player.sendMessage("Weight Multiplier = " + craft.weightMult
								+ ", use \"/ship buoy weight <value>\" to set");
					}
				}
				return true;
			} else {
				if( PermissionInterface.CheckPerm(player, "navycraft.basic") ){
					player.sendMessage("/ship - Ship Status");
					player.sendMessage("/ship release - (Cpt) Release your command of the ship");
					player.sendMessage("/ship leave - Leave the crew of your ship");
					player.sendMessage("/ship crew - (Cpt) Recreates your crew with players on your vehicle");
					player.sendMessage("/ship add - (Cpt) Add players on your vehicle to your crew");
					player.sendMessage(
							"/ship summon - (Cpt) Teleports you and your crew to your vehicle (10 min cooldown)");
					player.sendMessage("/ship tp - Teleport to your vehicle (10 min cooldown)");
					player.sendMessage("/ship repair - (Cpt) Repairs your vehicle if in repair dock region");
					player.sendMessage("/ship store - (Cpt) Stores your vehicle if in a storage dock region");
					player.sendMessage("/ship disable - (Cpt) Deactivates a vehicle, so that it can be modified");
					player.sendMessage("/ship sink - (Cpt) Scuttles your vehicle after a timer");
					player.sendMessage("/ship destroy - (Cpt) Destroys your vehicle, usable in safedock region");
					player.sendMessage("/radio <message> - (or /ra) Send radio message (if equipped)");
					player.sendMessage("/radio - (or /ra) Radio status");
					player.sendMessage("/crew <message> - Send message to your crew");
					player.sendMessage("/crew - Crew status");
				}
				if (PermissionInterface.CheckQuietPerm(player, "navycraft.admin")) {
					player.sendMessage("/ship command - (Mod) Steal command of a ship");
					player.sendMessage("/ship remove - (Mod) Instantly disable a ship");
					player.sendMessage("/ship drive - (Mod) Drive without sign");
					player.sendMessage("/ship buoy - (Mod) View and modify buoyancy variables");
				}
				return true;
			}
		}

		if (craft != null) {
			player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Vehicle Status");
			player.sendMessage(ChatColor.YELLOW + "Type : " + ChatColor.WHITE + craft.name);
			if (craft.customName != null) {
				player.sendMessage(ChatColor.YELLOW + "Name : " + ChatColor.WHITE + craft.customName);
			} else {
				player.sendMessage(ChatColor.YELLOW + "Name : " + ChatColor.WHITE + craft.name);
			}
			player.sendMessage(ChatColor.YELLOW + "Captain : " + ChatColor.WHITE + craft.captainName);
			player.sendMessage(ChatColor.YELLOW + "Crew : " + ChatColor.WHITE + craft.crewNames.size());
			player.sendMessage(ChatColor.YELLOW + "Size : " + ChatColor.WHITE + craft.blockCount + " blocks");
			player.sendMessage(ChatColor.YELLOW + "Weight (current) : " + ChatColor.WHITE + craft.weightCurrent + " tons");
			player.sendMessage(ChatColor.YELLOW + "Weight (start) : " + ChatColor.WHITE + craft.weightStart + " tons");
			player.sendMessage(ChatColor.YELLOW + "Displacement : " + ChatColor.WHITE + craft.displacement + " tons ("
					+ craft.blockDisplacement + " block," + craft.airDisplacement + " air)");
			player.sendMessage(ChatColor.YELLOW + "Health : " + ChatColor.WHITE
					+ (int) (((float) craft.blockCount * 100) / craft.blockCountStart) + "%");
			player.sendMessage(ChatColor.YELLOW + "Engines : " + ChatColor.WHITE + craft.engineIDLocs.size() + " of "
					+ craft.engineIDIsOn.size());
			if (craft.isAutoCraft) {
				player.sendMessage(ChatColor.YELLOW + "Auto Merchant : " + ChatColor.WHITE + craft.routeID + ":"
						+ craft.routeStage);
			}
		} else {
			player.sendMessage(ChatColor.YELLOW + "You have no active vehicle.");
		}

		return true;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerEggThrow(PlayerEggThrowEvent event) {
		// event.getEgg().remove();
		Egg egg = event.getEgg();

		if (NavyCraft.explosiveEggsList.contains(egg)) {
			if (checkProtectedRegion(event.getPlayer(), egg.getLocation())) {
				event.getPlayer().sendMessage(ChatColor.RED + "No AA Allowed In Dock Area");
				return;
			}

			event.setHatching(false);

			Block eggBlock = egg.getLocation().getBlock();
			int fuseDelay = 5;
			if (eggBlock.getY() >= 63) {
				Craft checkCraft = Craft.getPlayerCraft(event.getPlayer());
				if (checkCraft != null) {
					if (checkCraft.isIn(eggBlock.getX(), eggBlock.getY(), eggBlock.getZ())) {
						return;
					}
				}

				int blockType = eggBlock.getTypeId();
				double randomNum = Math.random();
				if ((blockType != 0) && (blockType != 8) && (blockType != 9)) {

					if (Craft.blockHardness(blockType) == 1) {
						if (randomNum >= .3) {
							eggBlock.setTypeId(0);
						}
					} else if (Craft.blockHardness(blockType) == 0) {
						eggBlock.setTypeId(0);
					} else if ((Craft.blockHardness(blockType) == 46) && (randomNum >= 0.5)) {

						TNTPrimed tnt = (TNTPrimed) eggBlock.getWorld().spawnEntity(
								new Location(eggBlock.getWorld(), eggBlock.getX(), eggBlock.getY(), eggBlock.getZ()),
								org.bukkit.entity.EntityType.PRIMED_TNT);
						tnt.setFuseTicks(fuseDelay);
						fuseDelay = fuseDelay + 100;
					}
				}

				//// north south
				randomNum = Math.random();

				if (randomNum >= .2) {
					blockType = eggBlock.getRelative(BlockFace.NORTH).getTypeId();
					if ((blockType != 0) && (blockType != 8) && (blockType != 9)) {

						if (Craft.blockHardness(blockType) == 1) {
							if (randomNum >= .3) {
								eggBlock.getRelative(BlockFace.NORTH).setTypeId(0);
							}
						} else if (Craft.blockHardness(blockType) == 0) {
							eggBlock.getRelative(BlockFace.NORTH).setTypeId(0);
						} else if ((Craft.blockHardness(blockType) == 46) && (randomNum >= 0.5)) {

							TNTPrimed tnt = (TNTPrimed) eggBlock.getWorld()
									.spawnEntity(new Location(eggBlock.getWorld(), eggBlock.getX(), eggBlock.getY(),
											eggBlock.getZ()), org.bukkit.entity.EntityType.PRIMED_TNT);
							tnt.setFuseTicks(fuseDelay);
							fuseDelay = fuseDelay + 2;
						}
					}

					blockType = eggBlock.getRelative(BlockFace.SOUTH).getTypeId();
					if ((blockType != 0) && (blockType != 8) && (blockType != 9)) {

						if (Craft.blockHardness(blockType) == 1) {
							if (randomNum >= .3) {
								eggBlock.getRelative(BlockFace.SOUTH).setTypeId(0);
							}
						} else if (Craft.blockHardness(blockType) == 0) {
							eggBlock.getRelative(BlockFace.SOUTH).setTypeId(0);
						} else if ((Craft.blockHardness(blockType) == 46) && (randomNum >= 0.5)) {

							TNTPrimed tnt = (TNTPrimed) eggBlock.getWorld()
									.spawnEntity(new Location(eggBlock.getWorld(), eggBlock.getX(), eggBlock.getY(),
											eggBlock.getZ()), org.bukkit.entity.EntityType.PRIMED_TNT);
							tnt.setFuseTicks(fuseDelay);
							fuseDelay = fuseDelay + 2;
						}
					}
				}

				///// east/west
				randomNum = Math.random();

				if (randomNum >= .2) {
					blockType = eggBlock.getRelative(BlockFace.EAST).getTypeId();
					if ((blockType != 0) && (blockType != 8) && (blockType != 9)) {

						if (Craft.blockHardness(blockType) == 1) {
							if (randomNum >= .3) {
								eggBlock.getRelative(BlockFace.EAST).setTypeId(0);
							}
						} else if (Craft.blockHardness(blockType) == 0) {
							eggBlock.getRelative(BlockFace.EAST).setTypeId(0);
						} else if ((Craft.blockHardness(blockType) == 46) && (randomNum >= 0.5)) {

							TNTPrimed tnt = (TNTPrimed) eggBlock.getWorld()
									.spawnEntity(new Location(eggBlock.getWorld(), eggBlock.getX(), eggBlock.getY(),
											eggBlock.getZ()), org.bukkit.entity.EntityType.PRIMED_TNT);
							tnt.setFuseTicks(fuseDelay);

							fuseDelay = fuseDelay + 2;
						}
					}

					blockType = eggBlock.getRelative(BlockFace.WEST).getTypeId();
					if ((blockType != 0) && (blockType != 8) && (blockType != 9)) {

						if (Craft.blockHardness(blockType) == 1) {
							if (randomNum >= .3) {
								eggBlock.getRelative(BlockFace.WEST).setTypeId(0);
							}
						} else if (Craft.blockHardness(blockType) == 0) {
							eggBlock.getRelative(BlockFace.WEST).setTypeId(0);
						} else if ((Craft.blockHardness(blockType) == 46) && (randomNum >= 0.5)) {

							TNTPrimed tnt = (TNTPrimed) eggBlock.getWorld()
									.spawnEntity(new Location(eggBlock.getWorld(), eggBlock.getX(), eggBlock.getY(),
											eggBlock.getZ()), org.bukkit.entity.EntityType.PRIMED_TNT);
							tnt.setFuseTicks(fuseDelay);
							fuseDelay = fuseDelay + 2;
						}
					}
				}

				///// up down
				randomNum = Math.random();

				if (randomNum >= .2) {
					blockType = eggBlock.getRelative(BlockFace.UP).getTypeId();
					if ((blockType != 0) && (blockType != 8) && (blockType != 9)) {

						if (Craft.blockHardness(blockType) == 1) {
							if (randomNum >= .3) {
								eggBlock.getRelative(BlockFace.UP).setTypeId(0);
							}
						} else if (Craft.blockHardness(blockType) == 0) {
							eggBlock.getRelative(BlockFace.UP).setTypeId(0);
						} else if ((Craft.blockHardness(blockType) == 46) && (randomNum >= .5)) {

							TNTPrimed tnt = (TNTPrimed) eggBlock.getWorld()
									.spawnEntity(new Location(eggBlock.getWorld(), eggBlock.getX(), eggBlock.getY(),
											eggBlock.getZ()), org.bukkit.entity.EntityType.PRIMED_TNT);
							tnt.setFuseTicks(fuseDelay);
							fuseDelay = fuseDelay + 2;
						}
					}

					blockType = eggBlock.getRelative(BlockFace.DOWN).getTypeId();
					if ((blockType != 0) && (blockType != 8) && (blockType != 9)) {

						if (Craft.blockHardness(blockType) == 1) {
							if (randomNum >= .3) {
								eggBlock.getRelative(BlockFace.DOWN).setTypeId(0);
							}
						} else if (Craft.blockHardness(blockType) == 0) {
							eggBlock.getRelative(BlockFace.DOWN).setTypeId(0);
						} else if ((Craft.blockHardness(blockType) == 46) && (randomNum >= .5)) {

							TNTPrimed tnt = (TNTPrimed) eggBlock.getWorld()
									.spawnEntity(new Location(eggBlock.getWorld(), eggBlock.getX(), eggBlock.getY(),
											eggBlock.getZ()), org.bukkit.entity.EntityType.PRIMED_TNT);
							tnt.setFuseTicks(fuseDelay);
							fuseDelay = fuseDelay + 2;
						}
					}
				}

				event.getPlayer().getWorld().playEffect(egg.getLocation(), Effect.SMOKE, 0);
				// event.getPlayer().getWorld().playEffect(egg.getLocation(),
				// Effect.CLICK1, 0);
				event.getPlayer().getWorld().playSound(egg.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 1.00f);

				Craft otherCraft = Craft.getOtherCraft(null, event.getPlayer(), egg.getLocation().getBlockX(),
						egg.getLocation().getBlockY(), egg.getLocation().getBlockZ());
				if (otherCraft != null) {
					CraftMover cm = new CraftMover(otherCraft, plugin);
					cm.structureUpdate(event.getPlayer(), false);
				} else {
					otherCraft = Craft.getOtherCraft(null, event.getPlayer(),
							egg.getLocation().getBlock().getRelative(2, 1, 2).getX(),
							egg.getLocation().getBlock().getRelative(2, 1, 2).getY(),
							egg.getLocation().getBlock().getRelative(2, 1, 2).getZ());
					if (otherCraft != null) {
						CraftMover cm = new CraftMover(otherCraft, plugin);
						cm.structureUpdate(event.getPlayer(), false);
					} else {
						otherCraft = Craft.getOtherCraft(null, event.getPlayer(),
								egg.getLocation().getBlock().getRelative(-2, -1, -2).getX(),
								egg.getLocation().getBlock().getRelative(-2, -1, -2).getY(),
								egg.getLocation().getBlock().getRelative(-2, -1, -2).getZ());
						if (otherCraft != null) {
							CraftMover cm = new CraftMover(otherCraft, plugin);
							cm.structureUpdate(event.getPlayer(), false);
						}
					}
				}

			}
			NavyCraft.explosiveEggsList.remove(egg);
			egg.remove();
		}
	}

	public boolean checkProtectedRegion(Player player, Location loc) {
		if ((player != null) && (loc != null)) {
			wgp = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			if (wgp != null) {
				if (!PermissionInterface.CheckEnabledWorld(loc)) {
					return true;
				}
				RegionManager regionManager = wgp.getRegionManager(player.getWorld());

				ApplicableRegionSet set = regionManager.getApplicableRegions(loc);

				Iterator<ProtectedRegion> it = set.iterator();
				while (it.hasNext()) {
					String id = it.next().getId();
					String[] splits = id.split("_");
					if (splits.length == 2) {
						if (splits[1].equalsIgnoreCase("safedock") || splits[1].equalsIgnoreCase("red")
								|| splits[1].equalsIgnoreCase("blue")) {
							return true;
						}
					}

				}
				return false;
			}
			return false;
		}
		return true; // reach here in error, return true to protect property
	}


	public void playerDisableThread(final Player player, final Craft craft) {

		Thread td = new Thread() {

			@Override
			public void run() {

				setPriority(Thread.MIN_PRIORITY);

				try {
					sleep(180000);
					playerDisableUpdate(player, craft);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}; // , 20L);
		td.start();
	}

	public void playerDisableUpdate(final Player player, final Craft craft) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			if ((craft != null) && (player != null) && player.isOnline()) {
				craft.doRemove = true;
				if (player.getInventory().contains(Material.GOLD_SWORD)) {
					player.getInventory().remove(Material.GOLD_SWORD);
				}
				player.sendMessage("Vehicle disabled.");
			}
		});
	}

	public void playerSinkThread(final Craft craft) {

		Thread td = new Thread() {

			@Override
			public void run() {

				setPriority(Thread.MIN_PRIORITY);

				try {
					sleep(180000);
					playerSinkUpdate(craft);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}; // , 20L);
		td.start();
	}

	public void playerSinkUpdate(final Craft craft) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			if (craft != null) {
				craft.sinking = true;
				CraftMover cm = new CraftMover(craft, plugin);
				cm.sinkingThread();
				for (String s : craft.crewNames) {
					Player p = plugin.getServer().getPlayer(s);
					if (p != null) {
						p.sendMessage(ChatColor.RED + "***We're sinking!***");
						p.sendMessage(ChatColor.RED + "***All Hands Abandon Ship!***");
					}
				}
			}
		});
	}


}
