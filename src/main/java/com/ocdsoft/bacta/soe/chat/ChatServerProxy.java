package com.ocdsoft.bacta.soe.chat;

import com.google.inject.Inject;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * Created by crush on 1/12/2015.
 */
public final class ChatServerProxy implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ChatServerProxy.class);

    private BactaConfiguration configuration;

    public ChatServerProxy(BactaConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void run() {
        try {
            InetAddress bindAddress = InetAddress.getByName(
                    configuration.getStringWithDefault("Bacta/ChatServer", "BindIp", "127.0.0.1"));

            int port = configuration.getIntWithDefault("Bacta/ChatServer", "Port", 44499);

            logger.info("Binding to [{}:{}].", bindAddress.toString(), port);

            String chatModuleType = configuration.getString("Bacta/ChatServer", "ChatModule");
            String mailModuleType = configuration.getString("Bacta/ChatServer", "MailModule");

            logger.debug("Chat module type is [{}].", chatModuleType);
            logger.debug("Mail module type is [{}].", mailModuleType);

            Class chatModuleClass = Class.forName(chatModuleType);
            Class mailModuleClass = Class.forName(mailModuleType);

        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}