package bacta.soe.controller;

import bacta.buffer.BactaBuffer;
import bacta.soe.SoeController;
import bacta.soe.client.SoeUdpClient;
import bacta.soe.message.MultiMessage;
import bacta.soe.router.SoeMessageRouter;

@SoeController(opcode = 0x3, handles = MultiMessage.class)
public class MultiController extends SoeMessageController {

    private SoeMessageRouter soeRouter;
    private SwgMessageRouter swgRouter;

    @Override
    public void setRouter(SoeMessageRouter soeRouter) {
        this.soeRouter = soeRouter;
        this.swgRouter = soeRouter.getSwgRouter();
    }

    @Override
    public void handleIncoming(SoeUdpClient client, BactaBuffer buffer) {

        short length = buffer.readUnsignedByte();

        while (buffer.readableBytes() >= length) {

            if (length == 0xFF) {
                if (buffer.readUnsignedByte() == 0x01)
                    length += buffer.readUnsignedByte();
            }

            int swgByte = buffer.readUnsignedByte();
            short soeByte = buffer.readUnsignedByte();

            if (swgByte != 0) {
                int opcode = buffer.readInt();

                BactaBuffer gameMessage = new BactaBuffer(buffer.slice(buffer.readerIndex(), length - 6));
                swgRouter.routeMessage(opcode, client, gameMessage);

                buffer.skipBytes(length - 6);
            } else {
                soeRouter.routeMessage(soeByte, client, new BactaBuffer(buffer.slice(buffer.readerIndex(), length - 2)));
                buffer.skipBytes(length - 2);
            }

            if (buffer.readableBytes() <= 3)
                break;

            length = buffer.readUnsignedByte();
        }
    }

}
