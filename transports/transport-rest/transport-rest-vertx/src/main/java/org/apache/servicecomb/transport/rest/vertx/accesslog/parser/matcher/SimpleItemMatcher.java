package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;

public class SimpleItemMatcher implements AccessLogItemMatcher {
  private static final Map<String, AccessLogItemTypeEnum> PLACEHOLDER_ENUM_MAP = new LinkedHashMap<String, AccessLogItemTypeEnum>();

  static {
    PLACEHOLDER_ENUM_MAP.put("%m", AccessLogItemTypeEnum.HTTP_METHOD);
    PLACEHOLDER_ENUM_MAP.put("cs-method", AccessLogItemTypeEnum.HTTP_METHOD);
    PLACEHOLDER_ENUM_MAP.put("%s", AccessLogItemTypeEnum.HTTP_STATUS);
    PLACEHOLDER_ENUM_MAP.put("sc-status", AccessLogItemTypeEnum.HTTP_STATUS);
    PLACEHOLDER_ENUM_MAP.put("%T", AccessLogItemTypeEnum.DURATION_IN_SECOND);
    PLACEHOLDER_ENUM_MAP.put("%D", AccessLogItemTypeEnum.DURATION_IN_MILLISECOND);
    PLACEHOLDER_ENUM_MAP.put("%h", AccessLogItemTypeEnum.REMOTE_HOSTNAME);
    PLACEHOLDER_ENUM_MAP.put("%v", AccessLogItemTypeEnum.LOCAL_HOSTNAME);
    PLACEHOLDER_ENUM_MAP.put("%p", AccessLogItemTypeEnum.LOCAL_PORT);
    PLACEHOLDER_ENUM_MAP.put("%B", AccessLogItemTypeEnum.RESPONSE_SIZE);
    PLACEHOLDER_ENUM_MAP.put("%b", AccessLogItemTypeEnum.RESPONSE_SIZE_CLF);
    PLACEHOLDER_ENUM_MAP.put("%r", AccessLogItemTypeEnum.FIRST_LINE_OF_REQUEST);
    PLACEHOLDER_ENUM_MAP.put("%U", AccessLogItemTypeEnum.URL_PATH);
    PLACEHOLDER_ENUM_MAP.put("cs-uri-stem", AccessLogItemTypeEnum.URL_PATH);
    PLACEHOLDER_ENUM_MAP.put("%q", AccessLogItemTypeEnum.QUERY_STRING);
    PLACEHOLDER_ENUM_MAP.put("cs-uri-query", AccessLogItemTypeEnum.QUERY_STRING);
    PLACEHOLDER_ENUM_MAP.put("cs-uri", AccessLogItemTypeEnum.URL_PATH_WITH_QUERY);
    PLACEHOLDER_ENUM_MAP.put("%H", AccessLogItemTypeEnum.REQUEST_PROTOCOL);
    PLACEHOLDER_ENUM_MAP.put("%t", AccessLogItemTypeEnum.DATETIME_DEFAULT);
    PLACEHOLDER_ENUM_MAP.put("%SCB-traceId", AccessLogItemTypeEnum.SCB_TRACE_ID);
  }

  @Override
  public AccessLogItemLocation match(String rawPattern, int offset) {
    int start = -1;
    Entry<String, AccessLogItemTypeEnum> nearestEntry = null;
    for (Entry<String, AccessLogItemTypeEnum> entry : PLACEHOLDER_ENUM_MAP.entrySet()) {
      int cursor = rawPattern.indexOf(entry.getKey(), offset);
      if (cursor < 0) {
        continue;
      }
      if (start < 0 || cursor < start) {
        start = cursor;
        nearestEntry = entry;
      }
    }

    if (null == nearestEntry) {
      return null;
    }

    return new AccessLogItemLocation().setStart(start).setEnd(start + nearestEntry.getKey().length())
        .setPlaceHolder(nearestEntry.getValue());
  }
}
