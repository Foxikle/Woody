package me.flame.menus.menu;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import lombok.Getter;
import lombok.Setter;

import me.flame.menus.adventure.TextHolder;
import me.flame.menus.builders.menus.MenuBuilder;
import me.flame.menus.builders.menus.PaginatedBuilder;
import me.flame.menus.components.nbt.*;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.actions.Actions;
import me.flame.menus.menu.animation.Animation;
import me.flame.menus.menu.fillers.*;
import me.flame.menus.modifiers.Modifier;
import me.flame.menus.util.ItemResponse;
import me.flame.menus.util.Option;
import me.flame.menus.util.VersionHelper;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.*;

import java.io.Serializable;
import java.util.*;
import java.util.function.*;

/**
 * Most commonly used normal Menu
 * <p>
 *
 */
@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class Menu implements IMenu, RandomAccess, Serializable {
    protected @Getter @NotNull Inventory inventory;

    protected final @Getter @NotNull MenuType type;

    protected final @Getter @NotNull EnumSet<Modifier> modifiers;

    protected @NotNull TextHolder title;

    protected @Getter @Setter MenuFiller defaultFiller = Filler.from(this);

    protected @Getter @Setter boolean dynamicSizing = false, updating = false;

    boolean hasAnimationsStarted = false;

    protected @Getter boolean changed = false;

    protected int rows = 1, size;

    protected ItemData data;

    protected ItemResponse[] slotActions;

    final @Getter List<Animation> animations = new ArrayList<>(5);

    protected static final @Getter Plugin plugin;

    static {
        plugin = JavaPlugin.getProvidingPlugin(Menu.class);
        ItemNbt.wrapper(VersionHelper.IS_PDC_VERSION ? new Pdc(plugin) : new LegacyNbt());
        Bukkit.getPluginManager().registerEvents(new MenuListeners(), plugin);
    }

    private final Actions actions = new Actions(this);

    @Override
    public int rows() { return rows; }

    @Override
    public int size() { return size; }

    public Menu(int rows, @NotNull TextHolder title, @NotNull EnumSet<Modifier> modifiers) {
        this.modifiers = modifiers;
        this.rows = rows;
        this.type = MenuType.CHEST;
        this.title = title;
        this.size = rows * 9;
        this.data = new ItemData(this);
        this.inventory = this.title.toInventory(this, size);
    }

    public Menu(@NotNull MenuType type, @NotNull TextHolder title, @NotNull EnumSet<Modifier> modifiers) {
        this.type = type;
        this.modifiers = modifiers;
        this.title = title;
        this.size = type.getLimit();
        this.data = new ItemData(this);
        this.inventory = this.title.toInventory(this, type.getType());
    }

    public List<ItemResponse> getSlotActions() { return List.of((slotActions == null) ? (slotActions = new ItemResponse[size]) : slotActions); }


   ItemResponse[] getMutableSlotActions() { return (slotActions == null) ? (slotActions = new ItemResponse[size]) : slotActions; }

    public boolean hasSlotActions() { return slotActions != null; }

    public MenuFiller getFiller() { return defaultFiller; }

    public <T extends MenuFiller> T getFiller(@NotNull Class<T> value) { return value.cast(getFiller()); }

    public void forEach(Consumer<? super MenuItem> action) { data.forEach(action); }

    public List<HumanEntity> getViewers() { return inventory.getViewers(); }

    public boolean addItem(@NotNull final ItemStack... items) { return (changed = data.addItem(items)); }

    public boolean addItem(@NotNull final MenuItem... items) {
        return (changed = data.addItem(items));
    }

    public boolean addItem(@NotNull final List<MenuItem> items) { return (changed = addItem(items.toArray(new MenuItem[0]))); }

    public void set(int slot, ItemStack item) {
        this.data.setItem(slot, MenuItem.of(item));
    }

    public void set(int slot, MenuItem item) {
        this.data.setItem(slot, item);
        changed = true;
    }

    public Option<MenuItem> get(int i) { return Option.some(data.getItem(i)); }

    public boolean hasItem(int slot) {
        return this.data.hasItem(slot);
    }

    public boolean hasItem(ItemStack item) { return data.findFirst(itemOne -> itemOne.getItemStack().equals(item)).isSome(); }

    public boolean hasItem(MenuItem item) { return data.findFirst(itemOne -> itemOne.equals(item)).isSome(); }

    public Option<MenuItem> get(Predicate<MenuItem> itemDescription) { return data.findFirst(itemDescription);
     }

    public void setSlotAction(int slot, ItemResponse response) {
        ItemResponse[] slotActions = this.getMutableSlotActions();
        slotActions[slot] = response;
    }

    public void removeItem(@NotNull final ItemStack... itemStacks) { changed = data.removeItem(itemStacks); }

    public void removeItemStacks(@NotNull final List<ItemStack> itemStacks) { removeItem(itemStacks.toArray(new ItemStack[0])); }

    public void removeItem(@NotNull final MenuItem... items) { changed = data.removeItem(items); }

    @Override
    public void removeItem(@NotNull final List<MenuItem> itemStacks) { removeItem(itemStacks.toArray(new MenuItem[0])); }

    @Contract("_ -> new")
    @CanIgnoreReturnValue
    public MenuItem removeItem(int index) {
        MenuItem item = this.data.removeItem(index);
        if (item != null) changed = true;
        return item;
    }

    @Override
    public void update() { update(false); }

    public void update(boolean force) {
        if (force) {
            updatePlayerInventories(inventory, player -> ((Player) player).updateInventory());
            return;
        }
        if (!changed) return;
        updatePlayerInventories(inventory, player -> ((Player) player).updateInventory());
        this.changed = false;
    }

    public void updateTitle(TextHolder title) {
        Inventory oldInventory = inventory, updatedInventory = copyInventory(type, title, this, rows);
        this.inventory = updatedInventory;
        updatePlayerInventories(oldInventory, player -> player.openInventory(updatedInventory));
    }

    private void updatePlayerInventories(@NotNull Inventory oldInventory, Consumer<HumanEntity> entityPredicate) {
        this.updating = true;
        data.recreateItems(inventory);
        oldInventory.getViewers().forEach(entityPredicate);
        this.updating = false;
    }

    public void open(@NotNull HumanEntity entity) { if (!entity.isSleeping()) entity.openInventory(inventory); }

    public void close(@NotNull final HumanEntity player) { VersionHelper.runDelayed(player::closeInventory, 1L); }

    public boolean addModifier(Modifier modifier) { return modifiers.add(modifier); }

    public boolean removeModifier(Modifier modifier) { return modifiers.remove(modifier); }

    public boolean addAllModifiers() { return modifiers.addAll(Modifier.ALL); }

    public void removeAllModifiers() { Modifier.ALL.forEach(modifiers::remove); }

    public boolean areItemsPlaceable() { return !modifiers.contains(Modifier.DISABLE_ITEM_ADD); }

    public boolean areItemsRemovable() { return !modifiers.contains(Modifier.DISABLE_ITEM_REMOVAL); }

    public boolean areItemsSwappable() { return !modifiers.contains(Modifier.DISABLE_ITEM_SWAP); }

    public boolean areItemsCloneable() { return !modifiers.contains(Modifier.DISABLE_ITEM_CLONE); }

    public void updateItem(final int slot, @NotNull final ItemStack itemStack) {
        data.updateItem(slot, itemStack, this.data.getItem(slot));
    }

    public void setContents(MenuItem... items) {
        changed = true;
        this.data.contents(items);
    }

    @NotNull
    public MenuItem[] getItems() { return this.data.getItems(); }

    public @NotNull ItemData getData() { return new ItemData(this.data); }
    public @NotNull Option<ItemData> getOptionalData() { return Option.some(this.getData()); }

    public @NotNull @Unmodifiable List<MenuItem> getItemList() { return ImmutableList.copyOf(getItems()); }

    @Override
    public boolean hasAnimations() { return !animations.isEmpty(); }

    @Override
    public void addAnimation(Animation animation) { animations.add(animation); }

    @Override
    public void removeAnimation(Animation animation) { animations.remove(animation); }

    @Override
    public void removeAnimation(int animationIndex) { animations.remove(animationIndex); }

    @Override
    public boolean hasAnimationsStarted() { return this.hasAnimationsStarted; }

    @Override
    public void hasAnimationsStarted(boolean state) { this.hasAnimationsStarted = state; }

    public void clear() { data = new ItemData(this); }

    @Override
    public boolean allModifiersAdded() { return modifiers.size() == 4; }

    public void recreateInventory(boolean shouldRecreate) {
        if (rows == 6) return;

        rows++;
        size = rows * 9;
        inventory = copyInventory(type, title, this, size);
        if (shouldRecreate) data.recreateInventory();
    }

    private static @NotNull Inventory copyInventory(@NotNull MenuType type, @NotNull TextHolder title, Menu menu, int size) {
        return type == MenuType.CHEST ? title.toInventory(menu, size) : title.toInventory(menu, type.getType());
    }

    public Menu copy() { return MenuData.intoData(this).asMenu(); }

    public @NotNull @Contract("_ -> new") static MenuBuilder builder(int rows) { return new MenuBuilder(rows); }
    public @NotNull @Contract(" -> new") static MenuBuilder builder() { return new MenuBuilder(); }

    public @NotNull @Contract("_ -> new") static MenuBuilder paginated(int rows) { return new PaginatedBuilder(rows); }
    public @NotNull @Contract(" -> new") static MenuBuilder paginated() { return new PaginatedBuilder(); }

    @Override
    public String getTitle() { return title.toString(); }

    @Override
    public TextHolder title() { return title; }

    @Override
    public Actions actions() { return this.actions; }

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
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull MenuLayoutBuilder layout(Map<Character, MenuItem> itemMap) { return new MenuLayoutBuilder(itemMap); }
}