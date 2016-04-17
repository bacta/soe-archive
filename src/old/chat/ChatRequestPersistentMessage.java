package com.ocdsoft.bacta.swg.precu.message.chat;

import com.ocdsoft.bacta.soe.message.GameNetworkMessage;

/**
 * Sent when a persistent message is requested from the mail window.
 *
 * @direction c->s
 */
public class ChatRequestPersistentMessage extends GameNetworkMessage {

    //int messageId
    //int sequence

    public ChatRequestPersistentMessage() {
        super(0x05, 0x07E3559F);
    }
}
