package com.ocdsoft.bacta.soe.chat.message;

import com.ocdsoft.bacta.soe.chat.ChatAvatarId;

/**
 * Created by crush on 1/12/2015.
 */
public class ChatPersistentMessageToServer {
    private String message; //utf
    private String outOfBand; //utf
    private int sequence;
    private String subject; //utf
    private ChatAvatarId toCharacterName;
}
