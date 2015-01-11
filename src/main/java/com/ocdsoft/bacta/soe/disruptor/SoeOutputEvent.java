package com.ocdsoft.bacta.soe.disruptor;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.List;

public class SoeOutputEvent<T extends SoeUdpConnection> {

    @Getter
    @Setter
    private T client;

    @Getter
    @Setter
    List<ByteBuffer> messageList;

}
