package com.ocdsoft.bacta.soe.io.udp.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.soe.io.udp.BaseNetworkConfiguration;
import com.ocdsoft.bacta.soe.io.udp.NetworkConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by kyle on 4/12/2016.
 */

@Singleton
public final class GameNetworkConfiguration extends BaseNetworkConfiguration implements NetworkConfiguration {

    @Inject
    public GameNetworkConfiguration(final BactaConfiguration configuration) throws UnknownHostException {
        super(configuration);

        bindIp = InetAddress.getByName(configuration.getString("Bacta/GameServer", "BindIp"));
        port = configuration.getInt("Bacta/GameServer", "Port");
        trustedClients = configuration.getStringCollection("Bacta/GameServer", "TrustedClient");
    }
}
