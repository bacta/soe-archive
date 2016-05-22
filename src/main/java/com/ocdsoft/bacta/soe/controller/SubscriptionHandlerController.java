package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.message.Subscribable;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by kyle on 5/21/2016.
 */
public abstract class SubscriptionHandlerController<T extends GameNetworkMessage & Subscribable> implements GameNetworkMessageController<T> {

    private final Set<SoeUdpConnection> subscribers;

    public SubscriptionHandlerController() {
        subscribers = new HashSet<>();
    }

    @Override
    public final void handleIncoming(SoeUdpConnection connection, T message) throws Exception {
        handleIncomingInternal(connection, message);
        notifySubscribers(subscribers, message);
    }

    public final boolean subscribe(final SoeUdpConnection connection) {
        return subscribers.add(connection);
    }

    public final boolean unsubscribe(final SoeUdpConnection connection) {
        return subscribers.remove(connection);
    }

    public abstract void handleIncomingInternal(SoeUdpConnection connection, T message) throws Exception;
    protected abstract void notifySubscribers(Set<SoeUdpConnection> subscribers, T message);
}
