package bacta.soe.disruptor;

import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import com.ocdsoft.bacta.swg.network.soe.ServerState;
import com.ocdsoft.bacta.swg.network.soe.buffer.SoeByteBuf;
import com.ocdsoft.bacta.swg.network.soe.client.SoeUdpClient;
import com.ocdsoft.bacta.swg.network.soe.router.SoeMessageRouter;
import com.ocdsoft.bacta.swg.network.soe.router.SoeMessageRouterFactory;
import com.ocdsoft.bacta.swg.network.swg.router.SwgMessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalInputEventHandler<T extends SoeUdpClient> implements EventHandler<SoeInputEvent<T>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final SoeMessageRouter soeRouter;
    private final SwgMessageRouter swgRouter;
    private final ServerState serverState;

    @Inject
    public LocalInputEventHandler(SoeMessageRouterFactory routerFactory, ServerState serverState) {

        soeRouter = routerFactory.create(serverState.getServerType());
        swgRouter = soeRouter.getSwgRouter();
        this.serverState = serverState;
    }

    @Override
    public void onEvent(SoeInputEvent<T> event, long sequence, boolean endOfBatch)
            throws Exception {

        io.netty.buffer.ByteBuf buffer = event.getBuffer();
        T client = event.getClient();
        SoeByteBuf bactaBuffer = new SoeByteBuf(buffer);

        if (event.isSwgMessage()) {

            int opcode = bactaBuffer.readInt();

            swgRouter.routeMessage(opcode, client, bactaBuffer);

        } else {

            soeRouter.routeMessage(bactaBuffer.readShortBE(), client, bactaBuffer);

        }
    }

}
