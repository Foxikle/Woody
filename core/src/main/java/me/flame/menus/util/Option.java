package me.flame.menus.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings({ "unchecked", "unused" })
public class Option<T> {
    private static final Option<?> NONE = new Option<>(null);

    private final T value;

    public Option(final T value) {
        this.value = value;
    }

    public static <E> Option<E> some(E value) {
        return value != null ? new Option<>(value) : none();
    }

    public static <E> Option<E> someIf(E value, boolean condition) {
        return value != null && condition ? new Option<>(value) : none();
    }

    public static <E> Option<E> fromOptional(Optional<E> optional) {
        return some(optional.orElse(null));
    }

    public Optional<T> toOptional(Optional<T> optional) {
        return Optional.ofNullable(value);
    }

    public static <E> Option<E> none() { return (Option<E>) NONE; }

    public void peek(Consumer<T> actionIfPresent) {
        if (this.isNone()) return;
        actionIfPresent.accept(value);
    }

    public T orElse(T defaultValue) {
        return this.isNone() ? defaultValue : this.value;
    }

    public <E extends Throwable> T orThrow(E throwable) throws E {
        if (this.isSome()) return value;
        throw throwable;
    }

    public <E extends Throwable> T orGetThrow(Supplier<E> throwable) throws E {
        if (value != null) return value;
        throw throwable.get();
    }

    public T orGet(Supplier<T> value) { return this.isNone() ? value.get() : this.value; }

    public Option<T> or(Option<T> otherValue) {
        return otherValue.isSome() ? otherValue : this;
    }

    public Option<T> filter(Predicate<T> filtering) {
        return isNone() || !filtering.test(value) ? none() : some(value);
    }

    public <E> Option<E> map(Function<T, E> mapper) {
        return this.isNone() ? none() : some(mapper.apply(value));
    }

    public <E> Option<E> flatMap(Function<T, Option<E>> mapper) {
        return this.isNone() ? none() : mapper.apply(value);
    }

    public boolean isSome() { return value != null; }

    public boolean isNone() { return value == null; }
}
