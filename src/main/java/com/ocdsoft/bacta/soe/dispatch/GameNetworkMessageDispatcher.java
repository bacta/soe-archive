package com.ocdsoft.bacta.soe.dispatch;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

public interface GameNetworkMessageDispatcher {
    void dispatch(short priority, int opcode, SoeUdpConnection client, ByteBuffer message);
}
