package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;

import java.nio.ByteBuffer;

@SoeController(handles = {UdpPacketType.cUdpPacketAckAll1,UdpPacketType.cUdpPacketAckAll2, UdpPacketType.cUdpPacketAckAll3, UdpPacketType.cUdpPacketAckAll4})
public class AckAllController extends BaseSoeController {

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {

        short sequenceNum = buffer.getShort();
        connection.sendAckAll(sequenceNum);
    }
}
