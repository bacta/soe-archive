package com.ocdsoft.bacta.soe.service;

/**
 * Created by kburkhardt on 2/5/15.
 */
public interface ContainerService<T> {
    void createObjectContainer(T object);

    <U> U getSlottedObject(T container, String slotName);

    int mayAdd(T container, T item);

    int checkDepth(T object);

    int removeFromContainer(T item);

    int transferItemToContainer(T container, T item);

    boolean transferItemToWorld(T item);

    int getTotalVolumeLimitedByParents(T volumeContainer);
}
