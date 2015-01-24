package com.ocdsoft.bacta.soe.object.chat;

import com.ocdsoft.bacta.engine.buffer.ByteBufferSerializable;
import com.ocdsoft.bacta.engine.utils.BufferUtil;
import lombok.Getter;

import java.nio.ByteBuffer;

/**
 * The ChatAvatarId represents a player on a chat server. It is composed of three parts:
 * <ul>
 * <li><i>Game</i> - The game that the player's avatar was created on.</li>
 * <li><i>Server</i> - The game server that the player's avatar was created on.</li>
 * <li><i>Username</i> - The username of the player's avatar.</li>
 * </ul>
 * This helps to distinguish the chat user from other users who may be connected to the same chat server, yet using
 * different games or the same game on a different game server.
 */
public final class ChatAvatarId implements ByteBufferSerializable {
    @Getter
    private String gameCode;
    @Getter
    private String cluster;
    @Getter
    private String name;

    /**
     * Creates a new ChatAvatarId based on the three components game, server, and username.
     *
     * @param gameCode The game the user is playing.
     * @param cluster  The game server the user is playing on.
     * @param name     The username of the user.
     */
    public ChatAvatarId(final String gameCode, final String cluster, final String name) {
        this.gameCode = gameCode;
        this.cluster = cluster;
        this.name = name;
    }

    /**
     * Creates a new ChatAvatarId based on the buffer of an incoming {@link ByteBuffer}. Reads each component as
     * an individual ASCII 8-bit sequence of characters. The ASCII string is encoded with its length as a short
     * before the bytes of the string, and is not null terminated. This constructor will advance the position of
     * BactaBuffer's underlying buffer.
     *
     * @param buffer The {@link ByteBuffer} containing the parts of the ChatAvatarId.
     */
    public ChatAvatarId(ByteBuffer buffer) {
        this.gameCode = BufferUtil.getAscii(buffer);
        this.cluster = BufferUtil.getAscii(buffer);
        this.name = BufferUtil.getAscii(buffer);
    }

    /**
     * Attempts to create a new ChatAvatarId based upon a <code>fullpath</code> string. This string can be in a
     * number of formats. The following examples demonstrate the acceptable formats:
     * <ol>
     * <li><em>Game.Server.Username</em> - <code>SWG.Chilastra.cRush</code></li>
     * <li><em>ChatAvatarId@host</em> - <code>SWG.Eclipse.kyle@localhost</code></li>
     * </ol>
     * In case 1, the <code>Username</code> may not contain spaces, or the additional tokens will be ignored after
     * the first space is encountered. This is helpful for parsing a character's full name into a ChatAvatarId. For
     * example, <code>SWG.Ahazi.Playdoh Playa</code> would produce a ChatAvatarId with components:
     * <ul>
     * <li><em>Game:</em> SWG</li>
     * <li><em>Server:</em> Ahazi</li>
     * <li><em>Username:</em> Playdoh</li>
     * </ul>
     * If the <code>fullpath</code> does not contain enough tokens to create a full ChatAvatarId (3 tokens), then
     * an {@link InvalidChatAvatarIdException} will be thrown.
     *
     * @param fullpath The fullpath ChatAvatarId representation that will attempt to parse into this object.
     * @throws InvalidChatAvatarIdException if there are not enough tokens to parse.
     */
    public ChatAvatarId(final String fullpath) throws InvalidChatAvatarIdException {
        String[] parts = fullpath.split("@")[0].split("\\.");

        if (parts.length < 3)
            throw new InvalidChatAvatarIdException("The ChatAvatarId '" + fullpath + "' did not have enough tokens.");

        this.gameCode = parts[0];
        this.cluster = parts[1];
        this.name = parts[2].split(" ")[0]; //We only want the first token.
    }

    /**
     * Returns a textual representation of the ChatServerId. The format is period-delimited.
     */
    public String toString() {
        return gameCode + "." + cluster + "." + name;
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        BufferUtil.putAscii(buffer, gameCode);
        BufferUtil.putAscii(buffer, cluster);
        BufferUtil.putAscii(buffer, name);
    }
}
