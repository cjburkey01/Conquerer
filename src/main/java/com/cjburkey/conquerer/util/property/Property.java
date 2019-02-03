package com.cjburkey.conquerer.util.property;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Created by CJ Burkey on 2019/02/03
 */
public class Property<T> implements IProperty<T> {

    private final ObjectArrayList<IListener<T>> listeners = new ObjectArrayList<>();
    private T value;

    public Property() {
    }

    @SuppressWarnings("WeakerAccess")
    public Property(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        listeners.forEach(listener -> listener.onChange(value, newValue));
        value = newValue;
    }

    public void listen(IListener<T> listener) {
        listeners.add(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }

}
