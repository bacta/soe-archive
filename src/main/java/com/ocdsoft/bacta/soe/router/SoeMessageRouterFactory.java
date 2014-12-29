package com.ocdsoft.bacta.soe.router;


import com.ocdsoft.bacta.soe.ServerType;

public interface SoeMessageRouterFactory {
	SoeMessageRouter create(ServerType serverEnv);
}
