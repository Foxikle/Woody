package me.flame.menus.builders.items;

import me.flame.menus.components.nbt.ItemNbt;
import me.flame.menus.items.ClickSound;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

@SuppressWarnings({ "unused", "deprecation" })
public class ItemBuilder extends BaseItemBuilder {
    static final ItemFlag[] FLAGS = ItemFlag.values();



    ItemBuilder(Material material, int amount) {
        super(new ItemStack(material, amount));
    }

    ItemBuilder(@NotNull ItemStack item) {
        super(item);
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material, 1);
    }

    public static ItemBuilder of(Material material, int amount) {
        return new ItemBuilder(material, amount);
    }

    public static ItemBuilder of(ItemStack item) {
        return new ItemBuilder(item);
    }

    /**
     * Sets the glow effect on the item.
     *
     * @param  glow  true to add enchantment and hide it, false to remove enchantment and show it
     * @apiNote Will hide the enchantments by default.
     * @return       the builder for chaining
     */
    public ItemBuilder glow(boolean glow) {
        // add enchantment and hide it if "glow" is true
        if (this.hasNoItemMeta) return this;
        if (!glow) {
            this.meta.removeEnchant(Enchantment.DURABILITY);
            this.meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            return  this;
        }
        this.meta.addEnchant(Enchantment.DURABILITY, 1, true);
        this.meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    /**
     * Sets the amount of the item.
     * @param amount the amount to set
     * @return the builder for chaining
     */
    public ItemBuilder amount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    /**
     * Sets the name of the itemStack to whatever the provided name is.
     * @param name the new name
     * @return the builder for chaining
     */
    public ItemBuilder setName(String name) {
        if (this.hasNoItemMeta) return this;
        this.meta.setDisplayName(translateAlternateColorCodes('&', name));
        return this;
    }

    /**
     * Sets the name of the itemStack to whatever the provided name is.
     * @param name the new name
     * @return the builder for chaining
     */
    public ItemBuilder name(Component name) {
        if (this.hasNoItemMeta) return this;
        meta.displayName(name);
        return this;
    }

    /**
     * Sets the lore of the itemStack to whatever the provided lore is.
     * @param lore the new lore
     * @return the builder for chaining
     */
    public ItemBuilder setLore(String... lore) {
        return this.setLore(List.of(lore));
    }

    /**
     * Sets the lore of the itemStack to whatever the provided lore is.
     * @param lore the new lore
     * @return the builder for chaining
     */
    public ItemBuilder setLore(List<String> lore) {
        if (this.hasNoItemMeta) return this;
        this.meta.setLore(lore);
        return this;
    }

    /**
     * Sets the lore of the itemStack to whatever the provided lore is.
     * @param lore the new lore
     * @return the builder for chaining
     */
    public ItemBuilder lore(Component... lore) {
        return lore(List.of(lore));
    }

    /**
     * Sets the lore of the itemStack to whatever the provided lore is.
     * @param lore the new lore
     * @return the builder for chaining
     */
    public ItemBuilder lore(List<Component> lore) {
        if (this.hasNoItemMeta) return this;
        meta.lore(lore);
        return this;
    }

    /**
     * Enchant the itemStack with the provided enchantment
     * @param enchant the enchantment to enchant the itemStack with
     * @return the builder for chaining
     */
    public ItemBuilder enchant(Enchantment enchant) {
        if (this.hasNoItemMeta) return this;
        this.meta.addEnchant(enchant, 1, false);
        return this;
    }

    /**
     * Enchant the itemStack with the provided enchantment
     * @param enchant the enchantment to enchant the itemStack with
     * @param level the level of the enchantment
     * @return the builder for chaining
     */
    public ItemBuilder enchant(Enchantment enchant, int level) {
        if (this.hasNoItemMeta) return this;
        this.meta.addEnchant(enchant, level, false);
        return this;
    }

    /**
     * Enchant the itemStack with the provided enchantment
     * @param enchant the enchantment to enchant the itemStack with
     * @param level the level of the enchantment
     * @param ignore whether to ignore the enchantment restrictions
     * @return the builder for chaining
     */
    public ItemBuilder enchant(Enchantment enchant, int level, boolean ignore) {
        if (this.hasNoItemMeta) return this;
        this.meta.addEnchant(enchant, level, ignore);
        return this;
    }

    /**
     * Apply all the enchantments to the itemStack with the same level & ignore restrictions
     * @param level the level of the enchantment
     * @param ignore whether to ignore the enchantment restrictions
     * @param enchant the enchantments to apply
     * @return the builder for chaining
     */
    public ItemBuilder enchant(int level, boolean ignore, Enchantment... enchant) {
        if (this.hasNoItemMeta) return this;
        for (Enchantment enchantment : enchant)
            this.meta.addEnchant(enchantment, level, ignore);
        return this;
    }

    /**
     * Apply all the enchantments to the itemStack with the same level
     * @param level the level of the enchantment
     * @param enchant the enchantments to apply
     * @return the builder for chaining
     */
    public ItemBuilder enchant(int level, Enchantment... enchant) {
        if (this.hasNoItemMeta) return this;
        for (Enchantment enchantment : enchant)
            this.meta.addEnchant(enchantment, level, false);
        return this;
    }

    /**
     * Set nbt of the item.
     * @param key the key
     * @param value the value.
     * @return the builder for chaining
     */
    public ItemBuilder setNbt(String key, String value) {
        if (this.hasNoItemMeta) return this;
        ItemNbt.setString(item, key, value);
        return this;
    }

    /**
     * Apply all the enchantments to the itemStack (level 1)
     * @param enchant the enchantments to apply
     * @return the builder for chaining
     */
    public ItemBuilder enchant(Enchantment... enchant) {
        if (this.hasNoItemMeta) return this;
        for (Enchantment enchantment : enchant)
            this.meta.addEnchant(enchantment, 1, false);
        return this;
    }

    /**
     * Set the itemStack to be unbreakable
     * @return the builder for chaining
     */
    public ItemBuilder unbreakable() {
        if (this.hasNoItemMeta) return this;
        this.meta.setUnbreakable(true);
        return this;
    }

    /**
     * Set the itemStack to be unbreakable or not
     * @param breakable whether the itemStack is unbreakable
     * @return the builder for chaining
     */
    public ItemBuilder unbreakable(boolean breakable) {
        if (this.hasNoItemMeta) return this;
        this.meta.setUnbreakable(breakable);
        return this;
    }

    /**
     * Add all the item flags to the item meta
     * @return the builder for chaining
     */
    public ItemBuilder allItemFlags() {
        if (this.hasNoItemMeta) return this;
        this.meta.addItemFlags(ItemFlag.values());
        return this;
    }

    /**
     * Adds an item flag to the item meta.
     * @param flag the flag to add
     * @return the updated item meta
     */
    public ItemBuilder addItemFlag(ItemFlag flag) {
        if (this.hasNoItemMeta) return this;
        this.meta.addItemFlags(flag);
        return this;
    }

    /**
     * Adds an item flag to the item meta.
     * @param flag the flag to add
     * @return the updated item meta
     */
    public ItemBuilder addItemFlags(ItemFlag... flag) {
        if (this.hasNoItemMeta) return this;
        this.meta.addItemFlags(flag);
        return this;
    }

    /**
     * Adds an item flag to the item meta.
     * @return the updated item meta
     */
    public ItemBuilder addAllItemFlags() {
        if (this.hasNoItemMeta) return this;
        this.meta.addItemFlags(FLAGS);
        return this;
    }

    /**
     * Adds an attribute modifier to the item meta.
     *
     * @param  attribute  the attribute to modify
     * @param  modifier   the modifier to apply
     * @return            the updated item meta
     */
    public ItemBuilder attributeModifier(Attribute attribute, AttributeModifier modifier) {
        if (this.hasNoItemMeta) return this;
        this.meta.addAttributeModifier(attribute, modifier);
        return this;
    }

    /**
     * Set the damage to the itemStack
     * @param d the damage
     * @return the builder for chaining
     */
    public ItemBuilder damage(int d) {
        if (this.hasNoItemMeta || !(meta instanceof Damageable)) return this;
        ((Damageable) meta).damage(d);
        return this;
    }

    /**
     * Exclude the default NBT, if it has no NBT then you can do stuff like stack the items
     * @param exclude to exclude NBT or not
     * @return the builder for chaining
     */
    public ItemBuilder excludeNbt(boolean exclude) {
        this.excludeNbt = exclude;
        return this;
    }

    public ItemBuilder editMeta(@NotNull Consumer<ItemMeta> meta) {
        meta.accept(this.meta);
        return this;
    }

    public ItemBuilder setSound(ClickSound sound) {
        this.sound = sound;
        return this;
    }
}