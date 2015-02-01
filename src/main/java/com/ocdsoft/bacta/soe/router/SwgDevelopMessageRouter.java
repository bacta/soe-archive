package com.ocdsoft.bacta.soe.router;

import com.google.inject.Injector;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.SwgController;
import com.ocdsoft.bacta.soe.SwgMessageController;
import com.ocdsoft.bacta.soe.annotation.RolesAllowed;
import com.ocdsoft.bacta.soe.connection.ConnectionRole;
import com.ocdsoft.bacta.soe.connection.SoeUdpConnection;
import com.ocdsoft.bacta.soe.message.GameNetworkMessage;
import com.ocdsoft.bacta.soe.message.ReliableNetworkMessage;
import com.ocdsoft.bacta.soe.util.ClientString;
import com.ocdsoft.bacta.soe.util.SOECRC32;
import com.ocdsoft.bacta.soe.util.SoeMessageUtil;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SwgDevelopMessageRouter<Connection extends SoeUdpConnection> implements SwgMessageRouter<Connection> {
    private static final Logger logger = LoggerFactory.getLogger(SwgDevelopMessageRouter.class);

    private VelocityEngine ve = null;
    private final boolean developmentMode;

    private final TIntObjectMap<ControllerData> controllers = new TIntObjectHashMap<>();

    private final Map<String, String> existingControllerMap = new HashMap<>();
    private final List<String> existingControllers = new ArrayList<>();
    private final List<Class<? extends ReliableNetworkMessage>> existingMessages = new ArrayList<>();

    private final ServerType serverEnv;

    public SwgDevelopMessageRouter(final Injector injector,
                                   final ServerState serverState,
                                   final String controllerFileName,
                                   final boolean developmentMode) {

        this.serverEnv = serverState.getServerType();
        this.developmentMode = developmentMode;

        loadControllers(injector, controllerFileName);
    }

    @Override
    public void routeMessage(byte priority, int opcode, Connection connection, ByteBuffer buffer) {

        ControllerData controllerData = controllers.get(opcode);
        if(controllerData != null) {
            if(!hasControllerAccess(connection, controllerData)) {
                logger.error("Controller security blocked access:" + controllerData.getSwgMessageController().getClass().getName());
                logger.error("Connection: " + connection.toString());
                return;
            }

            SwgMessageController controller = controllerData.getSwgMessageController();
            Constructor<? extends GameNetworkMessage> constructor = controllerData.getConstructor();
            try {

                GameNetworkMessage message = constructor.newInstance(buffer);

                try {

                    logger.debug("Routing to " + controller.getClass().getSimpleName());

                    controller.handleIncoming(connection, message);

                } catch (Exception e) {
                    logger.error("SWG Message Handling", e);
                }


            } catch (Exception e) {
                logger.error("Unable to create incoming message", e);
            }
        } else {
            handleMissingController(opcode, buffer);
        }
    }

    private boolean hasControllerAccess(Connection connection, ControllerData controllerData) {
        return controllerData.containsRoles(connection.getRoles());
    }

    private void handleMissingController(int opcode, ByteBuffer buffer) {

        if(developmentMode) {
            writeTemplates(opcode, buffer);
        }

        String propertyName = Integer.toHexString(opcode);

        logger.error("Unhandled SWG Message: '" + ClientString.get(propertyName) + "' 0x" + propertyName);
        logger.error(SoeMessageUtil.bytesToHex(buffer));
    }

    private void loadControllers(final Injector injector, final String controllerFileName) {

        File file = new File("../conf/" + controllerFileName);
        if(!file.exists()) {
            file = new File(getClass().getResource("/" + controllerFileName).getFile());
        }

        List<String> classNameList;
        try {
            classNameList = Files.readAllLines(Paths.get(file.toURI()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for(String className : classNameList) {

            try {
                Class<? extends SwgMessageController> controllerClass = (Class<? extends SwgMessageController>) Class.forName(className);;

                synchronized (existingControllers) {
                    existingControllers.add(controllerClass.getSimpleName());
                }

                SwgController controllerAnnotation = controllerClass.getAnnotation(SwgController.class);

                if (controllerAnnotation == null) {
                    logger.warn("Missing @SwgController annotation, discarding: " + controllerClass.getName());
                    continue;
                }

                Class<?> handledMessageClass = controllerAnnotation.handles();

                if (handledMessageClass != null) {
                    synchronized (existingControllerMap) {
                        existingControllerMap.put(handledMessageClass.getSimpleName(), controllerClass.getSimpleName());
                    }
                }

                boolean match = false;

                for (ServerType server : controllerAnnotation.server()) {
                    if (server == serverEnv) {
                        match = true;
                    }
                }

                if (!match) {
                    continue;
                }


                RolesAllowed rolesAllowed = controllerClass.getAnnotation(RolesAllowed.class);
                if(rolesAllowed == null) {
                    logger.warn("Missing @RolesAllowed annotation, discarding: " + controllerClass.getName());
                    continue;
                }

                ConnectionRole[] connectionRoles = rolesAllowed.value();
                SwgMessageController controller = injector.getInstance(controllerClass);

                int hash = SOECRC32.hashCode(handledMessageClass.getSimpleName());
                Constructor constructor = handledMessageClass.getConstructor(ByteBuffer.class);

                ControllerData newControllerData = new ControllerData(controller, constructor, connectionRoles);

                if (!controllers.containsKey(hash)) {
                    String propertyName = Integer.toHexString(hash);
                    logger.debug("Adding Controller for " + serverEnv + ": " + controllerClass.getName() + " " + ClientString.get(propertyName) + "' 0x" + propertyName);

                    synchronized (controllers) {
                        controllers.put(hash, newControllerData);
                    }
                }
            } catch (Exception e) {
                logger.error("Unable to add controller: " + className, e);
            }
        }
    }

    private class ControllerData {
        @Getter
        private final SwgMessageController swgMessageController;

        @Getter
        private final Constructor constructor;

        @Getter
        private final ConnectionRole[] roles;

        public ControllerData(final SwgMessageController swgMessageController,
                              final Constructor constructor,
                              final ConnectionRole[] roles) {
            this.swgMessageController = swgMessageController;
            this.constructor = constructor;
            this.roles = roles;

        }

        public boolean containsRoles(List<ConnectionRole> userRoles) {
            for(ConnectionRole role : roles) {
                if(userRoles.contains(role)) {
                    return true;
                }
            }

            return false;
        }
    }

    private void writeTemplates(int opcode, ByteBuffer buffer) {

        initializeTemplating();

        String messageName = ClientString.get(opcode);
        String messageClass = "";

        if (messageName.isEmpty() || messageName.equalsIgnoreCase("unknown")) {
            logger.error("Unknown message opcode: 0x" + Integer.toHexString(opcode));
            return;
        }

        boolean controllerExists = existingControllerMap.containsKey(messageName);

        boolean messageExists = false;
        for (Class<? extends ReliableNetworkMessage> existingMessage : existingMessages) {
            if (existingMessage.getSimpleName().equalsIgnoreCase(messageName)) {
                messageExists = true;
                messageClass = existingMessage.getName();
                break;
            }
        }

        if (!messageExists) {

            try {
                writeMessage(messageName, buffer);
                messageClass = "com.ocdsoft.bacta.swg.server." + serverEnv.getGroup() + ".message." + messageName;
            } catch (Exception e) {
                logger.error("Unable to write message", e);
            }
        }

        if (!controllerExists) {
            try {
                writeController(messageName, messageClass);
            } catch (Exception e) {
                logger.error("Unable to write controller", e);
            }
        }
    }

    private void writeController(String messageName, String messageClasspath) throws Exception {

        String className = messageName + "Controller";

        Template t = ve.getTemplate("swg/src/main/resources/templates/swgcontroller.vm");

        VelocityContext context = new VelocityContext();

        context.put("packageName", "com.ocdsoft.bacta.swg.server." + serverEnv.getGroup() + ".controller");
        context.put("messageClasspath", messageClasspath);
        context.put("serverType", "ServerType." + serverEnv);
        context.put("messageName", messageName);
        context.put("messageNameClass", messageName + ".class");
        context.put("className", className);

        /* lets render a template */
        String outFileName = System.getProperty("user.dir") + "/swg/src/main/java/com/ocdsoft/bacta/swg/server/" + serverEnv.getGroup() + "/controller/" + className + ".java";
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFileName)));

        if (!ve.evaluate(context, writer, t.getName(), "")) {
            throw new Exception("Failed to convert the template into class.");
        }

        t.merge(context, writer);

        writer.flush();
        writer.close();
    }

    private void writeMessage(String messageName, ByteBuffer buffer) throws Exception {

        Template t = ve.getTemplate("swg/src/main/resources/templates/swgmessage.vm");

        VelocityContext context = new VelocityContext();

        context.put("packageName", "com.ocdsoft.bacta.swg.server." + serverEnv.getGroup() + ".message");
        context.put("messageName", messageName);

        String messageStruct = SoeMessageUtil.makeMessageStruct(buffer);
        context.put("messageStruct", messageStruct);

        context.put("priority", "0x" + Integer.toHexString(buffer.getShort(6)));
        context.put("opcode", "0x" + Integer.toHexString(buffer.getInt(8)));

        /* lets render a template */

        String outFileName = System.getProperty("user.dir") + "/swg/src/main/java/com/ocdsoft/bacta/swg/server/" + serverEnv.getGroup() + "/message/" + messageName + ".java";
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFileName)));

        if (!ve.evaluate(context, writer, t.getName(), "")) {
            throw new Exception("Failed to convert the template into class.");
        }

        t.merge(context, writer);

        writer.flush();
        writer.close();

    }

    private void initializeTemplating() {
        synchronized (controllers) {
            if (ve == null) {
                ve = new VelocityEngine();
                ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, logger);
                ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");


                ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "true");
                ve.init();
            }
        }
    }
}
