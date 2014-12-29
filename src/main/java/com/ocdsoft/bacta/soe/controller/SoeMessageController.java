package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.buffer.BactaBuffer;
import com.ocdsoft.bacta.engine.network.controller.MessageController;
import com.ocdsoft.bacta.soe.client.SoeUdpClient;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SoeMessageController implements MessageController<SoeUdpClient, BactaBuffer> {

    protected Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    public void setRouter(SoeMessageRouter soeMessageRouter) {

    }

}
