package me.flame.menus.menu;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import me.flame.menus.adventure.TextHolder;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.fillers.*;
import me.flame.menus.modifiers.Modifier;

import me.flame.menus.util.Option;
import org.bukkit.entity.HumanEntity;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Menu that allows you to have multiple pages
 * <p>1.1.0: PaginatedMenu straight out of Triumph-GUIS</p>
 * <p>1.4.0: PaginatedMenu rewritten as List<Page></p>
 * <p>2.0.0: PaginatedMenu rewritten as List<ItemData> instead to improve DRY and reduce it by about 250+ lines</p>
 * @since 2.0.0
 * @author FlameyosFlow
 */
@SuppressWarnings("unused")
public final class PaginatedMenu extends Menu implements Pagination {
    @NotNull
    final List<ItemData> pages;

    private final Map<Integer, MenuItem> pageItems = new HashMap<>();

    @Getter
    private int pageNumber, nextItemSlot = -1, previousItemSlot = -1;
    
    private MenuItem nextItem, previousItem;

    private final MenuFiller pageDecorator = PageDecoration.create(this);

    public <T extends MenuFiller> T getPageDecorator(Class<T> pageClass) {
        return pageClass.cast(pageDecorator);
    }

    /**
     * Adds a blank page to the menu.
     * @return the index the page was added at
     */
    public int addPage() {
        pages.add(new ItemData(this));
        return pages.size() - 1;
    }

    public void setPageItems(ItemData items) {
        if (pageItems == null) return;
        for (int i : pageItems.keySet()) items.setItem(i, pageItems.get(i));
        if (nextItemSlot != -1) items.setItem(nextItemSlot, pages.get(0).getItem(nextItemSlot));
        if (previousItemSlot != -1) items.setItem(previousItemSlot, pages.get(0).getItem(previousItemSlot));
    }

