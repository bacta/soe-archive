package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.swg.network.soe.SoeController;
import com.ocdsoft.bacta.swg.network.soe.buffer.SoeByteBuf;
import com.ocdsoft.bacta.swg.network.soe.client.SoeUdpClient;
import com.ocdsoft.bacta.swg.network.soe.message.AckMessage;

@SoeController(opcode = 0x15, handles = AckMessage.class)
public class AckController extends SoeMessageController {

    @Override
    public void handleIncoming(SoeUdpClient client, SoeByteBuf buffer) {

        short sequenceNum = buffer.readShortBE();
        client.processAck(sequenceNum);
    }
}
