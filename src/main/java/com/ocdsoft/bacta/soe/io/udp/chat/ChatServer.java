package com.ocdsoft.bacta.soe.io.udp.chat;

import com.google.inject.Inject;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by crush on 1/12/2015.
 */
public final class ChatServer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final ChatServerState serverState;
    private final SoeTransceiver soeTransceiver;

    @Inject
    public ChatServer(final BactaConfiguration configuration,
                      final ChatServerState serverState,
                      final SoeTransceiver soeTransceiver) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

        this.serverState = serverState;
        this.soeTransceiver = soeTransceiver;
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