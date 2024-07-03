package me.flame.menus.menu;

import me.flame.menus.items.ItemResponse;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.contents.BukkitContents;
import me.flame.menus.menu.opener.MenuOpener;
import me.flame.menus.modifiers.Modifier;
import me.flame.menus.menu.contents.Contents;
import me.flame.menus.menu.pagination.Pagination;

import me.flame.menus.util.Option;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static me.flame.menus.menu.MenuImpl.updatePlayerInventories;

@SuppressWarnings("unused")
public class KeyedMenuImpl extends AbstractMenu implements
        Pagination<String>, Menu, RandomAccess, java.io.Serializable, InventoryHolder, BukkitContents {
    @NotNull
    private final Map<String, BukkitContents> pages;

    @NotNull
    private final Map<Integer, MenuItem> pageItems;

    private String pageIdentifier;

    /**
     * Adds a blank page to the menu.
     * @return the index the page was added at
     */
    public int addPage(String identifier) {
        Contents page = new Contents(this);
        pages.put(identifier, page);
        setPageItems(page);
        return pages.size() - 1;
    }

    /**
     * Main constructor to provide a way to create KeyedMenu
     *
     * @param pageRows The page size.
     */
    public KeyedMenuImpl(final int pageRows, final int pageCount, Component title, EnumSet<Modifier> modifiers, MenuOpener opener, Menus menus) {
        super(pageRows * 9, pageRows, title, modifiers, opener, menus);
        this.inventory = opener.open(manager, this, this.structure());
        this.pageItems = new LinkedHashMap<>(rows * 9);
        this.pages = new ConcurrentHashMap<>(pageCount);
        setPageItems(contents);
    }

    /**
     * Main constructor to provide a way to create KeyedMenu
     */
    public KeyedMenuImpl(OpenedType type, final int pageCount, Component title, EnumSet<Modifier> modifiers, MenuOpener opener, Menus menus) {
        super(type, title, modifiers, opener, menus);
        this.inventory = opener.open(manager, this, this.structure());
        this.pageItems = new LinkedHashMap<>(rows * 9);
        this.pages = new ConcurrentHashMap<>(pageCount);
        setPageItems(contents);
    }

    /**
     * Main constructor to provide a way to create KeyedMenu
     *
     * @param pageRows The page size.
     */
    public KeyedMenuImpl(final int pageRows, final int pageCount, Component title, EnumSet<Modifier> modifiers, Menus menus) {
        this(pageRows, pageCount, title, modifiers, MenuOpener.DEFAULT, menus);
    }

    /**
     * Main constructor to provide a way to create KeyedMenu
     */
    public KeyedMenuImpl(OpenedType type, final int pageCount, Component title, EnumSet<Modifier> modifiers, Menus menus) {
        this(type, pageCount, title, modifiers, MenuOpener.DEFAULT, menus);
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
    public int itemCount() {
        return contents.itemCount();
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

    public List<BukkitContents> pages() { return List.copyOf(pages.values()); }

    @Override
    public void setPage(final String index, final BukkitContents page) {
        Objects.requireNonNull(index);
        Objects.requireNonNull(page);
        this.pages.put(index, page);
        setPageItems(page);
    }

    @Override
    public void updatePer(final long delay, final long repeatTime) {
        if (repeatTime > 0) Bukkit.getScheduler().runTaskTimer(manager.getPlugin(), this::update, delay, repeatTime);
        throw new IllegalThreadStateException("Synchronously calling an update method that has no delay or repeat delay. \nGoal: This prevents blocking because the delay is too low.");
    }

    @Override
    public List<HumanEntity> getViewers() {
        return inventory.getViewers();
    }

    @Override
    public void update() {
        updating = true;
        updatePlayerInventories(inventory, getViewers(), contents, player -> player.openInventory(this.inventory));
        updating = false;
    }

    @Override
    public void recreateInventory(boolean shouldRecreate) {
        if (rows >= type.maxRows()) return;
        rows++;
        this.size = rows * type.maxColumns();
        this.inventory = opener.open(manager, this, this.structure());
    }

    public void recreateInventory() {
        recreateInventory(true);
    }

    @Override
    public void updateTitle(final Component title) {
        Inventory oldInventory = inventory;
        this.inventory = opener.open(manager, this, this.structure());
        updating = true;
        updatePlayerInventories(oldInventory, getViewers(), contents, player -> player.openInventory(this.inventory));
        updating = false;
    }

    @Override
    public void open(@NotNull final HumanEntity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        contents.clear();
    }

    @Override
    public void recreateItems(final Inventory inventory) {
        contents.recreateItems(inventory);
    }

    @Override
    public void replaceContents(final BukkitContents items) {
        this.contents = items;
    }

    @Override
    public Menu getMenu() {
        return this;
    }

    @Override
    public Set<Map.Entry<Integer, MenuItem>> getEntries() {
        return contents.getEntries();
    }

    @Override
    public int columns() {
        return type.maxColumns();
    }

    @Override
    public void replaceContents(final MenuItem... items) {
        contents.replaceContents(items);
    }

    @Override
    public void setItem(final int slot, final MenuItem item) {
        contents.setItem(slot, item);
    }

    @Override
    public Optional<MenuItem> getItem(final int index) {
        return contents.getItem(index);
    }

    @Override
    public void forEach(final Consumer<? super MenuItem> action) {
        contents.forEach(action);
    }

    @Override
    public void indexed(final ObjIntConsumer<? super MenuItem> action) {
        contents.indexed(action);
    }

    @Override
    public Optional<MenuItem> findFirst(final Predicate<MenuItem> action) {
        return contents.findFirst(action);
    }

    @Override
    public MenuItem removeItem(final int index) {
        return contents.removeItem(index);
    }

    @Override
    public boolean removeItem(final MenuItem... its) {
        return contents.removeItem(its);
    }

    @Override
    public boolean isConcurrent() {
        return contents.isConcurrent();
    }

    @Override
    public Map<Integer, MenuItem> getItems() {
        return contents.getItems();
    }

    @Override
    public Map<Integer, MenuItem> getMutableItems() {
        return contents.getMutableItems();
    }

    @Override
    public void close(final @NotNull HumanEntity player) {
        Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> player.closeInventory(), 1L);
    }

    @Override
    public boolean canResize() {
        return dynamicSizing && rows < 6 && type.inventoryType() == InventoryType.CHEST;
    }

    @Override
    public final List<ItemResponse> getSlotActions() {
        return List.of(getMutableSlotActions());
    }

    ItemResponse[] getMutableSlotActions() {
        return (slotActions == null) ? (slotActions = new ItemResponse[rows * 9]) : slotActions;
    }

    @Override
    public boolean hasSlotActions() {
        return slotActions != null;
    }

    @Override
    public void setSlotAction(final int slot, final ItemResponse response) {
        this.getMutableSlotActions()[slot] = response;
    }

    public void open(@NotNull final HumanEntity entity, final String openPage) {
        if (entity.isSleeping()) return;
        Objects.requireNonNull(inventory, "Inventory was provided null by the menu opener: " + this.opener.getClass().getSimpleName() + " at: " + this.opener.getClass().getPackageName());

        Bukkit.getScheduler().runTask(manager.getPlugin(), () -> {
            this.pageIdentifier = openPage;
            this.contents = pages.get(openPage);
            this.update();
            entity.openInventory(inventory);
        });
    }

    public int getPagesSize() {
        return pages.size();
    }

    @Override
    public void recreateInventory(int growRows) {
        if (rows >= type.maxRows()) return;
        rows += growRows;
        this.size = rows * type.maxColumns();
        this.inventory = opener.open(manager, this, this.structure());
    }

    /**
     * Goes to the specified page
     *
     * @return False if there is no next page.
     */
    public boolean page(String page) {
        BukkitContents pageData = pages.get(page);
        if (pageData == null) return false;
        this.pageIdentifier = page;
        this.contents = pageData;
        update();
        return true;
    }

    public BukkitContents getPage(String index) {
        return pages.get(index);
    }

    @Override
    public Class<String> indexClass() {
        return String.class;
    }

    @Override
    public Map<Integer, MenuItem> pageItems() { return pageItems; }

    public Pagination<String> copy() {
        var menu = type.inventoryType() == InventoryType.CHEST ? new KeyedMenuImpl(rows, pages.size(), title, modifiers, manager) : new KeyedMenuImpl(type, pages.size(), title, modifiers, manager);
        menu.setDynamicSizing(dynamicSizing);
        menu.replaceContents(this.contents);
        menu.actions = actions;
        menu.slotActions = slotActions;
        for (Map.Entry<String, BukkitContents> entry : pages.entrySet()) {
            BukkitContents page = menu.getPage(entry.getKey());
            page.replaceContents(entry.getValue());
        }
        return menu;
    }

    @Override
    public String getPageIdentifier() {
        return pageIdentifier;
    }
}