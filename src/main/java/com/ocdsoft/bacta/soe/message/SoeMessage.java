package com.ocdsoft.bacta.soe.message;

import com.ocdsoft.bacta.engine.buffer.BactaBuffer;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

public abstract class SoeMessage extends BactaBuffer {

    @Getter
    protected boolean compressed = true;
    @Getter
    protected boolean process = true;
    @Getter
    protected int opcode;

    public SoeMessage() {

    }

    public SoeMessage(int opcode) {
        this();

        this.opcode = opcode;
        writeShortBE(opcode);
    }

    public SoeMessage(ByteBuf buffer) {
        super(buffer);
    }
}
