package me.flame.menus.builders.items;

import me.flame.menus.items.ClickSound;
import me.flame.menus.items.ItemResponse;
import me.flame.menus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class BaseItemBuilder {
    protected final ItemStack item;

    protected final ItemMeta meta;

    protected final boolean hasNoItemMeta;

    protected boolean excludeNbt;

    protected ClickSound sound;

    BaseItemBuilder(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    BaseItemBuilder(@NotNull ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
        this.hasNoItemMeta = this.meta == null;
    }

    /**
     * Build the item into a new ItemStack.
     * @return the new ItemStack
     */
    public ItemStack build() {
        this.item.setItemMeta(meta);
        return item;
    }

    /**
     * Build the item into a new MenuItem.
     * @return the new MenuItem
     */
    public MenuItem buildItem() {
        return this.clickable(null);
    }

    /**
     * Build the item into a new MenuItem with the provided Click Event.
     * @param event the event
     * @return the new MenuItem
     */
    public MenuItem clickable(ItemResponse event) {
        MenuItem item = MenuItem.of(build(), event, excludeNbt);
        item.setSound(sound);
        return item;
    }

    /**
     * Build the item into a new MenuItem with the provided Click Event.
     * @param event the event
     * @return the new MenuItem
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "3.1.0")
    public MenuItem buildItem(ItemResponse event) {
        return this.clickable(event);
    }
}
