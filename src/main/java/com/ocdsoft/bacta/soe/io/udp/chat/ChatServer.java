package com.ocdsoft.bacta.soe.io.udp.chat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;
import com.ocdsoft.bacta.soe.service.OutgoingConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Created by crush on 5/23/2016.
 */
public class ChatServer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private final ChatServerState serverState;
    private final SoeTransceiver soeTransceiver;

    @Inject
    public ChatServer(final BactaConfiguration configuration,
                      final ChatServerState serverState,
                      final SoeTransceiver soeTransceiver,
                      final OutgoingConnectionService outgoingConnectionService) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

        this.serverState = serverState;
        this.soeTransceiver = soeTransceiver;

        ((ChatOutgoingConnectionService)outgoingConnectionService).createConnection = soeTransceiver::createOutgoingConnection;
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

    @Singleton
    final static public class ChatOutgoingConnectionService implements OutgoingConnectionService {

        private BiFunction<InetSocketAddress, Consumer<SoeUdpConnection>, SoeUdpConnection> createConnection;

        @Override
        public SoeUdpConnection createOutgoingConnection(final InetSocketAddress address, final Consumer<SoeUdpConnection> connectCallback) {
            if(createConnection == null) return null;

            return createConnection.apply(address, connectCallback);
        }
    }
}
