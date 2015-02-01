package com.ocdsoft.bacta.soe.chat.message;

import com.ocdsoft.bacta.soe.chat.ChatAvatarId;
import com.ocdsoft.bacta.soe.object.chat.ChatRoomData;

import java.util.List;

/**
 * Created by crush on 1/12/2015.
 */
public class ChatQueryRoomResults {
    private List<ChatAvatarId> avatars;
    private List<ChatAvatarId> invitees;
    private List<ChatAvatarId> moderators;
    private List<ChatAvatarId> banned;
    private ChatRoomData roomData;
    private int sequence;
}
