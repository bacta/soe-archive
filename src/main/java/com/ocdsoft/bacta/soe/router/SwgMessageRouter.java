package com.ocdsoft.bacta.soe.router;

import com.ocdsoft.bacta.engine.network.router.IntMessageRouter;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

public interface SwgMessageRouter<Connection extends SoeUdpConnection> extends IntMessageRouter<Connection, ByteBuffer> {

}
