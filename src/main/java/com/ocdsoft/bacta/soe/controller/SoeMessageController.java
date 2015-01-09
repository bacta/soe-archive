package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.network.controller.MessageController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

import java.nio.ByteBuffer;

public interface SoeMessageController extends MessageController<SoeUdpConnection, ByteBuffer> {

}
