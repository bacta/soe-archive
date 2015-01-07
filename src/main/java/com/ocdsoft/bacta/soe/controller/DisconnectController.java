package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.buffer.BactaBuffer;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.Disconnect;

@SoeController(opcode = 0x5, handles = Disconnect.class)
public class DisconnectController extends SoeMessageController {

    @Override
    public void handleIncoming(SoeUdpConnection client, BactaBuffer buffer) {
        long connectionID = buffer.readUnsignedInt();
        byte reasonID = (byte) buffer.readShort();

        client.disconnect();

        logger.debug("Client disconnected: " + client.getClass().getSimpleName() + " " + client.getRemoteAddress() + " Connection: " + connectionID + " Reason: " + Disconnect.reasons.get(reasonID));
    }

}
