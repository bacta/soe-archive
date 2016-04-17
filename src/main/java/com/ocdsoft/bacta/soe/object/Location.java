package com.ocdsoft.bacta.soe.object;

import com.ocdsoft.bacta.engine.buffer.ByteBufferSerializable;
import com.ocdsoft.bacta.engine.utils.BufferUtil;
import lombok.Data;
import org.magnos.steer.vec.Vec3;

import javax.vecmath.Quat4f;
import java.nio.ByteBuffer;

/**
 * Created by kyle on 4/7/2016.
 */
@Data
public class Location implements ByteBufferSerializable {

    private Vec3 position;

    public Location() {
        this.position = new Vec3();
    }

    public Location(Vec3 position) {
        this.position = position;
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        position = BufferUtil.getVec3(buffer);
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        BufferUtil.putVec3(buffer, position);
    }

    public void setPosition(float x, float z, float y) {
        this.position.x = x;
        this.position.z = z;
        this.position.y = y;
    }
}
