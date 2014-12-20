package bacta.soe.disruptor;

import com.ocdsoft.bacta.swg.network.soe.client.SoeUdpClient;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

public final class SoeInputEvent<Client extends SoeUdpClient> {

    @Getter
    @Setter
    private Client client;
    @Getter
    @Setter
    private ByteBuf buffer;
    @Getter
    @Setter
    private boolean SwgMessage = false;

}
