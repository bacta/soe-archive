package com.ocdsoft.bacta.soe.client;

import com.ocdsoft.bacta.engine.network.client.ConnectionState;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.ConnectMessage;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.protocol.SoeProtocol;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Function;

/**
 * Created by kburkhardt on 1/26/15.
 */

public class ClientConnection extends SoeUdpConnection implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnection.class);
    private static final SoeProtocol protocol = new SoeProtocol();

    private final SoeMessageRouter soeMessageRouter;

    private Channel channel;
    private final Thread sendThread;

    @Setter
    private Function<Void, Void> connectCallback;

    private final int udpSize;
    private final int protocolVersion;

    private final SendLoop sendLoop;
    
    public ClientConnection(final SoeMessageRouter soeMessageRouter, final int udpSize, final int protocolVersion) {
        
        this.soeMessageRouter = soeMessageRouter;
        this.udpSize = udpSize;
        this.protocolVersion = protocolVersion;

        sendLoop = new SendLoop();
        sendThread = new Thread(sendLoop);
        sendThread.start();
    }

    @Override
    public void run() {

        Bootstrap b = new Bootstrap();

        try {

            b.group(new NioEventLoopGroup())
                    .channel(NioDatagramChannel.class)
                    .localAddress(0)
                    .handler(new ClientInboundHandler(
                            protocol,
                            soeMessageRouter,
                            this
                            )
                    );


            channel = b.bind().sync().channel();
            channel.closeFuture().await();

        } catch (Exception e) {
            logger.error("Client Stopped", e);
            sendThread.interrupt();
            channel.close();
        }
    }

    @Override
    public synchronized void connect(final int connectionID) {

        if(getState() == ConnectionState.ONLINE) {
            confirm();
            return;
        }

        ConnectMessage connectMessage = new ConnectMessage(protocolVersion, connectionID, udpSize);
        sendMessage(connectMessage);
    }


    @Override
    public synchronized void confirm() {

        setState(ConnectionState.ONLINE);
        connectCallback.apply(null);
    }

    private class SendLoop implements Runnable {

        @Override
        public void run() {

            long nextIteration = 0;

            try {

                while (channel == null) {
                    Thread.sleep(500);
                }

                while (true) {


                    long currentTime = System.currentTimeMillis();

                    if (nextIteration > currentTime) {
                        Thread.sleep(nextIteration - currentTime);
                    }

                    try {

                        nextIteration = currentTime + 100;
                        List<ByteBuffer> messages = getPendingMessages();
                        messages.forEach(this::flushMessage);

                    } catch (Exception e) {
                        logger.error("Unknown", e);
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("Send thread interrupted", e);
            }
        }

        public void flushMessage(ByteBuffer buffer) {
            UdpPacketType packetType = UdpPacketType.values()[buffer.get(1)];

            if (packetType != UdpPacketType.cUdpPacketConnect) {
                buffer = protocol.encode(getSessionKey(), buffer, true);
                protocol.appendCRC(getSessionKey(), buffer, 2);
                buffer.rewind();
            }

            DatagramPacket datagramPacket = new DatagramPacket(Unpooled.wrappedBuffer(buffer), getRemoteAddress());
            channel.writeAndFlush(datagramPacket);
        }
    }
}
