package org.hiatusuk.selectorLint;

import org.hiatusuk.selectorLint.config.Options;
import org.testng.annotations.Test;

public class ConfigTest {

    @Test public void testConfig() {
        final Options opts = Options.read("options.yaml");

//        for (Entry<String,Object> rule : opts.getRules().entrySet()) {
//            System.out.println("Rule '" + rule.getKey() + "': " + rule.getValue());
//        }

        opts.handlers();
    }
}
