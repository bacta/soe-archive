package com.ocdsoft.bacta.soe.io.udp.chat;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.chat.ChatModule;
import com.ocdsoft.bacta.soe.chat.MailModule;
import com.ocdsoft.bacta.soe.io.udp.NetworkConfiguration;
import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;
import com.ocdsoft.bacta.soe.dispatch.SoeDevMessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

/**
 * Created by crush on 1/12/2015.
 */
public final class ChatServer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final ChatServerState serverState;
    private final SoeTransceiver soeTransceiver;
    private final ChatModule chatModule;
    private final MailModule mailModule;

    @Inject
    public ChatServer(final BactaConfiguration configuration,
                      final ChatServerState serverState,
                      final SoeTransceiver soeTransceiver) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

        this.serverState = serverState;
        this.soeTransceiver = soeTransceiver;


        final String chatModuleType = configuration.getString("Bacta/ChatServer", "chatModule");
        final String mailModuleType = configuration.getString("Bacta/ChatServer", "mailModule");

        LOGGER.debug("Chat module type is [{}].", chatModuleType);
        LOGGER.debug("Mail module type is [{}].", mailModuleType);

        final Class chatModuleClass = Class.forName(chatModuleType);
        final Class mailModuleClass = Class.forName(mailModuleType);

        if (!ChatModule.class.isAssignableFrom(chatModuleClass)) {
            throw new RuntimeException(String.format("Given chat module [%s] does not inherit [%s].",
                    chatModuleClass.getName(),
                    ChatModule.class.getName()));
        }

        if (!MailModule.class.isAssignableFrom(mailModuleClass)) {
            throw new RuntimeException(String.format("Given mail module [%s] does not inherit [%s].",
                    mailModuleClass.getName(),
                    MailModule.class.getName()));
        }

        this.chatModule = (ChatModule) chatModuleClass.newInstance();
        this.mailModule = (MailModule) mailModuleClass.newInstance();
    }

    @Override
    public void run() {
        LOGGER.info("Starting.");

        try {

            serverState.setServerStatus(ServerStatus.UP);

            soeTransceiver.run();

            serverState.setServerStatus(ServerStatus.DOWN);

        } catch (Exception ex) {
            LOGGER.error("Error in chat transceiver.", ex);
        }
    }

    public void stop() {
        if (soeTransceiver != null) {
            soeTransceiver.stop();
        }
    }
}