package com.ocdsoft.bacta.soe.router;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.ocdsoft.bacta.engine.network.ControllerScan;
import com.ocdsoft.bacta.engine.network.router.ShortMessageRouter;
import com.ocdsoft.bacta.soe.BactaController;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.controller.BactaMessageController;
import com.ocdsoft.bacta.soe.util.SoeMessageUtil;
import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Set;

@ControllerScan(target = "com.ocdsoft.bacta")
public class BactaMessageRouter implements ShortMessageRouter<SoeUdpConnection, ByteBuffer> {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private final TShortObjectMap<BactaMessageController> controllers = new TShortObjectHashMap<>();

    private final Injector injector;

    private final ServerType serverType;

    @Inject
    public BactaMessageRouter(@Assisted ServerType serverType, Injector injector) {
        this.injector = injector;
        this.serverType = serverType;
        loadControllers();
    }

    @Override
    public void routeMessage(short command, SoeUdpConnection client, ByteBuffer buffer) {
        BactaMessageController controller = controllers.get(command);

        if (controller == null) {
            logger.error("Unhandled Bacta command" + Integer.toHexString(command).toUpperCase());
            logger.error(SoeMessageUtil.bytesToHex(buffer));
            return;
        }

        try {

            logger.trace("Routing to " + controller.getClass().getSimpleName());
            controller.handleIncoming(client, buffer);

        } catch (Exception e) {
            logger.error("Bacta Routing", e);
        } finally {
        }
    }

    private void loadControllers() {

        ControllerScan scanAnnotation = getClass().getAnnotation(ControllerScan.class);

        if (scanAnnotation == null) {
            logger.error("Missing @ControllerScan annotation, unable to load controllers");
            return;
        }

        Reflections reflections = new Reflections(scanAnnotation.target());

        Set<Class<? extends BactaMessageController>> subTypes = reflections.getSubTypesOf(BactaMessageController.class);

        Iterator<Class<? extends BactaMessageController>> iter = subTypes.iterator();
        while (iter.hasNext()) {

            try {
                Class<? extends BactaMessageController> controllerClass = iter.next();

                BactaController controllerAnnotation = controllerClass.getAnnotation(BactaController.class);

                if (controllerAnnotation == null) {
                    logger.info("Missing @BactaController annotation, discarding: " + controllerClass.getName());
                    continue;
                }

                BactaMessageController controller = injector.getInstance(controllerClass);

                if (controllerAnnotation.server() == serverType && !controllers.containsKey((short) controllerAnnotation.command())) {
                    logger.debug("Adding Bacta controller for " + serverType + ": " + controllerClass.getSimpleName());

                    synchronized (controllers) {
                        controllers.put((short) controllerAnnotation.command(), controller);
                    }
                }
            } catch (Exception e) {
                logger.error("Unable to add controller", e);
            }
        }
    }
}
