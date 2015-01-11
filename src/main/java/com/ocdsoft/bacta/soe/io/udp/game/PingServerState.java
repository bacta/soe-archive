package com.ocdsoft.bacta.soe.io.udp.game;

import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.ServerType;

/**
 * Created by Kyle on 3/22/14.
 */
public class PingServerState extends ServerState {

    public PingServerState() {
        super(ServerType.PING);
    }
}

