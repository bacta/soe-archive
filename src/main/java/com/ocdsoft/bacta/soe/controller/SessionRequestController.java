package com.ocdsoft.bacta.soe.controller;

import com.google.inject.Inject;
import com.ocdsoft.bacta.engine.buffer.BactaBuffer;
import com.ocdsoft.bacta.engine.network.client.ClientState;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.client.SoeUdpClient;
import com.ocdsoft.bacta.soe.message.SessionRequest;
import com.ocdsoft.bacta.soe.message.SessionResponse;
import com.ocdsoft.bacta.soe.protocol.SoeProtocol;
import com.ocdsoft.bacta.soe.service.SessionKeyService;

@SoeController(opcode = 0x1, handles = SessionRequest.class)
public class SessionRequestController extends SoeMessageController {

    private SessionKeyService keyService;
    private byte cryptMethod;
    private boolean useComp;

    @Inject
    public SessionRequestController(SessionKeyService keyService, SoeProtocol protocol) {
        this.keyService = keyService;
        this.cryptMethod = protocol.getEncryptionID();
        this.useComp = true;
    }

    @Override
    public void handleIncoming(SoeUdpClient client, BactaBuffer buffer) {

        int crcLength = buffer.readIntBE();
        int connectionId = buffer.readIntBE();
        int udpSize = buffer.readIntBE();
        int sessionKey = keyService.getNextKey();

        client.setConnectionId(connectionId);
        client.setSessionKey(sessionKey);
        client.setClientState(ClientState.ONLINE);
        client.setUdpSize(udpSize);

        SessionResponse response = new SessionResponse(crcLength, connectionId, sessionKey, cryptMethod, useComp, udpSize);
        client.sendMessage(response);

    }

}
