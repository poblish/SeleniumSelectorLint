package org.hiatusuk.selectorLint.utils;

import java.util.UUID;

public class Uuids {

    public static boolean isUUID(String string) {
        try {
            UUID.fromString(string);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
