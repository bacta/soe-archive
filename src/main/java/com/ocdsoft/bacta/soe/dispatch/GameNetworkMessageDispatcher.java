package com.ocdsoft.bacta.soe.dispatch;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.*;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.controller.CommandController;
import com.ocdsoft.bacta.soe.controller.GameNetworkMessageController;
import com.ocdsoft.bacta.soe.controller.ObjController;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.serialize.GameNetworkMessageSerializer;
import com.ocdsoft.bacta.soe.serialize.GameNetworkMessageTypeNotFoundException;
import com.ocdsoft.bacta.soe.util.*;
import gnu.trove.map.TIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * GameNetworkMessageDispatcher receives and dispatches {@link GameNetworkMessage} instances.  It is able to process
 * messages based the the incoming priority and then create and dispatch the message to be handled by the
 * {@link GameNetworkMessageController} instances.  GameNetworkMessageDispatcher also will generate controllers and
 * messages if they are not recognized to assist in development
 *
 * @author Kyle Burkhardt
 * @since 1.0
 */

@Singleton
public class GameNetworkMessageDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameNetworkMessageDispatcher.class);
    private final static int OBJECT_CONTROLLER_MESSAGE = 0x80CE5E46;
    private final static int COMMAND_CONTROLLER_MESSAGE = 0x116;

    /**
     *  Map of controller data to dispatch messages
     */
    private final TIntObjectMap<ControllerData> controllers;

    /**
     * Creates the {@link GameNetworkMessage} to be passed to the appropriate controller
     */
    private final GameNetworkMessageSerializer gameNetworkMessageSerializer;


    /**
     * Generates missing {@link GameNetworkMessage} and {@link GameNetworkMessageController} classes for implementation
     * Writes files directly to the project structure
     */
    private final GameNetworkMessageTemplateWriter gameNetworkMessageTemplateWriter;

    private final ServerState serverState;

    @Inject
    public GameNetworkMessageDispatcher(final ClasspathControllerLoader controllerLoader,
                                        final ServerState serverState,
                                        final GameNetworkMessageSerializer gameNetworkMessageSerializer,
                                        final GameNetworkMessageTemplateWriter gameNetworkMessageTemplateWriter) {

        this.gameNetworkMessageSerializer = gameNetworkMessageSerializer;
        this.serverState = serverState;
        this.gameNetworkMessageTemplateWriter = gameNetworkMessageTemplateWriter;

        controllers = controllerLoader.getControllers(GameNetworkMessageController.class);
    }

    public void dispatch(short priority, int gameMessageType, SoeUdpConnection connection, ByteBuffer buffer) {

        connection.increaseGameNetworkMessageReceived();

        int internalMessageType = gameMessageType;
        if(gameMessageType == OBJECT_CONTROLLER_MESSAGE) {
            internalMessageType = buffer.getInt(4);

            if (internalMessageType == COMMAND_CONTROLLER_MESSAGE) {
                internalMessageType = buffer.getInt(24);
            }
        }

        ControllerData<GameNetworkMessageController> controllerData = controllers.get(gameMessageType);
        if (controllerData != null) {
            if (!controllerData.containsRoles(connection.getRoles())) {
                LOGGER.error("{} Controller security blocked access: {}", serverState.getServerType(), controllerData.getController().getClass().getName());
                LOGGER.error("Connection: " + connection.toString());
                return;
            }

            try {
                GameNetworkMessageController controller = controllerData.getController();
                GameNetworkMessage incomingMessage = gameNetworkMessageSerializer.readFromBuffer(internalMessageType, buffer);

                LOGGER.trace("[{}] received {}", serverState.getServerType().name(), incomingMessage.getClass().getSimpleName());

                LOGGER.debug("Routing to " + controller.getClass().getSimpleName());
                controller.handleIncoming(connection, incomingMessage);


            } catch (GameNetworkMessageTypeNotFoundException e) {
                handleMissingController(priority, gameMessageType, buffer);
            } catch (Exception e) {
                LOGGER.error("SWG Message Handling {}", controllerData.getClass(), e);
            }

        } else {
            handleMissingController(priority, gameMessageType, buffer);
        }
    }

    private void handleMissingController(short priority, int gameMessageType, ByteBuffer buffer) {

        if(gameMessageType == OBJECT_CONTROLLER_MESSAGE) {
            int objcType = buffer.getInt(4);

            if(objcType == COMMAND_CONTROLLER_MESSAGE) {
                int commandHash = buffer.getInt(24);

                String propertyName = Integer.toHexString(commandHash);
                gameNetworkMessageTemplateWriter.createCommandFiles(commandHash, buffer);
                LOGGER.error("{} Unhandled Command Message: 0x{}", CommandNames.get(propertyName), propertyName);
                LOGGER.error(SoeMessageUtil.bytesToHex(buffer));

            } else {
                String propertyName = Integer.toHexString(objcType);

                gameNetworkMessageTemplateWriter.createObjFiles(objcType, buffer);
                LOGGER.error("{} Unhandled ObjC Message: 0x{}", ObjectControllerNames.get(propertyName), propertyName);
                LOGGER.error(SoeMessageUtil.bytesToHex(buffer));
            }
        } else {

            String propertyName = Integer.toHexString(gameMessageType);
            gameNetworkMessageTemplateWriter.createGameNetworkMessageFiles(priority, gameMessageType, buffer);
            LOGGER.error("{} Unhandled SWG Message: '{}' 0x{}", serverState.getServerType(), ClientString.get(propertyName), propertyName);
            LOGGER.error(SoeMessageUtil.bytesToHex(buffer));
        }
    }
}
