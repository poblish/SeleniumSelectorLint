package org.hiatusuk.selectorLint;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.hiatusuk.selectorLint.handlers.*;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.common.base.Throwables;

public class Options {
    @SuppressWarnings("unused")
    private String name;

    @SuppressWarnings("unused")
    private boolean allowTextComparisons;

    private Map<String,Object> rules;
    private LinkedHashMap<String,Map<String,Object>> handlerOrdering;

    private final List<ElementHandler> generatedHandlers = new ArrayList<>();
    private final Rules namedFilters = new Rules();

    public static Options read(final String optsFilePath) {
        try {
            return read( new FileReader(optsFilePath) );
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
    
    private static Options read(final FileReader reader) throws IOException {
        final YamlConfig yc = new YamlConfig();
        yc.setPrivateFields(true);

        return new YamlReader(reader, yc).read( Options.class ).complete();
    }
    
    @SuppressWarnings("unchecked")
    private Options complete() {
        for (Entry<String,Map<String,Object>> handlerInfo : handlerOrdering.entrySet()) {
            // System.out.println("Handler '" + handlerInfo.getKey() + "': " + handlerInfo.getValue());

            ElementHandler handler;

            switch (handlerInfo.getKey()) {
                case "Tags":
                    List<String> x = (List<String>) handlerInfo.getValue().get("important");
                    
                    Object ignoreTagsObj = handlerInfo.getValue().get("ignore");

                    List<String> ignoreTags = ignoreTagsObj instanceof List ? (List<String>) ignoreTagsObj : Collections.<String>emptyList();
                    // System.out.println("Tag: use:" + x);
                    handler = new TagHandler( namedFilters, x, ignoreTags);
                    break;
                case "Id":
                    Object ignoreIdsObj = handlerInfo.getValue().get("ignore");
                    List<String> ignoreIds = ignoreIdsObj instanceof List ? (List<String>) ignoreIdsObj : Collections.<String>emptyList();
                    handler = new IdsHandler(this, ignoreIds);
                    break;
                case "Classes":
                    List<String> ignore = (List<String>) handlerInfo.getValue().get("ignore");
                    // System.out.println("Class: use:" + ignore);
                    handler = new ClassHandler( namedFilters, ignore, /* Min: */ Integer.parseInt((String) handlerInfo.getValue().get("minAcceptableClassLength") ) );
                    break;
                case "Attributes":
                    List<String> ignoreAttrs = (List<String>) handlerInfo.getValue().get("filterNames");
                    List<String> checkValue = (List<String>) handlerInfo.getValue().get("filterValues");
                    ignoreAttrs.add("id");
                    ignoreAttrs.add("class");
                    // System.out.println("Attrs: use:" + ignoreAttrs + " / " + checkValue);
                    handler = new AttributesHandler( namedFilters, ignoreAttrs, checkValue);
                    break;
                case "Input Values":
                    List<String> ignoreVals = (List<String>) handlerInfo.getValue().get("ignore");
                    handler = new InputValuesHandler( namedFilters, ignoreVals);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            generatedHandlers.add(handler);
        }
        
        for (Entry<String,Object> rule : rules.entrySet()) {
            Map<String,List<String>> elems = (Map<String, List<String>>) rule.getValue();
            FilterPredicate pred = new FilterPredicate(elems);
//            System.out.println("Rule '" + rule.getKey() + "': " + elems);

            namedFilters.put(rule.getKey(), pred);
        }

        System.out.println("=> rules: " + rules);

        return this;
    }

    public Rules getNamedFilters() {
        return namedFilters;
    }

//    public LinkedHashMap<String,Map<String,Object>> getHandlerOrdering() {
//        return handlerOrdering;
//    }

    public Iterable<ElementHandler> handlers() {
        return generatedHandlers;
    }

    public static class Rules extends HashMap<String,FilterPredicate> {
        
    }
}
