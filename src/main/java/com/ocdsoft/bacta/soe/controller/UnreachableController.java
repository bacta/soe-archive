package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.TerminateReason;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

@SoeController(handles = {UdpPacketType.cUdpPacketUnreachableConnection})
public class UnreachableController extends BaseSoeController {

    private static final Logger logger = LoggerFactory.getLogger(UnreachableController.class);

    @Override
    public void handleIncoming(byte zeroByte, UdpPacketType type, SoeUdpConnection connection, ByteBuffer buffer) {
        connection.terminate(TerminateReason.OTHERSIDETERMINATED, true);
    }
}
