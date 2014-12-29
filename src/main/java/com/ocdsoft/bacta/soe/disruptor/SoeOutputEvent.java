package com.ocdsoft.bacta.soe.disruptor;

import com.ocdsoft.bacta.swg.network.soe.client.SoeUdpClient;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SoeOutputEvent<T extends SoeUdpClient> {

    @Getter
    @Setter
    private T client;

    @Getter
    @Setter
    List<ByteBuf> messageList;

}
