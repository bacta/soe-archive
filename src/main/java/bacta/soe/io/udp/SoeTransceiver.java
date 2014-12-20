package bacta.soe.io.udp;

import bacta.network.io.udp.UdpTransceiver;
import bacta.soe.ServerType;
import bacta.soe.client.SoeUdpClient;
import bacta.soe.protocol.SoeProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kburkhardt on 2/15/14.
 */

public abstract class SoeTransceiver<Client extends SoeUdpClient> extends UdpTransceiver<Client> {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private final ServerType serverType;

    private SoeProtocol protocol;

    /**
     * This is a reference to the constructor used to create the clients
     */
    protected Constructor<Client> clientConstructor;

    /**
     * The structure that holds the "connected" udp clients
     */
    protected final Map<Object, Client> clients;

    private final Thread sendThread;
    private final int sendQueueInterval;

    public SoeTransceiver(InetAddress bindAddress,
                          int port,
                          ServerType serverType,
                          Class<Client> clientClass,
                          int sendQueueInterval,
                          SoeProtocol soeProtocol) {

        super(bindAddress, port);

        try {

            this.serverType = serverType;
            this.sendQueueInterval = sendQueueInterval;
            clientConstructor = clientClass.getConstructor();
            this.protocol = soeProtocol;

            ResourceBundle bundle = PropertyResourceBundle.getBundle("messageprocessing");
            protocol.setCompression(bundle.getString("Compression").equalsIgnoreCase("true"));

            clients = new ConcurrentHashMap<>();

            sendThread = new Thread(new SendLoop());
            sendThread.start();

        } catch (NoSuchMethodException | SecurityException e) {

            throw new RuntimeException("Unable to start SOE transceiver", e);
        }
    }

    /**
     * The factory method that creates instances of the {@link bacta.network.client.UdpClient} specified in the {@code Client} parameter
     *
     * @param address {@link java.net.InetSocketAddress of incoming {@code Data} message
     * @return New instance of user specified class {@code Client}
     * @throws Exception
     * @since 1.0
     */
    protected final Client createClient(InetSocketAddress address) throws RuntimeException {
        Client client;
        try {
            client = (Client) clientConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        client.setRemoteAddress(address);
        return client;
    }

    @Override
    public final void receiveMessage(InetSocketAddress sender, ByteBuffer buffer) {

        try {

            Client client = clients.get(sender);

            if (client == null) {
                if (buffer.getShort(0) == 0x1 || buffer.getShort(0) == 0x4) {

                    client = createClient(sender);
                    clients.put(sender, client);

                    logger.debug("{} connection from {} now has {} total connected clients.",
                            client.getClass().getSimpleName(),
                            sender,
                            clients.size());
                } else {
                    return;
                }
            }

            if(buffer.getShort(0) != 1) {
                buffer = protocol.decode(client.getSessionKey(), buffer.order(ByteOrder.LITTLE_ENDIAN));
            }

            receiveMessage(client.getRemoteAddress(), buffer);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendMessage(Client client, ByteBuffer buffer) {

        short op = Short.reverseBytes(buffer.getShort(0));

        if (op > 0x2 && op != 0x4) {
            buffer = protocol.encode(client.getSessionKey(), buffer, true);
            protocol.appendCRC(client.getSessionKey(), buffer, 2);
        }

        handleOutgoing(buffer, client.getRemoteAddress());
    }

    private class SendLoop implements Runnable {

        @Override
        public void run() {

            long nextIteration = 0;

            while (true) {
                try {
                    long currentTime = System.currentTimeMillis();
                    if (nextIteration > currentTime) {

                        try {
                            Thread.sleep(nextIteration - currentTime);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    nextIteration = currentTime + sendQueueInterval;

                    Set<Object> clientList = clients.keySet();
                    List<Object> deadClients = new ArrayList<>();

                    for (Object obj : clientList) {
                        Client client = clients.get(obj);

                        if (client == null || client.isStale()) {
                            deadClients.add(obj);
                            continue;
                        }

                        List<ByteBuffer> messages = client.getPendingMessages();

                        for (ByteBuffer message : messages) {
                            sendMessage(client, message);
                        }
                    }

                    for (Object key : deadClients) {
                        logger.debug("Removing client: " + key);
                        Client client = clients.remove(key);
                        client.close();
                    }

                } catch (Exception e) {
                    logger.error("Unknown", e);
                }
            }
        }
    }
}
