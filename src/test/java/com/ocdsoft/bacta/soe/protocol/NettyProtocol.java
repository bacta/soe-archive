package com.ocdsoft.bacta.soe.protocol;

import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.engine.utils.UnsignedUtil;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NettyProtocol extends MessageDumpLoader {

	@Test
	public void test() {
				 
		SoeProtocol protocol = new SoeProtocol();

		for(int i = 0; i < pre.size(); ++i) {
			
			short[] preArray = pre.get(i);
			ByteBuffer test1 = ByteBuffer.allocate(preArray.length);
			
			for(short b : preArray)
				test1.put((byte) b);
			
			System.out.println("Decoded");
			System.out.println("Test: " + BufferUtil.bytesToHex(test1.array()));
			System.out.println("	" + BufferUtil.bytesToHex(decomp.get(i)));
			
			test1 = test1.order(ByteOrder.LITTLE_ENDIAN);
			test1 = protocol.decode(sessionKey, test1);

			System.out.println("	" + BufferUtil.bytesToHex(test1.array()));
			
			test1.rewind();

			for(int j = 0 ; j < decomp.get(i).length; ++j) {
				
				short decCheck = (short) (decomp.get(i)[j] & 0xFF);
				
				if(test1.position() - test1.limit() == 0)
					fail("Decompression Decoded buffer too short at index " + j);
				
				short value = UnsignedUtil.getUnsignedByte(test1);

				if(decCheck != value)
					fail("Decompression Results do not match at index " + j);
			}

			test1 = ByteBuffer.allocate(decomp.get(i).length);
			
			for(short b : decomp.get(i))
				test1.put((byte) b);
			
			test1.position(0);
			
			System.out.println("Encoded");
			System.out.println("Test: " + BufferUtil.bytesToHex(test1.array()));
			System.out.println("	" + BufferUtil.bytesToHex(pre.get(i)));
			
			test1 = protocol.encode(sessionKey, test1, true);
			
			protocol.appendCRC(sessionKey, test1, 2);
			
			assertTrue(protocol.validate(sessionKey, test1));
	
			System.out.println("	" + BufferUtil.bytesToHex(test1.array()));
			
			test1.position(0);

			for(int j = 0 ; j < pre.get(i).length; ++j) {
				
				short decCheck = (short) (pre.get(i)[j] & 0xFF);
				
				if(test1.position() - test1.limit() == 0)
					fail("Encoded buffer too short at index " + j);
				
				short value = UnsignedUtil.getUnsignedByte(test1);

				if(decCheck != value)
					fail("Encoded Results do not match at index " + j);
			}
			
		}
	}
}
