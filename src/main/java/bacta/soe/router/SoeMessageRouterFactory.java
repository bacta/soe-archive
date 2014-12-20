package bacta.soe.router;


import bacta.soe.ServerType;

public interface SoeMessageRouterFactory {
	SoeMessageRouter create(ServerType serverEnv);
}
