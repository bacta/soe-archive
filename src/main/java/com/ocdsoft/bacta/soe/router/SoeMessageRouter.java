package com.ocdsoft.bacta.soe.router;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

/**
 * Created by kburkhardt on 2/10/15.
 */
public interface SoeMessageRouter {
    void routeMessage(SoeUdpConnection client, ByteBuffer buffer);
    void load();
}
