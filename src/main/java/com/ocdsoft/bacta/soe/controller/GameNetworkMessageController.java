package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

public interface GameNetworkMessageController<Data extends GameNetworkMessage>  {
    void handleIncoming(SoeUdpConnection connection, Data message) throws Exception;
}
