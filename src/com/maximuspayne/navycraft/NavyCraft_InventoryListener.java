package com.maximuspayne.navycraft;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.plugin.Plugin;

public class NavyCraft_InventoryListener implements Listener {


    public NavyCraft_InventoryListener(Plugin p) {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCraftItem(CraftItemEvent event) 
    {
    	if( event.getRecipe().getResult().getType() == Material.STONE_SWORD
    			|| event.getRecipe().getResult().getType() == Material.IRON_SWORD
    			|| event.getRecipe().getResult().getType() == Material.GOLD_SWORD
    			|| event.getRecipe().getResult().getType() == Material.DIAMOND_SWORD
				|| event.getRecipe().getResult().getType() == Material.LEATHER_BOOTS
    			|| event.getRecipe().getResult().getType() == Material.LEATHER_LEGGINGS
    			|| event.getRecipe().getResult().getType() == Material.LEATHER_CHESTPLATE
    			|| event.getRecipe().getResult().getType() == Material.LEATHER_HELMET
				|| event.getRecipe().getResult().getType() == Material.CHAINMAIL_BOOTS
    			|| event.getRecipe().getResult().getType() == Material.CHAINMAIL_LEGGINGS
    			|| event.getRecipe().getResult().getType() == Material.CHAINMAIL_CHESTPLATE
    			|| event.getRecipe().getResult().getType() == Material.CHAINMAIL_HELMET
				|| event.getRecipe().getResult().getType() == Material.GOLD_BOOTS
    			|| event.getRecipe().getResult().getType() == Material.GOLD_LEGGINGS
    			|| event.getRecipe().getResult().getType() == Material.GOLD_CHESTPLATE
    			|| event.getRecipe().getResult().getType() == Material.GOLD_HELMET
    			|| event.getRecipe().getResult().getType() == Material.IRON_BOOTS
    			|| event.getRecipe().getResult().getType() == Material.IRON_LEGGINGS
    			|| event.getRecipe().getResult().getType() == Material.IRON_CHESTPLATE
    			|| event.getRecipe().getResult().getType() == Material.IRON_HELMET
    			|| event.getRecipe().getResult().getType() == Material.DIAMOND_BOOTS
    			|| event.getRecipe().getResult().getType() == Material.DIAMOND_LEGGINGS
    			|| event.getRecipe().getResult().getType() == Material.DIAMOND_CHESTPLATE
    			|| event.getRecipe().getResult().getType() == Material.DIAMOND_HELMET)
    	{
    		if( event.getWhoClicked().getType() == EntityType.PLAYER )
    		{
    			Player p = (Player)event.getWhoClicked();
    			p.sendMessage("Sorry, you are not allowed to craft this item. Purchase it from a Safe Dock instead.");
    		}
    		event.setCancelled(false);
    	}
 
    }
    
}

