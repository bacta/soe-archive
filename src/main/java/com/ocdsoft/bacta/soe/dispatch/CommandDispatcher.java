package com.ocdsoft.bacta.soe.dispatch;

import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.network.dispatch.MessageDispatcher;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

public interface CommandDispatcher<T, U> extends MessageDispatcher {
    void dispatchCommand(SoeUdpConnection connection, T message, U invoker);
}
