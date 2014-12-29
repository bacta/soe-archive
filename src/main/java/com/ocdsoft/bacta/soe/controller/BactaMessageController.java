package com.ocdsoft.bacta.soe.controller;


import com.ocdsoft.bacta.engine.buffer.BactaBuffer;
import com.ocdsoft.bacta.engine.network.controller.MessageController;
import com.ocdsoft.bacta.soe.client.SoeUdpClient;

public abstract class BactaMessageController implements MessageController<SoeUdpClient, BactaBuffer> {

}
