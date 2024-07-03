package me.flame.menus.items;

import org.bukkit.Sound;

/**
 * A good wrapper for the sound of the menu item click action,
 * @param volume how loud the sound is
 * @param pitch the pitch of the sound
 * @param sound the actual sound itself.
 */
public record ClickSound(float volume, float pitch, Sound sound) {
    public ClickSound(final Sound sound) {
        this(50, 1, sound);
    }
}
