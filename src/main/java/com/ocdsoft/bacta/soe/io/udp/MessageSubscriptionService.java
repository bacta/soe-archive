package com.ocdsoft.bacta.soe.io.udp;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.message.Subscribable;

/**
 * Created by kyle on 5/21/2016.
 */
public interface MessageSubscriptionService {
    void onConnect(final SoeUdpConnection connection);
    void onDisconnect(final SoeUdpConnection connection);
    <T extends GameNetworkMessage & Subscribable> void subscribe(final SoeUdpConnection connection, final Class<T> messageClass);
    <T extends GameNetworkMessage & Subscribable> void unsubscribe(final SoeUdpConnection connection, final Class<T> messageClass);
}
