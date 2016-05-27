package com.ocdsoft.bacta.soe.dispatch;

import com.google.inject.Inject;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.controller.GameClientMessageController;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.serialize.GameNetworkMessageSerializer;
import gnu.trove.map.TIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by crush on 5/26/2016.
 */
public final class GameClientMessageDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameClientMessageDispatcher.class);

    private final TIntObjectMap<ControllerData> controllers;
    private final GameNetworkMessageSerializer gameNetworkMessageSerializer;
    private final ServerState serverState;

    @Inject
    public GameClientMessageDispatcher(final ClasspathControllerLoader controllerLoader,
                                       final ServerState serverState,
                                       final GameNetworkMessageSerializer gameNetworkMessageSerializer) {

        this.gameNetworkMessageSerializer = gameNetworkMessageSerializer;
        this.controllers = controllerLoader.getControllers(GameClientMessageController.class);
        this.serverState = serverState;

        controllers.forEachEntry((key, data) -> {
            LOGGER.info("Loaded GameClientMessageController {} for messageType {}", data.getController().getClass().getSimpleName(), key);
            return true;
        });
    }

    public void dispatch(final long[] distributionList, final boolean reliable, final ByteBuffer internalMessage, final SoeUdpConnection connection) {
        final int messageType = internalMessage.getInt();

        final ControllerData<GameClientMessageController> controllerData = controllers.get(messageType);

        if (controllerData != null) {
            if (!controllerData.containsRoles(connection.getRoles())) {
                LOGGER.error("{} Controller security blocked access: {}", serverState.getServerType(), controllerData.getController().getClass().getName());
                LOGGER.error("Connection: " + connection.toString());
                return;
            }

            try {
                final GameClientMessageController controller = controllerData.getController();
                final GameNetworkMessage message = gameNetworkMessageSerializer.readFromBuffer(messageType, internalMessage);

                LOGGER.debug("Dispatching GameClientMessage with client message {} to {} clients.",
                        message.getClass().getSimpleName(),
                        distributionList.length);

                controller.handleIncoming(distributionList, reliable, connection, message);

            } catch (Exception e) {
                LOGGER.error("SWG Message Handling {}", controllerData.getClass(), e);
            }
        } else {
            LOGGER.error("Missing controller for GameClientMessage with messageType {}", messageType);
        }
    }
}
