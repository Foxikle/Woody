package me.flame.menus.events;

import me.flame.menus.menu.contents.BukkitContents;
import me.flame.menus.menu.pagination.IndexedPagination;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import org.jetbrains.annotations.NotNull;

/**
 * Called right before a paginated menu's current page has switched back or forward.
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class PageChangeEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;

    private final IndexedPagination menu;
    private final BukkitContents oldPage, newPage;
    private final int oldIndex, newIndex;

    public PageChangeEvent(IndexedPagination menu,
                           BukkitContents oldPage,
                           BukkitContents newPage,
                           Player player,
                           int oldIndex,
                           int newIndex) {
        super(player);
        this.menu = menu;
        this.oldPage = oldPage;
        this.newPage = newPage;
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public IndexedPagination getMenu() {
        return menu;
    }

    public BukkitContents getOldPage() {
        return oldPage;
    }

    public BukkitContents getNewPage() {
        return newPage;
    }

    public int getOldIndex() {
        return oldIndex;
    }

    public int getNewIndex() {
        return newIndex;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
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
