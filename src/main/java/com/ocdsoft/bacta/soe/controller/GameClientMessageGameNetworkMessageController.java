package com.ocdsoft.bacta.soe.controller;

import com.google.inject.Inject;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.connection.ConnectionRole;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.dispatch.GameClientMessageDispatcher;
import com.ocdsoft.bacta.soe.message.GameClientMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by crush on 5/23/2016.
 * <p>
 * Handles {@link GameClientMessage}, and then dispatches the internal message to the
 * loaded {@link GameClientMessageController}.
 */
@MessageHandled(handles = GameClientMessage.class, type = {ServerType.GAME, ServerType.CHAT})
@ConnectionRolesAllowed(value = ConnectionRole.AUTHENTICATED)
public final class GameClientMessageGameNetworkMessageController implements GameNetworkMessageController<GameClientMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameClientMessageGameNetworkMessageController.class);

    private final GameClientMessageDispatcher gameClientMessageDispatcher;

    @Inject
    public GameClientMessageGameNetworkMessageController(final GameClientMessageDispatcher gameClientMessageDispatcher) {
        this.gameClientMessageDispatcher = gameClientMessageDispatcher;
    }

    @Override
    public void handleIncoming(final SoeUdpConnection connection, final GameClientMessage message) throws Exception {
        LOGGER.debug("Handling GameClientMessage");

        gameClientMessageDispatcher.dispatch(
                message.getDistributionList(),
                message.isReliable(),
                message.getInternalMessage(),
                connection);
    }
}