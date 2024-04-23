package me.flame.menus.adventure;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A {@link CompHolder} implementation for platforms where Adventure is natively supported.
 * Adventure components are directly passed to the Bukkit (Paper) API.
 *
 * @see ForeignCompHolder
 * @since 0.10.0
 * @author stefvanschie at <a href="https://github.com/stefvanschie/IF">IF's github.</a>
 */
class NativeCompHolder extends CompHolder {
    
    /**
     * Creates and initializes a new instance.
     *
     * @param value the Adventure component this instance should wrap
     * @since 0.10.0
     */
    NativeCompHolder(@NotNull Component value) { super(value); }

    @Override
    public boolean isComponent() {
        return false;
    }
    
    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory toInventory(InventoryHolder holder, InventoryType type) {
        return Bukkit.createInventory(holder, type, value);
    }
    
    @NotNull
    @Contract(pure = true)
    @Override
    public Inventory toInventory(InventoryHolder holder, int size) {
        return Bukkit.createInventory(holder, size, value);
    }

    @Override
    public void asItemDisplayName(ItemMeta meta) {
        meta.displayName(value);
    }
    
    @Override
    public void asItemLoreAtEnd(ItemMeta meta) {
        List<Component> lore = meta.hasLore()
                ? Objects.requireNonNull(meta.lore())
                : new ArrayList<>();
        lore.add(value);
        meta.lore(lore);
    }

    @Override
    public void asItemLore(@NotNull ItemMeta meta) {
        List<Component> lore = new ArrayList<>();
        lore.add(value);
        meta.lore(lore);
    }

    @Override
    public boolean contains(@NotNull TextHolder string) {
        if (string instanceof CompHolder) {
            return this.value.contains(((CompHolder) string).component());
        }
        return this.value.toString().contains(string.toString());
    }

    @Override
    public Component asComponent() {
        return component();
    }
}
