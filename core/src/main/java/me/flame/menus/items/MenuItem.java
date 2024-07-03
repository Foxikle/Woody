package me.flame.menus.items;

import me.flame.menus.components.nbt.ItemNbt;

import me.flame.menus.menu.Menu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;

import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;
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
 * @see me.flame.menus.builders.items.ItemBuilder
 */
@SuppressWarnings("unused")
@SerializableAs("woody-menu")
public final class MenuItem implements
        Serializable, ConfigurationSerializable, Comparable<MenuItem> {
    ItemResponse clickAction;

    @NotNull ItemStack stack;

    boolean async = false;
    private Predicate<Menu> visiblity;

    private final UUID uniqueId;

    private final boolean excludeDefaultNbt;

    private ClickSound sound;

    private MenuItem(ItemStack itemStack, @Nullable ItemResponse action, @Nullable UUID uniqueId, boolean excludeNbt) {
        Objects.requireNonNull(itemStack);
        this.uniqueId = uniqueId;
        this.excludeDefaultNbt = excludeNbt;
        this.stack = (uniqueId != null && !excludeNbt) ? ItemNbt.setString(itemStack, "woody-menu", uniqueId.toString()) : itemStack;
        this.clickAction = action;
    }

    public void setNbt(String key, String value) {
        ItemNbt.setString(stack, key, value);
    }

    public void setAmount(int amount) {
        this.stack.setAmount(amount);
    }

    public int getAmount() {
        return stack.getAmount();
    }

    @Nullable
    public ItemResponse getClickAction() { return clickAction; }

    public void setClickAction(@NotNull ItemResponse clickAction) {
        this.clickAction = clickAction;
    }

    @Contract(" -> new")
    public @NotNull ItemEditor editor() { return new ItemEditor(this); }

    @Contract(" -> new")
    public @NotNull SkullItemEditor skullEditor() { return new SkullItemEditor(this); }

    public void setItem(ItemStack stack) {
        this.stack = ItemNbt.setString(stack, "woody-menu", uniqueId.toString());
    }

    public @NotNull Material getType() { return stack.getType(); }

    @Override
    public boolean equals(Object item) {
        if (item == this) return true;
        if (!(item instanceof MenuItem)) return false;
        return this.uniqueId.equals(((MenuItem) item).uniqueId);
    }

    @Override
    public @NotNull MenuItem clone() {
        try {
            return (MenuItem) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public ClickSound getSound() {
        return sound;
    }

    public void setSound(final ClickSound sound) {
        this.sound = sound;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> result = new LinkedHashMap<>(4);
        result.put("type", getType().name());
        result.put("uuid", uniqueId);
        result.put("excludeNbt", excludeDefaultNbt);

        final ItemMeta meta = stack.getItemMeta();
        final int amount = stack.getAmount();
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

        return new MenuItem(result, null, uuid, (boolean) serialized.getOrDefault("excludeNbt", false));
    }

    @Override
    public int hashCode() {return uniqueId.hashCode(); } // UUID provide a fast and anti-collision hashcode

    @Override
    public int compareTo(@NotNull MenuItem item) {
        return uniqueId.compareTo(item.uniqueId);
    }

    @SuppressWarnings("deprecation")
    public @Nullable String getCustomName() {
        ItemMeta itemMeta = stack.getItemMeta();
        return itemMeta == null ? null : itemMeta.getDisplayName();
    }

    public void setCustomName(@Nullable String s) {
        editor().setName(spigotify(stack, s)).done();
    }

    public @NotNull UUID getUniqueId() {
        return uniqueId;
    }

    public Predicate<Menu> getVisiblity() {
        return visiblity;
    }

    public boolean isAsync() {
        return async;
    }

    public ItemStack getItemStack() {
        return stack;
    }


    /*
     * Static methods
     */

    public static @NotNull String spigotify(ItemStack itemStack, String s) {
        if (s != null && !s.isEmpty()) return ChatColor.translateAlternateColorCodes('&', s);

        String name = itemStack.getType().name();
        StringBuilder builder = new StringBuilder(name.length());

        boolean capitalizeNext = false;
        for (char character : name.toCharArray()) {
            if (character != '_') builder.append(capitalizeNext ? character : Character.toLowerCase(character));
            builder.append(' ');
            capitalizeNext = true;
        }
        return builder.toString();
    }

    /**
     * Static constructors
     */

    @Contract("_ -> new")
    public static @NotNull MenuItem of(ItemStack itemStack) {
        return new MenuItem(itemStack, null, UUID.randomUUID(), false);
    }

    @Contract("_, _ -> new")
    public static @NotNull MenuItem of(ItemStack itemStack, ItemResponse response) {
        return new MenuItem(itemStack, response, UUID.randomUUID(), false);
    }

    @Contract("_, _ -> new")
    public static @NotNull MenuItem of(ItemStack itemStack, boolean excludeNbt) {
        return new MenuItem(itemStack, null, UUID.randomUUID(), excludeNbt);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull MenuItem of(ItemStack itemStack, ItemResponse response, boolean excludeNbt) {
        return new MenuItem(itemStack, response, UUID.randomUUID(), excludeNbt);
    }
}
