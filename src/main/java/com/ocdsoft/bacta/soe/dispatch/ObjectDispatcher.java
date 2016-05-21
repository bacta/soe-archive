package com.ocdsoft.bacta.soe.dispatch;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

/**
 * Created by kyle on 4/10/2016.
 */
public interface ObjectDispatcher<T>  {
    void dispatch(SoeUdpConnection connection, T message);
}
