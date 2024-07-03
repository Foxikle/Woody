package me.flame.menus.items;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

@SuppressWarnings({ "unused", "deprecation" })
public class ItemEditor {
    @NotNull
    protected final ItemStack item;

    @NotNull
    protected final MenuItem button;

    protected ItemResponse clickAction;

    protected final ItemMeta meta;

    protected final boolean hasNoItemMeta;

    public ItemEditor(MenuItem button) {
        this.button = button;
        this.item = button.stack;
        this.clickAction = button.clickAction;
        this.meta = this.item.getItemMeta();
        this.hasNoItemMeta = this.meta == null;
    }

    /**
     * Edits the name of the itemStack to whatever the provided title is.
     * <p>
     * Automatically colorized, so no need to try to colorize it again.
     * @param title the new name of the title
     * @return the builder for chaining
     */
    @ApiStatus.Obsolete
    public ItemEditor setName(String title) {
        return this.setName(title, true);
    }

    /**
     * Edits the name of the itemStack to whatever the provided title is.
     * @param title the new name of the title
     * @return the builder for chaining
     */
    public ItemEditor name(Component title) {
        meta.displayName(title);
        return this;
    }

    /**
     * Sets the glow effect on the item.
     *
     * @param  glow  true to add enchantment and hide it, false to remove enchantment and show it
     * @apiNote Will hide the enchantments by default.
     * @return       the builder for chaining
     */
    public ItemEditor setGlow(boolean glow) {
        // add enchantment and hide it if "glow" is true
        if (this.hasNoItemMeta) return this;
        if (!glow) {
            this.meta.removeEnchant(Enchantment.DURABILITY);
            this.meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            return this;
        }
        this.meta.addEnchant(Enchantment.DURABILITY, 1, true);
        this.meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    /**
     * Edits the name of the itemStack to whatever the provided title is.
     * <p>
     * Automatically colorized, so no need to try to colorize it again.
     * @param title the new name of the title
     * @return the builder for chaining
     */
    @ApiStatus.Obsolete
    public ItemEditor setName(String title, boolean colorize) {
        if (this.hasNoItemMeta) return this;
        this.meta.setDisplayName(colorize ? translateAlternateColorCodes('&', title) : title);
        return this;
    }


    /**
     * Edits the lore of the itemStack to whatever the provided lore is.
     * <p>
     * Automatically colorized, so no need to try to colorize it again.
     * @param lore the new lore of the itemStack
     * @return the builder for chaining
     */
    public ItemEditor setLore(String... lore) {
        return this.setLore(List.of(lore));
    }


    /**
     * Edits the lore of the itemStack to whatever the provided lore is.
     * <p>
     * Automatically colorized, so no need to try to colorize it again.
     * @param lore the new lore of the itemStack
     * @return the builder for chaining
     */
    public ItemEditor lore(List<Component> lore) {
        if (this.hasNoItemMeta) return this;
        meta.lore(lore);
        return this;
    }

    /**
     * Edits the lore of the itemStack to whatever the provided lore is.
     * <p>
     * Automatically colorized, so no need to try to colorize it again.
     * @param lore the new lore of the itemStack
     * @return the builder for chaining
     */
    public ItemEditor setLore(@NotNull List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    /**
     * Edits the lore of the itemStack to whatever the provided lore is.
     * <p>
     * Automatically colorized, so no need to try to colorize it again.
     * @param colorized whether to colorize it or not
     * @param lore the new lore of the itemStack
     * @return the builder for chaining
     */
    public ItemEditor setLore(boolean colorized, String... lore) {
        return this.setLore(colorized, List.of(lore));
    }

    /**
     * Edits the lore of the itemStack to whatever the provided lore is.
     * <p>
     * Automatically colorized, so no need to try to colorize it again.
     * @param colorized whether to colorize it or not
     * @param lore the new lore of the itemStack
     * @return the builder for chaining
     */
    public ItemEditor setLore(boolean colorized, List<String> lore) {
        if (this.hasNoItemMeta) return this;
        if (colorized) return this.setLore(lore);
        this.meta.setLore(lore);
        return this;
    }

    /**
     * Edits the lore of the itemStack to whatever the provided lore is.
     * <p>
     * Automatically colorized, so no need to try to colorize it again.
     * @param lore the new lore of the itemStack
     * @return the builder for chaining
     */
    public ItemEditor lore(Component... lore) {
        return lore(List.of(lore));
    }

    /**
     * Enchant the itemStack regularly
     * @param enchantment the enchantment
     * @return the builder for chaining
     */
    public ItemEditor enchant(Enchantment enchantment) {
        if (this.hasNoItemMeta) return this;
        this.meta.addEnchant(enchantment, 1, false);
        return this;
    }

    /**
     * Enchant the itemStack regularly
     * @param enchantment the enchantment
     * @param level the level of the enchantment
     * @return the builder for chaining
     */
    public ItemEditor enchant(Enchantment enchantment, int level) {
        if (this.hasNoItemMeta) return this;
        this.meta.addEnchant(enchantment, level, false);
        return this;
    }

    /**
     * Enchant the itemStack regularly
     * @param enchantment the enchantment
     * @param level the level of the enchantment
     * @param ignoreEnchantRestriction ignore the enchant restriction or not (max level depends on the enchantment)
     * @return the builder for chaining
     */
    public ItemEditor enchant(Enchantment enchantment, int level, boolean ignoreEnchantRestriction) {
        if (this.hasNoItemMeta) return this;
        this.meta.addEnchant(enchantment, level, ignoreEnchantRestriction);
        return this;
    }

    /**
     * Set the amount of items to a specific provided amount
     * <p>
     * guaranteed to fail and return if over a stack
     * @param amount the provided amount
     * @return the builder for chaining
     */
    public ItemEditor setAmount(int amount) {
        this.button.setAmount(amount);
        return this;
    }

    /**
     * add an amount of items to a specific provided amount
     * <p>
     * guaranteed to fail and return if over a stack
     * @param amount the provided amount
     * @return the builder for chaining
     */
    public ItemEditor addAmount(int amount) {
        this.button.setAmount(button.getAmount() + amount);
        return this;
    }

    public ItemEditor setCustomModelData(Integer customModelData) {
        if (this.hasNoItemMeta) return this;
        this.meta.setCustomModelData(customModelData);
        return this;
    }

    public ItemEditor setAction(@NotNull ItemResponse event) {
        this.clickAction = event;
        return this;
    }

    /**
     * Calling this method means you're finally done.
     * <p>
     * and also that you want the new itemStack as you edited everything you need
     * @return the new menu itemStack
     */
    @CanIgnoreReturnValue
    @SuppressWarnings("UnusedReturnValue")
    public MenuItem done() {
        this.item.setItemMeta(meta);
        button.stack = item;
        button.clickAction = clickAction;
        return button;
    }
}
