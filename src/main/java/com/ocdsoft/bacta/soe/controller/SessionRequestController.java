package com.ocdsoft.bacta.soe.controller;

import com.google.inject.Inject;
import com.ocdsoft.bacta.engine.network.client.ClientState;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.SessionResponse;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.protocol.SoeProtocol;
import com.ocdsoft.bacta.soe.service.SessionKeyService;

import java.nio.ByteBuffer;

@SoeController(handles = {UdpPacketType.cUdpPacketConnect})
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
    public void handleIncoming(SoeUdpConnection client, ByteBuffer buffer) {

        int crcLength = buffer.getInt();
        int connectionId = buffer.getInt();
        int udpSize = buffer.getInt();
        int sessionKey = keyService.getNextKey();

        client.setConnectionId(connectionId);
        client.setSessionKey(sessionKey);
        client.setClientState(ClientState.ONLINE);
        client.setUdpSize(udpSize);

        SessionResponse response = new SessionResponse(crcLength, connectionId, sessionKey, cryptMethod, useComp, udpSize);
        client.sendMessage(response);
    }

}
