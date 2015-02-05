package com.ocdsoft.bacta.soe.io.udp.game;

import com.ocdsoft.bacta.soe.ServerState;

/**
 * Created by kburkhardt on 1/24/15.
 */
public interface GameServerState<Data> extends ServerState {
    void setId(int id);
    int getId();
    Data getClusterEntry();
}
