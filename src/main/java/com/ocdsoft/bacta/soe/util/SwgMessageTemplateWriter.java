package com.ocdsoft.bacta.soe.util;

import com.ocdsoft.bacta.soe.ServerType;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;

/**
 * Created by kburkhardt on 1/31/15.
 */
public class SwgMessageTemplateWriter {

    private static final Logger logger = LoggerFactory.getLogger(SwgMessageTemplateWriter.class);

    private final VelocityEngine ve;
    private final ServerType serverEnv;

    private final String controllerPath;
    private final String messagePath;

    public SwgMessageTemplateWriter(final ServerType serverEnv) {

        this.serverEnv = serverEnv;

        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, logger);
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");


        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "true");
        ve.init();

        controllerPath = System.getProperty("project.classpath") + ".controller." + serverEnv.name().toLowerCase() + ".server";
        messagePath = System.getProperty("project.classpath") + ".message." + serverEnv.name().toLowerCase() + ".server";
    }

    public void createFiles(int opcode, ByteBuffer buffer) {

        String messageName = ClientString.get(opcode);
        String messageClass = messagePath + "." + messageName;

        if (messageName.isEmpty() || messageName.equalsIgnoreCase("unknown")) {
            logger.error("Unknown message opcode: 0x" + Integer.toHexString(opcode));
            return;
        }

        writeMessage(messageName, buffer);
        writeController(messageName, messageClass);
    }

    public void deleteFiles(int opcode) {

    }

    private void writeController(String messageName, String messageClasspath) {

        String className = messageName + "Controller";

        Template t = ve.getTemplate("/templates/swgcontroller.vm");

        VelocityContext context = new VelocityContext();

        context.put("packageName", messagePath);
        context.put("messageClasspath", messageClasspath);
        context.put("serverType", "ServerType." + serverEnv);
        context.put("messageName", messageName);
        context.put("messageNameClass", messageName + ".class");
        context.put("className", className);

        /* lets render a template */
        String outFileName = System.getProperty("user.dir") + "/swg/src/main/java/com/ocdsoft/bacta/swg/server/" + serverEnv.getGroup() + "/controller/" + className + ".java";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFileName)));

            if (!ve.evaluate(context, writer, t.getName(), "")) {
                throw new Exception("Failed to convert the template into class.");
            }

            t.merge(context, writer);

            writer.flush();
            writer.close();
        } catch(Exception e) {
            logger.error("Unable to write controller", e);
        }
    }

    private void writeMessage(String messageName, ByteBuffer buffer)  {

        Template t = ve.getTemplate("/templates/swgmessage.vm");

        VelocityContext context = new VelocityContext();

        context.put("packageName", "com.ocdsoft.bacta.swg.server." + serverEnv.getGroup() + ".message");
        context.put("messageName", messageName);

        String messageStruct = SoeMessageUtil.makeMessageStruct(buffer);
        context.put("messageStruct", messageStruct);

        context.put("priority", "0x" + Integer.toHexString(buffer.getShort(6)));
        context.put("opcode", "0x" + Integer.toHexString(buffer.getInt(8)));

        /* lets render a template */

        String outFileName = System.getProperty("user.dir") + "/swg/src/main/java/com/ocdsoft/bacta/swg/server/" + serverEnv.getGroup() + "/message/" + messageName + ".java";
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
}
