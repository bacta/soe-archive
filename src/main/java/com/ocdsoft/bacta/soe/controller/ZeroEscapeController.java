package com.ocdsoft.bacta.soe.controller;

import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by kburkhardt on 1/9/15.
 */

@Singleton
@SoeController(handles = {UdpPacketType.cUdpPacketZeroEscape})
public class ZeroEscapeController extends BaseSoeController {

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {

        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int opcode = buffer.getInt();

        gameNetworkMessageDispatcher.dispatch(zeroByte, opcode, connection, buffer.slice().order(ByteOrder.LITTLE_ENDIAN));
    }

}
