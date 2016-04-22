package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.dispatch.GameNetworkMessageDispatcher;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.dispatch.SoeMessageDispatcher;
import com.ocdsoft.bacta.soe.dispatch.ClasspathControllerLoader;

import java.nio.ByteBuffer;

public interface SoeMessageController {
    void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) throws Exception;
    void setSoeMessageDispatcher(SoeMessageDispatcher soeMessageDispatcher);
    void setGameNetworkMessageDispatcher(GameNetworkMessageDispatcher gameNetworkMessageDispatcher);
}
