package com.ocdsoft.bacta.soe.controller;

import com.google.inject.Inject;
import com.ocdsoft.bacta.swg.network.soe.ServerState;
import com.ocdsoft.bacta.swg.network.soe.SoeController;
import com.ocdsoft.bacta.swg.network.soe.buffer.SoeByteBuf;
import com.ocdsoft.bacta.swg.network.soe.client.SoeUdpClient;
import com.ocdsoft.bacta.swg.network.soe.message.BactaMessage;
import com.ocdsoft.bacta.swg.network.soe.router.BactaMessageRouter;
import com.ocdsoft.bacta.swg.network.soe.router.BactaMessageRouterFactory;

@SoeController(opcode = 0x4, handles = BactaMessage.class)
public class BactaNetworkController extends SoeMessageController {

    private BactaMessageRouter router;

    @Inject
    public BactaNetworkController(BactaMessageRouterFactory bactaRouterFactory, ServerState serverState) {
        router = bactaRouterFactory.create(serverState.getServerType());
    }

    @Override
    public void handleIncoming(SoeUdpClient client, SoeByteBuf buffer) {

        try {
            short command = buffer.readShort();

            router.routeMessage(command, client, buffer);
        } catch (Exception e) {
            logger.error("Bacta Routing", e);
        }
    }
}
