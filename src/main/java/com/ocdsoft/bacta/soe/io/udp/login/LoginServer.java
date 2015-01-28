package com.ocdsoft.bacta.soe.io.udp.login;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by kburkhardt on 2/14/14.
 */

public class LoginServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LoginServer.class);

    private final LoginTransceiverFactory loginTransceiverFactory;

    private final BactaConfiguration configuration;

    private final LoginServerState serverState;

    private final Injector injector;

    private LoginTransceiver loginTransceiver;

    @Inject
    public LoginServer(final BactaConfiguration configuration,
                       final LoginTransceiverFactory loginTransceiverFactory,
                       final LoginServerState serverState,
                       final Injector injector) {

        this.configuration = configuration;
        this.loginTransceiverFactory = loginTransceiverFactory;
        this.serverState = serverState;
        this.injector = injector;
    }

    @Override
    public void run() {
        logger.info("Starting");

        InetAddress bindAddress;
        try {
            bindAddress = InetAddress.getByName(configuration.getString("Bacta/LoginServer", "BindIp"));
        } catch (UnknownHostException e) {
            bindAddress = null;
            logger.error("Unknown Host", e);
        }
        int port = configuration.getInt("Bacta/LoginServer", "Port");
        int sendInterval = configuration.getInt("Bacta/LoginServer", "SendInterval");

        SoeMessageRouter soeMessageRouter = new SoeMessageRouter(
                injector,
                configuration.getString("Bacta/LoginServer", "SoeControllerList"),
                configuration.getString("Bacta/LoginServer", "SwgControllerList")
        );

        serverState.setServerStatus(ServerStatus.LOADING);

        loginTransceiver = loginTransceiverFactory.create(bindAddress, port, LoginConnection.class, sendInterval, soeMessageRouter);

        logger.info("Listening on port " + port);
        serverState.setServerStatus(ServerStatus.UP);
        loginTransceiver.run();
    }

    public void stop() {
        if(loginTransceiver != null) {
            loginTransceiver.stop();
        }
    }
}
