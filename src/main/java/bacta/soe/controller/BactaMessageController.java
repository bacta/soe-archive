package bacta.soe.controller;


import bacta.buffer.BactaBuffer;
import bacta.network.controller.MessageController;
import bacta.soe.client.SoeUdpClient;

public abstract class BactaMessageController implements MessageController<SoeUdpClient, BactaBuffer> {

}
