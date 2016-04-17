package com.ocdsoft.bacta.soe.io.udp.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.engine.network.io.udp.BasicUdpTransceiver;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

@Singleton
public final class PingTransceiver extends BasicUdpTransceiver {

    @Inject
    public PingTransceiver(final BactaConfiguration configuration) throws UnknownHostException {
        super(
                InetAddress.getByName(configuration.getString("Bacta/GameServer", "BindIp")),
                configuration.getIntWithDefault("Bacta/GameServer", "Ping", 44462)
        );
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
