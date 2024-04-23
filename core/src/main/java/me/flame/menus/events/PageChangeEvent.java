package me.flame.menus.events;

import lombok.Getter;
import lombok.Setter;
import me.flame.menus.menu.ItemData;
import me.flame.menus.menu.PaginatedMenu;

import me.flame.menus.util.Option;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import org.jetbrains.annotations.NotNull;

/**
 * Called right before a paginated menu's current page has switched back or forward.
 * @since 2.0.0
 */
@Getter
public class PageChangeEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Setter
    private boolean cancelled = false;

    private final PaginatedMenu menu;
    private final Option<ItemData> oldPage, newPage;

    private final int currentPageNumber, oldPageNumber;

    public PageChangeEvent(PaginatedMenu menu,
                           Option<ItemData> oldPage,
                           Option<ItemData> newPage,
                           Player player,
                           int currentPageNumber,
                           int oldPageNumber) {
        super(player);
        this.menu = menu;
        this.oldPage = oldPage;
        this.newPage = newPage;
        this.currentPageNumber = currentPageNumber;
        this.oldPageNumber = oldPageNumber;
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
