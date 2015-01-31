package com.ocdsoft.bacta.soe.client;

/**
 * Created by kburkhardt on 1/26/15.
 */

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.UdpPacketType;
import com.ocdsoft.bacta.soe.protocol.SoeProtocol;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.nio.ByteBuffer;

public class ClientInboundHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final SoeMessageRouter router;
    private final SoeUdpConnection connection;
    private final SoeProtocol protocol;

    public ClientInboundHandler(SoeProtocol protocol, SoeMessageRouter router, SoeUdpConnection connection) {
        this.router = router;
        this.connection = connection;
        this.protocol = protocol;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {

        ByteBuffer heapBuffer = ByteBuffer.allocate(msg.content().readableBytes());
        msg.content().getBytes(0, heapBuffer);

        if(heapBuffer.get(1) != UdpPacketType.cUdpPacketConfirm.getValue()) {
            heapBuffer = protocol.decode(connection.getSessionKey(), heapBuffer);
        }

        if (heapBuffer != null) {
            heapBuffer.rewind();
            router.routeMessage(connection, heapBuffer);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}