package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.util.StringUtils;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;

public class DatetimeConfigurableElement implements AccessLogElement {
  private String pattern;

  private TimeZone timezone;

  private Locale locale;

  private final ThreadLocal<SimpleDateFormat> datetimeFormatHolder = new ThreadLocal<>();

  public DatetimeConfigurableElement() {
    this("||");
  }

  public DatetimeConfigurableElement(String config) {
    String[] configArr = null;
    if (config.contains("|")) {
      configArr = splitConfig(config);
    } else {
      // if there is no seperator "|", regard config as pattern.
      configArr = new String[3];
      configArr[0] = config;
    }
    if (3 != configArr.length) {
      throw new IllegalArgumentException(
          "wrong format of configuration, \"PATTERN|TIMEZONE|LOCALE\" is expected, but actually is \"" + config + "\"");
    }

    setConfigruations(configArr);
  }

  protected String[] splitConfig(String config) {
    return config.split("\\|{1}?", -1);
  }

  private void setConfigruations(String[] configArr) {
    this.pattern = StringUtils.isEmpty(configArr[0]) ? "EEE, dd MMM yyyy HH:mm:ss zzz" : configArr[0];
    this.timezone = StringUtils.isEmpty(configArr[1]) ? TimeZone.getDefault() : TimeZone.getTimeZone(configArr[1]);
    this.locale = StringUtils.isEmpty(configArr[2]) ? Locale.US : Locale.forLanguageTag(configArr[2]);
  }

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    SimpleDateFormat dateFormat = getDatetimeFormat();
    String datetime = dateFormat.format(new Date(accessLogParam.getStartMillisecond()));
    return datetime;
  }

  private SimpleDateFormat getDatetimeFormat() {
    SimpleDateFormat dateFormat = datetimeFormatHolder.get();
    if (null == dateFormat) {
      dateFormat = new SimpleDateFormat(pattern, locale);
      dateFormat.setTimeZone(timezone);

      datetimeFormatHolder.set(dateFormat);
    }

    return dateFormat;
  }

  public String getPattern() {
    return pattern;
  }

  public TimeZone getTimezone() {
    return timezone;
  }

  public Locale getLocale() {
    return locale;
  }
}
