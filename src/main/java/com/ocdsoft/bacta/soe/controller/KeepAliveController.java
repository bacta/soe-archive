package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.KeepAliveMessage;
import com.ocdsoft.bacta.soe.message.UdpPacketType;

import java.nio.ByteBuffer;

@SoeController(handles = {UdpPacketType.cUdpPacketKeepAlive})
public class KeepAliveController implements SoeMessageController {

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {

        KeepAliveMessage keepAliveMessage = new KeepAliveMessage(buffer.getShort(), buffer.get());
        connection.sendMessage(keepAliveMessage);

    }

}
