package com.ocdsoft.bacta.soe.chat;

/**
 * Created by crush on 1/12/2015.
 */
public interface ChatModule {
    void register(ChatAvatarId avatarId);
    void connect(ChatAvatarId avatarId, String password);
    void disconnect(ChatAvatarId avatarId);




    /*

    void addFriend(IdentityType friendIdentity);
    void removeFriend(IdentityType friendIdentity);

    void addIgnore(IdentityType ignoreIdentity);
    void removeIgnore(IdentityType ignoreIdentity);

    void addModeratorToRoom(IdentityType moderatorIdentity, String roomName);
    void removeModeratorFromRoom(IdentityType moderatorIdentity, String roomName);
    void banModeratorFromRoom(IdentityType moderatorIdentity, String roomName);
    void unbanModeratorFromRoom(IdentityType moderatorIdentity, String roomName);

    void queryRoom(String roomName);
    void requestRoomList();
    void createRoom(String roomName);
    void destroyRoom(String roomName);
    void enterRoomById(String roomId);
    void sendToRoom(String roomId, String message);
    void inviteToRoom(IdentityType avatarIdentity, String roomName);
    void inviteGroupToRoom(String groupIdentity, String roomName);
    void uninviteFromRoom(IdentityType avatarIdentity, String roomName);
    void kickFromRoom(IdentityType avatarIdentity, String roomName);
    void removeAvatarFromRoom(IdentityType avatarIdentity, String roomName);
    void unbanAvatarFromRoom(IdentityType avatarIdentity, String roomName);

    void sendSystemMessage();

    void sendPersistentMessage();
    void requestPersistentMessage();
    void deletePersistentMessage();
    void deleteAllPersistentMessages();

    void sendInstantMessage(IdentityType identity, String message);*/
}
