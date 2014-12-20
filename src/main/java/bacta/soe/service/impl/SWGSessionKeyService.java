package bacta.soe.service.impl;

import com.google.inject.Singleton;
import com.ocdsoft.bacta.swg.network.soe.service.SessionKeyService;

import java.util.Random;

@Singleton
public class SWGSessionKeyService implements SessionKeyService {

	private Random random = new Random();
	
	@Override
	public int getNextKey() {
		return random.nextInt();
	}

}
