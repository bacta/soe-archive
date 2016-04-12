package com.ocdsoft.bacta.soe.dispatch;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.*;
import com.ocdsoft.bacta.soe.connection.ConnectionRole;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.factory.GameNetworkMessageFactory;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.util.ClientString;
import com.ocdsoft.bacta.soe.util.GameNetworkMessageTemplateWriter;
import com.ocdsoft.bacta.soe.util.SOECRC32;
import com.ocdsoft.bacta.soe.util.SoeMessageUtil;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * GameNetworkDevMessageDispatcher receives and dispatches {@link GameNetworkMessage} instances.  It is able to process
 * messages based the the incoming priority and then create and dispatch the message to be handled by the
 * {@link GameNetworkMessageController} instances.  GameNetworkDevMessageDispatcher also will generate controllers and
 * messages if they are not recognized to assist in development
 *
 * @author Kyle Burkhardt
 * @since 1.0
 */

@Singleton
public final class GameNetworkDevMessageDispatcher implements GameNetworkMessageDispatcher<ByteBuffer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameNetworkDevMessageDispatcher.class);

    /**
     * Holds the controller references in a map to be looked up and used for processing incoming messages
     */
    private final TIntObjectMap<ControllerData> controllers = new TIntObjectHashMap<>();

    /**
     * Creates the {@link GameNetworkMessage} to be passed to the appropriate controller
     */
    private final GameNetworkMessageFactory gameNetworkMessageFactory;

    /**
     * Generates missing {@link GameNetworkMessage} and {@link GameNetworkMessageController} classes for implementation
     * Writes files directly to the project structure
     */
    private final GameNetworkMessageTemplateWriter gameNetworkMessageTemplateWriter;

    public GameNetworkDevMessageDispatcher(final Injector injector,
                                           final GameNetworkMessageFactory gameNetworkMessageFactory,
                                           final GameNetworkMessageTemplateWriter gameNetworkMessageTemplateWriter,
                                           final Collection<String> swgControllerClasspaths) {

        this.gameNetworkMessageFactory = gameNetworkMessageFactory;
        this.gameNetworkMessageTemplateWriter = gameNetworkMessageTemplateWriter;

        loadControllers(injector, swgControllerClasspaths);
    }

    @Override
    public void dispatch(short priority, int gameMessageType, SoeUdpConnection connection, ByteBuffer buffer) {

        ControllerData controllerData = controllers.get(gameMessageType);
        if(controllerData != null) {
            if(!hasControllerAccess(connection, controllerData)) {
                LOGGER.error("Controller security blocked access:" + controllerData.getGameNetworkMessageController().getClass().getName());
                LOGGER.error("Connection: " + connection.toString());
                return;
            }
            
            connection.increaseGameNetworkMessageReceived();

            GameNetworkMessageController controller = controllerData.getGameNetworkMessageController();
            GameNetworkMessage incomingMessage = gameNetworkMessageFactory.create(gameMessageType, buffer);

            if(incomingMessage != null) {
                incomingMessage.readFromBuffer(buffer);

                try {

                    LOGGER.debug("Routing to " + controller.getClass().getSimpleName());

                    controller.handleIncoming(connection, incomingMessage);

                } catch (Exception e) {
                    LOGGER.error("SWG Message Handling", e);
                }
            } else {

            }
        } else {
            handleMissingController(gameMessageType, buffer);
        }
    }

    private void loadControllers(final Injector injector, final Collection<String> swgControllerClasspaths) {

        for(String classPath : swgControllerClasspaths) {

            try {
            
                Class<? extends GameNetworkMessageController> controllerClass = (Class<? extends GameNetworkMessageController>) Class.forName(classPath);

                LOGGER.info("Loading GameNetworkMessageController '{}'", classPath);

                loadControllerClass(injector, controllerClass);
                continue;
                
            } catch (ClassNotFoundException e) {  }

            LOGGER.info("Loading GameNetworkMessageControllers from classpath: '{}'", classPath);

            Reflections reflections = new Reflections(classPath);

            Set<Class<? extends GameNetworkMessageController>> subTypes = reflections.getSubTypesOf(GameNetworkMessageController.class);

            Iterator<Class<? extends GameNetworkMessageController>> iter = subTypes.iterator();

            while (iter.hasNext()) {
                Class<? extends GameNetworkMessageController> controllerClass = iter.next();
                loadControllerClass(injector, controllerClass);
            }
        }
    }

    private boolean hasControllerAccess(SoeUdpConnection connection, ControllerData controllerData) {
        return controllerData.containsRoles(connection.getRoles());
    }

    private void handleMissingController(int opcode, ByteBuffer buffer) {

        gameNetworkMessageTemplateWriter.createFiles(opcode, buffer);

        String propertyName = Integer.toHexString(opcode);

        LOGGER.error("Unhandled SWG Message: '" + ClientString.get(propertyName) + "' 0x" + propertyName);
        LOGGER.error(SoeMessageUtil.bytesToHex(buffer));
    }
    
    private void loadControllerClass(final Injector injector, Class<? extends GameNetworkMessageController> controllerClass) {

        try {
            
            if (Modifier.isAbstract(controllerClass.getModifiers())) {
                return;
            }

            GameNetworkMessageHandled controllerAnnotation = controllerClass.getAnnotation(GameNetworkMessageHandled.class);

            if (controllerAnnotation == null) {
                LOGGER.warn("Missing @GameNetworkMessageHandled annotation, discarding: " + controllerClass.getName());
                return;
            }


            RolesAllowed rolesAllowed = controllerClass.getAnnotation(RolesAllowed.class);
            if (rolesAllowed == null) {
                LOGGER.warn("Missing @RolesAllowed annotation, discarding: " + controllerClass.getName());
                return;
            }

            Class<?> handledMessageClass = controllerAnnotation.value();


            ConnectionRole[] connectionRoles = rolesAllowed.value();
            GameNetworkMessageController controller = injector.getInstance(controllerClass);

            int hash = SOECRC32.hashCode(handledMessageClass.getSimpleName());
            Constructor constructor = handledMessageClass.getConstructor();

            ControllerData newControllerData = new ControllerData(controller, constructor, connectionRoles);

            if (!controllers.containsKey(hash)) {
                String propertyName = Integer.toHexString(hash);
                LOGGER.debug("Adding Controller for " + serverEnv + ": " + controllerClass.getName() + " " + ClientString.get(propertyName) + "' 0x" + propertyName);

                synchronized (controllers) {
                    controllers.put(hash, newControllerData);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to add controller: " + controllerClass.getName(), e);
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
