package bacta.soe.message;

import io.netty.buffer.ByteBuf;

public final class MultiMessage extends SoeMessage {

    public MultiMessage(ByteBuf buffer1, ByteBuf buffer2) {
        super(0x3);
        add(buffer1);
        add(buffer2);
    }

    public void add(ByteBuf buffer) {

        int byteCount = buffer.readableBytes();
        if(byteCount > 0xFF) {
            int sizeCount = (byteCount / 0xFF) - (byteCount % 0xFF == 0 ? 1 : 0);

            writeByte(0xFF);
            writeByte(sizeCount);
            byteCount -= 0xFF;

            for (int i = 0; i < sizeCount; ++i) {
                writeByte(byteCount > 0xFF ? 0xFF : byteCount);
                byteCount -= 0xFF;
            }

        } else {
            writeByte(byteCount);
        }

        writeBytes(buffer);
    }
}
