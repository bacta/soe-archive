package com.ocdsoft.bacta.soe.object;

import com.ocdsoft.bacta.engine.buffer.ByteBufferSerializable;
import com.ocdsoft.bacta.engine.utils.BufferUtil;
import lombok.Data;
import org.magnos.steer.vec.Vec3;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;

/**
 * Created by kyle on 4/7/2016.
 */
@Data
public class Transform extends Location implements ByteBufferSerializable{

    private Quat4f orientation;

    public Transform() {
        this.orientation = new Quat4f();
    }

    public Transform(Quat4f orientation) {
        this.orientation = orientation;
    }

    public Transform(Vec3 position) {
        super(position);
        this.orientation = new Quat4f();
    }

    public Transform(Vec3 position, Quat4f orientation) {
        super(position);
        this.orientation = orientation;
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        orientation = BufferUtil.getQuat4f(buffer);
        super.readFromBuffer(buffer);
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        BufferUtil.putQuat4f(buffer, orientation);
        super.writeToBuffer(buffer);
    }

    public void setOrientation(float x, float y, float z, float w) {
        this.orientation.x = x;
        this.orientation.y = y;
        this.orientation.z = z;
        this.orientation.w = w;
    }
}
