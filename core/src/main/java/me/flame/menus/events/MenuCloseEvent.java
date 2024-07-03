package me.flame.menus.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

public class MenuCloseEvent extends InventoryEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    private final InventoryCloseEvent.Reason reason;

    @NotNull
    public InventoryCloseEvent.Reason getReason() {
        return reason;
    }

    public MenuCloseEvent(InventoryView view) {
        this(view, InventoryCloseEvent.Reason.PLUGIN);
    }

    public MenuCloseEvent(InventoryView view, InventoryCloseEvent.Reason reason) {
        super(view);
        this.reason = reason;
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
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Player getPlayer() {
        return (Player) transaction.getPlayer();
    }
}
