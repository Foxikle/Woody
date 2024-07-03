package me.flame.menus.menu;


import org.bukkit.event.inventory.InventoryType;

public interface OpenedType {
    int maxColumns();

    int maxRows();

    int maxSize();

    InventoryType inventoryType();
}
