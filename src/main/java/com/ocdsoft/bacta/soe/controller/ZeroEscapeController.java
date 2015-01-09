package com.ocdsoft.bacta.soe.controller;

import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;

import java.nio.ByteBuffer;

/**
 * Created by kburkhardt on 1/9/15.
 */

@SoeController(handles = {UdpPacketType.cUdpPacketZeroEscape})
@Singleton
public class ZeroEscapeController implements SoeMessageController {

    @Override
    public void handleIncoming(SoeUdpConnection client, ByteBuffer data) throws Exception {

    }

}
