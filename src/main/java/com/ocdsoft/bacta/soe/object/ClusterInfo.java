package com.ocdsoft.bacta.soe.object;

import com.ocdsoft.bacta.engine.buffer.BactaBufferSerializable;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.Map;

public class ClusterInfo implements BactaBufferSerializable, Comparable<ClusterInfo> {

    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private String secret;
    @Getter
    @Setter
    private String name;
	@Getter
    @Setter
    private String address;
	@Getter
    @Setter
    private int port;
	@Getter
    @Setter
    private int pingPort;
	@Getter
    @Setter
    private int population;
	@Getter
    @Setter
    private int maximumPopulation;
	@Getter
    @Setter
    private int maximumCharacters;
	@Getter
    @Setter
    private int timezone;
	@Getter
    @Setter
    private ServerStatus status;
	@Getter
    @Setter
    private boolean recommended;

    public ClusterInfo() {}

    public ClusterInfo(Map<String, Object> clusterInfo) {
        id = ((Double)clusterInfo.get("id")).intValue();
        secret = (String) clusterInfo.get("secret");
        name = (String) clusterInfo.get("name");
        address = (String) clusterInfo.get("address");
        port = ((Double)clusterInfo.get("port")).intValue();
        pingPort = ((Double)clusterInfo.get("pingPort")).intValue();
        population = ((Double)clusterInfo.get("population")).intValue();
        maximumPopulation = ((Double)clusterInfo.get("maximumPopulation")).intValue();
        maximumCharacters = ((Double)clusterInfo.get("maximumCharacters")).intValue();
        timezone = ((Double)clusterInfo.get("timezone")).intValue();
        status = ServerStatus.valueOf((String) clusterInfo.get("status"));
        recommended = (boolean) clusterInfo.get("recommended");

    }

    public boolean isOffline() { return status == ServerStatus.OFFLINE; }
	public boolean isLoading() { return status == ServerStatus.LOADING; }
	public boolean isOnline()  { return status == ServerStatus.ONLINE;  }
	public boolean isLocked()  { return status == ServerStatus.LOCKED;  }
    public boolean isRestricted()  { return status == ServerStatus.RESTRICTED;  }
    public boolean isFull()  { return status == ServerStatus.FULL;  }

	@Override
	public void writeToBuffer(ByteBuffer message) {
		message.writeInt(getId());
        message.writeAscii(getName());
        message.writeAscii(getAddress());
		message.writeShort((short)getPort());
		message.writeShort((short)getPingPort());
		message.writeInt(getPopulation());
		message.writeInt(getMaximumPopulation());
		message.writeInt(getMaximumCharacters());
		message.writeInt(getTimezone());
		message.writeInt(getStatus().getValue());
		message.writeBoolean(isRecommended());
	}

    @Override
    public int compareTo(ClusterInfo o) {
        return o.getName().compareTo(getName());
    }

}
