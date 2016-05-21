package com.ocdsoft.bacta.soe.disruptor;

import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.dispatch.SoeDevMessageDispatcher;
import com.ocdsoft.bacta.soe.dispatch.ClasspathControllerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class LocalInputEventHandler<T extends SoeUdpConnection> implements EventHandler<SoeInputEvent<T>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final SoeDevMessageDispatcher soeRouter;
    private final ClasspathControllerLoader swgRouter;
    private final ServerState serverState;

    @Inject
    public LocalInputEventHandler(SoeDevMessageDispatcher soeMessageRouter, ServerState serverState) {

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

            //swgRouter.dispatch(opcode, client, bactaBuffer);

        } else {

           // soeRouter.dispatch(buffer.getShort(), client, buffer);

        }
    }

}
