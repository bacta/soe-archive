package com.ocdsoft.bacta.soe.chat;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created by crush on 1/12/2015.
 */
public final class ChatServer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    private final SoeTransceiver transceiver;
    private final SoeMessageRouter router;
    private final ChatModule chatModule;
    private final MailModule mailModule;

    @Inject
    public ChatServer(final BactaConfiguration configuration,
                      final Injector injector) throws
            ClassNotFoundException,
            IllegalAccessException,
            UnknownHostException,
            InstantiationException {

        final InetAddress bindHost = InetAddress.getByName(configuration.getString("Bacta/ChatServer", "bindHost"));
        final int bindPort = configuration.getInt("Bacta/ChatServer", "bindPort");

        logger.info("Binding to [{}:{}].", bindHost.getHostName(), bindPort);

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

        final String soeControllerFileName = configuration.getString("Bacta/ChatServer", "soeControllerFileName");
        final String swgControllerFileName = configuration.getString("Bacta/ChatServer", "swgControllerFileName");

        this.router = new SoeMessageRouter(injector, soeControllerFileName, swgControllerFileName);

        this.transceiver = new SoeTransceiver(
                bindHost,
                bindPort,
                ServerType.CHAT,
                configuration.getInt("Bacta/ChatServer", "sendInterval"),
                this.router,
                configuration.getStringCollection("Bacta/ChatServer", "trustedClient"));
    }

    @Override
    public void run() {
    }
}