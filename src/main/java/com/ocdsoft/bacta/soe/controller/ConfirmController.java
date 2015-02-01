package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;

import java.nio.ByteBuffer;

@SoeController(handles = {UdpPacketType.cUdpPacketConfirm})
public class ConfirmController extends BaseSoeController {

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {

        int connectionID = buffer.getInt();
        int sessionKey = buffer.getInt();
        byte crcLength = buffer.get();
        boolean useComp = BufferUtil.getBoolean(buffer);
        byte cryptMethod = buffer.get();
        int udpSize = buffer.getInt();

        connection.setId(connectionID);
        connection.setSessionKey(sessionKey);
        connection.setUdpSize(udpSize);

        connection.confirm();
    }
}
