package com.ocdsoft.bacta.soe.io.udp.login;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.io.udp.SoeTransceiver;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * Created by kburkhardt on 2/14/14.
 */

public class LoginServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LoginServer.class);

    private final BactaConfiguration configuration;

    private final LoginServerState serverState;

    private final Injector injector;

    private SoeTransceiver transceiver;

    @Inject
    public LoginServer(final BactaConfiguration configuration,
                       final LoginServerState serverState,
                       final Injector injector) {

        this.configuration = configuration;
        this.serverState = serverState;
        this.injector = injector;
    }

    @Override
    public void run() {
        logger.info("Starting");

        try {

            SoeMessageRouter soeMessageRouter = new SoeMessageRouter(
                    injector,
                    configuration.getString("Bacta/LoginServer", "SoeControllerList"),
                    configuration.getString("Bacta/LoginServer", "SwgControllerList")
            );

            serverState.setServerStatus(ServerStatus.LOADING);

            SoeTransceiver transceiver = new SoeTransceiver(
                    InetAddress.getByName(configuration.getString("Bacta/LoginServer", "BindIp")),
                    configuration.getInt("Bacta/LoginServer", "Port"),
                    ServerType.GAME,
                    configuration.getInt("Bacta/LoginServer", "SendInterval"),
                    soeMessageRouter,
                    configuration.getStringCollection("Bacta/LoginServer", "TrustedClient"));

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
}
