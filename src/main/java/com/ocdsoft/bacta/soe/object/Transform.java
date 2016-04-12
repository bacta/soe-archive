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
public class Transform implements ByteBufferSerializable {

    private Quat4f orientation;
    private Vec3 position;

    public Transform() {
        this.orientation = new Quat4f();
        this.position = new Vec3();
    }

    public Transform(Quat4f orientation) {
        this.orientation = orientation;
        this.position = new Vec3();
    }

    public Transform(Vec3 position) {
        this.orientation = new Quat4f();
        this.position = position;
    }

    public Transform(Vec3 position, Quat4f orientation) {
        this.orientation = orientation;
        this.position = position;
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        orientation = BufferUtil.getQuat4f(buffer);
        position = BufferUtil.getVec3(buffer);
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        BufferUtil.putQuat4f(buffer, orientation);
        BufferUtil.putVec3(buffer, position);
    }

    public void setOrientation(float x, float y, float z, float w) {
        this.orientation.x = x;
        this.orientation.y = y;
        this.orientation.z = z;
        this.orientation.w = w;
    }

    public void setPosition(float x, float z, float y) {
        this.position.x = x;
        this.position.z = z;
        this.position.y = y;
    }
}
