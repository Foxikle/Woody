package me.flame.menus.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import me.flame.menus.items.ItemResponse;
import me.flame.menus.util.Option;
import me.flame.menus.menu.contents.BukkitContents;
import me.flame.menus.menu.contents.Contents;
import me.flame.menus.items.MenuItem;
import me.flame.menus.modifiers.Modifier;
import me.flame.menus.menu.opener.MenuOpener;
import me.flame.menus.menu.pagination.IndexedPagination;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/**
 * Most commonly used normal Menu
 * @since 1.0.0
 */
@SuppressWarnings({ "unused", "UnusedReturnValue" })
public final class MenuImpl extends AbstractMenu implements
        Menu, RandomAccess, java.io.Serializable, InventoryHolder, BukkitContents {
    public MenuImpl(int rows, @NotNull Component title, @NotNull EnumSet<Modifier> modifiers, Menus manager, MenuOpener opener) {
        super(rows * 9, rows, title, modifiers, opener, manager);
        this.inventory = opener.open(manager, this, this.structure());
        this.contents = new Contents(this);
    }

    public MenuImpl(@NotNull OpenedType type, @NotNull Component title, @NotNull EnumSet<Modifier> modifiers, Menus manager, MenuOpener opener) {
        super(type, title, modifiers, opener, manager);
        this.inventory = opener.open(manager, this, this.structure());
        this.contents = new Contents(this);
    }

    public MenuImpl(int rows, @NotNull Component title, @NotNull EnumSet<Modifier> modifiers, Menus manager) {
        this(rows, title, modifiers, manager, MenuOpener.DEFAULT);
    }

    public MenuImpl(@NotNull OpenedType type, @NotNull Component title, @NotNull EnumSet<Modifier> modifiers, Menus manager) {
        this(type, title, modifiers, manager, MenuOpener.DEFAULT);
    }

    @Override
    public int itemCount() {
        return contents.itemCount();
    }

    @Override
    public @Unmodifiable List<ItemResponse> getSlotActions() {
        return List.of(getMutableSlotActions());
    }

    @Override
    public boolean canResize() {
        return dynamicSizing && rows < 6 && type.inventoryType() == InventoryType.CHEST;
    }

    ItemResponse[] getMutableSlotActions() {
        return (slotActions == null) ? (slotActions = new ItemResponse[rows * 9]) : slotActions;
    }

    @Override
    public boolean hasSlotActions() {
        return slotActions != null;
    }

    @Override
    public void forEach(Consumer<? super MenuItem> action) {
        this.contents.forEach(action);
    }

    @Override
    public void indexed(final ObjIntConsumer<? super MenuItem> action) {
        contents.indexed(action);
    }

    public @NotNull IndexedPagination pagination(int pages, MenuItem nextItem, MenuItem previousItem, int nextItemSlot, int previousItemSlot) {
        var pagination = new PaginatedMenuImpl(rows, pages, title, modifiers, nextItem, previousItem, nextItemSlot, previousItemSlot, manager);
        pagination.replaceContents(contents);
        return pagination;
    }

    @Override
    public void updatePer(long delay, long repeatTime) {
        if (repeatTime > 0) Bukkit.getScheduler().runTaskTimer(manager.getPlugin(), this::update, delay, repeatTime);
        throw new IllegalThreadStateException("Synchronously calling an update method that has no delay or repeat delay. \nGoal: This prevents blocking because the delay is too low.");
    }

    @Override
    public List<HumanEntity> getViewers() {
        return inventory.getViewers();
    }

    @Override
    public void replaceContents(final BukkitContents items) { this.contents = items; }

    @Override
    public void setItem(int slot, MenuItem item) {
        this.contents.setItem(slot, item);
    }

    @Override
    public Optional<MenuItem> getItem(int i) {
        return this.contents.getItem(i);
    }

    @Override
    public boolean hasItem(int slot) {
        return this.contents.hasItem(slot);
    }

    @Override
    public void recreateItems(final Inventory inventory) {
        contents.recreateItems(inventory);
    }

    @Override
    public Optional<MenuItem> findFirst(Predicate<MenuItem> itemDescription) {
        return this.contents.findFirst(itemDescription);
    }

    @Override
    public void setSlotAction(int slot, ItemResponse response) {
        this.getMutableSlotActions()[slot] = response;
    }

    @Override
    public boolean removeItem(@NotNull final MenuItem... items) {
        return this.contents.removeItem(items);
    }

    @Override
    public boolean isConcurrent() {
        return true;
    }

    @Override
    public boolean removeItem(@NotNull final List<MenuItem> itemStacks) {
        return removeItem(itemStacks.toArray(new MenuItem[0]));
    }

    @Override
    public Menu getMenu() { return this; }

    @Override
    public Set<Map.Entry<Integer, MenuItem>> getEntries() {
        return contents.getEntries();
    }

    public @Contract("_ -> new") @CanIgnoreReturnValue @Override MenuItem removeItem(int index) {
        return this.contents.removeItem(index);
    }

    @Override
    public void update() {
        updating = true;
        updatePlayerInventories(this.inventory, getViewers(), contents, (player) -> ((Player) player).updateInventory());
        updating = false;
    }

    @Override
    public void updateTitle(Component title) {
        Inventory oldInventory = inventory;
        this.inventory = opener.open(manager, this, this.structure());
        updating = true;
        updatePlayerInventories(oldInventory, getViewers(), contents, player -> player.openInventory(this.inventory));
        updating = false;
    }

    static void updatePlayerInventories(Inventory inventory, @NotNull List<HumanEntity> viewers, @NotNull BukkitContents contents, Consumer<HumanEntity> entityPredicate) {
        contents.recreateItems(inventory);
        List<HumanEntity> snapshot = new ArrayList<>(viewers);

        // debug remove
        Bukkit.getLogger().info("Snapshot size: " + snapshot);

        if (!snapshot.isEmpty()) snapshot.forEach((e) -> {
            Bukkit.getLogger().info("Name: " + e.getName());
            entityPredicate.accept(e);
        });
    }

    @Override
    public void open(@NotNull HumanEntity entity) {
        if (!entity.isSleeping()) Bukkit.getScheduler().runTask(manager.getPlugin(), () -> {
            this.update();
            entity.openInventory(inventory);
        });
    }

    @Override
    public void close(@NotNull final HumanEntity player) {
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> player.closeInventory(), 1L);
    }

    @Override
    public Map<Integer, MenuItem> getItems() {
        return this.contents.getItems();
    }

    @Override
    public Map<Integer, MenuItem> getMutableItems() {
        return this.contents.getMutableItems();
    }

    @Override
    public void refreshItem(final int index) {
        this.contents.refreshItem(index);
    }

    @Override
    public void refreshItem(final int index, final Supplier<MenuItem> item) {
        this.contents.refreshItem(index, item);
    }

    @Override
    public int firstEmptySlot(final int startingPoint) {
        return this.contents.firstEmptySlot(startingPoint);
    }

    @Override
    public void addRefreshableItem(final int index, final Supplier<MenuItem> item) {
        this.contents.addRefreshableItem(index, item);
    }

    @Override
    public void removeRefreshableItem(final int index) {
        this.contents.removeRefreshableItem(index);
    }

    @Override
    public int addItem(final int fromIndex, final @NotNull MenuItem... items) {
        return contents.addItem(fromIndex, items);
    }

    @Override
    public int addItem(final int fromIndex, final int toIndex, final @NotNull MenuItem... items) {
        return contents.addItem(fromIndex, toIndex, items);
    }

    @Override
    public void clear() {
        this.contents.clear();
    }

    @Override
    public int columns() {
        return type.maxColumns();
    }

    @Override
    public boolean allModifiersAdded() { return modifiers.size() == Modifier.ALL.size(); }

    @Override
    public void recreateInventory(boolean shouldRecreate) {
        if (rows >= type.maxRows()) return;
        rows++;
        this.size = rows * type.maxColumns();
        this.inventory = opener.open(manager, this, this.structure());
    }

    @Override
    public void recreateInventory(int growRows) {
        if (rows >= type.maxRows()) return;
        rows += growRows;
        this.size = rows * type.maxColumns();
        this.inventory = opener.open(manager, this, this.structure());
    }

    public void recreateInventory() { recreateInventory(true); }

    @Override
    public void replaceContents(final MenuItem... items) {
        this.contents.replaceContents(items);
    }

    public @NotNull Menu copy() {
        MenuImpl menu = type.inventoryType() == InventoryType.CHEST ? new MenuImpl(rows, title, modifiers, manager, opener) : new MenuImpl(type, title, modifiers, manager, opener);
        menu.setDynamicSizing(dynamicSizing);
        menu.replaceContents(contents);
        menu.actions = actions;
        menu.slotActions = slotActions;
        return menu;
    }
}