package com.ocdsoft.bacta.soe.io.udp.game;

import com.ocdsoft.bacta.engine.network.client.UdpConnection;
import com.ocdsoft.bacta.engine.network.io.udp.BasicUdpTransceiver;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

public final class PingTransceiver extends BasicUdpTransceiver {

    private final Map<Object, SoeUdpConnection> connectionMap;

    public PingTransceiver(InetAddress bindAddress, int port, Map<Object, SoeUdpConnection> connectionMap) {
        super(bindAddress, port);

        this.connectionMap = connectionMap;
    }

    @Override
    protected void handleIncoming(DatagramPacket msg) {
       receiveMessage(msg.sender(), msg.content().nioBuffer());
    }

    @Override
    public void sendMessage(UdpConnection udpConnection, ByteBuffer buffer) {
        handleOutgoing(buffer, udpConnection.getRemoteAddress());
    }

    @Override
    public void receiveMessage(InetSocketAddress inetSocketAddress, ByteBuffer buffer) {

        SoeUdpConnection connection = connectionMap.get(inetSocketAddress);
        if(connection != null) {

            ByteBuffer pong = ByteBuffer.allocate(4);
            pong.putInt(buffer.getInt());

            sendMessage(connection, pong);
        }
    }
}
