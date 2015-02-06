package com.ocdsoft.bacta.soe.service;

import com.google.inject.Singleton;

import java.util.Random;

@Singleton
public class SWGSessionKeyService implements SessionKeyService {

	private final Random random = new Random();
	
	@Override
	public int getNextKey() {
		return random.nextInt();
	}

}
