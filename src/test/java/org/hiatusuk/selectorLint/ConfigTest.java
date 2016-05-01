package org.hiatusuk.selectorLint;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hiatusuk.selectorLint.handlers.*;
import org.testng.annotations.Test;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlReader;

public class ConfigTest {

    @Test public void testConfig() throws IOException {
        final YamlConfig yc = new YamlConfig();
        yc.setPrivateFields(true);

        final YamlReader reader = new YamlReader(new FileReader("options.yaml"), yc);
        final Options opts = reader.read( Options.class );

        for (Entry<String,Object> rule : opts.rules.entrySet()) {
            System.out.println("Rule '" + rule.getKey() + "': " + rule.getValue());
        }

        for (Entry<String,Map<String,Object>> handlerInfo : opts.handlerOrdering.entrySet()) {
            System.out.println("Handler '" + handlerInfo.getKey() + "': " + handlerInfo.getValue());

            ElementHandler handler;

            switch (handlerInfo.getKey()) {
                case "Tags":
                    List<String> x = (List<String>) handlerInfo.getValue().get("important");
                    handler = new TagHandler( x.toArray( new String[x.size()] ) );
                    break;
                case "Id":
                    handler = new IdsHandler();
                    break;
                case "Classes":
                    List<String> ignore = (List<String>) handlerInfo.getValue().get("ignore");
                    handler = new ClassHandler( ignore, /* Min: */ Integer.parseInt((String) handlerInfo.getValue().get("minAcceptableClassLength") ) );
                    break;
                case "Attributes":
                    List<String> ignoreAttrs = (List<String>) handlerInfo.getValue().get("ignore");
                    List<String> checkValue = (List<String>) handlerInfo.getValue().get("checkValue");
                    handler = new AttributesHandler( ignoreAttrs, checkValue);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    public static class Options {
        @SuppressWarnings("unused")
        private String name;

        @SuppressWarnings("unused")
        private boolean allowTextComparisons;

        private Map<String,Object> rules;
        private LinkedHashMap<String,Map<String,Object>> handlerOrdering;
    }
}
