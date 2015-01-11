package com.ocdsoft.bacta.soe.io.udp.login;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;
import com.ocdsoft.bacta.soe.protocol.SoeProtocol;
import com.ocdsoft.bacta.soe.router.SoeMessageRouterFactory;

import java.net.InetAddress;

/**
 * Created by Kyle on 8/18/2014.
 */
public class LoginTransceiver extends SoeTransceiver<LoginConnection> {

    @Inject
    public LoginTransceiver(@Assisted InetAddress bindAddress,
                            @Assisted("port") int port,
                            @Assisted Class<LoginConnection> gameClientClass,
                            @Assisted("sendQueueInterval") int sendQueueInterval,
                            SoeMessageRouterFactory soeMessageRouterFactory,
                            SoeProtocol protocol) {

        super(bindAddress, port, ServerType.LOGIN, gameClientClass, sendQueueInterval, soeMessageRouterFactory.create(ServerType.LOGIN), protocol);

    }
}
