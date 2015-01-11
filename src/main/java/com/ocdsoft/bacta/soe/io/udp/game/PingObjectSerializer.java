package com.ocdsoft.bacta.soe.io.udp.game;

import com.esotericsoftware.kryo.Kryo;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.serialization.KryoSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Kyle on 8/14/2014.
 */
@Singleton
public final class PingObjectSerializer extends KryoSerializer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public PingObjectSerializer(Injector injector) {
        super(injector);
    }

    @Override
    public void registerTypes(Kryo kryo, Injector injector) {

    }
}
