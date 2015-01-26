package com.ocdsoft.bacta.soe.router;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.network.ControllerScan;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.controller.SoeMessageController;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.util.SoeMessageUtil;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Singleton
@ControllerScan(target = "com.ocdsoft.bacta.soe.controller")
public final class SoeMessageRouterImpl implements SoeMessageRouter {

    private final static Logger logger = LoggerFactory.getLogger(SoeMessageRouterImpl.class);

    private Map<UdpPacketType, SoeMessageController> controllers = new HashMap<>();

    @Inject
    public SoeMessageRouterImpl(Injector injector) {
        loadControllers(injector);
    }

    @Override
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

            logger.trace("Routing to " + controller.getClass().getSimpleName());
            controller.handleIncoming(zeroByte, packetType, client, buffer);

        } catch (Exception e) {
            logger.error("SOE Routing", e);
        }
    }

    private void loadControllers(Injector injector) {

        ControllerScan scanAnnotation = getClass().getAnnotation(ControllerScan.class);

        if (scanAnnotation == null) {
            logger.error("Missing @ControllerScan annotation, unable to load controllers");
            return;
        }

        Reflections reflections = new Reflections(scanAnnotation.target());
        Set<Class<? extends SoeMessageController>> subTypes = reflections.getSubTypesOf(SoeMessageController.class);

        Iterator<Class<? extends SoeMessageController>> iter = subTypes.iterator();
        while (iter.hasNext()) {

            try {
                Class<? extends SoeMessageController> controllerClass = iter.next();

                SoeController controllerAnnotation = controllerClass.getAnnotation(SoeController.class);

                if (controllerAnnotation == null) {
                    logger.info("Missing @SoeController annotation, discarding: " + controllerClass.getName());
                    continue;
                }

                UdpPacketType[] types = controllerAnnotation.handles();
                logger.debug("Loading SoeMessageController: " + controllerClass.getSimpleName());

                SoeMessageController controller = injector.getInstance(controllerClass);

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
