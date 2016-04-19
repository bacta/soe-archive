package com.ocdsoft.bacta.soe.factory;


/**
 * Created by kyle on 4/10/2016.
 */
public interface CommandMessageFactory<T> {
    public T create(Class<? extends T> messageClass, T parentMessage);
}
