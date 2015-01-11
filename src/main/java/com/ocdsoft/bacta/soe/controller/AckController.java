package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

@SoeController(handles = {UdpPacketType.cUdpPacketAck1})
public class AckController implements SoeMessageController {

    private static final Logger logger = LoggerFactory.getLogger(AckController.class);

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) throws Exception {
        short sequenceNum = buffer.getShort();
        //client.setClientSequenceNumber(sequenceNum);
        logger.info("Not fully implemented");
    }
}
