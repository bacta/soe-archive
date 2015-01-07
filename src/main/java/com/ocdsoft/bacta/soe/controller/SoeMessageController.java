package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.network.controller.MessageController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public abstract class SoeMessageController implements MessageController<SoeUdpConnection, ByteBuffer> {

    protected Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    public void setRouter(SoeMessageRouter soeMessageRouter) {

    }

}
