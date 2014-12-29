package com.ocdsoft.bacta.soe.controller;


import com.ocdsoft.bacta.engine.buffer.BactaBuffer;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.client.SoeUdpClient;
import com.ocdsoft.bacta.soe.message.PingMessage;
import com.ocdsoft.bacta.soe.message.ping.PongMessage;

@SoeController(opcode = 0x6, handles = PingMessage.class)
public class PingController extends SoeMessageController {

    @Override
    public void handleIncoming(SoeUdpClient client, BactaBuffer buffer) {

        PongMessage pong = new PongMessage(buffer.readShort(), buffer.readByte());
        client.sendMessage(pong);

    }

}
