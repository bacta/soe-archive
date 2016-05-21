package com.ocdsoft.bacta.soe.dispatch;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.connection.ConnectionRole;
import com.ocdsoft.bacta.soe.controller.ConnectionRolesAllowed;
import com.ocdsoft.bacta.soe.controller.MessageHandled;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.serialize.GameNetworkMessageSerializer;
import com.ocdsoft.bacta.soe.util.ClientString;
import com.ocdsoft.bacta.soe.util.SOECRC32;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by kyle on 4/22/2016.
 */
public final class ClasspathControllerLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathControllerLoader.class);

    private final GameNetworkMessageSerializer gameNetworkMessageSerializer;
    private final Injector injector;
    private final ServerState serverState;

    @Inject
    public ClasspathControllerLoader(final Injector injector,
                                     final GameNetworkMessageSerializer gameNetworkMessageSerializer,
                                     final ServerState serverState) {

        this.injector = injector;
        this.gameNetworkMessageSerializer = gameNetworkMessageSerializer;
        this.serverState = serverState;
    }

    public <T> TIntObjectMap<ControllerData> getControllers(Class<T> clazz) {

        TIntObjectMap<ControllerData> controllers = new TIntObjectHashMap<>();

        Reflections reflections = new Reflections();
        Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(clazz);
        Iterator<Class<? extends T>> iter = subTypes.iterator();

        while (iter.hasNext()) {

            Class<? extends T> controllerClass = iter.next();
            loadControllerClass(controllers, injector, controllerClass, serverState);
        }

        return controllers;
    }

    private <T> void loadControllerClass(final TIntObjectMap<ControllerData> controllers,
                                     final Injector injector,
                                     Class<? extends T> controllerClass,
                                     final ServerState serverState) {

        try {

            if (Modifier.isAbstract(controllerClass.getModifiers())) {
                return;
            }

            MessageHandled controllerAnnotation = controllerClass.getAnnotation(MessageHandled.class);

            if (controllerAnnotation == null) {
                LOGGER.warn("Missing @MessageHandled annotation, discarding: " + controllerClass.getName());
                return;
            }

            ConnectionRolesAllowed connectionRolesAllowed = controllerClass.getAnnotation(ConnectionRolesAllowed.class);
            ConnectionRole[] connectionRoles;
            if (connectionRolesAllowed == null) {
                connectionRoles = new ConnectionRole[]{ConnectionRole.AUTHENTICATED};
            } else {
                connectionRoles = connectionRolesAllowed.value();
            }

            Class<? extends GameNetworkMessage> handledMessageClass = (Class<? extends GameNetworkMessage>) controllerAnnotation.handles();

            int controllerId;

            MessageCRC messageCRC = handledMessageClass.getAnnotation(MessageCRC.class);
            if(messageCRC != null) {
                controllerId = messageCRC.value();
            } else if(!controllerAnnotation.command().isEmpty()) {
                controllerId = SOECRC32.hashCode(controllerAnnotation.command().toLowerCase());
            } else {
                controllerId = SOECRC32.hashCode(handledMessageClass.getSimpleName());
            }

            if (controllerAnnotation.id() != -1) {
                controllerId = controllerAnnotation.id();
            }

            gameNetworkMessageSerializer.addHandledMessageClass(controllerId, handledMessageClass);
            List<ServerType> serverTypes = new ArrayList<>();
            for (ServerType serverType : controllerAnnotation.type()) {
                serverTypes.add(serverType);
            }

            String propertyName = Integer.toHexString(controllerId);

            if (serverTypes.contains(serverState.getServerType())) {

                T controller = injector.getInstance(controllerClass);

                ControllerData<T> newControllerData = new ControllerData(controller, connectionRoles);

                if (!controllers.containsKey(controllerId)) {
                    LOGGER.debug("{} Adding Controller {} '{}' 0x{}", serverState.getServerType().name(), controllerClass.getName(), ClientString.get(propertyName), propertyName);
                    controllers.put(controllerId, newControllerData);
                } else {
                    LOGGER.error("{} Duplicate Controller {} '{}' 0x{}", serverState.getServerType().name(), controllerClass.getName(), ClientString.get(propertyName), propertyName);

                }

            } else {
                LOGGER.debug("{} Ignoring Controller {} '{}' 0x{}", serverState.getServerType().name(), controllerClass.getName(), ClientString.get(propertyName), propertyName);
            }

        } catch (Throwable e) {
            LOGGER.error("Unable to add controller: " + controllerClass.getName(), e);
        }
    }
}
