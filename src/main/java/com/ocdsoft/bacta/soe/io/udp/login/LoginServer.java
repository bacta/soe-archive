package com.ocdsoft.bacta.soe.io.udp.login;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.io.udp.NetworkConfiguration;
import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import com.ocdsoft.bacta.soe.service.OutgoingConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Created by kburkhardt on 2/14/14.
 */

public class LoginServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LoginServer.class);

    private final BactaConfiguration configuration;

    private final LoginServerState serverState;

    private final SoeTransceiver transceiver;

    @Inject
    public LoginServer(final BactaConfiguration configuration,
                       final LoginServerState serverState,
                       final OutgoingConnectionService outgoingConnectionService,
                       final Injector injector,
                       final MetricRegistry metricRegistry) throws UnknownHostException {

        this.configuration = configuration;
        this.serverState = serverState;

        SoeMessageRouter soeMessageRouter = new SoeMessageRouter(
                injector,
                configuration.getStringCollection("Bacta/LoginServer", "swgControllerClasspath")
        );

        serverState.setServerStatus(ServerStatus.LOADING);

        transceiver = new SoeTransceiver(
                metricRegistry,
                injector.getInstance(NetworkConfiguration.class),
                InetAddress.getByName(configuration.getString("Bacta/LoginServer", "BindIp")),
                configuration.getInt("Bacta/LoginServer", "Port"),
                ServerType.LOGIN,
                soeMessageRouter,
                configuration.getStringCollection("Bacta/LoginServer", "TrustedClient"));

        ((LoginOutgoingConnectionService)outgoingConnectionService).createConnection = transceiver::createOutgoingConnection;

        soeMessageRouter.load();
    }

    @Override
    public void run() {
        logger.info("Starting");

        try {
            
            serverState.setServerStatus(ServerStatus.UP);
            transceiver.run();

        } catch (Exception e) {
            logger.error("Error login game transceiver", e);
        }
    }

    public void stop() {
        if(transceiver != null) {
            transceiver.stop();
        }
    }

    @Singleton
    final static public class LoginOutgoingConnectionService implements OutgoingConnectionService {

        private BiFunction<InetSocketAddress, Consumer<SoeUdpConnection>, SoeUdpConnection> createConnection;

        @Override
        public SoeUdpConnection createOutgoingConnection(final InetSocketAddress address, final Consumer<SoeUdpConnection> connectCallback) {
            if(createConnection == null) return null;

            return createConnection.apply(address, connectCallback);
        }
    }
}
