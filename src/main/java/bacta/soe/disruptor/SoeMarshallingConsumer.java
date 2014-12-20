package bacta.soe.disruptor;

import com.google.inject.Inject;
import com.lmax.disruptor.EventHandler;
import com.ocdsoft.bacta.swg.network.soe.client.SoeUdpClient;
import com.ocdsoft.network.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SoeMarshallingConsumer<T extends SoeUdpClient> implements EventHandler<SoeOutputEvent<T>> {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private final Protocol<ByteBuf> protocol;

    @Inject
    public SoeMarshallingConsumer(Protocol<ByteBuf> protocol) {
        this.protocol = protocol;
    }

    @Override
    public void onEvent(SoeOutputEvent<T> event, long sequence, boolean endOfBatch)
            throws Exception {

        T client = event.getClient();

        List<ByteBuf> messageList = event.getMessageList();

        for (int i = 0; i < messageList.size(); ++i) {

            ByteBuf buffer = messageList.get(i);

            short op = ByteBufUtil.swapShort(buffer.getShort(0));

            if (op > 0x2 && op != 0x4) {
                try {
                    buffer = protocol.encode(client.getSessionKey(), buffer, true);

                    protocol.appendCRC(client.getSessionKey(), buffer, 2);

                    messageList.set(i, buffer);

                    if (!protocol.validate(client.getSessionKey(), buffer)) {
                        logger.info("Invalid Packet");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
