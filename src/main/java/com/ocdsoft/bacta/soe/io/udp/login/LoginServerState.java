package com.ocdsoft.bacta.soe.io.udp.login;

import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.ServerType;

/**
 * Created by Kyle on 3/22/14.
 */
public class LoginServerState implements ServerState {

    private ServerStatus serverStatus;

    public LoginServerState() {
        serverStatus = ServerStatus.DOWN;
    }

    @Override
    public void setId(int id) {

    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.LOGIN;
    }

    @Override
    public ServerStatus getServerStatus() {
        return serverStatus;
    }

    @Override
    public void setServerStatus(ServerStatus status) {
        this.serverStatus = status;
    }
}

