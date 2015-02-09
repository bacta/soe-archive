package com.ocdsoft.bacta.soe.router;

import com.google.inject.Injector;
import com.ocdsoft.bacta.soe.*;
import com.ocdsoft.bacta.soe.connection.ConnectionRole;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.util.ClientString;
import com.ocdsoft.bacta.soe.util.SOECRC32;
import com.ocdsoft.bacta.soe.util.SoeMessageUtil;
import com.ocdsoft.bacta.soe.util.SwgMessageTemplateWriter;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class SwgDevelopMessageRouter implements SwgMessageRouter {
    private static final Logger logger = LoggerFactory.getLogger(SwgDevelopMessageRouter.class);

    private final boolean developmentMode;

    private final TIntObjectMap<ControllerData> controllers = new TIntObjectHashMap<>();

    private final ServerType serverEnv;

    private final SwgMessageTemplateWriter swgMessageTemplateWriter;

    public SwgDevelopMessageRouter(final Injector injector,
                                   final ServerState serverState,
                                   final String controllerFileName,
                                   final boolean developmentMode) {

        this.serverEnv = serverState.getServerType();
        this.developmentMode = developmentMode;
        swgMessageTemplateWriter = new SwgMessageTemplateWriter(serverEnv);

        loadControllers(injector, controllerFileName);
    }

    @Override
    public void routeMessage(byte priority, int opcode, SoeUdpConnection connection, ByteBuffer buffer) {

        ControllerData controllerData = controllers.get(opcode);
        if(controllerData != null) {
            if(!hasControllerAccess(connection, controllerData)) {
                logger.error("Controller security blocked access:" + controllerData.getGameNetworkMessageController().getClass().getName());
                logger.error("Connection: " + connection.toString());
                return;
            }
            
            connection.increaseGameNetworkMessageReceived();

            GameNetworkMessageController controller = controllerData.getGameNetworkMessageController();
            Constructor<? extends GameNetworkMessage> constructor = controllerData.getConstructor();
            try {

                GameNetworkMessage message = constructor.newInstance(buffer);

                try {

                    logger.debug("Routing to " + controller.getClass().getSimpleName());

                    controller.handleIncoming(connection, message);

                } catch (Exception e) {
                    logger.error("SWG Message Handling", e);
                }


            } catch (Exception e) {
                logger.error("Unable to create incoming message", e);
            }
        } else {
            handleMissingController(opcode, buffer);
        }
    }

    private boolean hasControllerAccess(SoeUdpConnection connection, ControllerData controllerData) {
        return controllerData.containsRoles(connection.getRoles());
    }

    private void handleMissingController(int opcode, ByteBuffer buffer) {

        if(developmentMode) {
            swgMessageTemplateWriter.createFiles(opcode, buffer);
        }

        String propertyName = Integer.toHexString(opcode);

        logger.error("Unhandled SWG Message: '" + ClientString.get(propertyName) + "' 0x" + propertyName);
        logger.error(SoeMessageUtil.bytesToHex(buffer));
    }

    private void loadControllers(final Injector injector, final String controllerFileName) {

        String projectClassPath = System.getProperty("base.classpath");
        Reflections reflections = new Reflections(projectClassPath + ".controller." + serverEnv.name().toLowerCase());

        Set<Class<? extends GameNetworkMessageController>> subTypes = reflections.getSubTypesOf(GameNetworkMessageController.class);

        Iterator<Class<? extends GameNetworkMessageController>> iter = subTypes.iterator();

        while (iter.hasNext()) {

            Class<? extends GameNetworkMessageController> controllerClass = iter.next();

            try {


                if (Modifier.isAbstract(controllerClass.getModifiers())) {
                    continue;
                }

                GameNetworkMessageHandled controllerAnnotation = controllerClass.getAnnotation(GameNetworkMessageHandled.class);

                if (controllerAnnotation == null) {
                    logger.warn("Missing @SwgController annotation, discarding: " + controllerClass.getName());
                    continue;
                }


                RolesAllowed rolesAllowed = controllerClass.getAnnotation(RolesAllowed.class);
                if (rolesAllowed == null) {
                    logger.warn("Missing @RolesAllowed annotation, discarding: " + controllerClass.getName());
                    continue;
                }

                Class<?> handledMessageClass = controllerAnnotation.value();


                ConnectionRole[] connectionRoles = rolesAllowed.value();
                GameNetworkMessageController controller = injector.getInstance(controllerClass);

                int hash = SOECRC32.hashCode(handledMessageClass.getSimpleName());
                Constructor constructor = handledMessageClass.getConstructor(ByteBuffer.class);

                ControllerData newControllerData = new ControllerData(controller, constructor, connectionRoles);

                if (!controllers.containsKey(hash)) {
                    String propertyName = Integer.toHexString(hash);
                    logger.debug("Adding Controller for " + serverEnv + ": " + controllerClass.getName() + " " + ClientString.get(propertyName) + "' 0x" + propertyName);

                    synchronized (controllers) {
                        controllers.put(hash, newControllerData);
                    }
                }
            } catch (Exception e) {
                logger.error("Unable to add controller: " + controllerClass.getName(), e);
            }
        }
    }

    private class ControllerData {
        @Getter
        private final GameNetworkMessageController gameNetworkMessageController;

        @Getter
        private final Constructor constructor;

        @Getter
        private final ConnectionRole[] roles;

        public ControllerData(final GameNetworkMessageController gameNetworkMessageController,
                              final Constructor constructor,
                              final ConnectionRole[] roles) {
            this.gameNetworkMessageController = gameNetworkMessageController;
            this.constructor = constructor;
            this.roles = roles;

        }

        public boolean containsRoles(List<ConnectionRole> userRoles) {
            
            for(ConnectionRole role : roles) {
                if(userRoles.contains(role)) {
                    return true;
                }
            }
            
            return roles.length == 0;
        }
    }
}
