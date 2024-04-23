package me.flame.menus.menu.actions;

import lombok.extern.log4j.Log4j2;
import me.flame.menus.events.BeforeAnimatingEvent;
import me.flame.menus.events.MenuCloseEvent;
import me.flame.menus.events.PageChangeEvent;
import me.flame.menus.menu.IMenu;
import me.flame.menus.menu.ItemData;
import me.flame.menus.menu.Menu;
import me.flame.menus.menu.PaginatedMenu;
import me.flame.menus.util.Option;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

@Log4j2
@SuppressWarnings("unused")
public class Actions {
    private final Map<Class<?>, List<Action<? extends Event, ? extends Event>>> actions = new HashMap<>(5);
    private final Menu menu;

    private static final Set<Class<?>> reflectionCaches = new HashSet<>(3);

    public Actions(Menu menu) {
        this.menu = menu;
    }

    public <T extends Event, E extends Event> void addInventoryEvent(Class<E> eventClass, Action<E, T> event) {
        if (!reflectionCaches.contains(eventClass) && !eventClass.isAssignableFrom(Event.class)) {
            if (!eventClass.isAssignableFrom(Event.class)) {
                throw new IllegalArgumentException("Found mismatch; Actions only supports Event and it's extenders." + "\nClass Name: " + eventClass.getSimpleName());
            } else {
                reflectionCaches.add(eventClass);
            }
        }
        var list = actions.get(eventClass);
        if (list == null) {
            actions.put(eventClass, new ArrayList<>(4));
            list = actions.get(eventClass);
        }
        list.add(event);
    }

    public <T extends Event, E extends Event> void addInventoryEvent(Class<T> eventClass, List<Action<E, T>> event) {
        var list = actions.get(eventClass);
        if (list == null) {
            actions.put(eventClass, new ArrayList<>(4));
            list = actions.get(eventClass);
        }
        list.addAll(event);
    }

    public <T extends Event> void removeInventoryEvents(Class<T> eventClass) {
        actions.remove(eventClass);
    }

    public void addDefaultClickAction(Consumer<InventoryClickEvent> eventAction) {
        addInventoryEvent(
            InventoryClickEvent.class,
            Action.createOnlyPredicate(
                    eventAction,
                    InventoryClickEvent.class,
                    (e) -> e,
                    (e, t) -> true)
        );
    }

    public void addTopClickAction(Consumer<InventoryClickEvent> eventAction) {
        addInventoryEvent(
            InventoryClickEvent.class,
            Action.createOnlyPredicate(
                eventAction,
                InventoryClickEvent.class,
                (e) -> e,
                (e, function) -> {
                    Inventory clickedInventory = e.getClickedInventory();
                    return (clickedInventory != null) && clickedInventory.equals(e.getView().getBottomInventory());
                }
            )
        );
    }

    public void addOpenClickAction(Consumer<InventoryOpenEvent> eventAction) {
        addInventoryEvent(
            InventoryOpenEvent.class,
            Action.createOnlyPredicate(eventAction, InventoryOpenEvent.class, (e) -> e, (event, function) -> true)
        );
    }

    public void addCloseClickAction(Consumer<MenuCloseEvent> eventAction) {
        addInventoryEvent(
            MenuCloseEvent.class,
            Action.createOnlyPredicate(
                    eventAction,
                    InventoryCloseEvent.class,
                    (event) -> new MenuCloseEvent(((Player) event.getPlayer())),
                    (event, function) -> true)
        );
    }

    public void addDragClickAction(Consumer<InventoryDragEvent> eventAction) {
        addInventoryEvent(
            InventoryDragEvent.class,
            Action.createOnlyPredicate(
                    eventAction,
                    InventoryDragEvent.class,
                    (e) -> e,
                    (event, function) -> true)
        );
    }

    public void addBottomClickAction(Consumer<InventoryClickEvent> eventAction) {
        addInventoryEvent(
            InventoryClickEvent.class,
            Action.createOnlyPredicate(
                    eventAction,
                    InventoryClickEvent.class,
                    (e) -> e,
                    (e, func) -> {
                        Inventory clickedInventory = e.getClickedInventory();
                        return (clickedInventory != null) &&  clickedInventory.equals(e.getView().getBottomInventory());
                    })
        );
    }

