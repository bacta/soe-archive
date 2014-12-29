package com.ocdsoft.bacta.soe.router;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.ocdsoft.bacta.engine.buffer.BactaBuffer;
import com.ocdsoft.bacta.engine.network.ControllerScan;
import com.ocdsoft.bacta.engine.network.router.ShortMessageRouter;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.client.SoeUdpClient;
import com.ocdsoft.bacta.soe.controller.SoeMessageController;
import com.ocdsoft.bacta.soe.utils.SoeMessageUtil;
import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import lombok.Getter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;


@ControllerScan(target = "com.ocdsoft.bacta")
public class SoeMessageRouter implements ShortMessageRouter<SoeUdpClient, BactaBuffer> {

    private Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private Injector injector;

    private TShortObjectMap<SoeMessageController> controllers = new TShortObjectHashMap<SoeMessageController>();

    @Getter
    private final SwgMessageRouter swgRouter;

    @Inject
    public SoeMessageRouter(Injector injector, SwgMessageRouterFactory factory, ServerState serverState) {
        this.injector = injector;
        swgRouter = factory.create(serverState.getServerType());
        loadControllers();
    }

    @Override
    public void routeMessage(short opcode, SoeUdpClient client, BactaBuffer buffer) {
        SoeMessageController controller = controllers.get(opcode);

        if (controller == null) {
            logger.error("Unhandled SOE Opcode 0x" + Integer.toHexString(opcode).toUpperCase());
            logger.error(SoeMessageUtil.bytesToHex(buffer));
            return;
        }

        try {

            //logger.trace("Routing to " + controller.getClass().getSimpleName());
            controller.handleIncoming(client, buffer);

        } catch (Exception e) {
            logger.error("SOE Routing", e);
        }
    }

    private void loadControllers() {

        ControllerScan scanAnnotiation = getClass().getAnnotation(ControllerScan.class);

        if (scanAnnotiation == null) {
            logger.error("Missing @ControllerScan annotation, unable to load controllers");
            return;
        }

        Reflections reflections = new Reflections(scanAnnotiation.target());

        Set<Class<? extends SoeMessageController>> subTypes = reflections.getSubTypesOf(SoeMessageController.class);

        Iterator<Class<? extends SoeMessageController>> iter = subTypes.iterator();
        while (iter.hasNext()) {

            try {
                Class<? extends SoeMessageController> controllerClass = iter.next();

                SoeController controllerAnnotiation = controllerClass.getAnnotation(SoeController.class);

                if (controllerAnnotiation == null) {
                    logger.info("Missing @SoeController annotation, discarding: " + controllerClass.getName());
                    continue;
                }

                SoeMessageController controller = injector.getInstance(controllerClass);
                controller.setRouter(this);

                if (!controllers.containsKey(controllerAnnotiation.opcode())) {
                    //logger.debug("Adding SOE controller for: " + handledMessageClass.getSimpleName());

                    synchronized (controllers) {
                        controllers.put(controllerAnnotiation.opcode(), controller);
                    }
                }
            } catch (Exception e) {
                logger.error("Unable to add controller", e);
            }
        }
    }
}
