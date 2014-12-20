package bacta.soe.router;


import com.ocdsoft.bacta.swg.network.swg.ServerType;

public interface BactaMessageRouterFactory {
	BactaMessageRouter create(ServerType serverEnv);
}
