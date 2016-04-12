package com.ocdsoft.bacta.soe.dispatch;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

public interface GameNetworkMessageDispatcher<T> {
    void dispatch(short priority, int opcode, SoeUdpConnection client, T message);
}
