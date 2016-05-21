package com.ocdsoft.bacta.soe;

import com.ocdsoft.bacta.engine.network.client.ServerStatus;

/**
 * Created by kburkhardt on 2/22/14.
 */
public interface ServerState {
    int getId();
    ServerType getServerType();
    ServerStatus getServerStatus();
    void setServerStatus(ServerStatus status);
}
