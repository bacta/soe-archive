package com.ocdsoft.bacta.soe.io.udp.game;

import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.ServerType;

/**
 * Created by Kyle on 3/22/14.
 */
@Singleton
public class GameServerState extends ServerState {

    public GameServerState() {
        super(ServerType.GAME);
    }
}
