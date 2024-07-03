package me.flame.menus.menu;

import org.bukkit.event.inventory.InventoryType;

import java.io.Serializable;

@SuppressWarnings("unused")
public enum MenuType implements Serializable, OpenedType {
    CHEST(InventoryType.CHEST, 54, 6, 9),
    FURNACE(InventoryType.FURNACE, 3, 3, 1),
    WORKBENCH(InventoryType.WORKBENCH, 10, 3, 3),
    HOPPER(InventoryType.HOPPER, 5, 1, 5),
    ANVIL(InventoryType.ANVIL, 3, 1, 1),
    DISPENSER(InventoryType.DISPENSER, 9, 3, 3),
    BREWING(InventoryType.BREWING, 4, 1, 1);

    private final InventoryType inventoryType;
    private final int maxSize, maxRows, maxColumns;

    MenuType(final InventoryType inventoryType, final int maxSize, final int maxRows, final int maxColumns) {
        this.inventoryType = inventoryType;
        this.maxSize = maxSize;
        this.maxRows = maxRows;
        this.maxColumns = maxColumns;
    }

    @Override
    public InventoryType inventoryType() {
        return inventoryType;
    }

    @Override
    public int maxSize() {
        return maxSize;
    }

    @Override
    public int maxRows() {
        return maxRows;
    }

    @Override
    public int maxColumns() {
        return maxColumns;
    }
}
