package org.apache.servicecomb.foundation.log;

import com.netflix.config.DynamicPropertyFactory;

public class LogConfig {

    private static final String SERVER_BASE = "servicecomb.accesslog.";

    private static final String CLIENT_BASE = "servicecomb.outlog.";

    private static final String SERVER_LOG_ENABLED = SERVER_BASE + "enabled";

    private static final String SERVER_LOG_PATTERN = SERVER_BASE + "pattern";

    private static final String CLIENT_LOG_ENABLED = CLIENT_BASE + "enabled";

    private static final String CLIENT_LOG_PATTERN = CLIENT_BASE + "pattern";

    public static final String DEFAULT_PATTERN = "%h - - %t %r %s %B %D";

    public static final LogConfig INSTANCE = new LogConfig();

    private boolean serverLogEnabled;

    private boolean clientLogEnabled;

    private String serverLogPattern;

    private String clientLogPattern;

    private LogConfig() {
        init();
    }

    private void init() {
        clientLogEnabled = DynamicPropertyFactory
            .getInstance().getBooleanProperty(CLIENT_LOG_ENABLED, false).get();
        serverLogEnabled = DynamicPropertyFactory
            .getInstance().getBooleanProperty(SERVER_LOG_ENABLED, false).get();
        clientLogPattern = DynamicPropertyFactory
          .getInstance().getStringProperty(CLIENT_LOG_PATTERN, DEFAULT_PATTERN).get();
        serverLogPattern = DynamicPropertyFactory
            .getInstance().getStringProperty(SERVER_LOG_PATTERN, DEFAULT_PATTERN).get();
    }

    public boolean isServerLogEnabled() {
        return serverLogEnabled;
    }

    public boolean isClientLogEnabled() {
        return clientLogEnabled;
    }

    public String getServerLogPattern() {
        return serverLogPattern;
    }

    public String getClientLogPattern() {
        return clientLogPattern;
    }
}
