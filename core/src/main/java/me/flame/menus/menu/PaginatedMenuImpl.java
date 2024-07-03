package me.flame.menus.menu;

import me.flame.lotte.LinkedConcurrentCache;

import me.flame.menus.items.ItemResponse;
import me.flame.menus.menu.contents.BukkitContents;
import me.flame.menus.events.PageChangeEvent;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.contents.Contents;
import me.flame.menus.menu.opener.MenuOpener;
import me.flame.menus.modifiers.Modifier;

import me.flame.menus.menu.pagination.IndexedPagination;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static me.flame.menus.menu.MenuImpl.updatePlayerInventories;

/**
 * Menu that allows you to have multiple pages
 * @since 2.0.0
 * @author FlameyosFlow
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class PaginatedMenuImpl extends AbstractMenu implements
        IndexedPagination, Menu, RandomAccess, java.io.Serializable, InventoryHolder, BukkitContents {
    @NotNull
    final Map<Integer, BukkitContents> pages;
    private Integer pageIdentifier = 0;
    private int nextItemSlot = -1;

    @Override
    public Integer getPageIdentifier() {
        return pageIdentifier;
    }

    private int previousItemSlot = -1;

    private final Map<Integer, MenuItem> pageItems;
    private MenuItem nextItem, previousItem;

    /**
     * Adds a blank page to the menu.
     * @return the index the page was added at
     */
    public int addPage(BukkitContents data) {
        Objects.requireNonNull(data);
        this.pages.put(pages.size(), data);
        setPageItems(data);
        return pages.size();
    }

    public void setPageItems(BukkitContents items) {
        Objects.requireNonNull(items);
        if (pageItems.isEmpty()) return;
        for (Map.Entry<Integer, MenuItem> entry : this.pageItems.entrySet()) items.setItem(entry.getKey(), entry.getValue());
    }

    /**
     * Main constructor to provide a way to create PaginatedMenu
     *
     * @param rows The page size.
     */
    public PaginatedMenuImpl(final int rows, final int pages, Component title, EnumSet<Modifier> modifiers, MenuItem nextItem, MenuItem previousItem, int nextItemSlot, int previousItemSlot, MenuOpener opener, Menus menus) {
        super(rows * 9, rows, title, modifiers, opener, menus);
        this.inventory = opener.open(manager, this, this.structure());
        this.pageItems = new LinkedConcurrentCache<>(this.rows * 9);
        this.pages = new LinkedConcurrentCache<>(pages);
        paginationInitialization(MenuType.CHEST, pages, nextItem, previousItem, nextItemSlot, previousItemSlot);
    }

    /**
     * Main constructor to provide a way to create PaginatedMenu
     */
    public PaginatedMenuImpl(OpenedType type, final int pages, Component title, EnumSet<Modifier> modifiers, MenuItem nextItem, MenuItem previousItem, int nextItemSlot, int previousItemSlot, MenuOpener opener, Menus menus) {
        super(type, title, modifiers, opener, menus);
        this.inventory = opener.open(manager, this, this.structure());
        this.pageItems = new LinkedConcurrentCache<>(type.maxSize());
        this.pages = new LinkedConcurrentCache<>(pages);
        paginationInitialization(type, pages, nextItem, previousItem, nextItemSlot, previousItemSlot);
    }

    /**
     * Main constructor to provide a way to create PaginatedMenu
     *
     * @param rows The page size.
     */
    public PaginatedMenuImpl(final int rows, final int pages, Component title, EnumSet<Modifier> modifiers, MenuItem nextItem, MenuItem previousItem, int nextItemSlot, int previousItemSlot, Menus menus) {
        this(rows, pages, title, modifiers, nextItem, previousItem, nextItemSlot, previousItemSlot, MenuOpener.DEFAULT, menus);
    }

    /**
     * Main constructor to provide a way to create PaginatedMenu
     */
    public PaginatedMenuImpl(OpenedType type, final int pages, Component title, EnumSet<Modifier> modifiers, MenuItem nextItem, MenuItem previousItem, int nextItemSlot, int previousItemSlot, Menus menus) {
        this(type, pages, title, modifiers, nextItem, previousItem, nextItemSlot, previousItemSlot, MenuOpener.DEFAULT, menus);
    }

    private void paginationInitialization(OpenedType type, int pageCount, MenuItem nextItem, MenuItem previousItem, int nextItemSlot, int previousItemSlot) {
        Contents items = new Contents(this);
        this.addPage(items);
        this.contents = items;
        this.setNextPageItem(nextItemSlot, nextItem);
        this.setPreviousPageItem(previousItemSlot, previousItem);
        for (int pageIndex = 1; pageIndex < pageCount; pageIndex++) this.addPage(new Contents(this));
    }

    public List<BukkitContents> pages() { return List.copyOf(pages.values()); }

    @Override
    public void setPage(final Integer index, final BukkitContents page) {
        this.pages.put(index, page);
        setPageItems(page);
    }

    public void setNextPageItem(int nextItemSlot, MenuItem nextItem) {
        this.nextItemSlot = nextItemSlot;
        this.nextItem = nextItem;
        this.pageItems.put(nextItemSlot, nextItem);
        this.setItem(nextItemSlot, nextItem);
        nextItem.setClickAction((player, event) -> this.next());
        //nextItem.setVisiblity(menu -> menu instanceof PaginatedMenuImpl && ((PaginatedMenuImpl) menu).getCurrentPageNumber() != pages.size());
    }

    public void setPreviousPageItem(int previousItemSlot, MenuItem previousItem) {
        this.previousItemSlot = previousItemSlot;
        this.previousItem = previousItem;
        this.pageItems.put(previousItemSlot, previousItem);
        this.setItem(previousItemSlot, previousItem);
        previousItem.setClickAction((player, event) -> this.previous());
        //previousItem.setVisiblity(menu -> menu instanceof PaginatedMenuImpl && ((PaginatedMenuImpl) menu).getCurrentPageNumber() != 0);
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
        if (this.rows >= type.maxRows()) return;
        this.rows++;
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

    public void open(@NotNull final HumanEntity entity, final @Range(from = 0, to = Integer.MAX_VALUE) Integer openPage) {
        if (entity.isSleeping()) return;
        if (openPage < 0 || openPage >= pages.size()) throw new IllegalArgumentException("\"openPage\" out of bounds; must be 0-" + (pages.size() - 1) + "\nopenPage: " + openPage + "\nFix: Make sure \"openPage\" is 0-" + (pages.size() - 1));
        Objects.requireNonNull(inventory, "Inventory was provided null by the menu opener: " + this.opener.getClass().getSimpleName() + " at: " + this.opener.getClass().getPackageName());

        Bukkit.getScheduler().runTask(manager.getPlugin(), () -> {
            this.updatePage(openPage);
            entity.openInventory(inventory);
        });
    }

    public void open(@NotNull final HumanEntity player) { this.open(player, 0); }

    @Override
    public void recreateInventory(int growRows) {
        if (rows >= type.maxRows()) return;
        rows += growRows;
        this.size = rows * type.maxColumns();
        this.inventory = opener.open(manager, this, this.structure());
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

    /**
     * Gets the current page number (Inflated by 1)
     * @return The current page number
     */
    public int getCurrentPageNumber() { return pageIdentifier + 1; }

    @Override
    public boolean isLastPage() { return pageIdentifier == (pages.size() - 1); }

    @Override
    public boolean isFirstPage() { return pageIdentifier == 0; }

    @Override
    public int getPagesSize() { return pages.size(); }

    @Override
    public boolean pageChangingAction(final int clickedSlot) {
        return (clickedSlot == this.nextItemSlot) || (clickedSlot == this.previousItemSlot);
    }

    @Override
    public @Nullable PageChangeEvent createPageEvent(final InventoryClickEvent event) {
        int newNumber = pageIdentifier + (nextItemSlot == event.getSlot() ? 1 : -1);
        if (newNumber < 0 || newNumber >= pages.size()) return null;
        BukkitContents currentPage = getPage(newNumber);
        return new PageChangeEvent(this, this.contents, currentPage, (Player) event.getWhoClicked(), pageIdentifier, newNumber);
    }

    @Override
    public boolean next() { return page(pageIdentifier + 1); }

    @Override
    public boolean previous() { return page(pageIdentifier - 1); }

    @Override
    public boolean page(Integer pageNum) {
        if (pageNum < 0 || pageNum >= pages.size()) return false;

        Bukkit.getScheduler().runTask(manager.getPlugin(), () -> {
            Bukkit.getLogger().info("Old page: " + this.pageIdentifier);
            updatePage(pageNum);
            Bukkit.getLogger().info("New page: " + this.pageIdentifier);
        });
        return true;
    }

    private void updatePage(final Integer pageNum) {
        this.pageIdentifier = pageNum;
        this.contents = pages.get(pageNum);
        update();
    }

    @Nullable
    public @Override BukkitContents getPage(Integer index) { return this.pages.get(index); }

    @Override
    public Class<Integer> indexClass() { return Integer.class; }

    public @Override Map<Integer, MenuItem> pageItems() { return pageItems; }

    public void addItems(@NotNull MenuItem... items) {
       /* if (items == null || items.length == 0) return;
        int currentIndex = 0;
        int length = items.length;

        // Iterate through existing pages
        final int paginationSize = this.pages.size();
        for (int pageIndex = this.pageIdentifier; pageIndex < paginationSize && currentIndex < length; pageIndex++) {
            int remainingItems = length - currentIndex;

            final int index = currentIndex;
            int itemsAdded = Option.some(this.getPage(pageIndex))
                    .map(page -> page.addItem(index, remainingItems, items))
                    .orElse(0);
            currentIndex += itemsAdded;
        }

        // Handle dynamic page creation if needed
        int newestPageNumber = this.pageIdentifier;
        if (!dynamicSizing) {
            page(newestPageNumber);
            return;
        }

        while (currentIndex < length) {
            int newPageNum = addPage(new Contents(this));
            newestPageNumber = newPageNum;

            int remainingItems = length - currentIndex;
            final int index = currentIndex;
            int itemsAdded = Option.some(this.getPage(newPageNum))
                    .map(page -> page.addItem(index, remainingItems, items))
                    .orElse(0);
            currentIndex += itemsAdded;
        }
        page(newestPageNumber);*/
        if (items == null || items.length == 0) return;

        List<MenuItem> leftovers = new ArrayList<>();
        for (int i = pageIdentifier; i < pages.size(); i++) {
            BukkitContents page = this.getPage(i);
            if (page == null) continue;

            page.addItem(leftovers, items);
            items = leftovers.toArray(new MenuItem[0]);
            leftovers.clear();
        }
        if (!dynamicSizing) return;

        int newestPageNumber = pageIdentifier;
        while (items.length != 0) {
            BukkitContents newPage = new Contents(this);
            newestPageNumber = addPage(newPage);

            newPage.addItem(leftovers, items);
            items = leftovers.toArray(new MenuItem[0]);
            leftovers.clear();
        }

        page(newestPageNumber);
    }

    public void replaceContents(Contents data) { this.contents = data; }

    public @NotNull IndexedPagination copy() {
        final int paginationSize = pages.size();
        PaginatedMenuImpl menu = type.inventoryType() == InventoryType.CHEST
                ? new PaginatedMenuImpl(rows, paginationSize, title, modifiers, nextItem, previousItem, nextItemSlot, previousItemSlot, manager)
                : new PaginatedMenuImpl(this.type, paginationSize, title, modifiers, nextItem, previousItem, nextItemSlot, previousItemSlot, manager);
        menu.setDynamicSizing(dynamicSizing);
        menu.replaceContents(this.contents);
        menu.actions = actions;
        menu.slotActions = slotActions;
        for (int index = 0; index < paginationSize; index++) {
            BukkitContents page = menu.getPage(index);
            if (page != null) page.replaceContents(this.pages.get(index));
        }
        return menu;
    }
}
