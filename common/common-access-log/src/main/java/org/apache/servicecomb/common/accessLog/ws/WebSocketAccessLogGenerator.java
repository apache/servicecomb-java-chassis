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

package org.apache.servicecomb.common.accessLog.ws;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.servicecomb.core.event.WebSocketActionEvent;

/**
 * Similar to {@link org.apache.servicecomb.common.accessLog.core.AccessLogGenerator},
 * this is an access log generator for WebSocket protocol.
 */
public class WebSocketAccessLogGenerator {

  public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  public static final Locale DEFAULT_LOCALE = Locale.US;

  public static final TimeZone TIME_ZONE = TimeZone.getDefault();

  private final ThreadLocal<SimpleDateFormat> datetimeFormatHolder = new ThreadLocal<>();

  public String generateServerLog(WebSocketActionEvent actionEvent) {
    return generateLog(actionEvent);
  }

  public String generateClientLog(WebSocketActionEvent actionEvent) {
    return generateLog(actionEvent);
  }

  private String generateLog(WebSocketActionEvent actionEvent) {
    return actionEvent.getInvocationType()
        + "|"
        + actionEvent.getOperationMeta().getMicroserviceQualifiedName()
        + "|"
        + formatTimestampToDateTimeStr(actionEvent.getActionStartTimestamp())
        + "|"
        + actionEvent.getTraceId()
        + "|"
        + actionEvent.getConnectionId()
        + "|"
        + actionEvent.getActionType()
        + "|"
        + (actionEvent.getActionStartTimestamp() - actionEvent.getScheduleStartTimestamp())
        + "|"
        + (actionEvent.getActionEndTimestamp() - actionEvent.getActionStartTimestamp())
        + "|"
        + actionEvent.getHandleThreadName()
        + "|"
        + actionEvent.getDataSize();
  }

  private String formatTimestampToDateTimeStr(long timestamp) {
    return getDatetimeFormat()
        .format(new Date(timestamp));
  }

  private SimpleDateFormat getDatetimeFormat() {
    SimpleDateFormat dateFormat = datetimeFormatHolder.get();
    if (null == dateFormat) {
      dateFormat = new SimpleDateFormat(DEFAULT_DATETIME_PATTERN, DEFAULT_LOCALE);
      dateFormat.setTimeZone(TIME_ZONE);

      datetimeFormatHolder.set(dateFormat);
    }

    return dateFormat;
  }
}
