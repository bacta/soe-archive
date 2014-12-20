package bacta.soe.controller;


import bacta.buffer.BactaBuffer;
import bacta.soe.SoeController;
import bacta.soe.client.SoeUdpClient;
import bacta.soe.message.PingMessage;
import bacta.soe.message.ping.PongMessage;

@SoeController(opcode = 0x6, handles = PingMessage.class)
public class PingController extends SoeMessageController {

    @Override
    public void handleIncoming(SoeUdpClient client, BactaBuffer buffer) {

        PongMessage pong = new PongMessage(buffer.readShort(), buffer.readByte());
        client.sendMessage(pong);

    }

}
