package me.flame.menus.events;

import me.flame.menus.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import org.jetbrains.annotations.NotNull;

public class BeforeAnimatingEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Menu menu;
    private boolean cancelled;

    public BeforeAnimatingEvent(Player player, Menu menu) {
        super(player);
        this.menu = menu;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
