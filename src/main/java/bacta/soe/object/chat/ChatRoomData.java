package bacta.soe.object.chat;

import com.ocdsoft.bacta.swg.network.soe.buffer.SoeByteBuf;
import com.ocdsoft.bacta.swg.network.soe.buffer.SoeByteBufSerializable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ChatRoomData implements SoeByteBufSerializable {
    private String serverName;
    private String serviceName;

    @Getter
    @Setter
    private int roomId;
    @Getter
    @Setter
    private boolean moderated;
    @Getter
    @Setter
    private boolean membersOnly;
    @Getter
    @Setter
    private boolean nonanonymous;
    @Getter
    @Setter
    private boolean passwordProtected;
    @Getter
    @Setter
    private boolean persistent;
    @Getter
    @Setter
    private String address;
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private ChatAvatarId owner;
    @Getter
    @Setter
    private ChatAvatarId creator;
    @Getter
    @Setter
    private List<ChatAvatarId> moderators;
    @Getter
    @Setter
    private List<ChatAvatarId> users;

    public ChatRoomData(String address, ChatAvatarId owner) {
        this.address = address;
        this.owner = owner;
        this.creator = owner;
        this.serverName = owner.getGameCode();
        this.serviceName = owner.getCluster();
    }

    public String getFullAddress() {
        return address + "@" + serviceName + "." + serverName;
    }

    @Override
    public void writeToBuffer(SoeByteBuf message) {
        message.writeInt(roomId);
        message.writeBoolean(passwordProtected);
        message.writeBoolean(nonanonymous);
        message.writeBoolean(persistent);
        message.writeBoolean(membersOnly);
        message.writeBoolean(moderated);
        message.writeAscii(address);

        owner.writeToBuffer(message);
        creator.writeToBuffer(message);

        message.writeUnicode(title);

        message.writeInt(0); //Moderator list
        message.writeInt(0); //User list
    }
}