    /**
     * Main constructor to provide a way to create PaginatedMenu
     *
     * @param pageRows The page size.
     */
    public PaginatedMenu(final int pageRows, final int pageCount, TextHolder title, EnumSet<Modifier> modifiers, MenuItem nextItem, MenuItem previousItem, int nextItemSlot, int previousItemSlot) {
        super(pageRows, title, modifiers);
        this.pages = new ArrayList<>(pageCount);

        this.setNextPageItem(nextItemSlot, nextItem);
        this.setPreviousPageItem(previousItemSlot, previousItem);

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++)
            pages.add(new ItemData(this));
        this.data = pages.get(pageNumber);
    }

    /**
     * Main constructor to provide a way to create PaginatedMenu
     */
    public PaginatedMenu(MenuType type, final int pageCount, TextHolder title, EnumSet<Modifier> modifiers, MenuItem nextItem, MenuItem previousItem, int nextItemSlot, int previousItemSlot) {
        super(type, title, modifiers);
        this.pages = new ArrayList<>(pageCount);

        this.nextItemSlot = nextItemSlot;
        this.pageItems.put(nextItemSlot, nextItem);
        nextItem.setVisiblity(menu -> menu instanceof PaginatedMenu && ((PaginatedMenu) menu).getCurrentPageNumber() != getPagesSize());

        this.previousItemSlot = previousItemSlot;
        this.pageItems.put(previousItemSlot, previousItem);
        previousItem.setVisiblity(menu -> menu instanceof PaginatedMenu && ((PaginatedMenu) menu).getCurrentPageNumber() != 0);

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++)
            pages.add(new ItemData(this));
        this.data = pages.get(pageNumber);
    }

    public ImmutableList<ItemData> pages() { return ImmutableList.copyOf(pages); }

    public void setNextPageItem(int nextItemSlot, MenuItem nextItem) {
        this.nextItemSlot = nextItemSlot;
        this.pageItems.put(nextItemSlot, nextItem);
        this.setItem(nextItemSlot, nextItem);
        nextItem.setVisiblity(menu -> menu instanceof PaginatedMenu && ((PaginatedMenu) menu).getCurrentPageNumber() != getPagesSize());
    }

    public void setPreviousPageItem(int previousItemSlot, MenuItem previousItem) {
        this.previousItemSlot = previousItemSlot;
        this.pageItems.put(previousItemSlot, previousItem);
        this.setItem(previousItemSlot, previousItem);
        nextItem.setVisiblity(menu -> menu instanceof PaginatedMenu && ((PaginatedMenu) menu).getCurrentPageNumber() != 0);

    }

    public void recreateInventory() {
        super.recreateInventory(false);
        pages.forEach(ItemData::recreateInventory);
    }

    @Override
    public @NotNull MenuType getType() { return type; }

    /**
     * Opens the GUI to a specific page for the given player
     *
     * @param player   The {@link HumanEntity} to open the GUI to
     * @param openPage The specific page to open at
     */
    public void open(@NotNull final HumanEntity player, final int openPage) {
        if (player.isSleeping()) return;
        if (openPage < 0 || openPage >= pages.size()) {
            throw new IllegalArgumentException(
                "\"openPage\" out of bounds; must be 0-" + (pages.size() - 1) +
                "\nopenPage: " + openPage + "\nFix: Make sure \"openPage\" is 0-" + (pages.size() - 1)
            );
        }

        this.pageNumber = openPage;
        this.data = pages.get(openPage);
        player.openInventory(inventory);
    }

    /**
     * Opens the GUI to a specific page for the given player
     *
     * @param player   The {@link HumanEntity} to open the GUI to
     */
    public void open(@NotNull final HumanEntity player) { this.open(player, 0); }

    /**
     * Gets the current page number (Inflated by 1)
     *
     * @return The current page number
     */
    @Override
    public int getCurrentPageNumber() { return pageNumber + 1; }

    /**
     * Gets the number of pages the GUI has
     *
     * @return The number of pages
     */
    @Override
    public int getPagesSize() { return pages.size(); }

    /**
     * Goes to the next page
     *
     * @return False if there is no next page.
     */
    @Override
    public boolean next() { return page(pageNumber + 1); }

    /**
     * Goes to the previous page if possible
     *
     * @return False if there is no previous page.
     */
    @Override
    public boolean previous() { return page(pageNumber - 1); }

    /**
     * Goes to the specified page
     *
     * @return False if there is no next page.
     */
    @Override
    public boolean page(int pageNum) {
        if (pageNum < 0 || pageNum >= pages.size()) return false;
        this.pageNumber = pageNum;
        this.data = pages.get(pageNum);
        update(true);
        return true;
    }

    @Override
    public Option<ItemData> getOptionalPage(int index) {
        return (index < 0 || index > pages.size()) ? Option.none() : Option.some(pages.get(index));
    }

    @Override
    public Map<Integer, MenuItem> pageItems() {
        return pageItems;
    }

    public void addItems(@NotNull MenuItem... items) {
        if (items == null || items.length == 0) return;

        List<MenuItem> leftovers = new ArrayList<>();
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            @NotNull MenuItem[] finalItems = items;
            getOptionalPage(pageIndex).peek(page -> page.addItem(leftovers, finalItems));
            items = leftovers.toArray(new MenuItem[0]);
            leftovers.clear();
        }
        if (!dynamicSizing) return;

        int newestPageNumber = pageNumber;
        while (items.length != 0) {
            int newPageNum = addPage();
            newestPageNumber = newPageNum;

            @NotNull MenuItem[] finalItems = items;
            getOptionalPage(newPageNum).peek(page -> page.addItem(leftovers, finalItems));

            items = leftovers.toArray(new MenuItem[0]);
            leftovers.clear();
        }

        page(newestPageNumber);
    }

    public @NotNull MenuData getMenuData() { return MenuData.intoData(this); }

    /*public PaginatedMenu copy() {
        return create(getMenuData());
    }*/

    public void setContents(ItemData data) {
        this.data = data;
        this.changed = true;
    }
}
