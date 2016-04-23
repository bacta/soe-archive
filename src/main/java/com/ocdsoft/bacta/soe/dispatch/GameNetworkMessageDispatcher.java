package com.ocdsoft.bacta.soe.dispatch;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.*;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.controller.GameNetworkMessageController;
import com.ocdsoft.bacta.soe.factory.GameNetworkMessageFactory;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.util.ClientString;
import com.ocdsoft.bacta.soe.util.GameNetworkMessageTemplateWriter;
import com.ocdsoft.bacta.soe.util.SoeMessageUtil;
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

    /**
     *  Map of controller data to dispatch messages
     */
    private final TIntObjectMap<ControllerData> controllers;

    /**
     * Creates the {@link GameNetworkMessage} to be passed to the appropriate controller
     */
    private final GameNetworkMessageFactory gameNetworkMessageFactory;

    /**
     * Generates missing {@link GameNetworkMessage} and {@link GameNetworkMessageController} classes for implementation
     * Writes files directly to the project structure
     */
    private final GameNetworkMessageTemplateWriter gameNetworkMessageTemplateWriter;

    private final ServerState serverState;

    @Inject
    public GameNetworkMessageDispatcher(final ClasspathControllerLoader<GameNetworkMessageController> controllerLoader,
                                        final ServerState serverState,
                                        final GameNetworkMessageFactory gameNetworkMessageFactory,
                                        final GameNetworkMessageTemplateWriter gameNetworkMessageTemplateWriter) {

        this.gameNetworkMessageFactory = gameNetworkMessageFactory;
        this.serverState = serverState;
        this.gameNetworkMessageTemplateWriter = gameNetworkMessageTemplateWriter;

        controllers = controllerLoader.getControllers(GameNetworkMessageController.class);
    }

    public void dispatch(short priority, int gameMessageType, SoeUdpConnection connection, ByteBuffer buffer) {

        ControllerData<GameNetworkMessageController> controllerData = controllers.get(gameMessageType);
        if(controllerData != null) {
            if(!controllerData.containsRoles(connection.getRoles())) {
                LOGGER.error("{} Controller security blocked access: {}", serverState.getServerType(), controllerData.getController().getClass().getName());
                LOGGER.error("Connection: " + connection.toString());
                return;
            }
            
            connection.increaseGameNetworkMessageReceived();

            GameNetworkMessageController controller = controllerData.getController();
            GameNetworkMessage incomingMessage = gameNetworkMessageFactory.createAndDeserialize(gameMessageType, buffer);

            try {

                LOGGER.debug("Routing to " + controller.getClass().getSimpleName());

                controller.handleIncoming(connection, incomingMessage);

            } catch (Exception e) {
                LOGGER.error("SWG Message Handling", e);
            }

        } else {
            handleMissingController(gameMessageType, buffer);
        }
    }

    private void handleMissingController(int opcode, ByteBuffer buffer) {

        gameNetworkMessageTemplateWriter.createGameNetworkMessageFiles(opcode, buffer);

        String propertyName = Integer.toHexString(opcode);

        LOGGER.error("{} Unhandled SWG Message: '{}' 0x{}", serverState.getServerType(), ClientString.get(propertyName), propertyName);
        LOGGER.error(SoeMessageUtil.bytesToHex(buffer));
    }
}
