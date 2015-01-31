package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.network.client.ConnectionState;
import com.ocdsoft.bacta.engine.utils.UnsignedUtil;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.TerminateReason;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

@SoeController(handles = {UdpPacketType.cUdpPacketTerminate})
public class TerminateController extends BaseSoeController {

    private static final Logger logger = LoggerFactory.getLogger(TerminateController.class);

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {

        long connectionID = UnsignedUtil.getUnsignedInt(buffer);
        TerminateReason reason = TerminateReason.values()[buffer.getShort()];

        connection.setState(ConnectionState.DISCONNECTED);

        logger.debug("Client disconnected: " + connection.getClass().getSimpleName() + " " + connection.getRemoteAddress() + " Connection: " + connectionID + " Reason: " + reason.getReason());
    }

}
