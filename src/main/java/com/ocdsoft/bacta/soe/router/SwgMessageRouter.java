package com.ocdsoft.bacta.soe.router;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

public interface SwgMessageRouter {
    void routeMessage(byte priority, int opcode, SoeUdpConnection client, ByteBuffer buffer);
}
