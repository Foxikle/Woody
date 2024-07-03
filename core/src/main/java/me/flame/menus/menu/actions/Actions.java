package me.flame.menus.menu.actions;

import me.flame.menus.events.BeforeAnimatingEvent;
import me.flame.menus.events.MenuCloseEvent;
import me.flame.menus.events.PageChangeEvent;
import me.flame.menus.menu.*;
import me.flame.menus.menu.pagination.IndexedPagination;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class Actions {
    private final Map<Class<?>, List<Action<? extends Event, ? extends Event>>> actions = new HashMap<>(5);
    private final Menu menu;

    public Actions(Menu menu) {
        this.menu = menu;

        // add the default inventory event classes
        actions.put(InventoryOpenEvent.class, new ArrayList<>(5));
        actions.put(InventoryCloseEvent.class, new ArrayList<>(5));
        actions.put(InventoryClickEvent.class, new ArrayList<>(5));
        actions.put(InventoryDragEvent.class, new ArrayList<>(5));
    }

    public <T extends Event, E extends Event> void addInventoryEvent(@NotNull Action<E, T> event) {
        this.actions.computeIfAbsent(event.executionArea(), (key) -> new ArrayList<>(5)).add(event);
    }

    public <T extends Event> void removeInventoryEvents(Class<T> eventClass) {
        actions.remove(eventClass);
    }

    public void addDefaultClickAction(Consumer<InventoryClickEvent> eventAction) {
        addInventoryEvent(
            Action.builder(InventoryClickEvent.class, InventoryClickEvent.class)
                    .customEvent(Function.identity())
                    .action(eventAction)
                    .build()
        );
    }

    public void addTopClickAction(Consumer<InventoryClickEvent> eventAction) {
        addInventoryEvent(
            Action.builder(InventoryClickEvent.class, InventoryClickEvent.class)
                    .customEvent(Function.identity())
                    .action(eventAction)
                    .executeIf((e, func) -> {
                        Inventory clickedInventory = e.getClickedInventory();
                        return (clickedInventory != null) && clickedInventory.equals(e.getView().getTopInventory());
                    })
                    .build()
        );
    }

    public void addOpenClickAction(Consumer<InventoryOpenEvent> eventAction) {
        addInventoryEvent(
            Action.builder(InventoryOpenEvent.class, InventoryOpenEvent.class)
                    .customEvent(Function.identity())
                    .action(eventAction)
                    .build()
        );
    }

    public void addCloseClickAction(Consumer<MenuCloseEvent> eventAction) {
        addInventoryEvent(
            Action.builder(MenuCloseEvent.class, InventoryCloseEvent.class)
                    .customEvent((event) -> new MenuCloseEvent(event.getView()))
                    .action(eventAction)
                    .build()
        );
    }

    public void addDragClickAction(Consumer<InventoryDragEvent> eventAction) {
        addInventoryEvent(
            Action.builder(InventoryDragEvent.class, InventoryDragEvent.class)
                    .customEvent(Function.identity())
                    .action(eventAction)
                    .build()
        );
    }

    public void addBottomClickAction(Consumer<InventoryClickEvent> eventAction) {
        addInventoryEvent(
            Action.builder(InventoryClickEvent.class, InventoryClickEvent.class)
                    .customEvent(Function.identity())
                    .action(eventAction)
                    .executeIf((e, func) -> {
                        Inventory clickedInventory = e.getClickedInventory();
                        return (clickedInventory != null) &&  clickedInventory.equals(e.getView().getBottomInventory());
                    })
                    .build()
        );
    }

    public void addAnimateAction(Consumer<BeforeAnimatingEvent> eventAction) {
        addInventoryEvent(
            Action.builder(BeforeAnimatingEvent.class, InventoryOpenEvent.class)
                    .customEvent((event) ->
                            new BeforeAnimatingEvent((Player) event.getPlayer(), (Menu) event.getInventory().getHolder()))
                    .action(eventAction)
                    .executeIf((e, func) -> e.getViewers().isEmpty())
                    .build()
        );
    }

    public void addOnPageAction(Consumer<PageChangeEvent> eventAction) {
        addInventoryEvent(
            Action.builder(PageChangeEvent.class, InventoryClickEvent.class)
                    .customEvent((event) -> ((IndexedPagination) menu).createPageEvent(event))
                    .action(eventAction)
                    .executeIf((e, func) -> menu instanceof IndexedPagination pagination && pagination.pageChangingAction(e.getSlot()))
                    .build()
        );
    }

    /**
     *
     * @param event the event to supply the custom event
     * @param menu the menu required to execute the action
     * @return the cancelled events (if cancelled then it doesn't return the ignore cancel events)
     * @param <E> the custom event
     * @param <T> the event that has been provided
     */
    public <E extends Event, T extends Event> Set<Class<?>> executeInventoryEventBy(@NotNull T event, Menu menu) {
        @SuppressWarnings("unchecked") // java is kinda stupid, don't mind java
        Class<T> executionArea = (Class<T>) event.getClass();
        return this.processExecution(executionArea, event, menu);
    }

    @SuppressWarnings("unchecked")
    private <E extends Event, T extends Event> Set<Class<?>> processExecution(Class<T> executionArea, T event, Menu menu) {
        boolean cancelled = false;
        Set<Class<?>> cancelledEvents = new HashSet<>(3);
        for (Action<? extends Event, ? extends Event> unchecked : this.actions.get(executionArea)) {
            if (cancelled && !unchecked.ignoreCancelled()) {
                cancelledEvents.add(unchecked.getClass());
                continue;
            }

            Action<E, T> action = ((Action<E, T>) unchecked).copy();

            E executedEvent = checkExecution(event, menu, action);
            cancelled = executedEvent instanceof Cancellable cancellable && cancellable.isCancelled();

            checkCancellation(event, executedEvent, cancelledEvents, action);
        }
        return cancelledEvents;
    }

    private static <T extends Event, E extends Event> void checkCancellation(final T event, final E executedEvent, final Set<Class<?>> cancelledEvents, final Action<E, T> action) {
        if (event instanceof Cancellable cancellable
                && executedEvent instanceof Cancellable cancellableEvent
                && cancellableEvent.isCancelled()) {
            cancellable.setCancelled(true);
            cancelledEvents.add(cancellableEvent.getClass());
            action.executeCancellationEvent(executedEvent);
        }
    }

    private static <E extends Event, T extends Event> void checkCancellation(final T event, final E executedEvent, final Set<Class<?>> cancelledEvents) {
        if (event instanceof Cancellable cancellable
                && executedEvent instanceof Cancellable cancellableEvent
                && cancellableEvent.isCancelled()) {
            cancellable.setCancelled(true);
            cancelledEvents.add(cancellableEvent.getClass());
        }
    }

    @Nullable
    private static <E extends Event, T extends Event> E checkExecution(T event, Menu menu, Action<E, T> action) {
        if (action.canExecute(event, menu)) {
            E requiredEvent = action.createEvent(event);
            action.executeEvent(requiredEvent);
        }
        return null;
    }

    public <T> boolean hasActionList(Class<T> actionType) {
        return actions.containsKey(actionType);
    }
}