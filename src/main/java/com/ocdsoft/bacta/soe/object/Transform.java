package com.ocdsoft.bacta.soe.object;

import com.ocdsoft.bacta.engine.buffer.ByteBufferSerializable;
import lombok.Data;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;

/**
 * Created by kyle on 4/7/2016.
 */
@Data
public class Transform implements ByteBufferSerializable {

    private final Quat4f orientation;
    private final Vector3f position;

    public Transform() {
        this.orientation = new Quat4f();
        this.position = new Vector3f();
    }

    public Transform(Quat4f orientation) {
        this.orientation = orientation;
        this.position = new Vector3f();
    }

    public Transform(Vector3f position) {
        this.orientation = new Quat4f();
        this.position = position;
    }

    public Transform(Vector3f position, Quat4f orientation) {
        this.orientation = orientation;
        this.position = position;
    }


    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        orientation.x = buffer.getFloat();
        orientation.y = buffer.getFloat();
        orientation.z = buffer.getFloat();
        orientation.w = buffer.getFloat();

        position.x = buffer.getFloat();
        position.z = buffer.getFloat();
        position.y = buffer.getFloat();
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        buffer.putFloat(orientation.x);
        buffer.putFloat(orientation.y);
        buffer.putFloat(orientation.z);
        buffer.putFloat(orientation.w);

        buffer.putFloat(position.x);
        buffer.putFloat(position.z);
        buffer.putFloat(position.y);
    }
}
