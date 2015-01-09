package com.ocdsoft.bacta.soe;

import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.object.ClusterInfo;
import lombok.Delegate;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

/**
 * Created by kburkhardt on 2/22/14.
 */
public abstract class ServerState {

    @Getter
    private final ServerType serverType;

    @Getter
    @Setter
    private ServerStatus serverStatus;

    @Delegate
    private final ClusterInfo info = new ClusterInfo();
    
    public ServerState(ServerType serverType) {
        this.serverType = serverType;
        serverStatus = ServerStatus.LOADING;
    }

    @Synchronized("info")
    public void increasePopulation() {
        info.setPopulation(info.getPopulation() + 1);
    }

    @Synchronized("info")
    public void decreasePopulation() {
        info.setPopulation(info.getPopulation() - 1);
    }
}
