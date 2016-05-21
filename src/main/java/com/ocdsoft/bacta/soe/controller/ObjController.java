package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.network.controller.Controller;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

public interface ObjController<Message, Object> {
    public void handleIncoming(final SoeUdpConnection connection, final Message message, final Object invoker);
}
