package com.ocdsoft.bacta.soe.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import org.jsonschema2pojo.AbstractAnnotator;

/**
 * Created by kburkhardt on 12/29/14.
 */
public class SoeAnnotator extends AbstractAnnotator {

    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {

        JsonNode unicodeDiscovery = propertyNode.get("isUnicode");

        if (unicodeDiscovery != null) {
            if(unicodeDiscovery.asBoolean()) {
                //field.annotate(UnicodeString.class);
            }
        }
    }
}
