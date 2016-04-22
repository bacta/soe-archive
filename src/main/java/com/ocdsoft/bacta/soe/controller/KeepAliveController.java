package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;

import java.nio.ByteBuffer;

@SoeController(handles = {UdpPacketType.cUdpPacketKeepAlive})
public class KeepAliveController extends BaseSoeController {

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {
        connection.updateLastActivity();
    }

}
