package me.flame.menus.menu;

import com.google.common.collect.ImmutableList;
import me.flame.menus.builders.menus.MenuBuilder;
import me.flame.menus.builders.menus.MenuLayoutBuilder;
import me.flame.menus.builders.menus.PaginatedBuilder;
import me.flame.menus.menu.loader.PagedMenuLoader;
import me.flame.menus.menu.pagination.IndexedPagination;
import me.flame.menus.modifiers.Modifier;
import me.flame.menus.items.ItemResponse;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.actions.Actions;
import me.flame.menus.menu.animation.Animation;
import me.flame.menus.menu.contents.BukkitContents;
import me.flame.menus.menu.loader.MenuLoader;
import me.flame.menus.menu.opener.MenuOpener;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public interface Menu extends InventoryHolder, BukkitContents {
    boolean isAnimating();

    /**
     * Updates the menu every X ticks (repeatTime)
     *
     * @param  repeatTime  the time interval between each execution of the task
     */
    default void updatePer(long repeatTime) {
        updatePer(0, repeatTime);
    }

    /**
     * Updates the menu every X time (repeatTime)
     *
     * @param  repeatTime  the time interval between each execution of the task
     */
    default void updatePer(@NotNull Duration repeatTime) {
        this.updatePer(repeatTime.toMillis() * 50);
    }

    /**
     * Updates the menu every X ticks (repeatTime) with the delay of X ticks
     *
     * @param  delay       the time interval before the first execution
     * @param  repeatTime  the time interval between each execution of the task
     */
    void updatePer(long delay, long repeatTime);

    /**
     * Updates the menu every X ticks (repeatTime) with the delay of X ticks
     *
     * @param  delay       the time interval before the first execution
     * @param  repeatTime  the time interval between each execution of the task
     */
    default void updatePer(@NotNull Duration delay, @NotNull Duration repeatTime) {
        this.updatePer(delay.toMillis() * 50, repeatTime.toMillis() * 50);
    }

    /**
     * Update the inventory with the title
     * @apiNote this does not use NMS; so while this is backwards compatible, it can be slow.
     * @param title the new title
     */
    default void updateTitle(String title) { updateTitle(Component.text(title)); }

    /**
     * Get the modifiers of this Menu
     * @return the modifiers, a mutable view.
     */
    EnumSet<Modifier> getModifiers();

    List<HumanEntity> getViewers();

    /**
     * Update the inventory which recreates the items on default
     */
    void update();

    void recreateInventory(boolean shouldRecreate);

    void recreateInventory();

    void recreateInventory(int growRows);

    /**
     * Update the inventory with the title (RE-OPENS THE INVENTORY)
     * @param title the new title
     */
    void updateTitle(Component title);

    /**
     * Open the inventory for the provided player.
     * @apiNote Will not work if the player is sleeping.
     * @param entity the provided entity to open the inventory for.
     */
    void open(@NotNull HumanEntity entity);

    /**
     * Add a modifier to prevent a player from doing an action.
     * @param modifier the action to prevent the player from doing
     * @return the result of the operation
     */
    boolean addModifier(Modifier modifier);

    /**
     * Remove a modifier to allow a player to do an action once again.
     * @param modifier the action to allow a player to do
     * @return the result of the operation
     */
    boolean removeModifier(Modifier modifier);

    /**
     * Add every modifier to prevent a player from doing all actions
     * @return the result of the operation
     */
    boolean addAllModifiers();

    /**
     * Remove every modifier to allow a player to do all actions
     */
    void removeAllModifiers();

    /**
     * Check if items are placeable in the menu
     * @return if the items are placeable
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean areItemsPlaceable();

    /**
     * Check if items are removable in the menu
     * @return if the items are removable
     */
    boolean areItemsRemovable();

    /**
     * Check if items are swappable in the menu
     * @return if the items are swappable
     */
    boolean areItemsSwappable();

    /**
     * Check if items are cloneable in the menu
     * @return if the items are cloneable
     */
    boolean areItemsCloneable();

    /**
     * Clear every item in the menu.
     * @apiNote this doesn't iterate through every item in the menu and set it to null,
     *          but rather it just allocates a new array with a size of the menu with no items.
     */
    void clear();

    /**
     * Get how many rows there are in the menu
     * @return the number of rows
     */
    int rows();

    /**
     * Get how many columns there are in the menu
     * @return the number of rows
     */
    int columns();

    /**
     * Get the size of the menu
     * @return the size of the menu
     */
    int size();

    /**
     * Set the contents of the menu
     * @param items the contents to set
     * @apiNote this will replace all items in the menu, and this is an O(1) operation
     */
    void replaceContents(MenuItem... items);

    /**
     * Get a copy of the item data's menu items.
     *
     * @return the copy
     */
    Map<Integer, MenuItem> getItems();

    /**
     * Closes the menu for the player.
     * @param player to close the inventory for.
     */
    void close(@NotNull final HumanEntity player);

    /**
     * Get a list of the menu items in the menu
     * @return an unmodifiable list.
     */
    @NotNull
    @Unmodifiable
    default List<MenuItem> getItemList() { return ImmutableList.copyOf(getMutableItems().values()); }

    /**
     * Checks if all modifiers have been added
     * @return if the size of modifiers are equal to 4
     */
    boolean allModifiersAdded();

    /**
     * Get the title of the menu as a legacy String.
     * @return the title
     * @apiNote  Use {@link #title()}, it has the same control, but it supports legacy and adventure.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "3.1.0")
    default String getTitle() {
        return title().toString();
    }

    /**
     * Get the title of the menu as a {@link Component}.
     * <p>
     * Component+ exists to allow support for legacy and adventure titles.
     * @return the title
     */
    Component title();

    /**
     * get the type of this menu
     * @return the type of this menu.
     */
    OpenedType getType();

    /**
     * Checks if the menu is updating and doing a heavy task
     * @return if the menu is updating
     */
    boolean isUpdating();

    /**
     * Check if the menu has the ability to size itself when it's full
     * @return true if it can.
     */
    boolean isDynamicSizing();

    /**
     * Checks if this menu can be resized, usually returns false when the menu is at maximum rows, or does not use a resizable inventory type.
     * @return true if it can be resized
     */
    boolean canResize();

    static void validateSlot(int slot, int size) {
        if (slot < 0 || slot >= size) throw new IllegalArgumentException("This slot is out of bounds for menu size: " + size + "\nSize: " + size + "\nSlot: " + slot);
    }

    /**
     * The action manager for the Menu.
     * @return the action manager.
     */
    Actions actions();

    /**
     * The immutable list of slot actions.
     * @return slot actions
     */
    List<ItemResponse> getSlotActions();

    /**
     * Checks if this menu utilizes slot actions
     * @return Returns true if this menu utilizes slot actions, otherwise false
     */
    boolean hasSlotActions();

    /**
     * Sets a slot to an item response of the menu, anytime the slot is clicked, no matter the item, it will execute.
     * @param slot the slot.
     * @param response the slot action.
     */
    void setSlotAction(int slot, ItemResponse response);

    /**
     * The direct link to the contents of this menu.
     * <p>
     * The menu relies on this mostly, and even might change to rely on this in the future.
     * @return contents
     */
    BukkitContents contents();

    MenuOpener opener();

    Menus manager();

    @NotNull
    @Contract("_, _ -> new")
    static MenuBuilder builder(Menus menus, int rows) {
        return new MenuBuilder(menus, rows);
    }

    @NotNull
    @Contract("_ -> new")
    static MenuBuilder builder(Menus menus) {
        return new MenuBuilder(menus);
    }

    @NotNull
    @Contract("_, _ -> new")
    static PaginatedBuilder paginated(Menus menus, int rows) {
        return new PaginatedBuilder(menus, rows);
    }

    @NotNull
    @Contract("_ -> new")
    static PaginatedBuilder paginated(Menus menus) {
        return new PaginatedBuilder(menus);
    }

    static Menu create(Menus manager, MenuLoader loader) {
        Menu menu = loader.structure().type().inventoryType() == InventoryType.CHEST
                ? new MenuImpl(loader.structure().rows(), loader.title(), EnumSet.noneOf(Modifier.class), manager, loader.opener())
                : new MenuImpl(loader.structure().type(), loader.title(), EnumSet.noneOf(Modifier.class), manager, loader.opener());
        loader.setup(menu);

        BukkitContents contents = loader.load(menu);
        if (contents != null) menu.replaceContents(contents);

        return menu;
    }

    static Menu create(Menus manager, PagedMenuLoader loader) {
        var nextItem = loader.nextItem();
        var previousItem = loader.previousItem();
        IndexedPagination menu = loader.structure().type().inventoryType() == InventoryType.CHEST
                ? new PaginatedMenuImpl(loader.structure().rows(), loader.pageCount(), loader.title(), EnumSet.noneOf(Modifier.class), nextItem.getValue(), previousItem.getValue(), nextItem.getKey(), previousItem.getKey(), manager)
                : new PaginatedMenuImpl(loader.structure().type(), loader.pageCount(), loader.title(), EnumSet.noneOf(Modifier.class), nextItem.getValue(), previousItem.getValue(), nextItem.getKey(), previousItem.getKey(), manager);
        List<BukkitContents> contents = loader.load(menu);

        final int size = contents.size();
        for (int pageIndex = 0; pageIndex < size; pageIndex++) {
            menu.setPage(pageIndex, contents.get(pageIndex));
        }
        menu.replaceContents(contents.get(0));
        loader.setup(menu);
        return menu;
    }

    /**
     * Complex and Fast builder to build (paginated) menus from a list of strings or a so-called pattern.
     * <p>
     * Example usage:
     * <pre>{@code
     *     Map<Character, MenuItem> menuItems = ImmutableMap.of(
     *          'X', ItemBuilder.of(Material.STONE).buildItem();
     *          'K', ItemBuilder.of(Material.WHITE_STAINED_GLASS_PANE).buildItem();
     *     );
     *     Menu menu = MenuLayoutBuilder.bind(menuItems)
     *                  .pattern(
     *                      "KKKKKKKKK"
     *                      "KXX   XXK"
     *                      "KX     XK"
     *                      "KX     XK"
     *                      "KXX   XXK"
     *                      "KKKKKKKKK"
     *                  )
     *                  .createMenu("Awesome");
     * }</pre>
     * @author FlameyosFlow
     * @since 1.2.0, 100% Stabilized at 1.5.7
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "3.1.0")
    @Contract(value = "_ -> new", pure = true)
    static @NotNull MenuLayoutBuilder layout(Map<Character, MenuItem> itemMap) {
        return new MenuLayoutBuilder(itemMap);
    }

    Structure structure();

    void setDynamicSizing(boolean dynamicSizing);

    default int addItem(@NotNull final MenuItem... items) {
        return this.contents().addItem(items);
    }

    default int addItem(final List<MenuItem> toAdd, final @NotNull MenuItem... items) {
        return this.contents().addItem(toAdd, items);
    }

    List<Animation> getActiveAnimations();

    void setAnimating(boolean animating);
}
