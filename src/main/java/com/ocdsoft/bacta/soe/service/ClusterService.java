package com.ocdsoft.bacta.soe.service;

import com.google.gson.internal.LinkedTreeMap;
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

    private static Logger logger = LoggerFactory.getLogger(ClusterService.class);

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

            Set<LinkedTreeMap<String, Object>> servers = dbConnector.getObject("ClusterSet", Set.class);

            if (servers != null) {
                for (LinkedTreeMap<String, Object> clusterInfoMap : servers) {
                    clusterInfoMap.put("status", ServerStatus.DOWN);
                    T clusterInfo = clusterEntryConstructor.newInstance(clusterInfoMap);
                    clusterEntrySet.add(clusterInfo);
                }
            }
        } catch(NullPointerException e) {
            logger.error("Null Pointer", e);
        }
    }

    public void updateClusterInfo(T incomingClusterEntry) {

        for(T clusterEntry : clusterEntrySet) {
            if(clusterEntry.equals(incomingClusterEntry)) {
                clusterEntrySet.remove(clusterEntry);
                incomingClusterEntry.setId(clusterEntry.getId());
                logger.debug("Updating cluster entry: " + incomingClusterEntry);
                clusterEntrySet.add(incomingClusterEntry);
                update();
                return;
            }
        }

        int incomingClusterId = incomingClusterEntry.getId();
        for (T clusterEntry : clusterEntrySet) {
            if (clusterEntry.getId() == incomingClusterId) {
                logger.error("Server ID already in use: Existing=" + clusterEntry + " Incoming=" + incomingClusterEntry);
                return;
            }
        }


        createNewClusterEntry(incomingClusterEntry);
    }

    private void createNewClusterEntry(T incomingClusterEntry) {
        int clusterId = dbConnector.nextClusterId();
        incomingClusterEntry.setId(clusterId);
        clusterEntrySet.add(incomingClusterEntry);
        logger.debug("Created new cluster entry: " + incomingClusterEntry);
        update();
    }

    public Set<T> getClusterEntries() {
        return clusterEntrySet;
    }

    private void update() {
        dbConnector.updateObject("ClusterSet", clusterEntrySet);
    }

}
