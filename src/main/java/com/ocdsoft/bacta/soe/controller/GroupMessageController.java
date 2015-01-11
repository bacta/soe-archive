package com.ocdsoft.bacta.soe.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.utils.UnsignedUtil;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

@Singleton
@SoeController(handles = {UdpPacketType.cUdpPacketGroup})
public class GroupMessageController implements SoeMessageController {

    private static final Logger logger = LoggerFactory.getLogger(GroupMessageController.class);

    private final SoeMessageRouter soeMessageRouter;

    @Inject
    public GroupMessageController(final SoeMessageRouter soeMessageRouter) {
        this.soeMessageRouter = soeMessageRouter;
    }

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {

        while (buffer.hasRemaining()) {

            int length = UnsignedUtil.getUnsignedByte(buffer);
            ByteBuffer slicedBuffer = buffer.slice();
            slicedBuffer.limit(buffer.position() + length);

            soeMessageRouter.routeMessage(connection, slicedBuffer);

            buffer.position(buffer.position() + length);
        }
    }
}
