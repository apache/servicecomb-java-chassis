package org.apache.servicecomb.transport.rest.vertx.accesslog.parser.matcher;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;

public class SimpleItemMatcher implements AccessLogItemMatcher {
  private static final Map<String, AccessLogItemTypeEnum> placeholderEnumMap = new LinkedHashMap<String, AccessLogItemTypeEnum>();

  static {
    placeholderEnumMap.put("%m", AccessLogItemTypeEnum.HTTP_METHOD);
    placeholderEnumMap.put("cs-method", AccessLogItemTypeEnum.HTTP_METHOD);
    placeholderEnumMap.put("%s", AccessLogItemTypeEnum.HTTP_STATUS);
    placeholderEnumMap.put("sc-status", AccessLogItemTypeEnum.HTTP_STATUS);
    placeholderEnumMap.put("%T", AccessLogItemTypeEnum.DURATION_IN_SECOND);
    placeholderEnumMap.put("%D", AccessLogItemTypeEnum.DURATION_IN_MILLISECOND);
    placeholderEnumMap.put("%h", AccessLogItemTypeEnum.REMOTE_HOSTNAME);
    placeholderEnumMap.put("%v", AccessLogItemTypeEnum.LOCAL_HOSTNAME);
    placeholderEnumMap.put("%p", AccessLogItemTypeEnum.LOCAL_PORT);
    placeholderEnumMap.put("%B", AccessLogItemTypeEnum.RESPONSE_SIZE);
    placeholderEnumMap.put("%b", AccessLogItemTypeEnum.RESPONSE_SIZE_CLF);
    placeholderEnumMap.put("%r", AccessLogItemTypeEnum.FIRST_LINE_OF_REQUEST);
    placeholderEnumMap.put("%U", AccessLogItemTypeEnum.URL_PATH);
    placeholderEnumMap.put("cs-uri-stem", AccessLogItemTypeEnum.URL_PATH);
    placeholderEnumMap.put("%q", AccessLogItemTypeEnum.QUERY_STRING);
    placeholderEnumMap.put("cs-uri-query", AccessLogItemTypeEnum.QUERY_STRING);
    placeholderEnumMap.put("cs-uri", AccessLogItemTypeEnum.URL_PATH_WITH_QUERY);
    placeholderEnumMap.put("%H", AccessLogItemTypeEnum.REQUEST_PROTOCOL);
    placeholderEnumMap.put("%t", AccessLogItemTypeEnum.DATETIME_DEFAULT);
    placeholderEnumMap.put("%SCB-traceId", AccessLogItemTypeEnum.SCB_TRACE_ID);
  }

  @Override
  public AccessLogItemLocation match(String rawPattern, int offset) {
    for (Entry<String, AccessLogItemTypeEnum> entry : placeholderEnumMap.entrySet()) {
      if (rawPattern.startsWith(entry.getKey(), offset)) {
        return new AccessLogItemLocation().setStart(offset).setEnd(offset + entry.getKey().length())
            .setPlaceHolder(entry.getValue());
      }
    }

    return null;
  }
}
