package com.ocdsoft.bacta.soe.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.router.SwgMessageRouter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by kburkhardt on 1/9/15.
 */

@Singleton
@SoeController(handles = {UdpPacketType.cUdpPacketZeroEscape})
public class ZeroEscapeController implements SoeMessageController {

    private final SwgMessageRouter swgMessageRouter;

    @Inject
    public ZeroEscapeController(final SwgMessageRouter swgMessageRouter) {
        this.swgMessageRouter = swgMessageRouter;
    }

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {

        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int opcode = buffer.getInt();
        swgMessageRouter.routeMessage(zeroByte, opcode, connection, buffer);
    }

}
