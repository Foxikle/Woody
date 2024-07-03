package me.flame.menus.menu.opener;

import me.flame.menus.menu.Menu;
import me.flame.menus.menu.Structure;
import me.flame.menus.menu.Menus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public interface MenuOpener {
    MenuOpener DEFAULT = (manager, menu, structure) -> {
        InventoryType type = structure.type().inventoryType();
        return type == InventoryType.CHEST
                ? Bukkit.createInventory(menu, menu.size(), menu.title())
                : Bukkit.createInventory(menu, type, menu.title());
    };


    Inventory open(Menus manager, Menu menu, Structure structure);
}
