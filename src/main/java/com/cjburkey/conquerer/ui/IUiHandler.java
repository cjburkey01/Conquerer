package com.cjburkey.conquerer.ui;

/**
 * Created by CJ Burkey on 2019/02/04
 */
public interface IUiHandler {

    void add(UiComponent component);

    int indexOf(UiComponent component);

    void rem(int componentId);

    default void rem(UiComponent component) {
        int at = indexOf(component);
        if (at >= 0 && at < getCount()) rem(at);
    }

    int getCount();

    UiComponent get(int componentId);

    default void updateChildren() {
    }

}
