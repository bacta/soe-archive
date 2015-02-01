package com.ocdsoft.bacta.soe.chat;

import java.lang.String;import java.lang.StringBuilder; /**
 * Created by crush on 12/30/2014.
 *
 * ChatAvatarId is the object used by SOE to represent a chat participant's identity on their servers. These identities
 * can be specified locally without required prefixes, but after passing through the server, these prefixes will be
 * assumed by whatever the game server has specified. For example, if a local ChatAvatarId is specified only as "crush",
 * and it resides on the "bacta" galaxy of the game "swg", then it should be assumed that the prefixes
 * "swg.bacta.crush" shall be appended before it is passed along to the associated chat server.
 */
public final class ChatAvatarId {
    private final String gameCode;
    private final String cluster;
    private final String name;

    public ChatAvatarId(final String gameCode, final String cluster, final String name) {
        this.gameCode = gameCode;
        this.cluster = cluster;
        this.name = name;
    }

    public ChatAvatarId(final String cluster, final String name) {
        this.gameCode = "";
        this.cluster = cluster;
        this.name = name;
    }

    /**
     * Turns a string into a ChatAvatarId object. Attempts to parse blocks separated by a '.' as the three components
     * of the ChatAvatarId: gameCode.cluster.name. Works in reverse order. The following should be expected:
     * <code>
     *     "com.swg.bacta.crush" => { gameCode: "com.swg", cluster: "bacta", name: "crush" }
     *     "swg.bacta.crush" => { gameCode: "swg", cluster: "bacta", name: "crush" }
     *     "bacta.crush" => { gameCode: "", cluster: "bacta", name: "crush" }
     *     "crush" => { gameCode: "", cluster: "", name: "crush" }
     * </code>
     * @param name The name used to create this ChatAvatarId.
     */
    public ChatAvatarId(final String name) {
        int index = name.lastIndexOf('.');

        if (index != -1) {
            this.name = name.substring(index + 1);

            index = name.lastIndexOf('.', index - 1);

            if (index != -1) {
                this.cluster = name.substring(index + 1, name.length() - this.name.length() - 1);
                this.gameCode = name.substring(0, index);
            } else {
                this.cluster = name.substring(0, name.length() - this.name.length() - 1);
                this.gameCode = "";
            }
        } else {
            this.name = name;
            this.cluster = "";
            this.gameCode = "";
        }
    }

    public final String getGameCode() { return gameCode; }
    public final String getCluster() { return cluster; }
    public final String getName() { return name; }

    /**
     * Gets the name exactly as it was specified. If prefixes are missing, then they will be omitted from the returned
     * string.
     * @return String representation fo the ChatAvatarId with any existing prefixes attached.
     * @see #getNameWithNecessaryPrefix(String, String)
     */
    public final String getFullName() {
        final StringBuilder stringBuilder = new StringBuilder(gameCode.length() + cluster.length() + name.length());

        if (gameCode.length() > 0)
            stringBuilder.append(gameCode).append('.');

        if (gameCode.length() > 0)
            stringBuilder.append(cluster).append('.');

        stringBuilder.append(name);

        return stringBuilder.toString();
    }

    /**
     * Gets the name with any missing prefixes appended based on the provided parts. For example, if the existing
     * ChatAvatarId only consisted of the name part "crush", then one might call this method as follows:
     * <code>
     *     ChatAvatarId chatAvatarId = new ChatAvatarId("crush"); //Only the name part has a value
     *     chatAvatarId.getNameWithNecessaryPrefix("swg", "bacta");
     * </code>
     * This would produce the string "swg.bacta.crush" as opposed to simply "crush" which would be returned from #getFullName().
     * @param localGameCode The game code to use if this ChatAvatarId does not have one specified.
     * @param localCluster The cluster to use if this ChatAvatarId does not have one specified.
     * @return A fully qualified ChatAvatarId name with all missing prefixes supplied as specified by the values passed.
     */
    public final String getNameWithNecessaryPrefix(final String localGameCode, final String localCluster) {
        int gameCodeLength = gameCode.length() > 0 ? gameCode.length() : localGameCode.length();
        int clusterLength = cluster.length() > 0 ? cluster.length() : localCluster.length();

        final StringBuilder stringBuilder = new StringBuilder(gameCodeLength + clusterLength + name.length());

        stringBuilder.append(gameCode.length() > 0 ? gameCode : localGameCode).append('.');
        stringBuilder.append(gameCode.length() > 0 ? gameCode : localCluster).append('.');
        stringBuilder.append(name);

        return stringBuilder.toString();
    }
}
