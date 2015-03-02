package com.ocdsoft.bacta.soe.disruptor;

import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.router.SoeDevelopMessageRouter;
import com.ocdsoft.bacta.soe.router.SwgMessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class LocalInputEventHandler<T extends SoeUdpConnection> implements EventHandler<SoeInputEvent<T>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final SoeDevelopMessageRouter soeRouter;
    private final SwgMessageRouter swgRouter;
    private final ServerState serverState;

    @Inject
    public LocalInputEventHandler(SoeDevelopMessageRouter soeMessageRouter, ServerState serverState) {

        soeRouter = soeMessageRouter;
        swgRouter = null;
        this.serverState = serverState;
    }

    @Override
    public void onEvent(SoeInputEvent<T> event, long sequence, boolean endOfBatch)
            throws Exception {

        ByteBuffer buffer = event.getBuffer();
        T client = event.getClient();

        if (event.isSwgMessage()) {

            int opcode = buffer.getInt();

            //swgRouter.routeMessage(opcode, client, bactaBuffer);

        } else {

           // soeRouter.routeMessage(buffer.getShort(), client, buffer);

        }
    }

}
