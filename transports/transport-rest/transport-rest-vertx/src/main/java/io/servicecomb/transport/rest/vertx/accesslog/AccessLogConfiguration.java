package io.servicecomb.transport.rest.vertx.accesslog;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.core.BootListener.BootEvent;

public final class AccessLogConfiguration {
  private static final String BASE = "cse.accesslog.";

  private static final String ACCESSLOG_ENABLED = BASE + "enabled";

  private static final String ACCESSLOG_PATTERN = BASE + "pattern";

  public static final AccessLogConfiguration INSTANCE = new AccessLogConfiguration();

  private AccessLogConfiguration() {

  }

  public boolean getAccessLogEnabled() {
    String enabled = getProperty("false", ACCESSLOG_ENABLED);
    return Boolean.parseBoolean(enabled);
  }

  public String getAccesslogPattern() {
    String pattern = getProperty("%h - - %t %r %s %B", ACCESSLOG_PATTERN);
    return pattern;
  }

  private String getProperty(String defaultValue, String key) {
    String property = DynamicPropertyFactory.getInstance().getStringProperty(key, defaultValue).get();
    if (null == property) {
      return defaultValue;
    } else {
      return property;
    }
  }
}
