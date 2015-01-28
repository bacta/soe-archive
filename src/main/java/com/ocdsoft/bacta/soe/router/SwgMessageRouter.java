package com.ocdsoft.bacta.soe.router;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

public interface SwgMessageRouter<Connection extends SoeUdpConnection> {
    void routeMessage(byte priority, int opcode, Connection client, ByteBuffer buffer);
}
