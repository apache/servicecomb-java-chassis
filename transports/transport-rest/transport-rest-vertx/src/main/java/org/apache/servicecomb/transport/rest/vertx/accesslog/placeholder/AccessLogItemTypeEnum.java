package org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder;

/**
 * record what kinds of access log item we support
 */
public enum AccessLogItemTypeEnum {
  TEXT_PLAIN,
  // %m, cs-method
  HTTP_METHOD,
  // %s, sc-status
  HTTP_STATUS,
  // %T
  DURATION_IN_SECOND,
  // %D
  DURATION_IN_MILLISECOND,
  // %h
  REMOTE_HOSTNAME,
  // %v
  LOCAL_HOSTNAME,
  // %p
  LOCAL_PORT,
  // %B
  RESPONSE_SIZE,
  // %b
  RESPONSE_SIZE_CLF,
  // %r
  FIRST_LINE_OF_REQUEST,
  // %U, cs-uri-stem
  URL_PATH,
  // %q, cs-uri-query
  QUERY_STRING,
  // cs-uri
  URL_PATH_WITH_QUERY,
  //%H
  REQUEST_PROTOCOL,
  // %t
  DATETIME_DEFAULT,
  // %{PATTERN}t, %{PATTERN|TIMEZONE|LOCALE}t
  DATETIME_CONFIGURABLE,
  // %{VARNAME}i
  REQUEST_HEADER,
  // %{VARNAME}o
  RESPONSE_HEADER,
  // %{VARNAME}C
  COOKIE,
  // %SCB-traceId
  SCB_TRACE_ID;
}
