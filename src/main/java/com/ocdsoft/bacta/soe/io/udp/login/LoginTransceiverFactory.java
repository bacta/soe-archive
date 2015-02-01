package com.ocdsoft.bacta.soe.io.udp.login;

import com.google.inject.assistedinject.Assisted;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;

import java.net.InetAddress;

/**
 * Created by Kyle on 8/24/2014.
 */
public interface LoginTransceiverFactory {
    LoginTransceiver create(InetAddress bindAddress, @Assisted("port") int port, Class<LoginConnection> clientClass, @Assisted("sendQueueInterval") int sendQueueInterval, SoeMessageRouter soeMessageRouter);
}
