package me.flame.menus.builders.menus;

import me.flame.menus.menu.KeyedMenuImpl;
import me.flame.menus.menu.Menu;
import me.flame.menus.menu.MenuImpl;

import me.flame.menus.menu.Menus;
import me.flame.menus.menu.pagination.Pagination;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Universal menu builder for menus (Menu, PaginatedMenu).
 * @since 2.0.0
 * @author FlameyosFlow
 */
@SuppressWarnings("unused")
public class MenuBuilder extends BaseMenuBuilder<MenuBuilder> {
    public MenuBuilder(final @NotNull Menus menus, int rows) {
        super(menus, rows);
    }

    public MenuBuilder(final @NotNull Menus menus) {
        super(menus);
    }

    @NotNull
    @Contract(" -> new")
    public Menu normal() {
        checkRequirements(rows, title);
        return type.inventoryType() == InventoryType.CHEST ? new MenuImpl(rows, title, modifiers, menus) : new MenuImpl(type, title, modifiers, menus);
    }

    @NotNull
    @Contract("_ -> new")
    public Pagination<String> keyed(int pageCount) {
        checkRequirements(rows, title);
        return type.inventoryType() == InventoryType.CHEST
                ? new KeyedMenuImpl(rows, pageCount, title, modifiers, menus)
                : new KeyedMenuImpl(type, pageCount, title, modifiers, menus);
    }
}
