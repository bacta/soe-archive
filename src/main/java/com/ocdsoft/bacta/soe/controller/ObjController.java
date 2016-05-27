package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

public interface ObjController<Message, Object> {
    void handleIncoming(final SoeUdpConnection connection, final Message message, final Object invoker);
}
