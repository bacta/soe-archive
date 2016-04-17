package com.ocdsoft.bacta.soe.io.udp.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.ServerType;
import lombok.Data;

/**
 * Created by kyle on 4/11/2016.
 */
@Singleton
@Data
public final class GameServerState implements ServerState {
    private int id;
    private ServerType serverType;
    private ServerStatus serverStatus;

    @Inject
    public GameServerState() {
        this.serverStatus = ServerStatus.LOADING;
        this.serverType = ServerType.GAME;
    }
}
