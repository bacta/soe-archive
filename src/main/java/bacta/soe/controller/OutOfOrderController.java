package bacta.soe.controller;

import bacta.buffer.BactaBuffer;
import bacta.soe.SoeController;
import bacta.soe.client.SoeUdpClient;
import bacta.soe.message.OutOfOrderMessage;

@SoeController(opcode = 0x11, handles = OutOfOrderMessage.class)
public class OutOfOrderController extends SoeMessageController {

    @Override
    public void handleIncoming(SoeUdpClient client, BactaBuffer buffer) {

        short sequenceNum = buffer.readShortBE();
        //client.setClientSequenceNumber(sequenceNum);
        logger.info("Not fully implemented");
    }
}
