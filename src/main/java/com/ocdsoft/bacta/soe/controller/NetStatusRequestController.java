package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.buffer.BactaBuffer;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.NetStatusRequestMessage;
import com.ocdsoft.bacta.soe.message.NetStatusServerResponse;

@SoeController(opcode = 0x7, handles = NetStatusRequestMessage.class)
public class NetStatusRequestController extends SoeMessageController {

    @Override
    public void handleIncoming(SoeUdpConnection client, BactaBuffer buffer) {

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
