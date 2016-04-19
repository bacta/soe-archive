package com.ocdsoft.bacta.soe.util;

import com.google.inject.Inject;
import com.ocdsoft.bacta.engine.conf.BactaConfiguration;
import com.ocdsoft.bacta.soe.ServerState;
import com.ocdsoft.bacta.soe.ServerType;
import com.ocdsoft.bacta.soe.io.udp.NetworkConfiguration;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by kburkhardt on 1/31/15.
 */
public class GameNetworkMessageTemplateWriter {

    private static final Logger logger = LoggerFactory.getLogger(GameNetworkMessageTemplateWriter.class);

    private final VelocityEngine ve;
    private final ServerType serverEnv;

    private final String controllerClassPath;
    private final String controllerFilePath;

    private final String messageFilePath;
    private final String messageClassPath;

    @Inject
    public GameNetworkMessageTemplateWriter(final NetworkConfiguration configuration, final ServerState serverState) {

        this.serverEnv = serverState.getServerType();

        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, logger);
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "true");
        ve.init();

        controllerClassPath = configuration.getBasePackage() + ".controller." + serverEnv.name().toLowerCase();
        String fs = System.getProperty("file.separator");

        controllerFilePath = System.getProperty("user.dir") + fs + System.getProperty("bacta.serverPath") + "src"
                + fs + "main" + fs + "java" + fs +
                configuration.getBasePackage().replace(".", fs) + fs + "controller" + fs +
                serverEnv.name().toLowerCase() + fs;
        
        messageClassPath = configuration.getBasePackage() + ".message." + serverEnv.name().toLowerCase();
        messageFilePath = System.getProperty("user.dir") + fs + System.getProperty("bacta.serverPath") + "src"
                + fs + "main" + fs + "java" + fs +
                configuration.getBasePackage().replace(".", fs) + fs + "message" + fs +
                serverEnv.name().toLowerCase() + fs;
    }

    public void createFiles(int opcode, ByteBuffer buffer) {

        String messageName = ClientString.get(opcode);
        
        if (messageName.isEmpty() || messageName.equalsIgnoreCase("unknown")) {
            logger.error("Unknown message opcode: 0x" + Integer.toHexString(opcode));
            return;
        }

        writeMessage(messageName, buffer);
        writeController(messageName);
    }

    private void writeMessage(String messageName, ByteBuffer buffer)  {

        String outFileName = messageFilePath + messageName + ".java";
        File file = new File(outFileName);
        if(file.exists()) {
            logger.info("'" + messageName + "' already exists");
            return;
        }
        
        Template t = ve.getTemplate("/template/GameNetworkMessage.vm");

        VelocityContext context = new VelocityContext();

        context.put("packageName", messageClassPath);
        context.put("messageName", messageName);

        String messageStruct = SoeMessageUtil.makeMessageStruct(buffer);
        context.put("messageStruct", messageStruct);

        context.put("priority", "0x" + Integer.toHexString(buffer.getShort(4)));
        context.put("opcode", "0x" + Integer.toHexString(buffer.getInt(6)));
        /* lets render a template */

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFileName)));

            if (!ve.evaluate(context, writer, t.getName(), "")) {
                throw new Exception("Failed to convert the template into class.");
            }

            t.merge(context, writer);

            writer.flush();
            writer.close();
        } catch(Exception e) {
            logger.error("Unable to write message", e);
        }

    }
    
    private void writeController(String messageName) {

        String className = messageName + "Controller";
        
        String outFileName = controllerFilePath + className + ".java";
        File file = new File(outFileName);
        if(file.exists()) {
            logger.info("'" + className + "' already exists");
            return;
        }

        Template t = ve.getTemplate("/template/GameNetworkController.vm");

        VelocityContext context = new VelocityContext();

        context.put("packageName", controllerClassPath);
        context.put("messageClasspath", messageClassPath);
        context.put("serverType", "ServerType." + serverEnv);
        context.put("messageName", messageName);
        context.put("messageNameClass", messageName + ".class");
        context.put("className", className);

        /* lets render a template */
        
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFileName)));

            if (!ve.evaluate(context, writer, t.getName(), "")) {
                throw new Exception("Failed to convert the template into class.");
            }

            t.merge(context, writer);

            writer.flush();
            writer.close();

//            BufferedWriter controllerFileWriter = new BufferedWriter(new FileWriter(new File(controllerFile), true));
//            controllerFileWriter.append(controllerClassPath + "." + className);
//            controllerFileWriter.newLine();
//
//            controllerFileWriter.flush();
//            controllerFileWriter.close();
//
        } catch(Exception e) {
            logger.error("Unable to write controller", e);
        }
    }


}
