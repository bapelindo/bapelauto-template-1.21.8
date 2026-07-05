// ============================================
// FILE: Log.java
// Path: src/main/java/com/bapelauto/util/Log.java
//
// Centralized logging for the mod. Replaces scattered System.out/err calls
// with a single SLF4J logger (the same logging backend Minecraft itself
// uses internally via com.mojang.logging.LogUtils, so no extra dependency
// is needed) tagged with the mod id, at proper levels so problems are
// actually visible/filterable in the log instead of mixed into stdout noise.
// ============================================
package com.bapelauto.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Log {
    private static final Logger LOGGER = LoggerFactory.getLogger("bapelauto");

    private Log() {}

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void warn(String message, Throwable t) {
        LOGGER.warn(message, t);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void error(String message, Throwable t) {
        LOGGER.error(message, t);
    }
}
