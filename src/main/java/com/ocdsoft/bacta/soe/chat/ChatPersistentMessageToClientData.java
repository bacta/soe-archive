package com.ocdsoft.bacta.soe.chat;

/**
 * Created by crush on 1/12/2015.
 */
public class ChatPersistentMessageToClientData {
    private ChatAvatarId from;
    private int id;
    private boolean isHeader;
    private String message; //utf
    private String subject; //utf
    private String outOfBand; //utf
    private char status;
    private int timeStamp;
}
