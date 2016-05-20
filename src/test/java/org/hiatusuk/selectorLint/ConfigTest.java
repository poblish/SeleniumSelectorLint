package org.hiatusuk.selectorLint;

import java.io.IOException;

import org.testng.annotations.Test;

public class ConfigTest {

    @Test public void testConfig() throws IOException {
        final Options opts = Options.read("options.yaml");

//        for (Entry<String,Object> rule : opts.getRules().entrySet()) {
//            System.out.println("Rule '" + rule.getKey() + "': " + rule.getValue());
//        }

        opts.handlers();
    }
}
