package com.ocdsoft.bacta.soe.io.udp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.TerminateReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kyle on 5/28/2016.
 * This class houses and manages all the connection objects
 */
@Singleton
public final class AccountCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountCache.class);

    private final NetworkConfiguration networkConfiguration;
    private final Map<InetSocketAddress, SoeUdpConnection> connectionMap;
    private final Map<Integer, Queue<SoeUdpConnection>> connectedAccountCache;

    @Inject
    public AccountCache(final NetworkConfiguration networkConfiguration) {
        this.connectionMap = new ConcurrentHashMap<>();
        this.connectedAccountCache = new ConcurrentHashMap<>();
        this.networkConfiguration = networkConfiguration;
    }

    public int getConnectionCount() {
        return connectionMap.size();
    }

    public void put(final InetSocketAddress remoteAddress, final SoeUdpConnection connection) {
        connectionMap.put(remoteAddress, connection);
        LOGGER.trace("Adding connection from {}", remoteAddress);
    }

    public SoeUdpConnection get(final InetSocketAddress sender) {
        return connectionMap.get(sender);
    }

    public Set<InetSocketAddress> keySet() {
        return connectionMap.keySet();
    }

    public SoeUdpConnection remove(final InetSocketAddress inetSocketAddress) {
        final SoeUdpConnection connection = connectionMap.remove(inetSocketAddress);

        if(connection != null) {
            Queue<SoeUdpConnection> connectionQueue = connectedAccountCache.get(connection.getAccountId());
            connectionQueue.remove(connection);
            if(connectionQueue.isEmpty()) {
                connectedAccountCache.remove(connection.getAccountId());
                LOGGER.trace("No more connections for account {}", connection.getAccountUsername());
            }
        }

        return connection;
    }

    public void addAccountConnection(final SoeUdpConnection connection) {

        Queue<SoeUdpConnection> connectionQueue = connectedAccountCache.get(connection.getAccountId());
        if (connectionQueue != null) {
            while (connectionQueue.size() >= networkConfiguration.getConnectionsPerAccount()) {
                SoeUdpConnection connectionToDisconnect = connectionQueue.poll();
                connectionToDisconnect.terminate(TerminateReason.NEWATTEMPT);
                LOGGER.trace("Account {} exceed the number of allowed connections ({}), terminating oldest connection {}",
                        connection.getAccountUsername(),
                        networkConfiguration.getConnectionsPerAccount(),
                        connection.getId());
            }
        } else {
            connectionQueue = new ArrayBlockingQueue<>(networkConfiguration.getConnectionsPerAccount());
            connectedAccountCache.put(connection.getAccountId(), connectionQueue);
            LOGGER.trace("Account {} is making first connection {}", connection.getAccountUsername(), connection.getId());
        }

        connectionQueue.add(connection);
    }
}
