package com.ocdsoft.bacta.soe.io.udp.chat;

import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.ServerType;

/**
 * Created by crush on 2/1/15.
 */
public class ChatServerState implements ServerState {
    private ServerStatus serverStatus;

    public ChatServerState() {
        this.serverStatus = ServerStatus.DOWN;
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
        return ServerType.CHAT;
    }

    @Override
    public ServerStatus getServerStatus() {
        return this.serverStatus;
    }

    @Override
    public void setServerStatus(ServerStatus status) {
        this.serverStatus = status;
    }
}
