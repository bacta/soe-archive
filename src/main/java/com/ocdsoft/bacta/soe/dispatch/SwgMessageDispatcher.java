package com.ocdsoft.bacta.soe.dispatch;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

public interface SwgMessageDispatcher {
    void dispatch(byte priority, int opcode, SoeUdpConnection client, ByteBuffer buffer);
}
