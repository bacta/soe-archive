package com.ocdsoft.bacta.soe.service;

import com.google.gson.internal.LinkedTreeMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.engine.data.DatabaseConnector;
import com.ocdsoft.bacta.engine.network.client.ServerStatus;
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
public class ClusterService<T> {

    private static Logger logger = LoggerFactory.getLogger(ClusterService.class);

    private transient final DatabaseConnector dbConnector;
    private transient final Set<T> clusterEntrySet;
    private transient final Constructor<T> clusterEntryConstructor;

    @Inject
    public ClusterService(DatabaseConnector dbConnector, T clusterEntry) throws Exception {

        this.clusterEntrySet = Collections.synchronizedSortedSet(new TreeSet<T>());
        this.dbConnector = dbConnector;
        this.clusterEntryConstructor = (Constructor<T>) clusterEntry.getClass().getConstructor(Map.class);

        loadData();
    }

    private void loadData() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        try {

            LinkedTreeMap<String, LinkedTreeMap<String, Object>> servers = dbConnector.getAdminObject("ClusterList", LinkedTreeMap.class);

            if (servers != null) {
                for (String key : servers.keySet()) {
                    Map<String, Object> serverInfo = servers.get(key);
                    serverInfo.put("status", ServerStatus.DOWN);
                    T clusterInfo = clusterEntryConstructor.newInstance(serverInfo);
                    clusterEntrySet.add(clusterInfo);
                }
            }
        } catch(NullPointerException e) {
            logger.error("Null Pointer", e);
        }
    }

    public void updateClusterInfo(T clusterInfo) {
        clusterEntrySet.add(clusterInfo);
        dbConnector.updateAdminObject("ClusterList", this);
    }

    public Set<T> getClusterEntries() {
        return clusterEntrySet;
    }

}
