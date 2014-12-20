package bacta.soe.controller;

import bacta.buffer.BactaBuffer;
import bacta.network.client.ClientState;
import bacta.soe.SoeController;
import bacta.soe.client.SoeUdpClient;
import bacta.soe.message.SessionRequest;
import bacta.soe.message.SessionResponse;
import bacta.soe.protocol.SoeProtocol;
import bacta.soe.service.SessionKeyService;
import com.google.inject.Inject;

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
