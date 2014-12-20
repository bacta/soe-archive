package bacta.soe.client;

import bacta.network.client.ClientState;
import bacta.network.client.UdpMessageBuilder;
import bacta.network.client.UdpMessageProcessor;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

/**
 * @author kyle
 */
public final class SoeUdpMessageProcessor implements UdpMessageProcessor<ByteBuf> {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private final UdpMessageBuilder<ByteBuf> udpMessageBuilder;
    private final UdpMessageBuilder<ByteBuf> reliableUdpMessageBuilder;

    private final int udpMaxSize;
    private final SoeUdpClient client;

    public SoeUdpMessageProcessor(SoeUdpClient client, final ResourceBundle messageProperties) {

        this.client = client;

        this.udpMaxSize = Integer.parseInt(messageProperties.getString("UdpMaxSize"));
        int footerLength = Integer.parseInt(messageProperties.getString("FooterLength"));

        int udpMaxMultiPayload = udpMaxSize - footerLength - 3;

        reliableUdpMessageBuilder = new ReliableUdpMessageBuilder(client, messageProperties);
        udpMessageBuilder = new SoeUdpMessageBuilder(udpMaxMultiPayload, messageProperties);
    }

    @Override
    public boolean addReliable(ByteBuf buffer) {
        if (buffer == null) throw new NullPointerException();

        return reliableUdpMessageBuilder.add(buffer);
    }

    @Override
    public boolean addUnreliable(ByteBuf buffer) {
        if (buffer == null) throw new NullPointerException();

        flushReliable();
        return udpMessageBuilder.add(buffer);
    }

    @Override
    public ByteBuf processNext() {

        flushReliable();
        ByteBuf message = udpMessageBuilder.buildNext();
        if (message != null && message.readableBytes() > udpMaxSize) {
            throw new RuntimeException("Sending packet that exceeds " + udpMaxSize + " bytes");
        }
        return message;

    }

    @Override
    public void acknowledge(short reliableSequence) {
        if(client.getClientState() != ClientState.ONLINE) {
            client.setClientState(ClientState.ONLINE);
        }
        reliableUdpMessageBuilder.acknowledge(reliableSequence);
    }

    private void flushReliable() {
        ByteBuf message;
        while ((message = reliableUdpMessageBuilder.buildNext()) != null) {
            udpMessageBuilder.add(message);
        }
    }
}
