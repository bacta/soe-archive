package com.ocdsoft.bacta.soe.io.udp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.soe.io.udp.BaseNetworkConfiguration;
import com.ocdsoft.bacta.soe.io.udp.NetworkConfiguration;
import lombok.Getter;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by kyle on 4/12/2016.
 */

@Singleton
@Getter
public final class GameNetworkConfiguration extends BaseNetworkConfiguration implements NetworkConfiguration {

    private final int pingPort;

    @Inject
    public GameNetworkConfiguration(final BactaConfiguration configuration) throws UnknownHostException {
        super(configuration, "Bacta/GameServer");

        pingPort = configuration.getInt("Bacta/GameServer", "PingPort");
    }
}
