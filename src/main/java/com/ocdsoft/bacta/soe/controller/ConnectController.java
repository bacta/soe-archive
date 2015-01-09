package com.ocdsoft.bacta.soe.controller;

import com.google.inject.Inject;
import com.ocdsoft.bacta.engine.network.client.ConnectionState;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.ConfirmMessage;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.protocol.SoeProtocol;
import com.ocdsoft.bacta.soe.service.SessionKeyService;

import java.nio.ByteBuffer;

@SoeController(handles = {UdpPacketType.cUdpPacketConnect})
public class ConnectController implements SoeMessageController {

    private final SessionKeyService keyService;
    private final byte cryptMethod;
    private final byte crcLength;
    private final boolean useComp;

    @Inject
    public ConnectController(SessionKeyService keyService, SoeProtocol protocol) {
        this.keyService = keyService;
        this.cryptMethod = protocol.getEncryptionID();
        this.crcLength = 2;
        this.useComp = true;
    }

    @Override
    public void handleIncoming(SoeUdpConnection connection, ByteBuffer buffer) {

        int protocolVersion = buffer.getInt();
        int connectionId = buffer.getInt();
        int udpSize = buffer.getInt();
        int sessionKey = keyService.getNextKey();

        connection.setId(connectionId);
        connection.setSessionKey(sessionKey);
        connection.setConnectionState(ConnectionState.ONLINE);
        connection.setUdpSize(udpSize);

        ConfirmMessage response = new ConfirmMessage(crcLength, connectionId, sessionKey, cryptMethod, useComp, udpSize);
        connection.sendMessage(response);
    }
}
