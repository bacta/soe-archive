package com.ocdsoft.bacta.soe.disruptor;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;

public final class SoeInputEvent<Client extends SoeUdpConnection> {

    @Getter
    @Setter
    private Client client;
    @Getter
    @Setter
    private ByteBuffer buffer;
    @Getter
    @Setter
    private boolean SwgMessage = false;

}
