package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

/**
 * Created by crush on 5/26/2016.
 */
public interface GameClientMessageController<Data extends GameNetworkMessage> {
    void handleIncoming(long[] distributionList, boolean reliable, SoeUdpConnection connection, Data message) throws Exception;
}
