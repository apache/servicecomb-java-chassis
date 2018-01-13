/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.springframework.util.StringUtils;

/**
 * Configurable datetime element.
 */
public class DatetimeConfigurableElement implements AccessLogElement {

  public static final String DEFAULT_DATETIME_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";

  public static final Locale DEFAULT_LOCALE = Locale.US;

  private final ThreadLocal<SimpleDateFormat> datetimeFormatHolder = new ThreadLocal<>();

  private String pattern;

  private TimeZone timezone;

  private Locale locale;

  /**
   * all config is set to default value.
   */
  public DatetimeConfigurableElement() {
    this(DEFAULT_DATETIME_PATTERN);
  }

  /**
   * the configurations not specified will get a default value.
   * @param config the format of config is "PATTERN|TIMEZONE|LOCALE" or "PATTERN". It depends on whether the config contains the separator "|"
   */
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

  private String[] splitConfig(String config) {
    return config.split("\\|{1}?", -1);
  }

  private void setConfigruations(String[] configArr) {
    this.pattern = StringUtils.isEmpty(configArr[0]) ? DEFAULT_DATETIME_PATTERN : configArr[0];
    this.timezone = StringUtils.isEmpty(configArr[1]) ? TimeZone.getDefault() : TimeZone.getTimeZone(configArr[1]);
    this.locale = StringUtils.isEmpty(configArr[2]) ? DEFAULT_LOCALE : Locale.forLanguageTag(configArr[2]);
  }

  @Override
  public String getFormattedElement(AccessLogParam accessLogParam) {
    SimpleDateFormat dateFormat = getDatetimeFormat();
    return dateFormat.format(new Date(accessLogParam.getStartMillisecond()));
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
