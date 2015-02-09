package com.ocdsoft.bacta.soe.chat;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.chat.message.ChatAgentIdentity;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import com.ocdsoft.bacta.soe.service.OutgoingConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by crush on 2/1/15.
 */
@Singleton
public final class ChatServerAgentService {
    private static final Logger logger = LoggerFactory.getLogger(ChatServerAgentService.class);

    private final SoeUdpConnection chatServerConnection;

    @Inject
    public ChatServerAgentService(final BactaConfiguration configuration,
                                  final OutgoingConnectionService connectionService) {

        logger.debug("Initializing.");

        final InetSocketAddress chatServerAddress = new InetSocketAddress(
                configuration.getString("Bacta/GameServer", "chatServerAddress"),
                configuration.getInt("Bacta/GameServer", "chatServerPort"));

        this.chatServerConnection = connectionService.createOutgoingConnection(
                chatServerAddress,
                this::onConnected);

        logger.debug("Done initializing.");
    }

    private final void onConnected(final SoeUdpConnection connection) {
        logger.info("Connected to the chat server.");
    }
}
