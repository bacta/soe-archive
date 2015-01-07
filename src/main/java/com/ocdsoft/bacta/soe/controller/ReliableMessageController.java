package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.buffer.BactaBuffer;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.ReliableNetworkMessage;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;

import java.nio.ByteOrder;

@SoeController(opcode = 0x9, handles = ReliableNetworkMessage.class)
public class ReliableMessageController extends SoeMessageController {

    private SwgMessageRouter swgRouter;

    @Override
    public void setRouter(SoeMessageRouter soeRouter) {
        this.swgRouter = soeRouter.getSwgRouter();
    }

    @Override
    public void handleIncoming(SoeUdpConnection client, BactaBuffer buffer) {
        short sequenceNum = buffer.readShortBE();

        client.sendAck(sequenceNum);

        short priority = buffer.readShort();
        int opcode = 0;

        if (priority != 0x1900) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            opcode = buffer.readInt();
            swgRouter.routeMessage(opcode, client, buffer);

        } else {

            while (buffer.readableBytes() > 3) {

                int length = buffer.readUnsignedByte();

                BactaBuffer gameMessage = new BactaBuffer(buffer.slice(buffer.readerIndex(), length).order(ByteOrder.LITTLE_ENDIAN));

                priority = gameMessage.readShort();
                opcode = gameMessage.readInt();

                swgRouter.routeMessage(opcode, client, gameMessage);

                buffer.skipBytes(length);
            }

        }
    }

}