    public void addAnimateAction(Consumer<BeforeAnimatingEvent> eventAction) {
        addInventoryEvent(
            BeforeAnimatingEvent.class,
            Action.createOnlyPredicate(
                    eventAction,
                    InventoryOpenEvent.class,
                    (event) -> new BeforeAnimatingEvent((Player) event.getPlayer(), (Menu) event.getInventory().getHolder()),
                    (e, func) -> e.getViewers().isEmpty())
        );
    }

    public void addOnPageAction(Consumer<PageChangeEvent> eventAction) {
        addInventoryEvent(
            PageChangeEvent.class,
            Action.createOnlyPredicate(
                    eventAction,
                    InventoryClickEvent.class,
                    (event) -> {
                        // This cast is safe because the condition is checked before hand
                        @NotNull
                        PaginatedMenu menu = (PaginatedMenu) Objects.requireNonNull(event.getInventory().getHolder());

                        int oldNumber = menu.getPageNumber(), newNumber = oldNumber + (menu.getNextItemSlot() == event.getSlot()
                                ? 1 : -1);
                        Option<ItemData> oldPage = menu.getOptionalData(), currentPage = menu.getOptionalPage(newNumber);
                        return new PageChangeEvent(
                                menu, oldPage, currentPage, (Player) event.getWhoClicked(), oldNumber, newNumber

                        );
                    },
                    (e, func) -> menu instanceof PaginatedMenu && ((PaginatedMenu) menu).getNextItemSlot() == e.getSlot() || ((PaginatedMenu) menu).getPreviousItemSlot() == e.getSlot()
            )
        );
    }

    public <E extends Event, T extends Event> boolean executeInventoryEventBy(Class<E> type, Class<T> executionArea, T event, IMenu menu) {
        List<Action<? extends Event, ? extends Event>> actions = this.actions.get(type);
        if (actions == null) return false;

        return isCancelled(executionArea, event, actions, false, menu, null);
    }

    public <E extends Event, T extends Event> boolean executeInventoryEventBy(Class<T> executionArea, T event, IMenu menu) {
        Collection<List<Action<? extends Event, ? extends Event>>> values = this.actions.values();
        boolean isCancelled = false;

        for (List<Action<? extends Event, ? extends Event>> actionList : values) {
            isCancelled = isCancelled(executionArea, event, actionList, isCancelled, menu, null);
        }
        return isCancelled;
    }

    public <E extends Event, T extends Event> boolean executeInventoryEventBy(Class<T> executionArea, T event, IMenu menu, Class<E>... exclude) {
        Collection<List<Action<? extends Event, ? extends Event>>> values = this.actions.values();
        Set<Class<E>> classes = Set.of(exclude);
        boolean isCancelled = false;

        for (List<Action<? extends Event, ? extends Event>> actionList : values) {
            isCancelled = isCancelled(executionArea, event, actionList, isCancelled, menu, classes);
        }
        return isCancelled;
    }

    private static <E extends Event, T extends Event> boolean isCancelled(Class<T> executionArea, T event, List<Action<? extends Event, ? extends Event>> actionList, boolean isCancelled, IMenu menu, Set<Class<E>> classes) {
        for (Action<? extends Event, ? extends Event> action : actionList) {
            if (executionArea != action.getExecuteOnSupportedEvent()) continue;
            if (isCancelled && !action.isIgnoreCancelled()) continue;

            @SuppressWarnings("unchecked")
            Action<E, T> castedAction = (Action<E, T>) action;

            if (castedAction.executeWhen.test(event, menu)) {
                E castedEvent = castedAction.customEventSupplier.apply(event);
                if (classes.contains(castedEvent.getClass())) continue;
                castedAction.eventConsumer.accept(castedEvent);
            }
            if (action.getResult() == Event.Result.DENY) {
                if (event instanceof Cancellable) ((Cancellable) event).setCancelled(true);
                isCancelled = true;
            }
        }
        return isCancelled;
    }

    public <T> boolean hasActionList(Class<T> actionType) {
        return actions.containsKey(actionType);
    }
}