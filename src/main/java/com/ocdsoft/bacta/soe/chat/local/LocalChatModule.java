package com.ocdsoft.bacta.soe.chat.local;

import com.ocdsoft.bacta.soe.chat.ChatAvatarId;
import com.ocdsoft.bacta.soe.chat.ChatModule;
import com.ocdsoft.bacta.soe.object.chat.ChatRoomData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by crush on 1/12/2015.
 *
 * LocalChatModule acts as a local chat server instance instead of delegating to an external server. For this reason,
 * it only understands the SOE chat protocol, but requires no third-party server installation.
 */
public class LocalChatModule implements ChatModule {

    private final Map<String, ChatRoomData> chatRooms;
    private final Set<ChatAvatarId> registeredAvatars;

    public LocalChatModule() {
        chatRooms = new HashMap<>();
        registeredAvatars = new TreeSet<>();
    }

    @Override
    public void register(ChatAvatarId avatarId) {
        if (registeredAvatars.contains(avatarId))
            return; //return something. findout how statuses should work.

        registeredAvatars.add(avatarId);
    }

    @Override
    public void connect(ChatAvatarId avatarId, String password) {

    }

    @Override
    public void disconnect(ChatAvatarId avatarId) {

    }
}
