package me.flame.menus.adventure;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Immutable wrapper of a text-like value.
 * Support for both Adventure and legacy strings is achieved through this class.
 * To get an instance of this class please refer to either {@link StringHolder#of(String)}
 * or {@link CompHolder#of(Component)}.
 * Other methods like {@link #empty()} and {@link #of(String)}
 * also exist, but their use cases are very limited.
 *
 * @see StringHolder
 * @see CompHolder
 * @since 2.0.0
 * @author stefvanschie at <a href="https://github.com/stefvanschie/IF">IF's github.</a>
 */
public abstract class TextHolder {
    
    /**
     * Gets an instance that contains no characters and no formatting.
     *
     * @return an instance without any characters or formatting
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public static TextHolder empty() {
        return StringHolder.empty();
    }

    /**
     * Gets an instance of Lore that contains no characters and no formatting.
     *
     * @return an instance without any characters or formatting
     * @since 2.0.1
     */
    @NotNull
    @Contract(pure = true)
    public static Lore emptyLore() {
        return Lore.empty();
    }
    
    /**
     * Deserializes the specified {@link String} as a {@link TextHolder}.
     * This method is still WIP and may change drastically in the future:
     * <ul>
     *     <li>Are we going to use MiniMessage if it's present?</li>
     *     <li>Is MiniMessage going to be opt-in? If yes, how do we opt-in?</li>
     * </ul>
     *
     * @param string the raw data to deserialize
     * @return an instance containing the text from the string
     * @since 0.10.0
     */
    @NotNull
    @Contract(pure = true)
    public static TextHolder of(@NotNull String string) {
        return StringHolder.of(ChatColor.translateAlternateColorCodes('&', string));
    }
    
    TextHolder() {}
    
    /**
     * Creates a new inventory with the wrapped value as the inventory's title.
     *
     * @param holder the holder to use for the new inventory
     * @param type the type of inventory to create
     * @return a newly created inventory with the wrapped value as its title
     * @since 2.0.0
     */
    @NotNull
    @Contract(pure = true)
    public abstract Inventory toInventory(InventoryHolder holder, InventoryType type);

    /**
     * Takes the lore from the specified meta.
     * <p>
     * If the meta has no lore, this method returns {@link #emptyLore()}
     * @param meta the meta to take the lore from
     * @return the lore from the meta with compatibility for legacy strings and components.
     *         or it may return {@link #emptyLore()} if the meta has no lore
     * @since 2.0.1
     */
    public Lore takeLore(ItemMeta meta) {
        return meta == null ? Lore.empty() : new Lore(meta);
    }

    /**
     * Creates a new inventory with the wrapped value as the inventory's title.
     *
     * @param holder the holder to use for the new inventory
     * @param size the count of slots the inventory should have (normal size restrictions apply)
     * @return a newly created inventory with the wrapped value as its title
     * @since 2.0.0
     */
    @NotNull
    @Contract(pure = true)
    public abstract Inventory toInventory(InventoryHolder holder, int size);
    
    /**
     * Modifies the specified meta: sets the display name to the wrapped value.
     *
     * @param meta the meta whose display name to set
     * @since 2.0.0
     */
    public abstract void asItemDisplayName(ItemMeta meta);
    
    /**
     * Modifies the specified meta: adds the wrapped value as a new lore line at the end
     *
     * @param meta the meta whose lore to append to
     * @since 2.0.0
     */
    public abstract void asItemLoreAtEnd(ItemMeta meta);

    /**
     * Modifies the specified meta: sets the lore to the value
     *
     * @param meta the meta whose lore to append to
     * @since 2.0.0
     */
    public abstract void asItemLore(ItemMeta meta);

    /**
     * Converts the text wrapped by this class instance to a legacy string,
     * keeping the original formatting.
     *
     * @return the wrapped value represented as a legacy string
     * @since 2.0.0
     */
    @NotNull
    @Override
    @Contract(pure = true)
    public abstract String toString();

    public abstract boolean contains(@NotNull TextHolder holder);

    Component asComponent() {
        throw new UnsupportedOperationException();
    }
}
