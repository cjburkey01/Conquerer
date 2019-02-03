package com.cjburkey.conquerer.util.property;

/**
 * Created by CJ Burkey on 2019/02/03
 */
@FunctionalInterface
public interface IListener<T> {

    void onChange(T oldValue, T newValue);

}
