package bacta.soe.controller;

import bacta.buffer.BactaBuffer;
import bacta.soe.SoeController;
import bacta.soe.client.SoeUdpClient;
import bacta.soe.message.Disconnect;

@SoeController(opcode = 0x5, handles = Disconnect.class)
public class DisconnectController extends SoeMessageController {

    @Override
    public void handleIncoming(SoeUdpClient client, BactaBuffer buffer) {
        long connectionID = buffer.readUnsignedInt();
        byte reasonID = (byte) buffer.readShort();

        client.disconnect();

        logger.debug("Client disconnected: " + client.getClass().getSimpleName() + " " + client.getRemoteAddress() + " Connection: " + connectionID + " Reason: " + Disconnect.reasons.get(reasonID));
    }

}
