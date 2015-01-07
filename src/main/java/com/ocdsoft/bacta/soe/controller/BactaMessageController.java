package com.ocdsoft.bacta.soe.controller;


import com.ocdsoft.bacta.engine.buffer.BactaBuffer;
import com.ocdsoft.bacta.engine.network.controller.MessageController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;

public abstract class BactaMessageController implements MessageController<SoeUdpConnection, BactaBuffer> {

}
