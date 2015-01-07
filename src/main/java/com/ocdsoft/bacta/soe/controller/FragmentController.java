package com.ocdsoft.bacta.soe.controller;

import com.ocdsoft.bacta.engine.buffer.BactaBuffer;
import com.ocdsoft.bacta.soe.SoeController;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.ReliableNetworkMessage;
import com.ocdsoft.bacta.soe.message.SoeMessage;
import com.ocdsoft.bacta.soe.router.SoeMessageRouter;
import io.netty.buffer.ByteBuf;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

@SoeController(opcode = 0x0D, handles = ReliableNetworkMessage.class)
public class FragmentController extends SoeMessageController {

    private SoeMessageRouter soeRouter;

    private final Map<SoeUdpConnection, FragmentContainer> pendingFragments = new ConcurrentHashMap<>();

    @Override
    public void setRouter(SoeMessageRouter soeRouter) {
        this.soeRouter = soeRouter;
    }

    @Override
    public void handleIncoming(SoeUdpConnection client, BactaBuffer buffer) {

        FragmentContainer fragmentContainer = pendingFragments.get(client);

        if (fragmentContainer == null) {
            fragmentContainer = new FragmentContainer();
        }

        fragmentContainer.add(buffer);

        if (fragmentContainer.isComplete()) {
            pendingFragments.remove(client);
            soeRouter.routeMessage((short) 0x9, client, fragmentContainer.getMessage());
        }

    }

    @SuppressWarnings("serial")
    private class FragmentContainer extends PriorityQueue<ByteBuf> {

        private int firstFragment = 0;
        private int lastFragment = 0;
        private int fragmentCount = 0;

        @Override
        public boolean add(ByteBuf buffer) {
            return false;
        }

        public boolean isComplete() {
            // TODO Auto-generated method stub
            return false;
        }

        public SoeMessage getMessage() {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
