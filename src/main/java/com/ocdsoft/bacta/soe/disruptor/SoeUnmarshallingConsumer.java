package com.ocdsoft.bacta.soe.disruptor;

import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;

public class SoeUnmarshallingConsumer<T extends SoeUdpClient> implements EventHandler<SoeInputEvent<T>> {
	
	private final SoeProtocol protocol;
	
	@Inject
	public SoeUnmarshallingConsumer(SoeProtocol protocol) {
		this.protocol = protocol;
	}

	@Override
	public void onEvent(SoeInputEvent<T> event, long sequence, boolean endOfBatch)
			throws Exception {

		ByteBuf message = event.getBuffer();
		T client = event.getClient(); 
		
		int swgByte = message.getByte(0);
		
		ByteBuf decodedMessage;
		
		if(swgByte != 0) {
			
			//decodedMessage = protocol.decode(client.getSessionKey(), message.order(ByteOrder.LITTLE_ENDIAN), 1);
			//decodedMessage.skipBytes(2);
            event.setSwgMessage(true);

		} else {
			
 			//decodedMessage = protocol.decode(client.getSessionKey(), message.order(ByteOrder.LITTLE_ENDIAN), 2);
		
		}

       // event.setBuffer(decodedMessage);
	}

}
