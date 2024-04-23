package me.flame.menus.events;

import lombok.Getter;
import lombok.Setter;
import me.flame.menus.menu.IMenu;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import org.jetbrains.annotations.NotNull;

@Setter @Getter
public class BeforeAnimatingEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final IMenu menu;
    private boolean cancelled;

    public BeforeAnimatingEvent(Player player, IMenu menu) {
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
}
