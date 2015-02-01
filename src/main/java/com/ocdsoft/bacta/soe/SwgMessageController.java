package com.ocdsoft.bacta.soe;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

public interface SwgMessageController<Data extends GameNetworkMessage>  {
    void handleIncoming(SoeUdpConnection connection, Data message) throws Exception;
}
