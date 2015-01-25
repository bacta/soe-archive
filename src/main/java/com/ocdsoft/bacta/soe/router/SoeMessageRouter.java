package com.ocdsoft.bacta.soe.router;

/**
 * Created by kburkhardt on 1/24/15.
 */

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

public interface SoeMessageRouter {
    void routeMessage(SoeUdpConnection client, ByteBuffer buffer);
}
