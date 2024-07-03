package me.flame.menus.items;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @since 2.0.0
 */
public class SkullItemEditor extends ItemEditor {
    private final SkullMeta skullMeta;
    public SkullItemEditor(MenuItem item) {
        super(item);
        this.skullMeta = (SkullMeta) meta;
    }

    /**
     * Sets the texture applied to this Skull.
     * @param value The VALUE of the skin, not the signature. The value is a base64 string with the skin url, and some other relevant details.
     * @return This editor
     * @author Foxikle
     */
    public SkullItemEditor setTexture(@NotNull String value) {
        PlayerProfile profile = Bukkit.getServer().createProfile(UUID.randomUUID(), "player_head");
        profile.setProperty(new ProfileProperty("textures", value));
        skullMeta.setPlayerProfile(profile);
        return this;
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
