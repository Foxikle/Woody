package me.flame.menus.menu;

import me.flame.menus.items.ItemResponse;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.actions.Actions;
import me.flame.menus.menu.animation.Animation;
import me.flame.menus.menu.contents.BukkitContents;
import me.flame.menus.menu.opener.MenuOpener;
import me.flame.menus.modifiers.Modifier;

import net.kyori.adventure.text.Component;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractMenu implements Menu {
    @NotNull
    protected EnumSet<Modifier> modifiers;

    protected Component title;

    protected boolean dynamicSizing = false, updating = false;

    protected boolean animating = false;
    protected int rows, size;

    @Override
    public Structure structure() { return type.inventoryType() == InventoryType.CHEST ? Structure.of(size) : Structure.of(type); }

    protected MenuOpener opener;

    protected final Menus manager;

    protected OpenedType type;
    protected Inventory inventory;
    protected BukkitContents contents;

    protected final List<Animation> activeAnimations = new ArrayList<>(2);

    protected ItemResponse[] slotActions;

    Actions actions = new Actions(this);

    protected AbstractMenu(int size, int rows, Component title, @NotNull EnumSet<Modifier> modifiers, MenuOpener opener, Menus manager) {
        this.manager = manager;
        this.modifiers = modifiers;
        this.size = size;
        this.rows = rows;
        this.type = MenuType.CHEST;
        this.title = title;
        this.opener = opener;
    }

    protected AbstractMenu(@NotNull OpenedType type, Component title, @NotNull EnumSet<Modifier> modifiers, MenuOpener opener, Menus manager) {
        if (type.inventoryType() == InventoryType.CHEST) {
            throw new IllegalArgumentException("Not allowed, use the size/rows constructor. CHEST InventoryType detected.");
        }
        this.manager = manager;
        this.modifiers = modifiers;
        this.size = type.maxSize();
        this.rows = type.maxRows();
        this.type = type;
        this.title = title;
        this.opener = opener;
    }

    @Override
    public Stream<MenuItem> stream() {
        return contents.stream();
    }

    @Override
    public boolean addModifier(Modifier modifier) {
        return modifiers.add(modifier);
    }

    @Override
    public boolean removeModifier(Modifier modifier) {
        return modifiers.remove(modifier);
    }

    @Override
    public boolean addAllModifiers() {
        return modifiers.addAll(Modifier.ALL);
    }

    @Override
    public void removeAllModifiers() {
        Modifier.ALL.forEach(modifiers::remove);
    }

    @Override
    public boolean areItemsPlaceable() {
        return modifiers.contains(Modifier.DISABLE_ITEM_ADD);
    }

    @Override
    public boolean areItemsRemovable() {
        return modifiers.contains(Modifier.DISABLE_ITEM_REMOVAL);
    }

    @Override
    public boolean areItemsSwappable() {
        return modifiers.contains(Modifier.DISABLE_ITEM_SWAP);
    }

    @Override
    public boolean areItemsCloneable() {
        return modifiers.contains(Modifier.DISABLE_ITEM_CLONE);
    }

    @Override
    public int rows() {
        return rows;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean allModifiersAdded() {
        return modifiers.size() == 4;
    }

    @Override
    public Component title() {
        return title;
    }

    @Override
    public MenuOpener opener() {
        return opener;
    }

    @Override
    public Menus manager() {
        return manager;
    }

    @Override
    public Actions actions() {
        return actions;
    }

    @Override
    public BukkitContents contents() {
        return contents;
    }

    @Override
    public @NotNull EnumSet<Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public boolean isDynamicSizing() {
        return dynamicSizing;
    }

    @Override
    public boolean isUpdating() {
        return updating;
    }

    @Override
    public boolean isAnimating() {
        return animating;
    }

    @Override
    public OpenedType getType() {
        return type;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public List<Animation> getActiveAnimations() {
        return activeAnimations;
    }

    @Override
    public void setDynamicSizing(final boolean dynamicSizing) {
        this.dynamicSizing = dynamicSizing;
    }

    @Override
    public void setAnimating(final boolean animating) {
        this.animating = animating;
    }

    public void setUpdating(final boolean updating) {
        this.updating = updating;
    }

    public void setOpener(final MenuOpener opener) {
        this.opener = opener;
    }

    public void setInventory(final Inventory inventory) {
        this.inventory = inventory;
    }
}
