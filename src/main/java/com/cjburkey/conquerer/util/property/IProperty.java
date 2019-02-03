package com.cjburkey.conquerer.util.property;

/**
 * Created by CJ Burkey on 2019/02/03
 */
public interface IProperty<T> extends IReadonlyProperty<T> {

    void set(T newValue);

    void listen(IListener<T> listener);

    void clearListeners();

}
