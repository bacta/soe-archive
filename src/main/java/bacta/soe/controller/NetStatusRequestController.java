package bacta.soe.controller;

import bacta.buffer.BactaBuffer;
import bacta.soe.SoeController;
import bacta.soe.client.SoeUdpClient;
import bacta.soe.message.NetStatusRequestMessage;
import bacta.soe.message.NetStatusServerResponse;

@SoeController(opcode = 0x7, handles = NetStatusRequestMessage.class)
public class NetStatusRequestController extends SoeMessageController {

    @Override
    public void handleIncoming(SoeUdpClient client, BactaBuffer buffer) {

		short clientTick = buffer.readShort();
//		int lastClientUpdateTime = buffer.readInt();
//		int averageClientUpdateTime = buffer.readInt();
//		int shortestClientUpdateTime = buffer.readInt();
//		int longestClientUpdateTime = buffer.readInt();
//		int lastServerUpdateTime = buffer.readInt();
//		long numClientPacketsSent = buffer.readLong();
//		long numClientPacketsReceived = buffer.readLong();
        NetStatusServerResponse outMessage = new NetStatusServerResponse(clientTick);
        client.sendMessage(outMessage);

    }

}
