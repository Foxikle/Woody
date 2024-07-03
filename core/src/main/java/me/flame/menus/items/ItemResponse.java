package me.flame.menus.items;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface ItemResponse {
    void execute(Player player, InventoryClickEvent event);
}
