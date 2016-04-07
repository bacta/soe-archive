package com.ocdsoft.bacta.soe.controller;

import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

@Singleton
@SoeController(handles = {UdpPacketType.cUdpPacketReliable1, UdpPacketType.cUdpPacketFragment1})
public class ReliableMessageController extends BaseSoeController {

    private static final Logger logger = LoggerFactory.getLogger(ReliableMessageController.class);

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {

        short sequenceNum = buffer.getShort();
        connection.sendAck(sequenceNum);

        if(type == UdpPacketType.cUdpPacketFragment1) {
            buffer = connection.addIncomingFragment(buffer);
        }

        if (buffer != null) {

            try {

                soeMessageDispatcher.dispatch(connection, buffer);

            } catch (Exception e) {
                logger.error("Unable to handle ZeroEscape", e);
            }

        }
    }
}
