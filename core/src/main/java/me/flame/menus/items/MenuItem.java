package me.flame.menus.items;

import lombok.Getter;
import lombok.Setter;

import me.flame.menus.components.nbt.ItemNbt;
import me.flame.menus.items.states.State;
import me.flame.menus.menu.Menu;
import me.flame.menus.util.ItemResponse;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * A Gui itemStack which was particularly made to have an action.
 * <p>
 * Good example of using "MenuItem":
 * <pre>{@code
 *      var menuItem = ...;
 *      menuItem.setClickAction(event -> {
 *          ...
 *      });
 *
 *      // implementing a new itemStack:
 *      menu.addItem(ItemBuilder.of(itemStack, 2) // 2 is the amount of items you get from this "ItemBuilder"
 *                                  .setName(...).setLore(...)
 *                                  .buildItem(() -> ...); // ItemBuilder#build will give you a normal ItemStack
 *      // the lambda (Consumer) at ItemBuilder#buildItem(Consumer) is optional and you do not have to provide an action, you can use ItemBuilder#buildItem()
 *
 *      // editing the item stack
 *      menuItem.editor() // use methods, such as
 *              .setName("Pumpkin")
 *              .setLore("This is a random item named a Pumpkin")
 *              .done(); // no need to set item again in the menu but you can.
 * }</pre>
 */
@SuppressWarnings("unused")
@SerializableAs("woody-menu")
public final class MenuItem implements Cloneable, Serializable, Comparable<MenuItem>, ConfigurationSerializable {
    @NotNull transient CompletableFuture<ItemResponse> clickAction;
    @NotNull @Getter ItemStack itemStack;

    @Getter @Setter boolean async = false;
    private transient @Getter @Setter Predicate<Menu> visiblity;

    private final @NotNull @Getter UUID uniqueId;
    private Map<UUID, Long> usageCooldown;

    private List<State> states;

    private MenuItem(ItemStack itemStack, @Nullable ItemResponse action) {
        Objects.requireNonNull(itemStack);
        this.uniqueId =  UUID.randomUUID();
        this.itemStack = ItemNbt.setString(itemStack, "woody-menu", uniqueId.toString());
        this.clickAction = CompletableFuture.completedFuture(action == null ? (slot, event) -> {} : action);
    }

    private MenuItem(ItemStack itemStack, @Nullable ItemResponse action, @NotNull UUID uniqueId) {
        Objects.requireNonNull(itemStack);
        this.uniqueId =  UUID.randomUUID();
        this.itemStack = ItemNbt.setString(itemStack, "woody-menu", uniqueId.toString());
        this.clickAction = CompletableFuture.completedFuture(action == null ? (slot, event) -> {} : action);
    }

    public static @NotNull MenuItem of(ItemStack itemStack, @Nullable ItemResponse action) {
        return new MenuItem(itemStack, action);
    }

    public static @NotNull MenuItem of(ItemStack itemStack) {
        return new MenuItem(itemStack, null);
    }

    @ApiStatus.Internal
    public static MenuItem of(ItemStack itemStack, ItemResponse clickAction, UUID uniqueId) {
        return new MenuItem(itemStack, clickAction, uniqueId);
    }

    @NotNull
    public CompletableFuture<ItemResponse> getClickAction() { return clickAction; }

    @NotNull
    public ItemResponse getClickActionNow(ItemResponse ifAbsent) { return clickAction.getNow(ifAbsent); }

    public void setClickAction(@NotNull ItemResponse clickAction) {
        this.clickAction = CompletableFuture.completedFuture(clickAction);
    }

    @Contract(" -> new")
    public @NotNull ItemEditor editor() { return new ItemEditor(this); }

    @Contract(" -> new")
    public @NotNull SkullItemEditor skullEditor() { return new SkullItemEditor(this); }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = ItemNbt.setString(itemStack, "woody-menu", uniqueId.toString());
    }

    public @NotNull Material getType() { return itemStack.getType(); }

    @Override
    public boolean equals(Object item) {
        if (item == this) return true;
        if (!(item instanceof MenuItem)) return false;
        return uniqueId.equals(((MenuItem) item).uniqueId);
    }

    @Override
    public @NotNull MenuItem clone() {
        try {
            return (MenuItem) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Void> click(final Player player, final InventoryClickEvent event) {
        return async ? clickAction.thenAcceptAsync(ca -> ca.execute(player, event)) : clickAction.thenAccept(ca -> ca.execute(player, event));
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<>(4);
        result.put("type", getType().name());
        result.put("uuid", uniqueId);

        final ItemMeta meta = itemStack.getItemMeta();
        final int amount = itemStack.getAmount();
        if (amount != 1) result.put("amount", amount);
        if (meta != null) result.put("meta", meta);

        return result;
    }

    @NotNull
    public static MenuItem deserialize(@NotNull Map<String, Object> serialized) {
        final String type = (String) serialized.get("type");
        if (type == null) throw new NullPointerException("Type turned out to be null, not good! \nResorting to error NPE \nSerialized Map: " + serialized);

        final int amount = (int) serialized.getOrDefault("amount", 1);
        final ItemMeta meta = (ItemMeta) serialized.get("meta");

        final UUID uuid = (UUID) serialized.get("uuid");
        if (uuid == null) throw new NullPointerException("UUID turned out to be null, not good! \nResorting to error NPE \nSerialized Map: " + serialized);

        final ItemStack result = new ItemStack(Material.valueOf(type), amount);
        if (meta != null) result.setItemMeta(meta);

        return new MenuItem(result, null, uuid);
    }

    @Override
    public int hashCode() {return uniqueId.hashCode(); } // UUID provide a fast and anti-collision hashcode

    @Override
    public int compareTo(@NotNull MenuItem menuItem) {
        return uniqueId.compareTo(menuItem.uniqueId);
    }

    private List<State> getStates() {
        return (states == null) ? (states = new ArrayList<>(5)) : states;
    }

    private Map<UUID, Long> getUsageCooldown() {
        return (usageCooldown == null) ? (usageCooldown = new HashMap<>(10)) : usageCooldown;
    }

    public @Nullable String getCustomName() {
        ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta == null ? null : itemMeta.getDisplayName();
    }

    public void setCustomName(@Nullable String s) { editor().setName(spigotify(itemStack, s)).done(); }

    private static @NotNull String spigotify(ItemStack itemStack, String s) {
        if (s == null || s.isEmpty()) {
            String name = itemStack.getType().name();
            boolean capitalizeNext = false;
            StringBuilder builder = new StringBuilder(name.length());
            for (char character : name.toCharArray()) {
                if (character == '_') {
                    builder.append(' ');
                    capitalizeNext = true;
                }
                builder.append(capitalizeNext ? character : Character.toLowerCase(character));
            }
            return builder.toString();
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public void updateStates() { getStates().forEach(State::update); }

    public boolean hasStates() { return states != null && !states.isEmpty(); }

    public boolean hasCooldowns() { return usageCooldown != null && !usageCooldown.isEmpty(); }

    public void addState(State state) { getStates().add(state); }

    public void removeState(State state) { getStates().remove(state); }

    public void removeState(int state) { getStates().remove(state); }

    public boolean isOnCooldown(Player player) {
        if (!hasCooldowns()) return false;
        Long cooldown = getUsageCooldown().get(player.getUniqueId());
        return cooldown != null && cooldown < System.currentTimeMillis();
    }

    public void addCooldown(@NotNull Player player, long millis) {
        getUsageCooldown().put(player.getUniqueId(), System.currentTimeMillis() + millis);
    }
}
