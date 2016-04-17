package com.ocdsoft.bacta.soe.dispatch;

import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.network.dispatch.MessageDispatcher;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

@Singleton
public interface CommandDispatcher<T> extends MessageDispatcher {
    void dispatchCommand(int opcode, SoeUdpConnection connection, ByteBuffer message, T invoker);
}
