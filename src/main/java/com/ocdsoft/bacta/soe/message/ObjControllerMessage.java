package com.ocdsoft.bacta.soe.message;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;

@Getter
@AllArgsConstructor
public abstract class ObjControllerMessage extends GameNetworkMessage {
    private final int flag;
    private final int messageType;
    @Setter private long receiver;
    private final int tickCount;

	public ObjControllerMessage(final ByteBuffer buffer) {
        this.flag = buffer.getInt();
        this.messageType = buffer.getInt();
        this.receiver = buffer.getLong();
        this.tickCount = buffer.getInt();
	}

	@Override
	public void writeToBuffer(final ByteBuffer buffer) {
        buffer.putInt(flag);
        buffer.putInt(messageType);
        buffer.putLong(receiver);
        buffer.putInt(tickCount);
	}
}
