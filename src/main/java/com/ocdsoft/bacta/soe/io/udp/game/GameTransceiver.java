package com.ocdsoft.bacta.soe.io.udp.game;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;
import com.ocdsoft.bacta.soe.protocol.SoeProtocol;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;

import java.net.InetAddress;

/**
 * Created by Kyle on 8/18/2014.
 */
public class GameTransceiver extends SoeTransceiver<GameConnection> {

    @Inject
    public GameTransceiver(@Assisted InetAddress bindAddress,
                           @Assisted("port") int port,
                           @Assisted("pingPort") int pingPort,
                           @Assisted Class<GameConnection> gameClientClass,
                           @Assisted("sendQueueInterval") int sendQueueInterval,
                           @Assisted SoeMessageRouter soeMessageRouter,
                           final Injector injector,
                           final SoeProtocol protocol,
                           final BactaConfiguration configuration) {

        super(bindAddress,
                port,
                ServerType.GAME,
                gameClientClass,
                sendQueueInterval,
                soeMessageRouter,
                protocol
        );

        Thread pingThread = new Thread( new PingServer(bindAddress, pingPort, connectionMap));
        pingThread.start();
    }
}
