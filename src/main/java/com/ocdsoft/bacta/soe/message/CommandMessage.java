package com.ocdsoft.bacta.soe.message;

import com.ocdsoft.bacta.engine.utils.BufferUtil;
import lombok.Getter;

import java.nio.ByteBuffer;

/**
 * Created by kyle on 4/17/2016.
 */
@Getter
public abstract class CommandMessage extends ObjControllerMessage {

    private final int sequenceId;
    private final int commandHash;
    private final long targetId;
    private final String params;  // Unicode

    public CommandMessage(final ByteBuffer buffer) {
        super(buffer);

        sequenceId = buffer.getInt();
        commandHash = buffer.getInt();
        targetId = buffer.getLong();
        params = BufferUtil.getUnicode(buffer);
    }

}
