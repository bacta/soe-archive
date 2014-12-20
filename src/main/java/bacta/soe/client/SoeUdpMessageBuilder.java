package bacta.soe.client;

import bacta.network.client.UdpMessageBuilder;
import bacta.soe.message.MultiMessage;
import bacta.soe.utils.SoeMessageUtil;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Kyle on 3/28/14.
 */
public class SoeUdpMessageBuilder implements UdpMessageBuilder<ByteBuf> {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private final Queue<ByteBuf> bufferList;
    private final int udpMaxMultiPayload;
    private MultiMessage pendingMulti;
    private ByteBuf pendingBuffer;
    private boolean multiMessages;

    public SoeUdpMessageBuilder(int udpMaxMultiPayload, final ResourceBundle messageProperties) {
        bufferList = new ArrayBlockingQueue<>(500);
        this.udpMaxMultiPayload = udpMaxMultiPayload;
        pendingMulti = null;
        pendingBuffer = null;
        multiMessages = Boolean.parseBoolean(messageProperties.getString("MultiSoeMessages"));
    }

    @Override
    public synchronized boolean add(ByteBuf buffer) {

        if(!multiMessages) {
            return bufferList.add(buffer);
        }

        logger.trace("Adding: " + SoeMessageUtil.bytesToHex(buffer));
        logger.trace("  Queue Size: " + bufferList.size());
        logger.trace("  Pending Multi: " + (pendingMulti != null));
        logger.trace("  Pending Buffer: " + (pendingBuffer != null));

        if(pendingMulti != null) {
            if(pendingMulti.readableBytes() + buffer.readableBytes() <= udpMaxMultiPayload) {
                pendingMulti.add(buffer);
                logger.trace("Appending: " + SoeMessageUtil.bytesToHex(buffer));
            } else {
                bufferList.add(pendingMulti);
                logger.trace("Ready to send: " + SoeMessageUtil.bytesToHex(pendingMulti));
                pendingMulti = null;
                pendingBuffer = buffer;
            }
        } else {
            if(pendingBuffer != null) {
                if(pendingBuffer.readableBytes() + buffer.readableBytes() <= udpMaxMultiPayload) {
                    pendingMulti = new MultiMessage(pendingBuffer, buffer);
                    logger.trace("Combining: " + SoeMessageUtil.bytesToHex(pendingBuffer));
                    logger.trace("Combining: " + SoeMessageUtil.bytesToHex(buffer));
                    pendingBuffer = null;
                } else {
                    bufferList.add(pendingBuffer);
                    pendingBuffer = buffer;
                }
            }  else {
                pendingBuffer = buffer;
            }
        }

        logger.trace("  Queue Size: " + bufferList.size());
        logger.trace("  Pending Multi: " + (pendingMulti != null));
        logger.trace("  Pending Buffer: " + (pendingBuffer != null));
        return true;
    }

    @Override
    public synchronized ByteBuf buildNext() {

        ByteBuf buffer = bufferList.poll();

        if(buffer == null && pendingMulti != null) {
            buffer = pendingMulti;
            pendingMulti = null;
        }
        if(buffer == null) {
            buffer = pendingBuffer;
            pendingBuffer = null;
        }

        return buffer;
    }

    @Override
    public void acknowledge(short sequenceNumber) {

    }
}
