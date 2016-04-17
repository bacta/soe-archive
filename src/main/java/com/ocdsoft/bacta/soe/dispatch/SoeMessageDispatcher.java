package com.ocdsoft.bacta.soe.dispatch;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

/**
 * Created by kburkhardt on 2/10/15.
 */
public interface SoeMessageDispatcher {
    void dispatch(SoeUdpConnection client, ByteBuffer buffer);
}
