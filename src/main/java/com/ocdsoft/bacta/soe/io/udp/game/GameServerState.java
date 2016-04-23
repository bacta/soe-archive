package com.ocdsoft.bacta.soe.io.udp.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.object.ClusterEntryItem;
import lombok.Data;

/**
 * Created by kyle on 4/11/2016.
 */

public interface GameServerState<T extends ClusterEntryItem> extends ServerState {
    void setId(int id);
    int getId();
    T getClusterEntry();
}
