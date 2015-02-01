package com.ocdsoft.bacta.soe.chat;

import java.util.List;

/**
 * Created by crush on 1/12/2015.
 */
public class ChatRoomData {
    private int id;
    private int roomType;
    private String path;
    private ChatAvatarId owner;
    private ChatAvatarId creator;
    private String title; //utf
    private List<ChatAvatarId> moderators;
    private List<ChatAvatarId> invitees;
    private char moderated;
}
