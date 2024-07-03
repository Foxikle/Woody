package me.flame.menus.items;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * @since 2.0.0
 */
public class SkullItemEditor extends ItemEditor {
    private final SkullMeta skullMeta;
    public SkullItemEditor(MenuItem item) {
        super(item);
        this.skullMeta = (SkullMeta) meta;
    }

    public SkullItemEditor setOwner(OfflinePlayer player) {
        skullMeta.setOwningPlayer(player);
        return this;
    }

    @Override
    public MenuItem done() {
        this.item.setItemMeta(skullMeta);
        button.stack = this.item;
        button.clickAction = clickAction;
        return button;
    }
}
