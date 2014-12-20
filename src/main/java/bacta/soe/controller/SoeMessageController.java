package bacta.soe.controller;

import bacta.buffer.BactaBuffer;
import bacta.network.controller.MessageController;
import bacta.soe.client.SoeUdpClient;
import bacta.soe.router.SoeMessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SoeMessageController implements MessageController<SoeUdpClient, BactaBuffer> {

    protected Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    public void setRouter(SoeMessageRouter soeMessageRouter) {

    }

}
