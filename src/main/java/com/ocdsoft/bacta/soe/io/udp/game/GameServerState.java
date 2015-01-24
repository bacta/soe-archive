package com.ocdsoft.bacta.soe.io.udp.game;

import com.ocdsoft.bacta.soe.ServerState;

/**
 * Created by kburkhardt on 1/24/15.
 */
public interface GameServerState<Data, Status> extends ServerState {

    int getId();
    String getName();
    String getSecret();
    Status getClusterStatus();
    Data getClusterData();
}
