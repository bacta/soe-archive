package com.ocdsoft.bacta.soe.chat;

import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by crush on 1/7/2015.
 *
 * ChatServerAgent acts as an agent on the behalf of multiple chat users, relaying chat requests to the chat server.
 * Its usage should allow fewer network connections to be reserved simply for chat. Opposed to having a connection per
 * connected user on the game server, all users (or a portion) can go through a ChatServerAgent to reach the server,
 * and receive from the server vice versa.
 */
public final class ChatServerAgent implements ChatModule, MailModule {
    private static final Logger logger = LoggerFactory.getLogger(ChatServerAgent.class);

    private final SoeUdpConnection connection;

    public ChatServerAgent(final SoeUdpConnection connection) {
        this.connection = connection;

        //this.connection.connect(0xCCCC); //TODO: use session key service to get a generated code.
    }

    @Override
    public void register(ChatAvatarId avatarId) {

    }

    @Override
    public void connect(ChatAvatarId avatarId, String password) {

    }

    @Override
    public void disconnect(ChatAvatarId avatarId) {

    }
}
