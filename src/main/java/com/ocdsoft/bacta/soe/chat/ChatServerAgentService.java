package com.ocdsoft.bacta.soe.chat;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.soe.chat.message.ChatAgentIdentity;
import com.ocdsoft.bacta.soe.client.ClientConnection;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by crush on 2/1/15.
 */
@Singleton
public final class ChatServerAgentService {
    private static final Logger logger = LoggerFactory.getLogger(ChatServerAgentService.class);

    private final BactaConfiguration configuration;
    private final List<ChatServerAgent> agents;
    private final SoeMessageRouter router;
    private final InetSocketAddress chatServerAddress;
    private final int udpSize;
    private final int protocolVersion;

    @Inject
    public ChatServerAgentService(
            final Injector injector,
            final BactaConfiguration configuration) {

        logger.debug("Initializing.");

        this.configuration = configuration;

        final String soeControllerFileName = configuration.getString("Bacta/GameServer/ChatServerAgent", "soeControllerFileName");
        final String swgControllerFileName = configuration.getString("Bacta/GameServer/ChatServerAgent", "swgControllerFileName");

        this.router = new SoeMessageRouter(injector, soeControllerFileName, swgControllerFileName);
        this.chatServerAddress = new InetSocketAddress(
                configuration.getString("Bacta/GameServer/ChatServerAgent", "chatServerHost"),
                configuration.getInt("Bacta/GameServer/ChatServerAgent", "chatServerPort"));

        this.udpSize = configuration.getIntWithDefault("SharedNetwork", "maxRawPacketSize", 496);
        this.protocolVersion = 2;

        final int numberOfAgents = configuration.getIntWithDefault("Bacta/GameServer/ChatServerAgent", "numberOfAgents", 1);

        this.agents = new ArrayList<>(numberOfAgents);

        for (int i = 0; i < numberOfAgents; i++)
            this.agents.add(createAgent());

        logger.debug("Done initializing.");
    }

    private final ChatServerAgent createAgent() {
        logger.debug("Creating chat agent.");

        final ClientConnection connection = new ClientConnection(this.router, this.udpSize, this.protocolVersion);
        connection.setRemoteAddress(this.chatServerAddress);
        connection.setConnectCallback(this::onAgentConnected);

        return new ChatServerAgent(connection);
    }

    /**
     * Sends a chat agent identification message to the chat server when the chat agent connects successfully.
     * This tells the chat server to which game server the chat agent belongs.
     * @param connection The connection belonging to the chat agent.
     */
    private final void onAgentConnected(ClientConnection connection) {
        ChatAgentIdentity identity = new ChatAgentIdentity(
                configuration.getString("Bacta/GameServer", "Name"),
                configuration.getString("Bacta/GameServer", "BindIp"),
                configuration.getShort("Bacta/GameServer", "Port"));

        connection.sendMessage(identity);
    }
}
