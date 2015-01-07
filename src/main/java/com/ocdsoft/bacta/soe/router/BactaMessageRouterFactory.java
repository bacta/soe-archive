package com.ocdsoft.bacta.soe.router;

import com.ocdsoft.bacta.soe.ServerType;

public interface BactaMessageRouterFactory {
	BactaMessageRouter create(ServerType serverEnv);
}
