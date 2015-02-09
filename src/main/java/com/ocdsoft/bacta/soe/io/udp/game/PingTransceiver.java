package com.ocdsoft.bacta.soe.io.udp.game;

import com.ocdsoft.bacta.engine.network.io.udp.BasicUdpTransceiver;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class PingTransceiver extends BasicUdpTransceiver {


    public PingTransceiver(InetAddress bindAddress, int port) {
        super(bindAddress, port);

    }

    @Override
    protected void handleIncoming(DatagramPacket msg) {
       receiveMessage(msg.sender(), msg.content().nioBuffer());
    }

    @Override
    public void sendMessage(InetSocketAddress inetSocketAddress, ByteBuffer buffer) {
        handleOutgoing(buffer, inetSocketAddress);
    }

    @Override
    public void receiveMessage(InetSocketAddress inetSocketAddress, ByteBuffer buffer) {


        ByteBuffer pong = ByteBuffer.allocate(4);
        pong.putInt(buffer.getInt());
        pong.rewind();

        sendMessage(inetSocketAddress, pong);

    }

}
