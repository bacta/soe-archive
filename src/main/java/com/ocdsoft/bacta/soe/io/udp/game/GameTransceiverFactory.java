package com.ocdsoft.bacta.soe.io.udp.game;

import com.google.inject.assistedinject.Assisted;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;

import java.net.InetAddress;

/**
 * Created by Kyle on 8/24/2014.
 */
public interface GameTransceiverFactory {
    GameTransceiver create(InetAddress bindAddress, @Assisted("port") int port, @Assisted("pingPort") int pingPort, Class<GameConnection> clientClass, @Assisted("sendQueueInterval") int sendQueueInterval, SoeMessageRouter soeMessageRouter);
}
