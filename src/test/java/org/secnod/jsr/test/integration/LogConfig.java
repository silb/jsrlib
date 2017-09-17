package org.secnod.jsr.test.integration;

class LogConfig {
    static void configure() {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
        System.setProperty("org.slf4j.simpleLogger.log.org.secnod", "trace");
    }
}
