package com.ocdsoft.bacta.soe.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.data.ConnectionDatabaseConnector;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
import com.ocdsoft.bacta.soe.object.ClusterEntryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by kburkhardt on 1/18/15.
 */
@Singleton
public class ClusterService<T extends ClusterEntryItem> {

    private static final Logger logger = LoggerFactory.getLogger(ClusterService.class);

    private transient final ConnectionDatabaseConnector dbConnector;
    private transient final Set<T> clusterEntrySet;
    private transient final Constructor<T> clusterEntryConstructor;

    @Inject
    public ClusterService(ConnectionDatabaseConnector dbConnector, T clusterEntry) throws Exception {

        this.clusterEntrySet = Collections.synchronizedSortedSet(new TreeSet<T>());
        this.dbConnector = dbConnector;
        this.clusterEntryConstructor = (Constructor<T>) clusterEntry.getClass().getConstructor(Map.class);

        loadData();
    }

    private void loadData() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        try {

            Set<Map<String, Object>> servers = dbConnector.getObject("ClusterSet", Set.class);

            if (servers != null) {
                for (Map<String, Object> clusterInfoMap : servers) {
                    clusterInfoMap.put("status", ServerStatus.DOWN);
                    T clusterInfo = clusterEntryConstructor.newInstance(clusterInfoMap);
                    clusterEntrySet.add(clusterInfo);
                }
            }
        } catch(NullPointerException e) {
            logger.error("Null Pointer", e);
        }
    }

    public T updateClusterInfo(T incomingClusterEntry) {

        for(T clusterEntry : clusterEntrySet) {
            if(clusterEntry.getId() == incomingClusterEntry.getId()) {
                if(!clusterEntry.equals(incomingClusterEntry)) {
                    logger.debug("Server Attempting to use an existing ID with the wrong secret please change GameServers ID in configuration: {}", clusterEntry.getId());
                    return null;
                }

                clusterEntrySet.remove(clusterEntry);
            }
        }


        clusterEntrySet.add(incomingClusterEntry);
        update();

        return incomingClusterEntry;
    }

    public Set<T> getClusterEntries() {
        return clusterEntrySet;
    }

    private void update() {
        dbConnector.updateObject("ClusterSet", clusterEntrySet);
    }

}
