package com.ocdsoft.bacta.soe.chat;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.ServerType;
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
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    private final ChatServerState serverState;
    private final SoeTransceiver transceiver;
    private final SoeDevMessageDispatcher router;
    private final ChatModule chatModule;
    private final MailModule mailModule;

    @Inject
    public ChatServer(final BactaConfiguration configuration,
                      final ChatServerState serverState,
                      final Injector injector,
                      final MetricRegistry metricRegistry) throws
            ClassNotFoundException,
            IllegalAccessException,
            UnknownHostException,
            InstantiationException {

        this.serverState = serverState;

        final InetAddress bindAddress = InetAddress.getByName(configuration.getString("Bacta/ChatServer", "bindAddress"));
        final int bindPort = configuration.getInt("Bacta/ChatServer", "bindPort");

        logger.info("Binding to [{}:{}].", bindAddress.getHostName(), bindPort);

        final String chatModuleType = configuration.getString("Bacta/ChatServer", "chatModule");
        final String mailModuleType = configuration.getString("Bacta/ChatServer", "mailModule");

        logger.debug("Chat module type is [{}].", chatModuleType);
        logger.debug("Mail module type is [{}].", mailModuleType);

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

        final Collection<String> swgControllerClasspaths = configuration.getStringCollection("Bacta/ChatServer", "swgControllerClasspaths");

        this.router = new SoeDevMessageDispatcher(injector, swgControllerClasspaths);

        this.transceiver = new SoeTransceiver(
                metricRegistry,
                injector.getInstance(NetworkConfiguration.class),
                bindAddress,
                bindPort,
                ServerType.CHAT,
                this.router,
                configuration.getStringCollection("Bacta/ChatServer", "trustedClient"));
    }

    @Override
    public void run() {
        logger.info("Starting.");

        try {

            serverState.setServerStatus(ServerStatus.UP);

            transceiver.run();

            serverState.setServerStatus(ServerStatus.DOWN);

        } catch (Exception ex) {
            logger.error("Error in chat transceiver.", ex);
        }
    }

    public void stop() {
        if (transceiver != null) {
            transceiver.stop();
        }
    }
}