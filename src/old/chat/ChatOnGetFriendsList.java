package com.ocdsoft.bacta.swg.precu.message.chat;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.swg.precu.object.tangible.creature.CreatureObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatOnGetFriendsList extends GameNetworkMessage {
    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    public ChatOnGetFriendsList(CreatureObject creo) {
        super(0x03, 0xE97AB594);

        logger.info("Getting friends list");

        writeLong(creo.getNetworkId());

        writeInt(0);

        /*
        PlayerObject ghost = creo.<PlayerObject>getSlottedObject("ghost");

        if (ghost != null) {
            AutoDeltaVector<String> friendsList = ghost.getFriendList();

            writeInt(friendsList.size());

            for (String name : friendsList) {
                putAscii("SWG"); //TODO: retrieve these from galaxy information?
                putAscii("Bacta");
                putAscii(name);
            }
        } else {
            writeInt(0);
        }*/
    }
}
