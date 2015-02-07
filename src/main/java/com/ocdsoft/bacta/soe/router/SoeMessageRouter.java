package com.ocdsoft.bacta.soe.router;

import com.google.inject.Injector;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.utils.BufferUtil;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.controller.SoeMessageController;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.util.SoeMessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SoeMessageRouter {

    private final static Logger logger = LoggerFactory.getLogger(SoeMessageRouter.class);

    private Map<UdpPacketType, SoeMessageController> controllers = new HashMap<>();

    private final Injector injector;
    private final String soeControllerFileName;
    private final String swgControllerFileName;

    public SoeMessageRouter(final Injector injector,
                            final String soeControllerFileName,
                            final String swgControllerFileName) {
        
        this.injector = injector;
        this.soeControllerFileName = soeControllerFileName;
        this.swgControllerFileName = swgControllerFileName;
    }

    public void routeMessage(SoeUdpConnection client, ByteBuffer buffer) {

        byte zeroByte = buffer.get();
        byte type = buffer.get();
        if(type < 0 || type > 0x1E) {
            throw new RuntimeException("Type out of range:" + type + " " + buffer.toString() + " " + SoeMessageUtil.bytesToHex(buffer));
        }

        UdpPacketType packetType = UdpPacketType.values()[type];

        SoeMessageController controller = controllers.get(packetType);

        if (controller == null) {
            logger.error("Unhandled SOE Opcode 0x" + Integer.toHexString(packetType.getValue()).toUpperCase());
            logger.error(SoeMessageUtil.bytesToHex(buffer));
            return;
        }

        try {

            logger.trace("Routing to " + controller.getClass().getSimpleName() + ": " + BufferUtil.bytesToHex(buffer));
            controller.handleIncoming(zeroByte, packetType, client, buffer);

        } catch (Exception e) {
            logger.error("SOE Routing", e);
        }
    }

    public void load() {
        
        File file = new File("../conf/" + soeControllerFileName);
        if(!file.exists()) {
            file = new File(getClass().getResource("/" + soeControllerFileName).getFile());
        }

        List<String> classNameList;
        try {
            classNameList = Files.readAllLines(Paths.get(file.toURI()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        controllers.clear();

        ServerState serverState = injector.getInstance(ServerState.class);
        BactaConfiguration configuration = injector.getInstance(BactaConfiguration.class);

        SwgMessageRouter swgMessageRouter = new SwgDevelopMessageRouter(
                injector,
                serverState,
                swgControllerFileName,
                configuration.getBoolean("Bacta/GlobalSettings", "ControllerGeneration"));

        for(String className : classNameList) {

            try {
                Class<? extends SoeMessageController> controllerClass = (Class<? extends SoeMessageController>) Class.forName(className);

                SoeController controllerAnnotation = controllerClass.getAnnotation(SoeController.class);

                if (controllerAnnotation == null) {
                    logger.info("Missing @SoeController annotation, discarding: " + controllerClass.getName());
                    continue;
                }

                UdpPacketType[] types = controllerAnnotation.handles();
                logger.debug("Loading SoeMessageController: " + serverState.getServerType() + " " + controllerClass.getSimpleName());

                SoeMessageController controller = injector.getInstance(controllerClass);
                controller.setSoeMessageRouter(this);
                controller.setSwgMessageRouter(swgMessageRouter);

                for(UdpPacketType udpPacketType : types) {

                    if (!controllers.containsKey(udpPacketType)) {
                        //logger.debug("Adding SOE controller for: " + handledMessageClass.getSimpleName());
                        synchronized (controllers) {
                            controllers.put(udpPacketType, controller);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Unable to add controller", e);
            }
        }
    }
}
