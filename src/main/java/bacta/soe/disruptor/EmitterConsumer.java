package bacta.soe.disruptor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.lmax.disruptor.EventHandler;
import com.ocdsoft.bacta.swg.network.soe.client.SoeUdpClient;
import com.ocdsoft.network.io.udp.UdpTransceiver;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class EmitterConsumer<C extends SoeUdpClient, T extends UdpTransceiver> implements EventHandler<SoeOutputEvent<C>> {

    private final T transceiver;

    @Inject
    public EmitterConsumer(@Assisted T transceiver) {
        this.transceiver = transceiver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onEvent(SoeOutputEvent<C> event, long sequence, boolean endOfBatch)
            throws Exception {

        C client = event.getClient();

        List<ByteBuf> messageList = event.getMessageList();

        while (!messageList.isEmpty()) {

            ByteBuf message = messageList.remove(0);
            transceiver.sendMessage(client, message);
        }

    }

}
