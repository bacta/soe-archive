package com.ocdsoft.bacta.soe.object.account;

import com.ocdsoft.bacta.engine.buffer.ByteBufferWritable;
import com.ocdsoft.bacta.engine.utils.BufferUtil;
import lombok.Data;
import lombok.Getter;

import java.nio.ByteBuffer;

/**
 * 
060E51D5   -   human male		
04FEC8FA   -   trandoshan male	
32F6307A   -   twilek male		
9B81AD32   -   bothan male		
22727757   -   zabrak male		
CB8F1F9D   -   rodian male		
79BE87A9   -   moncal male		
2E3CE884   -   wookiee male		
1C95F5BC   -   sullstan male		
D3432345   -   ithorian male		
D4A72A70   -   human female		
64C24976   -   trandoshan female	
6F6EB65D   -   twilek female		
F6AB978F   -   bothan female		
421ABB7C   -   zabrak female		
299DC0DA   -   rodian female		
73D65B5F   -   moncal female		
1AAD09FA   -   wookiee female	
44739CC1   -   sullstan female	
E7DA1366   -   ithorian female */

@Data
public final class CharacterInfo implements ByteBufferWritable, Comparable<CharacterInfo> {


	private String name; //UnicodeString
    private int objectTemplateId;
    private long networkId; //NetworkId
    private int clusterId;
    private Type characterType;
    private boolean disabled;

    public CharacterInfo() {
    }

    public CharacterInfo(ByteBuffer buffer) {
        name = BufferUtil.getUnicode(buffer);
        objectTemplateId = buffer.getInt();
        networkId = buffer.getLong();
        clusterId = buffer.getInt();
        characterType = Type.values()[buffer.getInt()];
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        BufferUtil.putUnicode(buffer, name);
        buffer.putInt(objectTemplateId);
        buffer.putLong(networkId);
        buffer.putInt(clusterId);
        characterType.writeToBuffer(buffer);
    }

    @Override
    public int compareTo(CharacterInfo o) {
        return name.compareTo(o.name);
    }

    public enum Type implements ByteBufferWritable {
        NONE(0x0),
        NORMAL(0x1),
        JEDI(0x2),
        SPECTRAL(0x3);

        @Getter
        private int value;

        private Type(int value) {
            this.value = value;
        }

        @Override
        public void writeToBuffer(ByteBuffer buffer) {
            buffer.putInt(value);
        }
    }
}