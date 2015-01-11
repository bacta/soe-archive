package com.ocdsoft.bacta.soe.io.udp.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.engine.service.scheduler.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by Kyle on 4/4/14.
 */
@Singleton
public class GameServerStatusUpdater {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private final BactaConfiguration configuration;

    private final GameServerState serverState;

    private final SchedulerService schedulerService;

    //private final GameServerStatusTask statusUpdateTask;

    private boolean started = false;

    @Inject
    public GameServerStatusUpdater(BactaConfiguration configuration, GameServerState serverState, SchedulerService schedulerService) {
        this.configuration = configuration;
        this.serverState = serverState;
        this.schedulerService = schedulerService;

        initServerState();
        //statusUpdateTask = new GameServerStatusTask();
        //statusUpdateTask.run();
    }

    private void initServerState() {

        serverState.setSecret(configuration.getString("Bacta/GameServer", "Secret"));
        serverState.setName(configuration.getStringWithDefault("Bacta/GameServer", "Name", "BactaGalaxy"));
        serverState.setAddress(configuration.getStringWithDefault("Bacta/GameServer", "PublicAddress", "127.0.0.1"));
        serverState.setPort(configuration.getIntWithDefault("Bacta/GameServer", "Port", 44463));
        serverState.setPingPort(configuration.getIntWithDefault("Bacta/GameServer", "Ping", 44462));
        serverState.setMaximumPopulation(configuration.getIntWithDefault("Bacta/GameServer", "MaxPopulation", 3000));
        serverState.setMaximumCharacters(configuration.getIntWithDefault("Bacta/GameServer", "MaxCharsPerAccount", 2));
        serverState.setServerStatus(ServerStatus.LOADING);

        TimeZone timeZone = Calendar.getInstance().getTimeZone();
        Calendar cal = GregorianCalendar.getInstance(timeZone);
        int offsetInSeconds = timeZone.getOffset(cal.getTimeInMillis()) / 1000;

        serverState.setTimezone(offsetInSeconds);

        //TODO: We should probably calculate this based on something like population + distance.
        serverState.setRecommended(configuration.getBooleanWithDefault("Bacta/GameServer", "Recommended", false));
    }

    public void start() {
       /* if(!started) {
            schedulerService.scheduleAtFixedRate(statusUpdateTask, 2, 60, TimeUnit.SECONDS);
            started = true;
        }*/
    }

    /*class GameServerStatusTask extends GameTask {

        private DatagramSocket s;
        private InetSocketAddress hostAddress;

        public GameServerStatusTask()  {
            try {
                s = new DatagramSocket();
                s.setSoTimeout(2000);

                hostAddress = new InetSocketAddress(
                    configuration.getStringWithDefault("Bacta/LoginServer", "BindIp", "127.0.0.1"),
                    configuration.getIntWithDefault("Bacta/LoginServer", "Port", 44453));
            } catch (Exception e) {
                logger.error("Can't Start", e);
            }
        }

        @Override
        public void run() {

            try {

                GameServerStatusUpdate update = new GameServerStatusUpdate(serverState);

                byte[] buf = new byte[1000];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);

                DatagramPacket out = new DatagramPacket(
                        update.array(),
                        update.array().length,
                        hostAddress.getAddress(),
                        hostAddress.getPort());

                s.send(out);

                try {
                    s.receive(dp);
                    if(serverState.getId() < 2) {
                        serverState.setId(dp.getData()[3]);
                    }

                } catch (Exception e) {
                    logger.info("Unable to contact login server, will try again");
                }

            } catch (Exception e) {
                logger.error("Error notifying login", e);
            }
        }
    }*/
}
