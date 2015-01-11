package com.ocdsoft.bacta.soe.controller;

import com.google.inject.Inject;
import com.ocdsoft.bacta.engine.utils.UnsignedUtil;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;

import java.nio.ByteBuffer;

@SoeController(handles = {UdpPacketType.cUdpPacketMulti})
public class MultiController implements SoeMessageController {

    private final SoeMessageRouter soeMessageRouter;

    @Inject
    public MultiController(final SoeMessageRouter soeMessageRouter) {
        this.soeMessageRouter = soeMessageRouter;
    }

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {

        short length = UnsignedUtil.getUnsignedByte(buffer);

        while (buffer.remaining() >= length) {

            if (length == 0xFF) {
                if (UnsignedUtil.getUnsignedByte(buffer) == 0x01) {
                    length += UnsignedUtil.getUnsignedByte(buffer);
                }
            }

            ByteBuffer slicedMessage = buffer.slice();
            slicedMessage.limit(length - 2);

            soeMessageRouter.routeMessage(connection, slicedMessage);

            if (!buffer.hasRemaining()) {
                break;
            }

            length = UnsignedUtil.getUnsignedByte(buffer);
        }
    }

}
